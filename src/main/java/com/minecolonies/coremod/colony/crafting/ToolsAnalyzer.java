package com.minecolonies.coremod.colony.crafting;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.ItemStackUtils;
import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
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
    public static List<ToolUsage> findTools()
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
                        tryAddingEnchantedTool(toolItems, tool, stack, enchantLevel);
                    }
                }
            }
        }

        return toolItems.values().stream().sorted(Comparator.comparing(ToolUsage::tool)).toList();
    }

    private static void tryAddingEnchantedTool(@NotNull final Map<ToolType, ToolUsage> toolItems,
                                               @NotNull final ToolType tool,
                                               @NotNull final ItemStack stack,
                                               final int enchantLevel)
    {
        final ItemStack enchantedStack = stack.copy();

        // this list should theoretically end up applying a total of two enchants to each tool type
        tryEnchantStack(enchantedStack, Enchantments.UNBREAKING, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.MOB_LOOTING, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.FLAMING_ARROWS, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.BLOCK_FORTUNE, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.ALL_DAMAGE_PROTECTION, enchantLevel);
        tryEnchantStack(enchantedStack, Enchantments.FISHING_SPEED, enchantLevel);

        tryAddingToolWithLevel(toolItems, tool, enchantedStack);
    }

    private static void tryEnchantStack(@NotNull final ItemStack stack,
                                        @NotNull final Enchantment enchantment,
                                        final int enchantLevel)
    {
        if (enchantment.canEnchant(stack) && enchantLevel >= enchantment.getMinLevel() && enchantLevel <= enchantment.getMaxLevel())
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
