package com.minecolonies.entity.ai.citizen.builder;

import com.minecolonies.blocks.AbstractBlockHut;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.colony.buildings.AbstractBuilding;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.colony.workorders.WorkOrderBuildDecoration;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.ai.basic.AbstractEntityAIStructure;
import com.minecolonies.entity.ai.util.AIState;
import com.minecolonies.entity.ai.util.AITarget;
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
import org.jetbrains.annotations.Nullable;
import java.util.List;
import static com.minecolonies.entity.ai.util.AIState.*;

/**
 * AI class for the builder.
 * Manages building and repairing buildings.
 */
public class EntityAIStructureBuilder extends AbstractEntityAIStructure<JobBuilder>
{
    /**
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally)
     */
    private static final double   XP_EACH_BUILDING              = 2.5;
    /**
     * How often should intelligence factor into the builders skill modifier.
     */
    private static final int      INTELLIGENCE_MULTIPLIER       = 2;
    /**
     * How often should strength factor into the builders skill modifier.
     */
    private static final int      STRENGTH_MULTIPLIER           = 1;
    /**
     * The maximum range to keep from the current building place
     */
    private static final int      MAX_ADDITIONAL_RANGE_TO_BUILD = 25;
    /**
     * The standard range the builder should reach until his target.
     */
    private static final int      STANDARD_WORKING_RANGE        = 5;
    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int      MIN_WORKING_RANGE             = 7;
    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int      ACTIONS_UNTIL_DUMP            = 1024;
    /**
     * Position where the Builders constructs from.
     */
    private              BlockPos workFrom                      = null;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(JobBuilder job)
    {
        super(job);
        super.registerTargets(
                new AITarget(this::checkIfCanceled, IDLE),
                new AITarget(this::checkIfExecute, this::getState),
                new AITarget(IDLE, START_WORKING),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(BUILDER_CLEAR_STEP, this::clearStep),
                new AITarget(BUILDER_REQUEST_MATERIALS, this::requestMaterials),
                new AITarget(BUILDER_STRUCTURE_STEP, this::structureStep),
                new AITarget(BUILDER_DECORATION_STEP, this::decorationStep),
                new AITarget(BUILDER_COMPLETE_BUILD, this::completeBuild)
                             );
        worker.setSkillModifier(INTELLIGENCE_MULTIPLIER * worker.getCitizenData().getIntelligence()
                                + STRENGTH_MULTIPLIER * worker.getCitizenData().getStrength());
        worker.setCanPickUpLoot(true);
    }

    private boolean checkIfCanceled()
    {
        final WorkOrderBuild wo = job.getWorkOrder();

        if(wo == null)
        {
            cancelTask();
            return true;
        }

        return false;
    }

    private boolean checkIfExecute()
    {
        setDelay(1);

        if (!job.hasWorkOrder())
        {
            return true;
        }

        WorkOrderBuild wo = job.getWorkOrder();

        if (job.getColony().getBuilding(wo.getBuildingLocation()) == null && !(wo instanceof WorkOrderBuildDecoration))
        {
            job.complete();
            return true;
        }

        if (!job.hasSchematic())
        {
            initiate();
        }

        return false;
    }

