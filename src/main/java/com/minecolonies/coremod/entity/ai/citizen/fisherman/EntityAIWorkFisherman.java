package com.minecolonies.coremod.entity.ai.citizen.fisherman;

import com.minecolonies.api.colony.interactionhandling.ChatPriority;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.entity.ai.statemachine.AITarget;
import com.minecolonies.api.entity.ai.statemachine.states.IAIState;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.api.entity.pathfinding.WaterPathResult;
import com.minecolonies.api.sounds.EventType;
import com.minecolonies.api.util.*;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFisherman;
import com.minecolonies.coremod.colony.interactionhandling.StandardInteraction;
import com.minecolonies.coremod.colony.jobs.JobFisherman;
import com.minecolonies.coremod.entity.NewBobberEntity;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.util.WorkerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.entity.ai.statemachine.states.AIWorkerState.*;
import static com.minecolonies.api.util.constant.Constants.ONE_HUNDRED_PERCENT;
import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;
import static com.minecolonies.api.util.constant.ToolLevelConstants.TOOL_LEVEL_WOOD_OR_GOLD;
import static com.minecolonies.api.util.constant.TranslationConstants.WATER_TOO_FAR;

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
    private static final String RENDER_META_FISH = "fish";

    /**
     * The render name to render fish and rod.
     */
    private static final String RENDER_META_FISHANDROD = "rodfish";

    /**
     * The render name to render rod.
     */
    private static final String RENDER_META_ROD = "rod";

    /**
     * The maximum number of ponds to remember at one time.
     */
    private static final int MAX_PONDS = 20;

    /**
     * Variable to calculate the delay the fisherman needs to throw his rod. The delay will be calculated randomly. The FISHING_DELAY defines the upper limit. The delay is
     * calculated using the CHANCE anf fishingSkill variables. A higher FISHING_DELAY will lead to a longer delay.
     */
    private static final int FISHING_DELAY = 500 / TICKS_SECOND;

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
     * Required level for sponge/prismarine drop.
     */
    private static final int LEVEL_FOR_BONUS = 3;

    /**
     * Per level lure speed.
     */
    private static final int LURE_SPEED_DIVIDER = 15;

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
        if (checkForToolOrWeapon(ToolType.FISHINGROD))
        {
            worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
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
        SoundUtils.playSoundAtCitizenWith(world, worker.getPosition(), EventType.MISSING_EQUIPMENT, worker.getCitizenData());
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
            worker.setRenderMetadata(RENDER_META_FISHANDROD);
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
        return InventoryUtils.hasItemInItemHandler(getInventory(), item -> item.getItem().isIn(ItemTags.FISHES));
    }

    /**
     * Checks if the fisherman has a rod in his inventory but if he did not equip it.
     *
     * @return true if so.
     */
    private boolean hasRodButNotEquipped()
    {
        return InventoryUtils.hasItemInItemHandler(getInventory(), item -> item.getItem() instanceof FishingRodItem)
                && worker.getHeldItemMainhand() != null
                && !(worker.getHeldItemMainhand().getItem() instanceof FishingRodItem);
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
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.goingtopond"));

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

        if (world.getBlockState(worker.getPosition()).getMaterial().isLiquid())
        {
            return START_WORKING;
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
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.searchingwater"));

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
                    worker.getCitizenData().triggerInteraction(new StandardInteraction(new TranslationTextComponent(WATER_TOO_FAR), ChatPriority.IMPORTANT));
                }
            }

            if (pathResult == null || !pathResult.isInProgress())
            {
                pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D, job.getPonds());
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
            pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D, job.getPonds());
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
        worker.getCitizenStatusHandler().setLatestStatus(new TranslationTextComponent("com.minecolonies.coremod.status.fishing"));

        @Nullable final IAIState notReadyState = isReadyToFish();
        if (notReadyState != null)
        {
            return notReadyState;
        }

        if (caughtFish())
        {
            playCaughtFishSound();

            if(getOwnBuilding().getBuildingLevel() > LEVEL_FOR_BONUS)
            {
                final double primarySkillFactor = worker.getCitizenData().getCitizenSkillHandler().getSkills().get(getOwnBuilding().getPrimarySkill()).getB() / 10;
                final double rollResult = worker.getRandom().nextDouble() * ONE_HUNDRED_PERCENT;
                final double spongeTarget = MineColonies.getConfig().getServer().fisherSpongeChance.get() + primarySkillFactor; 
                final double shardTarget = spongeTarget + MineColonies.getConfig().getServer().fisherPrismarineChance.get() + primarySkillFactor;
                final double crystalTarget = shardTarget + MineColonies.getConfig().getServer().fisherPrismarineChance.get() + primarySkillFactor;
                ItemStack bonusItemStack = null;
                if (rollResult < spongeTarget)
                {
                    bonusItemStack = new ItemStack(Blocks.SPONGE);
                }
                else if (rollResult < shardTarget)
                {
                    bonusItemStack = new ItemStack(Items.PRISMARINE_SHARD);
                }
                else if (rollResult < crystalTarget)
                {
                    bonusItemStack = new ItemStack(Items.PRISMARINE_CRYSTALS);
                }

                if (bonusItemStack != null)
                {
                    InventoryUtils.addItemStackToItemHandler(worker.getInventoryCitizen(), bonusItemStack);
                }
            }

            this.incrementActionsDoneAndDecSaturation();

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
        SoundUtils.playSoundAtCitizenWith(world, worker.getPosition(), EventType.SUCCESS, worker.getCitizenData());
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
        if (!world.isRemote)
        {
            WorkerUtil.faceBlock(job.getWater().getA(), worker);
            world.playSound(null,
              this.worker.getPosition(),
              SoundEvents.ENTITY_FISHING_BOBBER_THROW,
              SoundCategory.NEUTRAL,
              0.5F,
              (float) (0.4D / (this.world.rand.nextFloat() * 0.4D + 0.8D)));
            this.entityFishHook = (NewBobberEntity) ModEntities.FISHHOOK.create(world);
            this.entityFishHook.setAngler((EntityCitizen) worker,
              EnchantmentHelper.getFishingLuckBonus(worker.getHeldItemMainhand()),
              (int) (5 + (getPrimarySkillLevel() / LURE_SPEED_DIVIDER) + EnchantmentHelper.getFishingSpeedBonus(worker.getHeldItemMainhand())));
            world.addEntity(this.entityFishHook);
        }

        worker.swingArm(worker.getActiveHand());
    }

    /**
     * Checks if the fishHook is stuck on land or in an entity. If the fishhook is neither in water,land nether connected with an entity, give it a time to land in water.
     *
     * @return false if the hook landed in water, else return true
     */
    private boolean isFishHookStuck()
    {
        return (!entityFishHook.isInWater() && (entityFishHook.isOnGround() || entityFishHook.shouldStopFishing()) || !entityFishHook.addedToChunk) || !entityFishHook.isAlive() || entityFishHook.caughtEntity != null;
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
        final double chance = worker.getRandom().nextInt(FISHING_DELAY) / ((getSecondarySkillLevel() / 2.0) + 1);
        return chance >= CHANCE;
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
            worker.setHeldItem(Hand.MAIN_HAND, ItemStackUtils.EMPTY);
            return PREPARING;
        }

        //If there is no close water, try to move closer
        if (!Utils.isBlockInRange(world, Blocks.WATER, (int) worker.getPosX(), (int) worker.getPosY(), (int) worker.getPosZ(), MIN_DISTANCE_TO_WATER))
        {
            return FISHERMAN_WALKING_TO_WATER;
        }

        //Check if Rod is held item if not put it as held item
        if (worker.getHeldItemMainhand() == null || (worker.getHeldItemMainhand().getItem() != worker.getItemHandlerCitizen().getStackInSlot(rodSlot).getItem()))
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
        worker.getCitizenItemHandler().setHeldItem(Hand.MAIN_HAND, getRodSlot());
    }

    /**
     * Get's the slot in which the rod is in.
     *
     * @return slot number
     */
    private int getRodSlot()
    {
        return InventoryUtils.getFirstSlotOfItemHandlerContainingTool(getInventory(), ToolType.FISHINGROD,
          TOOL_LEVEL_WOOD_OR_GOLD, getOwnBuilding().getMaxToolLevel());
    }

    /**
     * Will be called to check if the fisherman caught a fish. If the hook hasn't noticed a fish it will return false. Else the method will pick up the loot and call the method to
     * retrieve the rod.
     *
     * @return If the fisherman caught a fish.
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
            worker.swingArm(worker.getActiveHand());
            final int i = entityFishHook.getDamage();
            entityFishHook.remove();
            worker.getCitizenItemHandler().damageItemInHand(Hand.MAIN_HAND, i);
            entityFishHook = null;
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
