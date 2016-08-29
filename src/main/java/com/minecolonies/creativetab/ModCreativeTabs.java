package com.minecolonies.creativetab;

import com.minecolonies.blocks.ModBlocks;
import com.minecolonies.lib.Constants;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ModCreativeTabs
{

    public static final CreativeTabs MINECOLONIES = new CreativeTabs(Constants.MOD_ID)
    {
        @Override
        public Item getTabIconItem()
        {
            this.setBackgroundImageName("minecolonies_background.jpg");
            this.setNoScrollbar();
            return Item.getItemFromBlock(ModBlocks.blockHutTownHall);
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    private ModCreativeTabs()
    {

    }
}
