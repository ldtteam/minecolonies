package com.minecolonies.api.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.IBuilding;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface for using the structure codebase.
 */
public final class MCCreativeStructureHandler extends CreativeStructureHandler
{
    /**
     * The building associated with this placement.
     */
    private IBuilding building;

    /**
     * The minecolonies specific creative structure placer.
     * @param world the world.
     * @param pos the pos it is placed at.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     * @param fancyPlacement if fancy or complete.
     */
    public MCCreativeStructureHandler(final World world, final BlockPos pos, final String structureName, final PlacementSettings settings, final boolean fancyPlacement)
    {
        super(world, pos, structureName, settings, fancyPlacement);
        setupBuilding();
    }

    /**
     * Setup the building to register things to.
     */
    private void setupBuilding()
    {
        final IColony colony = IColonyManager.getInstance().getColonyByPosFromWorld(getWorld(), getWorldPos());
        if (colony != null)
        {
            this.building = colony.getBuildingManager().getBuilding(getWorldPos());
        }
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> list, final boolean placement)
    {
        super.triggerSuccess(pos, list, placement);
        final BlockPos worldPos = getProgressPosInWorld(pos);
        if (building != null)
        {
            building.registerBlockPosition(getBluePrint().getBlockState(pos), worldPos, this.getWorld());
        }
    }

    @Override
    public boolean shouldBlocksBeConsideredEqual(final BlockState state1, final BlockState state2)
    {
        final Block block1 = state1.getBlock();
        final Block block2 = state2.getBlock();

        if (block1 == Blocks.FLOWER_POT || block2 == Blocks.FLOWER_POT)
        {
            return block1 == block2;
        }

        if (block1 == Blocks.GRASS_BLOCK && block2 == Blocks.DIRT || block2 == Blocks.GRASS_BLOCK && block1 == Blocks.DIRT)
        {
            return true;
        }


        return super.shouldBlocksBeConsideredEqual(state1, state2);
    }

    @Override
    public boolean isStackFree(@Nullable final ItemStack itemStack)
    {
        return itemStack == null
                 ||itemStack.isEmpty()
                 || itemStack.getItem().isIn(ItemTags.LEAVES)
                 || itemStack.getItem() == new ItemStack(ModBlocks.blockDecorationPlaceholder, 1).getItem();
    }

    /**
     * Load a structure into this world
     * and place it in the right position and rotation.
     *
     * @param worldObj the world to load it in
     * @param name     the structures name
     * @param pos      coordinates
     * @param rotation the rotation.
     * @param mirror   the mirror used.
     * @param fancyPlacement if fancy or complete.
     * @param player   the placing player.
     * @return the placed blueprint.
     */
    public static Blueprint loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      @Nullable final ServerPlayerEntity player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new MCCreativeStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement);
            structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

            @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
            Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
            return structure.getBluePrint();
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
        return null;
    }
}
