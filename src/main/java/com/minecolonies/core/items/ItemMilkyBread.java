package com.minecolonies.core.items;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.EffectCures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Milk Bread item, made by the baker, with milk bucket effect
 */
public class ItemMilkyBread extends Item
{

    /**
     * Setup the food definition
     */
    private static FoodProperties milkBread = (new FoodProperties.Builder())
                                        .nutrition(5)
                                        .saturationModifier(0.6F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Milk Bread item.
     *
     * @param properties the properties.
     */
    public ItemMilkyBread(final Properties properties)
    {
        super(properties.stacksTo(STACKSIZE).food(milkBread));
    }

   /**
    * Remove the potion effects like Milk
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isClientSide)
        {
            entityLiving.removeEffectsCuredBy(EffectCures.MILK);
        }

        return super.finishUsingItem(stack, worldIn, entityLiving);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_COREMOD_MILKY_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, ctx, tooltip, flagIn);
    }
}
