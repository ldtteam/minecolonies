package com.minecolonies.entity.ai;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.util.*;
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
     * How often should intelligence factor into the builders skill modifier.
     */
    private static final int    INTELLIGENCE_MULTIPLIER               = 2;
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
                new AITarget(BUILDER_STRUCTURE_STEP, this::structureStep),
                new AITarget(BUILDER_DECORATION_STEP, this::decorationStep),
                new AITarget(BUILDER_COMPLETE_BUILD, this::completeBuild)
                             );
        worker.setSkillModifier(INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
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
        int          length     = job.getWorkOrder().getSchematic().getLength();
        int          width      = job.getWorkOrder().getSchematic().getWidth();
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
        return job.getWorkOrder().getSchematic().getPosition();
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
        return getFloor(job.getWorkOrder().getSchematic().getPosition().offset(facing, distance));
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

        BlockPos coordinates = job.getWorkOrder().getSchematic().getBlockPosition();
        Block    worldBlock  = world.getBlockState(coordinates).getBlock();

        if (!Objects.equals(worldBlock, Blocks.air)
            && !(worldBlock instanceof AbstractBlockHut)
            && !Objects.equals(worldBlock, Blocks.bedrock))
        {
            //Fill workFrom with the position from where the builder should build.
            if (walkToConstructionSite())
            {
                return this.getState();
            }

            worker.faceBlock(coordinates);
            //We need to deal with materials
            if (Configurations.builderInfiniteResources
                || worldBlock.getMaterial().isLiquid())
            {
                worker.setCurrentItemOrArmor(0, null);

                if (!world.setBlockToAir(coordinates))
                {
                    //TODO: create own logger in class
                    Log.logger.error(String.format("Block break failure at %d, %d, %d", coordinates.getX(), coordinates.getY(), coordinates.getZ()));
                    //TODO handle - for now, just skipping
                }
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

        if (!job.getWorkOrder().getSchematic().findNextBlockToClear())//method returns false if there is no next block (schematic finished)
        {
            job.getWorkOrder().getSchematic().reset();
            incrementBlock();
            job.getWorkOrder().setCleared();
            return AIState.BUILDER_REQUEST_MATERIALS;
        }
        return this.getState();
    }

    private AIState requestMaterials()
    {
        //todo as soon as material handling has been implemented this should be set to work!
        //We need to deal with materials
        if (!Configurations.builderInfiniteResources)
        {
            //TODO thread this
            while (job.getWorkOrder().getSchematic().findNextBlock())
            {
                if (job.getWorkOrder().getSchematic().doesSchematicBlockEqualWorldBlock())
                {
                    continue;
                }

                Block       block     = job.getWorkOrder().getSchematic().getBlock();
                IBlockState metadata  = job.getWorkOrder().getSchematic().getMetadata();
                ItemStack   itemstack = new ItemStack(block, 1);

                Block worldBlock = BlockPosUtil.getBlock(world, job.getWorkOrder().getSchematic().getBlockPosition());

                if (itemstack.getItem() != null
                    && block != null
                    && !block.equals(Blocks.air)
                    && !Objects.equals(worldBlock, Blocks.bedrock)
                    && !(worldBlock instanceof AbstractBlockHut)
                    && !isBlockFree(block, 0))
                {
                    if (checkOrRequestItems(new ItemStack(block)))
                    {
                        job.getWorkOrder().getSchematic().reset();
                        return this.getState();
                    }
                }
            }
            job.getWorkOrder().getSchematic().reset();
            incrementBlock();
        }
        return AIState.BUILDER_STRUCTURE_STEP;
    }

    /**
     * Increments the current working block in the schematic world.
     *
     * @return false if the schematic is done
     */
    private boolean incrementBlock()
    {
        return job.getWorkOrder().getSchematic().incrementBlock();
    }

    /**
     * Defines blocks that can be built for free
     *
     * @param block    The block to check if it is free
     * @param metadata The metadata of the block
     * @return true or false
     */
    private boolean isBlockFree(Block block, int metadata)
    {
        return block == null
               || BlockUtils.isWater(block.getDefaultState())
               || block.equals(Blocks.leaves)
               || block.equals(Blocks.leaves2)
               || (block.equals(Blocks.double_plant)
                   && Utils.testFlag(metadata, 0x08))
               || (block instanceof BlockDoor
                   && Utils.testFlag(metadata, 0x08))
               || block.equals(Blocks.grass)
               || block.equals(Blocks.dirt);
    }

    private AIState structureStep()
    {
        if (job.getWorkOrder().getSchematic().getBlock() == null
            || job.getWorkOrder().getSchematic().doesSchematicBlockEqualWorldBlock()
            || (!job.getWorkOrder().getSchematic().getBlock().getMaterial().isSolid() && job.getWorkOrder().getSchematic().getBlock() != Blocks.air))
        {
            //findNextBlock count was reached and we can ignore this block
            return findNextBlockSolid();
        }

        if (walkToConstructionSite())
        {
            return this.getState();
        }

        worker.faceBlock(job.getWorkOrder().getSchematic().getBlockPosition());
        Block       block    = job.getWorkOrder().getSchematic().getBlock();
        IBlockState metadata = job.getWorkOrder().getSchematic().getMetadata();

        BlockPos coordinates = job.getWorkOrder().getSchematic().getBlockPosition();
        int      x           = coordinates.getX();
        int      y           = coordinates.getY();
        int      z           = coordinates.getZ();

        //should never happen
        if (block == null)
        {
            BlockPos local = job.getWorkOrder().getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockSolid();
            return this.getState();
        }

        Block worldBlock = world.getBlockState(coordinates).getBlock();

        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut
            || worldBlock == Blocks.bedrock
            || block instanceof AbstractBlockHut)
        {
            findNextBlockSolid();
            return this.getState();
        }

        //We need to deal with materials if(!Configurations.builderInfiniteResources)
        if (!Configurations.builderInfiniteResources
            && !handleMaterials(block, metadata))
        {
            return this.getState();
        }

        if (Objects.equals(block, Blocks.air))
        {
            worker.setCurrentItemOrArmor(0, null);

            if (!world.setBlockToAir(coordinates))
            {
                Log.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                //todo: handle - for now, just skipping
            }
        }
        else
        {
            Item item = Item.getItemFromBlock(block);
            worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);

            if (placeBlock(new BlockPos(x, y, z), block, metadata))
            {
                setTileEntity(new BlockPos(x, y, z));
            }
            else
            {
                Log.logger.error(String.format("Block place failure %s at %d, %d, %d", block.getUnlocalizedName(), x, y, z));
                //todo: handle - for now, just skipping
            }
            worker.swingItem();
        }

        return findNextBlockSolid();
    }

    private AIState decorationStep()
    {
        if (job.getWorkOrder().getSchematic().doesSchematicBlockEqualWorldBlock()
            || job.getWorkOrder().getSchematic().getBlock().getMaterial().isSolid()
            || Objects.equals(job.getWorkOrder().getSchematic().getBlock(),
                              Blocks.air))
        {
            //findNextBlock count was reached and we can ignore this block
            return findNextBlockNonSolid();
        }

        if (walkToConstructionSite())
        {
            return this.getState();
        }

        worker.faceBlock(job.getWorkOrder().getSchematic().getBlockPosition());
        Block       block    = job.getWorkOrder().getSchematic().getBlock();
        IBlockState metadata = job.getWorkOrder().getSchematic().getMetadata();

        BlockPos coords = job.getWorkOrder().getSchematic().getBlockPosition();
        int      x      = coords.getX();
        int      y      = coords.getY();
        int      z      = coords.getZ();

        Block       worldBlock         = world.getBlockState(coords).getBlock();
        IBlockState worldBlockMetadata = world.getBlockState(coords);

        if (block == null)//should never happen
        {
            BlockPos local = job.getWorkOrder().getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockNonSolid();
            return this.getState();
        }

        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut
            || Objects.equals(worldBlock, Blocks.bedrock)
            || block instanceof AbstractBlockHut)
        {
            findNextBlockNonSolid();
            return this.getState();
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources)
        {
            if (!handleMaterials(block, metadata))
            {
                return this.getState();
            }
        }

        Item item = Item.getItemFromBlock(block);
        worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);

        if (placeBlock(new BlockPos(x, y, z), block, metadata))
        {
            setTileEntity(new BlockPos(x, y, z));
        }
        else
        {
            Log.logger.error(String.format("Block place failure %s at %d, %d, %d", block.getUnlocalizedName(), x, y, z));
            //TODO handle - for now, just skipping
        }
        worker.swingItem();

        return findNextBlockNonSolid();
    }

    private void spawnEntity(Entity entity)//TODO handle resources
    {
        if (entity != null)
        {
            BlockPos pos = job.getWorkOrder().getSchematic().getOffsetPosition();

            if (entity instanceof EntityHanging)
            {
                EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.posX += pos.getX();//tileX
                entityHanging.posY += pos.getY();//tileY
                entityHanging.posZ += pos.getZ();//tileZ
                entityHanging.setPosition(entityHanging.getHangingPosition().getX(),
                                          entityHanging.getHangingPosition().getY(),
                                          entityHanging.getHangingPosition().getZ());//also sets position based on tile

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

    private boolean handleMaterials(Block block, IBlockState metadata)
    {
        if (block != Blocks.air)//Breaking blocks doesn't require taking materials from the citizens inventory
        {
            if (isBlockFree(block, block.getMetaFromState(metadata)))
            {
                return true;
            }

            ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(metadata));

            if (stack.getItem() == null)
            {
                stack = new ItemStack(block.getItem(job.getWorkOrder().getSchematic().getWorldForRender(), job.getWorkOrder().getSchematic().getPosition()));
            }

            if (checkOrRequestItems(stack))
            {
                return false;
            }
        }

        return true;
    }

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
            world.setBlockState(pos, metadata, 0x03);
            EnumFacing meta = metadata.getValue(BlockBed.FACING);
            world.setBlockState(pos.offset(meta), metadata, 0x03);
        }
        else if (block instanceof BlockDoublePlant)
        {
            world.setBlockState(pos, metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 0x03);
            world.setBlockState(pos.up(), metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 0x03);
        }
        else
        {
            if (!world.setBlockState(pos, metadata, 0x03))
            {
                return false;
            }
            if (world.getBlockState(pos).getBlock() == block)
            {
                if (world.getBlockState(pos) != metadata)
                {
                    world.setBlockState(pos, metadata, 0x03);
                }
                //todo do we need this? block.onPostBlockPlaced(world, x, y, z, metadata);
            }
        }

        ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(metadata));

        if (stack.getItem() == null)
        {
            stack = new ItemStack(block.getItem(job.getWorkOrder().getSchematic().getWorldForRender(), job.getWorkOrder().getSchematic().getPosition()));
        }

        int slot = worker.findFirstSlotInInventoryWith(stack.getItem());
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
        }
        return true;
    }

    private void setTileEntity(BlockPos pos)
    {
        //TODO do we need to load TileEntities when building?
        TileEntity tileEntity = job.getWorkOrder().getSchematic().getTileEntity();
        if (tileEntity != null && world.getTileEntity(pos) != null)
        {
            world.setTileEntity(pos, tileEntity);
        }
    }

    private AIState findNextBlockSolid()
    {
        //method returns false if there is no next block (schematic finished)
        if (!job.getWorkOrder().getSchematic().findNextBlockSolid())
        {
            job.getWorkOrder().getSchematic().reset();
            incrementBlock();
            return AIState.BUILDER_DECORATION_STEP;
        }
        return this.getState();
    }

    private AIState findNextBlockNonSolid()
    {
        //method returns false if there is no next block (schematic finished)
        if (!job.getWorkOrder().getSchematic().findNextBlockNonSolid())
        {
            job.getWorkOrder().getSchematic().reset();
            incrementBlock();
            return AIState.BUILDER_COMPLETE_BUILD;
        }
        return this.getState();
    }

    private AIState completeBuild()
    {
        job.getWorkOrder().getSchematic().getEntities().forEach(this::spawnEntity);

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
