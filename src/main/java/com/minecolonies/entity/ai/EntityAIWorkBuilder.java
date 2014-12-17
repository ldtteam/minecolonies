package com.minecolonies.entity.ai;

import com.github.lunatrius.schematica.config.BlockInfo;
import com.minecolonies.MineColonies;
import com.minecolonies.blocks.BlockHut;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.entity.jobs.JobBuilder;
import com.minecolonies.tileentities.TileEntityColonyBuilding;
import com.minecolonies.util.*;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.*;
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
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

import static com.minecolonies.lib.Constants.BlockData.*;

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
        return super.shouldExecute() && (job.hasSchematic() || job.isNeeded());
    }

    @Override
    public void startExecuting()
    {
        if(!job.hasSchematic())//is build in progress
        {
            loadSchematic();

            if(!job.hasSchematic() || !findNextBlock())
            {
                return;
            }

            LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildStart", job.getSchematic().getName());
        }
        ChunkCoordUtils.tryMoveLivingToXYZ(worker, job.getSchematic().getPosition());
        if(!Configurations.builderInfiniteResources)
        {
            requestMaterials();
        }

        worker.setStatus(EntityCitizen.Status.WORKING);
    }

    @Override
    public void updateTask()
    {
        if(worker.getOffsetTicks() % job.getWorkInterval() == 0)
        {
            if(!job.hasSchematic())
                return;//Fixes crash caused by buildings needing no repairs

            if(job.getSchematic().doesSchematicBlockEqualWorldBlock())
            {
                findNextBlock();
                return;//findNextBlock count was reached and we can ignore this block
            }

            System.out.println(worker.getStatus().toString());

            if(worker.getStatus() != EntityCitizen.Status.GETTING_ITEMS) {
			    if(!ChunkCoordUtils.isWorkerAtSiteWithMove(worker, job.getSchematic().getPosition()))
				{
                    return;
				}
                worker.setStatus(EntityCitizen.Status.WORKING);
			}

            Block block = job.getSchematic().getBlock();
            int metadata = job.getSchematic().getMetadata();

            ChunkCoordinates coords = job.getSchematic().getBlockPosition();
            int x = coords.posX, y = coords.posY, z = coords.posZ;

            Block worldBlock = world.getBlock(x, y, z);
            int worldBlockMetadata = world.getBlockMetadata(x, y, z);

            if(block == null)//should never happen
            {
                ChunkCoordinates local = job.getSchematic().getLocalPosition();
                MineColonies.logger.error(LanguageHandler.format("entity.builder.ai.schematicNullBlock", x, y, z, local.posX, local.posY, local.posZ));
                findNextBlock();
                return;
            }
            if(worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock)//don't overwrite huts or bedrock
            {
                findNextBlock();
                return;
            }

            if(!Configurations.builderInfiniteResources)//We need to deal with materials
            {
                if(!handleMaterials(block, metadata, worldBlock, worldBlockMetadata)) return;
            }

            if(block == Blocks.air)
            {
                worker.setCurrentItemOrArmor(0, null);

                if(world.setBlockToAir(x, y, z))
                {
                    findNextBlock();
                }
                else
                {
                    MineColonies.logger.error(LanguageHandler.format("entity.builder.ai.blockBreakFailure", x, y, z));
                    findNextBlock();//TODO handle - for now, just skipping
                }
            }
            else
            {
                worker.setCurrentItemOrArmor(0, new ItemStack(block.getItem(world, x, y, z), 1, metadata));

                placeRequiredSupportingBlocks(x, y, z, block, metadata);

                if(placeBlock(x, y, z, block, metadata))
                {
                    setTileEntity(x, y, z);
                    findNextBlock();
                }
                else
                {
                    MineColonies.logger.error(LanguageHandler.format("entity.builder.ai.blockPlaceFailure", block.getUnlocalizedName(), x, y, z));
                    findNextBlock();//TODO handle - for now, just skipping
                }
            }
            worker.swingItem();//TODO doesn't work, may need item in hand
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return super.continueExecuting() && job.hasSchematic();
    }

    @Override
    public void resetTask()
    {
        super.resetTask();
        worker.setCurrentItemOrArmor(0, null);
    }

    private boolean findNextBlock()
    {
        if(!job.getSchematic().findNextBlock())//method returns false if there is no next block (schematic finished)
        {
            completeBuild();
            return false;
        }
        return true;
    }

    private void requestMaterials()
    {
        Schematic schematic = Schematic.loadSchematic(world, job.getSchematic().getName());
        schematic.setPosition(job.getSchematic().getPosition());
        boolean placesBlock = false;

        while(schematic.findNextBlock())
        {
            Block block = schematic.getBlock();
            int metadata = schematic.getMetadata();
            ItemStack itemstack = new ItemStack(block, 1, metadata);

            ChunkCoordinates pos = schematic.getBlockPosition();

            Block worldBlock = world.getBlock(pos.posX, pos.posY, pos.posZ);

            if(itemstack.getItem() == null || block == null || block == Blocks.air || worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock)
            {
                continue;
            }

            placesBlock = true;

            /*for(ItemStack material : builder.getSchematic().getMaterials())
            {
                if(material.isItemEqual(itemstack))
                {
                    if(material.stackSize > 0)
                    {*/
                        if(InventoryUtils.containsStack(worker.getInventory(), itemstack) == -1)
                        {
                            job.addItemNeeded(itemstack);
                        }
                    /*}
                    break;
                }
            }*/
        }

        if(placesBlock)
        {
            for(ItemStack neededItem : job.getItemsNeeded())
            {
                LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageNeedMaterial", neededItem.getDisplayName(), neededItem.stackSize);
            }
        }
    }

    private boolean handleMaterials(Block block, int metadata, Block worldBlock, int worldBlockMetadata)
    {
        TileEntityColonyBuilding workBuildingTileEntity = worker.getWorkBuilding().getTileEntity();

        System.out.println(FMLCommonHandler.instance().getSide().toString() + " : " + FMLCommonHandler.instance().getEffectiveSide().toString());
        if(block != Blocks.air)
        {
            System.out.println(block.getUnlocalizedName());

            if(Utils.isWater(block) || block == Blocks.leaves || block == Blocks.leaves2 || (block == Blocks.double_plant && Utils.testFlag(metadata, 0x08)) || (block instanceof BlockDoor && Utils.testFlag(metadata, 0x08))) return true;//free blocks

            Item item = BlockInfo.getItemFromBlock(block);
            if(BlockInfo.BLOCK_LIST_IGNORE_METADATA.contains(block))
            {
                metadata = 0;
            } else if(block == Blocks.log || block == Blocks.log2 || block == Blocks.wooden_slab)//will probably need more in the future, will fix as I come across them
            {
                metadata %= 4;
            } else if(block == Blocks.stone_slab)
            {
                metadata %= 8;
            }

            ItemStack material = new ItemStack(item, 1, metadata);
            System.out.println(material.getItem().getUnlocalizedName() + " : " + material.getItemDamage());

            int slotID = InventoryUtils.containsStack(worker.getInventory(), material);
            if(slotID == -1)//inventory doesn't contain item
            {
                if (workBuildingTileEntity == null)
                {
                    //  Work Building is not loaded
                    return false;
                }

                int chestSlotID = InventoryUtils.containsStack(workBuildingTileEntity, material);
                if(chestSlotID != -1)//chest contains item
                {
                    if(ChunkCoordUtils.distanceSqrd(worker.getWorkBuilding().getLocation(), worker.getPosition()) < 16)
                    {
                        if(!InventoryUtils.takeStackInSlot(workBuildingTileEntity, worker.getInventory(), chestSlotID, 1, true))
                        {
                            ItemStack chestItem = workBuildingTileEntity.getStackInSlot(chestSlotID);
                            workBuildingTileEntity.setInventorySlotContents(chestSlotID, null);
                            setStackInBuilder(chestItem, true);
                            worker.setStatus(EntityCitizen.Status.WORKING);
                        }
                    }
                    else if(worker.getNavigator().noPath() || !ChunkCoordUtils.isPathingTo(worker, worker.getWorkBuilding().getLocation()))
                    {
                        if(!ChunkCoordUtils.tryMoveLivingToXYZ(worker, worker.getWorkBuilding().getLocation()))
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

        if(worldBlock != Blocks.air)//Don't collect air blocks.
        {
            Item itemDropped = worldBlock.getItemDropped(worldBlockMetadata, world.rand, EnchantmentHelper.getFortuneModifier(worker));
            int quantityDropped = worldBlock.quantityDropped(worldBlockMetadata, EnchantmentHelper.getFortuneModifier(worker), world.rand);
            int damageDropped = worldBlock.damageDropped(worldBlockMetadata);
            ItemStack stack = new ItemStack(itemDropped, quantityDropped, damageDropped);//get item for inventory

            if(stack.getItem() != null && stack.stackSize > 0)
            {
                setStackInBuilder(stack, false);
            }
        }
        worker.setStatus(EntityCitizen.Status.WORKING);
        return true;
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
                    if(!Utils.containsStackInList(invItem, job.getSchematic().getMaterials()))
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

            if(leftOvers != null)
            {
                worker.entityDropItem(leftOvers);
            }
        }
    }

    private boolean isSupportNeeded(World world, int x, int y, int z, ForgeDirection direction)
    {
        return !world.isSideSolid(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, direction, true);
    }

    private void placeRequiredSupportingBlocks(int x, int y, int z, Block block, int metadata)
    {
        if(block instanceof BlockTorch)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(metadata)
            {
                case TORCH_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case TORCH_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case TORCH_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case TORCH_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && isSupportNeeded(world, x, y, z, direction))
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
        else if(block instanceof BlockLever || block instanceof BlockButton)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(Utils.mask(metadata, BUTTON_LEVER_MASK))
            {
                case BUTTON_LEVER_CEILING:
                    direction = ForgeDirection.DOWN;
                    break;
                case BUTTON_LEVER_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case BUTTON_LEVER_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case BUTTON_LEVER_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case BUTTON_LEVER_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && isSupportNeeded(world, x, y, z, direction))
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
        else if(block instanceof BlockLadder)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(metadata)
            {
                case LADDER_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case LADDER_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case LADDER_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case LADDER_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && isSupportNeeded(world, x, y, z, direction))
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
        else if(block instanceof BlockSign)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(metadata)
            {
                case SIGN_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case SIGN_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case SIGN_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case SIGN_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && !world.getBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ).getMaterial().isSolid())
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
        else if(block instanceof BlockTrapDoor)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(Utils.mask(metadata, TRAPDOOR_MASK))
            {
                case TRAPDOOR_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case TRAPDOOR_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case TRAPDOOR_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case TRAPDOOR_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && !(trapDoorCheck(world.getBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ)) || world.isSideSolid(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, ForgeDirection.UP)))
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
        else if(block instanceof BlockVine)
        {
            if(metadata == 0 && vineCheck(world.getBlock(x, y + 1, z)))
            {
                world.setBlock(x, y + 1, z, Blocks.dirt);
            }
            else
            {
                if(Utils.testFlag(metadata, VINE_EAST) && vineCheck(world.getBlock(x - 1, y, z)))
                {
                    world.setBlock(x - 1, y, z, Blocks.dirt);
                }
                if(Utils.testFlag(metadata, VINE_WEST) && vineCheck(world.getBlock(x + 1, y, z)))
                {
                    world.setBlock(x + 1, y, z, Blocks.dirt);
                }
                if(Utils.testFlag(metadata, VINE_SOUTH) && vineCheck(world.getBlock(x, y, z - 1)))
                {
                    world.setBlock(x, y, z - 1, Blocks.dirt);
                }
                if(Utils.testFlag(metadata, VINE_NORTH) && vineCheck(world.getBlock(x, y, z + 1)))
                {
                    world.setBlock(x, y, z + 1, Blocks.dirt);
                }
            }
        }
        else if(block instanceof BlockCocoa)
        {
            int l = BlockDirectional.getDirection(metadata);
            Block testBlock = world.getBlock(x + Direction.offsetX[l], y, z + Direction.offsetZ[l]);
            int testMetadata = world.getBlockMetadata(x + Direction.offsetX[l], y, z + Direction.offsetZ[l]);
            if(testBlock == Blocks.log && Utils.testFlag(testMetadata, 0x3))
            {
                world.setBlock(x + Direction.offsetX[l], y, z + Direction.offsetZ[l], Blocks.log, 3, 0x03);
            }
        }
        else if(block instanceof BlockTripWireHook)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(Utils.mask(metadata, TRIPWIRE_HOOK_MASK))
            {
                case LADDER_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case LADDER_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case LADDER_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case LADDER_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && isSupportNeeded(world, x, y, z, direction))
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
        else if(block instanceof BlockSkull)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(metadata)
            {
                case SKULL_EAST:
                    direction = ForgeDirection.EAST;
                    break;
                case SKULL_WEST:
                    direction = ForgeDirection.WEST;
                    break;
                case SKULL_SOUTH:
                    direction = ForgeDirection.SOUTH;
                    break;
                case SKULL_NORTH:
                    direction = ForgeDirection.NORTH;
            }
            if(direction != ForgeDirection.UNKNOWN && !world.getBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ).getMaterial().isSolid())
            {
                world.setBlock(x - direction.offsetX, y - direction.offsetY, z - direction.offsetZ, Blocks.dirt);
            }
        }
    }

    private boolean vineCheck(Block block)
    {
        return !(block.renderAsNormalBlock() && block.getMaterial().blocksMovement());
    }

    private boolean trapDoorCheck(Block block)
    {
        return (block.getMaterial().isOpaque() && block.renderAsNormalBlock() || block == Blocks.glowstone || block instanceof BlockSlab || block instanceof BlockStairs);
    }

    private boolean placeBlock(int x, int y, int z, Block block, int metadata)
    {
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
        if(tileEntity != null && !(world.getTileEntity(x, y, z) instanceof TileEntityColonyBuilding))//TODO check if TileEntity already exists
        {
            world.setTileEntity(x, y, z, tileEntity);
        }
    }

    private void loadSchematic()
    {
        Map.Entry<ChunkCoordinates, String> entry = worker.getColony().getBuildingUpgrades().entrySet().iterator().next();
        ChunkCoordinates pos = entry.getKey();
        String name = entry.getValue();

        job.setSchematic(Schematic.loadSchematic(world, name));

        if(job.getSchematic() == null)
        {
            MineColonies.logger.warn(LanguageHandler.format("entity.builder.ai.schematicLoadFailure", name));
            worker.getColony().removeBuildingForUpgrade(pos);
            return;
        }
        job.getSchematic().setPosition(pos);
    }

    private void completeBuild()
    {
        spawnEntities();//TODO handle materials - would work well in staged building

        String schematicName = job.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, worker.getColony().getPermissions().getMessagePlayers()), "entity.builder.messageBuildComplete", schematicName);
        ChunkCoordinates pos = job.getSchematic().getPosition();

        if(ChunkCoordUtils.getTileEntity(world, pos) instanceof TileEntityColonyBuilding)
        {
            int schematicLevel = Integer.parseInt(schematicName.substring(schematicName.length() - 1));

            TileEntityColonyBuilding hut = (TileEntityColonyBuilding) ChunkCoordUtils.getTileEntity(world, pos);
            hut.getBuilding().setBuildingLevel(schematicLevel);
        }

        worker.getColony().removeBuildingForUpgrade(pos);
        job.setSchematic(null);
        resetTask();
    }

    private void spawnEntities()
    {
        for(Entity entity : job.getSchematic().getEntities())
        {
            if(entity != null)
            {
                ChunkCoordinates pos = job.getSchematic().getOffsetPosition();//min position

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
    }
}