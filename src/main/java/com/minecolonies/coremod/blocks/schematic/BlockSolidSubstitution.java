package com.minecolonies.coremod.blocks.schematic;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.blocks.AbstractBlockMinecolonies;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;

import static com.minecolonies.api.util.constant.Suppression.DEPRECATION;

/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockSolidSubstitution extends AbstractBlockMinecolonies<BlockSolidSubstitution>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockSolidSubstitution";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockSolidSubstitution()
    {
        super(Material.WOOD);
        initBlock();
    }

    /**
     * initialize the block
     * sets the creative tab, as well as the resistance and the hardness.
     */
    private void initBlock()
    {
        setRegistryName(BLOCK_NAME);
        setTranslationKey(String.format("%s.%s", Constants.MOD_ID.toLowerCase(), BLOCK_NAME));
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        setHardness(BLOCK_HARDNESS);
        setResistance(RESISTANCE);
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks
     * for render.
     *
     * @return true
     */
    //todo: remove once we no longer need to support this
    @SuppressWarnings(DEPRECATION)
    @Override
    public boolean isOpaqueCube(final IBlockState state)
    {
        return true;
    }

}
