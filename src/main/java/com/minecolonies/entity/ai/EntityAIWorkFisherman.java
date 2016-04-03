package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.entity.pathfinding.PathJobFindWater;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.InventoryUtils;
import com.minecolonies.util.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.minecolonies.entity.ai.AIState.*;

/**
 * Fisherman AI class
 * <p>
 * A fisherman has some ponds where
 * he randomly selects one and fishes there.
 * <p>
 * To keep it immersive he chooses his place at random around the pond.
 *
 * @author Raycoms, Kostronor
 */
public class EntityAIWorkFisherman extends AbstractEntityAIWork<JobFisherman>
{
    /**
     * The maximum number of ponds to remember at one time.
     */
    private static final int MAX_PONDS = 20;
    private static final int FISHING_DELAY = 500;
    private static final int CHANCE = 2;
    private static final int MIN_DISTANCE_TO_WATER = 3;
    private static final int MAX_FISHES_IN_INV = 10;
    private static final int MAX_ROTATIONS = 12;
    private static final String TOOL_TYPE_ROD = "rod";
    private static final int SEARCH_RANGE = 50;
    private static final Logger logger = LogManager.getLogger("Fisherman");
    private int fishesCaught = 0;
    private PathJobFindWater.WaterPathResult pathResult;
    private int fishingSkill = worker.getIntelligence() * worker.getSpeed() * (worker.getExperienceLevel() + 1);
    private int executedRotations = 0;
    //Connects the fishingHook with the citizen
    private EntityFishHook fishEntity;


    /**
     * Constructor for the Fisherman.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkFisherman(JobFisherman job)
    {
        super(job);
        super.registerTargets(
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(PREPARING, this::prepareForFishing),
                new AITarget(FISHERMAN_CHECK_WATER, this::tryDifferentAngles),
                new AITarget(FISHERMAN_SEARCHING_WATER, this::findWater),
                new AITarget(FISHERMAN_WATER_FOUND, this::getToWater),
                new AITarget(FISHERMAN_START_FISHING, this::doFishing)
                             );
    }

    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return state;
        }
        return PREPARING;
    }

    private AIState prepareForFishing()
    {
        if (checkOrRequestItems(new ItemStack(Items.fishing_rod)))
        {
            return state;
        }
        if (job.getWater() == null)
        {
            return FISHERMAN_SEARCHING_WATER;
        }
        return FISHERMAN_WATER_FOUND;
    }

    @Override
    protected boolean wantInventoryDumped()
    {
        if (fishesCaught > MAX_FISHES_IN_INV)
        {
            fishesCaught = 0;
            job.setWater(null);

            return true;
        }
        return false;
    }

    /**
     * This method will be overridden by AI implementations.
     * It will serve as a tick function.
     */
    @Override
    protected void workOnTask()
    {
        //Migration to new system complete
    }

    @Override
    protected BuildingFisherman getOwnBuilding()
    {
        return (BuildingFisherman) worker.getWorkBuilding();
    }

    @Override
    protected boolean neededForWorker(ItemStack stack)
    {
        return isStackRod(stack);
    }

    private static boolean isStackRod(ItemStack stack)
    {
        return stack != null && stack.getItem().equals(Items.fishing_rod);
    }

    /**
     * If the job class has no water object the fisherman should search water.
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState getToWater()
    {
        if (job.getWater() == null)
        {
            return FISHERMAN_SEARCHING_WATER;
        }
        if (walkToWater())
        {
            return state;
        }
        return FISHERMAN_CHECK_WATER;
    }

    /**
     * Let's the fisherman walk to the water if the water object in his job class has been filled.
     * @return true if the fisherman has arrived at the water
     */
    private boolean walkToWater()
    {
        return !(job.getWater() == null || job.getWater().getLocation() == null) && walkToBlock(job.getWater()
                                                                                                   .getLocation());
    }

