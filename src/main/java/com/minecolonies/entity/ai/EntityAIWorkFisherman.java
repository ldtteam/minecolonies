package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.BuildingFisherman;
import com.minecolonies.colony.jobs.JobFisherman;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.EntityFishHook;
import com.minecolonies.entity.pathfinding.PathJobFindWater;
import com.minecolonies.inventory.InventoryCitizen;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

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
    private static final int DELAY = 100;
    /**
     * TODO: fix this with ttl
     */
    private static final int NANO_TIME_DIVIDER = 1000 * 1000 * 1000;
    private static final float ROTATION_ANGLE = 90F;
    private static final String TOOL_TYPE_ROD = "rod";
    /**
     * TODO: add actual rendering of the fish
     */
    private static final String RENDER_META_FISH = "fish";
    private static final int SEARCH_RANGE = 50;
    private static final Logger logger = LogManager.getLogger("Fisherman");
    private int fishesCaught = 0;
    private PathJobFindWater.WaterPathResult pathResult;
    /**
     * TODO: don't just assign it one time at class initialization.
     */
    private int fishingSkill = worker.getIntelligence() * worker.getSpeed() * (worker.getExperienceLevel() + 1);

    /**
     * Constructor for the Fisherman.
     *
     * @param job a fisherman job to use.
     */
    public EntityAIWorkFisherman(JobFisherman job)
    {
        super(job);
    }

    @Override
    protected boolean wantInventoryDumped()
    {
        if (fishesCaught > MAX_FISHES_IN_INV)
        {
            fishesCaught = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void updateRenderMetaData()
    {
        String renderMetaData = getRenderMetaFish();
        //TODO: Have rod displayed as well?
    }

    //TODO Render model ROD/Fish
    private String getRenderMetaFish()
    {
        if (worker.hasItemInInventory(Items.fish))
        {
            return RENDER_META_FISH;
        }
        return "";
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

    private boolean isStackRod(ItemStack stack)
    {
        return stack != null && stack.getItem().equals(Items.fishing_rod);
    }

    @Override
    public void workOnTask()
    {
        //Fisherman wants to work but is not at building
        if (job.getStage() == Stage.START_WORKING)
        {
            if (walkToBuilding())
            {
                return;
            }
            //Fisherman is at building
            job.setStage(Stage.PREPARING);
            return;
        }

        //Fisherman is at building and prepares for work
        if (job.getStage() == Stage.PREPARING)
        {
            if (worker.hasItemInInventory(Items.fishing_rod))
            {
                if (job.water == null)
                {
                    job.setStage(Stage.SEARCHING_WATER);
                    return;
                }
                job.setStage(Stage.WATER_FOUND);
            }
            else
            {
                requestTool();
            }
            return;
        }

        //Looking for the water to walk to
        if (job.getStage() == Stage.SEARCHING_WATER)
        {
            findWater();
            return;
        }

        //Walking to the water to check out the mine
        if (job.getStage() == Stage.WATER_FOUND)
        {
            if (job.water == null)
            {
                job.setStage(Stage.SEARCHING_WATER);
                return;
            }
            if (walkToWater())
            {
                return;
            }
            job.setStage(Stage.CHECK_WATER);
        }

        //Standing at the water, checking out pond
        if (job.getStage() == Stage.CHECK_WATER)
        {
            //TODO After the executed a full rotation choose a new fishing spot!
            int x = (int) (Math.random() * 3);

            //TODO: animation stuff on client only
            if (x == 1)
            {
                setAngles(worker, worker.rotationYaw, 400);
            }
            else
            {
                setAngles(worker, ROTATION_ANGLE, worker.rotationPitch);
            }

            //TODO Different angle to throw the hook not that far
            worker.getLookHelper();

            job.setStage(Stage.START_FISHING);
            return;
        }

        if (job.getStage() == Stage.START_FISHING)
        {
            doFishing();
            return;
        }
        setDelay(DELAY);
    }

    /**
     * shitty fix for a shitty method on client side only.
     * TODO: fix this for real....
     *
     * @param worker
     * @param a
     * @param b
     */
    public void setAngles(EntityCitizen worker, float a, float b)
    {
        float f2 = worker.rotationPitch;
        float f3 = worker.rotationYaw;
        worker.rotationYaw = (float) ((double) worker.rotationYaw + (double) a * 0.15D);
        worker.rotationPitch = (float) ((double) worker.rotationPitch - (double) b * 0.15D);

        if (worker.rotationPitch < -90.0F)
        {
            worker.rotationPitch = -90.0F;
        }

        if (worker.rotationPitch > 90.0F)
        {
            worker.rotationPitch = 90.0F;
        }

        worker.prevRotationPitch += worker.rotationPitch - f2;
        worker.prevRotationYaw += worker.rotationYaw - f3;
    }

    private boolean walkToWater()
    {
        return !(job.water == null || job.water.getLocation() == null) && walkToBlock(job.water.getLocation());
    }

    private void findWater()
    {
        //If 20 ponds are already stored, take a random stored location
        if (job.ponds.size() >= MAX_PONDS)
        {
            job.water = new Water(world, job.ponds.get(new Random().nextInt(MAX_PONDS)));
            return;
        }

        if (pathResult == null || (!pathResult.isComputing() && !pathResult.getPathReachesDestination()))
        {
            pathResult = worker.getNavigator().moveToWater(SEARCH_RANGE, 1.0D, job.ponds);
        }
        else if (pathResult.getPathReachesDestination())
        {
            if (pathResult.ponds != null)
            {
                job.water = new Water(world, pathResult.ponds);
                job.ponds.add(pathResult.ponds);
            }
            pathResult = null;
            job.setStage(Stage.CHECK_WATER);
        }
        else if (pathResult.isCancelled())
        {
            job.setStage(Stage.PREPARING);
            pathResult = null;
        }

    }

    private void requestTool()
    {
        job.setStage(Stage.PREPARING);
        needsRod = true;
    }

    private void doFishing()
    {

        //We really do have our Rod in our inventory?
        if (!worker.hasItemInInventory(Items.fishing_rod))
        {
            job.setStage(Stage.PREPARING);
            return;
        }

        //Check if there is water really close to me!
        boolean foundCloseWater = false;
        for (int x = (int) worker.posX - MIN_DISTANCE_TO_WATER; x < (int) worker.posX + MIN_DISTANCE_TO_WATER; x++)
        {
            for (int z = (int) worker.posZ - MIN_DISTANCE_TO_WATER; z < (int) worker.posZ + MIN_DISTANCE_TO_WATER; z++)
            {
                if (world.getBlock(x, (int) worker.posY - 1, z).equals(Blocks.water))
                {
                    foundCloseWater = true;
                }
            }
        }

        //If there is no close water, try to move closer
        if (!foundCloseWater)
        {
            job.setStage(Stage.WATER_FOUND);
            return;
        }

        //Check if Rod is held item if not put it as held item
        if (worker.getHeldItem() == null || !worker.getInventory().getHeldItem().getItem().equals(Items.fishing_rod))
        {
            equipRod();
        }

        for (EntityXPOrb orb : worker.getXPOrbsOnGrid())
        {
            worker.addExperience(orb.getXpValue());
            orb.setDead();
        }

        if (worker.isCaughtFish())
        {
            if (worker.getFishEntity() == null)
            {
                worker.setCaughtFish(false);
                return;
            }

            worker.setCanPickUpLoot(true);
            worker.captureDrops = true;

            int i = worker.getFishEntity().func_146034_e();
            worker.damageItemInHand(i);
            worker.swingItem();
            if (worker.getFishEntity() != null)
            {
                worker.getFishEntity().setDead();
                worker.setFishEntity(null);
            }

            worker.setCaughtFish(false);
            fishesCaught++;
            return;
        }

        if (worker.getFishEntity() == null)
        {
            //Only sometimes the fisherman gets to throw its Rod (depends on intelligence)
            if (testRandomChance())
            {
                return;
            }

            if (!world.isRemote)
            {
                world.playSoundAtEntity(worker,
                                        "random.bow",
                                        0.5F,
                                        (float) (0.4D / (itemRand.nextDouble() * 0.4D + 0.8D)));
                EntityFishHook hook = new EntityFishHook(world, worker);
                worker.setFishEntity(hook);
                world.spawnEntityInWorld(hook);
            }

            worker.swingItem();
        }
        else
        {
            //Check if hook landed on ground or in water, in some cases the hook bugs -> remove it after 2 minutes
            if (!worker.getFishEntity().isInWater() && (worker.getFishEntity().onGround
                                                        || (System.nanoTime() - worker.getFishEntity()
                                                                                      .getCreationTime())
                                                           / NANO_TIME_DIVIDER
                                                           > worker.getFishEntity().getTtl()))
            {
                worker.swingItem();
                int i = worker.getFishEntity().func_146034_e();
                worker.damageItemInHand(i);

                //May already be null if the itemInHand has been destroyed
                if (worker.getFishEntity() != null)
                {
                    worker.getFishEntity().setDead();
                    worker.setFishEntity(null);
                }

                job.setStage(Stage.WATER_FOUND);
            }
        }
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
        double chance = (Math.random() * FISHING_DELAY) / (fishingSkill + 1);
        return chance >= CHANCE;
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

    public enum Stage
    {
        IDLE,
        START_WORKING,
        CHECK_WATER,
        WATER_FOUND,
        SEARCHING_WATER,
        START_FISHING,
        PREPARING,
    }
}
