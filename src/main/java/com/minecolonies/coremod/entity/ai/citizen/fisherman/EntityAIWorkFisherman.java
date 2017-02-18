package com.minecolonies.coremod.entity.ai.citizen.fisherman;

import com.minecolonies.coremod.colony.buildings.BuildingFisherman;
import com.minecolonies.coremod.colony.jobs.JobFisherman;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.EntityFishHook;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAISkill;
import com.minecolonies.coremod.entity.ai.util.AIState;
import com.minecolonies.coremod.entity.ai.util.AITarget;
import com.minecolonies.coremod.entity.pathfinding.PathJobFindWater;
import com.minecolonies.coremod.sounds.FishermanSounds;
import com.minecolonies.coremod.util.InventoryUtils;
import com.minecolonies.coremod.util.SoundUtils;
import com.minecolonies.coremod.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

import static com.minecolonies.coremod.entity.ai.util.AIState.*;

/**
 * Fisherman AI class.
 * <p>
 * A fisherman has some ponds where
 * he randomly selects one and fishes there.
 * <p>
 * To keep it immersive he chooses his place at random around the pond.
 */
public class EntityAIWorkFisherman extends AbstractEntityAISkill<JobFisherman>
{

    /**
     * The render name to render fish.
     */
    private static final String RENDER_META_FISH = "Fish";

    /**
     * The render name to render fish and rod.
     */
    private static final String RENDER_META_FISHANDROD = "RodFish";

    /**
     * The render name to render rod.
     */
    private static final String RENDER_META_ROD = "Rod";

    /**
     * The maximum number of ponds to remember at one time.
     */
    private static final int MAX_PONDS = 20;

    /**
     * Variable to calculate the delay the fisherman needs to throw his rod.
     * The delay will be calculated randomly. The FISHING_DELAY defines the upper limit.
     * The delay is calculated using the CHANCE anf fishingSkill variables. A higher FISHING_DELAY will lead
     * to a longer delay.
     */
    private static final int FISHING_DELAY = 500;

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
     * The tool used by the fisherman.
     */
    private static final String TOOL_TYPE_ROD = "rod";

    /**
     * The range in which the fisherman searches water.
     */
    private static final int SEARCH_RANGE = 50;

    /**
     * The percentage of times where the fisherman will check out a new pond.
     */
    private static final double CHANCE_NEW_POND = 0.05D;

    /**
     * How often should intelligence factor into the fisherman's skill modifier.
     */
    private static final int INTELLIGENCE_MULTIPLIER = 2;

    /**
     * How often should dexterity factor into the fisherman's skill modifier.
     */
    private static final int DEXTERITY_MULTIPLIER = 1;

    /**
     * Time out fo fish again.
     */
    private static final int FISHING_TIMEOUT = 5;

    /**
     * Chance to play a specific fisherman sound.
     */
    private static final int CHANCE_TO_PLAY_SOUND = 20;

    /**
     * The number of executed adjusts of the fisherman's rotation.
     */
    private int executedRotations = 0;

    /**
     * The PathResult when the fisherman searches water.
     */
    @Nullable
    private PathJobFindWater.WaterPathResult pathResult;

    /**
     * The Previous PathResult when the fisherman already found water.
     */
    @Nullable
    private PathJobFindWater.WaterPathResult lastPathResult;

    /**
     * The fishingSkill which directly influences the fisherman's chance to throw his rod.
     * May in the future also influence his luck/charisma.
     */
    private int fishingSkill = worker.getLevel();

    /**
     * Connects the citizen with the fishingHook.
     */
    @Nullable
    private EntityFishHook entityFishHook;

    @NotNull
    private final Random random = new Random();

