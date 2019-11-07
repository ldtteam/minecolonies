package com.minecolonies.api.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TileEntityEnchanter extends TileEntityColonyBuilding implements ITickable
{
    public               int    tickCount;
    public               float  pageFlip;
    public               float  pageFlipPrev;
    public               float  flipT;
    public               float  flipA;
    public               float  bookSpread;
    public               float  bookSpreadPrev;
    public               float  bookRotation;
    public               float  bookRotationPrev;
    public               float  tRot;
    private static final Random rand = new Random();

    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityEnchanter()
    {
        super();
    }

    /**
     * Empty standard constructor.
     *
     * @param registryName the registry name of the building.
     */
    public TileEntityEnchanter(final ResourceLocation registryName)
    {
        super(registryName);
    }

    public void update()
    {
        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;
        EntityPlayer player = this.world.getClosestPlayer(((float) this.pos.getX() + 0.5F), ((float) this.pos.getY() + 0.5F), ((float) this.pos.getZ() + 0.5F), 3.0D, false);
        if (player != null)
        {
            double playerXPos = player.posX - (double) ((float) this.pos.getX() + 0.5F);
            double playerZPos = player.posZ - (double) ((float) this.pos.getZ() + 0.5F);
            this.tRot = (float) MathHelper.atan2(playerZPos, playerXPos);
            this.bookSpread += 0.1F;
            if (this.bookSpread < 0.5F || rand.nextInt(40) == 0)
            {
                float flip = this.flipT;

                do
                {
                    this.flipT += (float) (rand.nextInt(4) - rand.nextInt(4));
                }
                while (flip == this.flipT);
            }
        }
        else
        {
            this.tRot += 0.02F;
            this.bookSpread -= 0.1F;
        }

        this.bookRotation = (float) ((this.bookRotation + Math.PI % (2 * Math.PI)) - Math.PI);

        while (this.bookRotation < -Math.PI)
        {
            this.bookRotation += 2 * Math.PI;
        }

        while (this.tRot >= Math.PI)
        {
            this.tRot -= 2 * Math.PI;
        }

        while (this.tRot < -Math.PI)
        {
            this.tRot += 2 * Math.PI;
        }
        float circleBasedRot = (float) ((this.tRot - this.bookRotation + Math.PI % (2 * Math.PI)) - Math.PI);

        this.bookRotation += circleBasedRot * 0.4F;
        this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
        ++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float pageFlip = (this.flipT - this.pageFlip) * 0.4F;
        pageFlip = MathHelper.clamp(pageFlip, -0.2F, 0.2F);
        this.flipA += (pageFlip - this.flipA) * 0.9F;
        this.pageFlip += this.flipA;
    }
}
