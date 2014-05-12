package com.minecolonies.util;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

public class CreativeTab
{
    public static final CreativeTabs mineColoniesTab = new CreativeTabs(Constants.MODID)
    {
        @Override
        public Item getTabIconItem()
        {
            return Item.getItemFromBlock(ModBlocks.blockHutTownhall);
        }
    };
}