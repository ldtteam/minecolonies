package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Golden Bread, made by the Baker. Heals 2 hearts 
 */
public class ItemGoldenBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food goldenBread = (new Food.Builder())
                                        .nutrition(5)
                                        .saturationMod(0.6F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Golden Bread item.
     *
     * @param properties the properties.
     */
    public ItemGoldenBread(final Properties properties)
    {
        super("golden_bread", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES).food(goldenBread));
    }

   /**
    * Heal 2 hearts
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isClientSide)
        {
            entityLiving.heal(4);
        }

        return super.finishUsingItem(stack, worldIn, entityLiving);
    }    

    @Override
    public void appendHoverText(
    @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_GOLDEN_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(TextFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
