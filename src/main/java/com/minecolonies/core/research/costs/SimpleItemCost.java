package com.minecolonies.core.research.costs;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.ModResearchCostTypes.ResearchCostType;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.core.research.GlobalResearch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.*;
import static com.minecolonies.core.research.GlobalResearch.RESEARCH_ITEM_NAME_PROP;

/**
 * A simple item cost which contains a singular item.
 */
public class SimpleItemCost implements IResearchCost
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
     * The item.
     */
    private Item item;

    /**
     * Default constructor.
     */
    public SimpleItemCost(final ResearchCostType type)
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
            this.item = BuiltInRegistries.ITEM.get(new ResourceLocation(compound.getString(TAG_COST_ITEM)));
            this.count = compound.getInt(TAG_COST_COUNT);
        }
        else
        {
            // Migration code
            String[] costParts = compound.getString(TAG_COST_ITEM).split(":");
            if (costParts.length == 3)
            {
                final ItemStack is = new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation(costParts[0], costParts[1])));
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
        compound.putString(TAG_COST_ITEM, BuiltInRegistries.ITEM.getKey(this.item).toString());
        compound.putInt(TAG_COST_COUNT, this.count);
    }

    @Override
    public void serialize(final @NotNull FriendlyByteBuf buf)
    {
        buf.writeInt(this.count);
        buf.writeById(BuiltInRegistries.ITEM::getIdOrThrow, this.item);
    }

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        this.count = buf.readInt();
        this.item = buf.readById(BuiltInRegistries.ITEM::byIdOrThrow);
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
        this.item = BuiltInRegistries.ITEM.get(new ResourceLocation(jsonObject.getAsJsonPrimitive(RESEARCH_ITEM_NAME_PROP).getAsString()));
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