    //todo why does this return AIState if it isn't used.
    private AIState initiate()
    {
        if (!job.hasSchematic())
        {
            workFrom = null;
            loadSchematic();

            WorkOrderBuild wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.logger.error(
                        String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)",
                                      worker.getColony().getID(),
                                      worker.getCitizenData().getId(), job.getWorkOrderId()));
                return this.getState();
            }

            if (wo instanceof WorkOrderBuildDecoration)
            {
                LanguageHandler.sendPlayersLocalizedMessage(ServerUtils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()),
                                                            "entity.builder.messageBuildStart",
                                                            job.getSchematic().getName());

                if (!job.hasSchematic() || !job.getSchematic().decrementBlock())
                {
                    return this.getState();
                }
                return AIState.START_WORKING;
            }
            else
            {
                AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.logger.error(
                            String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                                          worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return this.getState();
                }

                LanguageHandler.sendPlayersLocalizedMessage(ServerUtils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()),
                                                            "entity.builder.messageBuildStart",
                                                            job.getSchematic().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                    if (!job.hasSchematic() || !incrementBlock())
                    {
                        return this.getState();
                    }
                    return AIState.BUILDER_REQUEST_MATERIALS;
                }
                else
                {
                    if (!job.hasSchematic() || !job.getSchematic().decrementBlock())
                    {
                        return this.getState();
                    }
                    return AIState.START_WORKING;
                }
            }
        }

        BlockPosUtil.tryMoveLivingToXYZ(worker, job.getSchematic().getPosition());

        return AIState.IDLE;
    }

    private boolean incrementBlock()
    {
        //method returns false if there is no next block (schematic finished)
        return job.getSchematic().incrementBlock();
    }

    private void loadSchematic()
    {
        WorkOrderBuild workOrder = job.getWorkOrder();
        if (workOrder == null)
        {
            return;
        }

        BlockPos pos = workOrder.getBuildingLocation();

        if (!(workOrder instanceof WorkOrderBuildDecoration) && worker.getColony().getBuilding(pos) == null)
        {
            Log.logger.warn("AbstractBuilding does not exist - removing build request");
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        try
        {
            job.setSchematic(new SchematicWrapper(world, workOrder.getSchematicName()));
        }
        catch (IllegalStateException e)
        {
            Log.logger.warn(String.format("Schematic: (%s) does not exist - removing build request", workOrder.getSchematicName()), e);
            job.setSchematic(null);
            return;
        }

        job.getSchematic().rotate(workOrder.getRotation());
        job.getSchematic().setPosition(pos);
        workOrder.setCleared(false);
    }

    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return getState();
        }
        return BUILDER_CLEAR_STEP;
    }

    /**
     * Will lead the worker to a good position to construct.
     *
     * @return true if the position has been reached.
     */
    private boolean goToConstructionSite()
    {
        if (workFrom == null)
        {
            workFrom = getWorkingPosition();
        }

        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < MIN_WORKING_RANGE;
    }

    /**
     * Calculates the working position. Takes a min distance from width and length.
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition()
    {
        return getWorkingPosition(0);
    }

    /**
     * Calculates the working position. Takes a min distance from width and length.
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param offset the extra distance to apply away from the building
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition(int offset)
    {
        if (offset > MAX_ADDITIONAL_RANGE_TO_BUILD)
        {
            return job.getSchematic().getBlockPosition();
        }
        //get length or width either is larger.
        int          length     = job.getSchematic().getLength();
        int          width      = job.getSchematic().getWidth();
        int          distance   = width > length ? width : length;
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
        return getWorkingPosition(offset + 1);
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
        return getFloor(job.getSchematic().getPosition().offset(facing, distance));
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
        WorkOrderBuild wo = job.getWorkOrder();

        if (wo.isCleared())
        {
            return AIState.BUILDER_STRUCTURE_STEP;
        }

        BlockPos coordinates = job.getSchematic().getBlockPosition();
        Block    worldBlock  = world.getBlockState(coordinates).getBlock();

        if (worldBlock != Blocks.air
             && !(worldBlock instanceof AbstractBlockHut)
             && worldBlock != Blocks.bedrock
             && job.getSchematic().getBlock() != ModBlocks.blockSubstitution)
        {
            //Fill workFrom with the position from where the builder should build.
            if (!goToConstructionSite())
            {
                return this.getState();
            }

            worker.faceBlock(coordinates);
            //We need to deal with materials
            if (Configurations.builderInfiniteResources || worldBlock.getMaterial().isLiquid())
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

        //method returns false if there is no next block (schematic finished)
        if (!job.getSchematic().findNextBlockToClear())
        {
            job.getSchematic().reset();
            incrementBlock();
            wo.setCleared(true);
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
            while (job.getSchematic().findNextBlock())
            {
                if (job.getSchematic().doesSchematicBlockEqualWorldBlock())
                {
                    continue;
                }

                Block     block     = job.getSchematic().getBlock();
                ItemStack itemstack = new ItemStack(block, 1);

                Block worldBlock = BlockPosUtil.getBlock(world, job.getSchematic().getBlockPosition());

                if (itemstack.getItem() != null
                    && block != null
                    && block != Blocks.air
                    && worldBlock != Blocks.bedrock
                    && !(worldBlock instanceof AbstractBlockHut)
                    && !isBlockFree(block, 0)
                    && checkOrRequestItems(new ItemStack(block)))
                {
                    job.getSchematic().reset();
                    return this.getState();
                }
            }
            job.getSchematic().reset();
            incrementBlock();
        }
        return AIState.BUILDER_STRUCTURE_STEP;
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
               || (block.equals(Blocks.double_plant) && Utils.testFlag(metadata, 0x08))
               || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08))
               || block.equals(Blocks.grass)
               || block.equals(Blocks.dirt);
    }

    private AIState structureStep()
    {
        if (!goToConstructionSite())
        {
            return this.getState();
        }

        if (job.getSchematic().getBlock() == null
            || job.getSchematic().doesSchematicBlockEqualWorldBlock()
            || (!job.getSchematic().getBlock().getMaterial().isSolid()
                && job.getSchematic().getBlock() != Blocks.air))
        {
            //findNextBlock count was reached and we can ignore this block
            return findNextBlockSolid();
        }

        worker.faceBlock(job.getSchematic().getBlockPosition());
        Block       block    = job.getSchematic().getBlock();
        IBlockState metadata = job.getSchematic().getBlockState();

        BlockPos coordinates = job.getSchematic().getBlockPosition();
        int      x           = coordinates.getX();
        int      y           = coordinates.getY();
        int      z           = coordinates.getZ();

        Block worldBlock = world.getBlockState(coordinates).getBlock();

        //should never happen
        if (block == null)
        {
            BlockPos local = job.getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockSolid();
            return this.getState();
        }

        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut
            || worldBlock == Blocks.bedrock
            || block instanceof AbstractBlockHut)
        {
            return findNextBlockSolid();
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources
            && !handleMaterials(block, metadata))
        {
            return this.getState();
        }

        if (block == Blocks.air)
        {
            worker.setCurrentItemOrArmor(0, null);

            if (!world.setBlockToAir(coordinates))
            {
                Log.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                //TODO handle - for now, just skipping
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
                //TODO handle - for now, just skipping
            }
            worker.swingItem();
        }

        return findNextBlockSolid();
    }

    private AIState decorationStep()
    {
        if (!goToConstructionSite())
        {
            return this.getState();
        }
        //|| job.getSchematic().getBlock() == Blocks.air
        if (job.getSchematic().doesSchematicBlockEqualWorldBlock()
            || job.getSchematic().getBlock().getMaterial().isSolid())
        {
            //findNextBlock count was reached and we can ignore this block
            return findNextBlockNonSolid();
        }

        worker.faceBlock(job.getSchematic().getBlockPosition());
        Block       block    = job.getSchematic().getBlock();
        IBlockState metadata = job.getSchematic().getBlockState();

        BlockPos coords = job.getSchematic().getBlockPosition();
        int      x      = coords.getX();
        int      y      = coords.getY();
        int      z      = coords.getZ();

        Block       worldBlock         = world.getBlockState(coords).getBlock();
        IBlockState worldBlockMetadata = world.getBlockState(coords);
        //should never happen
        if (block == null)
        {
            BlockPos local = job.getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockNonSolid();
            return this.getState();
        }
        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut
            || worldBlock == Blocks.bedrock
            || block instanceof AbstractBlockHut)
        {
            findNextBlockNonSolid();
            return this.getState();
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && !handleMaterials(block, metadata))
        {
            return this.getState();
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

    //TODO handle resources
    private void spawnEntity(Entity entity)
    {
        if (entity != null)
        {
            BlockPos pos = job.getSchematic().getOffsetPosition();

            if (entity instanceof EntityHanging)
            {
                EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.posX += pos.getX();
                entityHanging.posY += pos.getY();
                entityHanging.posZ += pos.getZ();
                //also sets position based on tile
                entityHanging.setPosition(
                        entityHanging.getHangingPosition().getX(),
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

    private boolean handleMaterials(Block block, IBlockState metadata)
    {
        //Breaking blocks doesn't require taking materials from the citizens inventory
        if (block != Blocks.air)
        {
            if (isBlockFree(block, block.getMetaFromState(metadata)))
            {
                return true;
            }

            ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(metadata));

            if (stack.getItem() == null)
            {
                stack = new ItemStack(block);
            }

            if (stack.getItem() == null)
            {
                stack = new ItemStack(BlockUtils.getItemFromBlock(block));
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
            && world.getBlockState(pos).getValue(BlockStairs.FACING) == metadata.getValue(BlockStairs.FACING)
            && metadata.getBlock() == world.getBlockState(pos).getBlock())
        {
            return true;
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && world.getBlockState(pos).getBlock() != Blocks.air)
        {
            List<ItemStack> items = BlockPosUtil.getBlockDrops(world, pos, 0);
            for (ItemStack item : items)
            {
                InventoryUtils.setStack(worker.getInventoryCitizen(), item);
            }
        }

        if (block instanceof BlockDoor && metadata.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
        {
            ItemDoor.placeDoor(world, pos, metadata.getValue(BlockDoor.FACING), block);
        }
        else if (block instanceof BlockBed)
        {
            world.setBlockState(pos, metadata, 0x03);
            EnumFacing facing = metadata.getValue(BlockBed.FACING);

            //Set other part of the bed, to the opposite PartType
            if (metadata.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
            {
                //pos.offset(facing) will get the other part of the bed
                world.setBlockState(pos.offset(facing), metadata.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD), 0x03);
            }
            else
            {
                world.setBlockState(pos.offset(facing), metadata.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT), 0x03);
            }
        }
        else if (block instanceof BlockDoublePlant)
        {
            world.setBlockState(pos, metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 0x03);
            world.setBlockState(pos.up(), metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 0x03);
        }
        else if (block instanceof BlockEndPortal || block instanceof BlockMobSpawner || block instanceof BlockDragonEgg || block instanceof BlockPortal)
        {
            return true;
        }
        else
        {
            if (!world.setBlockState(pos, metadata, 0x03))
            {
                return false;
            }
            if (world.getBlockState(pos).getBlock() == block && world.getBlockState(pos) != metadata)
            {
                world.setBlockState(pos, metadata, 0x03);
            }
        }

        ItemStack stack = new ItemStack(Item.getItemFromBlock(block), 1, block.damageDropped(metadata));

        if (stack.getItem() == null)
        {
            stack = new ItemStack(block);
        }

        if (stack.getItem() == null)
        {
            stack = new ItemStack(BlockUtils.getItemFromBlock(block));
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
        TileEntity tileEntity = job.getSchematic().getTileEntity();
        if (tileEntity != null && world.getTileEntity(pos) != null)
        {
            world.setTileEntity(pos, tileEntity);
        }
    }

    private AIState findNextBlockSolid()
    {
        //method returns false if there is no next block (schematic finished)
        if (!job.getSchematic().findNextBlockSolid())
        {
            job.getSchematic().reset();
            incrementBlock();
            return AIState.BUILDER_DECORATION_STEP;
        }
        return this.getState();
    }

    private AIState findNextBlockNonSolid()
    {
        //method returns false if there is no next block (schematic finished)
        if (!job.getSchematic().findNextBlockNonSolid())
        {
            job.getSchematic().reset();
            incrementBlock();
            return AIState.BUILDER_COMPLETE_BUILD;
        }
        return this.getState();
    }

    private AIState completeBuild()
    {
        job.getSchematic().getEntities().forEach(this::spawnEntity);

        String schematicName = job.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(ServerUtils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()),
                                                    "entity.builder.messageBuildComplete",
                                                    schematicName);

        WorkOrderBuild wo = job.getWorkOrder();
        if (wo != null)
        {
            if (!(wo instanceof WorkOrderBuildDecoration))
            {
                AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building != null)
                {
                    building.setBuildingLevel(wo.getUpgradeLevel());
                }
                else
                {
                    Log.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                                                   worker.getColony().getID(),
                                                   worker.getCitizenData().getId(),
                                                   wo.getBuildingLocation()));
                }
            }
            job.complete();
        }
        else
        {
            Log.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing work order(%d)",
                                           worker.getColony().getID(),
                                           worker.getCitizenData().getId(),
                                           job.getWorkOrderId()));
        }

        resetTask();
        worker.addExperience(XP_EACH_BUILDING);
        workFrom = null;

        return AIState.IDLE;
    }

    /**
     * Resets the builders current task.
     */
    public void cancelTask()
    {
        job.setWorkOrder(null);
        workFrom = null;
        job.setSchematic(null);
    }

    /**
     * Calculates after how many actions the ai should dump it's inventory.
     *
     * @return the number of actions done before item dump.
     */
    @Override
    protected int getActionsDoneUntilDumping()
    {
        return ACTIONS_UNTIL_DUMP;
    }

    /**
     * Can be overriden by implementations to specify which tools are useful for the worker.
     * When dumping he will keep these.
     *
     * @param stack the stack to decide on
     * @return if should be kept or not.
     */
    @Override
    protected boolean neededForWorker(@Nullable final ItemStack stack)
    {
        return Utils.isMiningTool(stack);
    }
}
