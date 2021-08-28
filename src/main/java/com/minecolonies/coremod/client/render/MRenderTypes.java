package com.minecolonies.coremod.client.render;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

import net.minecraft.client.renderer.RenderType.CompositeState;

/**
 * Holding all kind of render types of minecolonies
 */
public final class MRenderTypes extends RenderType
{
    private static final VertexFormat format = new VertexFormat(ImmutableList.of(ELEMENT_POSITION, ELEMENT_UV0, ELEMENT_UV2));

    /**
     * Private constructor to hide implicit one.
     *
     * @param name   the name of the rendertype.
     * @param format its format.
     * @param id1    no idea.
     * @param id2    no idea.
     * @param b1     no idea.
     * @param b2     no idea.
     * @param b3     no idea.
     * @param state  the rendertype state.
     */
    private MRenderTypes(final String name, final VertexFormat format, final int id1, final int id2, final boolean b1, final boolean b2, final Runnable b3, final Runnable state)
    {
        super(name, format, id1, id2, b1, b2, b3, state);
    }

    /**
     * Custom texture renderer type.
     *
     * @param resourceLocation the location fo the texture.
     * @return the renderType which is created.
     */
    public static RenderType customTextRenderer(@NotNull final ResourceLocation resourceLocation)
    {
        final CompositeState state = CompositeState.builder()
                              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))//Texture state
                              .setAlphaState(RenderStateShard.MIDWAY_ALPHA)
                              .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                              .createCompositeState(true);

        return create("custommctextrenderer", format, 7, 256, true, false, state);
    }

    /**
     * Custom line renderer type.
     *
     * @return the renderType which is created.
     */
    public static RenderType customLineRenderer()
    {
        return create("minecolonieslines", DefaultVertexFormat.POSITION_COLOR, 1, 256,
          CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).createCompositeState(false));
    }
}
