package com.minecolonies.structures.lib;

/*
 * Class based on the work by Maruohon
 * https://github.com/maruohon/placementpreview
 */

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model holder of the structures.
 */
public class ModelHolder
{
    /**
     * Position the model is at.
     */
    public final BlockPos pos;

    /**
     * Actual state of the model.
     */
    public final IBlockState actualState;

    /**
     * Extended state of that model.
     */
    public final IBlockState extendedState;

    /**
     * Tile entity of the model.
     */
    public final TileEntity te;

    /**
     * The model of the model.
     */
    public final IBakedModel model;

    /**
     * Quads to render for the model.
     */
    public final List<BakedQuad> quads;

    /**
     * If it is rendered.
     */
    private boolean rendered = false;

    /**
     * Creates a model holder.
     *
     * @param pos           at position.
     * @param actualState   actual state.
     * @param extendedState extended state.
     * @param te            tileEntity.
     * @param model         model.
     */
    public ModelHolder(final BlockPos pos, final IBlockState actualState, final IBlockState extendedState, @Nullable final TileEntity te, final IBakedModel model)
    {
        this.pos = pos;
        this.actualState = actualState;
        this.extendedState = extendedState;
        this.te = te;
        this.model = model;
        this.quads = new ArrayList<>();
        this.rendered = false;
    }

    /**
     * Checks if it is rendered.
     *
     * @return true if so.
     */
    public boolean isRendered()
    {
        return rendered;
    }

    /**
     * Sets if it rendered.
     *
     * @param rendered state to set.
     */
    public void setRendered(final boolean rendered)
    {
        this.rendered = rendered;
    }
}
