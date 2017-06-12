package com.minecolonies.api.colony.requestsystem.requestable;

import com.minecolonies.api.util.ItemStackUtils;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Class used to represent Weapons Inside the request system.
 */
public class Weapon
{

    @NotNull
    private final WeaponType type;

    @NotNull
    private final Integer minLevel;

    @NotNull
    private final Integer maxLevel;

    @NotNull
    private final ItemStack result;

    public Weapon(@NotNull final WeaponType type, @NotNull final Integer minLevel, @NotNull final Integer maxLevel)
    {
        this(type, minLevel, maxLevel, ItemStackUtils.EMPTY);
    }

    public Weapon(@NotNull final WeaponType type, @NotNull final Integer minLevel, @NotNull final Integer maxLevel, @NotNull final ItemStack result)
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
    private WeaponType getType()
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
     * The resulting stack if set during creation, else ItemStackUtils.EMPTY.
     *
     * @return The resulting stack.
     */
    @NotNull
    public ItemStack getResult()
    {
        return result;
    }
}
