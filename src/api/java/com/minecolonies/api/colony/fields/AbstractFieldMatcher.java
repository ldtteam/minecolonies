package com.minecolonies.api.colony.fields;

import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract field matcher implementation, can be overridden without having to do
 * anything else to get a basic "position only" field matcher.
 */
public abstract class AbstractFieldMatcher implements IFieldMatcher
{
    /**
     * The field type.
     */
    private final @NotNull FieldRegistries.FieldEntry fieldType;

    /**
     * The position of the field.
     */
    private final @NotNull BlockPos position;

    /**
     * Default constructor.
     *
     * @param fieldType the field type.
     * @param position  the position of the field.
     */
    protected AbstractFieldMatcher(@NotNull FieldRegistries.FieldEntry fieldType, @NotNull BlockPos position)
    {
        this.fieldType = fieldType;
        this.position = position;
    }

    @Override
    public final @NotNull FieldRegistries.FieldEntry getFieldType()
    {
        return fieldType;
    }

    @Override
    public final @NotNull BlockPos getPosition()
    {
        return position;
    }

    @Override
    public boolean matches(@Nullable final IField other)
    {
        return other != null && fieldType.getRegistryName().equals(other.getFieldType().getRegistryName()) && position.equals(other.getPosition());
    }

    @Override
    public boolean matchesView(final IFieldView other)
    {
        return other != null && fieldType.getRegistryName().equals(other.getFieldType().getRegistryName()) && position.equals(other.getPosition());
    }

    @Override
    public boolean matches(final IFieldMatcher other)
    {
        return other != null && fieldType.getRegistryName().equals(other.getFieldType().getRegistryName()) && position.equals(other.getPosition());
    }

    @Override
    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeRegistryId(FieldRegistries.getFieldRegistry(), fieldType);
        buf.writeBlockPos(position);
    }

    @Override
    public void fromBytes(@NotNull final FriendlyByteBuf buf)
    {
    }

    @Override
    public int hashCode()
    {
        int result = fieldType.hashCode();
        result = 31 * result + position.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof IFieldMatcher matcher)
        {
            return matches(matcher);
        }

        return false;
    }
}
