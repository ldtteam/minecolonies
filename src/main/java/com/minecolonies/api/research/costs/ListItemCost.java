package com.minecolonies.api.research.costs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecolonies.api.research.IResearchCost;
import com.minecolonies.api.research.ModResearchCosts;
import com.minecolonies.api.util.NBTUtils;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.*;

/**
 * A plain item cost that takes a list of several items that have to be fulfilled.
 */
public class ListItemCost implements IResearchCost
{
    /**
     * The property name for items list.
     */
    private static final String JSON_PROP_ITEMS = "items";

    /**
     * The property name for quantity.
     */
    private static final String JSON_PROP_QUANTITY = "quantity";

    /**
     * The list of items.
     */
    private final List<Item> items;

    /**
     * The count of items.
     */
    private final int count;

    /**
     * Create a list item cost.
     *
     * @param compound the nbt containing the relevant data.
     */
    public ListItemCost(final CompoundTag compound)
    {
        this.items = NBTUtils.streamCompound(compound.getList(TAG_COST_ITEMS, Tag.TAG_COMPOUND))
            .map(itemCompound -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemCompound.getString(TAG_COST_ITEM))))
            .toList();
        this.count = compound.getInt(TAG_COST_COUNT);
    }

    /**
     * Create a list item cost.
     *
     * @param json the json containing the relevant data.
     */
    public ListItemCost(final JsonObject json)
    {
        this.items = new ArrayList<>();
        for (final JsonElement arrayItem : GsonHelper.getAsJsonArray(json, JSON_PROP_ITEMS))
        {
            this.items.add(ForgeRegistries.ITEMS.getValue(new ResourceLocation(arrayItem.getAsJsonPrimitive().getAsString())));
        }
        this.count = Math.max(GsonHelper.getAsInt(json, JSON_PROP_QUANTITY, 1), 1);
    }

    @Override
    public ModResearchCosts.ResearchCostEntry getType()
    {
        return ModResearchCosts.listItemCost.get();
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
    public CompoundTag writeToNBT()
    {
        final CompoundTag compound = new CompoundTag();
        final ListTag itemList = this.items.stream().map(item -> {
            final CompoundTag itemCompound = new CompoundTag();
            itemCompound.putString(TAG_COST_ITEM, ForgeRegistries.ITEMS.getKey(item).toString());
            return itemCompound;
        }).collect(NBTUtils.toListNBT());
        compound.put(TAG_COST_ITEMS, itemList);
        compound.putInt(TAG_COST_COUNT, this.count);
        return compound;
    }
}
