package com.minecolonies.coremod.entity.ai.citizen.enchanter;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingEnchanter;
import com.minecolonies.coremod.colony.jobs.JobEnchanter;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIInteract;
import com.minecolonies.coremod.network.messages.CircleParticleEffectMessage;
import com.minecolonies.coremod.network.messages.StreamParticleEffectMessage;
import com.minecolonies.coremod.util.ExperienceUtils;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.TranslationConstants.NO_WORKERS_TO_DRAIN_SET;

/**
 * Enchanter AI class.
 */
public class EntityAIWorkEnchanter extends AbstractEntityAIInteract<JobEnchanter>
{
    /**
     * How often should intelligence factor into the enchanter's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should intelligence factor into the enchanter's skill modifier.
     */
    private static final int CHARISMA_MULTIPLIER = 1;

    /**
     * Predicate to define an ancient tome which can be enchanted.
     */
    private static final Predicate<ItemStack> IS_ANCIENT_TOME = item -> !item.isEmpty() && item.getItem() == ModItems.ancientTome;

    /**
     * Min distance to drain from citizen.
     */
    private static final long MIN_DISTANCE_TO_DRAIN = 5;

    /**
     * Max progress ticks until drainage is complete (per Level).
     */
    private static final int MAX_PROGRESS_TICKS = 20;

    /**
     * Max progress ticks until drainage is complete (per Level).
     */
    private static final int MAX_ENCHANTMENT_TICKS = 60 * 5;

    /**
     * The citizen entity to gather from.
     */
    private ICitizenData citizenToGatherFrom = null;

    /**
     * Variable to check if the draining is in progress.
     * And at which tick it is.
     */
    private int progressTicks = 0;

    /**
     * Creates the abstract part of the AI.
     * Always use this constructor!
     *
     * @param job the job to fulfill
     */
    public EntityAIWorkEnchanter(@NotNull final JobEnchanter job)
    {
        super(job);
        super.registerTargets(
            new AITarget(IDLE, START_WORKING, TICKS_SECOND),
            new AITarget(START_WORKING, DECIDE, TICKS_SECOND),
            new AITarget(DECIDE, this::decide, TICKS_SECOND),
            new AITarget(ENCHANTER_DRAIN, this::gatherAndDrain, 10),
            new AITarget(ENCHANT, this::enchant, TICKS_SECOND)
        );
        worker.getCitizenExperienceHandler().setSkillModifier(CHARISMA_MULTIPLIER * worker.getCitizenData().getCharisma() + INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Decide method of the enchanter.
     * Check if everything is alright to work and then decide between gathering and draining and actually enchanting.
     *
     * @return the next state to go to.
     */
    private IAIState decide()
    {
        worker.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
        if (walkToBuilding())
        {
            return DECIDE;
        }

        if (worker.getCitizenExperienceHandler().getLevel() < getOwnBuilding().getBuildingLevel())
        {
            final BuildingEnchanter enchanterBuilding = getOwnBuilding(BuildingEnchanter.class);
            if (enchanterBuilding.getBuildingsToGatherFrom().isEmpty())
            {
                chatSpamFilter.talkWithoutSpam(NO_WORKERS_TO_DRAIN_SET);
                return IDLE;
            }

            final BlockPos posToDrainFrom = enchanterBuilding.getRandomBuildingToDrainFrom();
            if (posToDrainFrom == null)
            {
                return IDLE;
            }
            job.setBuildingToDrainFrom(posToDrainFrom);
            return ENCHANTER_DRAIN;
        }

        final int ancientTomesInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), IS_ANCIENT_TOME);
        if (ancientTomesInInv <= 0)
        {
            final int amountOfAncientTomes = InventoryUtils.getItemCountInProvider(getOwnBuilding(), IS_ANCIENT_TOME);
            if (amountOfAncientTomes > 0)
            {
                needsCurrently = IS_ANCIENT_TOME;
                return GATHERING_REQUIRED_MATERIALS;
            }
            checkIfRequestForItemExistOrCreateAsynch(new ItemStack(ModItems.ancientTome, 1));
            return IDLE;
        }

        return ENCHANT;
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return 1;
    }

