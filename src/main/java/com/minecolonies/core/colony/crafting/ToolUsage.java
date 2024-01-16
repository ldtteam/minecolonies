package com.minecolonies.core.colony.crafting;

import com.minecolonies.api.util.constant.ToolType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MAX_BUILDING_LEVEL;

/**
 * Describes a tool and its level compatibility.
 *
 * @param tool                the tool type
 * @param toolLevels          basic items accepted at each building level.
 * @param enchantedToolLevels (some) enchanted items accepted at each building level.
 */
public record ToolUsage(@NotNull ToolType tool,
                        @NotNull List<List<ItemStack>> toolLevels,
                        @NotNull List<List<ItemStack>> enchantedToolLevels)
{
    @NotNull
    public static ToolUsage create(@NotNull final ToolType tool)
    {
        final List<List<ItemStack>> basicLevels = new ArrayList<>();
        final List<List<ItemStack>> enchantedLevels = new ArrayList<>();
        for (int i = 0; i <= MAX_BUILDING_LEVEL; ++i)
        {
            basicLevels.add(new ArrayList<>());
            enchantedLevels.add(new ArrayList<>());
        }
        return new ToolUsage(tool, basicLevels, enchantedLevels);
    }
}
