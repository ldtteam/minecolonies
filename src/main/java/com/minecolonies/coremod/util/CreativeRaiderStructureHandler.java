package com.minecolonies.coremod.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider;
import com.ldtteam.structurize.management.Manager;
import com.ldtteam.structurize.placement.StructurePlacer;
import com.ldtteam.structurize.placement.structure.CreativeStructureHandler;
import com.ldtteam.structurize.placement.structure.IStructureHandler;
import com.ldtteam.structurize.util.BlockInfo;
import com.ldtteam.structurize.util.PlacementSettings;
import com.ldtteam.structurize.util.TickedWorldOperation;
import com.minecolonies.api.colony.colonyEvents.IColonyRaidEvent;
import com.minecolonies.api.util.BlockPosUtil;
import com.minecolonies.coremod.colony.colonyEvents.raidEvents.pirateEvent.ShipBasedRaiderUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ldtteam.structurize.blocks.interfaces.IBlueprintDataProvider.TAG_BLUEPRINTDATA;

/**
 * Raider specific creative structure handler.
 * Will correctly place spawners
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
     * @param world the world.
     * @param pos the pos it is placed at.
     * @param structureName the name of the structure.
     * @param settings the placement settings.
     * @param fancyPlacement if fancy or complete.
     * @param event the raid event.
     * @param colonyId the colony id.
     */
    public CreativeRaiderStructureHandler(final World world, final BlockPos pos, final String structureName, final PlacementSettings settings, final boolean fancyPlacement, final IColonyRaidEvent event, final int colonyId)
    {
        super(world, pos, structureName, settings, fancyPlacement);
        getBluePrint().rotateWithMirror(settings.getRotation(), settings.getMirror(), world);

        this.event = event;
        this.colonyId = colonyId;
        final BlockInfo info = getBluePrint().getBlockInfoAsMap().getOrDefault(getBluePrint().getPrimaryBlockOffset(), null);
        if (info.getTileEntityData() != null)
        {
            final CompoundNBT teData = getBluePrint().getTileEntityData(pos, getBluePrint().getPrimaryBlockOffset());
            if (teData != null && teData.contains(TAG_BLUEPRINTDATA))
            {
                final TileEntity entity = TileEntity.create(info.getTileEntityData());
                if (entity instanceof IBlueprintDataProvider)
                {
                    entity.setPos(pos);
                    this.map = ((IBlueprintDataProvider) entity).getWorldTagPosMap();
                }
            }
        }
    }

    @Override
    public void triggerSuccess(final BlockPos pos, final List<ItemStack> list, final boolean placement)
    {
        super.triggerSuccess(pos, list, placement);
        final BlockPos worldPos = getProgressPosInWorld(pos);
        if (getWorld().getBlockState(worldPos).getBlock() == Blocks.GOLD_BLOCK)
        {
            final List<String> tags = map.getOrDefault(worldPos, Collections.emptyList());
            for (final String tag: tags)
            {
                switch (tag)
                {
                    case "normal":
                        ShipBasedRaiderUtils.setupSpawner(worldPos, getWorld(), event.getNormalRaiderType(), event, colonyId);
                        return;
                    case "archer":
                        ShipBasedRaiderUtils.setupSpawner(worldPos, getWorld(), event.getArcherRaiderType(), event, colonyId);
                        return;
                    case "boss":
                        ShipBasedRaiderUtils.setupSpawner(worldPos, getWorld(), event.getBossRaiderType(), event, colonyId);
                        return;
                }
            }
        }
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
     * @param colonyId the colony id.
     * @param event the raid event.
     * @param player   the placing player.
     * @return the placed blueprint.
     */
    public static Blueprint loadAndPlaceStructureWithRotation(
      final World worldObj, @NotNull final String name,
      @NotNull final BlockPos pos, final Rotation rotation,
      @NotNull final Mirror mirror,
      final boolean fancyPlacement, final int colonyId, final IColonyRaidEvent event,
      @Nullable final ServerPlayerEntity player)
    {
        try
        {
            @NotNull final IStructureHandler structure = new CreativeRaiderStructureHandler(worldObj, pos, name, new PlacementSettings(mirror, rotation), fancyPlacement, event, colonyId);
            if (structure.hasBluePrint())
            {
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
}
