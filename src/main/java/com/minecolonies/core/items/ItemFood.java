package com.minecolonies.core.items;

import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A custom item class for food items.
 */
public class ItemFood extends Item implements IMinecoloniesFoodItem
{
    /**
     * The job producing this.
     */
    private final String producer;

    /**
     * The food tier.
     */
    private final int tier;

    /**
     * A list of food tags for the item.
     */
    private final List<TagKey<Item>> tags;

    /**
     * Creates a new food item.
     *
     * @param builder the item properties to use.
     * @param producer the key for the worker that produces it.
     * @param tier the nutrition tier.
     */
    public ItemFood(@NotNull final Properties builder, final String producer, final int tier, final List<TagKey<Item>> tags)
    {
        super(builder);
        this.producer = producer;
        this.tier = tier;
        this.tags = tags;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable(TranslationConstants.FOOD_TOOLTIP + this.producer));
        tooltip.add(Component.translatable(TranslationConstants.TIER_TOOLTIP + this.tier));
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }

    /**
     * Get all food tags for the given item.
     *
     * @return the list of food tags.
     */
    public List<TagKey<Item>> getTags()
    {
        return tags;
    }
}
