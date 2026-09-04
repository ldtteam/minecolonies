package com.minecolonies.core.client.gui.map;

import com.ldtteam.blockui.BOGuiGraphics;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneParams;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

/**
 * Simple minecraft map element.
 */
public class MinecraftMap extends Pane
{
    public static final int MAP_SIZE = 128;
    public static final int MAP_CENTER = 64;

    private MapItemSavedData mapData;
    private final MapRenderState mapRenderState = new MapRenderState();
    private MapId mapId;

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
    public void setMapData(final MapId mapId, final MapItemSavedData mapData)
    {
        this.mapId = mapId;
        this.mapData = mapData;
    }

    /**
     * Draw this image on the GUI.
     *
     * @param mx Mouse x (relative to parent)
     * @param my Mouse y (relative to parent)
     */
    @Override
    public void drawSelf(final BOGuiGraphics ms, final double mx, final double my)
    {
        if (mapData != null)
        {
            Minecraft.getInstance().getMapRenderer().extractRenderState(mapId, mapData, mapRenderState);
            ms.pose().pushMatrix();
            ms.pose().translate((float) x, (float) y);
            ms.pose().scale(getWidth() / (float) MAP_SIZE, getHeight() / (float) MAP_SIZE);
            ms.map(mapRenderState);
            ms.pose().popMatrix();
        }
    }
}
