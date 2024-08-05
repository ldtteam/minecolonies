package com.minecolonies.api.research.costs;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.ModResearchCostTypes.ResearchCostType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Cost item for researches.
 */
public interface IResearchCost
{
    /**
     * The type for this cost.
     *
     * @return the type.
     */
    ResearchCostType getType();

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
     * Get the translated text for this research cost.
     *
     * @return the translated text.
     */
    default Component getTranslatedName()
    {
        return ComponentUtils.formatList(getItems().stream().map(Item::getDescription).toList(), Component.literal(" / "));
    }

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
     * Serialize the cost instance to the given {@link FriendlyByteBuf}.
     *
     * @param buf the network buffer.
     */
    void serialize(@NotNull final RegistryFriendlyByteBuf buf);

    /**
     * Deserialize the {@link FriendlyByteBuf} to this cost instance.
     *
     * @param buf the network buffer.
     */
    void deserialize(@NotNull final RegistryFriendlyByteBuf buf);

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
