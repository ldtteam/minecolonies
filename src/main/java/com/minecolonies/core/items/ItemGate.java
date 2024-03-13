package com.minecolonies.core.items;

import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
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
      final Properties properties)
    {
        super(block, properties);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final Level worldIn, @NotNull final List<Component> tooltip, @NotNull final TooltipFlag flagIn)
    {
        final MutableComponent guiHint2 = Component.translatableEscape(TranslationConstants.GATE_PLACEMENT_TOOLTIP);
        guiHint2.setStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA));
        tooltip.add(guiHint2);
    }
}
