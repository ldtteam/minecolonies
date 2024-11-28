package com.minecolonies.core.entity.ai.workers.production.agriculture;

import com.ldtteam.structurize.util.BlockUtils;
import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.loot.ModLootTables;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.*;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingFisherman;
import com.minecolonies.core.colony.interactionhandling.StandardInteraction;
import com.minecolonies.core.colony.jobs.JobFisherman;
import com.minecolonies.core.entity.ai.workers.AbstractEntityAISkill;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.util.citizenutils.CitizenItemUtils;
import com.minecolonies.core.entity.other.NewBobberEntity;
import com.minecolonies.core.entity.pathfinding.Pathfinding;
import com.minecolonies.core.entity.pathfinding.PathfindingUtils;
import com.minecolonies.core.entity.pathfinding.pathjobs.PathJobFindWater;
import com.minecolonies.core.entity.pathfinding.pathresults.WaterPathResult;
import com.minecolonies.core.util.WorkerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.EquipmentLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.StatisticsConstants.FISH_CAUGHT;
import static com.minecolonies.api.util.constant.TranslationConstants.WATER_TOO_FAR;
import static com.minecolonies.core.colony.buildings.modules.BuildingModules.STATS_MODULE;
import static com.minecolonies.core.entity.other.NewBobberEntity.XP_PER_CATCH;

/**
 * Fisherman AI class.
 * <p>
 * A fisherman has some ponds where he randomly selects one and fishes there.
 * <p>
 * To keep it immersive he chooses his place at random around the pond.
 */
public class EntityAIWorkFisherman extends AbstractEntityAISkill<JobFisherman, BuildingFisherman>
{
    /**
     * The render name to render fish.
     */
    public static final String RENDER_META_FISH = "fish";

    /**
     * The render name to render rod.
     */
    public static final String RENDER_META_ROD = "rod";

    /**
     * The maximum number of ponds to remember at one time.
     */
    private static final int MAX_PONDS = 20;

    /**
     * Base chance to fail an action
     */
    private static final int FISHING_SKILL_CHANCE = 10;

    /**
     * The chance the fisherman has to throw his rod. Directly connected with delay.
     */
    private static final int CHANCE = 2;

    /**
     * The minimum distance in blocks to the water which is required for the fisherman to throw his rod.
     */
    private static final int MIN_DISTANCE_TO_WATER = 3;

    /**
     * The amount of catches until the fisherman empties his inventory.
     */
    private static final int MAX_FISHES_IN_INV = 10;

    /**
     * The maximum amount of adjusts of his rotation until the fisherman discards a fishing location.
     */
    private static final int MAX_ROTATIONS = 6;

    /**
     * The range in which the fisherman searches water.
     */
    private static final int SEARCH_RANGE = 50;

    /**
     * The percentage of times where the fisherman will check out a new pond.
     */
    private static final double CHANCE_NEW_POND = 0.05D;

    /**
     * Time out fo fish again.
     */
    private static final int FISHING_TIMEOUT = 5;

    /**
     * Per level lure speed.
     */
    private static final int LURE_SPEED_DIVIDER = 25;

    /**
     * The number of executed adjusts of the fisherman's rotation.
     */
    private int executedRotations = 0;

    /**
     * The PathResult when the fisherman searches water.
     */
    @Nullable
    private WaterPathResult pathResult;

    /**
     * The Previous PathResult when the fisherman already found water.
     */
    @Nullable
    private WaterPathResult lastPathResult;

    /**
     * Connects the citizen with the fishingHook.
     */
    @Nullable
    private NewBobberEntity entityFishHook;

    /**
     * Hook stuck counter. Don't immediately retrieve until a bit of time passed.
     */
    private int stuckCounter = 3;