    /**
     * Rotates the fisherman to guarantee that the fisherman throws his rod in the correct direction
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState tryDifferentAngles()
    {
        if (executedRotations >= MAX_ROTATIONS)
        {
            job.removeFromPonds(job.getWater().getLocation());
            job.setWater(null);
            executedRotations=0;
            return FISHERMAN_SEARCHING_WATER;
        }
        //Try a different angle to throw the hook not that far
        worker.faceBlock(job.getWater().getLocation());
        executedRotations++;
        return FISHERMAN_START_FISHING;
    }

    /**
     * Checks if the fisherman already has found 20 pools, if yes search a water pool out of these 20, else
     * search a new one.
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState findWater()
    {
        //Reset executedRotations when fisherman searches a new Pond
        executedRotations=0;
        //If he can't find any pond, tell that to the player
        //If 20 ponds are already stored, take a random stored location
        if (job.getPonds().size() >= MAX_PONDS)
        {
            return setRandomWater();
        }
        return findNewWater();
    }

    /**
     * Uses the pathFinding system to search close water spots which possibilitate fishing.
     * Sets a number of possible water pools and sets the water pool the fisherman should fish now.
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState findNewWater()
    {
        if (pathResult == null)
        {
            pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D, job.getPonds());
            return state;
        }
        if (pathResult.isComputedAndDoesntReachDestination())
        {
            return setRandomWater();
        }
        if (pathResult.getPathReachesDestination())
        {
            if (pathResult.pond != null)
            {
                job.setWater(new Water(world, pathResult.pond));
                job.addToPonds(pathResult.pond);
            }
            pathResult = null;
            return FISHERMAN_CHECK_WATER;
        }
        if (pathResult.isCancelled())
        {
            pathResult = null;
            return PREPARING;
        }
        return state;
    }

    private AIState setRandomWater()
    {
        if (job.getPonds().isEmpty())
        {
            chatSpamFilter.talkWithoutSpam("entity.fisherman.messageWaterTooFar");
            pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D, job.getPonds());
            return state;
        }
        job.setWater(new Water(world, job.getPonds().get(itemRand.nextInt(job.getPonds().size()))));
        return FISHERMAN_CHECK_WATER;
    }

    /**
     *  Main fishing methods, let's the fisherman gather xp orbs next to him, check if all requirements to fish are given.
     *  Actually fish, retrieve his rod if stuck or if a fish bites.
     * @return the next AIState the fisherman should switch to, after executing this method
     */
    private AIState doFishing()
    {
        worker.gatherXp();
        AIState notReadyState = isReadyToFish();
        if (notReadyState != null)
        {
            return notReadyState;
        }
        else if (caughtFish())
        {
            if(fishesCaught>=MAX_FISHES_IN_INV)
            {
                job.setWater(null);
                return FISHERMAN_SEARCHING_WATER;
            }
            return FISHERMAN_WATER_FOUND;
        }

        if (getFishEntity() == null)
        {
            //Only sometimes the fisherman gets to throw its Rod (depends on intelligence)
            if (testRandomChance())
            {
                return state;
            }
            throwRod();
        }
        else
        {
            //Check if hook landed on ground or in water, in some cases the hook bugs -> remove it after 2 minutes.
            if (isFishHookStuck())
            {
                retrieveRod();
                return FISHERMAN_WATER_FOUND;
            }
        }
        return state;
    }

    /**
     * Let's the fisherman face the water, play the throw sound and create the fishingHook and throw it
     */
    private void throwRod()
    {
        if (!world.isRemote)
        {
            worker.faceBlock(job.getWater().getLocation());
            world.playSoundAtEntity(worker,
                    "random.bow",
                    0.5F,
                    (float) (0.4D / (itemRand.nextDouble() * 0.4D + 0.8D)));
            EntityFishHook hook = new EntityFishHook(world, this);
            setFishEntity(hook);
            world.spawnEntityInWorld(hook);
        }

        worker.swingItem();
    }

