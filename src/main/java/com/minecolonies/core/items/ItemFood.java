package com.minecolonies.core.items;

import com.minecolonies.api.items.IMinecoloniesFoodItem;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
     * Creates a new food item.
     *
     * @param builder the item properties to use.
     */
    public ItemFood(@NotNull final Properties builder, final String producer)
    {
        super(builder);
        this.producer = producer;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        tooltip.add(Component.translatable(TranslationConstants.FOOD_TOOLTIP + "." + this.producer));
    }
}
