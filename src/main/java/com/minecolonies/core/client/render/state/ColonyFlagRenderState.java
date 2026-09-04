package com.minecolonies.core.client.render.state;

import net.minecraft.client.renderer.blockentity.state.BannerRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ColonyFlagRenderState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState
{
    public final BannerRenderState banner = new BannerRenderState();
    public boolean creativePlaceholder;
    public final ItemStackRenderState placeholder = new ItemStackRenderState();
}
