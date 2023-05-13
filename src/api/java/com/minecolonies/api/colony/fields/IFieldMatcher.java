package com.minecolonies.api.colony.fields;

import com.minecolonies.api.colony.fields.registry.FieldRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for field matcher instances.
 */
public interface IFieldMatcher
{
    /**
     * Get the field type for the matcher.
     *
     * @return the field type.
     */
    FieldRegistries.FieldEntry getFieldType();

    /**
     * Get the position for the matcher.
     *
     * @return the position of the field.
     */
    BlockPos getPosition();

    /**
     * Whether this field matcher matches the other provided field.
     *
     * @param other the other field.
     * @return true if so.
     */
    boolean matches(IField other);

    /**
     * Whether this field matcher matches the other provided field view.
     *
     * @param other the other field view.
     * @return true if so.
     */
    boolean matchesView(IFieldView other);

    /**
     * Whether this field matcher matches the other provided field matcher.
     *
     * @param other the other field matcher.
     * @return true if so.
     */
    boolean matches(IFieldMatcher other);

    /**
     * Serialize a field to a buffer.
     *
     * @param buf the buffer to write the field data to.
     */
    void toBytes(@NotNull FriendlyByteBuf buf);

    /**
     * Deserialize a field from a buffer.
     *
     * @param buf the buffer to read the data from.
     */
    void fromBytes(@NotNull FriendlyByteBuf buf);

    /**
     * Hashcode implementation for this field.
     */
    int hashCode();

    /**
     * Equals implementation for this field.
     */
    boolean equals(Object other);
}
