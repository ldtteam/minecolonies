package com.minecolonies.coremod.blocks.schematic;

import com.minecolonies.api.blocks.AbstractBlockMinecolonies;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import org.jetbrains.annotations.NotNull;


/**
 * This block is used as a substitution block for the Builder.
 * Every solid block can be substituted by this block in schematics.
 * This helps make schematics independent from location and ground.
 */
public class BlockWaypoint extends AbstractBlockMinecolonies<BlockWaypoint>
{

    /**
     * The hardness this block has.
     */
    private static final float BLOCK_HARDNESS = 0.0F;

    /**
     * This blocks name.
     */
    private static final String BLOCK_NAME = "blockWayPoint";

    /**
     * The resistance this block has.
     */
    private static final float RESISTANCE = 1F;

    /**
     * Constructor for the Substitution block.
     * sets the creative tab, as well as the resistance and the hardness.
     */
    public BlockWaypoint()
    {
        super(Properties.create(Material.WOOD).hardnessAndResistance(BLOCK_HARDNESS, RESISTANCE).doesNotBlockMovement());
        setRegistryName(BLOCK_NAME);
    }

    @Override
    public boolean doesSideBlockRendering(final BlockState state, final IEnviromentBlockReader world, final BlockPos pos, final Direction face)
    {
        return false;
    }

    @NotNull
    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
