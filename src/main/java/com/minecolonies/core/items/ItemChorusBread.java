package com.minecolonies.core.items;

import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Chorus Bread, made by the baker. Teleports user to surface.
 */
public class ItemChorusBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static FoodProperties chorusBread = (new FoodProperties.Builder())
                                        .nutrition(5)
                                        .saturationMod(2.0F)
                                        .alwaysEat()
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Chorus Bread item.
     *
     * @param properties the properties.
     */
    public ItemChorusBread(final Properties properties)
    {
        super("chorus_bread", properties.stacksTo(STACKSIZE).food(chorusBread));
    }

   /**
    * Teleport to the surface. 
    */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving)
    {
        if (!worldIn.isClientSide && entityLiving instanceof ServerPlayer && WorldUtil.isOverworldType(worldIn))
        {
            TeleportHelper.surfaceTeleport((ServerPlayer)entityLiving);
        }

        return super.finishUsingItem(stack, worldIn, entityLiving);
    }

    @Override
    public void appendHoverText(
    @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatable(TranslationConstants.COM_MINECOLONIES_COREMOD_CHORUS_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }
}
