package com.minecolonies.core.research.costs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.research.ModResearchCostTypes.ResearchCostType;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.research.GlobalResearch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.core.research.GlobalResearch.RESEARCH_ITEM_LIST_PROP;
import static com.minecolonies.core.research.GlobalResearch.RESEARCH_ITEM_NAME_PROP;

/**
 * A plain item cost that takes a list of several items that have to be fulfilled.
 */
public class ListItemCost implements IResearchCost
{
    /**
     * The cost type.
     */
    private final ResearchCostType type;

    /**
     * The count of items.
     */
    private int count;

    /**
     * The list of items.
     */
    private List<Item> items;

    /**
     * Default constructor.
     */
    public ListItemCost(final ResearchCostType type)
    {
        this.type = type;
    }

    @Override
    public ResearchCostType getType()
    {
        return type;
    }

    @Override
    public int getCount()
    {
        return this.count;
    }

    @Override
    public List<Item> getItems()
    {
        return this.items;
    }

    @Override
    public void read(@NotNull final CompoundTag compound)
    {
        this.items = NBTUtils.streamCompound(compound.getList(TAG_COST_ITEMS, Tag.TAG_COMPOUND))
                       .map(itemCompound -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemCompound.getString(TAG_COST_ITEM))))
                       .toList();
        this.count = compound.getInt(TAG_COST_COUNT);
    }

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        final ListTag itemList = this.items.stream().map(item -> {
            final CompoundTag itemCompound = new CompoundTag();
            itemCompound.putString(TAG_COST_ITEM, BuiltInRegistries.ITEM.getKey(item).toString());
            return itemCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_COST_ITEMS, itemList);
        compound.putInt(TAG_COST_COUNT, this.count);
    }

    @Override
    public void serialize(final @NotNull RegistryFriendlyByteBuf buf)
    {
        buf.writeInt(this.count);
        buf.writeInt(this.items.size());
        for (final Item item : this.items)
        {
            buf.writeById(BuiltInRegistries.ITEM::getIdOrThrow, item);
        }
    }

    @Override
    public void deserialize(final @NotNull RegistryFriendlyByteBuf buf)
    {
        this.count = buf.readInt();
        this.items = new ArrayList<>();
        final int itemCount = buf.readInt();
        for (int i = 0; i < itemCount; i++)
        {
            this.items.add(buf.readById(BuiltInRegistries.ITEM::byIdOrThrow));
        }
    }

    @Override
    public boolean hasCorrectJsonFields(final JsonObject jsonObject)
    {
        return jsonObject.has(RESEARCH_ITEM_NAME_PROP)
                 && jsonObject.get(RESEARCH_ITEM_NAME_PROP).isJsonObject()
                 && jsonObject.getAsJsonObject(RESEARCH_ITEM_NAME_PROP).has(RESEARCH_ITEM_LIST_PROP);
    }

    @Override
    public void parseFromJson(final JsonObject jsonObject)
    {
        this.items = new ArrayList<>();
        for (JsonElement arrayItem : jsonObject.getAsJsonObject(RESEARCH_ITEM_NAME_PROP).getAsJsonArray(RESEARCH_ITEM_LIST_PROP))
        {
            this.items.add(BuiltInRegistries.ITEM.get(ResourceLocation.parse(arrayItem.getAsJsonPrimitive().getAsString())));
        }
        this.count = GlobalResearch.parseItemCount(jsonObject);
    }

    @Override
    public int hashCode()
    {
        int result = this.count;
        result = 31 * result + this.items.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ListItemCost listItemCost = (ListItemCost) o;

        if (this.count != listItemCost.count)
        {
            return false;
        }
        return this.items.equals(listItemCost.items);
    }
}
