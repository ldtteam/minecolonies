package com.minecolonies.coremod.proxy;

import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.colony.CitizenDataView;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.File;

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
    void openBuildToolWindow(final BlockPos pos);

    /**
     * Opens a build tool window for a specific structure.
     * @param pos the position.
     * @param structureName the structure name.
     * @param rotation the rotation.
     * @param mode the mode.
     */
    void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode);

    /**
     * Registers all block and item renderer.
     */
    void registerRenderer();

    /**
     * Get the file representation of the additional schematics' folder.
     *
     * @return the folder for the schematic
     */
    @Nullable
    File getSchematicsFolder();
}
