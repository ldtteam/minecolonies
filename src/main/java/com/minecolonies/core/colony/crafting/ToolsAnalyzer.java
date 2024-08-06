package com.minecolonies.core.colony.crafting;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * Utility helpers for analyzing the available tools and determining which levels are able to use them.
 */
public final class ToolsAnalyzer
{
    /**
     * Generate the list of {@link ToolUsage}.
     */
    @NotNull
    public static List<ToolUsage> findTools(final Level level)
    {
        final Map<ToolType, ToolUsage> toolItems = new HashMap<>();

        for (final ItemStack stack : IColonyManager.getInstance().getCompatibilityManager().getListOfAllItems())
        {
            for (final ToolType tool : ToolType.values())
            {
                if (tool == ToolType.NONE || !ItemStackUtils.isTool(stack, tool)) { continue; }

                tryAddingToolWithLevel(toolItems, tool, stack);

                if (stack.isEnchantable())
                {
                    for (int enchantLevel = 1; enchantLevel < 4; ++enchantLevel)
                    {
                        tryAddingEnchantedTool(toolItems, tool, stack, enchantLevel, level);
                    }
                }
            }
        }

        return toolItems.values().stream().sorted(Comparator.comparing(ToolUsage::tool)).toList();
    }

    private static void tryAddingEnchantedTool(@NotNull final Map<ToolType, ToolUsage> toolItems,
                                               @NotNull final ToolType tool,
                                               @NotNull final ItemStack stack,
                                               final int enchantLevel, final Level level)
    {
        final ItemStack enchantedStack = stack.copy();

        // this list should theoretically end up applying a total of two enchants to each tool type
        tryEnchantStack(enchantedStack, Utils.getRegistryValue(Enchantments.UNBREAKING, level), enchantLevel);
        tryEnchantStack(enchantedStack, Utils.getRegistryValue(Enchantments.LOOTING, level), enchantLevel);
        tryEnchantStack(enchantedStack, Utils.getRegistryValue(Enchantments.FLAME, level), enchantLevel);
        tryEnchantStack(enchantedStack, Utils.getRegistryValue(Enchantments.FORTUNE, level), enchantLevel);
        tryEnchantStack(enchantedStack, Utils.getRegistryValue(Enchantments.PROTECTION, level), enchantLevel);
        tryEnchantStack(enchantedStack, Utils.getRegistryValue(Enchantments.LURE, level), enchantLevel);

        tryAddingToolWithLevel(toolItems, tool, enchantedStack);
    }

    private static void tryEnchantStack(@NotNull final ItemStack stack,
                                        @NotNull final Holder<Enchantment> enchantment,
                                        final int enchantLevel)
    {
        if (enchantment.value().canEnchant(stack) && enchantLevel >= enchantment.value().getMinLevel() && enchantLevel <= enchantment.value().getMaxLevel())
        {
            stack.enchant(enchantment, enchantLevel);
        }
    }

    private static void tryAddingToolWithLevel(@NotNull final Map<ToolType, ToolUsage> toolItems,
                                               @NotNull final ToolType tool,
                                               @NotNull final ItemStack stack)
    {
        int level = ItemStackUtils.getMiningLevel(stack, tool);
        if (level < 0) {
            return;
        }
        level = Math.min(MAX_BUILDING_LEVEL, level + ItemStackUtils.getMaxEnchantmentLevel(stack));

        final ToolUsage usage = toolItems.computeIfAbsent(tool, ToolUsage::create);

        if (stack.isEnchanted())
        {
            usage.enchantedToolLevels().get(level).add(stack);
        }
        else
        {
            usage.toolLevels().get(level).add(stack);
        }
    }
}
