package com.minecolonies.coremod.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;

/**
 * Holding all kind of render types of minecolonies
 */
public final class MRenderTypes extends RenderType
{
    private static final VertexFormat format = new VertexFormat(ImmutableList.of(POSITION_3F, TEX_2F, TEX_2SB));

    /**
     * Private constructor to hide implicit one.
     * @param name the name of the rendertype.
     * @param format its format.
     * @param id1 no idea.
     * @param id2 no idea.
     * @param b1 no idea.
     * @param b2 no idea.
     * @param b3 no idea.
     * @param state the rendertype state.
     */
    private MRenderTypes(final String name, final VertexFormat format, final int id1, final int id2, final boolean b1, final boolean b2, final Runnable b3, final Runnable state)
    {
        super(name, format, id1, id2, b1, b2, b3, state);
    }

    /**
     * Custom texture renderer type.
     * @param resourceLocation the location fo the texture.
     * @return the renderType which is created.
     */
    public static RenderType customTextRenderer(@NotNull final ResourceLocation resourceLocation)
    {
        final RenderType.State state = RenderType.State.builder()
                                         .texture(new RenderState.TextureState(resourceLocation, false, false))//Texture state
                                         .alpha(RenderState.HALF_ALPHA)
                                         .depthTest(RenderState.DEPTH_ALWAYS)
                                         .build(true);

        return RenderType.get("custommctextrenderer", format, 7, 256, true, false, state);
    }

    /**
     * Custom line renderer type.
     * @return the renderType which is created.
     */
    public static RenderType customLineRenderer()
    {

        return get("minecolonieslines", DefaultVertexFormats.POSITION_COLOR, 1, 256,
          RenderType.State.builder().line(new RenderState.LineState(OptionalDouble.empty())).build(false));
    }
}
