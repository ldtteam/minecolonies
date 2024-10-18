package com.minecolonies.core.colony.expeditions.colony.types;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

/**
 * The expedition difficulty.
 */
public enum ColonyExpeditionTypeDifficulty
{
    EASY("easy", 1, Items.IRON_SWORD, false, Style.EMPTY, 30, 5, 1, 1f),
    MEDIUM("medium", 2, Items.IRON_SWORD, false, Style.EMPTY, 45, 10, 2, 1.2f),
    HARD("hard", 3, Items.IRON_SWORD, false, Style.EMPTY, 60, 15, 3, 1.5f),
    NIGHTMARE("nightmare", 4, Items.NETHERITE_SWORD, true, Style.EMPTY.withColor(ChatFormatting.DARK_RED).withItalic(true), 120, 30, 4, 2f);

    /**
     * The key of the difficulty, used in the json files.
     */
    private final String key;

    /**
     * The level of the difficulty.
     */
    private final int level;

    /**
     * The sword item which should be rendered for the difficulty icons.
     */
    private final Item icon;

    /**
     * Whether the icon should by default be hidden, only shown if the difficulty is selected.
     */
    private final boolean hidden;

    /**
     * The style for the hover pane to display.
     */
    private final Style style;

    /**
     * The base amount of ticks the expedition will take.
     */
    private final int baseTime;

    /**
     * The amount of random time that can be added/removed from the base time.
     */
    private final int randomTime;

    /**
     * A multiplier that will spawn more mobs during encounters.
     */
    private final int mobEncounterMultiplier;

    /**
     * A multiplier that will increase the damage for mobs during encounters.
     */
    private final float mobDamageMultiplier;

    /**
     * Internal constructor.
     */
    ColonyExpeditionTypeDifficulty(
      final String key,
      final int level,
      final Item icon,
      final boolean hidden,
      final Style style,
      final int baseTime,
      final int randomTime,
      final int mobEncounterMultiplier,
      final float mobDamageMultiplier)
    {
        this.key = key;
        this.level = level;
        this.icon = icon;
        this.hidden = hidden;
        this.style = style;
        this.baseTime = baseTime;
        this.randomTime = randomTime;
        this.mobEncounterMultiplier = mobEncounterMultiplier;
        this.mobDamageMultiplier = mobDamageMultiplier;
    }

    /**
     * Get the difficulty from its key value.
     *
     * @param key the input key.
     * @return the difficulty, or none if the key is incorrect.
     */
    @Nullable
    public static ColonyExpeditionTypeDifficulty fromKey(final String key)
    {
        for (final ColonyExpeditionTypeDifficulty item : ColonyExpeditionTypeDifficulty.values())
        {
            if (item.getKey().equals(key))
            {
                return item;
            }
        }
        return null;
    }

    /**
     * Get the key for this difficulty instance.
     *
     * @return the key for the difficulty.
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Get the level of the expedition.
     *
     * @return the level.
     */
    public int getLevel()
    {
        return level;
    }

    /**
     * Get the icon of the expedition difficulty.
     *
     * @return the item.
     */
    public Item getIcon()
    {
        return icon;
    }

    /**
     * Whether this difficulty is hidden.
     *
     * @return true if so.
     */
    public boolean isHidden()
    {
        return hidden;
    }

    /**
     * Get the style for the hover pane to display.
     *
     * @return the custom style.
     */
    public Style getStyle()
    {
        return style;
    }

    /**
     * Get the base amount of ticks the expedition will take.
     *
     * @return the amount of time.
     */
    public int getBaseTime()
    {
        return baseTime;
    }

    /**
     * Get the amount of random time that can be added/removed from the base time.
     *
     * @return the amount of time.
     */
    public int getRandomTime()
    {
        return randomTime;
    }

    /**
     * Get a multiplier that will spawn more mobs during encounters.
     *
     * @return the multiplier.
     */
    public int getMobEncounterMultiplier()
    {
        return mobEncounterMultiplier;
    }

    /**
     * Get a multiplier that will increase the damage for mobs during encounters.
     *
     * @return the multiplier.
     */
    public float getMobDamageMultiplier()
    {
        return mobDamageMultiplier;
    }
}
