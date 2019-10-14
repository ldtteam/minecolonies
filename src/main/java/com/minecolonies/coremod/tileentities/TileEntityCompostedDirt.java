package com.minecolonies.coremod.tileentities;

import com.ldtteam.structurize.util.BlockUtils;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBlockSpecial;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

import static com.minecolonies.api.util.constant.Constants.TICKS_SECOND;

/**
 * The composted dirty tileEntity to grow all kinds of flowers.
 */
public class TileEntityCompostedDirt extends TileEntity implements ITickable
{
    /**
     * If currently composted.
     */
    private boolean composted = false;

    /**
     * The current tick timer.
     */
    private int ticker = 0;

    /**
     * Chance to grow something (per second).
     */
    private double percentage = 1.0D;

    /**
     * Max tick limit.
     */
    private final static int TICKER_LIMIT = 300;

    /**
     * Random tick.
     */
    private final Random random = new Random();

    /**
     * The flower to grow.
     */
    private ItemStack flower;

    @Override
    public void update()
    {
        final World world = this.getWorld();
        if(!world.isRemote && this.composted && ticker % TICKS_SECOND == 0)
        {
            this.updateTick(world);
        }
        ticker++;
    }

    /**
     * Update tick running on the server world.
     * @param worldIn the server world.
     */
    private void updateTick(@NotNull final World worldIn)
    {
        if ( flower == null || flower.isEmpty())
        {
            this.composted = false;
            return;
        }

        if(this.composted)
        {
            ((WorldServer)worldIn).spawnParticle(
              EnumParticleTypes.VILLAGER_HAPPY, this.getPos().getX()+0.5,
              this.getPos().getY()+1, this.getPos().getZ()+0.5,
              1, 0.2, 0, 0.2, 0);
        }

        if(random.nextDouble() * 100 <= this.percentage)
        {
            final BlockPos position = pos.up();
            if(worldIn.getBlockState(position).getBlock()== Blocks.AIR)
            {
                if (flower.getItem() instanceof ItemSeeds)
                {
                    worldIn.setBlockState(position, ((ItemSeeds) flower.getItem()).getPlant(world, position));
                }
                else if (flower.getItem() instanceof ItemBlock)
                {
                    if (((ItemBlock) flower.getItem()).getBlock() instanceof BlockDoublePlant)
                    {
                        ((BlockDoublePlant) ((ItemBlock) flower.getItem()).getBlock()).placeAt(worldIn, position, BlockDoublePlant.EnumPlantType.byMetadata(flower.getMetadata()), 0x03);
                    }
                    else
                    {
                        worldIn.setBlockState(position, ((ItemBlock) flower.getItem()).getBlock().getStateFromMeta(flower.getMetadata()));
                    }
                }
                else if (flower.getItem() instanceof ItemBlockSpecial)
                {
                    worldIn.setBlockState(position, ((ItemBlockSpecial) flower.getItem()).getBlock().getStateFromMeta(flower.getMetadata()));
                }
                else
                {
                    worldIn.setBlockState(position, BlockUtils.getBlockStateFromStack(flower));
                }
            }
        }

        if(this.ticker >= TICKER_LIMIT * TICKS_SECOND)
        {
            this.ticker = 0;
            this.composted = false;
        }
    }

    /**
     * Method for the composter to call to start producing flowers.
     * @param percentage the chance for this block to appear per second.
     * @param flower the flower to grow.
     */
    public void compost(final double percentage, @NotNull final ItemStack flower)
    {
        if(percentage >= 0 && percentage <= 100)
        {
            this.percentage = percentage;
            try
            {
                this.flower = flower;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        this.composted = true;
    }

    /**
     * Check if the current compost tile entity is running.
     * @return true if so.
     */
    public boolean isComposted()
    {
        return this.composted;
    }
}
