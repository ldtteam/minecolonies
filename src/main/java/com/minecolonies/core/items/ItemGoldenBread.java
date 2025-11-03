package com.minecolonies.core.items;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Golden Bread, made by the Baker. Heals 2 hearts 
 */
public class ItemGoldenBread extends ItemFood
{
    /**
     * Sets the name, creative tab, and registers the Golden Bread item.
     *
     */
    public ItemGoldenBread(final Properties properties)
    {
        super(properties, 1);
    }

   /**
    * Heal 2 hearts
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isClientSide)
        {
            entityLiving.heal(4);
        }

        return super.finishUsingItem(stack, worldIn, entityLiving);
    }    

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_COREMOD_GOLDEN_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, ctx, tooltip, flagIn);
    }
}
