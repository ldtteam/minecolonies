package com.minecolonies.core.items;

import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.core.util.TeleportHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Chorus Bread, made by the baker. Teleports user to surface.
 */
public class ItemChorusBread extends ItemFood
{
    /**
     * Sets the name, creative tab, and registers the Chorus Bread item.
     */
    public ItemChorusBread()
    {
        super(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(2.0F).alwaysEdible().build()), 2);
    }

    /**
     * Teleport to the surface.
     */
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving)
    {
        if (!worldIn.isClientSide && entityLiving instanceof ServerPlayer && WorldUtil.isOverworldType(worldIn))
        {
            TeleportHelper.surfaceTeleport((ServerPlayer) entityLiving);
        }

        return super.finishUsingItem(stack, worldIn, entityLiving);
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final TooltipContext ctx, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint = Component.translatableEscape(TranslationConstants.COM_MINECOLONIES_COREMOD_CHORUS_BREAD_TOOLTIP_GUI);
        guiHint.setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY));
        tooltip.add(guiHint);

        super.appendHoverText(stack, ctx, tooltip, flagIn);
    }
}
