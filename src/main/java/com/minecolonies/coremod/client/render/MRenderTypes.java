package com.minecolonies.coremod.client.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

/**
 * Holding all kind of render types of minecolonies
 */
public final class MRenderTypes extends RenderType
{
    public static final VertexFormat format = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position", ELEMENT_POSITION).put("UV0", ELEMENT_UV0).put("UV2", ELEMENT_UV2).build());

    public MRenderTypes(final String nameIn,
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
                              //.setAlphaState(RenderStateShard.MIDWAY_ALPHA) todo 1.17
                              .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                              .createCompositeState(true);

        return create("custommctextrenderer", format, VertexFormat.Mode.QUADS, 256, true, false, state);
    }

    /**
     * Custom line renderer type.
     *
     * @return the renderType which is created.
     */
    public static RenderType customLineRenderer()
    {
        return create("minecolonieslines", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256, false, false,
          CompositeState.builder().setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty())).createCompositeState(false));
    }
}
