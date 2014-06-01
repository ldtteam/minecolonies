package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.blocks.BlockHut;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.tileentities.TileEntityBuildable;
import com.minecolonies.tileentities.TileEntityHutBuilder;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Arrays;
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
        Vec3 pos = builder.getSchematic().getPosition();
        builder.getNavigator().tryMoveToXYZ(pos.xCoord, pos.yCoord, pos.zCoord, 1.0F);

        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageBuildStart", builder.getSchematic().getName());
    }

    @Override
    public void updateTask()
    {
        if(!builder.getNavigator().noPath()) return;//traveling

        if(builder.getOffsetTicks() % builder.getWorkInterval() == 0)
        {
            if(!builder.getSchematic().findNextBlockIncludingAir())//method returns false if there is no next block (schematic finished)
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
            int[] pos = Utils.vecToInt(builder.getSchematic().getBlockPosition());

            Block worldBlock = world.getBlock(pos[0], pos[1], pos[2]);
            if(worldBlock instanceof BlockHut || worldBlock == Blocks.bedrock) return;

            if(!Configurations.builderInfiniteResources)
            {
                int slotID = builder.getInventory().containsItemStack(new ItemStack(block, 1, metadata));
                if(slotID == -1) return;//TODO getMaterials - check chest, then request
                builder.getSchematic().useMaterial(builder.getInventory().getStackInSlot(slotID));
                builder.getInventory().decrStackSize(slotID, 1);

                ItemStack stack = worldBlock.getPickBlock(null, world, pos[0], pos[1], pos[2]);
                builder.getInventory().setStackInInventory(stack);
            }

            if(block == Blocks.air)
            {
                builder.swingItem();//TODO doesn't work, may need item in hand
                world.setBlockToAir(pos[0], pos[1], pos[2]);
            }
            else
            {
                if(!block.getMaterial().isSolid())
                {
                    //TODO support block or add delay system
                    if (block == Blocks.torch) {
                        if (metadata == 1 && world.getBlock(pos[0] - 1, pos[1], pos[2]) == Blocks.air) {
                            world.setBlock(pos[0] - 1, pos[1], pos[2], Blocks.dirt);
                        } else if (metadata == 2 && world.getBlock(pos[0] + 1, pos[1], pos[2]) == Blocks.air) {
                            world.setBlock(pos[0] + 1, pos[1], pos[2], Blocks.dirt);
                        } else if (metadata == 3 && world.getBlock(pos[0], pos[1], pos[2] - 1) == Blocks.air) {
                            world.setBlock(pos[0], pos[1], pos[2] - 1, Blocks.dirt);
                        } else if (metadata == 4 && world.getBlock(pos[0], pos[1], pos[2] + 1) == Blocks.air) {
                            world.setBlock(pos[0], pos[1], pos[2] + 1, Blocks.dirt);
                        }
                    }
                }
                builder.swingItem();
                world.setBlock(pos[0], pos[1], pos[2], block, metadata, 0x02);
                if(builder.getSchematic().getTileEntity() != null)
                {
                    world.setTileEntity(pos[0], pos[1], pos[2], builder.getSchematic().getTileEntity());
                }
            }
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return builder.isWorkTime() && builder.hasSchematic() && (builder.hasMaterials() || Configurations.builderInfiniteResources);
    }

    private void loadSchematic()
    {
        Map.Entry<int[], String> entry = builder.getTownHall().getBuilderRequired().entrySet().iterator().next();
        builder.setSchematic(Schematic.loadSchematic(world, entry.getValue()));
        int[] pos = entry.getKey();
        builder.getSchematic().setPosition(Vec3.createVectorHelper(pos[0], pos[1], pos[2]));
    }

    private void completeBuild()
    {
        LanguageHandler.sendPlayersLocalizedMessage(Utils.getPlayersFromUUID(world, builder.getTownHall().getOwners()), "entity.builder.messageBuildComplete", builder.getSchematic().getName());
        int[] pos = Utils.vecToInt(builder.getSchematic().getPosition());
        builder.getTownHall().removeHutForUpgrade(pos);
        builder.setSchematic(null);

        if(world.getTileEntity(pos[0], pos[1], pos[2]) instanceof TileEntityBuildable)
        {
            TileEntityBuildable hut = (TileEntityBuildable) world.getTileEntity(pos[0], pos[1], pos[2]);
            hut.setBuildingLevel(hut.getBuildingLevel() + 1);
        }
    }
}