package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent tools inside the request system.
 */
public class Tool
{

    @NotNull
    private final String toolClass;

    @NotNull
    private final Integer minLevel;

    @NotNull
    private final Integer maxLevel;

    @NotNull
    private final ItemStack result;

    public Tool(@NotNull String toolClass, @NotNull Integer minLevel, @NotNull Integer maxLevel)
    {
        this(toolClass, minLevel, maxLevel, ItemStackUtils.EMPTY);
    }

    public Tool(@NotNull String toolClass, @NotNull Integer minLevel, @NotNull Integer maxLevel, @NotNull ItemStack result)
    {
        this.toolClass = toolClass;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.result = result;
    }

    /**
     * Returns the tool class that is requested.
     *
     * @return The tool class that is requested.
     */
    @NotNull
    public String getToolClass()
    {
        return toolClass;
    }

    /**
     * The minimal tool level requested.
     *
     * @return The minimal tool level requested.
     */
    @NotNull
    public Integer getMinLevel()
    {
        return minLevel;
    }

    /**
     * The maximum tool level requested.
     *
     * @return The maximum tool level requested.
     */
    @NotNull
    public Integer getMaxLevel()
    {
        return maxLevel;
    }

    /**
     * The resulting stack if set during creation, else ItemStack.Empty.
     *
     * @return The resulting stack.
     */
    @NotNull
    public ItemStack getResult()
    {
        return result;
    }
}
