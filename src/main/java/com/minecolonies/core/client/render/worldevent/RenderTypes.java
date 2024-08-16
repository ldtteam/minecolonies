package com.minecolonies.core.client.render.worldevent;

import com.ldtteam.structurize.util.WorldRenderMacros.RenderTypes.AlwaysDepthTestStateShard;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public class RenderTypes
{
    /**
     * Usable for rendering simple flat textures
     * 
     * @param  resLoc texture location
     * @return        render type
     */
    public static RenderType worldEntityIcon(final ResourceLocation resLoc)
    {
        return InnerRenderTypes.WORLD_ENTITY_ICON.apply(resLoc);
    }

    public static final class InnerRenderTypes extends RenderType
    {
        private InnerRenderTypes(final String nameIn,
            final VertexFormat formatIn,
            final VertexFormat.Mode drawModeIn,
            final int bufferSizeIn,
            final boolean useDelegateIn,
            final boolean needsSortingIn,
            final Runnable setupTaskIn,
            final Runnable clearTaskIn)
        {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
            throw new IllegalStateException();
        }

        private static final Function<ResourceLocation, RenderType> WORLD_ENTITY_ICON = Util.memoize((p_173202_) -> {
            return create("minecolonies_entity_icon",
                DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.QUADS,
                1024,
                false,
                false,
                RenderType.CompositeState.builder()
                    .setShaderState(POSITION_TEX_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(p_173202_, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(AlwaysDepthTestStateShard.ALWAYS_DEPTH_TEST)
                    .createCompositeState(false));
        });
    }
}
