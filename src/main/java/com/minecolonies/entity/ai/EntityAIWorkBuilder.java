package com.minecolonies.entity.ai;

import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.BlockUtils;
import com.minecolonies.util.EntityUtils;
import com.minecolonies.util.InventoryUtils;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.minecolonies.entity.ai.AIState.*;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIWorkBuilder extends AbstractEntityAIWork<JobBuilder>
{
    /**
     * The localization key for start building messages
     */
    private static final String ENTITY_BUILDER_MESSAGE_BUILD_START    = "entity.builder.messageBuildStart";
    /**
     * The localization key for finish building messages
     */
    private static final String ENTITY_BUILDER_MESSAGE_BUILD_COMPLETE = "entity.builder.messageBuildComplete";
    /**
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally)
     */
    private static final double XP_EACH_BUILDING                      = 2.5;
    /**
     * How often should dexterity factor into the builders skill modifier.
     */
    private static final int    DEXTERITY_MULTIPLIER                  = 2;
    /**
     * How often should strength factor into the builders skill modifier.
     */
    private static final int    STRENGTH_MULTIPLIER                   = 1;
    /**
     * The time in ticks to wait until checking for new building tasks
     */
    private static final int    IDLE_WAIT_TIME                        = 100;
    /**
     * The amount of blocks away from his working position until the builder will build
     */
    private static final int    BUILDING_WALK_RANGE                   = 10;
    /**
     * Flags 1 and 2 to send update to client
     */
    private static final int    BLOCK_PLACE_FLAGS                     = 0x03;
    /**
     * Position where the Builders constructs from.
     */
    private BlockPos workFrom;

    /**
     * Create a new builder ai and register all actions it will perform.
     *
     * @param job the job to execute this ai on
     */
    public EntityAIWorkBuilder(JobBuilder job)
    {
        super(job);
        super.registerTargets(
                new AITarget(this::checkIfExecute),
                new AITarget(IDLE, () -> BUILDER_CLEAR_STEP),
                new AITarget(BUILDER_CLEAR_STEP, this::clearStep),
                new AITarget(BUILDER_REQUEST_MATERIALS, this::requestMaterials),
                new AITarget(BUILDER_STRUCTURE_STEP, stepProducer(this::findNextBlockSolid, true)),
                new AITarget(BUILDER_DECORATION_STEP, stepProducer(this::findNextBlockNonSolid, false)),
                new AITarget(BUILDER_COMPLETE_BUILD, this::completeBuild)
                             );
        worker.setSkillModifier(DEXTERITY_MULTIPLIER * worker.getCitizenData().getDexterity()
                                + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        //enable vanilla mc item pickup
        worker.setCanPickUpLoot(true);
    }

    /**
     * Performs some cleanup to ensure that the builder always is in a good state.
     *
     * @return false if all is good to go
     */
    private AIState checkIfExecute()
    {
        setDelay(1);

        //If we are idle, wait for some time
        if (!job.hasWorkOrder())
        {
            setDelay(IDLE_WAIT_TIME);
            return this.getState();
        }

        WorkOrderBuild workOrder = job.getWorkOrder();

        //cleanup dead jobs
        if (null == workOrder
            || null == job.getColony().getBuilding(workOrder.getBuildingId()))
        {
            job.complete();
            return this.getState();
        }

        //create a schematic for a build if it not exist
        if (!workOrder.hasSchematic())
        {
            initializeWorkOrderSchematic();
        }

        //all systems go, continue executing
        return null;
    }

    /**
     * Initialize the schematic world for the current work order.
     * <p>
     * Only call this if there is no schematic present
     */
    private void initializeWorkOrderSchematic()
    {

        workFrom = null;

        WorkOrderBuild workOrder = job.getWorkOrder();
        //Load the appropriate schematic into the work order
        workOrder.loadSchematic(world);

        //Send a chat message that we start working
        talkStartBuilding();
    }

    /**
     * Send a chat message telling that the current building is started
     * Will rely on having a current job
     */
    private void talkStartBuilding()
    {
        worker.sendLocalizedChat(ENTITY_BUILDER_MESSAGE_BUILD_START, job.getWorkOrder().getSchematicName());
    }

    /**
     * Will lead the worker to a good position to construct.
     *
     * @return false if the position has been reached.
     */
    private boolean walkToConstructionSite()
    {
        if (workFrom == null)
        {
            workFrom = getWorkingPosition();
        }
        return walkToBlock(workFrom, BUILDING_WALK_RANGE);
    }

    /**
     * Calculates the working position. Takes a min distance from width and length.
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition()
    {
        //get length or width either is larger.
        int          length     = job.getWorkOrder().getLength();
        int          width      = job.getWorkOrder().getWidth();
        int          distance   = Math.max(width, length);
        EnumFacing[] directions = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};

        //then get a solid place with two air spaces above it in any direction.
        for (EnumFacing direction : directions)
        {
            BlockPos positionInDirection = getPositionInDirection(direction, distance);
            if (EntityUtils.checkForFreeSpace(world, positionInDirection))
            {
                return positionInDirection;
            }
        }

        //if necessary we can could implement calling getWorkingPosition recursively and add some "offset" to the sides.
        return job.getWorkOrder().getCurrentBlockPosition();
    }

    /**
     * Gets a floorPosition in a particular direction
     *
     * @param facing   the direction
     * @param distance the distance
     * @return a BlockPos position.
     */
    private BlockPos getPositionInDirection(EnumFacing facing, int distance)
    {
        return getFloor(job.getWorkOrder().getCurrentBlockPosition().offset(facing, distance));
    }

    /**
     * Calculates the floor level
     *
     * @param position input position
     * @return returns BlockPos position with air above
     */
    private BlockPos getFloor(BlockPos position)
    {
        //If the position is floating in Air go downwards
        if (!EntityUtils.solidOrLiquid(world, position))
        {
            return getFloor(position.down());
        }
        //If there is no air above the block go upwards
        if (!EntityUtils.solidOrLiquid(world, position.up()))
        {
            return position;
        }
        return getFloor(position.up());
    }

    private AIState clearStep()
    {
        if (job.getWorkOrder().isCleared())
        {
            return AIState.BUILDER_STRUCTURE_STEP;
        }

        //get the current working position and block
        BlockPos coordinates = job.getWorkOrder().getCurrentBlockPosition();
        Block    worldBlock  = world.getBlockState(coordinates).getBlock();

        //Don't break bedrock etc.
        if (!BlockUtils.shouldNeverBeMessedWith(worldBlock))
        {
            //Fill workFrom with the position from where the builder should build.
            //also ensure we are at that position
            if (walkToConstructionSite())
            {
                return this.getState();
            }

            worker.faceBlock(coordinates);
            //We need to deal with materials
            if (Configurations.builderInfiniteResources
                || BlockUtils.freeToPlace(worldBlock))
            {
                worker.setCurrentItemOrArmor(0, null);

                world.setBlockToAir(coordinates);
                worker.swingItem();
            }
            else
            {
                if (!mineBlock(coordinates))
                {
                    return this.getState();
                }
            }
        }

        //If we are done with clearing, move on to the next phase
        if (!job.getWorkOrder().doneWithClear())
        {
            job.getWorkOrder().setCleared();
            return AIState.BUILDER_REQUEST_MATERIALS;
        }
        return this.getState();
    }

    private AIState requestMaterials()
    {
        //We need to deal with materials
        if (!Configurations.builderInfiniteResources)
        {
            //TODO thread this
            while (job.getWorkOrder().findNextBlockNotEqual())
            {

                Block       block     = job.getWorkOrder().getCurrentBlock();
                IBlockState metadata  = job.getWorkOrder().getCurrentBlockMetadata();
                ItemStack   itemstack = new ItemStack(block, 1);

                if (itemstack.getItem() != null
                    && !BlockUtils.shouldNeverBeMessedWith(block)
                    && !BlockUtils.freeToPlace(block, metadata))
                {
                    if (checkOrRequestItems(new ItemStack(block)))
                    {
                        job.getWorkOrder().resetSchematic();
                        return this.getState();
                    }
                }
            }
            job.getWorkOrder().resetSchematic();
        }
        return AIState.BUILDER_STRUCTURE_STEP;
    }

    /**
     * A factory method to generate different build passes
     * <p>
     * the builder has to go over all solid blocks and over all nonsolid blocks
     *
     * @param finalCallback the final statefunction to execute
     * @return the new AIState
     */
    private Supplier<AIState> stepProducer(Supplier<AIState> finalCallback, boolean shouldBeSolid)
    {
        return () -> {
            AIState intermediate = checkAndSetBlock(shouldBeSolid);
            if (intermediate != null)
            {
                return intermediate;
            }
            return finalCallback.get();
        };
    }

    /**
     * Check if we should and can set the current schematic block.
     * <p>
     * If we cannot, we return the new state
     * <p>
     * If we successfully placed the new block, return null
     *
     * @param shouldBeSolid if the block we place should be solid
     * @return null if placed ok
     */
    private AIState checkAndSetBlock(boolean shouldBeSolid)
    {
        WorkOrderBuild workOrder = job.getWorkOrder();
        if (workOrder.doesSchematicBlockEqualWorldBlock()
            || shouldBeSolid != workOrder.getCurrentBlock().getMaterial().isSolid())
        {
            //we can ignore this block, to the next
            return findNextBlockSolid();
        }

        if (walkToConstructionSite())
        {
            return this.getState();
        }

        BlockPos coordinates = workOrder.getCurrentBlockPosition();
        worker.faceBlock(coordinates);

        Block worldBlock = world.getBlockState(coordinates).getBlock();

        //don't overwrite huts or bedrock, nor place huts
        if (BlockUtils.shouldNeverBeMessedWith(worldBlock))
        {
            findNextBlockSolid();
            return this.getState();
        }

        Block       block    = workOrder.getCurrentBlock();
        IBlockState metadata = workOrder.getCurrentBlockMetadata();
        //We need to deal with materials if(!Configurations.builderInfiniteResources)
        if (!Configurations.builderInfiniteResources
            && !hasMaterialsToPlace(block, metadata))
        {
            return this.getState();
        }

        int  x    = coordinates.getX();
        int  y    = coordinates.getY();
        int  z    = coordinates.getZ();
        Item item = Item.getItemFromBlock(block);
        //set visual effect held item
        worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);
        //try to place item
        if (placeBlock(new BlockPos(x, y, z), block, metadata))
        {
            setTileEntity();
        }
        worker.swingItem();
        return null;
    }

    /**
     * Spawn an entity in the world
     * <p>
     * Entities are loaded from the schematic
     *
     * @param entity the entity to spawn
     */
    private void spawnEntity(Entity entity)
    {
        if (entity != null)
        {
            BlockPos pos = job.getWorkOrder().getOffsetPosition();

            if (entity instanceof EntityHanging)
            {
                EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.posX += pos.getX();
                entityHanging.posY += pos.getY();
                entityHanging.posZ += pos.getZ();
                //also sets position based on tile
                entityHanging.setPosition(entityHanging.getHangingPosition().getX(),
                                          entityHanging.getHangingPosition().getY(),
                                          entityHanging.getHangingPosition().getZ());

                entityHanging.setWorld(world);
                entityHanging.dimension = world.provider.getDimensionId();

                world.spawnEntityInWorld(entityHanging);
            }
            else if (entity instanceof EntityMinecart)
            {
                EntityMinecart minecart = (EntityMinecart) entity;
                minecart.riddenByEntity = null;
                minecart.posX += pos.getX();
                minecart.posY += pos.getY();
                minecart.posZ += pos.getZ();

                minecart.setWorld(world);
                minecart.dimension = world.provider.getDimensionId();

                world.spawnEntityInWorld(minecart);
            }
        }
    }

    /**
     * Check if the worker has materials to place this block.
     * <p>
     * Will request the needed materials.
     *
     * @param block    the block to place
     * @param metadata the metadata attached
     * @return true once all conditions are satisfied
     */
    private boolean hasMaterialsToPlace(Block block, IBlockState metadata)
    {
        if (BlockUtils.freeToPlace(block, metadata))
        {
            return true;
        }
        ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(metadata));
        return !checkOrRequestItems(stack);
    }

    /**
     * Place a block in the world
     *
     * @param pos      where to place the block
     * @param block    the block to place
     * @param metadata the blocks metadata
     * @return true if it worked
     */
    private boolean placeBlock(BlockPos pos, Block block, IBlockState metadata)
    {
        //Move out of the way when placing blocks
        if (MathHelper.floor_double(worker.posX) == pos.getX()
            && MathHelper.abs_int(pos.getY() - (int) worker.posY) <= 1
            && MathHelper.floor_double(worker.posZ) == pos.getZ()
            && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, 4.1, 1.0);
        }

        //Workaround as long as we didn't rescan all of our buildings since BlockStairs now have different metadata values.
        if (metadata.getBlock() instanceof BlockStairs
            && world.getBlockState(pos).getBlock() instanceof BlockStairs
            && Objects.equals(world.getBlockState(pos).getValue(BlockStairs.FACING),
                              metadata.getValue(BlockStairs.FACING))
            && Objects.equals(metadata.getBlock(),
                              world.getBlockState(pos).getBlock()))
        {
            return true;
        }

        if (!Objects.equals(world.getBlockState(pos).getBlock(),
                            Blocks.air))
        {
            List<ItemStack> items = BlockPosUtil.getBlockDrops(world, pos, 0);
            for (ItemStack item : items)
            {
                InventoryUtils.setStack(worker.getInventoryCitizen(), item);
            }
        }

        if (block instanceof BlockDoor
            && Objects.equals(metadata.getValue(BlockDoor.HALF),
                              BlockDoor.EnumDoorHalf.LOWER))
        {
            ItemDoor.placeDoor(world, pos, metadata.getValue(BlockDoor.FACING), block);
        }
        else if (block instanceof BlockBed)
        {
            world.setBlockState(pos, metadata, BLOCK_PLACE_FLAGS);
            EnumFacing meta = metadata.getValue(BlockBed.FACING);
            world.setBlockState(pos.offset(meta), metadata, BLOCK_PLACE_FLAGS);
        }
        else if (block instanceof BlockDoublePlant)
        {
            world.setBlockState(pos, metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), BLOCK_PLACE_FLAGS);
            world.setBlockState(pos.up(), metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), BLOCK_PLACE_FLAGS);
        }
        else
        {
            if (!world.setBlockState(pos, metadata, BLOCK_PLACE_FLAGS))
            {
                return false;
            }
            if (world.getBlockState(pos).getBlock() == block)
            {
                if (world.getBlockState(pos) != metadata)
                {
                    world.setBlockState(pos, metadata, BLOCK_PLACE_FLAGS);
                }
            }
        }

        ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(metadata));

        int slot = worker.findFirstSlotInInventoryWith(stack.getItem());
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
        }
        return true;
    }

    /**
     * Tries to set the current tile entity
     */
    private void setTileEntity()
    {
        TileEntity tileEntity = job.getWorkOrder().getCurrentTileEntity();
        BlockPos   pos        = job.getWorkOrder().getCurrentBlockPosition();
        if (tileEntity != null && world.getTileEntity(pos) != null)
        {
            world.setTileEntity(pos, tileEntity);
        }
    }

    /**
     * advance to the next solid block of to the decoration stage
     *
     * @return the new state
     */
    private AIState findNextBlockSolid()
    {
        //method returns false if there is no next block (schematic finished)
        if (!job.getWorkOrder().findNextBlockSolid())
        {
            job.getWorkOrder().resetSchematic();
            return AIState.BUILDER_DECORATION_STEP;
        }
        return this.getState();
    }

    /**
     * advance to the next non solid block of to the complete stage
     *
     * @return the new state
     */
    private AIState findNextBlockNonSolid()
    {
        //method returns false if there is no next block (schematic finished)
        if (!job.getWorkOrder().findNextBlockNonSolid())
        {
            job.getWorkOrder().resetSchematic();
            return AIState.BUILDER_COMPLETE_BUILD;
        }
        return this.getState();
    }

    /**
     * finalize the current build
     * and add entities (like torches)
     *
     * @return IDLE to wait for new workorder
     */
    private AIState completeBuild()
    {
        job.getWorkOrder().getEntities().forEach(this::spawnEntity);

        worker.sendLocalizedChat(ENTITY_BUILDER_MESSAGE_BUILD_COMPLETE, job.getWorkOrder().getSchematicName());

        WorkOrderBuild wo = job.getWorkOrder();
        if (wo != null)
        {
            Building building = job.getColony().getBuilding(wo.getBuildingId());
            if (building != null)
            {
                building.setBuildingLevel(wo.getUpgradeLevel());
            }
        }

        job.complete();
        worker.addExperience(XP_EACH_BUILDING);

        return AIState.IDLE;
    }
}