    /**
     * Constructor for the Fisherman.
     * Defines the tasks the fisherman executes.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkFisherman(@NotNull final JobFisherman job)
    {
        super(job);
        super.registerTargets(
          new AITarget(IDLE, START_WORKING),
          new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
          new AITarget(PREPARING, this::prepareForFishing),
          new AITarget(FISHERMAN_CHECK_WATER, this::tryDifferentAngles),
          new AITarget(FISHERMAN_SEARCHING_WATER, this::findWater),
          new AITarget(FISHERMAN_WALKING_TO_WATER, this::getToWater),
          new AITarget(FISHERMAN_START_FISHING, this::doFishing)
        );
        worker.setSkillModifier(
          INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
            + DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity());
        worker.setCanPickUpLoot(true);
    }

    /**
     * Redirects the fisherman to his building.
     *
     * @return the next state.
     */
    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return PREPARING;
    }

    /**
     * Prepares the fisherman for fishing and
     * requests fishingRod and checks if the fisherman already had found a pond.
     *
     * @return the next AIState
     */
    private AIState prepareForFishing()
    {
        if (checkOrRequestItems(false, new ItemStack(Items.FISHING_ROD)))
        {
            playNeedRodSound();
            return getState();
        }
        if (job.getWater() == null)
        {
            return FISHERMAN_SEARCHING_WATER;
        }
        return FISHERMAN_WALKING_TO_WATER;
    }

    /**
     * Plays a sound when the fisherman needs a rod.
     */
    private void playNeedRodSound()
    {
        if (worker != null)
        {
            final SoundEvent needFishingRod = worker.isFemale() ? FishermanSounds.Female.needFishingRod : FishermanSounds.Male.needFishingRod;
            SoundUtils.playSoundAtCitizenWithChance(world, worker.getPosition(), needFishingRod, CHANCE_TO_PLAY_SOUND);
        }
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     * <p>
     * Override this to change the value.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return MAX_FISHES_IN_INV;
    }

    /**
     * Returns the fisherman's work building.
     *
     * @return building instance
     */
    @Override
    protected BuildingFisherman getOwnBuilding()
    {
        return (BuildingFisherman) worker.getWorkBuilding();
    }

    /**
     * Can be overridden in implementations.
     * <p>
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
        return InventoryUtils.hasitemInInventory(getInventory(), Items.FISH, -1);
    }

    /**
     * Checks if the fisherman has a rod in his inventory but if he did not equip it.
     *
     * @return true if so.
     */
    private boolean hasRodButNotEquipped()
    {
        return worker.hasItemInInventory(Items.FISHING_ROD, -1) && worker.getHeldItemMainhand() != null && !(worker.getHeldItemMainhand().getItem() instanceof ItemFishingRod);
    }

    /**
     * If the job class has no water object the fisherman should search water.
     *
     * @return the next AIState the fisherman should switch to, after executing this method.
     */
    private AIState getToWater()
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
        return job.getWater() != null && walkToBlock(job.getWater());
    }

    /**
     * Rotates the fisherman to guarantee that the fisherman throws his rod in the correct direction.
     *
     * @return the next AIState the fisherman should switch to, after executing this method.
     */
    @NotNull
    private AIState tryDifferentAngles()
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
        //Try a different angle to throw the hook not that far
        worker.faceBlock(job.getWater());
        executedRotations++;
        return FISHERMAN_START_FISHING;
    }

    /**
     * Checks if the fisherman already has found 20 pools, if yes search a water pool out of these 20, else
     * search a new one.
     *
     * @return the next AIState the fisherman should switch to, after executing this method.
     */
    private AIState findWater()
    {
        //Reset executedRotations when fisherman searches a new Pond
        executedRotations = 0;
        //If he can't find any pond, tell that to the player
        //If 20 ponds are already stored, take a random stored location
        if (job.getPonds().size() >= MAX_PONDS)
        {
            return setRandomWater();
        }
        return findNewWater();
    }

    /**
     * If the fisherman can't find 20 ponds or already has found 20, the fisherman should randomly choose a fishing spot
     * from the previously found ones.
     *
     * @return the next AIState.
     */
    private AIState setRandomWater()
    {
        if (job.getPonds().isEmpty())
        {
            if (lastPathResult != null && lastPathResult.isEmpty && !lastPathResult.isCancelled())
            {
                chatSpamFilter.talkWithoutSpam("entity.fisherman.messageWaterTooFar");
            }
            pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D, job.getPonds());
            return getState();
        }
        job.setWater(job.getPonds().get(random.nextInt(job.getPonds().size())));

        return FISHERMAN_CHECK_WATER;
    }

    /**
     * Uses the pathFinding system to search close water spots which possibilitate fishing.
     * Sets a number of possible water pools and sets the water pool the fisherman should fish now.
     *
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState findNewWater()
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
        if (pathResult.getPathReachesDestination())
        {
            if (pathResult.pond != null)
            {
                job.setWater(pathResult.pond);
                job.addToPonds(pathResult.pond);
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
     * Main fishing methods,
     * let's the fisherman gather xp orbs next to him,
     * check if all requirements to fish are given.
     * Actually fish, retrieve his rod if stuck or if a fish bites.
     *
     * @return the next AIState the fisherman should switch to, after executing this method.
     */
    @Nullable
    private AIState doFishing()
    {
        worker.gatherXp();
        @Nullable final AIState notReadyState = isReadyToFish();
        if (notReadyState != null)
        {
            return notReadyState;
        }
        if (caughtFish())
        {
            this.getOwnBuilding().getColony().incrementStatistic("fish");
            playCaughtFishSound();
            if (random.nextDouble() < CHANCE_NEW_POND)
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
        if (worker != null)
        {
            final SoundEvent iGotOne = worker.isFemale() ? FishermanSounds.Female.iGotOne : FishermanSounds.Male.iGotOne;
            SoundUtils.playSoundAtCitizenWithChance(world, worker.getPosition(), iGotOne, CHANCE_TO_PLAY_SOUND);
        }
    }

    /**
     * Check if a hook is out there,
     * and throw/retrieve it if needed.
     *
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState throwOrRetrieveHook()
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
                retrieveRod();
                return FISHERMAN_WALKING_TO_WATER;
            }
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
            worker.faceBlock(job.getWater());
            world.playSound((EntityPlayer) null,
              this.worker.getPosition(),
              SoundEvents.ENTITY_BOBBER_THROW,
              SoundCategory.NEUTRAL,
              0.5F,
              (float) (0.4D / (this.world.rand.nextFloat() * 0.4D + 0.8D)));
            this.entityFishHook = new EntityFishHook(world, this.getCitizen());
            world.spawnEntityInWorld(this.entityFishHook);
        }

        worker.swingArm(worker.getActiveHand());
    }

    /**
     * Checks if the fishHook is stuck on land or in an entity.
     * If the fishhook is neither in water,land nether connected with an entity, give it a time to land in water.
     *
     * @return false if the hook landed in water, else return true
     */
    private boolean isFishHookStuck()
    {
        return (!entityFishHook.isInWater() && (entityFishHook.onGround || entityFishHook.fishHookIsOverTimeToLive())) || entityFishHook.isDead;
    }

    /**
     * Checks how lucky the fisherman is.
     * <p>
     * This check depends on his fishing skill.
     * Which in turn depends on intelligence.
     *
     * @return true if he has to wait.
     */
    private boolean testRandomChance()
    {
        //+1 since the level may be 0
        setDelay(FISHING_TIMEOUT);
        final double chance = random.nextInt(FISHING_DELAY) / (double) (fishingSkill + 1);
        return chance >= CHANCE;
    }

    /**
     * Checks if the fisherman has his fishingRod in his hand and is close to the water.
     *
     * @return true if fisherman meets all requirements to fish, else returns false.
     */
    private AIState isReadyToFish()
    {
        //We really do have our Rod in our inventory?
        if (!worker.hasItemInInventory(Items.FISHING_ROD, -1))
        {
            return PREPARING;
        }

        if (world.getBlockState(worker.getPosition()).getBlock() == Blocks.WATER)
        {
            job.removeFromPonds(job.getWater());
            job.setWater(null);
            return FISHERMAN_SEARCHING_WATER;
        }
        //If there is no close water, try to move closer
        if (!Utils.isBlockInRange(world, Blocks.WATER, (int) worker.posX, (int) worker.posY, (int) worker.posZ, MIN_DISTANCE_TO_WATER))
        {
            return FISHERMAN_WALKING_TO_WATER;
        }

        //Check if Rod is held item if not put it as held item
        if (worker.getHeldItemMainhand() == null || !worker.getHeldItemMainhand().getItem().equals(Items.FISHING_ROD))
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
        worker.setHeldItem(getRodSlot());
    }

    /**
     * Get's the slot in which the rod is in.
     *
     * @return slot number
     */
    private int getRodSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_ROD);
    }

    /**
     * Will be called to check if the fisherman caught a fish. If the hook hasn't noticed a fish it will return false.
     * Else the method will pick up the loot and call the method to retrieve the rod.
     *
     * @return If the fisherman caught a fish.
     */
    private boolean caughtFish()
    {
        if (entityFishHook == null)
        {
            return false;
        }
        if (!entityFishHook.caughtFish())
        {
            return false;
        }

        worker.setCanPickUpLoot(true);
        worker.captureDrops = true;
        retrieveRod();
        fishingSkill = worker.getLevel();
        this.incrementActionsDone();
        return true;
    }

    /**
     * Retrieves the previously thrown fishingRod.
     * If the fishingRod still has a hook connected to it, destroy the hook object.
     */
    private void retrieveRod()
    {
        worker.swingArm(worker.getActiveHand());
        final int i = entityFishHook.getDamage(this.getCitizen());
        worker.damageItemInHand(i);
        entityFishHook = null;
    }

    /**
     * Returns the fisherman's worker instance. Called from outside this class.
     *
     * @return citizen object.
     */
    @Nullable
    public EntityCitizen getCitizen()
    {
        return worker;
    }
}
