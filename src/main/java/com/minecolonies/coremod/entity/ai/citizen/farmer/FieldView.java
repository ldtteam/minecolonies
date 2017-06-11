package com.minecolonies.coremod.entity.ai.citizen.farmer;

import com.minecolonies.api.util.BlockPosUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Client side representation of a field.
 */
public class FieldView
{
    /**
     * Unique id of the field.
     */
    private BlockPos id;

    /**
     * Has the field been taken yet?
     */
    private boolean taken;

    /**
     * Owner of the field.
     */
    private String owner;

    /**
     * Set item of the field - might be null.
     */
    @Nullable
    private Item item;

    /**
     * Constructor to instantiate field to fill it from a byteBuffer.
     */
    public FieldView()
    {
        /**
         * Intentionally left empty.
         */
    }

    /**
     * Create the fieldView.
     *
     * @param field the field.
     */
    public FieldView(@NotNull final Field field)
    {
        this.id = field.getID();
        this.taken = field.isTaken();
        this.owner = field.getOwner();
        this.item = field.getSeed() == null ? null : field.getSeed().getItem();
    }

    /**
     * Getter of the field id.
     *
     * @return blockPos of the field.
     */
    public BlockPos getId()
    {
        return id;
    }

    /**
     * Checks if field has been claimed yet.
     *
     * @return true if so.
     */
    public boolean isTaken()
    {
        return taken;
    }

    /**
     * Claims or frees the field.
     *
     * @param taken true if should be claimed.
     */
    public void setTaken(final boolean taken)
    {
        this.taken = taken;
    }

    /**
     * Checks for the name of the owner.
     *
     * @return String of the name.
     */
    public String getOwner()
    {
        return owner;
    }

    /**
     * Sets the owner of the field.
     *
     * @param owner name of the owner.
     */
    public void setOwner(final String owner)
    {
        this.owner = owner;
    }

    /**
     * Writes the fields data to a byte buf for transition.
     *
     * @param buf Buffer to write to.
     */
    public void serializeViewNetworkData(@NotNull final ByteBuf buf)
    {
        BlockPosUtil.writeToByteBuf(buf, id);
        buf.writeBoolean(taken);
        ByteBufUtils.writeUTF8String(buf, owner);
        final int itemId = item == null ? 0 : Item.getIdFromItem(item);
        buf.writeInt(itemId);
    }

    /**
     * Deserialize the attributes and variables from transition.
     *
     * @param buf Byte buffer to deserialize.
     * @return FieldView return an instance of the fieldView.
     */
    @NotNull
    public FieldView deserialize(@NotNull final ByteBuf buf)
    {
        id = BlockPosUtil.readFromByteBuf(buf);
        taken = buf.readBoolean();
        owner = ByteBufUtils.readUTF8String(buf);
        final int itemId = buf.readInt();
        item = itemId == 0 ? null : Item.getItemById(itemId);
        return this;
    }

    /**
     * Returns the item of the field.
     *
     * @return an item object.
     */
    @Nullable
    public Item getItem()
    {
        return item;
    }
}
