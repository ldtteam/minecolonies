package com.minecolonies.api.creativetab;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * Class used to handle the creativeTab of minecolonies.
 */
public final class ModCreativeTabs
{
    public static final ItemGroup MINECOLONIES = new ItemGroup(Constants.MOD_ID)
    {

        @Override
        public ItemStack createIcon()
        {
            this.setBackgroundImageName("minecolonies_background.png");
            return new ItemStack(ModBlocks.blockHutTownHall);
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
