package com.minecolonies.entity.ai.citizen.builder;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.colony.Colony;
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
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
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
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally).
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
     * The maximum range to keep from the current building place.
     */
    private static final int      MAX_ADDITIONAL_RANGE_TO_BUILD = 25;
    /**
     * The standard range the builder should reach until his target.
     */
    private static final int      STANDARD_WORKING_RANGE = 5;
    /**
     * The minimum range the builder has to reach in order to construct or clear.
     */
    private static final int      MIN_WORKING_RANGE      = 7;
    /**
     * After how many actions should the builder dump his inventory.
     */
    private static final int      ACTIONS_UNTIL_DUMP     = 1024;

    /**
     * Speed the builder should run away when he castles himself in.
     */
    private static final double RUN_AWAY_SPEED           = 4.1D;
    /**
     * Position where the Builders constructs from.
     */
    @Nullable
    private              BlockPos workFrom               = null;

    /**
     * Initialize the builder and add all his tasks.
     *
     * @param job the job he has.
     */
    public EntityAIStructureBuilder(@NotNull final JobBuilder job)
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

        if (wo == null)
        {
            cancelTask();
            return true;
        }

        return false;
    }

    /**
     * Resets the builders current task.
     */
    public void cancelTask()
    {
        super.resetTask();
        job.setWorkOrder(null);
        workFrom = null;
        job.setStructure(null);
    }

    private boolean checkIfExecute()
    {
        setDelay(1);

        if (!job.hasWorkOrder())
        {
            return true;
        }

        final WorkOrderBuild wo = job.getWorkOrder();

        if (job.getColony().getBuilding(wo.getBuildingLocation()) == null && !(wo instanceof WorkOrderBuildDecoration))
        {
            job.complete();
            return true;
        }

        if (!job.hasStructure())
        {
            initiate();
        }

        return false;
    }

    //todo why does this return AIState if it isn't used.
    private AIState initiate()
    {
        if (!job.hasStructure())
        {
            workFrom = null;
            loadStructure();

            final WorkOrderBuild wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.getLogger().error(
                  String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)",
                    worker.getColony().getID(),
                    worker.getCitizenData().getId(), job.getWorkOrderId()));
                return this.getState();
            }

            if (wo instanceof WorkOrderBuildDecoration)
            {
                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                  "entity.builder.messageBuildStart",
                  job.getStructure().getName());

                if (!job.hasStructure() || !job.getStructure().decrementBlock())
                {
                    return this.getState();
                }
                return AIState.START_WORKING;
            }
            else
            {
                final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(
                      String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                        worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingLocation()));
                    return this.getState();
                }

                LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
                  "entity.builder.messageBuildStart",
                  job.getStructure().getName());

                //Don't go through the CLEAR stage for repairs and upgrades
                if (building.getBuildingLevel() > 0)
                {
                    wo.setCleared(true);
                    if (!job.hasStructure() || !incrementBlock())
                    {
                        return this.getState();
                    }
                    return AIState.BUILDER_REQUEST_MATERIALS;
                }
                else
                {
                    if (!job.hasStructure() || !job.getStructure().decrementBlock())
                    {
                        return this.getState();
                    }
                    return AIState.START_WORKING;
                }
            }
        }

        BlockPosUtil.tryMoveLivingToXYZ(worker, job.getStructure().getPosition());

        return AIState.IDLE;
    }

    private void loadStructure()
    {
        final WorkOrderBuild workOrder = job.getWorkOrder();
        if (workOrder == null)
        {
            return;
        }

        final BlockPos pos = workOrder.getBuildingLocation();

        if (!(workOrder instanceof WorkOrderBuildDecoration) && worker.getColony().getBuilding(pos) == null)
        {
            Log.getLogger().warn("AbstractBuilding does not exist - removing build request");
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        try
        {
            job.setStructure(new StructureWrapper(world, workOrder.getStructureName()));
        }
        catch (final RuntimeException e)
        {
            Log.getLogger().warn(String.format("StructureProxy: (%s) does not exist - removing build request", workOrder.getStructureName()), e);
            job.setStructure(null);
            return;
        }

        final Colony colony = worker.getColony();
        if(workOrder instanceof WorkOrderBuildDecoration)
        {
            job.getStructure().rotate(workOrder.getRotation());
        }
        else if(colony != null)
        {
            if(workOrder.getRotation() == 0)
            {
                final IBlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() instanceof AbstractBlockHut)
                {
                    job.getStructure().rotate(getRotationFromFacing(blockState.getValue(AbstractBlockHut.FACING)));
                }
            }
            else
            {
                job.getStructure().rotate(workOrder.getRotation());
            }
        }
        job.getStructure().setPosition(pos);
        workOrder.setCleared(false);
    }

    /**
     * Gets a rotation from a block facing.
     * @param facing the block facing.
     * @return the int rotation.
     */
    private static int getRotationFromFacing(final EnumFacing facing)
    {
        switch (facing)
        {
            case SOUTH:
                return 2;
            case EAST:
                return 1;
            case WEST:
                return 3;
            default:
                return 0;
        }
    }

    private boolean incrementBlock()
    {
        //method returns false if there is no next block (structures finished)
        return job.getStructure().incrementBlock();
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
            workFrom = getPositionToWorkFrom();
        }

        return worker.isWorkerAtSiteWithMove(workFrom, STANDARD_WORKING_RANGE) || MathUtils.twoDimDistance(worker.getPosition(), workFrom) < MIN_WORKING_RANGE;
    }

    /**
     * Calculates the working position. Takes a min distance from width and length.
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @return BlockPos position to work from.
     */
    private BlockPos getPositionToWorkFrom()
    {
        return getPositionToWorkFrom(0);
    }

    /**
     * Calculates the working position. Takes a min distance from width and length.
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     *
     * @param offset the extra distance to apply away from the building
     * @return BlockPos position to work from.
     */
    private BlockPos getPositionToWorkFrom(final int offset)
    {
        if (offset > MAX_ADDITIONAL_RANGE_TO_BUILD)
        {
            return job.getStructure().getBlockPosition();
        }
        //get length or width either is larger.
        final int length = job.getStructure().getLength();
        final int width = job.getStructure().getWidth();
        final int distance = width > length ? width : length;
        @NotNull final EnumFacing[] directions = {EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH};

        //then get a solid place with two air spaces above it in any direction.
        for (final EnumFacing direction : directions)
        {
            @NotNull final BlockPos positionInDirection = getPositionInDirection(direction, distance);
            if (EntityUtils.checkForFreeSpace(world, positionInDirection))
            {
                return positionInDirection;
            }
        }

        //if necessary we can could implement calling getPositionToWorkFrom recursively and add some "offset" to the sides.
        return getPositionToWorkFrom(offset + 1);
    }

    /**
     * Gets a floorPosition in a particular direction.
     *
     * @param facing   the direction.
     * @param distance the distance.
     * @return a BlockPos position.
     */
    @NotNull
    private BlockPos getPositionInDirection(final EnumFacing facing, final int distance)
    {
        return getFloor(job.getStructure().getPosition().offset(facing, distance));
    }

    /**
     * Calculates the floor level.
     *
     * @param position input position.
     * @return returns BlockPos position with air above.
     */
    @NotNull
    private BlockPos getFloor(@NotNull final BlockPos position)
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
        final WorkOrderBuild wo = job.getWorkOrder();

        if(job.getStructure() == null)
        {
            //fix for bad structures
            job.complete();
        }

        if (wo.isCleared())
        {
            return AIState.BUILDER_STRUCTURE_STEP;
        }

        final BlockPos coordinates = job.getStructure().getBlockPosition();
        final IBlockState worldBlockState = world.getBlockState(coordinates);
        final Block worldBlock = worldBlockState.getBlock();

        if (worldBlock != Blocks.AIR
              && !(worldBlock instanceof AbstractBlockHut)
              && worldBlock != Blocks.BEDROCK
              && job.getStructure().getBlock() != ModBlocks.blockSubstitution)
        {
            //Fill workFrom with the position from where the builder should build.
            if (!goToConstructionSite())
            {
                return this.getState();
            }

            worker.faceBlock(coordinates);
            //We need to deal with materials
            if (Configurations.builderInfiniteResources || worldBlockState.getMaterial().isLiquid())
            {
                worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);

                if (!world.setBlockToAir(coordinates))
                {
                    //TODO: create own logger in class
                    Log.getLogger().error(String.format("Block break failure at %d, %d, %d", coordinates.getX(), coordinates.getY(), coordinates.getZ()));
                    //TODO handle - for now, just skipping
                }
                worker.swingArm(worker.getActiveHand());
            }
            else
            {
                if (!mineBlock(coordinates))
                {
                    return this.getState();
                }
            }
        }

        //method returns false if there is no next block (structure finished)
        if (!job.getStructure().findNextBlockToClear())
        {
            job.getStructure().reset();
            incrementBlock();
            wo.setCleared(true);
            return AIState.BUILDER_REQUEST_MATERIALS;
        }
        return this.getState();
    }

    private AIState requestMaterials()
    {
        if(job.getStructure() == null)
        {
            //fix for bad structures
            job.complete();
        }

        //todo as soon as material handling has been implemented this should be set to work!
        //We need to deal with materials
        if (!Configurations.builderInfiniteResources)
        {
            //TODO thread this
            while (job.getStructure().findNextBlock())
            {
                if (job.getStructure().doesStructureBlockEqualWorldBlock())
                {
                    continue;
                }

                @Nullable final Block block = job.getStructure().getBlock();
                @NotNull final ItemStack itemstack = new ItemStack(block, 1);

                final Block worldBlock = BlockPosUtil.getBlock(world, job.getStructure().getBlockPosition());

                if (itemstack.getItem() != null
                      && block != null
                      && block != Blocks.AIR
                      && worldBlock != Blocks.BEDROCK
                      && !(worldBlock instanceof AbstractBlockHut)
                      && !isBlockFree(block, 0)
                      && checkOrRequestItems(new ItemStack(block)))
                {
                    job.getStructure().reset();
                    return this.getState();
                }
            }
            job.getStructure().reset();
            incrementBlock();
        }
        return AIState.BUILDER_STRUCTURE_STEP;
    }

    /**
     * Defines blocks that can be built for free.
     *
     * @param block    The block to check if it is free.
     * @param metadata The metadata of the block.
     * @return true or false.
     */
    private static boolean isBlockFree(@Nullable final Block block, final int metadata)
    {
        return block == null
                 || BlockUtils.isWater(block.getDefaultState())
                 || block.equals(Blocks.LEAVES)
                 || block.equals(Blocks.LEAVES2)
                 || (block.equals(Blocks.DOUBLE_PLANT) && Utils.testFlag(metadata, 0x08))
                 || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08))
                 || block.equals(Blocks.GRASS)
                 || block.equals(Blocks.DIRT);
    }

    private AIState structureStep()
    {
        if(job.getStructure() == null)
        {
            //fix for bad structures
            job.complete();
        }
        if (!goToConstructionSite())
        {
            return this.getState();
        }

        if (job.getStructure().getBlock() == null
              || job.getStructure().doesStructureBlockEqualWorldBlock()
              || (!job.getStructure().getBlockState().getMaterial().isSolid()
                    && job.getStructure().getBlock() != Blocks.AIR))
        {
            //findNextBlock count was reached and we can ignore this block
            return findNextBlockSolid();
        }

        worker.faceBlock(job.getStructure().getBlockPosition());



        final BlockPos coordinates = job.getStructure().getBlockPosition();
        @Nullable final Block block = job.getStructure().getBlock();
        @Nullable final IBlockState blockState = job.getStructure().getBlockState();

        //should never happen
        if (block == null)
        {
            @NotNull final BlockPos local = job.getStructure().getLocalPosition();
            Log.getLogger().error(String.format("StructureProxy has null block at %s - local(%s)", coordinates, local));
            findNextBlockSolid();
            return this.getState();
        }

        final Block worldBlock = world.getBlockState(coordinates).getBlock();
        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut
              || worldBlock == Blocks.BEDROCK
              || block instanceof AbstractBlockHut)
        {
            return findNextBlockSolid();
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources
              && !handleMaterials(block, blockState))
        {
            return this.getState();
        }

        placeBlockAt(block, blockState, coordinates);

        return findNextBlockSolid();
    }

    private AIState decorationStep()
    {
        if(job.getStructure() == null)
        {
            //fix for bad structures
            job.complete();
        }
        if (!goToConstructionSite())
        {
            return this.getState();
        }
        //|| job.getStructure().getBlock() == Blocks.AIR
        if (job.getStructure().doesStructureBlockEqualWorldBlock()
              || job.getStructure().getBlockState().getMaterial().isSolid())
        {
            //findNextBlock count was reached and we can ignore this block
            return findNextBlockNonSolid();
        }

        worker.faceBlock(job.getStructure().getBlockPosition());

        @Nullable final Block block = job.getStructure().getBlock();
        @Nullable final IBlockState blockState = job.getStructure().getBlockState();

        final BlockPos coords = job.getStructure().getBlockPosition();

        final Block worldBlock = world.getBlockState(coords).getBlock();

        //should never happen
        if (block == null)
        {
            @NotNull final BlockPos local = job.getStructure().getLocalPosition();
            Log.getLogger().error(String.format("StructureProxy has null block at %s- local(%s)", coords, local));
            findNextBlockNonSolid();
            return this.getState();
        }
        //don't overwrite huts or bedrock, nor place huts
        if (worldBlock instanceof AbstractBlockHut
              || worldBlock == Blocks.BEDROCK
              || block instanceof AbstractBlockHut)
        {
            findNextBlockNonSolid();
            return this.getState();
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && !handleMaterials(block, blockState))
        {
            return this.getState();
        }

        placeBlockAt(block, blockState, coords);

        return findNextBlockNonSolid();
    }

    private void placeBlockAt(@NotNull final Block block, @NotNull final IBlockState blockState, @NotNull final BlockPos coords)
    {
        if (block == Blocks.AIR)
        {
            worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);

            if (!world.setBlockToAir(coords))
            {
                Log.getLogger().error(String.format("Block break failure at %s", coords));
                //TODO handle - for now, just skipping
            }
        }
        else
        {
            final Item item = Item.getItemFromBlock(block);
            worker.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item == null ? null : new ItemStack(item, 1));

            if (placeBlock(coords, block, blockState))
            {
                setTileEntity(coords);
            }
            else
            {
                Log.getLogger().error(String.format("Block place failure %s at %s", block.getUnlocalizedName(), coords));
                //TODO handle - for now, just skipping
            }
            worker.swingArm(worker.getActiveHand());
        }
    }

    //TODO handle resources
    private void spawnEntity(@Nullable final Entity entity)
    {
        if (entity != null)
        {
            final BlockPos pos = job.getStructure().getOffsetPosition();

            if (entity instanceof EntityHanging)
            {
                @NotNull final EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.posX += pos.getX();
                entityHanging.posY += pos.getY();
                entityHanging.posZ += pos.getZ();
                //also sets position based on tile
                entityHanging.setPosition(
                  entityHanging.getHangingPosition().getX(),
                  entityHanging.getHangingPosition().getY(),
                  entityHanging.getHangingPosition().getZ());

                entityHanging.setWorld(world);
                entityHanging.dimension = world.provider.getDimension();

                world.spawnEntityInWorld(entityHanging);
            }
            else if (entity instanceof EntityMinecart)
            {
                @Nullable final EntityMinecart minecart = (EntityMinecart) entity;
                minecart.posX += pos.getX();
                minecart.posY += pos.getY();
                minecart.posZ += pos.getZ();

                minecart.setWorld(world);
                minecart.dimension = world.provider.getDimension();

                world.spawnEntityInWorld(minecart);
            }
        }
    }

    private boolean handleMaterials(@NotNull final Block block, @NotNull final IBlockState blockState)
    {
        //Breaking blocks doesn't require taking materials from the citizens inventory
        if (block == Blocks.AIR)
        {
            return true;
        }

        if (isBlockFree(block, block.getMetaFromState(blockState)))
        {
            return true;
        }

        return !checkOrRequestItems(BlockUtils.getItemStackFromBlockState(blockState));
    }

    private boolean placeBlock(@NotNull final BlockPos pos, final Block block, @NotNull final IBlockState blockState)
    {
        //Move out of the way when placing blocks
        if (MathHelper.floor_double(worker.posX) == pos.getX()
              && MathHelper.abs_int(pos.getY() - (int) worker.posY) <= 1
              && MathHelper.floor_double(worker.posZ) == pos.getZ()
              && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, RUN_AWAY_SPEED, 1.0);
        }

        //Workaround as long as we didn't rescan all of our buildings since BlockStairs now have different metadata values.
        if (blockState.getBlock() instanceof BlockStairs
              && world.getBlockState(pos).getBlock() instanceof BlockStairs
              && world.getBlockState(pos).getValue(BlockStairs.FACING) == blockState.getValue(BlockStairs.FACING)
              && blockState.getBlock() == world.getBlockState(pos).getBlock())
        {
            return true;
        }

        //We need to deal with materials
        if (!Configurations.builderInfiniteResources && world.getBlockState(pos).getBlock() != Blocks.AIR)
        {
            final List<ItemStack> items = BlockPosUtil.getBlockDrops(world, pos, 0);
            for (final ItemStack item : items)
            {
                InventoryUtils.setStack(worker.getInventoryCitizen(), item);
            }
        }

        if (block instanceof BlockDoor)
        {
            if (blockState.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
            {
                ItemDoor.placeDoor(world, pos, blockState.getValue(BlockDoor.FACING), block, false);
            }
        }
        else if (block instanceof BlockBed)
        {
            world.setBlockState(pos, blockState, 0x03);
            final EnumFacing facing = blockState.getValue(BlockBed.FACING);

            //Set other part of the bed, to the opposite PartType
            if (blockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT)
            {
                //pos.offset(facing) will get the other part of the bed
                world.setBlockState(pos, blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.FOOT), 0x03);
                world.setBlockState(pos.offset(facing), blockState.withProperty(BlockBed.PART, BlockBed.EnumPartType.HEAD), 0x03);
            }
            else
            {
                return true;
            }
        }
        else if (block instanceof BlockDoublePlant)
        {
            world.setBlockState(pos, blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 0x03);
            world.setBlockState(pos.up(), blockState.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 0x03);
        }
        else if (block instanceof BlockEndPortal || block instanceof BlockMobSpawner || block instanceof BlockDragonEgg || block instanceof BlockPortal)
        {
            return true;
        }
        else
        {
            if (!world.setBlockState(pos, blockState, 0x03))
            {
                return false;
            }
            if (world.getBlockState(pos).getBlock() == block && world.getBlockState(pos) != blockState)
            {
                world.setBlockState(pos, blockState, 0x03);
            }
        }

        //It will crash at blocks like water which is actually free, we don't have to decrease the stacks we have.
        if(isBlockFree(block, block.getMetaFromState(blockState)))
        {
            return true;
        }

        @Nullable final ItemStack stack = BlockUtils.getItemStackFromBlockState(blockState);
        if (stack == null)
        {
            Log.getLogger().error("Block causes NPE: " + blockState.getBlock());
            return false;
        }

        final int slot = worker.findFirstSlotInInventoryWith(stack.getItem());
        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
        }
        return true;
    }

    private void setTileEntity(@NotNull final BlockPos pos)
    {
        //TODO do we need to load TileEntities when building?
        @Nullable final TileEntity tileEntity = job.getStructure().getTileEntity();
        if (tileEntity != null && world.getTileEntity(pos) != null)
        {
            world.setTileEntity(pos, tileEntity);
        }
    }

    private AIState findNextBlockSolid()
    {
        //method returns false if there is no next block (structures finished)
        if (!job.getStructure().findNextBlockSolid())
        {
            job.getStructure().reset();
            incrementBlock();
            return AIState.BUILDER_DECORATION_STEP;
        }
        return this.getState();
    }

    private AIState findNextBlockNonSolid()
    {
        //method returns false if there is no next block (structures finished)
        if (!job.getStructure().findNextBlockNonSolid())
        {
            job.getStructure().reset();
            incrementBlock();
            return AIState.BUILDER_COMPLETE_BUILD;
        }
        return this.getState();
    }

    @NotNull
    private AIState completeBuild()
    {
        if(job.getStructure() == null)
        {
            //fix for bad structures
            job.complete();
        }
        job.getStructure().getEntities().forEach(this::spawnEntity);

        final String structureName = job.getStructure().getName();

        LanguageHandler.sendPlayersLocalizedMessage(worker.getColony().getMessageEntityPlayers(),
          "entity.builder.messageBuildComplete",
          structureName);

        final WorkOrderBuild wo = job.getWorkOrder();
        if (wo == null)
        {
            Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing work order(%d)",
                    worker.getColony().getID(),
                    worker.getCitizenData().getId(),
                    job.getWorkOrderId()));
        }
        else
        {
            if (!(wo instanceof WorkOrderBuildDecoration))
            {
                final AbstractBuilding building = job.getColony().getBuilding(wo.getBuildingLocation());
                if (building == null)
                {
                    Log.getLogger().error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                            worker.getColony().getID(),
                            worker.getCitizenData().getId(),
                            wo.getBuildingLocation()));
                }
                else
                {
                    building.setBuildingLevel(wo.getUpgradeLevel());
                }
            }
            job.complete();
        }

        resetTask();
        worker.addExperience(XP_EACH_BUILDING);
        workFrom = null;

        return AIState.IDLE;
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
