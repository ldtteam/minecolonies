package com.minecolonies.api.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;
import com.ldtteam.structurize.util.ChangeStorage;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

/**
 * Interface for using the structure codebase.
 */
public final class InstantStructurePlacer extends com.ldtteam.structurize.util.InstantStructurePlacer
{
    /**
     * Load a structure into this world.
     *
     * @param worldObj the world to load in
     * @param name     the structure name
     */
    public InstantStructurePlacer(final World worldObj, final String name)
    {
        this(new Structure(worldObj, name, new PlacementSettings()));
    }

    public InstantStructurePlacer(final Structure structure)
    {
        super(structure);
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotations number of times rotated
     * @param mirror    the mirror used.
     * @param complete  paste it complete (with structure blocks) or without
     */
    public static void loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror, final boolean complete)
    {
        try
        {
        	@NotNull final Structure structure = new Structure(worldObj, name, new PlacementSettings(mirror, rotation));
            structure.setPosition(pos);
            structure.rotate(rotation, worldObj, pos, mirror);
            @NotNull final InstantStructurePlacer structureWrapper = new InstantStructurePlacer(structure);
            structureWrapper.setupStructurePlacement(pos.subtract(structure.getOffset()), complete, null);
        }
        catch (final Exception e)
        {
            Log.getLogger().warn("Could not load structure:" + name, e);
        }
    }

    /**
     * Rotates the structure x times.
     *
     * @param times     times to rotateWithMirror.
     * @param world     world it's rotating it in.
     * @param rotatePos position to rotateWithMirror it around.
     * @param mirror    the mirror to rotate with.
     */
    public void rotate(final Rotation rotation, @NotNull final World world, @NotNull final BlockPos rotatePos, @NotNull final Mirror mirror)
    {
        structure.rotate(rotation, world, rotatePos, mirror);
    }

    /**
	 * Place a structure into the world.
	 *
	 * @param world    the placing player.
	 * @param inputPos the start pos.
	 * @return the last pos.
	 */
    public BlockPos placeStructure(final World world, final BlockPos inputPos)
    {
        return placeStructure(world, null, inputPos);
    }

    /**
	 * Place a structure into the world.
	 *
	 * @param world    the placing player.
	 * @param storage  the change storage.
	 * @param inputPos the start pos.
	 * @return the last pos.
	 */
    @Override
    public BlockPos placeStructure(final World world, final ChangeStorage storage, final BlockPos inputPos)
    {
        return placeStructure(world, storage, inputPos, (structure, pos) -> structure.getBlockState(pos).getBlock() == com.ldtteam.structurize.blocks.ModBlocks.blockSubstitution
                                                                             || ((structure.getBlockState(pos).getBlock() instanceof AbstractBlockHut) && structure.getBluePrint().getPrimaryBlockOffset().equals(pos)));
    }

    /**
     * This method handles the block placement.
     * When we extract this into another mod, we have to override the method.
     *
     * @param world          the world.
     * @param pos            the world position.
     * @param localState     the local state.
     * @param complete       if complete with it.
     * @param tileEntityData the tileEntity.
     */
    @Override
    public void handleBlockPlacement(
      final World world,
      final BlockPos pos,
      final BlockState localState,
      final boolean complete,
      final CompoundNBT tileEntityData)
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
        IBuilding building = null;
        if (colony != null)
        {
            building = colony.getBuildingManager().getBuilding(structure.getPosition());
        }

        for (final IPlacementHandler handlers : PlacementHandlers.handlers)
        {
            if (handlers.canHandle(world, pos, localState))
            {
                try
                {
                    handlers.handle(world, pos, localState, tileEntityData, complete, structure.getPosition(), structure.getSettings());
                    if (building != null)
                    {
                        building.registerBlockPosition(localState, pos, world);
                    }
                }
                catch (final ClassCastException e)
                {
                    Log.getLogger().warn("Failed to place block because of classcastexception, probably banner");
                }

                return;
            }
        }
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj  the world to load it in
     * @param name      the structures name
     * @param pos       coordinates
     * @param rotations number of times rotated
     * @param mirror    the mirror used.
     * @return true if succesful.
     */
    public static boolean tryToLoadAndPlaceSupplyCampWithRotation(
      final World worldObj,
      @NotNull final String name,
      @NotNull final BlockPos pos,
      final int rotations,
      @NotNull final Mirror mirror)
    {
        try
        {
            @NotNull final InstantStructurePlacer structureWrapper = new InstantStructurePlacer(worldObj, name);
            structureWrapper.rotate(BlockPosUtil.getRotationFromRotations(rotations), worldObj, pos, mirror);
            if (structureWrapper.checkForFreeSpace(pos))
            {
                structureWrapper.placeStructure(worldObj, pos);
                return true;
            }
            return false;
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
        return false;
    }
}
