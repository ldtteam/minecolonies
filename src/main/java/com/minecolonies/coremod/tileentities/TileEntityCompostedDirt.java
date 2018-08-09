package com.minecolonies.coremod.tileentities;

import com.minecolonies.blockout.Log;
import net.minecraft.block.BlockFlower;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.Random;

//Todo: implement this class
public class TileEntityCompostedDirt extends TileEntity implements ITickable
{

    private boolean composted = false;

    private int ticker = 0;

    private int percentage = 20;

    private final static int TICKER_LIMIT = 4500;

    private final Random random = new Random();

    private BlockFlower.EnumFlowerType flowerType;

    @Override
    public void update()
    {
        final World world = this.getWorld();
        if(!world.isRemote && this.composted)
        {
            this.updateTick(world);
        }
    }

    private void updateTick(World worldIn)
    {
        if(this.composted)
        {
            ((WorldServer)worldIn).spawnParticle(
              EnumParticleTypes.VILLAGER_HAPPY, this.getPos().getX()+0.5,
              this.getPos().getY()+1, this.getPos().getZ()+0.5,
              1, 0.2, 0, 0.2, 0);
        }
        ticker ++;

        if(this.ticker%(TICKER_LIMIT/20)==0)
        {
            if(random.nextInt(100) <= this.percentage)
            {
                Log.getLogger().info("SPAWNING A FLOWER");
                //Todo: spawn flower over the block if the block above is AIR
            }
        }

        if(this.ticker >= TICKER_LIMIT)
        {
            this.ticker = 0;
            this.composted = false;
        }
    }


    // AI interface

    public void compost(final int percentage, Class flowerType)
    {
        if(percentage >= 0 && percentage <= 100)
        {
            this.percentage = percentage;
            try
            {
                if(flowerType.newInstance() instanceof BlockFlower.EnumFlowerType)
                this.flowerType = (BlockFlower.EnumFlowerType)flowerType.newInstance();
            }
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        this.composted = true;
    }

    public boolean isComposted()
    {
        return this.composted;
    }
}
