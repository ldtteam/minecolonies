package com.minecolonies.api.tileentities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.MathHelper;

import java.util.Random;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TileEntityEnchanter extends TileEntityColonyBuilding implements ITickableTileEntity
{
    public int   tickCount;
    public float pageFlip;
    public float pageFlipPrev;
    public float flipT;
    public float flipA;
    public float bookSpread;
    public float bookSpreadPrev;
    public float bookRotation;
    public float bookRotationPrev;
    public float tRot;

    private static final Random rand = new Random();

    /**
     * Default constructor used to create a new TileEntity via reflection. Do not use.
     */
    public TileEntityEnchanter()
    {
        this(MinecoloniesTileEntities.ENCHANTER);
    }

    /**
     * Alternative overriden constructor.
     */
    public TileEntityEnchanter(final TileEntityType type)
    {
        super(type);
    }

    public void update()
    {
        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;
        PlayerEntity lvt_1_1_ =
          this.world.getClosestPlayer((double) ((float) this.pos.getX() + 0.5F), (double) ((float) this.pos.getY() + 0.5F), (double) ((float) this.pos.getZ() + 0.5F), 3.0D, false);
        if (lvt_1_1_ != null)
        {
            double lvt_2_1_ = lvt_1_1_.posX - (double) ((float) this.pos.getX() + 0.5F);
            double lvt_4_1_ = lvt_1_1_.posZ - (double) ((float) this.pos.getZ() + 0.5F);
            this.tRot = (float) MathHelper.atan2(lvt_4_1_, lvt_2_1_);
            this.bookSpread += 0.1F;
            if (this.bookSpread < 0.5F || rand.nextInt(40) == 0)
            {
                float lvt_6_1_ = this.flipT;

                do
                {
                    this.flipT += (float) (rand.nextInt(4) - rand.nextInt(4));
                }
                while (lvt_6_1_ == this.flipT);
            }
        }
        else
        {
            this.tRot += 0.02F;
            this.bookSpread -= 0.1F;
        }

        while (this.bookRotation >= 3.1415927F)
        {
            this.bookRotation -= 6.2831855F;
        }

        while (this.bookRotation < -3.1415927F)
        {
            this.bookRotation += 6.2831855F;
        }

        while (this.tRot >= 3.1415927F)
        {
            this.tRot -= 6.2831855F;
        }

        while (this.tRot < -3.1415927F)
        {
            this.tRot += 6.2831855F;
        }

        float lvt_2_2_;
        for (lvt_2_2_ = this.tRot - this.bookRotation; lvt_2_2_ >= 3.1415927F; lvt_2_2_ -= 6.2831855F)
        {
        }

        while (lvt_2_2_ < -3.1415927F)
        {
            lvt_2_2_ += 6.2831855F;
        }

        this.bookRotation += lvt_2_2_ * 0.4F;
        this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
        ++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float lvt_3_1_ = (this.flipT - this.pageFlip) * 0.4F;
        float lvt_4_2_ = 0.2F;
        lvt_3_1_ = MathHelper.clamp(lvt_3_1_, -0.2F, 0.2F);
        this.flipA += (lvt_3_1_ - this.flipA) * 0.9F;
        this.pageFlip += this.flipA;
    }
}
