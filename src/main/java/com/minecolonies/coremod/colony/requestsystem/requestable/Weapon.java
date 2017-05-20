package com.minecolonies.coremod.colony.requestsystem.requestable;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent Weapons Inside the request system.
 */
public class Weapon
{

    public enum Type
    {
        SWORD,
        BOW
    }

    @NotNull
    private final Type type;

    @NotNull
    private final Integer minLevel;

    @NotNull
    private final Integer maxLevel;

    @NotNull
    private final ItemStack result;

    public Weapon(@NotNull final Type type, @NotNull final Integer minLevel, @NotNull final Integer maxLevel)
    {
        this(type, minLevel, maxLevel, ItemStack.EMPTY);
    }

    public Weapon(@NotNull final Type type, @NotNull final Integer minLevel, @NotNull final Integer maxLevel, @NotNull final ItemStack result)
    {
        this.type = type;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.result = result;
    }

    /**
     * The weapon type of the Weapon requested
     *
     * @return The weapon type of the weapon requested.
     */
    @NotNull
    private Type getType()
    {
        return type;
    }

    /**
     * The minimal weapon level requested.
     *
     * @return The minimal weapon level requested.
     */
    @NotNull
    public Integer getMinLevel()
    {
        return minLevel;
    }

    /**
     * The maximum weapon level requested.
     *
     * @return The maximum weapon level requested.
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