    /**
     * Actually do the enchanting.
     * Making some great effects for some time and then apply a random enchantment.
     * Reduce own levels depending on the found enchantment.
     * @return the next state to go to.
     */
    private IAIState enchant()
    {
        final int ancientTomesInInv = InventoryUtils.getItemCountInItemHandler(new InvWrapper(worker.getInventoryCitizen()), IS_ANCIENT_TOME);
        if (ancientTomesInInv < 1)
        {
            return DECIDE;
        }

        if (progressTicks++ < MAX_ENCHANTMENT_TICKS / getOwnBuilding().getBuildingLevel())
        {
            MineColonies.getNetwork().sendToAllTracking(
              new CircleParticleEffectMessage(
                worker.getPositionVector().add(0, 2, 0),
                EnumParticleTypes.ENCHANTMENT_TABLE,
                progressTicks), worker);

            MineColonies.getNetwork().sendToAllTracking(
              new CircleParticleEffectMessage(
                worker.getPositionVector().add(0, 1.5, 0),
                EnumParticleTypes.ENCHANTMENT_TABLE,
                progressTicks), worker);

            MineColonies.getNetwork().sendToAllTracking(
              new CircleParticleEffectMessage(
                worker.getPositionVector().add(0, 1, 0),
                EnumParticleTypes.ENCHANTMENT_TABLE,
                progressTicks), worker);

            if (worker.getRandom().nextBoolean())
            {
                worker.swingArm(EnumHand.MAIN_HAND);
            }
            else
            {
                worker.swingArm(EnumHand.OFF_HAND);
            }
            return getState();
        }

        final int slot = InventoryUtils.findFirstSlotInItemHandlerWith(new InvWrapper(worker.getInventoryCitizen()), IS_ANCIENT_TOME);
        if (slot != -1)
        {
            final ICitizenData data = worker.getCitizenData();
            if (data != null)
            {
                final int openSlot = InventoryUtils.getFirstOpenSlotFromItemHandler(new InvWrapper(worker.getInventoryCitizen()));
                if (openSlot == -1)
                {
                    //Dump if there is no open slot.
                    incrementActionsDone();
                    progressTicks = 0;
                    return IDLE;
                }

                final Tuple<ItemStack, Integer> tuple = IColonyManager.getInstance().getCompatibilityManager().getRandomEnchantmentBook(getOwnBuilding().getBuildingLevel());

                data.spendLevels(tuple.getSecond());
                worker.getCitizenExperienceHandler().updateLevel();
                worker.getInventoryCitizen().setInventorySlotContents(openSlot,tuple.getFirst());

                InventoryUtils.reduceStackInItemHandler(new InvWrapper(worker.getInventoryCitizen()), new ItemStack(ModItems.ancientTome));
                incrementActionsDoneAndDecSaturation();
            }
        }
        progressTicks = 0;
        return IDLE;
    }

