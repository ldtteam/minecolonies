package com.minecolonies.coremod.blocks;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.tileentities.TileEntityInfoPoster;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
        GameRegistry.register((new ItemBlock(this)).setRegistryName(this.getRegistryName()));
        GameRegistry.register(this);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta)
    {
        return new TileEntityInfoPoster();
    }

    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }
}
