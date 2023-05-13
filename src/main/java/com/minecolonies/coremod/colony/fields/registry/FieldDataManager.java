package com.minecolonies.coremod.colony.fields.registry;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.IField;
import com.minecolonies.api.colony.fields.IFieldMatcher;
import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import com.minecolonies.api.colony.fields.registry.IFieldDataManager;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.api.util.Log;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * The field manager class responsible for creating field instances from NBT data, etc.
 */
public class FieldDataManager implements IFieldDataManager
{
    private static final String TAG_FIELD_NAME     = "name";
    private static final String TAG_FIELD_POSITION = "position";
    private static final String TAG_FIELD_DATA     = "data";

    @Override
    public IField createFrom(final @NotNull IColony colony, final @NotNull CompoundTag compound)
    {
        ResourceLocation fieldName = new ResourceLocation(compound.getString(TAG_FIELD_NAME));
        BlockPos position = BlockPosUtil.read(compound, TAG_FIELD_POSITION);

        IField field = createFrom(colony, position, fieldName);
        if (field != null)
        {
            field.deserializeNBT(compound.getCompound(TAG_FIELD_DATA));
        }
        return field;
    }

    @Override
    public IField createFrom(final @NotNull IColony colony, final @NotNull BlockPos position, final @NotNull ResourceLocation fieldName)
    {
        final FieldRegistries.FieldEntry fieldEntry = FieldRegistries.getFieldRegistry().getValue(fieldName);

        if (fieldEntry == null)
        {
            Log.getLogger().error("Unknown field type '{}'.", fieldName);
            return null;
        }

        return fieldEntry.produceField(colony, position);
    }

    @Override
    public CompoundTag createCompound(final @NotNull IField field)
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(TAG_FIELD_NAME, field.getFieldType().getRegistryName().toString());
        BlockPosUtil.write(compound, TAG_FIELD_POSITION, field.getPosition());
        compound.put(TAG_FIELD_DATA, field.serializeNBT());
        return compound;
    }

    @Override
    public IFieldMatcher matcherFromBytes(final @NotNull FriendlyByteBuf buf)
    {
        final FieldRegistries.FieldEntry fieldType = buf.readRegistryIdSafe(FieldRegistries.FieldEntry.class);
        final BlockPos position = buf.readBlockPos();
        final IFieldMatcher fieldMatcher = fieldType.produceFieldMatcher(position);
        fieldMatcher.fromBytes(buf);
        return fieldMatcher;
    }
}
