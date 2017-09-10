package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.block.BlockWallSign;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.Locale;

/**
 * Class for the minecolonies info Poster.
 */
public class BlockInfoPoster extends BlockWallSign
{
    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockInfoPoster";

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockInfoPoster()
    {
        super();
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setUnlocalizedName(String.format("%s.%s", Constants.MOD_ID.toLowerCase(Locale.ENGLISH), BLOCK_NAME));
        GameRegistry.register(this);
    }
}
