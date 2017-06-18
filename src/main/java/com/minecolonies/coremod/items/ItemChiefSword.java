package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Class handling the Chief Sword item.
 */
public class ItemChiefSword extends ItemSword
{
    private static final String ITEM_NAME = "chiefSword";

    public ItemChiefSword()
    {
        super(ToolMaterial.GOLD);
        super.setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + ITEM_NAME);
        setRegistryName(ITEM_NAME);
        GameRegistry.register(this);
    }

    public final String getName()
    {
        return ITEM_NAME;
    }
}
