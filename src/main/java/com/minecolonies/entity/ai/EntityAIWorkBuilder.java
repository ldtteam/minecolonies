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
import cpw.mods.fml.common.FMLCommonHandler;
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
        if (!job.hasSchematic())//is build in progress
        {
            loadSchematic();

            WorkOrderBuild wo = job.getWorkOrder();
            if (wo != null)
            {
                Building building = job.getColony().getBuilding(wo.getBuildingId());
                if (building != null)
                {
                    //Don't go through the CLEAR stage for repairs and upgrades
                    if(building.getBuildingLevel() > 0)
                    {
                        job.stage = JobBuilder.Stage.REQUEST_MATERIALS;

                        if (!job.hasSchematic() || !incrementBlock())
                        {
                            return;
                        }
                    }
                    else
                    {
                        job.stage = JobBuilder.Stage.CLEAR;
                        if (!job.hasSchematic() || !job.getSchematic().decrementBlock())
                        {
                            return;
                        }
                    }
                }
                else
                {
                    MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Starting and missing building(%s)",
                            worker.getColony().getID(), worker.getCitizenData().getId(),
                            wo.getBuildingId()));
                }
            }
            else
            {
                MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Starting and missing work order(%d)", worker.getColony().getID(), worker.getCitizenData().getId(), job.getWorkOrderId()));
            }

            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildStart", job.getSchematic().getName());
        }
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, job.getSchematic().getPosition());

        worker.setStatus(EntityCitizen.Status.WORKING);
    }

    @Override
    public void updateTask()
    {
        if (worker.getOffsetTicks() % job.getWorkInterval() != 0)
        {
            return;
        }

        WorkOrderBuild wo = job.getWorkOrder();
        if (wo == null || job.getColony().getBuilding(wo.getBuildingId()) == null)
        {
            job.complete();
            return;
        }

        if (!job.hasSchematic())
        {
            job.complete();
            return;//Fixes crash caused by buildings needing no repairs
        }

        ChunkCoordinates coords;
        int x, y, z;
        Block block, worldBlock;
        int metadata, worldBlockMetadata;

        switch (job.stage)
        {
        case CLEAR:
            coords = job.getSchematic().getBlockPosition();
            x = coords.posX;
            y = coords.posY;
            z = coords.posZ;

            worldBlock = world.getBlock(x, y, z);

            if (worldBlock != Blocks.air && !(worldBlock instanceof BlockHut) && worldBlock != Blocks.bedrock)
            {
                if (!Configurations.builderInfiniteResources)//We need to deal with materials
                {
                    if (!handleMaterials(Blocks.air, 0, worldBlock, world.getBlockMetadata(x, y, z)))
                        return;
                }

                worker.setCurrentItemOrArmor(0, null);

                if (!world.setBlockToAir(x, y, z))
                {
                    MineColonies.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                    //TODO handle - for now, just skipping
                }
                worker.swingItem();
            }

            if (!job.getSchematic().findNextBlockWorldNonAir())//method returns false if there is no next block (schematic finished)
            {
                job.stage = JobBuilder.Stage.STRUCTURE;
                job.getSchematic().reset();
                incrementBlock();
            }
            MineColonies.logger.info(x + ", " + y + ", " + z);
            worker.swingItem();
            break;
        case REQUEST_MATERIALS:
            if(Configurations.builderInfiniteResources)
            {
                job.stage = JobBuilder.Stage.STRUCTURE;
            }
            else
            {
                requestMaterials();
            }
            break;
        case STRUCTURE:
            if (job.getSchematic().doesSchematicBlockEqualWorldBlock() || !job.getSchematic().getBlock().getMaterial().isSolid())
            {
                findNextBlockSolid();
                return;//findNextBlock count was reached and we can ignore this block
            }

            if (worker.getStatus() != EntityCitizen.Status.GETTING_ITEMS)
            {
                if (!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.getSchematic().getPosition()))
                {
                    return;
                }
                worker.setStatus(EntityCitizen.Status.WORKING);
            }

            block = job.getSchematic().getBlock();
            metadata = job.getSchematic().getMetadata();

            coords = job.getSchematic().getBlockPosition();
            x = coords.posX;
            y = coords.posY;
            z = coords.posZ;

            worldBlock = world.getBlock(x, y, z);
            worldBlockMetadata = world.getBlockMetadata(x, y, z);

            if (block == null)//should never happen
            {
                ChunkCoordinates local = job.getSchematic().getLocalPosition();
                MineColonies.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.posX, local.posY, local.posZ));
                findNextBlockSolid();
                return;
            }
            if (worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock ||
                    block instanceof BlockHut)//don't overwrite huts or bedrock, nor place huts
            {
                findNextBlockSolid();
                return;
            }

            if (!Configurations.builderInfiniteResources)//We need to deal with materials
            {
                if (!handleMaterials(block, metadata, worldBlock, worldBlockMetadata))
                    return;
            }

            if (block == Blocks.air)
            {
                worker.setCurrentItemOrArmor(0, null);

                if (!world.setBlockToAir(x, y, z))
                {
                    MineColonies.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                    //TODO handle - for now, just skipping
                }
            }
            else
            {
                Item item = Item.getItemFromBlock(block);
                worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1, metadata) : null);

                if (placeBlock(x, y, z, block, metadata))
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
            break;
        case DECORATIONS:
            if (job.getSchematic().doesSchematicBlockEqualWorldBlock() || job.getSchematic().getBlock().getMaterial().isSolid())
            {
                findNextBlockNonSolid();
                return;//findNextBlock count was reached and we can ignore this block
            }

            if (worker.getStatus() != EntityCitizen.Status.GETTING_ITEMS)
            {
                if (!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.getSchematic().getPosition()))
                {
                    return;
                }
                worker.setStatus(EntityCitizen.Status.WORKING);
            }

            block = job.getSchematic().getBlock();
            metadata = job.getSchematic().getMetadata();

            coords = job.getSchematic().getBlockPosition();
            x = coords.posX;
            y = coords.posY;
            z = coords.posZ;

            worldBlock = world.getBlock(x, y, z);
            worldBlockMetadata = world.getBlockMetadata(x, y, z);

            if (block == null)//should never happen
            {
                ChunkCoordinates local = job.getSchematic().getLocalPosition();
                MineColonies.logger.error(String.format("Schematic has null block at %d, %d, %d - local(%d, %d, %d)", x, y, z, local.posX, local.posY, local.posZ));
                findNextBlockNonSolid();
                return;
            }
            if (worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock ||
                    block instanceof BlockHut)//don't overwrite huts or bedrock, nor place huts
            {
                findNextBlockNonSolid();
                return;
            }

            if (!Configurations.builderInfiniteResources)//We need to deal with materials
            {
                if (!handleMaterials(block, metadata, worldBlock, worldBlockMetadata))
                    return;
            }

            if (block == Blocks.air)
            {
                worker.setCurrentItemOrArmor(0, null);

                if (!world.setBlockToAir(x, y, z))
                {
                    MineColonies.logger.error(String.format("Block break failure at %d, %d, %d", x, y, z));
                    //TODO handle - for now, just skipping
                }
            }
            else
            {
                Item item = Item.getItemFromBlock(block);
                worker.setCurrentItemOrArmor(0, item != null ? new ItemStack(item, 1, metadata) : null);

                if (placeBlock(x, y, z, block, metadata))
                {
                    setTileEntity(x, y, z);
                }
                else
                {
                    MineColonies.logger.error(String.format("Block place failure %s at %d, %d, %d", block.getUnlocalizedName(), x, y, z));
                    //TODO handle - for now, just skipping
                }
            }
            findNextBlockNonSolid();
            worker.swingItem();
            break;
        case ENTITIES:
            for (Entity entity : job.getSchematic().getEntities())
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

    private void spawnEntity(Entity entity)//TODO handle resources
    {
        if (entity != null)
        {
            ChunkCoordinates pos = job.getSchematic().getOffsetPosition();

            if (entity instanceof EntityHanging)
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
            else if (entity instanceof EntityMinecart)
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

    private boolean findNextBlockSolid()
    {
        if (!job.getSchematic().findNextBlockSolid())//method returns false if there is no next block (schematic finished)
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
        if (!job.getSchematic().findNextBlockNonSolid())//method returns false if there is no next block (schematic finished)
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

    private void requestMaterials()
    {
        Schematic schematic = Schematic.loadSchematic(world, job.getSchematic().getName());
        schematic.setPosition(job.getSchematic().getPosition());
        boolean placesBlock = false;

        while (schematic.findNextBlock())
        {
            Block block = schematic.getBlock();
            int metadata = schematic.getMetadata();
            ItemStack itemstack = new ItemStack(block, 1, metadata);

            ChunkCoordinates pos = schematic.getBlockPosition();

            Block worldBlock = world.getBlock(pos.posX, pos.posY, pos.posZ);

            if (itemstack.getItem() == null || block == null || block == Blocks.air || worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock)
            {
                continue;
            }

            placesBlock = true;

            if (InventoryUtils.containsStack(worker.getInventory(), itemstack) == -1)
            {
                job.addItemNeeded(itemstack);
            }
        }

        if (placesBlock)
        {
            for (ItemStack neededItem : job.getItemsNeeded())
            {
                LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageNeedMaterial", neededItem.getDisplayName(), neededItem.stackSize);
            }
        }

        job.stage = JobBuilder.Stage.STRUCTURE;
    }

    private boolean handleMaterials(Block block, int metadata, Block worldBlock, int worldBlockMetadata)
    {
        System.out.println(FMLCommonHandler.instance().getSide().toString() + " : " + FMLCommonHandler.instance().getEffectiveSide().toString());
        if (block != Blocks.air)
        {
            System.out.println(block.getUnlocalizedName());

            if (Utils.isWater(block) || block == Blocks.leaves || block == Blocks.leaves2 || (block == Blocks.double_plant && Utils.testFlag(metadata, 0x08)) || (block instanceof BlockDoor
                    && Utils.testFlag(metadata, 0x08)))
                return true;//free blocks

            Item item = BlockInfo.getItemFromBlock(block);
            if (BlockInfo.BLOCK_LIST_IGNORE_METADATA.contains(block))
            {
                metadata = 0;
            }
            else if (block == Blocks.log || block == Blocks.log2 || block == Blocks.wooden_slab)//will probably need more in the future, will fix as I come across them
            {
                metadata %= 4;
            }
            else if (block == Blocks.stone_slab)
            {
                metadata %= 8;
            }

            ItemStack material = new ItemStack(item, 1, metadata);
            System.out.println(material.getItem().getUnlocalizedName() + " : " + material.getItemDamage());

            int slotID = InventoryUtils.containsStack(worker.getInventory(), material);
            if (slotID == -1)//inventory doesn't contain item
            {
                TileEntityColonyBuilding workBuildingTileEntity = worker.getWorkBuilding().getTileEntity();

                if (workBuildingTileEntity == null)
                {
                    //  Work Building is not loaded
                    return false;
                }

                int chestSlotID = InventoryUtils.containsStack(workBuildingTileEntity, material);
                if (chestSlotID != -1)//chest contains item
                {
                    if (ChunkCoordUtils.distanceSqrd(worker.getWorkBuilding().getLocation(), worker.getPosition()) < 16)
                    {
                        if (!InventoryUtils.takeStackInSlot(workBuildingTileEntity, worker.getInventory(), chestSlotID, 1, true))
                        {
                            ItemStack chestItem = workBuildingTileEntity.getStackInSlot(chestSlotID);
                            workBuildingTileEntity.setInventorySlotContents(chestSlotID, null);
                            setStackInBuilder(chestItem, true);
                            worker.setStatus(EntityCitizen.Status.WORKING);
                        }
                    }
                    else if (worker.getNavigator().noPath() || !ChunkCoordUtils.isPathingTo(worker, worker.getWorkBuilding().getLocation()))
                    {
                        if (!ChunkCoordUtils.tryMoveLivingToXYZ(worker, worker.getWorkBuilding().getLocation()))
                        {
                            worker.setStatus(EntityCitizen.Status.PATHFINDING_ERROR);
                        }
                        else
                        {
                            worker.setStatus(EntityCitizen.Status.GETTING_ITEMS);
                            return false;
                        }
                    }
                }
                else
                {/*
                    for(Object obj : CraftingManager.getInstance().getRecipeList())
                    {
                        if(obj instanceof ShapelessRecipes)
                        {
                            ShapelessRecipes recipe = (ShapelessRecipes) obj;
                            ItemStack output = recipe.getRecipeOutput();
                            if(!output.isItemEqual(material)) continue;

                            ArrayList<ItemStack> containedItems = new ArrayList<ItemStack>();
                            for(Object obj2 : recipe.recipeItems)
                            {
                                ItemStack recipeItem = (ItemStack) obj2;
                                int slot = InventoryUtils.containsStack(worker.getInventory(), recipeItem);
                                if(!Utils.containsStackInList(recipeItem, containedItems) && slot >= 0)
                                {
                                    int amount = recipeItem.stackSize;
                                    ItemStack invItem = worker.getInventory().getStackInSlot(slot);
                                    if(invItem.isItemEqual(recipeItem))
                                    {
                                        amount -= invItem.stackSize;
                                        if(amount <= 0)
                                        {
                                            containedItems.add(recipeItem);
                                        }
                                    }
                                }
                            }

                            if(recipe.getRecipeSize() == containedItems.size())
                            {
                                for(ItemStack recipeItem : containedItems)
                                {
                                    int amount = recipeItem.stackSize;
                                    while(amount > 0)
                                    {
                                        int itemSlotID = InventoryUtils.containsStack(worker.getInventory(), recipeItem);
                                        amount -= worker.getInventory().getStackInSlot(itemSlotID).stackSize;
                                        worker.getInventory().decrStackSize(itemSlotID, recipeItem.stackSize);
                                    }
                                }
                                setStackInBuilder(output, true);
                                break;
                            }
                        }
                    }*/
                }
                return false;
            }
            else
            {
                job.getSchematic().useMaterial(worker.getInventory().getStackInSlot(slotID));//remove item from materials list (--stackSize)
                worker.getInventory().decrStackSize(slotID, 1);
            }
        }

        if (worldBlock != Blocks.air)//Don't collect air blocks.
        {
            Item itemDropped = worldBlock.getItemDropped(worldBlockMetadata, world.rand, EnchantmentHelper.getFortuneModifier(worker));
            int quantityDropped = worldBlock.quantityDropped(worldBlockMetadata, EnchantmentHelper.getFortuneModifier(worker), world.rand);
            int damageDropped = worldBlock.damageDropped(worldBlockMetadata);
            ItemStack stack = new ItemStack(itemDropped, quantityDropped, damageDropped);//get item for inventory

            if (stack.getItem() != null && stack.stackSize > 0)
            {
                setStackInBuilder(stack, false);
            }
        }
        worker.setStatus(EntityCitizen.Status.WORKING);
        return true;
    }

    private void setStackInBuilder(ItemStack stack, boolean shouldUseForce)
    {
        if (stack != null && stack.getItem() != null && stack.stackSize > 0)
        {
            ItemStack leftOvers = InventoryUtils.setStack(worker.getInventory(), stack);
            if (shouldUseForce && leftOvers != null)
            {
                int slotID = world.rand.nextInt(worker.getInventory().getSizeInventory());
                for (int i = 0; i < worker.getInventory().getSizeInventory(); i++)
                {
                    ItemStack invItem = worker.getInventory().getStackInSlot(i);
                    if (!Utils.containsStackInList(invItem, job.getSchematic().getMaterials()))
                    {
                        leftOvers = invItem;
                        slotID = i;
                        break;
                    }
                }
                worker.getInventory().setInventorySlotContents(slotID, stack);
            }

            if (ChunkCoordUtils.distanceSqrd(worker.getWorkBuilding().getLocation(), worker.getPosition()) < 16)
            {
                TileEntityColonyBuilding tileEntity = worker.getWorkBuilding().getTileEntity();
                if (tileEntity != null)
                {
                    leftOvers = InventoryUtils.setStack(tileEntity, leftOvers);
                }
            }
            /*else
            {
                if(!ChunkCoordUtils.tryMoveLivingToXYZ(worker, worker.getWorkHut().getPosition()))//TODO
                {
                    worker.setStatus(EntityBuilder.Status.PATHFINDING_ERROR);
                }
            }*/

            if (leftOvers != null)
            {
                worker.entityDropItem(leftOvers);
            }
        }
    }

    private boolean placeBlock(int x, int y, int z, Block block, int metadata)
    {
        if (block instanceof BlockDoor)
        {
            ItemDoor.placeDoorBlock(world, x, y, z, metadata, block);
        }
        else if (block instanceof BlockBed && !Utils.testFlag(metadata, 0x8))
        {
            world.setBlock(x, y, z, block, metadata, 0x03);

            int xOffset = 0, zOffset = 0;
            if (metadata == 0)
            {
                zOffset = 1;
            }
            else if (metadata == 1)
            {
                xOffset = -1;
            }
            else if (metadata == 2)
            {
                zOffset = -1;
            }
            else if (metadata == 3)
            {
                xOffset = 1;
            }
            world.setBlock(x + xOffset, y, z + zOffset, block, metadata | 0x8, 0x03);
        }
        else if (block instanceof BlockDoublePlant)
        {
            world.setBlock(x, y, z, block, metadata, 0x03);
            world.setBlock(x, y + 1, z, block, 0x8, 0x03);
        }
        else
        {
            if (!world.setBlock(x, y, z, block, metadata, 0x03))
            {
                return false;
            }
            if (world.getBlock(x, y, z) == block)
            {
                if (world.getBlockMetadata(x, y, z) != metadata)
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
        if (tileEntity != null && !(world.getTileEntity(x, y, z) instanceof TileEntityColonyBuilding))//TODO check if TileEntity already exists
        {
            world.setTileEntity(x, y, z, tileEntity);
        }
    }

    private void loadSchematic()
    {
        WorkOrderBuild workOrder = job.getWorkOrder();
        if (workOrder == null)
        {
            return;
        }

        ChunkCoordinates pos = workOrder.getBuildingId();
        String name = "classic/" + workOrder.getUpgradeName();//TODO actually do styles

        job.setSchematic(Schematic.loadSchematic(world, name));

        if (job.getSchematic() == null)
        {
            MineColonies.logger.warn(String.format("Schematic: (%s) does not exist - removing build request", name));
            worker.getColony().getWorkManager().removeWorkOrder(workOrder);
            return;
        }

        //        workOrder.setClaimedBy(job);
        //        job.setWorkOrderId(workOrder.getID());
        job.getSchematic().setPosition(pos);
    }

    private void completeBuild()
    {
        String schematicName = job.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildComplete", schematicName);

        WorkOrderBuild wo = job.getWorkOrder();
        if (wo != null)
        {
            Building building = job.getColony().getBuilding(wo.getBuildingId());
            if (building != null)
            {
                building.setBuildingLevel(wo.getUpgradeLevel());
            }
            else
            {
                MineColonies.logger.error(String.format("Builder (%d:%d) ERROR - Finished, but missing building(%s)",
                        worker.getColony().getID(), worker.getCitizenData().getId(),
                        wo.getBuildingId()));
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