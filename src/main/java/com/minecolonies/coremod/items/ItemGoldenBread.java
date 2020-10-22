package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.util.text.IFormattableTextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import java.util.List;

/**
 * Golden Bread, made by the Baker. Heals 2 hearts 
 */
public class ItemGoldenBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food goldenBread = (new Food.Builder())
                                        .hunger(5)
                                        .saturation(0.6F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Golden Bread item.
     *
     * @param properties the properties.
     */
    public ItemGoldenBread(final Properties properties)
    {
        super("golden_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(goldenBread));
    }

   /**
    * Heal 2 hearts
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isRemote)
        {
            entityLiving.heal(4);
        }

        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }    

    @Override
    public void addInformation(
    @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_GOLDEN_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY));
        tooltip.add(guiHint);

        final IFormattableTextComponent getHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_BREAD_AVAILABLE_TOOLTIP_GUI);
        getHint.setStyle(Style.EMPTY.setItalic(true).setFormatting(TextFormatting.GRAY));
        tooltip.add(getHint);

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
