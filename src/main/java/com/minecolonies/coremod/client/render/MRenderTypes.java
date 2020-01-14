package com.minecolonies.coremod.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;

public final class MRenderTypes extends RenderType
{
    private static final VertexFormat format = new VertexFormat(ImmutableList.of(POSITION_3F, TEX_2F, LIGHT_ELEMENT));

    public MRenderTypes(final String p_i225992_1_, final VertexFormat p_i225992_2_, final int p_i225992_3_, final int p_i225992_4_, final boolean p_i225992_5_, final boolean p_i225992_6_, final Runnable p_i225992_7_, final Runnable p_i225992_8_)
    {
        super(p_i225992_1_, p_i225992_2_, p_i225992_3_, p_i225992_4_, p_i225992_5_, p_i225992_6_, p_i225992_7_, p_i225992_8_);
    }

    public static RenderType customTextRenderer(ResourceLocation resourceLocation) {
        final RenderType.State state = RenderType.State.builder()
                                   .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
                                         .alpha(RenderState.HALF_ALPHA)
                                         .depthTest(RenderState.ALWAYS_DEPTH_TEST)
                                         .build(true);

        return RenderType.of("customRenderer", format, 7, 256, true, false, state);
    }

}
