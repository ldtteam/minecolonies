package com.minecolonies.coremod.client.render.worldevent;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
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
    public static RenderType getPrimitiveTex(final ResourceLocation resLoc)
    {
        return InnerRenderTypes.PRIMITIVE_TEX.apply(resLoc);
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

        private static final Function<ResourceLocation, RenderType> PRIMITIVE_TEX = Util.memoize((resLoc) -> {
            return create("minecolonies_primitive_tex",
                DefaultVertexFormat.POSITION_TEX,
                VertexFormat.Mode.TRIANGLE_FAN,
                1 << 10,
                true,
                false,
                RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(resLoc, false, false))
                    .setShaderState(POSITION_TEX_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setCullState(CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .setLayeringState(NO_LAYERING)
                    .setOutputState(MAIN_TARGET)
                    .setTexturingState(DEFAULT_TEXTURING)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false));
        });
    }
}
