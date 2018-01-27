package com.minecolonies.coremod.creativetab;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Class used to handle the creativeTab of minecolonies.
 */
public final class ModCreativeTabs
{
    public static final CreativeTabs MINECOLONIES = new CreativeTabs(Constants.MOD_ID)
    {

        @Override
        public Item getTabIconItem()
        {
            this.setBackgroundImageName("minecolonies_background.png");
            return new ItemStack(ModBlocks.blockHutTownHall).getItem();
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    /**
     * Private constructor to hide the implicit one.
     */
    private ModCreativeTabs()
    {
        /*
         * Intentionally left empty.
         */
    }
}
