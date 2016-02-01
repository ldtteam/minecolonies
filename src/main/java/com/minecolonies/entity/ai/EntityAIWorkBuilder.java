package com.minecolonies.entity.ai;

import com.github.lunatrius.schematica.config.BlockInfo;
import com.minecolonies.MineColonies;
import com.minecolonies.blocks.BlockHut;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.jobs.JobBuilder;
import com.minecolonies.colony.workorders.WorkOrderBuild;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

/**
 * Performs builder work
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIWorkBuilder extends EntityAIWork<JobBuilder>
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
            if(wo != null)
            {
                Building building = job.getColony().getBuilding(wo.getBuildingId());
                if(building != null)
                {
                    //Don't go through the CLEAR stage for repairs and upgrades
                    if(building.getBuildingLevel() > 0)
                    {
                        job.stage = JobBuilder.Stage.REQUEST_MATERIALS;

                        if(!job.hasSchematic() || !incrementBlock())
                        {
                            return;
                        }
                    }
                    else
                    {
                        job.stage = JobBuilder.Stage.CLEAR;
                        if(!job.hasSchematic() || !job.getSchematic().decrementBlock())
                        {
                            return;
                        }
                    }
                }
                else
                {
                    MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)", worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingId()));
                }
            }
            else
            {
                MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)", worker.getColony().getID(), worker.getCitizenData().getId(), job.getWorkOrderId()));
                return;
            }

            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildStart", job.getSchematic().getName());
        }
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, job.getSchematic().getPosition());

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
                for(Entity entity : job.getSchematic().getEntities())
                {//TODO use iterator to do this overtime
                    spawnEntity(entity);
                }
                completeBuild();
                break;
            default:
                System.out.println("Case not implemented: " + job.stage.toString());
        }
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

        ChunkCoordinates coords = job.getSchematic().getBlockPosition();
        int x = coords.posX;
        int y = coords.posY;
        int z = coords.posZ;

        Block worldBlock = world.getBlock(x, y, z);

        if(worldBlock != Blocks.air && !(worldBlock instanceof BlockHut) && worldBlock != Blocks.bedrock)
        {
            if(!Configurations.builderInfiniteResources)//We need to deal with materials
            {
                if(!handleMaterials(Blocks.air, 0, worldBlock, world.getBlockMetadata(x, y, z))) return;
            }

            worker.setCurrentItemOrArmor(0, null);

            if(!world.setBlockToAir(x, y, z))
            {
                MineColonies.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
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
            int metadata = job.getSchematic().getMetadata();
            ItemStack itemstack = new ItemStack(block, 1, metadata);

            Block worldBlock = ChunkCoordUtils.getBlock(world, job.getSchematic().getBlockPosition());

            if(itemstack.getItem() != null && block != null && block != Blocks.air && worldBlock != Blocks.bedrock && !(worldBlock instanceof BlockHut) && !isBlockFree(block, metadata))
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
        int metadata = job.getSchematic().getMetadata();

        ChunkCoordinates coords = job.getSchematic().getBlockPosition();
        int x = coords.posX;
        int y = coords.posY;
        int z = coords.posZ;

        Block worldBlock = world.getBlock(x, y, z);
        int worldBlockMetadata = world.getBlockMetadata(x, y, z);

        if(block == null)//should never happen
        {
            ChunkCoordinates local = job.getSchematic().getLocalPosition();
            MineColonies.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.posX, local.posY, local.posZ));
            findNextBlockSolid();
            return;
        }
        if(worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock ||
                block instanceof BlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockSolid();
            return;
        }

        if(!Configurations.builderInfiniteResources)//We need to deal with materials
        {
            if(!handleMaterials(block, metadata, worldBlock, worldBlockMetadata)) return;
        }

        if(block == Blocks.air)
        {
            worker.setCurrentItemOrArmor(0, null);

            if(!world.setBlockToAir(x, y, z))
            {
                MineColonies.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                //TODO handle - for now, just skipping
            }
        }
        else
        {
            Item item = Item.getItemFromBlock(block);
            worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1, metadata) : null);

            if(placeBlock(x, y, z, block, metadata))
            {
                setTileEntity(x, y, z);
            }
            else
            {
                MineColonies.logger.error(String.format("Block place failure %s at %d, %d, %d", block.getUnlocalizedName(), x, y, z));
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
        int metadata = job.getSchematic().getMetadata();

        ChunkCoordinates coords = job.getSchematic().getBlockPosition();
        int x = coords.posX;
        int y = coords.posY;
        int z = coords.posZ;

        Block worldBlock = world.getBlock(x, y, z);
        int worldBlockMetadata = world.getBlockMetadata(x, y, z);

        if(block == null)//should never happen
        {
            ChunkCoordinates local = job.getSchematic().getLocalPosition();
            MineColonies.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.posX, local.posY, local.posZ));
            findNextBlockNonSolid();
            return;
        }
        if(worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock ||
                block instanceof BlockHut)//don't overwrite huts or bedrock, nor place huts
        {
            findNextBlockNonSolid();
            return;
        }

        if(!Configurations.builderInfiniteResources)//We need to deal with materials
        {
            if(!handleMaterials(block, metadata, worldBlock, worldBlockMetadata)) return;
        }

        Item item = Item.getItemFromBlock(block);
        worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1, metadata) : null);

        if(placeBlock(x, y, z, block, metadata))
        {
            setTileEntity(x, y, z);
        }
        else
        {
            MineColonies.logger.error(String.format("Block place failure %s at %d, %d, %d", block.getUnlocalizedName(), x, y, z));
            //TODO handle - for now, just skipping
        }

        findNextBlockNonSolid();
        worker.swingItem();
    }

    private void spawnEntity(Entity entity)//TODO handle resources
    {
        if(entity != null)
        {
            ChunkCoordinates pos = job.getSchematic().getOffsetPosition();

            if(entity instanceof EntityHanging)
            {
                EntityHanging entityHanging = (EntityHanging) entity;

                entityHanging.field_146063_b += pos.posX;//tileX
                entityHanging.field_146064_c += pos.posY;//tileY
                entityHanging.field_146062_d += pos.posZ;//tileZ
                entityHanging.setDirection(entityHanging.hangingDirection);//also sets position based on tile

                entityHanging.setWorld(world);
                entityHanging.dimension = world.provider.dimensionId;

                world.spawnEntityInWorld(entityHanging);
            }
            else if(entity instanceof EntityMinecart)
            {
                EntityMinecart minecart = (EntityMinecart) entity;
                minecart.riddenByEntity = null;
                minecart.posX += pos.posX;
                minecart.posY += pos.posY;
                minecart.posZ += pos.posZ;

                minecart.setWorld(world);
                minecart.dimension = world.provider.dimensionId;

                world.spawnEntityInWorld(minecart);
            }
        }
    }

    private boolean handleMaterials(Block block, int metadata, Block worldBlock, int worldBlockMetadata)
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

            int slotID = InventoryUtils.containsStack(worker.getInventory(), material);
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
                    if(ChunkCoordUtils.distanceSqrd(worker.getWorkBuilding().getLocation(), worker.getPosition()) < 16)//We are close to the chest
                    {
                        if(!InventoryUtils.takeStackInSlot(workBuildingTileEntity, worker.getInventory(), chestSlotID, 1, true))
                        {
                            ItemStack chestItem = workBuildingTileEntity.getStackInSlot(chestSlotID);
                            workBuildingTileEntity.setInventorySlotContents(chestSlotID, null);
                            setStackInBuilder(chestItem, true);//TODO prevent the dropping of items
                            worker.setStatus(EntityCitizen.Status.WORKING);
                        }
                    }
                    else if(worker.getNavigator().noPath() || !ChunkCoordUtils.isPathingTo(worker, worker.getWorkBuilding().getLocation()))
                    {
                        ChunkCoordUtils.moveLivingToXYZ(worker, worker.getWorkBuilding().getLocation());
                        worker.setStatus(EntityCitizen.Status.GETTING_ITEMS);
                    }
                }
                return false;
            }
            else
            {
                worker.getInventory().decrStackSize(slotID, 1);
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
        return Utils.isWater(block) || block == Blocks.leaves || block == Blocks.leaves2 || (block == Blocks.double_plant && Utils.testFlag(metadata, 0x08)) || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08));
    }

    private void setStackInBuilder(ItemStack stack, boolean shouldUseForce)
    {
        if(stack != null && stack.getItem() != null && stack.stackSize > 0)
        {
            ItemStack leftOvers = InventoryUtils.setStack(worker.getInventory(), stack);
            if(shouldUseForce && leftOvers != null)
            {
                int slotID = world.rand.nextInt(worker.getInventory().getSizeInventory());
                for(int i = 0; i < worker.getInventory().getSizeInventory(); i++)
                {
                    ItemStack invItem = worker.getInventory().getStackInSlot(i);
                    if(true)//TODO change to isRequired material using chris' system
                    {
                        leftOvers = invItem;
                        slotID = i;
                        break;
                    }
                }
                worker.getInventory().setInventorySlotContents(slotID, stack);
            }

            if(ChunkCoordUtils.distanceSqrd(worker.getWorkBuilding().getLocation(), worker.getPosition()) < 16)
            {
                TileEntityColonyBuilding tileEntity = worker.getWorkBuilding().getTileEntity();
                if(tileEntity != null)
                {
                    leftOvers = InventoryUtils.setStack(tileEntity, leftOvers);
                }
            }
            else
            {
                ChunkCoordUtils.moveLivingToXYZ(worker, worker.getWorkBuilding().getLocation());
            }

            if(leftOvers != null)
            {
                worker.entityDropItem(leftOvers);
            }
        }
    }

    private boolean placeBlock(int x, int y, int z, Block block, int metadata)
    {
        //Move out of the way when placing blocks
        if(MathHelper.floor_double(worker.posX) == x && MathHelper.abs_int(y - (int) worker.posY) <= 1 && MathHelper.floor_double(worker.posZ) == z && worker.getNavigator().noPath())
        {
            worker.getNavigator().moveAwayFromXYZ(x, y, z, 4.1, 1.0);
        }

        if(block instanceof BlockDoor)
        {
            ItemDoor.placeDoorBlock(world, x, y, z, metadata, block);
        }
        else if(block instanceof BlockBed && !Utils.testFlag(metadata, 0x8))
        {
            world.setBlock(x, y, z, block, metadata, 0x03);

            int xOffset = 0, zOffset = 0;
            if(metadata == 0)
            {
                zOffset = 1;
            }
            else if(metadata == 1)
            {
                xOffset = -1;
            }
            else if(metadata == 2)
            {
                zOffset = -1;
            }
            else if(metadata == 3)
            {
                xOffset = 1;
            }
            world.setBlock(x + xOffset, y, z + zOffset, block, metadata | 0x8, 0x03);
        }
        else if(block instanceof BlockDoublePlant)
        {
            world.setBlock(x, y, z, block, metadata, 0x03);
            world.setBlock(x, y + 1, z, block, 0x8, 0x03);
        }
        else
        {
            if(!world.setBlock(x, y, z, block, metadata, 0x03))
            {
                return false;
            }
            if(world.getBlock(x, y, z) == block)
            {
                if(world.getBlockMetadata(x, y, z) != metadata)
                {
                    world.setBlockMetadataWithNotify(x, y, z, metadata, 0x03);
                }
                block.onPostBlockPlaced(world, x, y, z, metadata);
            }
        }
        return true;
    }

    private void setTileEntity(int x, int y, int z)
    {
        TileEntity tileEntity = job.getSchematic().getTileEntity();//TODO do we need to load TileEntities when building?
        if(tileEntity != null && world.getTileEntity(x, y, z) != null)
        {
            world.setTileEntity(x, y, z, tileEntity);
        }
    }

    private boolean findNextBlockSolid()
    {
        if(!job.getSchematic().findNextBlockSolid())//method returns false if there is no next block (schematic finished)
        {
            job.stage = JobBuilder.Stage.DECORATIONS;
            job.getSchematic().reset();
            incrementBlock();
            return false;
        }
        return true;
    }

    private boolean findNextBlockNonSolid()
    {
        if(!job.getSchematic().findNextBlockNonSolid())//method returns false if there is no next block (schematic finished)
        {
            job.stage = JobBuilder.Stage.ENTITIES;
            job.getSchematic().reset();
            incrementBlock();
            return false;
        }
        return true;
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

        ChunkCoordinates pos = workOrder.getBuildingId();
        Building building = worker.getColony().getBuilding(pos);

        if(building == null)
        {
            MineColonies.logger.warn("Building does not exist - removing build request");
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        String name = building.getStyle() + '/' + workOrder.getUpgradeName();

        job.setSchematic(Schematic.loadSchematic(world, name));

        if(job.getSchematic() == null)
        {
            MineColonies.logger.warn(String.format("Schematic: (%s) does not exist - removing build request", name));
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        job.getSchematic().rotate(building.getRotation());

        job.getSchematic().setPosition(pos);
    }

    private void completeBuild()
    {
        String schematicName = job.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildComplete", schematicName);

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
                MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)", worker.getColony().getID(), worker.getCitizenData().getId(), wo.getBuildingId()));
            }
        }
        else
        {
            MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing work order(%d)", worker.getColony().getID(), worker.getCitizenData().getId(), job.getWorkOrderId()));
        }

        job.complete();
        resetTask();
    }
}