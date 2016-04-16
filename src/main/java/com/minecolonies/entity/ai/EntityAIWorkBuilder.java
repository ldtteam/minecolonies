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
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
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

/**
 * Performs builder work
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIWorkBuilder extends AbstractEntityAIWork<JobBuilder>
{
    public EntityAIWorkBuilder(JobBuilder job)
    {
        super(job);
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
            if (building.getBuildingLevel() > 0) {
                job.stage = JobBuilder.Stage.REQUEST_MATERIALS;

                if (!job.hasSchematic() || !incrementBlock()) {
                    return;
                }
            } else {
                job.stage = JobBuilder.Stage.CLEAR;
                if (!job.hasSchematic() || !job.getSchematic().decrementBlock()) {
                    return;
                }
            }


            LanguageHandler.sendPlayersLocalizedMessage(EntityUtils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildStart", job.getSchematic().getName());
        }
        BlockPosUtil.tryMoveLivingToXYZ(worker, job.getSchematic().getPosition());

        worker.setStatus(EntityCitizen.Status.WORKING);
    }

    @Override
    public void updateTask()
    {
        //Don't work every single tick
        if(worker.getOffsetTicks() % job.getWorkInterval() != 0)
        {
            return;
        }

        WorkOrderBuild wo = job.getWorkOrder();
        if(wo == null || job.getColony().getBuilding(wo.getBuildingId()) == null || !job.hasSchematic())
        {
            job.complete();
            return;
        }

        //TODO: break this up to make it more readable
        switch(job.stage)
        {
            case CLEAR:
                clearStep();
                break;
            case REQUEST_MATERIALS:
                if(!Configurations.builderInfiniteResources)
                {
                    requestMaterials();
                }
                job.stage = JobBuilder.Stage.STRUCTURE;
                break;
            case STRUCTURE:
                structureStep();
                break;
            case DECORATIONS:
                decorationStep();
                break;
            case ENTITIES:
                //TODO use iterator to do this overtime
                job.getSchematic().getEntities().forEach(this::spawnEntity);
                completeBuild();
                break;
            default:
                System.out.println("Case not implemented: " + job.stage.toString());
        }
    }

    /**
     * This method will be overridden by AI implementations
     */
    @Override
    protected void workOnTask()
    {
        //TODO: rework the builder to use workOnTask eventually
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

    private void clearStep()
    {
        if(worker.getStatus() != EntityCitizen.Status.GETTING_ITEMS)
        {
            if(!worker.isWorkerAtSiteWithMove(job.getSchematic().getPosition(), 3))
            {
                return;
            }
            worker.setStatus(EntityCitizen.Status.WORKING);
        }

        BlockPos coords = job.getSchematic().getBlockPosition();
        int      x      = coords.getX();
        int      y      = coords.getY();
        int      z      = coords.getZ();

        Block worldBlock = world.getBlockState(coords).getBlock();

        if(worldBlock != Blocks.air && !(worldBlock instanceof AbstractBlockHut) && worldBlock != Blocks.bedrock)
        {
            if(!Configurations.builderInfiniteResources)//We need to deal with materials
            {
                if(!handleMaterials(Blocks.air, 0, worldBlock, world.getBlockState(coords))) return;
            }

            worker.setCurrentItemOrArmor(0, null);

            if(!world.setBlockToAir(coords))
            {
                //TODO: create own logger in class
                Log.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                //TODO handle - for now, just skipping
            }
            worker.swingItem();
        }

        if(!job.getSchematic().findNextBlockToClear())//method returns false if there is no next block (schematic finished)
        {
            job.stage = JobBuilder.Stage.STRUCTURE;
            job.getSchematic().reset();
            incrementBlock();
        }
        worker.swingItem();
    }

    private void requestMaterials()
    {
        //TODO thread this
        while(job.getSchematic().findNextBlock())
        {
            if(job.getSchematic().doesSchematicBlockEqualWorldBlock())
            {
                continue;
            }

            Block block = job.getSchematic().getBlock();
            IBlockState metadata = job.getSchematic().getMetadata();
            ItemStack itemstack = new ItemStack(block, 1);

            Block worldBlock = BlockPosUtil.getBlock(world, job.getSchematic().getBlockPosition());

            if(itemstack.getItem() != null && block != null && block != Blocks.air && worldBlock != Blocks.bedrock && !(worldBlock instanceof AbstractBlockHut) && !isBlockFree(block, 0))
            {
                //TODO add item to prerequisites
            }
        }
        job.getSchematic().reset();
        incrementBlock();

        //TODO maybe print needed items, depends on how system works
    }

    private void structureStep()
    {
        if(job.getSchematic().doesSchematicBlockEqualWorldBlock() || (!job.getSchematic().getBlock().getMaterial().isSolid() && job.getSchematic().getBlock() != Blocks.air))
        {
            findNextBlockSolid();
            return;//findNextBlock count was reached and we can ignore this block
        }

        if(worker.getStatus() != EntityCitizen.Status.GETTING_ITEMS)
        {
            if(!worker.isWorkerAtSiteWithMove(job.getSchematic().getPosition(), 3))
            {
                return;
            }
            worker.setStatus(EntityCitizen.Status.WORKING);
        }

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
            findNextBlockSolid();
            return;
        }
        if(worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.bedrock ||
                block instanceof AbstractBlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockSolid();
            return;
        }

        if(!Configurations.builderInfiniteResources)//We need to deal with materials
        {
            if(!handleMaterials(block,0, worldBlock, worldBlockMetadata)) return;
        }

        if(block == Blocks.air)
        {
            worker.setCurrentItemOrArmor(0, null);

            if(!world.setBlockToAir(coords))
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
        }
        findNextBlockSolid();
        worker.swingItem();
    }

    private void decorationStep()
    {
        if(job.getSchematic().doesSchematicBlockEqualWorldBlock() || job.getSchematic().getBlock().getMaterial().isSolid() || job.getSchematic().getBlock() == Blocks.air)
        {
            findNextBlockNonSolid();
            return;//findNextBlock count was reached and we can ignore this block
        }

        if(worker.getStatus() != EntityCitizen.Status.GETTING_ITEMS)
        {
            if(!worker.isWorkerAtSiteWithMove(job.getSchematic().getPosition(), 3))
            {
                return;
            }
            worker.setStatus(EntityCitizen.Status.WORKING);
        }

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
            return;
        }
        if(worldBlock instanceof AbstractBlockHut || worldBlock == Blocks.bedrock ||
                block instanceof AbstractBlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockNonSolid();
            return;
        }

        if(!Configurations.builderInfiniteResources)//We need to deal with materials
        {
            if(!handleMaterials(block, 0, worldBlock, worldBlockMetadata)) return;
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

        findNextBlockNonSolid();
        worker.swingItem();
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
    private boolean handleMaterials(Block block, int metadata, Block worldBlock, IBlockState worldBlockMetadata)
    {
        if(block != Blocks.air)//Breaking blocks doesn't require taking materials from the citizens inventory
        {
            if(isBlockFree(block, metadata)) return true;

            //Modify metadata
            if(BlockInfo.BLOCK_LIST_IGNORE_METADATA.contains(block))
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
            }

            ItemStack material = new ItemStack(BlockInfo.getItemFromBlock(block), 1, metadata);

            int slotID = InventoryUtils.containsStack(getInventory(), material);
            if(slotID == -1)//inventory doesn't contain item
            {
                TileEntityColonyBuilding workBuildingTileEntity = worker.getWorkBuilding().getTileEntity();

                if(workBuildingTileEntity == null)//Work Building is not loaded
                {
                    return false;
                }

                int chestSlotID = InventoryUtils.containsStack(workBuildingTileEntity, material);
                if(chestSlotID != -1)//chest contains item
                {
                    if(worker.getWorkBuilding().getLocation().distanceSq(worker.getPosition()) < 16)//We are close to the chest
                    {
                        if(!InventoryUtils.takeStackInSlot(workBuildingTileEntity, getInventory(), chestSlotID, 1, true))
                        {
                            ItemStack chestItem = workBuildingTileEntity.getStackInSlot(chestSlotID);
                            workBuildingTileEntity.setInventorySlotContents(chestSlotID, null);
                            setStackInBuilder(chestItem, true);//TODO prevent the dropping of items
                            worker.setStatus(EntityCitizen.Status.WORKING);
                        }
                    }
                    else if(worker.getNavigator().noPath() || !BlockPosUtil.isPathingTo(worker, worker.getWorkBuilding().getLocation()))
                    {
                        BlockPosUtil.moveLivingToXYZ(worker, worker.getWorkBuilding().getLocation());
                        worker.setStatus(EntityCitizen.Status.GETTING_ITEMS);
                    }
                }
                return false;
            }
            else
            {
                getInventory().decrStackSize(slotID, 1);
            }
        }

        if(worldBlock != Blocks.air)//Don't collect air blocks.
        {
            int fortuneModifier = EnchantmentHelper.getFortuneModifier(worker);
            Item itemDropped = worldBlock.getItemDropped(worldBlockMetadata, world.rand, fortuneModifier);
            int quantityDropped = worldBlock.quantityDropped(worldBlockMetadata, fortuneModifier, world.rand);
            int damageDropped = worldBlock.damageDropped(worldBlockMetadata);

            if(itemDropped != null && quantityDropped > 0)
            {
                setStackInBuilder(new ItemStack(itemDropped, quantityDropped, damageDropped), false);
            }
        }
        worker.setStatus(EntityCitizen.Status.WORKING);
        return true;
    }

    private boolean isBlockFree(Block block, int metadata)
    {
        return BlockUtils.isWater(block.getDefaultState()) || block == Blocks.leaves || block == Blocks.leaves2 || (block == Blocks.double_plant && Utils.testFlag(metadata, 0x08)) || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08));
    }

    private void setStackInBuilder(ItemStack stack, boolean shouldUseForce)
    {
        if(stack != null && stack.getItem() != null && stack.stackSize > 0)
        {
            ItemStack leftOvers = InventoryUtils.setStack(getInventory(), stack);
            if(shouldUseForce && leftOvers != null)
            {
                int slotID = world.rand.nextInt(getInventory().getSizeInventory());
                for(int i = 0; i < getInventory().getSizeInventory(); i++)
                {
                    //Keeping the TODO but removing the if
                    //TODO change to isRequired material using chris' system
                    //TODO make looping??
                    leftOvers = getInventory().getStackInSlot(i);
                    slotID = i;
                    break;

                }
                getInventory().setInventorySlotContents(slotID, stack);
            }

            if(worker.getWorkBuilding().getLocation().distanceSq(worker.getPosition()) < 16)
            {
                TileEntityColonyBuilding tileEntity = worker.getWorkBuilding().getTileEntity();
                if(tileEntity != null)
                {
                    leftOvers = InventoryUtils.setStack(tileEntity, leftOvers);
                }
            }
            else
            {
                BlockPosUtil.moveLivingToXYZ(worker, worker.getWorkBuilding().getLocation());
            }

            if(leftOvers != null)
            {
                worker.entityDropItem(leftOvers);
            }
        }
    }

    private boolean placeBlock(BlockPos pos, Block block, IBlockState metadata)
    {
        //Move out of the way when placing blocks
        if(MathHelper.floor_double(worker.posX) == pos.getX() && MathHelper.abs_int(pos.getY() - (int) worker.posY) <= 1 && MathHelper.floor_double(worker.posZ) == pos.getZ() && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(pos, 4.1, 1.0);
        }

        if(block instanceof BlockDoor)
        {
            ItemDoor.placeDoor(world, pos, metadata.getValue(BlockDoor.FACING), block);
        }
        else if(block instanceof BlockBed /*&& !Utils.testFlag(metadata, 0x8)*/)
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
                //todo
                if(world.getBlockState(pos) != metadata)
                {
                    world.setBlockState(pos, metadata, 0x03);
                }
                //block.onPostBlockPlaced(world, x, y, z, metadata);
            }
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

    private void findNextBlockSolid()
    {
        if(!job.getSchematic().findNextBlockSolid())//method returns false if there is no next block (schematic finished)
        {
            job.stage = JobBuilder.Stage.DECORATIONS;
            job.getSchematic().reset();
            incrementBlock();
        }
    }

    private void findNextBlockNonSolid()
    {
        if(!job.getSchematic().findNextBlockNonSolid())//method returns false if there is no next block (schematic finished)
        {
            job.stage = JobBuilder.Stage.ENTITIES;
            job.getSchematic().reset();
            incrementBlock();
        }
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
    }

    private void completeBuild()
    {
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
    }
}