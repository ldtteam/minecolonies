package com.minecolonies.coremod.tileentities;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.util.InventoryFunctions;
import com.minecolonies.coremod.colony.buildings.IBuildingContainer;
import com.minecolonies.coremod.colony.buildings.views.IBuildingView;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public interface ITileEntityColonyBuilding extends ICapabilitySerializable<NBTTagCompound>, ILockableContainer, ILootContainer, ITickable
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

    @Override
    void markDirty();

    SPacketUpdateTileEntity getUpdatePacket();

    @NotNull
    NBTTagCompound getUpdateTag();

    void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet);

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

    @Override
    ITextComponent getDisplayName();

    /**
     * Returns the view of the building associated with the tile entity.
     *
     * @return {@link com.minecolonies.coremod.colony.buildings.views.IBuildingView} the tile entity is associated with.
     */
    IBuildingView getBuildingView();

    void readFromNBT(NBTTagCompound compound);

    @NotNull
    NBTTagCompound writeToNBT(@NotNull NBTTagCompound compound);

    @Override
    void update();

    @Override
    boolean isUsableByPlayer(EntityPlayer player);

    /**
     * Checks if the player has permission to access the hut.
     *
     * @param player Player to check permission of.
     * @return True when player has access, or building doesn't exist, otherwise false.
     */
    boolean hasAccessPermission(EntityPlayer player);

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

    @Override
    boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing);

    @Override
    <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing);

    boolean isInvalid();

    Block getBlockType();
}
