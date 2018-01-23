package com.minecolonies.coremod.proxy;

import com.minecolonies.coremod.client.gui.WindowBuildTool;
import com.minecolonies.coremod.colony.CitizenDataView;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Opens a clipboard window.
     *
     * @param colonyId the colony id.
     */
    void openClipBoardWindow(int colonyId);

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

    /**
     * Method to get a side specific world from a message context during networking.
     * @param context The context to get the world from.
     * @return The world.
     */
    @Nullable
    World getWorldFromMessage(@NotNull final MessageContext context);

    /**
     * Method to get a side specific world from a message context anywhere.
     * @return The world.
     */
    @Nullable
    World getWorld(final int dimension);

    /**
     * Returns the recipe book from the player.
     * @param player THe player.
     * @return The recipe book.
     */
    @NotNull
    RecipeBook getRecipeBookFromPlayer(@NotNull final EntityPlayer player);
}
