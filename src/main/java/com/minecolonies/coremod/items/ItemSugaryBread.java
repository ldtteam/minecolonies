package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
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
    private static Food sweetBread = (new Food.Builder())
                                        .nutrition(6)
                                        .saturationMod(0.7F)
                                        .effect(() -> new EffectInstance(Effects.MOVEMENT_SPEED, 600), 1.0F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Sweet Bread item.
     *
     * @param properties the properties.
     */
    public ItemSugaryBread(final Properties properties)
    {
        super("sugary_bread", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES).food(sweetBread));
    }

   /**
    * Remove the poison effect
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {

        if (!worldIn.isClientSide)
        {
            entityLiving.removeEffect(Effects.POISON);
        }
  
        return super.finishUsingItem(stack, worldIn, entityLiving);
    }    
    
    @Override
    public void appendHoverText(
    @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_SUGARY_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(TextFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
