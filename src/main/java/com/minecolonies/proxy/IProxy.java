package com.minecolonies.proxy;

import com.minecolonies.colony.CitizenData;
import com.schematica.world.SchematicWorld;

import net.minecraft.util.BlockPos;

public interface IProxy
{
    /**
     * Returns whether or not the proxy is client sided or server sided
     *
     * @return      true when client, false when server
     */
    boolean isClient();

    /**
     * Method to register Tile Entities in
     */
    void registerTileEntities();

    /**
     * Method to register Key Bindings in
     */
    void registerKeyBindings();

    /**
     * Method to register events in
     */
    void registerEvents();

    /**
     * Method to register Entities in
     */
    void registerEntities();

    /**
     * Method to register entity rendering in
     */
    void registerEntityRendering();

    /**
     * Method to register tile entity rendering in
     */
    void registerTileEntityRendering();

    /**
     * Method to display the citizen window
     *
     * @param citizen       {@link com.minecolonies.colony.CitizenData.View}
     */
    void showCitizenWindow(CitizenData.View citizen);

    /**
     * Opens a build tool window
     *
     * @param pos     coordinates
     */
    void openBuildToolWindow(BlockPos pos);

    //Schematica

    /**
     * Sets the active schematica.
     * The current {@link SchematicWorld} os given
     *
     * @param world     {@link SchematicWorld}
     */
    void setActiveSchematic(SchematicWorld world);

    /**
     * Returns the active schematica
     *
     * @return          {@link SchematicWorld}
     */
    SchematicWorld getActiveSchematic();
}
