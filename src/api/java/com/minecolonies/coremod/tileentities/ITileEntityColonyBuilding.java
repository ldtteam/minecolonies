package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.coremod.colony.buildings.IBuildingContainer;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public interface ITileEntityColonyBuilding extends ICapabilitySerializable<CompoundNBT>, ITickableTileEntity
{
    /**
     * Finds the first @see ItemStack the type of {@code is}.
     * It will be taken from the chest and placed in the worker inventory.
     * Make sure that the worker stands next the chest to not break immersion.
     * Also make sure to have inventory space for the stack.
     *
     * @param entity                      the tileEntity chest or building.
     * @param itemStackSelectionPredicate the itemStack predicate.
     * @return true if found the stack.
     */
    static boolean isInTileEntity(ICapabilityProvider entity, @NotNull Predicate<ItemStack> itemStackSelectionPredicate)
    {
        return InventoryFunctions.matchFirstInProvider(entity, itemStackSelectionPredicate);
    }

    /**
     * Returns the colony ID.
     *
     * @return ID of the colony.
     */
    int getColonyId();

    /**
     * Returns the colony of the tile entity.
     *
     * @return Colony of the tile entity.
     */
    IColony getColony();

    /**
     * Returns the position of the tile entity.
     *
     * @return Block Coordinates of the tile entity.
     */
    BlockPos getPosition();

    /**
     * Check for a certain item and return the position of the chest containing it.
     *
     * @param itemStackSelectionPredicate the stack to search for.
     * @return the position or null.
     */
    @Nullable
    BlockPos getPositionOfChestWithItemStack(@NotNull Predicate<ItemStack> itemStackSelectionPredicate);

    /**
     * Sets the colony of the tile entity.
     *
     * @param c Colony to set in references.
     */
    void setColony(IColony c);

    SUpdateTileEntityPacket getUpdatePacket();

    @NotNull
    CompoundNBT getUpdateTag();

    void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet);

    void onChunkUnload();

    /**
     * Returns the building associated with the tile entity.
     *
     * @return {@link IBuildingContainer} associated with the tile entity.
     */
    IBuildingContainer getBuilding();

    /**
     * Sets the building associated with the tile entity.
     *
     * @param b {@link IBuildingContainer} to associate with the tile entity.
     */
    void setBuilding(IBuildingContainer b);

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link com.minecolonies.coremod.colony.buildings.views.IBuildingView} the tile entity is associated with.
     */
    IBuildingView getBuildingView();

    void read(CompoundNBT compound);

    @NotNull
    CompoundNBT write(@NotNull CompoundNBT compound);

    @Override
    void tick();

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    boolean hasAccessPermission(PlayerEntity player);

    /**
     * Set if the entity is mirrored.
     *
     * @param mirror true if so.
     */
    void setMirror(boolean mirror);

    /**
     * Check if building is mirrored.
     *
     * @return true if so.
     */
    boolean isMirrored();

    /**
     * Getter for the style.
     *
     * @return the string of it.
     */
    String getStyle();

    /**
     * Set the style of the tileEntity.
     *
     * @param style the style to set.
     */
    void setStyle(String style);

    @Nonnull
    @Override
    <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @javax.annotation.Nullable Direction side);

    boolean isInvalid();
}