    /**
     * Constructor for the Fisherman. Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkFisherman(@NotNull final JobFisherman job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING, 1),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding, TICKS_SECOND),
          new AITarget(PREPARING, this::prepareForFishing, TICKS_SECOND),
          new AITarget(FISHERMAN_CHECK_WATER, this::tryDifferentAngles, 1),
          new AITarget(FISHERMAN_SEARCHING_WATER, this::findWater, TICKS_SECOND),
          new AITarget(FISHERMAN_WALKING_TO_WATER, this::getToWater, TICKS_SECOND),
          new AITarget(FISHERMAN_START_FISHING, this::doFishing, TICKS_SECOND)
        );
        worker.setCanPickUpLoot(true);
    }

    @Override
    public Class<BuildingFisherman> getExpectedBuildingClass()
    {
        return BuildingFisherman.class;
    }

    /**
     * Redirects the fisherman to his building.
     *
     * @return the next state.
     */
    private IAIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the fisherman for fishing and requests fishingRod and checks if the fisherman already had found a pond.
     *
     * @return the next IAIState
     */
    private IAIState prepareForFishing()
    {
        if (checkForToolOrWeapon(ModEquipmentTypes.fishing_rod.get()))
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
            playNeedRodSound();
            return getState();
        }

        return FISHERMAN_WALKING_TO_WATER;
    }

    /**
     * Plays a sound when the fisherman needs a rod.
     */
    private void playNeedRodSound()
    {
        SoundUtils.playSoundAtCitizenWith(world, worker.blockPosition(), EventType.MISSING_EQUIPMENT, worker.getCitizenData());
    }

    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_FISHES_IN_INV;
    }

    /**
     * Here the AI can check if the fishes or rods have to be re rendered and do it.
     */
    @Override
    protected void updateRenderMetaData()
    {
        if (hasFish() && hasRodButNotEquipped())
        {
            worker.setRenderMetadata(RENDER_META_ROD + RENDER_META_FISH);
        }
        else if (hasRodButNotEquipped() && !hasFish())
        {
            worker.setRenderMetadata(RENDER_META_ROD);
        }
        else
        {
            worker.setRenderMetadata(hasFish() ? RENDER_META_FISH : "");
        }
    }

    /**
     * Checks if the fisherman has fish in his inventory.
     *
     * @return true if so.
     */
    private boolean hasFish()
    {
        return InventoryUtils.hasItemInItemHandler(getInventory(), item -> item.is(ItemTags.FISHES));
    }

    /**
     * Checks if the fisherman has a rod in his inventory but if he did not equip it.
     *
     * @return true if so.
     */
    private boolean hasRodButNotEquipped()
    {
        return InventoryUtils.hasItemHandlerEquipmentWithLevel(getInventory(), ModEquipmentTypes.fishing_rod.get(), TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel())
                 && worker.getMainHandItem() != null
                 && !ModEquipmentTypes.fishing_rod.get().checkIsEquipment(worker.getMainHandItem());
    }

    /**
     * If the job class has no water object the fisherman should search water.
     *
     * @return the next IAIState the fisherman should switch to, after executing this method.
     */
    private IAIState getToWater()
    {
        if (job.getWater() == null)
        {
            return FISHERMAN_SEARCHING_WATER;
        }

        if (walkToWater())
        {
            return getState();
        }
        return FISHERMAN_CHECK_WATER;
    }

    /**
     * Let's the fisherman walk to the water if the water object in his job class already has been filled.
     *
     * @return true if the fisherman has arrived at the water.
     */
    private boolean walkToWater()
    {
        return job.getWater() != null && walkToBlock(job.getWater().getB());
    }

    /**
     * Rotates the fisherman to guarantee that the fisherman throws his rod in the correct direction.
     *
     * @return the next IAIState the fisherman should switch to, after executing this method.
     */
    @NotNull
    private IAIState tryDifferentAngles()
    {
        if (job.getWater() == null)
        {
            return FISHERMAN_SEARCHING_WATER;
        }
        if (executedRotations >= MAX_ROTATIONS)
        {
            job.removeFromPonds(job.getWater());
            job.setWater(null);
            executedRotations = 0;
            return FISHERMAN_SEARCHING_WATER;
        }

        if (world.getBlockState(worker.blockPosition()).liquid())
        {
            if (!BlockUtils.isAnySolid(world.getBlockState(job.getWater().getB().below())))
            {
                job.removeFromPonds(job.getWater());
                job.setWater(null);
                executedRotations = 0;
                return START_WORKING;
            }
            else
            {
                executedRotations++;
            }
        }

        //Try a different angle to throw the hook not that far
        WorkerUtil.faceBlock(job.getWater().getA(), worker);
        executedRotations++;
        return FISHERMAN_START_FISHING;
    }

    /**
     * Checks if the fisherman already has found 20 pools, if yes search a water pool out of these 20, else search a new one.
     *
     * @return the next IAIState the fisherman should switch to, after executing this method.
     */
    private IAIState findWater()
    {

        //Reset executedRotations when fisherman searches a new Pond
        executedRotations = 0;
        //If he can't find any pond, tell that to the player
        //If 20 ponds are already stored, take a random stored location
        //the fishman should not go to find water when fishman can find water but cant find pond
        if (job.getPonds().size() >= MAX_PONDS || (lastPathResult != null && lastPathResult.pond == null && job.getPonds().size() > 0))
        {
            return setRandomWater();
        }
        return findNewWater();
    }

    /**
     * Used to find a water.
     *
     * @param range in the range.
     * @param speed walking speed.
     * @param ponds a list of ponds.
     * @return the result of the search.
     */
    public WaterPathResult searchWater(final int range, final double speed, final List<Tuple<BlockPos, BlockPos>> ponds)
    {
        @NotNull final BlockPos start = PathfindingUtils.prepareStart(worker);
        final PathJobFindWater job = new PathJobFindWater(CompatibilityUtils.getWorldFromEntity(worker),
          start,
          worker.getCitizenColonyHandler().getWorkBuilding().getPosition(),
          range,
          ponds,
          worker);
        job.setPathingOptions(worker.getNavigation().getPathingOptions());
        final WaterPathResult waterPathresult = job.getResult();
        waterPathresult.startJob(Pathfinding.getExecutor());
        return waterPathresult;
    }

    /**
     * If the fisherman can't find 20 ponds or already has found 20, the fisherman should randomly choose a fishing spot from the previously found ones.
     *
     * @return the next IAIState.
     */
    private IAIState setRandomWater()
    {
        if (job.getPonds().isEmpty())
        {
            if ((pathResult != null && pathResult.failedToReachDestination() && lastPathResult == null) || (lastPathResult != null && lastPathResult.isEmpty
                                                                                                              && !lastPathResult.isCancelled()))
            {
                if (worker.getCitizenData() != null)
                {
                    worker.getCitizenData().triggerInteraction(new StandardInteraction(Component.translatable(WATER_TOO_FAR), ChatPriority.IMPORTANT));
                }
            }

            if (pathResult == null || !pathResult.isInProgress())
            {
                pathResult = searchWater(SEARCH_RANGE, 1.0D, job.getPonds());
            }

            return START_WORKING;
        }
        job.setWater(job.getPonds().get(worker.getRandom().nextInt(job.getPonds().size())));
        return FISHERMAN_CHECK_WATER;
    }

    /**
     * Uses the pathFinding system to search close water spots which possibilitate fishing. Sets a number of possible water pools and sets the water pool the fisherman should fish
     * now.
     *
     * @return the next IAIState the fisherman should switch to, after executing this method
     */
    private IAIState findNewWater()
    {
        if (pathResult == null)
        {
            pathResult = searchWater(SEARCH_RANGE * 3, 1.0D, job.getPonds());
            return getState();
        }
        if (pathResult.failedToReachDestination())
        {
            return setRandomWater();
        }
        if (pathResult.isPathReachingDestination())
        {
            if (pathResult.pond != null)
            {
                job.setWater(new Tuple<>(pathResult.pond, pathResult.parent));
                job.addToPonds(pathResult.pond, pathResult.parent);
            }
            lastPathResult = pathResult;
            pathResult = null;
            return FISHERMAN_CHECK_WATER;
        }
        if (pathResult.isCancelled())
        {
            pathResult = null;
            return PREPARING;
        }
        return getState();
    }

    /**
     * Main fishing methods, let's the fisherman gather xp orbs next to him, check if all requirements to fish are given. Actually fish, retrieve his rod if stuck or if a fish
     * bites.
     *
     * @return the next IAIState the fisherman should switch to, after executing this method.
     */
    @Nullable
    private IAIState doFishing()
    {

        @Nullable final IAIState notReadyState = isReadyToFish();
        if (notReadyState != null)
        {
            return notReadyState;
        }

        if (caughtFish())
        {
            playCaughtFishSound();
            this.incrementActionsDoneAndDecSaturation();
            worker.getCitizenColonyHandler().getColonyOrRegister().getStatisticsManager().increment(FISH_CAUGHT, worker.getCitizenColonyHandler().getColonyOrRegister().getDay());
            building.getModule(STATS_MODULE).increment(FISH_CAUGHT);

            if (worker.getRandom().nextDouble() < CHANCE_NEW_POND)
            {
                job.setWater(null);
                return FISHERMAN_SEARCHING_WATER;
            }
            return FISHERMAN_WALKING_TO_WATER;
        }
        return throwOrRetrieveHook();
    }

    /**
     * Plays a sound with a chance when a fish has been caught.
     */
    private void playCaughtFishSound()
    {
        SoundUtils.playSoundAtCitizenWith(world, worker.blockPosition(), EventType.SUCCESS, worker.getCitizenData());
    }

    /**
     * Check if a hook is out there, and throw/retrieve it if needed.
     *
     * @return the next IAIState the fisherman should switch to, after executing this method
     */
    private IAIState throwOrRetrieveHook()
    {
        if (entityFishHook == null)
        {
            //Only sometimes the fisherman gets to throw its Rod (depends on intelligence)
            if (testRandomChance())
            {
                return getState();
            }
            throwRod();
        }
        else
        {
            //Check if hook landed on ground or in water, in some cases the hook bugs -> remove it after 2 minutes.
            if (isFishHookStuck())
            {
                if (stuckCounter > 3)
                {
                    stuckCounter = 0;
                    retrieveRod();
                    return FISHERMAN_WALKING_TO_WATER;
                }
                stuckCounter++;
            }
            else
            {
                stuckCounter = 0;
            }
            this.entityFishHook.setInUse();
        }
        return getState();
    }

    /**
     * Let's the fisherman face the water, play the throw sound and create the fishingHook and throw it.
     */
    private void throwRod()
    {
        if (!world.isClientSide)
        {
            WorkerUtil.faceBlock(job.getWater().getA(), worker);
            world.playSound(null,
              this.worker.blockPosition(),
              SoundEvents.FISHING_BOBBER_THROW,
              SoundSource.NEUTRAL,
              0.5F,
              (float) (0.4D / (this.world.random.nextFloat() * 0.4D + 0.8D)));
            this.entityFishHook = (NewBobberEntity) ModEntities.FISHHOOK.create(world);
            this.entityFishHook.setAngler((EntityCitizen) worker,
              EnchantmentHelper.getFishingLuckBonus(worker.getMainHandItem()),
              (int) (5 + (getPrimarySkillLevel() / LURE_SPEED_DIVIDER) + EnchantmentHelper.getFishingSpeedBonus(worker.getMainHandItem())));
            world.addFreshEntity(this.entityFishHook);
        }

        worker.swing(worker.getUsedItemHand());
    }

    /**
     * Checks if the fishHook is stuck on land or in an entity. If the fishhook is neither in water,land nether connected with an entity, give it a time to land in water.
     *
     * @return false if the hook landed in water, else return true
     */
    private boolean isFishHookStuck()
    {
        return (!entityFishHook.isInWater() && (entityFishHook.onGround() || entityFishHook.shouldStopFishing())) || !entityFishHook.isAlive()
                 || entityFishHook.caughtEntity != null;
    }

    /**
     * Checks how lucky the fisherman is.
     * <p>
     * This check depends on his fishing skill. Which in turn depends on intelligence.
     *
     * @return true if he has to wait.
     */
    private boolean testRandomChance()
    {
        //+1 since the level may be 0
        setDelay(FISHING_TIMEOUT);
        final double chance = worker.getRandom().nextInt(FISHING_SKILL_CHANCE + ((getSecondarySkillLevel()) / 5));
        return chance <= CHANCE;
    }

    /**
     * Checks if the fisherman has his fishingRod in his hand and is close to the water.
     *
     * @return true if fisherman meets all requirements to fish, else returns false.
     */
    private IAIState isReadyToFish()
    {
        final int rodSlot = getRodSlot();
        //We really do have our Rod in our inventory?
        if (rodSlot == -1)
        {
            worker.setItemInHand(InteractionHand.MAIN_HAND, ItemStackUtils.EMPTY);
            return PREPARING;
        }

        //If there is no close water, try to move closer
        if (!Utils.isBlockInRange(world, Blocks.WATER, (int) worker.getX(), (int) worker.getY(), (int) worker.getZ(), MIN_DISTANCE_TO_WATER))
        {
            return FISHERMAN_WALKING_TO_WATER;
        }

        //Check if Rod is held item if not put it as held item
        if (worker.getMainHandItem() == null || !ItemStackUtils.compareItemStacksIgnoreStackSize(worker.getMainHandItem(),
          worker.getItemHandlerCitizen().getStackInSlot(rodSlot),
          false,
          true))
        {
            equipRod();
            return getState();
        }
        return null;
    }

    /**
     * Sets the rod as held item.
     */
    private void equipRod()
    {
        CitizenItemUtils.setHeldItem(worker, InteractionHand.MAIN_HAND, getRodSlot());
    }

    /**
     * Get's the slot in which the rod is in.
     *
     * @return slot number
     */
    private int getRodSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingEquipment(getInventory(), ModEquipmentTypes.fishing_rod.get(),
          TOOL_LEVEL_WOOD_OR_GOLD, building.getMaxEquipmentLevel());
    }

    /**
     * Will be called to check if the fisherman caught a fish. If the hook hasn't noticed a fish it will return false. Else the method will pick up the loot and call the method to
     * retrieve the rod.
     *
     * @return true when the fisherman caught a fish.
     */
    private boolean caughtFish()
    {
        if (entityFishHook == null)
        {
            return false;
        }
        if (!entityFishHook.isReadyToCatch())
        {
            return false;
        }

        if (testRandomChance())
        {
            return false;
        }

        worker.setCanPickUpLoot(true);
        retrieveRod();
        return true;
    }

    /**
     * Retrieves the previously thrown fishingRod. If the fishingRod still has a hook connected to it, destroy the hook object.
     */
    private void retrieveRod()
    {
        if (entityFishHook != null)
        {
            worker.swing(worker.getUsedItemHand());
            final int i = entityFishHook.getDamage();
            generateBonusLoot();
            CitizenItemUtils.damageItemInHand(worker, InteractionHand.MAIN_HAND, i);
            entityFishHook = null;
        }
    }

    /**
     * Generates bonus fishing loot according to the building-level table
     */
    private void generateBonusLoot()
    {
        final LootParams context = (new LootParams.Builder((ServerLevel) this.world))
                                     .withParameter(LootContextParams.ORIGIN, entityFishHook.position())
                                     .withParameter(LootContextParams.THIS_ENTITY, entityFishHook)
                                     .withParameter(LootContextParams.TOOL, worker.getMainHandItem())
                                     .withParameter(LootContextParams.KILLER_ENTITY, worker)
                                     .withLuck((float) getPrimarySkillLevel())
                                     .create(LootContextParamSets.FISHING);
        final LootTable bonusLoot =
          this.world.getServer().getLootData().getLootTable(ModLootTables.FISHERMAN_BONUS.getOrDefault(this.building.getBuildingLevel(), new ResourceLocation("")));
        final List<ItemStack> loot = bonusLoot.getRandomItems(context);

        for (final ItemStack itemstack : loot)
        {
            final ItemEntity itementity = new ItemEntity(this.world, entityFishHook.position().x, entityFishHook.position().y, entityFishHook.position().z, itemstack);
            final double d0 = worker.getX() - entityFishHook.position().x;
            final double d1 = (worker.getY() + 0.5D) - entityFishHook.position().y;
            final double d2 = worker.getZ() - entityFishHook.position().z;
            itementity.noPhysics = true;
            itementity.setDeltaMovement(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);
            this.world.addFreshEntity(itementity);
            worker.level.addFreshEntity(new ExperienceOrb(worker.level,
              worker.getX(),
              worker.getY() + 0.5D,
              worker.getZ() + 0.5D,
              XP_PER_CATCH));
        }
    }

    /**
     * Returns the fisherman's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public AbstractEntityCitizen getCitizen()
    {
        return worker;
    }
}
