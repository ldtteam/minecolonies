package com.minecolonies.coremod.research.costs;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.costs.IResearchCost;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.research.ModResearchCosts.TAG_ITEM_COST_ID;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_COST_COUNT;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_COST_TAG;
import static com.minecolonies.coremod.research.GlobalResearch.RESEARCH_ITEM_NAME_PROP;
import static com.minecolonies.coremod.research.GlobalResearch.RESEARCH_ITEM_TAG_PROP;

/**
 * A plain item cost that takes a list of several items that have to be fulfilled.
 */
public class TagItemCost implements IResearchCost
{
    /**
     * The count of items.
     */
    private int count;

    /**
     * The tag which contains all possible items.
     */
    private TagKey<Item> tag;

    /**
     * Default constructor.
     */
    public TagItemCost()
    {
    }

    @Override
    public ResourceLocation getId()
    {
        return TAG_ITEM_COST_ID;
    }

    @Override
    public int getCount()
    {
        return this.count;
    }

    @Override
    public List<Item> getItems()
    {
        return ForgeRegistries.ITEMS.tags().getTag(this.tag).stream().toList();
    }

    @Override
    public Component getTranslatedName()
    {
        return Component.translatable(String.format("com.minecolonies.coremod.research.tags.%s", this.tag.location()));
    }

    @Override
    public void read(final @NotNull CompoundTag compound)
    {
        this.count = compound.getInt(TAG_COST_COUNT);
        this.tag = ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(compound.getString(TAG_COST_TAG)));
    }

    @Override
    public void write(final @NotNull CompoundTag compound)
    {
        compound.putInt(TAG_COST_COUNT, this.count);
        compound.putString(TAG_COST_TAG, this.tag.location().toString());
    }

    @Override
    public boolean hasCorrectJsonFields(final JsonObject jsonObject)
    {
        return jsonObject.getAsJsonObject().has(RESEARCH_ITEM_NAME_PROP)
                 && jsonObject.getAsJsonObject().get(RESEARCH_ITEM_NAME_PROP).isJsonObject()
                 && jsonObject.getAsJsonObject().getAsJsonObject(RESEARCH_ITEM_NAME_PROP).has(RESEARCH_ITEM_TAG_PROP);
    }

    @Override
    public void parseFromJson(final JsonObject jsonObject)
    {
        final String tagKey = jsonObject.getAsJsonObject(RESEARCH_ITEM_NAME_PROP).getAsJsonPrimitive(RESEARCH_ITEM_TAG_PROP).getAsString();
        this.tag = ForgeRegistries.ITEMS.tags().createTagKey(new ResourceLocation(tagKey));
        this.count = CostUtils.getCount(jsonObject);
    }

    @Override
    public int hashCode()
    {
        int result = this.count;
        result = 31 * result + this.tag.hashCode();
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

        final TagItemCost that = (TagItemCost) o;

        if (this.count != that.count)
        {
            return false;
        }
        return this.tag.equals(that.tag);
    }
}
