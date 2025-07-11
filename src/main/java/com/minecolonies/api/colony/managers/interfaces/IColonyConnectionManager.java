package com.minecolonies.api.colony.managers.interfaces;

import com.minecolonies.api.colony.ColonyConnectionNode;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.core.colony.managers.ColonyConnectionManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_STATUS;

/**
 * Connection manager interface.
 */
public interface IColonyConnectionManager extends INBTSerializable<CompoundTag>
{
    /**
     * Connected Colony Data with:
     *
     * @param id              the colony id.
     * @param name            the colony name (cached).
     * @param pos             the colony gate position (cached).
     * @param diplomacyStatus the diplomacy status of the two colonies.
     */
    record ConnectedColonyData(
        int id,
        String name,
        BlockPos pos,
        ColonyConnectionManager.DiplomacyStatus diplomacyStatus)
    {

        public CompoundTag serializeNBT()
        {
            final CompoundTag compoundTag = new CompoundTag();
            compoundTag.putInt(TAG_ID, id);
            compoundTag.putString(TAG_NAME, name);
            BlockPosUtil.write(compoundTag, TAG_POS, pos);
            compoundTag.putInt(TAG_STATUS, diplomacyStatus.ordinal());
            return compoundTag;
        }

        public void serializeByteBuf(final FriendlyByteBuf buf)
        {
            buf.writeInt(id);
            buf.writeUtf(name);
            buf.writeBlockPos(pos);
            buf.writeInt(diplomacyStatus.ordinal());
        }

        public static ConnectedColonyData deserializeNBT(final CompoundTag compoundTag)
        {
            return new ConnectedColonyData(compoundTag.getInt(TAG_ID),
                compoundTag.getString(TAG_NAME),
                BlockPosUtil.read(compoundTag, TAG_POS),
                ColonyConnectionManager.DiplomacyStatus.values()[compoundTag.getInt(TAG_STATUS)]);
        }

        public static ConnectedColonyData deserializeByteBuf(final FriendlyByteBuf buf)
        {
            return new ConnectedColonyData(buf.readInt(), buf.readUtf(32767), buf.readBlockPos(), ColonyConnectionManager.DiplomacyStatus.values()[buf.readInt()]);
        }
    }

    /**
     * Add a new connection point and connect to neighbors.
     *
     * @param connectionPoint the node position.
     * @return
     */
    boolean addNewConnectionNode(final BlockPos connectionPoint);

    /**
     * Remove a connection point and update neighbors.
     * @param connectionPoint the node position.
     */
    void removeConnectionNode(final BlockPos connectionPoint);

    /**
     * Tick to process work.
     */
    void tick();

    /**
     * Get all directly connected colonies.
     * @return the list of directly connected colonies.
     */
    Int2ObjectMap<ColonyConnectionManager.ConnectedColonyData> getDirectlyConnectedColonies();

    /**
     * Get a connection node.
     *
     * @param blockPos its position.
     * @return the node object.
     */
    ColonyConnectionNode getNode(final BlockPos blockPos);

    /**
     * Add a new gatehouse.
     * @param gateHousePosition the blockpos.
     */
    void addNewGateHouse(final BlockPos gateHousePosition);

    /**
     * Remove a gatehouse.
     * @param gateHousePosition the blockpos.
     */
    void removeGateHouse(final BlockPos gateHousePosition);

    /**
     * Attempt to establish a connection.
     * @param clickedPos the clicked position.
     * @param targetColony, the colony we're trying to connect to.
     */
    boolean attemptEstablishConnection(final BlockPos clickedPos, final IColony targetColony);

    void serializeToView(@NotNull FriendlyByteBuf buf);

    void deserializeFromView(@NotNull FriendlyByteBuf buf);

    Collection<ColonyConnectionManager.ConnectedColonyData> getConnectedColonies();
}
