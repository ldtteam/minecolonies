package com.minecolonies.api.research.costs;

import com.google.gson.JsonObject;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Cost item for researches.
 */
public interface IResearchCost
{
    /**
     * The ID for this cost.
     *
     * @return the id.
     */
    ResourceLocation getId();

    /**
     * Get the count for this cost.
     *
     * @return the count.
     */
    int getCount();

    /**
     * Get the list of items for this cost.
     *
     * @return the list of items.
     */
    List<Item> getItems();

    /**
     * Read the cost from NBT.
     *
     * @param compound the compound.
     */
    void read(@NotNull final CompoundTag compound);

    /**
     * Write the cost to NBT.
     *
     * @param compound the compound.
     */
    void write(@NotNull final CompoundTag compound);

    /**
     * Checks if this json object has the correct fields for this cost instance.
     *
     * @param jsonObject the input json object.
     * @return true if the json object is in the right format.
     */
    boolean hasCorrectJsonFields(final JsonObject jsonObject);

    /**
     * Parses the json object on this cost instance.
     *
     * @param jsonObject the input json object.
     */
    void parseFromJson(final JsonObject jsonObject);
}
