package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
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
      @NotNull final ItemGroup tab,
      final Properties properties)
    {
        super(block, properties.tab(tab));

        // Registry name links to item model json file name
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + name);
    }

    @Override
    public void appendHoverText(
      @NotNull final ItemStack stack, @Nullable final World worldIn, @NotNull final List<ITextComponent> tooltip, @NotNull final ITooltipFlag flagIn)
    {
        final IFormattableTextComponent guiHint2 = new TranslationTextComponent(TranslationConstants.GATE_PLACEMENT_TOOLTIP);
        guiHint2.setStyle(Style.EMPTY.withColor(TextFormatting.DARK_AQUA));
        tooltip.add(guiHint2);
    }
}
