package com.minecolonies.core.client.render.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColonySignRenderState extends BlockEntityRenderState
{
    public float relativeRotation;
    public boolean connected;
    public String colonyName = "";
    public int colonyDistance;
    public String targetColonyName = "";
    public int targetColonyDistance;
}
