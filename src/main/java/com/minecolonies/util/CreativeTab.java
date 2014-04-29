package com.minecolonies.util;

import com.minecolonies.lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class CreativeTab
{
    public static final CreativeTabs mineColoniesTab = new CreativeTabs(Constants.MODID.toLowerCase())
    {
        @Override
        public Item getTabIconItem()
        {
            return Items.apple; // TODO
        }
    };
}