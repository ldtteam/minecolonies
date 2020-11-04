package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import java.util.List;

/**
 * Sweet Bread, made by the baker. Adds speed, removes poison
 */
public class ItemSugaryBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food sweetBread = (new Food.Builder())
                                        .hunger(6)
                                        .saturation(0.7F)
                                        .effect(() -> new EffectInstance(Effects.SPEED, 600), 1.0F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Sweet Bread item.
     *
     * @param properties the properties.
     */
    public ItemSugaryBread(final Properties properties)
    {
        super("sugary_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(sweetBread));
    }

   /**
    * Remove the poison effect
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {

        if (!worldIn.isRemote)
        {
            entityLiving.removePotionEffect(Effects.POISON);
        }
  
        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }    
    
    @Override
    public void addInformation(
    @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final ITextComponent guiHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_SUGARY_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(new Style().setColor(TextFormatting.GRAY));
        tooltip.add(guiHint);

        final ITextComponent getHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_BREAD_AVAILABLE_TOOLTIP_GUI);
        getHint.setStyle(new Style().setItalic(true).setColor(TextFormatting.GRAY));
        tooltip.add(getHint);

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