    /**
     * Gather experience from a worker.
     * Go to the hut of the worker.
     * Wait for the worker.
     * Drain, and then return to work building.
     * @return next state to go to.
     */
    private IAIState gatherAndDrain()
    {
        if (job.getPosToDrainFrom() == null)
        {
            return IDLE;
        }

        if (walkToBlock(job.getPosToDrainFrom()))
        {
            return getState();
        }

        final AbstractBuildingWorker buildingWorker = getOwnBuilding().getColony().getBuildingManager().getBuilding(job.getPosToDrainFrom(), AbstractBuildingWorker.class);
        if (buildingWorker == null)
        {
            resetDraining();
            getOwnBuilding(BuildingEnchanter.class).removeWorker(job.getPosToDrainFrom());
            return IDLE;
        }

        if (citizenToGatherFrom == null)
        {
            final List<Optional<AbstractEntityCitizen>> workers = buildingWorker.getAssignedEntities().stream()
                                                                    .filter(e -> e.isPresent() && e.get().getCitizenData().getLevel() > 0)
                                                                    .collect(Collectors.toList());
            final Optional<AbstractEntityCitizen> citizen;
            if (workers.size() > 1)
            {
                citizen = workers.get(worker.getRandom().nextInt(workers.size()));
            }
            else
            {
                if (workers.isEmpty())
                {
                    resetDraining();
                    return DECIDE;
                }
                citizen = workers.get(0);
            }

            citizen.ifPresent(abstractEntityCitizen -> citizenToGatherFrom = abstractEntityCitizen.getCitizenData());
            progressTicks = 0;
            return getState();
        }

        if (!citizenToGatherFrom.getCitizenEntity().isPresent())
        {
            citizenToGatherFrom = null;
            return getState();
        }

        if (progressTicks == 0)
        {
            // If worker is too far away wait.
            if (BlockPosUtil.getDistance2D(citizenToGatherFrom.getCitizenEntity().get().getPosition(), worker.getPosition()) > MIN_DISTANCE_TO_DRAIN)
            {
                if (!job.incrementWaitingTicks())
                {
                    resetDraining();
                    return DECIDE;
                }
                return getState();
            }
        }

        progressTicks++;
        final int maxDrain = Math.min(getOwnBuilding(BuildingEnchanter.class).getDailyDrain(), citizenToGatherFrom.getLevel());
        if (progressTicks < MAX_PROGRESS_TICKS * maxDrain)
        {
            final Vec3d start = worker.getPositionVector().add(0,2,0);
            final Vec3d goal = citizenToGatherFrom.getCitizenEntity().get().getPositionVector().add(0, 2, 0);

            MineColonies.getNetwork().sendToAllTracking(
              new StreamParticleEffectMessage(
                start,
                goal,
                EnumParticleTypes.ENCHANTMENT_TABLE,
                progressTicks%MAX_PROGRESS_TICKS,
                MAX_PROGRESS_TICKS), worker);

            MineColonies.getNetwork().sendToAllTracking(
              new CircleParticleEffectMessage(
                start,
                EnumParticleTypes.VILLAGER_HAPPY,
                progressTicks), worker);

            WorkerUtil.faceBlock(new BlockPos(goal), worker);

            return getState();
        }

        if (worker.getRandom().nextBoolean())
        {
            worker.swingArm(EnumHand.MAIN_HAND);
        }
        else
        {
            worker.swingArm(EnumHand.OFF_HAND);
        }

        final int size = citizenToGatherFrom.getInventory().getSizeInventory();
        final int attempts = getOwnBuilding().getBuildingLevel();

        for (int i = 0; i < attempts; i++)
        {
            int randomSlot = worker.getRandom().nextInt(size);
            final ItemStack stack = citizenToGatherFrom.getInventory().getStackInSlot(randomSlot);
            if (!stack.isEmpty() && stack.isItemEnchantable())
            {
                EnchantmentHelper.addRandomEnchantment(worker.getRandom(), stack, 1, false);
                break;
            }
        }

        worker.getCitizenData().addExperience(citizenToGatherFrom.drainExperience(maxDrain));
        while (ExperienceUtils.getXPNeededForNextLevel(worker.getCitizenData().getLevel()) < worker.getCitizenData().getExperience())
        {
            worker.getCitizenData().levelUp();
        }
        worker.getCitizenExperienceHandler().updateLevel();
        worker.getCitizenData().markDirty();

        resetDraining();
        return IDLE;
    }

    /**
     * Helper method to reset all variables of the draining.
     */
    private void resetDraining()
    {
        getOwnBuilding(BuildingEnchanter.class).setAsGathered(job.getPosToDrainFrom());
        citizenToGatherFrom = null;
        job.setBuildingToDrainFrom(null);
        progressTicks = 0;
        incrementActionsDoneAndDecSaturation();
    }
}
