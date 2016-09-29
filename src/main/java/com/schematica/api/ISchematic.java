package com.schematica.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface ISchematic
{
    /**
     * Gets a block state at a given location within the schematic. Requesting a block state outside of those bounds
     * returns the default block state for air.
     *
     * @param pos the location in world space.
     * @return the block at the requested location.
     */

    IBlockState getBlockState(BlockPos pos);

    /**
     * Sets the block state at the given location. Attempting to set a block state outside of the schematic
     * boundaries or with an invalid block state will result in no change being made and this method will return false.
     *
     * @param pos        the location in world space.
     * @param blockState the block state to set
     * @return true if the block state was successfully set.
     */
    boolean setBlockState(BlockPos pos, IBlockState blockState);

    /**
     * Gets the tile entity at the requested location. If no tile entity exists at that location, null will be returned.
     *
     * @param pos the location in world space.
     * @return the located tile entity.
     */
    TileEntity getTileEntity(BlockPos pos);

    /**
     * Returns a list of all tile entities in the schematic.
     *
     * @return all tile entities.
     */
    List<TileEntity> getTileEntities();

    /**
     * Add or replace a tile entity to a block at the requested location. Does nothing if the location is out of bounds.
     *
     * @param pos        the location in world space.
     * @param tileEntity the tile entity to set.
     */
    void setTileEntity(BlockPos pos, TileEntity tileEntity);

    /**
     * Removes a tile entity from the specific location if it exists, otherwise it silently continues.
     *
     * @param pos the location in world space.
     */
    void removeTileEntity(BlockPos pos);

    /**
     * Returns a list of all entities in the schematic.
     *
     * @return all entities.
     */
    List<Entity> getEntities();

    /**
     * Adds an entity to the schematic if it's not a player.
     *
     * @param entity the entity to add.
     */
    void addEntity(Entity entity);

    /**
     * Removes an entity from the schematic.
     *
     * @param entity the entity to remove.
     */
    void removeEntity(Entity entity);

    /**
     * Retrieves the icon that will be used to save the schematic.
     *
     * @return the schematic's future icon.
     */
    ItemStack getIcon();

    /**
     * Modifies the icon that will be used when saving the schematic.
     *
     * @param icon an ItemStack of the Item you wish you use as the icon.
     */
    void setIcon(ItemStack icon);

    /**
     * The width of the schematic
     *
     * @return the schematic width
     */
    int getWidth();

    /**
     * The length of the schematic
     *
     * @return the schematic length
     */
    int getLength();

    /**
     * The height of the schematic
     *
     * @return the schematic height
     */
    int getHeight();
}
