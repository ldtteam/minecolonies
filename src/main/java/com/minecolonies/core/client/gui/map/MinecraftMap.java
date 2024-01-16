package com.minecolonies.core.client.gui.map;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Simple minecraft map element.
 */
public class MinecraftMap extends Pane implements AutoCloseable
{
    private DynamicTexture texture;
    private ResourceLocation textureResLoc;

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
     * @param mapData the mapData to set.
     */
    public void setMapData(final MapItemSavedData mapData)
    {
        if (texture != null)
        {
            freeTexture();
        }

        texture = new DynamicTexture(128, 128, false);

        for (int y = 0; y < 128; ++y)
        {
            for (int x = 0; x < 128; ++x)
            {
                texture.getPixels().setPixelRGBA(x, y, MaterialColor.getColorFromPackedId(mapData.colors[x + y * 128]));
            }
        }

        texture.upload();
        textureResLoc = mc.textureManager.register("minecolonies_map/" + id, texture);
    }

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final PoseStack ms, final double mx, final double my)
    {
        if (textureResLoc != null)
        {
            blit(ms, textureResLoc, x, y, width, height);
        }
    }

    private void freeTexture()
    {
        if (textureResLoc != null)
        {
            texture.close();
            mc.textureManager.release(textureResLoc);

            texture = null;
            textureResLoc = null;
        }
    }

    @Override
    public void close()
    {
        freeTexture();
    }
}
