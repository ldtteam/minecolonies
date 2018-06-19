package com.minecolonies.coremod.blocks.cactus;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockDoor;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusDoor extends AbstractBlockDoor<BlockCactusDoor>
{

    public BlockCactusDoor(final Block block)
    {
        super(Material.WOOD, block);
        setRegistryName("blockcactusdoor");
        setUnlocalizedName(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusdoor");
        setSoundType(SoundType.WOOD);
        setLightOpacity(0);
    }

}
