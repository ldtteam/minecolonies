package com.minecolonies.coremod.entity.ai.util;

import net.minecraft.item.Item;
import org.jetbrains.annotations.NotNull;

public class StudyItem
{
    /**
     * The actual item to use for studies
     */
    private final Item item;

    /**
     * The percent chance increase for skillups [100-1000]
     */
    private final int skillIncrease;

    /**
     * The percent chance to break upon skillup-try [0-100]
     */
    private final int breakChance;

    /**
     * Inventory slot the item is found in, set for specific use
     */
    private int slot = -1;

    /**
     * Initializing Study item, making sure the rates are valid.
     */
    public StudyItem(@NotNull final Item item, final int skillIncrease, final int breakChance)
    {
        this.item = item;
        this.skillIncrease = skillIncrease > 0 ? skillIncrease : 100;
        this.breakChance = breakChance < 0 ? 0 : breakChance;
    }

    public int getSkillIncreasePct()
    {
        return skillIncrease;
    }

    public Item getItem()
    {
        return item;
    }

    public int getBreakPct()
    {
        return breakChance;
    }

    public int getSlot()
    {
        return slot;
    }

    public void setSlot(final int slot)
    {
        this.slot = slot;
    }
}
