package com.minecolonies.api.items;

import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IChiefSwordItem extends IForgeRegistryEntry<Item>
{
    /**
     * returns the items name
     *
     * @return Returns the items name in the form of a string
     */
    String getName();
}
