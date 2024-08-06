package com.minecolonies.core.research.costs;

import com.google.gson.JsonObject;
import com.minecolonies.api.research.ModResearchCostTypes.ResearchCostType;
import com.minecolonies.api.research.costs.IResearchCost;
import com.minecolonies.core.research.GlobalResearch;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.TAG_COST_COUNT;
import static com.minecolonies.api.research.util.ResearchConstants.TAG_COST_TAG;
import static com.minecolonies.core.research.GlobalResearch.RESEARCH_ITEM_NAME_PROP;
import static com.minecolonies.core.research.GlobalResearch.RESEARCH_ITEM_TAG_PROP;

/**
 * A plain item cost that takes a list of several items that have to be fulfilled.
 */
public class TagItemCost implements IResearchCost
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
     * The tag which contains all possible items.
     */
    private TagKey<Item> tag;

    /**
     * Default constructor.
     */
    public TagItemCost(final ResearchCostType type)
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
        return BuiltInRegistries.ITEM.getTag(this.tag).map(holders -> holders.stream().map(Holder::value).toList()).orElseGet(Collections::emptyList);
    }

    @Override
    public Component getTranslatedName()
    {
        return Component.translatableEscape(String.format("com.minecolonies.coremod.research.tags.%s", this.tag.location()));
    }

    @Override
    public void read(final @NotNull CompoundTag compound)
    {
        this.count = compound.getInt(TAG_COST_COUNT);
        this.tag = TagKey.create(Registries.ITEM, new ResourceLocation(compound.getString(TAG_COST_TAG)));
    }

    @Override
    public void write(final @NotNull CompoundTag compound)
    {
        compound.putInt(TAG_COST_COUNT, this.count);
        compound.putString(TAG_COST_TAG, this.tag.location().toString());
    }

    @Override
    public void serialize(final @NotNull FriendlyByteBuf buf)
    {
        buf.writeInt(this.count);
        buf.writeResourceLocation(this.tag.location());
    }

    @Override
    public void deserialize(final @NotNull FriendlyByteBuf buf)
    {
        this.count = buf.readInt();
        this.tag = TagKey.create(Registries.ITEM, buf.readResourceLocation());
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
        this.tag = TagKey.create(Registries.ITEM, new ResourceLocation(tagKey));
        this.count = GlobalResearch.parseItemCount(jsonObject);
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
