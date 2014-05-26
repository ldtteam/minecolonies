package com.minecolonies.entity.ai;

import com.minecolonies.MineColonies;
import com.minecolonies.blocks.BlockHut;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityBuilder;
import com.minecolonies.util.InventoryHelper;
import com.minecolonies.util.Schematic;
import com.minecolonies.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

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
                int slotID = InventoryHelper.doesInventoryContainItemStack(builder.getInventory(), new ItemStack(block, 1, metadata));
                if(slotID == -1) return;//TODO getMaterials
                builder.getInventory().decrStackSize(slotID, 1);

                ItemStack stack = worldBlock.getPickBlock(null, world, pos[0], pos[1], pos[2]);
                InventoryHelper.setStackInInventory(builder.getInventory(), stack);
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
                }
                builder.swingItem();
                world.setBlock(pos[0], pos[1], pos[2], block, metadata, 0x02);
            }
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return this.shouldExecute() && (builder.hasMaterials() || Configurations.builderInfiniteResources);//TODO finish hasMaterials, look above for ideas
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
        int[] toMatch = Utils.vecToInt(builder.getSchematic().getPosition());
        for(int[] key : builder.getTownHall().getBuilderRequired().keySet())
        {
            if(key[0] == toMatch[0] && key[1] == toMatch[1] && key[2] == toMatch[2])
            {
                builder.getTownHall().removeHutForUpgrade(key);
                builder.setSchematic(null);
            }
        }
    }
}