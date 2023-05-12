package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item for gates
 */
public class ItemGate extends BlockItem
{
    public ItemGate(
      @NotNull final String name,
      final Block block,
      @NotNull final CreativeModeTab tab,
      final Properties properties)
    {
        super(block, properties.tab(tab));
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint2 = Component.translatable(TranslationConstants.GATE_PLACEMENT_TOOLTIP);
        guiHint2.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
        tooltip.add(guiHint2);
    }
}
