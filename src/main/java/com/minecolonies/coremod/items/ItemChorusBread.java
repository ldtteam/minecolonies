package com.minecolonies.coremod.items;

import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.util.TeleportHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import static com.minecolonies.api.util.constant.Constants.STACKSIZE;
import java.util.List;

/**
 * Chorus Bread, made by the baker. Teleports user to surface.
 */
public class ItemChorusBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food chorusBread = (new Food.Builder())
                                        .hunger(5)
                                        .saturation(2.0F)
                                        .setAlwaysEdible()
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Chorus Bread item.
     *
     * @param properties the properties.
     */
    public ItemChorusBread(final Properties properties)
    {
        super("chorus_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(chorusBread));
    }

   /**
    * Teleport to the surface. 
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving)
    {
        if (!worldIn.isRemote && entityLiving instanceof ServerPlayerEntity && worldIn.getDimensionType().isSame(DimensionType.OVERWORLD_TYPE))
        {
            TeleportHelper.surfaceTeleport((ServerPlayerEntity)entityLiving);
        }

        return super.onItemUseFinish(stack, worldIn, entityLiving);
    }

    @Override
    public void addInformation(
    @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_CHORUS_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.setFormatting(TextFormatting.GRAY));
        tooltip.add(guiHint);

        final IFormattableTextComponent getHint = LanguageHandler.buildChatComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_BREAD_AVAILABLE_TOOLTIP_GUI);
        getHint.setStyle(Style.EMPTY.setItalic(true).setFormatting(TextFormatting.GRAY));
        tooltip.add(getHint);

        super.addInformation(stack, worldIn, tooltip, flagIn);
    }
}
