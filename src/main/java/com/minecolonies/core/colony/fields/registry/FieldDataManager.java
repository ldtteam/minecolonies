package com.minecolonies.core.colony.fields.registry;

import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * The field manager class responsible for creating field instances from NBT data, etc.
 */
public final class FieldDataManager
{
    private static final String TAG_FIELD_NAME     = "name";
    private static final String TAG_FIELD_POSITION = "position";
    private static final String TAG_FIELD_DATA     = "data";

    private FieldDataManager()
    {
    }

    /**
     * Creates a field instance from NBT compound data.
     *
     * @param compound the input compound data.
     * @return the created field instance.
     */
    public static IField compoundToField(@NotNull final HolderLookup.Provider provider, final @NotNull CompoundTag compound)
    {
        ResourceLocation fieldName = ResourceLocation.parse(compound.getString(TAG_FIELD_NAME));
        BlockPos position = BlockPosUtil.read(compound, TAG_FIELD_POSITION);

        IField field = resourceLocationToField(fieldName, position);
        if (field != null)
        {
            field.deserializeNBT(provider, compound.getCompound(TAG_FIELD_DATA));
        }
        return field;
    }

    /**
     * Creates a field instance from a field type and position.
     *
     * @param fieldName the field registry entry name.
     * @param position  the position of the field.
     * @return the field instance.
     */
    public static IField resourceLocationToField(final @NotNull ResourceLocation fieldName, final @NotNull BlockPos position)
    {
        final FieldRegistries.FieldEntry fieldEntry = FieldRegistries.getFieldRegistry().get(fieldName);

        if (fieldEntry == null)
        {
            Log.getLogger().error("Unknown field type '{}'.", fieldName);
            return null;
        }

        return fieldEntry.produceField(position);
    }

    /**
     * Creates a field instance from a complete network buffer.
     *
     * @param buf the buffer, still containing the field registry type and position.
     * @return the field instance.
     */
    public static IField bufferToField(final @NotNull RegistryFriendlyByteBuf buf)
    {
        final FieldRegistries.FieldEntry fieldType = buf.readById(FieldRegistries.getFieldRegistry()::byIdOrThrow);
        final BlockPos position = buf.readBlockPos();
        final IField field = fieldType.produceField(position);
        field.deserialize(buf);
        return field;
    }

    /**
     * Creates a network buffer from a field instance.
     *
     * @param field the field instance.
     * @return the network buffer.
     */
    public static RegistryFriendlyByteBuf fieldToBuffer(final @NotNull IField field, @NotNull final RegistryAccess provider)
    {
        final RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), provider);
        buf.writeById(FieldRegistries.getFieldRegistry()::getIdOrThrow, field.getFieldType());
        buf.writeBlockPos(field.getPosition());
        field.serialize(buf);
        return buf;
    }

    /**
     * Creates NBT compound data from a field instance.
     *
     * @param field the field instance.
     * @return the NBT compound.
     */
    public static CompoundTag fieldToCompound(@NotNull final HolderLookup.Provider provider, final @NotNull IField field)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(TAG_FIELD_NAME, field.getFieldType().getRegistryName().toString());
        BlockPosUtil.write(compound, TAG_FIELD_POSITION, field.getPosition());
        compound.put(TAG_FIELD_DATA, field.serializeNBT(provider));
        return compound;
    }
}
