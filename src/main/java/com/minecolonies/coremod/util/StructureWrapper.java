package com.minecolonies.coremod.util;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.blocks.AbstractBlockHut;
import com.minecolonies.coremod.blocks.BlockMinecoloniesRack;
import com.minecolonies.coremod.blocks.ModBlocks;
import com.minecolonies.coremod.blocks.schematic.BlockWaypoint;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.structurize.coremod.placementhandlers.IPlacementHandler;
import com.structurize.coremod.placementhandlers.PlacementHandlers;
import com.structurize.structures.helpers.StructureProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Mirror;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.Template;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Interface for using the structure codebase.
 */
public final class StructureWrapper extends com.structurize.coremod.util.StructureWrapper
{
    /**
     * Load a structure into this world.
     *
     * @param worldObj the world to load in
     * @param name     the structure name
     */
    public StructureWrapper(final World worldObj, final String name)
    {
        super(worldObj, new StructureProxy(worldObj, name), name);
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
            @NotNull final StructureWrapper structureWrapper = new StructureWrapper(worldObj, name);
            structureWrapper.position = pos;
            structureWrapper.rotate(rotations, worldObj, pos, mirror);
            structureWrapper.placeStructure(pos.subtract(structureWrapper.getOffset()), complete);
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
        structure.rotateWithMirror(times, world, rotatePos, mirror);
    }

    /**
     * Place a structure into the world.
     *
     * @param pos      coordinates
     * @param complete paste it complete (with structure blocks) or without
     */
    private void placeStructure(@NotNull final BlockPos pos, final boolean complete)
    {
        setLocalPosition(pos);

        @NotNull final List<BlockPos> delayedBlocks = new ArrayList<>();

        //structure.getBlockInfo()[0].pos
        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final IBlockState localState = this.structure.getBlockState(localPos);
                    final Block localBlock = localState.getBlock();

                    final BlockPos worldPos = pos.add(localPos);

                    if ((localBlock == com.structurize.coremod.blocks.ModBlocks.blockSubstitution && !complete) || localBlock instanceof AbstractBlockHut)
                    {
                        continue;
                    }

                    if (localState.getMaterial().isSolid())
                    {
                        handleBlockPlacement(worldPos, localState, complete, this.structure.getBlockInfo(localPos).tileentityData, world);
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
            final IBlockState localState = this.structure.getBlockState(coords);
            final BlockPos newWorldPos = pos.add(coords);

            handleBlockPlacement(newWorldPos, localState, complete, this.structure.getBlockInfo(coords).tileentityData, world);
        }

        for (int j = 0; j < structure.getHeight(); j++)
        {
            for (int k = 0; k < structure.getLength(); k++)
            {
                for (int i = 0; i < structure.getWidth(); i++)
                {
                    @NotNull final BlockPos localPos = new BlockPos(i, j, k);
                    final Template.EntityInfo info = this.structure.getEntityinfo(localPos);

                    if (info != null)
                    {
                        try
                        {
                            final Entity entity = EntityList.createEntityFromNBT(info.entityData, world);
                            entity.setUniqueId(UUID.randomUUID());
                            world.spawnEntity(entity);
                        }
                        catch (final RuntimeException e)
                        {
                            Log.getLogger().info("Couldn't restore entity", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method handles the block placement.
     * When we extract this into another mod, we have to override the method.
     *
     * @param pos the world position.
     * @param localState the local state.
     * @param complete if complete with it.
     * @param tileEntityData the tileEntity.
     * @param world the world it is being placed in.
     */
    private void handleBlockPlacement(final BlockPos pos, final IBlockState localState, final boolean complete, final NBTTagCompound tileEntityData, final World world)
    {
        for (final IPlacementHandler handlers : PlacementHandlers.handlers)
        {
            if (handlers.canHandle(world, pos, localState))
            {
                final Object result = handlers.handle(world, pos, localState, tileEntityData, complete, position);
                if (result instanceof IBlockState)
                {
                    final IBlockState blockState = (IBlockState) result;

                    final Colony colony = ColonyManager.getColonyByPosFromWorld(world, pos);
                    if (colony != null)
                    {
                        final AbstractBuilding building = colony.getBuildingManager().getBuilding(position);

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
                                                                   final World worldObj, @NotNull final String name,
                                                                   @NotNull final BlockPos pos, final int rotations, @NotNull final Mirror mirror)
    {
        try
        {
            @NotNull final StructureWrapper structureWrapper = new StructureWrapper(worldObj, name);
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

    /**
     * Checks if the block in the world is the same as what is in the structure.
     *
     * @return true if the structure block equals the world block.
     */
    public boolean isStructureBlockEqualWorldBlock()
    {
        final IBlockState structureBlockState = structure.getBlockState(this.getLocalPosition());
        final Block structureBlock = structureBlockState.getBlock();

        //All worldBlocks are equal the substitution block
        if (structureBlock == com.structurize.coremod.blocks.ModBlocks.blockSubstitution)
        {
            return true;
        }

        final BlockPos worldPos = this.getBlockPosition();

        final IBlockState worldBlockState = world.getBlockState(worldPos);

        if (structureBlock == com.structurize.coremod.blocks.ModBlocks.blockSolidSubstitution && worldBlockState.getMaterial().isSolid())
        {
            return true;
        }

        final Block worldBlock = worldBlockState.getBlock();

        //list of things to only check block for.
        //For the time being any flower pot is equal to each other.
        if (structureBlock instanceof BlockDoor || structureBlock == Blocks.FLOWER_POT)
        {
            return structureBlock == worldBlock;
        }
        else if (worldBlock == ModBlocks.blockRack)
        {
            return BlockMinecoloniesRack.shouldBlockBeReplacedWithRack(structureBlock);
        }
        else if ((structureBlock instanceof BlockStairs && structureBlockState == worldBlockState)
                   || BlockUtils.isGrassOrDirt(structureBlock, worldBlock, structureBlockState, worldBlockState)
                   || structureBlock instanceof BlockWaypoint)
        {
            return true;
        }

        final Template.EntityInfo entityInfo = structure.getEntityinfo(this.getLocalPosition());
        if (entityInfo != null)
        {
            return false;
            //todo get entity at position.
        }

        //had this problem in a super flat world, causes builder to sit doing nothing because placement failed
        return worldPos.getY() <= 0
                 || structureBlockState == worldBlockState;
    }
}
