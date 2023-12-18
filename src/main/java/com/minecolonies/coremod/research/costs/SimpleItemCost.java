package com.minecolonies.coremod.research.costs;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.coremod.research.GlobalResearch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.research.ModResearchCosts.SIMPLE_ITEM_COST_ID;
import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.coremod.research.GlobalResearch.RESEARCH_ITEM_NAME_PROP;

/**
 * A simple item cost which contains a singular item.
 */
public class SimpleItemCost implements IResearchCost
{
    /**
     * The count of items.
     */
    private int count;

    /**
     * The item.
     */
    private Item item;

    /**
     * Default constructor.
     */
    public SimpleItemCost()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return SIMPLE_ITEM_COST_ID;
    }

    @Override
    public int getCount()
    {
        return count;
    }

    @Override
    public List<Item> getItems()
    {
        return List.of(item);
    }

    @Override
    public void read(final @NotNull CompoundTag compound)
    {
        if (compound.contains(TAG_COST_COUNT))
        {
            this.item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(compound.getString(TAG_COST_ITEM)));
            this.count = compound.getInt(TAG_COST_COUNT);
        }
        else
        {
            // Migration code
            String[] costParts = compound.getString(TAG_COST_ITEM).split(":");
            if (costParts.length == 3)
            {
                final ItemStack is = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(costParts[0], costParts[1])));
                if (compound.contains(TAG_COST_NBT))
                {
                    is.setTag(compound.getCompound(TAG_COST_NBT));
                }

                this.item = is.getItem();
                this.count = Integer.parseInt(costParts[2]);
            }
        }
    }

    @Override
    public void write(final @NotNull CompoundTag compound)
    {
        compound.putString(TAG_COST_ITEM, ForgeRegistries.ITEMS.getKey(this.item).toString());
        compound.putInt(TAG_COST_COUNT, this.count);
    }

    @Override
    public boolean hasCorrectJsonFields(final JsonObject jsonObject)
    {
        return jsonObject.has(RESEARCH_ITEM_NAME_PROP)
                 && jsonObject.get(RESEARCH_ITEM_NAME_PROP).isJsonPrimitive()
                 && jsonObject.getAsJsonPrimitive(RESEARCH_ITEM_NAME_PROP).isString();
    }

    @Override
    public void parseFromJson(final JsonObject jsonObject)
    {
        this.item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(jsonObject.getAsJsonPrimitive(RESEARCH_ITEM_NAME_PROP).getAsString()));
        this.count = GlobalResearch.parseItemCount(jsonObject);
    }

    @Override
    public int hashCode()
    {
        int result = this.count;
        result = 31 * result + this.item.hashCode();
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

        final SimpleItemCost that = (SimpleItemCost) o;

        if (this.count != that.count)
        {
            return false;
        }
        return this.item.equals(that.item);
    }
}
