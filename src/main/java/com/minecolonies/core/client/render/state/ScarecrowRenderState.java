package com.minecolonies.core.client.render.state;

import com.minecolonies.api.tileentities.ScareCrowType;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScarecrowRenderState extends BlockEntityRenderState
{
    public ScareCrowType scarecrowType = ScareCrowType.NORMAL;
    public Direction facing = Direction.NORTH;
    public boolean lantern;
    public final BlockModelRenderState lanternModel = new BlockModelRenderState();
    public int blockLight;
    public int skyLight;
}
