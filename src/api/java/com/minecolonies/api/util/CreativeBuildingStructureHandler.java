package com.minecolonies.api.util;

import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;

/**
 * Minecolonies specific creative structure handler. Main difference related to registering blocks to colonies.
 */
public final class CreativeBuildingStructureHandler extends CreativeStructureHandler
{
    /**
     * The building associated with this placement.
     */
    private IBuilding building;

    /**
     * The minecolonies specific creative structure placer.
     *
     * @param world          the world.
     * @param pos            the pos it is placed at.
     * @param structureName  the name of the structure.
     * @param settings       the placement settings.
     * @param fancyPlacement if fancy or complete.
     */
    public CreativeBuildingStructureHandler(final Level world, final BlockPos pos, final String structureName, final PlacementSettings settings, final boolean fancyPlacement)
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

        final CompoundTag teData = getBluePrint().getTileEntityData(worldPos, pos);
        if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
        {
            final BlockEntity te = getWorld().getBlockEntity(worldPos);
            if (te != null)
            {
                ((IBlueprintDataProvider) te).readSchematicDataFromNBT(teData);
                ((ServerLevel) getWorld()).getChunkSource().blockChanged(worldPos);
                te.setChanged();
            }
        }

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
                 || itemStack.isEmpty()
                 || itemStack.is(ItemTags.LEAVES)
                 || itemStack.getItem() == new ItemStack(ModBlocks.blockDecorationPlaceholder, 1).getItem();
    }

    /**
     * Load a structure into this world and place it in the right position and rotation.
     *
     * @param worldObj       the world to load it in
     * @param name           the structures name
     * @param pos            coordinates
     * @param rotation       the rotation.
     * @param mirror         the mirror used.
     * @param fancyPlacement if fancy or complete.
     * @param player         the placing player.
     * @return the placed blueprint.
     */
    public static Blueprint loadAndPlaceStructureWithRotation(
      final Level worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      @Nullable final ServerPlayer player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeBuildingStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement);
            if (structure.hasBluePrint())
            {
                structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

                @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
                Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
            }
            return structure.getBluePrint();
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
        return null;
    }

    public static Blueprint loadAndPlaceStructureWithRotationWithoutPrimaryBlockOffset(
      final Level worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      @Nullable final ServerPlayer player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeBuildingStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement);
            if (structure.hasBluePrint())
            {
                structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);
                structure.getBluePrint().setCachePrimaryOffset(BlockPos.ZERO);

                @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
                Manager.addToQueue(new TickedWorldOperation(instantPlacer, player));
            }
            return structure.getBluePrint();
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
        return null;
    }

    /**
     * Load a structure into this world and place it in the right position and rotation.
     *
     * @param worldObj       the world to load it in
     * @param name           the structures name
     * @param pos            coordinates
     * @param rotation       the rotation.
     * @param mirror         the mirror used.
     * @param fancyPlacement if fancy or complete.
     * @param player         the placing player.
     * @return the placed blueprint.
     */
    public static Blueprint loadAndPlaceStructureWithRotationImmediatly(
      final ServerLevel worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement,
      @Nullable final ServerPlayer player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeBuildingStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement);
            if (structure.hasBluePrint())
            {
                structure.getBluePrint().rotateWithMirror(rotation, mirror, worldObj);

                @NotNull final StructurePlacer instantPlacer = new StructurePlacer(structure);
                final TickedWorldOperation operation = new TickedWorldOperation(instantPlacer, player);
                while(!operation.apply(worldObj)) {
                }
            }
            return structure.getBluePrint();
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
        return null;
    }
}
