package com.minecolonies.core.entity.ai.workers.production.herders;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.minecolonies.api.colony.IAnimalData;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.Stack;
import com.minecolonies.api.colony.requestsystem.requestable.StackList;
import com.minecolonies.api.entity.ai.JobStatus;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.VisibleCitizenStatus;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.StatsUtil;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.colony.buildings.modules.AnimalHerdingModule;
import com.minecolonies.core.colony.buildings.modules.BuildingModules;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingStable;
import com.minecolonies.core.colony.jobs.JobStablemaster;
import com.minecolonies.core.entity.other.cavalry.CavalryHorseEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.DECIDE;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.HERDER_GATHER_MOUNTS;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.HERDER_TRAIN;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.PREPARING;
import static com.minecolonies.api.util.constant.StatisticsConstants.HORSES_TRAINED;
import static com.minecolonies.api.util.constant.StatisticsConstants.MOUNTS_READIED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ROUNDUPS_COMPLETED;
import static com.minecolonies.api.util.constant.StatisticsConstants.ITEM_USED;
import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.HERDER_READY_FOR_COMBAT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class EntityAIWorkStablemaster extends AbstractEntityAIHerder<JobStablemaster, BuildingStable>
{
    public enum MountMaintenance
    {
        FEEDING,
        READYING
    }

    protected static final double TRAINING_CHANCE = .25;
    protected static final double READY_MOUNT_FOR_COMBAT_CHANCE = .40;
    protected static final double ROUND_UP_CHANCE = .15;

    protected static final float BASE_COMBAT_READINESS_RECOVERY = 4.0f;
    
    protected static final int MAX_ROUNDUP_COOLDOWN = 100;
    protected int roundupCooldown = MAX_ROUNDUP_COOLDOWN;

    protected static final int MAX_TRAINING_COOLDOWN = 50;
    protected int trainingCooldown = MAX_TRAINING_COOLDOWN;

    protected AbstractHorse horseToConvert = null;
    protected CavalryHorseEntity horseToGetReady = null;
    protected AbstractHorse horseToRetrieve = null;

    protected static final int RECOVERY_SKILL_PAR = 20;

    protected List<AbstractHorse> wanderingHorses = Collections.emptyList();

    /**
     * Get horse icon
     */
    private final static VisibleCitizenStatus FIND_HORSE =
      new VisibleCitizenStatus(new ResourceLocation(Constants.MOD_ID, "textures/icons/work/stablemaster.png"), "com.minecolonies.gui.visiblestatus.stablemaster");

    /**
     * Creates the abstract part of the AI. Always use this constructor!
     *
     * @param job the job to fulfill
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public EntityAIWorkStablemaster(@NotNull final JobStablemaster job)
    {
        super(job);
        super.registerTargets(
          new AITarget(HERDER_TRAIN, this::convertMount, 10),
          new AITarget(HERDER_READY_FOR_COMBAT, this::readyMountForCombat, 10),
          new AITarget(HERDER_GATHER_MOUNTS, this::gatherMounts, 10)
        );
    }

    @Override
    public Class<BuildingStable> getExpectedBuildingClass()
    {
        return BuildingStable.class;
    }

    /**
     * Ticks the ai state.
     * <p>
     * If the current state is not gathering mounts, and there is a horse to retrieve that is leashed to the worker, then the horse is unleased.
     * This prevents the horses from being dragged to bed when the citizen sleeps...
     */
    @Override
    public void tick()
    {
        super.tick();

        if (getState() != HERDER_GATHER_MOUNTS)
        {
            if (horseToRetrieve != null) 
            {
                Entity holder = horseToRetrieve.getLeashHolder();
                if (holder != null && holder.equals(worker)) 
                {
                    horseToRetrieve.dropLeash(true, false);
                }
            }
        }
    }

    /**
     * Get the extra tools needed for this job.
     *
     * @return a list of tools or empty.
     */
    @NotNull
    public List<EquipmentTypeEntry> getExtraToolsNeeded()
    {
        final List<EquipmentTypeEntry> toolsNeeded = new ArrayList<>();
        toolsNeeded.add(ModEquipmentTypes.lead.get());
        return toolsNeeded;
    }
    
    /**
     * Ensures that we have a appropriate tool available. Will set {@code needsTool} accordingly.
     * The Lead is level-less and so we need to override this here with such that a level is not checked.
     *
     * @param toolType type of tool we check for.
     * @return false if we have the tool
     */
    public boolean checkForToolOrWeapon(@NotNull final EquipmentTypeEntry toolType)
    {
        final boolean needTool = checkForToolOrWeapon(toolType, -1);
        if (needTool)
        {
            worker.getCitizenData().setJobStatus(JobStatus.STUCK);
        }
        else
        {
            worker.getCitizenData().setJobStatus(JobStatus.WORKING);
        }
        return needTool;
    }

    /**
     * This method is called when the AI wants to breed animals. It sets the current status of the citizen to FIND_HORSE.
     * <p>
     * This status is used to display the horse icon above the citizens head.
     */
    @Override
    protected IAIState breedAnimals()
    {
        worker.getCitizenData().setVisibleStatus(FIND_HORSE);
        return super.breedAnimals();
    }

    /**
     * Whether or not the stable master should feed adults to breed children.
     *
     * @return true if so.
     */
    @Override
    protected boolean canBreedChildren()
    {
        boolean breedSetting = super.canBreedChildren();

        if (!breedSetting)
        {
            return false;
        }

        final int limit = Math.max(0, building.getBuildingLevel() * 2);
        final int current = countCurrentMounts();

        if (current >= limit)
        {
            // Stable at capacity with steeds. Don't breed new horses to train.
            return false;
        }

        return true;
    }

    /**
     * Returns the chance to butcher an animal in the list of all animals in the building.
     * <p>
     * The chance is calculated based on the number of animals in the building and the max allowed.
     * <p>
     *
     * @param allAnimals the list of all animals in the building.
     * @return the chance to butcher an animal in the list of all animals in the building.
     */
    @Override
    public double chanceToButcher(final List<? extends Animal> allAnimals)
    {
        // No butchering the cavalry steeds!
        return 0;
    }

    /**
     * Decides what the AI should do next.
     * @return the next state to go to.
     */
    @Override
    public IAIState decideWhatToDo()
    {
        final IAIState result = super.decideWhatToDo();
        trainingCooldown--;
        roundupCooldown--;

        if (trainingCooldown <= 0 && worker.getRandom().nextDouble() < TRAINING_CHANCE)
        {
            trainingCooldown = MAX_TRAINING_COOLDOWN;
            return HERDER_TRAIN;
        }

        if (worker.getRandom().nextDouble() < READY_MOUNT_FOR_COMBAT_CHANCE)
        {
            return HERDER_READY_FOR_COMBAT;
        }

        // Searching for unstabled horses too frequently could be a performance hit.  Throttle it.
        if (roundupCooldown <= 0 && worker.getRandom().nextDouble() < ROUND_UP_CHANCE)
        {
            roundupCooldown = MAX_ROUNDUP_COOLDOWN;
            wanderingHorses = findNearbyUnstabledHorses();

            if (!wanderingHorses.isEmpty())
            {
                return HERDER_GATHER_MOUNTS;
            }
        }

        return result;
    }

    /**
     * Converts a normal horse into a cavalry horse. 
     * Capacity: at most (2 × building level) CavalryHorseEntity in the herded animals.
     */
    protected IAIState convertMount()
    {
        final int limit = Math.max(0, building.getBuildingLevel() * 2);

        // If we're mid-conversion, walk to the horse...
        if (horseToConvert != null && !walkToSafePos(horseToConvert.blockPosition()))
        {
            return HERDER_TRAIN;
        }

        // ... and then convert it!
        if (horseToConvert != null)
        {
            final CavalryHorseEntity cav = CavalryHorseEntity.createFromVanilla(building.getColony(), worker.level, horseToConvert);
            if (cav == null)
            {
                Log.getLogger().warn("Stablemaster in Colony {}: Could not train candidate horse to CavalryHorseEntity", building.getColony().getID());
            }
            else
            {
                cav.getAnimalData().setHomeBuilding(building);
                worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
                StatsUtil.trackStat(building, HORSES_TRAINED, 1);
                incrementActionsDoneAndDecSaturation();
            }
            horseToConvert = null;
            return DECIDE;
        }

        int current = countCurrentMounts();

        if (current >= limit)
        {
            // Stable at capacity. Don't train.
            Log.getLogger().info("Stablemaster in Colony {}: Already at {} mounts out of a limit of {}. Not training.", building.getColony().getID(), current, limit);
            return DECIDE;
        }

        AbstractHorse firstCandidate = null;

  
        final AnimalHerdingModule module = building.getModule(AnimalHerdingModule.class);

        // Pick first convertible AbstractHorse
        final List<? extends Animal> animals = searchForAnimals(module::isCompatible);
        
        for (final Animal a : animals)
        {
            if (a instanceof CavalryHorseEntity)
            {
                continue;
            }

            // Record first good vanilla horse candidate
            if (firstCandidate == null && a instanceof AbstractHorse h)
            {
                if (h.isAlive() && !h.isBaby() && h.getPassengers().isEmpty())
                {
                    firstCandidate = h;
                }
            }
        }

        if (firstCandidate != null)
        {
            horseToConvert = firstCandidate; 
            return HERDER_TRAIN;
        }

        // No suitable horses found to train.
        return DECIDE;
    }

    /**
     * Walks to the horse if already selected, and then gets it ready for combat.
     * If no horse is selected, it will single-pass scan for the first horse that needs to be readied for combat.
     * @return the next state to go to.
     */
    protected IAIState readyMountForCombat()
    {
        // Walk to the horse if already selected.
        if (horseToGetReady != null && !walkToSafePos(horseToGetReady.blockPosition()))
        {
            // If the horse picked up a cavalry rider while we were walking to it, we're done.
            if (horseToGetReady.hasCavalryRider())
            {
                return DECIDE;
            }

            return HERDER_READY_FOR_COMBAT;
        }

        // ... and then get it ready for combat!
        if (horseToGetReady != null)
        {
            boolean didWork = false;

            if (horseToGetReady.getHealth() < horseToGetReady.getMaxHealth())
            {
                didWork = readyMount(MountMaintenance.FEEDING, horseToGetReady);
            }

            if (horseToGetReady.getCombatCooldown() > 0)
            {
                didWork = didWork || readyMount(MountMaintenance.READYING, horseToGetReady);
            }
            
            if (didWork)
            {
                worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
                horseToGetReady = null;
            }

            return DECIDE;

        }

        AnimalHerdingModule module = building.getModule(BuildingModules.STABLEMASTER_HERDING);

        if (module == null)
        {
            Log.getLogger().warn("No herding module found for stable.");
            return DECIDE;
        }

        final List<? extends Animal> animals = searchForAnimals(module::isCompatible);

        for (final Animal a : animals)
        {
            if (a instanceof CavalryHorseEntity cav)
            {
                if (cav.getCombatCooldown() > 0 || cav.getHealth() < cav.getMaxHealth())
                {
                    horseToGetReady = cav;
                    return HERDER_READY_FOR_COMBAT;
                }
            }
        }

        // No mounts need to be readied.
        return DECIDE;
    }

    /**
     * Prepares the AI to feed the horse. If the worker has feed items in their inventory, it will take one of them and feed the horse.
     * If the worker doesn't have feed items, it will request the building for one. If the building has one, it will be taken and the horse will be fed.
     * If the building doesn't have any feed items, the AI will set a flag to indicate that it needs to request a feed item from the inventory.
     */
    public boolean readyMount(MountMaintenance task, CavalryHorseEntity horse)
    {
        final TagKey<Item> neededItem = task == MountMaintenance.FEEDING ? ModTags.feed : ModTags.leather;

        Component component = task == MountMaintenance.FEEDING ? Component.translatable(TranslationConstants.STABLEMASTER_NEEDED_FEEDITEMS) : Component.translatable(TranslationConstants.STABLEMASTER_NEEDED_READYITEMS);

        boolean hasNeededItem = false;
        boolean didWork = false;

        if (InventoryUtils.getItemCountInProvider(worker, i -> i.is(neededItem)) <= 0)
        {
            if (InventoryUtils.hasBuildingEnoughElseCount(building, i -> i.is(neededItem), 1) > 0)
            {
                walkToBuilding();

                int buildingSlot = InventoryUtils.findFirstSlotInProviderNotEmptyWith(building, itemStack -> itemStack.is(neededItem));
                if (buildingSlot >= 0)
                {
                    this.takeItemStackFromProvider(building, buildingSlot);
                }
            }
        }
        else
        {
            hasNeededItem  = true;
        }

        int slotOfStack = InventoryUtils.findFirstSlotInItemHandlerNotEmptyWith(worker.getItemHandlerCitizen(), itemStack -> itemStack.is(neededItem));

        if (hasNeededItem || slotOfStack >= 0)
        {
            ItemStack stackToUse = worker.getItemHandlerCitizen().extractItem(slotOfStack, 1, false);
            worker.setItemInHand(InteractionHand.MAIN_HAND, stackToUse);

            if (stackToUse.isEmpty())
            {
                return false;
            } 

            if (task == MountMaintenance.FEEDING)
            {
                feedHorse(horse);
                StatsUtil.trackStatByStack(building, ITEM_USED, stackToUse.copy(), 1);
                stackToUse.shrink(1);
                effectsAtHorse(horse);
            }

            if (task == MountMaintenance.READYING)
            {   
                float combatCooldownBefore = horse.getAnimalData().getCombatCooldown();

                // TODO: Reasearch to influence readiness recovery rate?
                if (stackToUse.getItem() == Items.SADDLE)
                {
                    horse.getAnimalData().setCombatCooldown(0);
                }
                else
                {
                    float recovery = BASE_COMBAT_READINESS_RECOVERY * ((float) getPrimarySkillLevel() / (float) RECOVERY_SKILL_PAR);
                    horse.prepareForCombat(recovery);
                }

                StatsUtil.trackStatByStack(building, ITEM_USED, stackToUse.copy(), 1);

                stackToUse.shrink(1);
                effectsAtHorse(horse);

                if (horse.isReadyForCombat())
                {
                    StatsUtil.trackStat(building, MOUNTS_READIED, 1);
                }
            }
            
            didWork = true;
            incrementActionsDone();
        }
        else
        {
            if (!building.hasWorkerOpenRequestsOfType(worker.getCitizenData().getId(), TypeToken.of(StackList.class)))
            {
                StackList requestableItems = new StackList(neededItem, (ServerLevel) worker.getCitizenData().getColony().getWorld(), component.getString(), 8, 4, 0);
                worker.getCitizenData().createRequestAsync(requestableItems);
            }
        }

        return didWork;
    }

    /** 
     * Feed a horse
     * */
    public static boolean feedHorse(AbstractHorse horse)
    {
        if (horse == null || horse.level().isClientSide()) return false;

        final float healPercentage = .20F;
        final int temper = 3;
        final float maxHealth = horse.getMaxHealth();

        boolean didSomething = false;

        if (horse.getHealth() < maxHealth)
        {
            horse.heal(maxHealth * healPercentage);
            didSomething = true;
        }

        if (temper != 0)
        {
            horse.setTemper(horse.getTemper() + temper);
        }

        if (didSomething)
        {
            horse.gameEvent(GameEvent.EAT);
            horse.level().playSound(null, horse, SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 0.8f, 1.0f);
        }
        return didSomething;
    }

    /**
     * Counts cavalry mounts currently assigned to this stable.
     *
     * @return number of {@link CavalryHorseEntity} records registered to this building.
     */
    protected int countCurrentMounts() 
    {
        List<IAnimalData> currentSteeds = building.getColony().getAnimalManager().getAnimalsOfClassByHome(CavalryHorseEntity.class, building);
        return currentSteeds.size();
    }

    /**
     * Gather a wandering horse and bring it back to the stable with a leash.
     * @return the next state to go to.
     */
    public IAIState gatherMounts()
    {
        
        if (horseToRetrieve != null)
        {
            if (!horseToRetrieve.isAlive() 
                || horseToRetrieve.isRemoved() 
                || (horseToRetrieve instanceof CavalryHorseEntity cav && cav.hasCavalryRider()) 
                || (horseToRetrieve instanceof CavalryHorseEntity cav && cav.hasReservation()))
            {
                detachHorse(horseToRetrieve);
                horseToRetrieve = null;
                return DECIDE;
            }

            for (final EquipmentTypeEntry tool : getExtraToolsNeeded())
            {
                // Verify that we still have the lead needed to gather this horse.
                if (checkForToolOrWeapon(tool))
                {
                    detachHorse(horseToRetrieve);
                    horseToRetrieve = null;
                    return PREPARING;
                }
            }

            if (horseToRetrieve.isLeashed())
            {
                if (!walkToSafePos(building.getNextStallPosition())) 
                {
                    horseToRetrieve.clearRestriction();
                    horseToRetrieve.restrictTo(worker.blockPosition(), 3);
                    return HERDER_GATHER_MOUNTS;
                }
                
                detachHorse(horseToRetrieve);

                worker.getCitizenExperienceHandler().addExperience(XP_PER_ACTION);
                incrementActionsDoneAndDecSaturation();

                if (wanderingHorses.contains(horseToRetrieve))
                {
                    wanderingHorses.remove(horseToRetrieve);
                }

                horseToRetrieve = null;

                if (!wanderingHorses.isEmpty())
                {
                    return HERDER_GATHER_MOUNTS;
                }
                
                StatsUtil.trackStat(building, ROUNDUPS_COMPLETED, 1);

                return DECIDE;
            }

            if (!walkToSafePos(horseToRetrieve.blockPosition()))
            {
                return HERDER_GATHER_MOUNTS;
            }

            if (attachHorse(horseToRetrieve))
            {
                return HERDER_GATHER_MOUNTS;
            }
            else
            {
                return DECIDE;
            }
        }

        if (wanderingHorses.isEmpty())
        {
            return DECIDE;
        }

        horseToRetrieve = wanderingHorses.get(0);

        return HERDER_GATHER_MOUNTS;
    }


    /**
     * Plays particles and sound effects at the given horse.
     * @param horse the horse to play effects at
     */
    protected void effectsAtHorse(CavalryHorseEntity horse) 
    {
        ServerLevel level = (ServerLevel) horse.level();
        level.sendParticles(ParticleTypes.WAX_ON, horse.getX(), horse.getY() + horse.getBbHeight() * 0.7, horse.getZ(), 12, 0.3, 0.4, 0.3, 0.02);

        level.playSound(
            null,
            BlockPos.containing(horse.getX(), horse.getY(), horse.getZ()),
            SoundEvents.HORSE_EAT,
            SoundSource.PLAYERS,
            0.8f,
            1.0f
        ); 
    }

    /**
     * Attempts to attach the given horse to the worker.
     * @param horse the horse to attach
     * @return true if the horse was successfully attached, false otherwise
     */
    protected boolean attachHorse(AbstractHorse horse)
    {
        if (worker == null || horse == null) return false;

        if (!horse.isAlive() || horse.isRemoved()) return false;

        // If already leashed to this citizen, nothing to do.
        if (horse.isLeashed() && worker.equals(horse.getLeashHolder())) return true;

        // If leashed to somebody else, drop that leash first
        if (horse.isLeashed())
        {
            horse.dropLeash(true, true);
        }

        if (worker.getOffhandItem().isEmpty())
        {
            worker.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.LEAD));
        }

        horse.setLeashedTo(worker, true);
        horse.restrictTo(worker.blockPosition(), 3);

        return true;
    }

    /**
     * Detaches the given horse from the worker, removing the leash and dropping
     * the lead item if held by the worker. If the worker is holding a lead
     * item, it will be replaced with an empty hand.
     * 
     * @param horse the horse to detach
     */
    public void detachHorse(AbstractHorse horse)
    {
        if (worker == null || horse == null || horse.level().isClientSide)
        {
            return;
        }

        horse.clearRestriction();

        if (!horse.isLeashed())
        {
            return;
        } 

        horse.dropLeash(true, false);

        if (worker.getOffhandItem().is(Items.LEAD))
        {
            worker.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

   /**
     * Finds all horses within 20 blocks of the given position, but
     * outside the stable.
     *
     * @param level The world/level to search in
     * @param center The center position
     * @return List of CavalryHorseEntity found within radius
     */
   public List<AbstractHorse> findNearbyUnstabledHorses()
   {
       // TODO: Research to increase the range of the stablemaster's round-up
       final BlockPos center = building.getPosition();
       final double radius = 30.0D;
       final double r2 = radius * radius;

       // Build a cube search box once; we'll do a spherical check in the predicate.
       final double cx = center.getX() + 0.5D;
       final double cy = center.getY() + 0.5D;
       final double cz = center.getZ() + 0.5D;
       final AABB searchBox = new AABB(cx - radius, cy - radius, cz - radius, cx + radius, cy + radius, cz + radius);

       return worker.level()
           .getEntitiesOfClass(AbstractHorse.class,
               searchBox,
               horse -> !horse.isRemoved() &&
                   horse.isAlive() &&
                   !(horse instanceof CavalryHorseEntity cav && cav.hasCavalryRider()) &&
                   !(horse instanceof CavalryHorseEntity cav && cav.isInStable()) &&
                   !(horseToRetrieve instanceof CavalryHorseEntity cav && cav.hasReservation()) &&
                   horse.distanceToSqr(cx, cy, cz) <= r2);
   }

}
