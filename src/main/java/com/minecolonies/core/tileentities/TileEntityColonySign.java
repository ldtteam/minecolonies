package com.minecolonies.core.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.tileentities.ITickable;
import com.minecolonies.api.tileentities.MinecoloniesTileEntities;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.MathUtils;
import com.minecolonies.api.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static net.minecraft.SharedConstants.TICKS_PER_SECOND;

public class TileEntityColonySign extends BlockEntity implements ITickable
{
    /**
     * Connected colony id.
     */
    private int colonyId = -1;

    /**
     * Anchor position to which its supposed to point.
     */
    private BlockPos anchor = null;

    /**
     * Colony name cache.
     */
    private String colonyNameCache = "";

    /**
     * Rotation this is pointing to.
     */
    private float rotation;

    /**
     * Distance to colony
     */
    private int distance;

    /**
     * Tick offset.
     */
    private final int tickOffset;

    public TileEntityColonySign(final BlockPos pos, final BlockState state)
    {
        super(MinecoloniesTileEntities.COLONY_SIGN.get(), pos, state);
        tickOffset = MathUtils.RANDOM.nextInt(TICKS_PER_SECOND);
    }

    @Override
    public void setChanged()
    {
        if (level != null)
        {
            WorldUtil.markChunkDirty(level, worldPosition);
        }
    }

    @Override
    public void load(@NotNull final CompoundTag compound)
    {
        super.load(compound);
        this.colonyId = compound.getInt(TAG_COLONY_ID);
        this.colonyNameCache = compound.getString(TAG_NAME);
        this.anchor = BlockPosUtil.read(compound, TAG_POS);
        this.rotation = compound.getFloat(TAG_ROTATION);
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt(TAG_COLONY_ID, this.colonyId);
        compound.putString(TAG_NAME, this.colonyNameCache);
        BlockPosUtil.write(compound, TAG_POS, anchor);
        compound.putFloat(TAG_ROTATION, this.rotation);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag()
    {
        return this.saveWithId();
    }

    @Override
    public void onDataPacket(final Connection net, final ClientboundBlockEntityDataPacket packet)
    {
        final CompoundTag compound = packet.getTag();
        this.load(compound);
    }

    @Override
    public void setRemoved()
    {
        super.setRemoved();
        //todo update colony
    }

    /**
     * Update method to be called by Minecraft every tick
     */
    @Override
    public void tick()
    {
        if (!level.isClientSide && (level.getGameTime() + tickOffset) % TICKS_PER_SECOND * 60 == 0)
        {
            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, level.dimension());
            if (colony != null)
            {
                colonyNameCache = colony.getName();
            }
        }
    }

    /**
     * Set the colony meta data.
     * @param colony the colony.
     * @param anchor the anchor position it points to.
     */
    public void setColonyAndAnchor(final IColony colony, @Nullable final BlockPos anchor)
    {
        this.colonyId = colony.getID();
        this.anchor = anchor == null ? colony.getCenter() : anchor;
        this.distance = (int) colony.getCenter().distSqr(getBlockPos());

        this.colonyNameCache = colony.getName();

        double dx = this.anchor.getX() + 0.5 - (getBlockPos().getX() + 0.5);  // Center of block
        double dz = this.anchor.getZ() + 0.5 - (getBlockPos().getZ() + 0.5);

        double angleRad = Math.atan2(-dz, dx);  // East = 0°, North = 90°, West = 180°, South = 270°
        float angleDeg = (float) Math.toDegrees(angleRad);

        this.rotation = (angleDeg + 360) % 360;
    }

    /**
     * Obtain the colony name.
     * Cached for efficiency.
     * @return string name.
     */
    public String getColonyName()
    {
        return colonyNameCache;
    }

    /**
     * Get the relative rotation to the colony.
     * Cached for efficiency.
     * @return the value.
     */
    public float getRelativeRotation()
    {
        return rotation;
    }

    /**
     * Get distance to colony.
     * @param dimension the dimension the colony is in.
     * @return the distance in blocks.
     */
    public int getColonyDistance(final ResourceKey<Level> dimension)
    {
        return distance;
    }

    /**
     * Get colony id from sign, like to copy it on another sign.
     * @return the colony id.
     */
    public int getColonyId()
    {
        return colonyId;
    }
}
