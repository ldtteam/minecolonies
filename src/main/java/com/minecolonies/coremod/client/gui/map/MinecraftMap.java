package com.minecolonies.coremod.client.gui.map;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.ldtteam.structurize.util.WorldRenderMacros;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Simple minecraft map element.
 */
public class MinecraftMap extends Pane
{
    /**
     * The map object associated to the rendering.
     */
    private MapItemSavedData mapData = null;

    private DynamicTexture texture;
    private RenderType renderType;

    /**
     * Default Constructor.
     */
    public MinecraftMap()
    {
        super();
    }

    /**
     * Constructor used by the xml loader.
     *
     * @param params PaneParams loaded from the xml.
     */
    public MinecraftMap(final PaneParams params)
    {
        super(params);
    }

    /**
     * Set the fitting map data.
     * @param data the mapData to set.
     */
    public void setMapData(final MapItemSavedData data)
    {
        this.mapData = data;
        this.texture = new DynamicTexture(128, 128, true);

        for(int i = 0; i < 128; ++i) {
            for(int j = 0; j < 128; ++j) {
                int k = j + i * 128;
                this.texture.getPixels().setPixelRGBA(j, i, MaterialColor.getColorFromPackedId(mapData.colors[k]));
            }
        }

        ResourceLocation resourcelocation = mc.gameRenderer.getMapRenderer().textureManager.register("map/" + mapData.hashCode(), this.texture);
        this.renderType = RenderType.text(resourcelocation);

        this.texture.upload();
    }

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    /*@Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        ms.pushPose();
        super.drawSelf(ms, mx, my);
        //ms.translate(x, y, 0.0d);

        if (stack != null && stack.getItem() == Items.FILLED_MAP)
        {
            final MultiBufferSource.BufferSource buffer = WorldRenderMacros.getBufferSource();
            mc.gameRenderer.getMapRenderer().render(ms, buffer, MapItem.getMapId(stack), MapItem.getSavedData(stack, mc.level), false, 15728640);

            buffer.endBatch();
        }
        ms.popPose();
    }*/

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        ms.pushPose();
        super.drawSelf(ms, mx, my);
        //ms.translate(x, y, 0.0d);

        if (mapData != null)
        {
            final MultiBufferSource.BufferSource buffer = WorldRenderMacros.getBufferSource();


            Matrix4f matrix4f = ms.last().pose();
            VertexConsumer vertexconsumer = buffer.getBuffer(this.renderType);
            vertexconsumer.vertex(matrix4f, 0.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 1.0F).uv2(15728640).endVertex();
            vertexconsumer.vertex(matrix4f, 128.0F, 128.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 1.0F).uv2(15728640).endVertex();
            vertexconsumer.vertex(matrix4f, 128.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(1.0F, 0.0F).uv2(15728640).endVertex();
            vertexconsumer.vertex(matrix4f, 0.0F, 0.0F, -0.01F).color(255, 255, 255, 255).uv(0.0F, 0.0F).uv2(15728640).endVertex();

            buffer.endBatch();
        }
        ms.popPose();
    }
}
