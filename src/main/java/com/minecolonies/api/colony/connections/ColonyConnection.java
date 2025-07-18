package com.minecolonies.api.colony.connections;

import com.minecolonies.api.util.BlockPosUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

import static com.minecolonies.api.util.constant.NbtTagConstants.*;
import static com.minecolonies.api.util.constant.NbtTagConstants.TAG_STATUS;

/**
 * Small storage class to hold colony connection data.
 */
public class ColonyConnection
{
    public DiplomacyStatus diplomacyStatus;
    public int                                      id;
    public String                                   name;
    public BlockPos                                 pos;

    /**
     * Connected Colony Data with:
     *
     * @param id              the colony id.
     * @param name            the colony name (cached).
     * @param pos             the colony gate position (cached).
     * @param diplomacyStatus the diplomacy status of the two colonies.
     */
    public ColonyConnection(
        int id,
        String name,
        BlockPos pos,
        DiplomacyStatus diplomacyStatus)
    {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.diplomacyStatus = diplomacyStatus;
    }

    /**
     * Constructor for deserialization/serialization.
     */
    public ColonyConnection()
    {
        // noop
    }

    public CompoundTag serializeNBT()
    {
        final CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt(TAG_ID, id);
        compoundTag.putString(TAG_NAME, name);
        BlockPosUtil.write(compoundTag, TAG_POS, pos);
        compoundTag.putInt(TAG_STATUS, diplomacyStatus.ordinal());
        return compoundTag;
    }

    public ColonyConnection deserializeNBT(final CompoundTag compoundTag)
    {
        this.id = compoundTag.getInt(TAG_ID);
        this.name = compoundTag.getString(TAG_NAME);
        this.pos = BlockPosUtil.read(compoundTag, TAG_POS);
        this.diplomacyStatus = DiplomacyStatus.values()[compoundTag.getInt(TAG_STATUS)];
        return this;
    }

    public void serializeByteBuf(final FriendlyByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeUtf(name);
        buf.writeBlockPos(pos);
        buf.writeInt(diplomacyStatus.ordinal());
    }

    public ColonyConnection deserializeByteBuf(final FriendlyByteBuf buf)
    {
        this.id = buf.readInt();
        this.name = buf.readUtf();
        this.pos = buf.readBlockPos();
        this.diplomacyStatus = DiplomacyStatus.values()[buf.readInt()];
        return this;
    }
}