    /**
     * Checks if the fisherman has his fishingRod in his hand and is close to the water
     * @return true if fisherman meets all requirements to fish, else returns false

     */
    private AIState isReadyToFish()
    {
        //We really do have our Rod in our inventory?
        if (!worker.hasItemInInventory(Items.fishing_rod))
        {
            return PREPARING;
        }

        //If there is no close water, try to move closer
        if (!isCloseToWater())
        {
            return FISHERMAN_WATER_FOUND;
        }

        //Check if Rod is held item if not put it as held item
        if (worker.getHeldItem() == null || !worker.getInventory().getHeldItem().getItem().equals(Items.fishing_rod))
        {
            equipRod();
            return state;
        }
        return null;
    }

    /**
     * Checks if there is water really close to the fisherman
     * @return true if he found close water
     */
    private boolean isCloseToWater()
    {
        boolean foundCloseWater = false;
        for (int x = (int) worker.posX - MIN_DISTANCE_TO_WATER; x < (int) worker.posX + MIN_DISTANCE_TO_WATER; x++)
        {
            for (int z = (int) worker.posZ - MIN_DISTANCE_TO_WATER; z < (int) worker.posZ + MIN_DISTANCE_TO_WATER; z++)
            {
                for (int y = (int) worker.posY - MIN_DISTANCE_TO_WATER; y
                                                                        < (int) worker.posY
                                                                          + MIN_DISTANCE_TO_WATER; y++)
                {
                    if (world.getBlock(x, y, z).equals(Blocks.water))
                    {
                        foundCloseWater = true;
                    }
                }
            }
        }
        return foundCloseWater;
    }


    /**
     * Checks if the fishHook is stuck on land or in an entity.
     * If the fishhook is neither in water,land nether connected with an entity, give it a time to land in water.
     * @return false if the hook landed in water, else return true
     */
    private boolean isFishHookStuck()
    {
        return !getFishEntity().isInWater() && (getFishEntity().onGround || getFishEntity().hasHitEntity()
                                                       || Utils.nanoSecondsToSeconds(System.nanoTime() - getFishEntity().getCreationTime())
                                                          > getFishEntity().getTtl());
    }

    /**
     * Will be called to check if the fisherman caught a fish. If the hook hasn't noticed a fish it will return false.
     * Else the method will pick up the loot and call the method to retrieve the rod.
     * @return If the fisherman caught a fish
     */
    private boolean caughtFish()
    {
        if (!fishEntity.caughtFish() || getFishEntity() == null)
        {
            fishEntity.setCaughtFish(false);
            return false;
        }

        worker.setCanPickUpLoot(true);
        worker.captureDrops = true;
        retrieveRod();
        fishingSkill = worker.getIntelligence() * worker.getSpeed() * (worker.getExperienceLevel() + 1);
        fishEntity.setCaughtFish(false);
        fishesCaught++;
        return true;
    }

    /**
     * Retrieves the previously thrown fishingRod.
     * If the fishingRod still has a hook connected to it, destroy the hook object
     */
    private void retrieveRod()
    {
        worker.swingItem();
        int i = getFishEntity().getDamage();
        worker.damageItemInHand(i);

        //May already be null if the itemInHand has been destroyed
        if (getFishEntity() != null)
        {
            getFishEntity().setDead();
            setFishEntity(null);
        }
    }

    private void equipRod()
    {
        worker.setHeldItem(getRodSlot());
    }

    private int getRodSlot()
    {
        return InventoryUtils.getFirstSlotContainingTool(getInventory(), TOOL_TYPE_ROD);
    }

    private InventoryCitizen getInventory()
    {
        return worker.getInventory();
    }

    public EntityFishHook getFishEntity()
    {
        return fishEntity;
    }

    public void setFishEntity(EntityFishHook fishEntity)
    {
        this.fishEntity = fishEntity;
    }

    public EntityCitizen getCitizen()
    {
        return worker;
    }

    /**
     * Checks how lucky the fisherman is.
     * <p>
     * This check depends on his fishing skill.
     * Which in turn depends on intelligence.
     *
     * @return true if he has to wait
     */
    private boolean testRandomChance()
    {
        //+1 since the level may be 0
        double chance = itemRand.nextInt(FISHING_DELAY) / (double)(fishingSkill + 1);
        return chance >= CHANCE;
    }

}
