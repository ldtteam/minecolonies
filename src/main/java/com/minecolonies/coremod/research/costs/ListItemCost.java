package com.minecolonies.coremod.research.costs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.api.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.research.ModResearchCosts.LIST_ITEM_COST_ID;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.coremod.research.GlobalResearch.RESEARCH_ITEM_LIST_PROP;
import static com.minecolonies.coremod.research.GlobalResearch.RESEARCH_ITEM_NAME_PROP;

/**
 * A plain item cost that takes a list of several items that have to be fulfilled.
 */
public class ListItemCost implements IResearchCost
{
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
    public ListItemCost()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return LIST_ITEM_COST_ID;
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
                       .map(itemCompound -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemCompound.getString(TAG_COST_ITEM))))
                       .toList();
        this.count = compound.getInt(TAG_COST_COUNT);
    }

    @Override
    public void write(@NotNull final CompoundTag compound)
    {
        final ListTag itemList = this.items.stream().map(item -> {
            final CompoundTag itemCompound = new CompoundTag();
            itemCompound.putString(TAG_COST_ITEM, ForgeRegistries.ITEMS.getKey(item).toString());
            return itemCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_COST_ITEMS, itemList);
        compound.putInt(TAG_COST_COUNT, this.count);
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
            this.items.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(arrayItem.getAsJsonPrimitive().getAsString())));
        }
        this.count = CostUtils.getCount(jsonObject);
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
