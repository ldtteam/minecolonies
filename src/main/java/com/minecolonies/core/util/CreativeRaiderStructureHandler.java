package com.minecolonies.core.util;

import com.ldtteam.structurize.api.Log;
import com.ldtteam.structurize.api.RotationMirror;
import com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.operations.PlaceStructureOperation;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockInfo;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.core.colony.events.raid.pirateEvent.ShipBasedRaiderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static com.ldtteam.structurize.blockentities.interfaces.IBlueprintDataProviderBE.TAG_BLUEPRINTDATA;
import static com.minecolonies.api.util.constant.SchematicTagConstants.*;

/**
 * Raider specific creative structure handler. Will correctly place spawners
 */
public final class CreativeRaiderStructureHandler extends CreativeStructureHandler
{
    /**
     * Map for the spawner positions.
     */
    private Map<BlockPos, List<String>> map;

    /**
     * The raid event associated to this.
     */
    private IColonyRaidEvent event;

    /**
     * The colony id.
     */
    private int colonyId;

    /**
     * The minecolonies specific creative structure placer.
     *
     * @param world          the world.
     * @param pos            the pos it is placed at.
     * @param blueprintFuture  the blueprint future of the structure.
     * @param rotMir       the placement settings.
     * @param fancyPlacement if fancy or complete.
     * @param event          the raid event.
     * @param colonyId       the colony id.
     */
    public CreativeRaiderStructureHandler(
      final Level world,
      final BlockPos pos,
      final Future<Blueprint> blueprintFuture,
      final RotationMirror rotMir,
      final boolean fancyPlacement,
      final IColonyRaidEvent event,
      final int colonyId)
    {
        super(world, pos, blueprintFuture, rotMir, fancyPlacement);

        this.event = event;
        this.colonyId = colonyId;
        final BlockInfo info = getBluePrint().getBlockInfoAsMap().getOrDefault(getBluePrint().getPrimaryBlockOffset(), null);
        if (info.getTileEntityData() != null)
        {
            final CompoundTag teData = getBluePrint().getTileEntityData(pos, getBluePrint().getPrimaryBlockOffset());
            if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
            {
                final BlockEntity entity = BlockEntity.loadStatic(pos, info.getState(), info.getTileEntityData(), world.registryAccess());
                if (entity instanceof IBlueprintDataProviderBE)
                {
                    this.map = ((IBlueprintDataProviderBE) entity).getWorldTagPosMap();
                }
            }
        }

        if (map == null)
        {
            Log.getLogger().error("Raider spawned without matching blueprint data for it: " + blueprintFuture);
        }
    }

    /**
     * The minecolonies specific creative structure placer.
     *
     * @param world          the world.
     * @param pos            the pos it is placed at.
     * @param blueprint      the blueprint of the structure.
     * @param rotMir       the placement settings.
     * @param fancyPlacement if fancy or complete.
     * @param event          the raid event.
     * @param colonyId       the colony id.
     */
    public CreativeRaiderStructureHandler(
      final Level world,
      final BlockPos pos,
      final Blueprint blueprint,
      final RotationMirror rotMir,
      final boolean fancyPlacement,
      final IColonyRaidEvent event,
      final int colonyId)
    {
        super(world, pos, blueprint, rotMir, fancyPlacement);

        this.event = event;
        this.colonyId = colonyId;
        final BlockInfo info = getBluePrint().getBlockInfoAsMap().getOrDefault(getBluePrint().getPrimaryBlockOffset(), null);
        if (info.getTileEntityData() != null)
        {
            final CompoundTag teData = getBluePrint().getTileEntityData(pos, getBluePrint().getPrimaryBlockOffset());
            if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
            {
                final BlockEntity entity = BlockEntity.loadStatic(pos, info.getState(), info.getTileEntityData(), world.registryAccess());
                if (entity instanceof IBlueprintDataProviderBE)
                {
                    this.map = ((IBlueprintDataProviderBE) entity).getWorldTagPosMap();
                }
            }
        }

        if (map == null)
        {
            Log.getLogger().error("Raider spawned without matching blueprint data for it: " + blueprint);
        }
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> list, final boolean placement)
    {
        super.triggerSuccess(pos, list, placement);
        final BlockPos worldPos = getProgressPosInWorld(pos);
        if (getWorld().getBlockState(worldPos).getBlock() == Blocks.GOLD_BLOCK && map != null)
        {
            final List<String> tags = map.getOrDefault(worldPos, Collections.emptyList());
            for (final String tag : tags)
            {
                switch (tag)
                {
                    case NORMAL_RAIDER:
                        ShipBasedRaiderUtils.setupSpawner(worldPos, getWorld(), event.getNormalRaiderType(), event, colonyId);
                        return;
                    case ARCHER_RAIDER:
                        ShipBasedRaiderUtils.setupSpawner(worldPos, getWorld(), event.getArcherRaiderType(), event, colonyId);
                        return;
                    case BOSS_RAIDER:
                        ShipBasedRaiderUtils.setupSpawner(worldPos, getWorld(), event.getBossRaiderType(), event, colonyId);
                        return;
                }
            }
        }
    }

    /**
     * Load a structure into this world and place it in the right position and rotation.
     *
     * @param worldObj       the world to load it in
     * @param blueprintFuture the blueprint future
     * @param pos            coordinates
     * @param fancyPlacement if fancy or complete.
     * @param colonyId       the colony id.
     * @param event          the raid event.
     * @param player         the placing player.
     */
    public static void loadAndPlaceStructure(
      final Level worldObj, @NotNull final Future<Blueprint> blueprintFuture,
      @NotNull final BlockPos pos,
      final boolean fancyPlacement, final int colonyId, final IColonyRaidEvent event,
      @Nullable final ServerPlayer player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeRaiderStructureHandler(worldObj, pos, blueprintFuture, RotationMirror.NONE, fancyPlacement, event, colonyId);
            Manager.addToQueue(new PlaceStructureOperation(new StructurePlacer(structure), player));
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }

    /**
     * Load a structure into this world and place it in the right position and rotation.
     *
     * @param worldObj       the world to load it in
     * @param blueprint the blueprint
     * @param pos            coordinates
     * @param fancyPlacement if fancy or complete.
     * @param colonyId       the colony id.
     * @param event          the raid event.
     * @param player         the placing player.
     */
    public static void loadAndPlaceStructure(
      final Level worldObj, @NotNull final Blueprint blueprint,
      @NotNull final BlockPos pos,
      final boolean fancyPlacement, final int colonyId, final IColonyRaidEvent event,
      @Nullable final ServerPlayer player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeRaiderStructureHandler(worldObj, pos, blueprint, RotationMirror.NONE, fancyPlacement, event, colonyId);
            Manager.addToQueue(new PlaceStructureOperation(new StructurePlacer(structure), player));
        }
        catch (final IllegalStateException e)
        {
            Log.getLogger().warn("Could not load structure!", e);
        }
    }
}
