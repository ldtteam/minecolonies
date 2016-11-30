package com.structures.lib;

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

public class ModelHolder
{
    public final BlockPos        pos;
    public final IBlockState     actualState;
    public final IBlockState     extendedState;
    public final TileEntity      te;
    public final IBakedModel     model;
    public final List<BakedQuad> quads;
    public boolean rendered = false;

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
}
