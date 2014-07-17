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
import com.minecolonies.util.Vec3Utils;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Vec3;
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
public class EntityAIWorkBuilder extends EntityAIBase
{
    private EntityBuilder builder;
    private World         world;
    int messageDelay = 0;

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
        if(!builder.hasSchematic())//is build in progress
        {
            loadSchematic();

            if(!findNextBlock())
            {
                return;
            }
        }
        Vec3 buildPos = builder.getSchematic().getPosition();
        builder.getNavigator().tryMoveToXYZ(buildPos.xCoord, buildPos.yCoord, buildPos.zCoord, 1.0F);

        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageBuildStart", builder.getSchematic().getName());
    }

    @Override
    public void updateTask()
    {
        if(!continueExecuting())
            return;//not called from startExecuting to first check causes repairs to crash if unneeded

        if(builder.getOffsetTicks() % builder.getWorkInterval() == 0)
        {
            messageDelay++;

            builder.setStatus(EntityBuilder.Status.WORKING);

            if(!isBuilderAtSite()) return;

            Block block = builder.getSchematic().getBlock();
            int metadata = builder.getSchematic().getMetadata();

            Vec3 vec = builder.getSchematic().getBlockPosition();
            int x = (int) vec.xCoord, y = (int) vec.yCoord, z = (int) vec.zCoord;

            Block worldBlock = world.getBlock(x, y, z);

            if(block == null)//should never happen
            {
                Vec3 local = builder.getSchematic().getLocalPosition();
                MineColonies.logger.error(LanguageHandler.format("entity.builder.ai.schematicNullBlock", x, y, z, local.xCoord, local.yCoord, local.zCoord));
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
                if(!handleMaterials(block, metadata, worldBlock, world.getBlockMetadata(x, y, z))) return;
            }

            if(block == Blocks.air)
            {
                builder.setCurrentItemOrArmor(0, null);

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
                builder.setCurrentItemOrArmor(0, new ItemStack(block.getItem(world, x, y, z), 1, metadata));

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
            builder.swingItem();//TODO doesn't work, may need item in hand
        }
    }

    private boolean isBuilderAtSite()
    {
        Vec3 buildPos = builder.getSchematic().getPosition();
        if(builder.getPosition().squareDistanceTo(buildPos) > 4)//Too far away
        {
            if(builder.getNavigator().noPath())//Not moving
            {
                if(!builder.getNavigator().tryMoveToXYZ(buildPos.xCoord, buildPos.yCoord, buildPos.zCoord, 1.0F))
                {
                    builder.setStatus(EntityBuilder.Status.PATHFINDING_ERROR);
                }
            }
            return false;
        }
        else
        {
            if(!builder.getNavigator().noPath())//within 2 blocks - can stop pathing //TODO may not need this check
            {
                builder.getNavigator().clearPathEntity();
            }
            return true;
        }
    }

    private boolean findNextBlock()
    {
        if(!builder.getSchematic().findNextBlock())//method returns false if there is no next block (schematic finished)
        {
            completeBuild();
            return false;
        }
        return true;
    }

    private boolean handleMaterials(Block block, int metadata, Block worldBlock, int worldBlockMetadata)
    {
        if(block != Blocks.air)//We are breaking and don't need materials.
        {
            int slotID = builder.getInventory().containsItemStack(new ItemStack(block, 1, metadata));
            if(slotID == -1)//inventory doesn't contain item
            {
                ItemStack material = new ItemStack(block, 1, metadata);

                int amount = -1;
                for(ItemStack item : builder.getSchematic().getMaterials())//find amount needed
                {
                    if(item.isItemEqual(material))
                    {
                        amount = item.stackSize;
                        break;
                    }
                }
                if(amount == -1)
                {
                    System.out.println(block.getLocalizedName());
                }

                int chestSlotID = builder.getWorkHut().containsItemStack(material);
                if(chestSlotID != -1)//chest contains item
                {
                    if(builder.getWorkHut().getDistanceFrom(builder.getPosition()) < 64) //Square Distance - within 8 blocks
                    {
                        builder.getWorkHut().takeItem(builder.getInventory(), chestSlotID, amount);//if chest doesn't contain full amount, take all.
                    }
                    else
                    {
                        if(!builder.getNavigator().tryMoveToXYZ(builder.getWorkHut().xCoord, builder.getWorkHut().yCoord, builder.getWorkHut().zCoord, 1.0D))
                        {
                            builder.setStatus(EntityBuilder.Status.PATHFINDING_ERROR);
                        }
                    }
                }
                else if(false)//TODO canCraft(material)
                {
                    //TODO craft item
                }
                else
                {
                    if(messageDelay % 10 == 0)
                    {
                        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageNeedMaterial", material.getDisplayName(), amount);
                    }
                    builder.setStatus(EntityBuilder.Status.NEED_MATERIALS);
                    //TODO request material - deliveryman
                }
                return false;
            }
            builder.getSchematic().useMaterial(builder.getInventory().getStackInSlot(slotID));//remove item from materials list (--stackSize)
            builder.getInventory().decrStackSize(slotID, 1);
        }

        if(worldBlock != Blocks.air)//Don't collect air blocks.
        {
            ItemStack stack = new ItemStack(Item.getItemFromBlock(worldBlock), 1, worldBlockMetadata);//get item for inventory
            if(stack != null && stack.getItem() != null)
            {
                ItemStack leftOvers = builder.getInventory().setStackInInventory(stack);
                if(leftOvers != null)
                {
                    if(builder.getWorkHut().getDistanceFrom(builder.getPosition()) < 64) //Square Distance - within 8 blocks
                    {
                        ItemStack chestLeftOvers = builder.getWorkHut().setStackInInventory(leftOvers);
                        if(chestLeftOvers != null)
                        {
                            builder.setStatus(EntityBuilder.Status.INVENTORY_FULL);
                            EntityItem itemDrop = new EntityItem(world, builder.posX, builder.posY + 1, builder.posZ, chestLeftOvers);
                            world.spawnEntityInWorld(itemDrop);
                        }
                    }
                    else
                    {
                        if(!builder.getNavigator().tryMoveToXYZ(builder.getWorkHut().xCoord, builder.getWorkHut().yCoord, builder.getWorkHut().zCoord, 1.0D))
                        {
                            builder.setStatus(EntityBuilder.Status.PATHFINDING_ERROR);
                        }
                    }
                }
            }
        }
        return true;
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
            switch(mask(metadata, BUTTON_LEVER_MASK))
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
            switch(mask(metadata, TRAPDOOR_MASK))
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
                if(testFlag(metadata, VINE_EAST) && vineCheck(world.getBlock(x - 1, y, z)))
                {
                    world.setBlock(x - 1, y, z, Blocks.dirt);
                }
                if(testFlag(metadata, VINE_WEST) && vineCheck(world.getBlock(x + 1, y, z)))
                {
                    world.setBlock(x + 1, y, z, Blocks.dirt);
                }
                if(testFlag(metadata, VINE_SOUTH) && vineCheck(world.getBlock(x, y, z - 1)))
                {
                    world.setBlock(x, y, z - 1, Blocks.dirt);
                }
                if(testFlag(metadata, VINE_NORTH) && vineCheck(world.getBlock(x, y, z + 1)))
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
            if(testBlock == Blocks.log && testFlag(testMetadata, 0x3))
            {
                world.setBlock(x + Direction.offsetX[l], y, z + Direction.offsetZ[l], Blocks.log, 3, 0x03);
            }
        }
        else if(block instanceof BlockTripWireHook)
        {
            ForgeDirection direction = ForgeDirection.UNKNOWN;
            switch(mask(metadata, TRIPWIRE_HOOK_MASK))
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

    private boolean testFlag(int data, int flag)
    {
        return (data & flag) == flag;
    }

    private int mask(int data, int mask)
    {
        return data & mask;
    }

    private boolean placeBlock(int x, int y, int z, Block block, int metadata)
    {
        if(block instanceof BlockDoor)
        {
            ItemDoor.placeDoorBlock(world, x, y, z, metadata, block);
        }
        else if(block instanceof BlockBed && !testFlag(metadata, 0x8))
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
        TileEntity tileEntity = builder.getSchematic().getTileEntity();//TODO do we need to load TileEntities when building?
        if(tileEntity != null && !(world.getTileEntity(x, y, z) instanceof TileEntityHut))
        {
            world.setTileEntity(x, y, z, tileEntity);
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return builder.isWorkTime() && builder.hasSchematic();// && (builder.hasMaterials() || Configurations.builderInfiniteResources);
    }

    private void loadSchematic()
    {
        Map.Entry<Vec3, String> entry = builder.getTownHall().getBuilderRequired().entrySet().iterator().next();
        Vec3 pos = entry.getKey();
        String name = entry.getValue();

        builder.setSchematic(Schematic.loadSchematic(world, name));

        if(builder.getSchematic() == null)
        {
            MineColonies.logger.warn(LanguageHandler.format("entity.builder.ai.schematicLoadFailure", name));
            builder.getTownHall().removeHutForUpgrade(pos);
            return;
        }
        builder.getSchematic().setPosition(pos);
    }

    private void completeBuild()
    {
        spawnEntities();//TODO handle materials - would work well in staged building

        String schematicName = builder.getSchematic().getName();
        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageBuildComplete", schematicName);
        Vec3 pos = builder.getSchematic().getPosition();

        if(Vec3Utils.getTileEntityFromVec(world, pos) instanceof TileEntityBuildable)
        {
            int schematicLevel = Integer.parseInt(schematicName.substring(schematicName.length() - 1));

            TileEntityBuildable hut = (TileEntityBuildable) Vec3Utils.getTileEntityFromVec(world, pos);
            hut.setBuildingLevel(schematicLevel);
        }

        builder.setCurrentItemOrArmor(0, null);
        builder.getTownHall().removeHutForUpgrade(pos);
        builder.setSchematic(null);
    }

    private void spawnEntities()
    {
        for(Entity entity : builder.getSchematic().getEntities())
        {
            if(entity != null)
            {
                Vec3 pos = builder.getSchematic().getOffsetPosition();//min position

                if(entity instanceof EntityHanging)
                {
                    EntityHanging entityHanging = (EntityHanging) entity;

                    entityHanging.field_146063_b += pos.xCoord;//tileX
                    entityHanging.field_146064_c += pos.yCoord;//tileY
                    entityHanging.field_146062_d += pos.zCoord;//tileZ
                    entityHanging.setDirection(entityHanging.hangingDirection);//also sets position based on tile

                    entityHanging.setWorld(world);
                    entityHanging.dimension = world.provider.dimensionId;

                    world.spawnEntityInWorld(entityHanging);
                }
                else if(entity instanceof EntityMinecart)
                {
                    EntityMinecart minecart = (EntityMinecart) entity;
                    minecart.riddenByEntity = null;
                    minecart.posX += pos.xCoord;
                    minecart.posY += pos.yCoord;
                    minecart.posZ += pos.zCoord;

                    minecart.setWorld(world);
                    minecart.dimension = world.provider.dimensionId;

                    world.spawnEntityInWorld(minecart);
                }
            }
        }
    }
}