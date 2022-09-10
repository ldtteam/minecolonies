package com.minecolonies.coremod.client.render.worldevent;

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
     * @param resLoc texture location
     * @return render type
     */
    public static RenderType getEntityCutoutFront(final ResourceLocation resLoc)
    {
        return InnerRenderTypes.ENTITY_CUTOUT_FRONT.apply(resLoc);
    }

    private static final class InnerRenderTypes extends RenderType
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

        private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT_FRONT = Util.memoize((p_173202_) -> {
            RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
              .setShaderState(RENDERTYPE_ENTITY_CUTOUT_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(p_173202_, false, false))
              .setTransparencyState(NO_TRANSPARENCY)
              .setLightmapState(LIGHTMAP)
              .setOverlayState(OVERLAY)
              .setDepthTestState(NO_DEPTH_TEST)
              .createCompositeState(true);
            return create("entity_cutout", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
        });
    }
}
