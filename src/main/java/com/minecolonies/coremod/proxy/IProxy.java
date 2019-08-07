package com.minecolonies.coremod.proxy;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.coremod.colony.CitizenDataView;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
    void showCitizenWindow(ICitizenDataView citizen);

    /**
     * Opens a build tool window.
     *
     * @param pos coordinates.
     */
    void openBuildToolWindow(final BlockPos pos);

    /**
     * Open the suggestion window.
     * @param pos the position to open it at.
     * @param state the state trying to place.
     * @param stack the itemStack.
     */
    void openSuggestionWindow(@NotNull BlockPos pos, @NotNull BlockState state, @NotNull final ItemStack stack);

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
     * Opens the resource scroll window.
     *
     * @param colonyId the colony id.
     */
    void openResourceScrollWindow(final int colonyId, final BlockPos pos);

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
     * Returns the recipe book from the player.
     * @param player THe player.
     * @return The recipe book.
     */
    @NotNull
    RecipeBook getRecipeBookFromPlayer(@NotNull final PlayerEntity player);

    /**
     * Open the Window of the decoration controller.
     * @param pos the position of the block.
     */
    void openDecorationControllerWindow(@NotNull final BlockPos pos);

    /**
     * Get the world for a dimension.
     * @param dimension the dimension.
     * @return the world.
     */
    World getWorld(int dimension);
}
