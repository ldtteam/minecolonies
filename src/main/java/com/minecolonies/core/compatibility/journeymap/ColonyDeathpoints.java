package com.minecolonies.core.compatibility.journeymap;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.IGraveData;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.tileentities.AbstractTileEntityGrave;
import journeymap.api.v2.common.waypoint.Waypoint;
import journeymap.api.v2.common.waypoint.WaypointFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_JOURNEY_MAP_INFO;

/**
 * Utility class to manage colony deathpoint mapping.
 */
public class ColonyDeathpoints
{
    private static final Map<ResourceKey<Level>, Map<Integer, Map<BlockPos, Waypoint>>> overlays = new HashMap<>();

    /**
     * Static utility class
     */
    private ColonyDeathpoints()
    {
    }

    /**
     * Clear overlay cache.  Note this does *not* remove the overlays from jmap
     */
    public static void clear()
    {
        overlays.clear();
    }

    /**
     * Clears the overlay but not the cache.
     *
     * @param dimension The dimension to unload.
     */
    public static void unload(@NotNull final Journeymap jmap,
                              @NotNull final ResourceKey<Level> dimension)
    {
        for (final Map<BlockPos, Waypoint> waypoints : overlays.getOrDefault(dimension, Collections.emptyMap()).values())
        {
            for (final Map.Entry<BlockPos, Waypoint> waypointEntry : waypoints.entrySet())
            {
                if (waypointEntry.getValue() != null)
                {
                    jmap.getApi().removeWaypoint(MOD_ID, waypointEntry.getValue());
                    waypointEntry.setValue(null);
                }
            }
        }
    }

    /**
     * Synchronise the list of currently-existing graves in the colony.
     * (Can be called when there is no change.)
     *
     * @param jmap The JourneyMap API
     * @param colony The colony.
     * @param graves The list of grave positions.
     */
    public static void updateGraves(@NotNull final Journeymap jmap,
                                    @NotNull final IColonyView colony,
                                    @NotNull final Set<BlockPos> graves)
    {
        final Map<BlockPos, Waypoint> waypoints = overlays
                .computeIfAbsent(colony.getDimension(), k -> new HashMap<>())
                .computeIfAbsent(colony.getID(), k -> new HashMap<>());
        final boolean permitted = colony.getPermissions().hasPermission(Minecraft.getInstance().player, Action.MAP_DEATHS)
                && JourneymapOptions.getDeathpoints(jmap.getOptions());

        final Iterator<Map.Entry<BlockPos, Waypoint>> iterator = waypoints.entrySet().iterator();
        while (iterator.hasNext())
        {
            final Map.Entry<BlockPos, Waypoint> waypointEntry = iterator.next();
            if (!permitted || !graves.contains(waypointEntry.getKey()))
            {
                if (waypointEntry.getValue() != null)
                {
                    jmap.getApi().removeWaypoint(MOD_ID, waypointEntry.getValue());
                }
                iterator.remove();
            }
        }

        if (permitted)
        {
            for (final BlockPos grave : graves)
            {
                waypoints.computeIfAbsent(grave, k -> tryCreatingWaypoint(jmap, colony, k));
            }
        }
    }

    /**
     * When a previously-unloaded chunk is loaded, we can refresh the deathpoint data for it.
     *
     * @param jmap The JourneyMap API
     * @param dimension The dimension of the world.  Nothing happens unless this is the client world.
     * @param chunk The chunk that was just loaded.
     */
    public static void updateChunk(@NotNull final Journeymap jmap,
                                   @NotNull final ResourceKey<Level> dimension,
                                   @NotNull final ChunkAccess chunk)
    {
        final IColonyManager colonyManager = MinecoloniesAPIProxy.getInstance().getColonyManager();

        for (final Map.Entry<Integer, Map<BlockPos, Waypoint>> colonyEntry : overlays.getOrDefault(dimension, Collections.emptyMap()).entrySet())
        {
            final IColonyView colony = colonyManager.getColonyView(colonyEntry.getKey(), dimension);
            if (colony == null)
            {
                // if the colony has ceased to exist, clear its deathpoints
                for (final Waypoint waypoint : colonyEntry.getValue().values())
                {
                    if (waypoint != null)
                    {
                        jmap.getApi().removeWaypoint(MOD_ID, waypoint);
                    }
                }
                colonyEntry.getValue().clear();
                continue;
            }

            for (final Map.Entry<BlockPos, Waypoint> waypointEntry : colonyEntry.getValue().entrySet())
            {
                if (waypointEntry.getValue() == null && chunk.getPos().equals(new ChunkPos(waypointEntry.getKey())))
                {
                    waypointEntry.setValue(tryCreatingWaypoint(jmap, colony, chunk, waypointEntry.getKey()));
                }
            }
        }
    }

    @Nullable
    private static Waypoint tryCreatingWaypoint(@NotNull final Journeymap jmap,
                                                @NotNull final IColonyView colony,
                                                @NotNull final BlockPos pos)
    {
        final ChunkPos chunkPos = new ChunkPos(pos);
        final ChunkAccess chunk = colony.getWorld().getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);

        return (chunk == null) ? null : tryCreatingWaypoint(jmap, colony, chunk, pos);
    }

    @Nullable
    private static Waypoint tryCreatingWaypoint(@NotNull final Journeymap jmap,
                                                @NotNull final IColonyView colony,
                                                @NotNull final ChunkAccess chunk,
                                                @NotNull final BlockPos pos)
    {
        final BlockEntity blockEntity = chunk.getBlockEntity(pos);
        if (blockEntity instanceof AbstractTileEntityGrave)
        {
            final IGraveData grave = ((AbstractTileEntityGrave) blockEntity).getGraveData();
            if (grave != null)
            {
                final Component text = grave.getCitizenJobName() == null
                                              ? Component.translatableEscape(PARTIAL_JOURNEY_MAP_INFO + "deathpoint_name", grave.getCitizenName())
                                              : Component.translatableEscape(PARTIAL_JOURNEY_MAP_INFO + "deathpoint_namejob", grave.getCitizenName(), grave.getCitizenJobName());
                final Waypoint waypoint = WaypointFactory.createClientWaypoint(MOD_ID, pos, text.getString(), colony.getDimension(), false);
                waypoint.setPersistent(false);
                waypoint.setIconResourceLoctaion(new ResourceLocation(MOD_ID, "textures/icons/grave_icon.png"));
                waypoint.setColor(0x888888);
                jmap.getApi().addWaypoint(MOD_ID, waypoint);
                return waypoint;
            }
        }

        return null;
    }
}
