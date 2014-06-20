package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.blocks.BlockHut;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.tileentities.TileEntityBuildable;
import com.minecolonies.tileentities.TileEntityHut;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import com.minecolonies.util.Utils;
import net.minecraft.block.*;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

/**
 * Performs builder work
 * Created: May 25, 2014
 *
 * @author Colton
 */
public class EntityAIWorkBuilder extends EntityAIBase
{
    private EntityBuilder builder;
    private World         world;

    public EntityAIWorkBuilder(EntityBuilder builder)
    {
        setMutexBits(3);
        this.builder = builder;
        this.world = builder.worldObj;
    }

    @Override
    public boolean shouldExecute()
    {
        return builder.isWorkTime() && (builder.hasSchematic() || builder.isBuilderNeeded());
    }

    @Override
    public void startExecuting()
    {
        if(!builder.hasSchematic())
        {
            loadSchematic();
        }
        Vec3 buildPos = builder.getSchematic().getPosition();
        builder.getNavigator().tryMoveToXYZ(buildPos.xCoord, buildPos.yCoord, buildPos.zCoord, 1.0F);

        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageBuildStart", builder.getSchematic().getName());
    }

    @Override
    public void updateTask()
    {
        //TODO: Need to do more in range and pathfind fail checks
        if(!builder.getNavigator().noPath())//traveling
        {
            if(builder.getNavigator().getPath().getFinalPathPoint().distanceToSquared(new PathPoint((int) builder.posX, (int) builder.posY, (int) builder.posZ)) < 4)//within 2 blocks
            {
                builder.getNavigator().clearPathEntity();
            }
            return;
        }

        if(builder.getOffsetTicks() % builder.getWorkInterval() == 0)
        {
            if(!builder.getSchematic().findNextBlock())//method returns false if there is no next block (schematic finished)
            {
                completeBuild();
                return;
            }

            Block block = builder.getSchematic().getBlock();
            if(block == null)//should never happen
            {
                MineColonies.logger.error("Schematic has null block");
                return;
            }
            int metadata = builder.getSchematic().getMetadata();
            Vec3 vec = builder.getSchematic().getBlockPosition();
            int x = (int) vec.xCoord;
            int y = (int) vec.yCoord;
            int z = (int) vec.zCoord;

            Block worldBlock = world.getBlock(x, y, z);
            if(worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock) return;//don't overwrite huts or bedrock

            if(!Configurations.builderInfiniteResources)//We need to deal with materials
            {
                int slotID = builder.getInventory().containsItemStack(new ItemStack(block, 1, metadata));
                if(slotID == -1)
                {
                    ItemStack material = new ItemStack(block, 1, metadata);

                    int amount = -1;
                    for(ItemStack item : builder.getSchematic().getMaterials())
                    {
                        if(item.isItemEqual(material))
                        {
                            amount = item.stackSize;
                            break;
                        }
                    }

                    int chestSlotID = builder.getWorkHut().containsItemStack(material);
                    if(chestSlotID != -1)
                    {
                        if(builder.getWorkHut().getDistanceFrom(builder.posX, builder.posY, builder.posZ) < 64) //Square Distance
                        {
                            builder.getWorkHut().takeItem(builder.getInventory(), chestSlotID, amount);
                        }
                        else
                        {
                            builder.getNavigator().tryMoveToXYZ(builder.getWorkHut().xCoord, builder.getWorkHut().yCoord, builder.getWorkHut().zCoord, 1.0D);
                        }
                    }
                    else if(false)//TODO canCraft()
                    {
                        //TODO craft item
                    }
                    else
                    {
                        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageNeedMaterial", material.getDisplayName(), amount);
                        //TODO request material - deliveryman
                    }
                    return;
                }
                builder.getSchematic().useMaterial(builder.getInventory().getStackInSlot(slotID));
                builder.getInventory().decrStackSize(slotID, 1);

                ItemStack stack = worldBlock.getPickBlock(null, world, x, y, z);
                builder.getInventory().setStackInInventory(stack);
                //TODO unload full inventory
            }

            if(block == Blocks.air)
            {
                world.setBlockToAir(x, y, z);
            }
            else
            {
                //TODO create proper system
                placeRequiredSupportingBlocks(world, x, y, z, block, metadata);

                if(block instanceof BlockDoor)
                {
                    ItemDoor.placeDoorBlock(world, x, y, z, metadata, block);
                }
                else if(block instanceof BlockBed && !testFlag(metadata, 8))
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
                    world.setBlock(x + xOffset, y, z + zOffset, block, metadata + 8, 0x03);
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
                        return;
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

                TileEntity tileEntity = builder.getSchematic().getTileEntity();//TODO do we need to load TileEntities when building?
                if(tileEntity != null && !(world.getTileEntity(x, y, z) instanceof TileEntityHut))
                {
                    world.setTileEntity(x, y, z, tileEntity);
                }
            }
            builder.swingItem();//TODO doesn't work, may need item in hand
        }
    }

