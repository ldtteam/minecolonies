package com.minecolonies.items;

import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.lib.Constants;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Handles simple things that all items need.
 */
public abstract class AbstractItemMinecolonies extends Item
{
    private final String name;

    /**
     * Sets the name, creative tab, and registers the item.
     *
     * @param name The name of this item
     */
    public AbstractItemMinecolonies(String name)
    {
        this.name = name;
        
        super.setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + this.name);
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setRegistryName(this.name);
        GameRegistry.registerItem(this);
    }

    /**
     * Returns the name of the item
     *
     * @return      Name of the item
     */
    public final String getName() {
        return name;
    }
}
