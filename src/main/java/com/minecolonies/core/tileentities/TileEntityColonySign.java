package com.minecolonies.core.tileentities;

import com.minecolonies.api.colony.connections.ColonyConnectionNode;
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
     * Target colony id we're trying to connect to.
     */
    private int targetColonyId = -1;

    /**
     * Anchor position to which its supposed to point.
     */
    private BlockPos anchor = null;

    /**
     * Colony name cache.
     */
    private String colonyNameCache = "";

    /**
     * Colony name cache.
     */
    private String targetColonyNameCache = "";

    /**
     * Rotation this is pointing to.
     */
    private float rotation;

    /**
     * Distance to colony
     */
    private int distance;

    /**
     * Distance to target colony.
     */
    private int targetColonyDistance;

    /**
     * Tick offset.
     */
    private final int tickOffset;

    /**
     * Cached colony in sign above.
     */
    private int cachedSignAboveColony = -1;

    /**
     * Previous sign/connection.
     */
    private BlockPos previousPosition = BlockPos.ZERO;

    /**
     * Next sign/connection.
     */
    private BlockPos nextPosition = BlockPos.ZERO;

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
        if (compound.contains(TAG_POS))
        {
            this.anchor = BlockPosUtil.read(compound, TAG_POS);
        }
        this.rotation = compound.getFloat(TAG_ROTATION);
        this.targetColonyId = compound.getInt(TAG_TARGET_COLONY_ID);
        this.targetColonyNameCache = compound.getString(TAG_TARGET_COLONY_NAME);
        this.distance = compound.getInt(TAG_DISTANCE);
        this.targetColonyDistance = compound.getInt(TAG_TARGET_DISTANCE);
        if (compound.contains(TAG_CACHED_ABOVE))
        {
            this.cachedSignAboveColony = compound.getInt(TAG_CACHED_ABOVE);
        }
        if (compound.contains(TAG_PREV_POS))
        {
            this.previousPosition = BlockPosUtil.read(compound, TAG_PREV_POS);
        }
        if (compound.contains(TAG_NEXT_POS))
        {
            this.nextPosition = BlockPosUtil.read(compound, TAG_NEXT_POS);
        }
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag compound)
    {
        super.saveAdditional(compound);
        compound.putInt(TAG_COLONY_ID, this.colonyId);
        compound.putString(TAG_NAME, this.colonyNameCache);
        if (anchor != null)
        {
            BlockPosUtil.write(compound, TAG_POS, anchor);
        }
        compound.putFloat(TAG_ROTATION, this.rotation);
        compound.putInt(TAG_TARGET_COLONY_ID, this.targetColonyId);
        compound.putString(TAG_TARGET_COLONY_NAME, this.targetColonyNameCache);
        compound.putInt(TAG_DISTANCE, this.distance);
        compound.putInt(TAG_TARGET_DISTANCE, this.targetColonyDistance);
        compound.putInt(TAG_CACHED_ABOVE, this.cachedSignAboveColony);
        BlockPosUtil.write(compound, TAG_NEXT_POS, nextPosition);
        BlockPosUtil.write(compound, TAG_PREV_POS, previousPosition);
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

    /**
     * Update method to be called by Minecraft every tick
     */
    @Override
    public void tick()
    {
        if (!level.isClientSide && (level.getGameTime() + tickOffset) % TICKS_PER_SECOND * 60 == 0)
        {
            final BlockEntity blockEntity = level.getBlockEntity(getBlockPos().above());
            if (blockEntity instanceof TileEntityColonySign tileEntityColonySign)
            {
                if (cachedSignAboveColony != tileEntityColonySign.getColonyId())
                {
                    cachedSignAboveColony = tileEntityColonySign.getColonyId();
                    setChanged();
                }
            }
            else
            {
                cachedSignAboveColony = -1;
            }

            final IColony colony = IColonyManager.getInstance().getColonyByDimension(colonyId, level.dimension());
            if (colony != null)
            {
                if (!colony.getName().equals(colonyNameCache))
                {
                    colonyNameCache = colony.getName();
                    setChanged();
                }
                final ColonyConnectionNode node = colony.getConnectionManager().getNode(getBlockPos());
                if (node != null)
                {
                    calculateRotation(node.getPreviousNode());
                    final BlockPos previousNodePos = node.getPreviousNode();
                    if (!previousNodePos.equals(BlockPos.ZERO) && WorldUtil.isBlockLoaded(level, previousNodePos))
                    {
                        final int prevDistance = this.distance;
                        if (level.getBlockEntity(previousNodePos) instanceof TileEntityColonySign tileEntityColonySign)
                        {
                            this.distance = (int) BlockPosUtil.dist(previousNodePos, getBlockPos()) + tileEntityColonySign.distance;
                        }
                        else
                        {
                            this.distance = (int) BlockPosUtil.dist(previousNodePos, getBlockPos());
                        }

                        if (prevDistance != this.distance)
                        {
                            setChanged();
                        }
                    }

                    if (!previousNodePos.equals(BlockPos.ZERO) && !previousNodePos.equals(previousPosition))
                    {
                        this.previousPosition = previousNodePos;
                        setChanged();
                    }

                    final BlockPos nextNodePos = node.getNextNode();
                    if (!nextNodePos.equals(nextPosition))
                    {
                        this.nextPosition = nextNodePos;
                        setChanged();
                    }

                    this.targetColonyId = node.getTargetColonyId();
                    if (this.targetColonyId != -1)
                    {
                        if (!nextNodePos.equals(BlockPos.ZERO))
                        {
                            if (WorldUtil.isBlockLoaded(level, nextNodePos))
                            {
                                final int prevDistance = this.targetColonyDistance;
                                if (level.getBlockEntity(nextNodePos) instanceof TileEntityColonySign tileEntityColonySign)
                                {
                                    this.targetColonyDistance = (int) BlockPosUtil.dist(nextNodePos, getBlockPos()) + tileEntityColonySign.targetColonyDistance;
                                }
                                else
                                {
                                    this.targetColonyDistance = (int) BlockPosUtil.dist(nextNodePos, getBlockPos());
                                }
                                if (prevDistance != this.targetColonyDistance)
                                {
                                    setChanged();
                                }
                            }
                        }

                        final IColony targetColony = IColonyManager.getInstance().getColonyByDimension(targetColonyId, level.dimension());
                        if (targetColony != null && !targetColonyNameCache.equals(targetColony.getName()))
                        {
                            this.targetColonyNameCache = targetColony.getName();
                            setChanged();
                        }
                    }
                }
            }
        }
    }

    /**
     * Properly calculate position from the node this should point at.
     * @param previousNode the node to point at.
     */
    private void calculateRotation(final BlockPos previousNode)
    {
        double dx = previousNode.getX() + 0.5 - (getBlockPos().getX() + 0.5);  // Center of block
        double dz = previousNode.getZ() + 0.5 - (getBlockPos().getZ() + 0.5);

        double angleRad = Math.atan2(-dz, dx);  // East = 0°, North = 90°, West = 180°, South = 270°
        float angleDeg = (float) Math.toDegrees(angleRad);

        this.rotation = (angleDeg + 360) % 360;
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
        this.distance = (int) BlockPosUtil.dist(colony.getCenter(), getBlockPos());

        this.colonyNameCache = colony.getName();

        calculateRotation(this.anchor);
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
     * @return the distance in blocks.
     */
    public int getColonyDistance()
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

    /**+
     * Get target colony id from sign, -1 if not set.
     * @return the target colony id.
     */
    public int getTargetColonyId()
    {
        return targetColonyId;
    }

    /**
     * Obtain the target colony name.
     * Cached for efficiency.
     * @return string name.
     */
    public String getTargetColonyName()
    {
        return targetColonyNameCache;
    }

    /**
     * Get target colony distance.
     * @return the distance in blocks.
     */
    public int getTargetColonyDistance()
    {
        return targetColonyDistance;
    }

    /**
     * Get colony of sign above (cached).
     * @return the colony id (-1 default).
     */
    public int getCachedSignAboveColony()
    {
        return cachedSignAboveColony;
    }

    /**
     * Get previous node pos.
     * @return the pos.
     */
    @NotNull
    public BlockPos getPreviousPos()
    {
        return previousPosition;
    }

    /**
     * Get next node pos.
     * @return the pos.
     */
    @NotNull
    public BlockPos getNextPosition()
    {
        return nextPosition;
    }
}