    private void placeRequiredSupportingBlocks(World world, int x, int y, int z, Block block, int metadata)
    {
        if(block instanceof BlockTorch || block instanceof BlockLever || block instanceof BlockButton)
        {
            if(testMask(metadata, 7, 0) && !(block instanceof BlockTorch) && !world.isSideSolid(x, y + 1, z, ForgeDirection.DOWN, true))
            {
                world.setBlock(x, y + 1, z, Blocks.dirt);
            }
            else if(testMask(metadata, 7, 1) && !world.isSideSolid(x - 1, y, z, ForgeDirection.EAST, true))
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(testMask(metadata, 7, 2) && !world.isSideSolid(x + 1, y, z, ForgeDirection.WEST, true))
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(testMask(metadata, 7, 3) && !world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH, true))
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(testMask(metadata, 7, 4) && !world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH, true))
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
        else if(block instanceof BlockLadder)
        {
            if(metadata == 5 && !world.isSideSolid(x - 1, y, z, ForgeDirection.EAST, true))
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(metadata == 4 && !world.isSideSolid(x + 1, y, z, ForgeDirection.WEST, true))
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(metadata == 3 && !world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH, true))
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(metadata == 2 && !world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH, true))
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
        else if(block instanceof BlockSign)
        {
            if(metadata == 5 && !world.getBlock(x - 1, y, z).getMaterial().isSolid())
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(metadata == 4 && !world.getBlock(x + 1, y, z).getMaterial().isSolid())
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(metadata == 3 && !world.getBlock(x, y, z - 1).getMaterial().isSolid())
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(metadata == 2 && !world.getBlock(x, y, z + 1).getMaterial().isSolid())
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
        else if(block instanceof BlockTrapDoor)
        {
            if(testMask(metadata, 3, 3) && !(trapDoorCheck(world.getBlock(x - 1, y, z)) || world.isSideSolid(x - 1, y, z, ForgeDirection.UP)))
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(testMask(metadata, 3, 2) && !(trapDoorCheck(world.getBlock(x + 1, y, z)) || world.isSideSolid(x + 1, y, z, ForgeDirection.UP)))
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(testMask(metadata, 3, 1) && !(trapDoorCheck(world.getBlock(x, y, z - 1)) || world.isSideSolid(x, y, z - 1, ForgeDirection.UP)))
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(testMask(metadata, 3, 0) && !(trapDoorCheck(world.getBlock(x, y, z + 1)) || world.isSideSolid(x, y, z + 1, ForgeDirection.UP)))
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
        else if(block instanceof BlockVine)
        {
            if(testFlag(metadata, 8) && !vineCheck(world.getBlock(x - 1, y, z)))
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(testFlag(metadata, 2) && !vineCheck(world.getBlock(x + 1, y, z)))
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(testFlag(metadata, 1) && !vineCheck(world.getBlock(x, y, z - 1)))
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(testFlag(metadata, 4) && !vineCheck(world.getBlock(x, y, z + 1)))
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
        else if(block instanceof BlockCocoa)
        {
            int l = BlockDirectional.getDirection(metadata);
            Block testBlock = world.getBlock(x + Direction.offsetX[l], y, z + Direction.offsetZ[l]);
            int testMetadata = world.getBlockMetadata(x + Direction.offsetX[l], y, z + Direction.offsetZ[l]);
            if(testBlock == Blocks.log && testFlag(testMetadata, 3))
            {
                world.setBlock(x + Direction.offsetX[l], y, z + Direction.offsetZ[l], Blocks.log, 3, 0x03);
            }
        }
        else if(block instanceof BlockTripWireHook)
        {
            if(testMask(metadata, 3, 3) && !world.isSideSolid(x - 1, y, z, ForgeDirection.EAST, true))
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(testMask(metadata, 3, 1) && !world.isSideSolid(x + 1, y, z, ForgeDirection.WEST, true))
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(testMask(metadata, 3, 0) && !world.isSideSolid(x, y, z - 1, ForgeDirection.SOUTH, true))
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(testMask(metadata, 3, 2) && !world.isSideSolid(x, y, z + 1, ForgeDirection.NORTH, true))
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
        else if(block instanceof BlockSkull)
        {
            if(metadata == 4 && !world.getBlock(x - 1, y, z).getMaterial().isSolid())
            {
                world.setBlock(x - 1, y, z, Blocks.dirt);
            }
            else if(metadata == 5 && !world.getBlock(x + 1, y, z).getMaterial().isSolid())
            {
                world.setBlock(x + 1, y, z, Blocks.dirt);
            }
            else if(metadata == 3 && !world.getBlock(x, y, z - 1).getMaterial().isSolid())
            {
                world.setBlock(x, y, z - 1, Blocks.dirt);
            }
            else if(metadata == 2 && !world.getBlock(x, y, z + 1).getMaterial().isSolid())
            {
                world.setBlock(x, y, z + 1, Blocks.dirt);
            }
        }
    }

    private boolean vineCheck(Block block)
    {
        return block.renderAsNormalBlock() && block.getMaterial().blocksMovement();
    }

    private boolean trapDoorCheck(Block block)
    {
        return (block.getMaterial().isOpaque() && block.renderAsNormalBlock() || block == Blocks.glowstone || block instanceof BlockSlab || block instanceof BlockStairs);
    }

    private boolean testFlag(int data, int flag)
    {
        return (data & flag) == flag;
    }

    private boolean testMask(int data, int mask, int id)
    {
        return (data & mask) == id;
    }

    @Override
    public boolean continueExecuting()
    {
        return builder.isWorkTime() && builder.hasSchematic() && (builder.hasMaterials() || Configurations.builderInfiniteResources);
    }

    private void loadSchematic()
    {
        Map.Entry<int[], String> entry = builder.getTownHall().getBuilderRequired().entrySet().iterator().next();
        int[] pos = entry.getKey();
        String name = entry.getValue();

        builder.setSchematic(Schematic.loadSchematic(world, name));

        if(builder.getSchematic() == null)
        {
            MineColonies.logger.warn(LanguageHandler.format("entity.builder.ai.schematicLoadFailure", name));
            builder.getTownHall().removeHutForUpgrade(pos);
            return;
        }
        builder.getSchematic().setPosition(Vec3.createVectorHelper(pos[0], pos[1], pos[2]));
    }

    private void completeBuild()
    {
        String schematicName = builder.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageBuildComplete", schematicName);
        int[] pos = Utils.vecToInt(builder.getSchematic().getPosition());

        if(world.getTileEntity(pos[0], pos[1], pos[2]) instanceof TileEntityBuildable)
        {
            int schematicLevel = Integer.parseInt(schematicName.substring(schematicName.length() - 1));

            TileEntityBuildable hut = (TileEntityBuildable) world.getTileEntity(pos[0], pos[1], pos[2]);
            hut.setBuildingLevel(schematicLevel);
        }

        builder.getTownHall().removeHutForUpgrade(pos);
        builder.setSchematic(null);
    }
}