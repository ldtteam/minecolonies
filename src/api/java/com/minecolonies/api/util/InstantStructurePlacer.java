package com.minecolonies.api.util;

import com.ldtteam.structures.helpers.Structure;
import com.ldtteam.structurize.placementhandlers.IPlacementHandler;
import com.ldtteam.structurize.placementhandlers.PlacementHandlers;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        super(new Structure(worldObj, name, new PlacementSettings()));
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
      @NotNull final BlockPos pos, final int rotations, @NotNull final Mirror mirror,
      final boolean complete)
    {
        try
        {
            @NotNull final InstantStructurePlacer structureWrapper = new InstantStructurePlacer(worldObj, name);
            structureWrapper.structure.setPosition(pos);
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            structureWrapper.structure.setPlacementSettings(new PlacementSettings(mirror, BlockPosUtil.getRotationFromRotations(rotations)));
            structureWrapper.placeStructure(pos.subtract(structureWrapper.structure.getOffset()), complete);
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
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
    public void rotate(final int times, @NotNull final World world, @NotNull final BlockPos rotatePos, @NotNull final Mirror mirror)
    {
        structure.rotate(BlockPosUtil.getRotationFromRotations(times), world, rotatePos, mirror);
    }

    /**
     * Place a structure into the world.
     *
     * @param pos      coordinates
     * @param complete paste it complete (with structure blocks) or without
     */
    private void placeStructure(@NotNull final BlockPos pos, final boolean complete)
    {
        structure.setLocalPosition(pos);

        @NotNull final List<BlockPos> delayedBlocks = new ArrayList<>();

        //structure.getBlockInfo()[0].pos
        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final BlockState localState = this.structure.getBlockState(localPos).getBlockState();
                    final Block localBlock = localState.getBlock();

                    final BlockPos worldPos = pos.add(localPos);

                    if ((localBlock == com.ldtteam.structurize.blocks.ModBlocks.blockSubstitution && !complete) || (localBlock instanceof AbstractBlockHut && !complete))
                    {
                        continue;
                    }

                    if (localState.getMaterial().isSolid())
                    {
                        handleBlockPlacement(worldPos, localState, complete, this.structure.getBlockInfo(localPos).getTileEntityData(), structure.getWorld());
                    }
                    else
                    {
                        delayedBlocks.add(localPos);
                    }
                }
            }
        }

        for (@NotNull final BlockPos coords : delayedBlocks)
        {
            final BlockState localState = this.structure.getBlockState(coords).getBlockState();
            final BlockPos newWorldPos = pos.add(coords);

            handleBlockPlacement(newWorldPos, localState, complete, this.structure.getBlockInfo(coords).getTileEntityData(), structure.getWorld());
        }

        for (final CompoundNBT compound : this.structure.getEntityData())
        {
            if (compound != null)
            {
                try
                {
                    final Optional<EntityType<?>> entityType = EntityType.readEntityType(compound);
                    if (entityType.isPresent())
                    {
                        final Entity entity = entityType.get().create(structure.getWorld());
                        entity.setUniqueId(UUID.randomUUID());
                        final Vec3d worldPos = entity.getPositionVector().add(pos.getX(), pos.getY(), pos.getZ());
                        entity.setPosition(worldPos.x, worldPos.y, worldPos.z);
                        structure.getWorld().addEntity(entity);
                    }
                }
                catch (final RuntimeException e)
                {
                    Log.getLogger().info("Couldn't restore entitiy", e);
                }
            }
        }
    }

    /**
     * This method handles the block placement.
     * When we extract this into another mod, we have to override the method.
     *
     * @param pos            the world position.
     * @param localState     the local state.
     * @param complete       if complete with it.
     * @param tileEntityData the tileEntity.
     * @param world          the world it is being placed in.
     */
    private void handleBlockPlacement(final BlockPos pos, final BlockState localState, final boolean complete, final CompoundNBT tileEntityData, final World world)
    {
        for (final IPlacementHandler handlers : PlacementHandlers.handlers)
        {
            if (handlers.canHandle(world, pos, localState))
            {
                final Object result = handlers.handle(world, pos, localState, tileEntityData, complete, structure.getLocalPosition(), structure.getSettings());
                if (result instanceof BlockState)
                {
                    final BlockState blockState = (BlockState) result;

                    final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(world, pos);
                    if (colony != null)
                    {
                        final IBuilding building = colony.getBuildingManager().getBuilding(structure.getPosition());

                        if (building != null)
                        {
                            building.registerBlockPosition(blockState, pos, world);
                        }
                    }

                    return;
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
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            if (structureWrapper.checkForFreeSpace(pos))
            {
                structureWrapper.placeStructure(pos, false);
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
