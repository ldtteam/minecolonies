package com.minecolonies.proxy;

import com.minecolonies.colony.CitizenDataView;
import net.minecraft.util.math.BlockPos;

/**
 * Basic proxy interface.
 */
public interface IProxy
{
    /**
     * Returns whether or not the proxy is client sided or server sided.
     *
     * @return true when client, false when server.
     */
    boolean isClient();

    /**
     * Method to register Tile Entities in.
     */
    void registerTileEntities();

    /**
     * Method to register events in.
     */
    void registerEvents();

    /**
     * Method to register Entities in.
     */
    void registerEntities();

    /**
     * Method to register entity rendering in.
     */
    void registerEntityRendering();

    /**
     * Method to register sounds.
     */
    void registerSounds();

    /**
     * Method to register tile entity rendering in.
     */
    void registerTileEntityRendering();

    /**
     * Method to display the citizen window.
     *
     * @param citizen {@link CitizenDataView}
     */
    void showCitizenWindow(CitizenDataView citizen);

    /**
     * Opens a build tool window.
     *
     * @param pos coordinates.
     */
    void openBuildToolWindow(BlockPos pos);

    /**
     * Registers all block and item renderer.
     */
    void registerRenderer();
}
