package com.minecolonies.core.client.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class NamedGraveRenderState extends BlockEntityRenderState
{
    public Direction facing = Direction.SOUTH;
    public boolean isNamedGrave;
    public List<String> textLines = new ArrayList<>();
}
