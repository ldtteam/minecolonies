package com.minecolonies.entity.ai;

import com.minecolonies.blocks.AbstractBlockHut;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.*;
import com.schematica.config.BlockInfo;
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

import static com.minecolonies.entity.ai.AIState.*;

/**
 * Performs builder work
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIWorkBuilder extends AbstractEntityAIWork<JobBuilder>
{
    /**
     * Amount of xp the builder gains each building (Will increase by attribute modifiers additionally)
     */
    private static final double XP_EACH_BUILDING = 2.5;
    /**
     * Position where the Builders constructs from.
     */
    private BlockPos workFrom = null;
    public EntityAIWorkBuilder(JobBuilder job)
    {
        super(job);
        super.registerTargets(
                new AITarget(this::checkIfExecute, () -> state),
                new AITarget(IDLE, () -> START_WORKING),
                new AITarget(PREPARING, this::preparing),
                new AITarget(START_WORKING, this::startWorkingAtOwnBuilding),
                new AITarget(BUILDER_CLEAR_STEP, this::clearStep),
                new AITarget(BUILDER_REQUEST_MATERIALS, this::requestMaterials),
                new AITarget(BUILDER_STRUCTURE_STEP, this::structureStep),
                new AITarget(BUILDER_DECORATION_STEP, this::decorationStep),
                new AITarget(BUILDER_COMPLETE_BUILD, this::completeBuild)
        );
        worker.setSkillModifier(2*worker.getCitizenData().getIntelligence() + worker.getCitizenData().getStrength());
    }

    private AIState preparing()
    {
        if(needsSomething())
        {
            return AIState.PREPARING;
        }
        return AIState.BUILDER_CLEAR_STEP;
    }

    private boolean checkIfExecute()
    {
        setDelay(1);
        WorkOrderBuild wo = job.getWorkOrder();
        if(wo == null || job.getColony().getBuilding(wo.getBuildingId()) == null || !job.hasSchematic())
        {
            job.complete();
            return true;
        }
        return false;
    }

    private AIState startWorkingAtOwnBuilding()
    {
        if (walkToBuilding())
        {
            return state;
        }
        return PREPARING;
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
    public boolean shouldExecute()
    {
        return super.shouldExecute() && job.hasWorkOrder();
    }

    @Override
    public void startExecuting()
    {
        if(!job.hasSchematic())//is build in progress
        {
            loadSchematic();

            WorkOrderBuild wo = job.getWorkOrder();
            if (wo == null)
            {
                Log.logger.error(
                        String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)",
                                      worker.getColony().getID(),
                                      worker.getCitizenData().getId(), job.getWorkOrderId()));
                return;
            }
            Building building = job.getColony().getBuilding(wo.getBuildingId());
            if (building == null)
            {
                Log.logger.error(
                        String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                                      worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingId()));
                return;
            }
            //Don't go through the CLEAR stage for repairs and upgrades
            if (building.getBuildingLevel() > 0)
            {
                this.state = AIState.BUILDER_REQUEST_MATERIALS;
                job.setCleared(true);
                if (!job.hasSchematic() || !incrementBlock())
                {
                    return;
                }
            }
            else
            {
                this.state = AIState.BUILDER_CLEAR_STEP;
                if (!job.hasSchematic() || !job.getSchematic().decrementBlock())
                {
                    return;
                }
            }

            LanguageHandler.sendPlayersLocalizedMessage(EntityUtils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildStart", job.getSchematic().getName());
        }
        BlockPosUtil.tryMoveLivingToXYZ(worker, job.getSchematic().getPosition());
        workFrom = null;
        worker.setStatus(EntityCitizen.Status.WORKING);
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting() /* <== this.shouldExecute() */ && job.hasSchematic();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
        worker.setCurrentItemOrArmor(0, null);
    }

    /**
     * Will lead the worker to a good position to construct.
     * @return true if the position has been reached.
     */
    private boolean goToConstructionSite()
    {
        if(workFrom == null)
        {
            workFrom = getWorkingPosition();
        }

        return walkToBlock(workFrom) || !(worker.getPosition().distanceSq(workFrom) < 10);
    }

    /**
     * Calculates the working position. Takes a min distance from width and length.
     * Then finds the floor level at that distance and then check if it does contain two air levels.
     * @return BlockPos position to work from.
     */
    private BlockPos getWorkingPosition()
    {
        //get length or width either is larger.
        int length = job.getSchematic().getLength();
        int width = job.getSchematic().getWidth();
        int distance = width > length? width : length;
        EnumFacing[] directions = {EnumFacing.EAST,EnumFacing.WEST,EnumFacing.NORTH,EnumFacing.SOUTH};

        //then get a solid place with two air spaces above it in any direction.
        for(EnumFacing direction: directions)
        {
            BlockPos positionInDirection = getPositionInDirection(direction,distance);
            if(world.getBlockState(positionInDirection.up(2)).getBlock().getMaterial().isOpaque() && world.getBlockState(positionInDirection).getBlock().getMaterial().isSolid())
            {
                return positionInDirection;
            }
        }

        //if necessary we can could implement calling getWorkingPosition recursively and add some "offset" to the sides.
        return job.getSchematic().getPosition();
    }

    /**
     * Gets a floorPosition in a particular direction
     * @param facing the direction
     * @param distance the distance
     * @return a BlockPos position.
     */
    private BlockPos getPositionInDirection(EnumFacing facing, int distance)
    {
        return getFloor(job.getSchematic().getPosition().offset(facing,distance));
    }
    /**
     * Calculates the floor level
     * @param position input position
     * @return returns BlockPos position with air above
     */
    private BlockPos getFloor(BlockPos position)
    {
        if(world.isAirBlock(position.up()))
        {
            return position;
        }
        else
        {
            return getFloor(position.up());
        }
    }

    private AIState clearStep()
    {
        if(job.isCleared())
        {
            return AIState.BUILDER_STRUCTURE_STEP;
        }

        BlockPos coordinates = job.getSchematic().getBlockPosition();
        Block worldBlock = world.getBlockState(coordinates).getBlock();

        if(worldBlock != Blocks.air && !(worldBlock instanceof AbstractBlockHut) && worldBlock != Blocks.bedrock)
        {
            //Fill workFrom with the position from where the builder should build.
            if(goToConstructionSite())
            {
                return AIState.BUILDER_CLEAR_STEP;
            }

            worker.faceBlock(coordinates);
            if(Configurations.builderInfiniteResources || worldBlock.getMaterial().isLiquid())//We need to deal with materials
            {
                worker.setCurrentItemOrArmor(0, null);

                if(!world.setBlockToAir(coordinates))
                {
                    //TODO: create own logger in class
                    Log.logger.error(String.format("Block break failure at %d, %d, %d", coordinates.getX(), coordinates.getY(), coordinates.getZ()));
                    //TODO handle - for now, just skipping
                }
                worker.swingItem();
            }
            else
            {
                if(!mineBlock(coordinates))
                {
                    //Worker running between his chest and working site, have to tweak this.
                    return AIState.PREPARING;
                }
            }

            worker.swingItem();
        }

        if(!job.getSchematic().findNextBlockToClear())//method returns false if there is no next block (schematic finished)
        {
            job.getSchematic().reset();
            incrementBlock();
            job.setCleared(true);
            return AIState.BUILDER_REQUEST_MATERIALS;
        }
        return AIState.BUILDER_CLEAR_STEP;
    }

    private AIState requestMaterials()
    {
         if(!Configurations.builderInfiniteResources)//We need to deal with materials
         {
            //TODO thread this
            while (job.getSchematic().findNextBlock())
            {
                if (job.getSchematic().doesSchematicBlockEqualWorldBlock())
                {
                    continue;
                }

                Block block = job.getSchematic().getBlock();
                IBlockState metadata = job.getSchematic().getMetadata();
                ItemStack itemstack = new ItemStack(block, 1);

                Block worldBlock = BlockPosUtil.getBlock(world, job.getSchematic().getBlockPosition());

                if (itemstack.getItem() != null && block != null && block != Blocks.air && worldBlock != Blocks.bedrock && !(worldBlock instanceof AbstractBlockHut) && !isBlockFree(block, 0))
                {
                    if (checkOrRequestItems(new ItemStack(block)))
                    {
                        job.getSchematic().reset();
                        return AIState.BUILDER_REQUEST_MATERIALS;
                    }
                }
            }
            job.getSchematic().reset();
            incrementBlock();
        }
        return AIState.BUILDER_STRUCTURE_STEP;
    }

    private AIState structureStep()
    {
        if(job.getSchematic().doesSchematicBlockEqualWorldBlock() || (!job.getSchematic().getBlock().getMaterial().isSolid() && job.getSchematic().getBlock() != Blocks.air))
        {
            return findNextBlockSolid();//findNextBlock count was reached and we can ignore this block
        }

        if(goToConstructionSite())
        {
            return AIState.BUILDER_STRUCTURE_STEP;
        }

        worker.faceBlock(job.getSchematic().getBlockPosition());
        Block block = job.getSchematic().getBlock();
        IBlockState metadata = job.getSchematic().getMetadata();

        BlockPos coordinates = job.getSchematic().getBlockPosition();
        int x = coordinates.getX();
        int y = coordinates.getY();
        int z = coordinates.getZ();

        Block       worldBlock         = world.getBlockState(coordinates).getBlock();

        if(block == null)//should never happen
        {
            BlockPos local = job.getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockSolid();
            return AIState.BUILDER_STRUCTURE_STEP;
        }
        if(worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.bedrock ||
                block instanceof AbstractBlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockSolid();
            return AIState.BUILDER_STRUCTURE_STEP;
        }

        if(!Configurations.builderInfiniteResources)//We need to deal with materials if(!Configurations.builderInfiniteResources)
        {
            if(!handleMaterials(block,metadata))
            {
                return AIState.BUILDER_STRUCTURE_STEP;
            }
        }

        if(block == Blocks.air)
        {
            worker.setCurrentItemOrArmor(0, null);

            if(!world.setBlockToAir(coordinates))
            {
                Log.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                //TODO handle - for now, just skipping
            }
        }
        else
        {
            Item item = Item.getItemFromBlock(block);
            worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);

            if(placeBlock(new BlockPos(x,y,z), block, metadata))
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
        if(job.getSchematic().doesSchematicBlockEqualWorldBlock() || job.getSchematic().getBlock().getMaterial().isSolid() || job.getSchematic().getBlock() == Blocks.air)
        {
            return findNextBlockNonSolid();//findNextBlock count was reached and we can ignore this block
        }

        if(goToConstructionSite())
        {
            return AIState.BUILDER_DECORATION_STEP;
        }

        worker.faceBlock(job.getSchematic().getBlockPosition());
        Block block = job.getSchematic().getBlock();
        IBlockState metadata = job.getSchematic().getMetadata();

        BlockPos coords = job.getSchematic().getBlockPosition();
        int x = coords.getX();
        int y = coords.getY();
        int z = coords.getZ();

        Block       worldBlock         = world.getBlockState(coords).getBlock();
        IBlockState worldBlockMetadata = world.getBlockState(coords);

        if(block == null)//should never happen
        {
            BlockPos local = job.getSchematic().getLocalPosition();
            Log.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.getX(), local.getY(), local.getZ()));
            findNextBlockNonSolid();
            return AIState.BUILDER_DECORATION_STEP;
        }
        if(worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.bedrock ||
                block instanceof AbstractBlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockNonSolid();
            return AIState.BUILDER_DECORATION_STEP;
        }

        if(!Configurations.builderInfiniteResources)//We need to deal with materials
        {
            if(!handleMaterials(block, metadata)) return AIState.BUILDER_DECORATION_STEP;
        }

        Item item = Item.getItemFromBlock(block);
        worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1) : null);

        if(placeBlock(new BlockPos(x, y, z), block, metadata))
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
        if(entity != null)
        {
            BlockPos pos = job.getSchematic().getOffsetPosition();

            if(entity instanceof EntityHanging)
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
            else if(entity instanceof EntityMinecart)
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean handleMaterials(Block block, IBlockState metadata)
    {
        if(block != Blocks.air)//Breaking blocks doesn't require taking materials from the citizens inventory
        {
            if(isBlockFree(block, block.getMetaFromState(metadata))) return true;

            //todo cobblestone slabs
            //Modify metadata
            /*if(BlockInfo.BLOCK_LIST_IGNORE_METADATA.contains(block))
            {
                metadata = 0;
            }
            else if(block == Blocks.log || block == Blocks.log2 || block == Blocks.wooden_slab)//will probably need more in the future, will fix as I come across them
            {
                metadata %= 4;
            }
            else if(block == Blocks.stone_slab)
            {
                metadata %= 8;
            }*/

            if (checkOrRequestItems(new ItemStack(metadata.getBlock(), 1)))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Defines blocks that can be built for free
     * @param block The block to check if it is free
     * @param metadata The metadata of the block
     * @return true or false
     */
    private boolean isBlockFree(Block block, int metadata)
    {
        return BlockUtils.isWater(block.getDefaultState()) || block == Blocks.leaves || block == Blocks.leaves2 || (block == Blocks.double_plant && Utils.testFlag(metadata, 0x08)) || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08) || block == Blocks.grass || block == Blocks.dirt);
    }

    private boolean placeBlock(BlockPos pos, Block block, IBlockState metadata)
    {
        //Move out of the way when placing blocks
        if(MathHelper.floor_double(worker.posX) == pos.getX() && MathHelper.abs_int(pos.getY() - (int) worker.posY) <= 1 && MathHelper.floor_double(worker.posZ) == pos.getZ() && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, 4.1, 1.0);
        }

        //Workaround as long as we didn't rescan all of our buildings since BlockStairs now have different metadata values.
        if( world.getBlockState(pos).getBlock() instanceof BlockStairs && world.getBlockState(pos).getValue(BlockStairs.FACING) == metadata.getValue(BlockStairs.FACING))
        {
            return true;
        }

        if(block instanceof BlockDoor && metadata.getValue(BlockDoor.HALF).equals(BlockDoor.EnumDoorHalf.LOWER))
        {
            ItemDoor.placeDoor(world, pos, metadata.getValue(BlockDoor.FACING), block);
        }
        else if(block instanceof BlockBed /*&& !Utils.testFlag(metadata, 0x8)*/)//TODO fix beds
        {
            world.setBlockState(pos, metadata,0x03);
            EnumFacing meta = metadata.getValue(BlockBed.FACING);
            int xOffset = 0, zOffset = 0;
            if(meta == EnumFacing.NORTH)
            {
                zOffset = 1;
            }
            else if(meta == EnumFacing.SOUTH)
            {
                xOffset = -1;
            }
            else if(meta == EnumFacing.WEST)
            {
                zOffset = -1;
            }
            else if(meta == EnumFacing.EAST)
            {
                xOffset = 1;
            }
            world.setBlockState(pos.add(xOffset,0,zOffset), metadata, 0x03);
        }
        else if(block instanceof BlockDoublePlant)
        {
            world.setBlockState(pos, metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.LOWER), 0x03);
            world.setBlockState(pos.up(), metadata.withProperty(BlockDoublePlant.HALF, BlockDoublePlant.EnumBlockHalf.UPPER), 0x03);
        }
        else
        {
            if(!world.setBlockState(pos, metadata, 0x03))
            {
                return false;
            }
            if(world.getBlockState(pos).getBlock() == block)
            {
                if(world.getBlockState(pos) != metadata)
                {
                    world.setBlockState(pos, metadata, 0x03);
                }
                //todo do we need this? block.onPostBlockPlaced(world, x, y, z, metadata);
            }
        }

        int slot = worker.findFirstSlotInInventoryWith(Item.getItemFromBlock(block));
        if(slot == -1)
        {
            slot = worker.findFirstSlotInInventoryWith(block.getItem(world,pos));
        }

        if (slot != -1)
        {
            getInventory().decrStackSize(slot, 1);
            //Flag 1+2 is needed for updates
        }
        return true;
    }

    private void setTileEntity(BlockPos pos)
    {
        TileEntity tileEntity = job.getSchematic().getTileEntity();//TODO do we need to load TileEntities when building?
        if(tileEntity != null && world.getTileEntity(pos) != null)
        {
            world.setTileEntity(pos, tileEntity);
        }
    }

    private AIState findNextBlockSolid()
    {
        if(!job.getSchematic().findNextBlockSolid())//method returns false if there is no next block (schematic finished)
        {
            job.getSchematic().reset();
            incrementBlock();
            return AIState.BUILDER_DECORATION_STEP;
        }
        return AIState.BUILDER_STRUCTURE_STEP;
    }

    private AIState findNextBlockNonSolid()
    {
        if(!job.getSchematic().findNextBlockNonSolid())//method returns false if there is no next block (schematic finished)
        {
            job.getSchematic().reset();
            incrementBlock();
            return AIState.BUILDER_COMPLETE_BUILD;
        }
        return AIState.BUILDER_DECORATION_STEP;
    }

    private boolean incrementBlock()
    {
        return job.getSchematic().incrementBlock();//method returns false if there is no next block (schematic finished)
    }

    private void loadSchematic()
    {
        WorkOrderBuild workOrder = job.getWorkOrder();
        if(workOrder == null)
        {
            return;
        }

        BlockPos pos = workOrder.getBuildingId();
        Building building = worker.getColony().getBuilding(pos);

        if(building == null)
        {
            Log.logger.warn("Building does not exist - removing build request");
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        String name = building.getStyle() + '/' + workOrder.getUpgradeName();

        try
        {
            job.setSchematic(new Schematic(world, name));
        }
        catch (IllegalStateException e)
        {
            Log.logger.warn(String.format("Schematic: (%s) does not exist - removing build request", name), e);
            job.setSchematic(null);
            return;
        }

        job.getSchematic().rotate(building.getRotation());
        job.getSchematic().setPosition(pos);
        job.setCleared(false);
    }

    private AIState completeBuild()
    {
        job.getSchematic().getEntities().forEach(this::spawnEntity);

        String schematicName = job.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(EntityUtils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildComplete", schematicName);

        WorkOrderBuild wo = job.getWorkOrder();
        if(wo != null)
        {
            Building building = job.getColony().getBuilding(wo.getBuildingId());
            if(building != null)
            {
                building.setBuildingLevel(wo.getUpgradeLevel());
            }
            else
            {
                Log.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)", worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingId()));
            }
        }
        else
        {
            Log.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing work order(%d)", worker.getColony().getID(), worker.getCitizenData().getId(), job.getWorkOrderId()));
        }

        job.complete();
        resetTask();
        worker.addExperience(XP_EACH_BUILDING);

        return AIState.IDLE;
    }
}