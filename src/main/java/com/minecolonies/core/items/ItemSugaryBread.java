package com.minecolonies.core.items;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Sweet Bread, made by the baker. Adds speed, removes poison
 */
public class ItemSugaryBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static FoodProperties sweetBread = (new FoodProperties.Builder())
                                        .nutrition(6)
                                        .saturationMod(0.7F)
                                        .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600), 1.0F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Sweet Bread item.
     *
     * @param properties the properties.
     */
    public ItemSugaryBread(final Properties properties)
    {
        super("sugary_bread", properties.stacksTo(STACKSIZE).food(sweetBread));
    }

   /**
    * Remove the poison effect
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {

        if (!worldIn.isClientSide)
        {
            entityLiving.removeEffect(MobEffects.POISON);
        }
  
        return super.finishUsingItem(stack, worldIn, entityLiving);
    }    
    
    @Override
    public void appendHoverText(
    @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_COREMOD_SUGARY_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
