package com.structures.lib;
/**
 * Class based on the work by Maruohon
 * https://github.com/maruohon/placementpreview
 */

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ModelHolder{
	
    public final BlockPos pos;
    public final IBlockState actualState;
    public final IBlockState extendedState;
    public final TileEntity te;
    public final IBakedModel model;
    public final List<BakedQuad> quads;
    public boolean rendered = false;

    public ModelHolder(BlockPos pos, IBlockState actualState, IBlockState extendedState, @Nullable TileEntity te, IBakedModel model)
    {
        this.pos = pos;
        this.actualState = actualState;
        this.extendedState = extendedState;
        this.te = te;
        this.model = model;
        this.quads = new ArrayList<BakedQuad>();
        this.rendered = false;
    }
}
