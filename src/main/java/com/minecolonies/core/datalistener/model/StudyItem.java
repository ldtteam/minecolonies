package com.minecolonies.core.datalistener.model;

import com.minecolonies.core.datalistener.StudyItemListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Container class for any study item.
 *
 * @param item                the item to use.
 * @param skillIncreaseChance chance for skill to increase after using the item.
 * @param breakChance         chance for the item to be used up after using it.
 */
public record StudyItem(
    Item item,
    int skillIncreaseChance,
    int breakChance)
{
    /**
     * Check if the given item is a valid study item.
     *
     * @param stack the item stack to check.
     * @return true if so.
     */
    public static boolean isStudyItem(final ItemStack stack)
    {
        return StudyItemListener.INSTANCE.getEntries().containsKey(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
