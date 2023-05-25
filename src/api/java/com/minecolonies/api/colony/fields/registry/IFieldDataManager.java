package com.minecolonies.api.colony.fields.registry;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.fields.IField;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for the field manager class responsible for creating field instances from NBT data, etc.
 */
public interface IFieldDataManager
{
    /**
     * Get the instance of the field data manager.
     *
     * @return the field data manager.
     */
    static IFieldDataManager getInstance()
    {
        return IMinecoloniesAPI.getInstance().getFieldDataManager();
    }

    /**
     * Creates a new entry from a given {@link IColony} and the data passed in as {@link CompoundTag}.
     *
     * @param colony   The {@link IColony} to which the new {@link IField} belongs.
     * @param compound The data from which to load new {@link IField} stored in a {@link CompoundTag}.
     * @return The {@link IField} with the data loaded from {@link CompoundTag}.
     */
    @Nullable
    IField createFrom(@NotNull final IColony colony, @NotNull final CompoundTag compound);

    /**
     * Creates a new entry from a given {@link IColony} and a {@link ResourceLocation} registry name.
     *
     * @param colony    The {@link IColony} to which the new {@link IField} belongs.
     * @param position  The position on which the new {@link IField} is created.
     * @param fieldName The name of the {@link IField} as registered to the registry.
     * @return The {@link IField}.
     */
    IField createFrom(@NotNull final IColony colony, @NotNull final BlockPos position, @NotNull final ResourceLocation fieldName);

    IField createFromBuffer(@NotNull final IColony colony, @NotNull final FriendlyByteBuf buf);

    /**
     * @param field
     */
    CompoundTag createCompound(@NotNull final IField field);
}
