package com.minecolonies.api.colony.buildings.workerbuildings.fields;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic record used to select specific fields.
 * This goes by the logic that fields are considered identical when their position and item are identical.
 *
 * @param position the position the field is supposed to occur in.
 * @param plant    the plant that is harvested on this field.
 */
public record FieldRecord(@NotNull BlockPos position, @Nullable Item plant)
{
    public static FieldRecord fromBytes(@NotNull final FriendlyByteBuf buf)
    {
        BlockPos position = buf.readBlockPos();
        Item plant = null;
        if (buf.readBoolean())
        {
            plant = buf.readItem().getItem();
        }
        return new FieldRecord(position, plant);
    }

    public void toBytes(@NotNull final FriendlyByteBuf buf)
    {
        buf.writeBlockPos(position);
        buf.writeBoolean(plant != null);
        if (plant != null)
        {
            buf.writeItem(new ItemStack(plant));
        }
    }
}