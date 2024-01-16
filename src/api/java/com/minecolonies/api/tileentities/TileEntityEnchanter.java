package com.minecolonies.api.tileentities;

import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

/**
 * Class which handles the tileEntity of our colonyBuildings.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TileEntityEnchanter extends TileEntityColonyBuilding
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
    public TileEntityEnchanter(final BlockPos pos, final BlockState state)
    {
        this(MinecoloniesTileEntities.ENCHANTER.get(), pos, state);
    }

    /**
     * Alternative overriden constructor.
     *
     * @param type the entity type.
     */
    public TileEntityEnchanter(final BlockEntityType<? extends TileEntityEnchanter> type, final BlockPos pos, final BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void tick()
    {
        super.tick();

        if (!level.isClientSide)
        {
            return;
        }

        this.bookSpreadPrev = this.bookSpread;
        this.bookRotationPrev = this.bookRotation;
        Player player = this.level.getNearestPlayer(((float) this.worldPosition.getX() + 0.5F), ((float) this.worldPosition.getY() + 0.5F), ((float) this.worldPosition.getZ() + 0.5F), 3.0D, false);
        if (player != null)
        {
            double playerXPos = player.getX() - (double) ((float) this.worldPosition.getX() + 0.5F);
            double playerZPos = player.getZ() - (double) ((float) this.worldPosition.getZ() + 0.5F);
            this.tRot = (float) Mth.atan2(playerZPos, playerXPos);
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
        this.bookSpread = Mth.clamp(this.bookSpread, 0.0F, 1.0F);
        ++this.tickCount;
        this.pageFlipPrev = this.pageFlip;
        float pageFlip = (this.flipT - this.pageFlip) * 0.4F;
        pageFlip = Mth.clamp(pageFlip, -0.2F, 0.2F);
        this.flipA += (pageFlip - this.flipA) * 0.9F;
        this.pageFlip += this.flipA;
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }
}
