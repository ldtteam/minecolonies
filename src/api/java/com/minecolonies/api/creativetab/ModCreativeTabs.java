package com.minecolonies.api.creativetab;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/**
 * Class used to handle the creativeTab of minecolonies.
 */
public final class ModCreativeTabs
{
    public static final CreativeModeTab MINECOLONIES = new CreativeModeTab(Constants.MOD_ID)
    {

        @Override
        public ItemStack makeIcon()
        {
            this.setBackgroundSuffix("minecolonies_background.png");
            return new ItemStack(ModBlocks.blockHutTownHall);
        }

        @Override
        public boolean hasSearchBar()
        {
            return true;
        }
    };

    public static final CreativeModeTab MINECOLONIES_ARCHEOLOGIST_LOOT = new CreativeModeTab(TranslationConstants.COM_MINECOLONIES_CREATIVE_TABS_ARCHEOGLOGIST_LOOT)
    {

        @Override
        public ItemStack makeIcon()
        {
            this.setBackgroundSuffix("minecolonies_background.png");
            if (ModItems.archeologistLootItems.size() > 0)
                return ModItems.archeologistLootItems.get(0).getDefaultInstance();

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
