package com.minecolonies.api.research.costs;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.IResearchCost;
import com.minecolonies.api.research.ModResearchCosts;
import com.minecolonies.core.util.GsonHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.TAG_COST_COUNT;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_COST_ITEM;

/**
 * A simple item cost which contains a singular item.
 */
public class SimpleItemCost implements IResearchCost
{
    /**
     * The property name for items list.
     */
    private static final String JSON_PROP_ITEM = "item";

    /**
     * The property name for item quantity.
     */
    private static final String JSON_PROP_QUANTITY = "quantity";

    /**
     * The item.
     */
    private final Item item;

    /**
     * The count of items.
     */
    private final int count;

    /**
     * Create a simple item cost.
     *
     * @param compound the nbt containing the relevant data.
     */
    public SimpleItemCost(final CompoundTag compound)
    {
        this.item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString(TAG_COST_ITEM)));
        this.count = compound.getInt(TAG_COST_COUNT);
    }

    /**
     * Create a simple item cost.
     *
     * @param json the nbt containing the relevant data.
     */
    public SimpleItemCost(final JsonObject json)
    {
        this.item = ForgeRegistries.ITEMS.getValue(GsonHelper.getAsResourceLocation(json, JSON_PROP_ITEM));
        this.count = Math.max(GsonHelper.getAsInt(json, JSON_PROP_QUANTITY, 1), 1);
    }

    @Override
    public ModResearchCosts.ResearchCostEntry getType()
    {
        return ModResearchCosts.simpleItemCost.get();
    }

    @Override
    public List<Item> getItems()
    {
        return List.of(item);
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public CompoundTag writeToNBT()
    {
        final CompoundTag compound = new CompoundTag();
        compound.putString(TAG_COST_ITEM, ForgeRegistries.ITEMS.getKey(this.item).toString());
        compound.putInt(TAG_COST_COUNT, this.count);
        return compound;
    }
}
