package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Milk Bread item, made by the baker, with milk bucket effect
 */
public class ItemMilkyBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static FoodProperties milkBread = (new FoodProperties.Builder())
                                        .nutrition(5)
                                        .saturationMod(0.6F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Milk Bread item.
     *
     * @param properties the properties.
     */
    public ItemMilkyBread(final Properties properties)
    {
        super("milky_bread", properties.stacksTo(STACKSIZE).tab(ModCreativeTabs.MINECOLONIES).food(milkBread));
    }

   /**
    * Remove the potion effects like Milk
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isClientSide)
        {
            entityLiving.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
        }

        return super.finishUsingItem(stack, worldIn, entityLiving);
    }    

    @Override
    public void appendHoverText(
    @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_MILKY_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
