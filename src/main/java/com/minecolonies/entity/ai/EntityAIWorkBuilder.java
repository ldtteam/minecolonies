package com.minecolonies.entity.ai;

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
        return !world.isRaining() && world.isDaytime() && (builder.hasSchematic() || (builder.getTownHall() != null && !builder.getTownHall().getBuilderRequired().isEmpty()));
    }

    @Override
    public void startExecuting()
    {
        if(!builder.hasSchematic())
        {
            Map.Entry<int[], String> entry = builder.getTownHall().getBuilderRequired().entrySet().iterator().next();
            builder.setSchematic(Schematic.loadSchematic(world, entry.getValue()));
            int[] coords = entry.getKey();
            builder.getSchematic().setPosition(Vec3.createVectorHelper(coords[0], coords[1], coords[2]));
        }
    }

    @Override
    public void updateTask()
    {
        if (builder.getOffsetTicks() % builder.getWorkInterval() == 0)
        {
            if(!builder.getSchematic().findNextBlockIncludingAir()) completeBuild();

            Block block = builder.getSchematic().getBlock();
            int metadata = builder.getSchematic().getMetadata();
            int[] coords = Utils.vecToInt(builder.getSchematic().getBlockPosition());

            Block worldBlock = world.getBlock(coords[0], coords[1], coords[2]);
            ItemStack stack = worldBlock.getPickBlock(null, world, coords[0], coords[1], coords[2]);
            InventoryHelper.setStackInInventory(builder.getInventory(), stack);

            if(block == Blocks.air)
            {
                world.setBlockToAir(coords[0], coords[1], coords[2]);
            }
            else
            {
                if (!block.getMaterial().isSolid())
                {
                    //TODO support block or add delay system
                }
                world.setBlock(coords[0], coords[1], coords[2], block, metadata, 0x02);
            }
        }
    }

    @Override
    public boolean continueExecuting()
    {
        return this.shouldExecute() && builder.hasMaterials();
    }

    public void completeBuild()
    {
        builder.getTownHall().getBuilderRequired().remove(Utils.vecToInt(builder.getSchematic().getPosition()));
    }
}