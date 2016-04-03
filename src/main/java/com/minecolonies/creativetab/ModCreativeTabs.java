package com.minecolonies.creativetab;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ModCreativeTabs
{
    private ModCreativeTabs() {
    }

    public static final CreativeTabs MINECOLONIES = new CreativeTabs(Constants.MOD_ID)
    {
        @Override
        public Item getTabIconItem()
        {
            return Item.getItemFromBlock(ModBlocks.blockHutTownhall);
        }
    };
}