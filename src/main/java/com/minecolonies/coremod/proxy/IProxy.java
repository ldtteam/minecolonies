package com.minecolonies.coremod.proxy;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.coremod.colony.CitizenDataView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic proxy interface.
 */
public interface IProxy
{
    /**
     * Sets up the API
     */
    void setupApi();

    /**
     * Returns whether or not the proxy is client sided or server sided.
     *
     * @return true when client, false when server.
     */
    boolean isClient();

    /**
     * Method to display the citizen window.
     *
     * @param citizen {@link CitizenDataView}
     */
    void showCitizenWindow(ICitizenDataView citizen);

    /**
     * Open the suggestion window.
     *
     * @param pos   the position to open it at.
     * @param state the state trying to place.
     * @param stack the itemStack.
     */
    void openSuggestionWindow(@NotNull BlockPos pos, @NotNull BlockState state, @NotNull final ItemStack stack);

    /**
     * Opens a rally banner window.
     *
     * @param banner The banner to edit in the GUI
     */
    void openBannerRallyGuardsWindow(final ItemStack banner);

    /**
     * Opens a clipboard window.
     *
     * @param colonyView the colony id.
     */
    void openClipboardWindow(IColonyView colonyView);

    /**
     * Opens the resource scroll window.
     *
     * @param colonyId          the colony id.
     * @param pos               the position of the builder.
     * @param warehousePos      the position of the warehouse clicked on (if any).
     * @param warehouseCompound the compound data to store the warehouse snapshot to.
     */
    void openResourceScrollWindow(
      final int colonyId,
      final BlockPos pos,
      final @Nullable BlockPos warehousePos,
      final @Nullable CompoundTag warehouseCompound);

    /**
     * Returns the recipe book from the player.
     *
     * @param player THe player.
     * @return The recipe book.
     */
    @NotNull
    RecipeBook getRecipeBookFromPlayer(@NotNull final Player player);

    /**
     * Open the BOWindow of the decoration controller.
     *
     * @param pos the position of the block.
     */
    void openDecorationControllerWindow(@NotNull final BlockPos pos);

    /**
     * Get the world for a dimension.
     *
     * @param dimension the dimension.
     * @return the world.
     */
    Level getWorld(ResourceKey<Level> dimension);
}
