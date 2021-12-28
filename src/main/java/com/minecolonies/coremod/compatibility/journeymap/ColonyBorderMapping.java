package com.minecolonies.coremod.compatibility.journeymap;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyTagCapability;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.coremod.MineColonies.CLOSE_COLONY_CAP;

/**
 * Utility class to manage colony border mapping.
 */
public class ColonyBorderMapping
{
    private static final Map<ResourceKey<Level>, Map<Integer, ColonyBorderOverlay>> overlays = new HashMap<>();

    /**
     * Static utility class
     */
    private ColonyBorderMapping()
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
    public static void unload(@NotNull final IClientAPI jmap,
                              @NotNull final ResourceKey<Level> dimension)
    {
        for (final ColonyBorderOverlay overlay : ColonyBorderMapping.overlays.getOrDefault(dimension, Collections.emptyMap()).values())
        {
            overlay.unload(jmap);
        }
    }

    /**
     * Flags the colony border overlay for update, if needed for a single just-loaded chunk.
     *
     * @param jmap The JourneyMap API
     * @param dimension The dimension of the world.  Nothing happens unless this is the client world.
     * @param chunk The chunk that was just loaded.
     */
    public static void updateChunk(@NotNull final IClientAPI jmap,
                                   @NotNull final ResourceKey<Level> dimension,
                                   @NotNull final LevelChunk chunk)
    {
        final Level world = Minecraft.getInstance().level;
        if (world == null || !dimension.equals(world.dimension())) return;

        final int id = getOwningColonyForChunk(chunk);
        if (id == 0)
        {
            for (final Map<Integer, ColonyBorderOverlay> overlayMap : overlays.values())
            {
                for (final ColonyBorderOverlay overlay : overlayMap.values())
                {
                    overlay.updateChunks(Collections.emptySet(), Collections.singleton(chunk.getPos()));
                }
            }
        }
        else
        {
            final IColonyManager colonyManager = MinecoloniesAPIProxy.getInstance().getColonyManager();
            final IColonyView colony = colonyManager.getColonyView(id, dimension);

            final ColonyBorderOverlay overlay = overlays
                    .computeIfAbsent(dimension, k -> new HashMap<>())
                    .computeIfAbsent(id, k -> new ColonyBorderOverlay(dimension, id));
            overlay.updateChunks(Collections.singleton(chunk.getPos()), Collections.emptySet());
            overlay.updateInfo(colony);
        }
    }

    /**
     * Check if any colony border overlays need to be updated.
     *
     * @param jmap The Journeymap API
     * @param dimension The dimension to check
     */
    public static void updatePending(@NotNull final IClientAPI jmap,
                                     @NotNull final ResourceKey<Level> dimension)
    {
        final IColonyManager colonyManager = MinecoloniesAPIProxy.getInstance().getColonyManager();

        for (final Map.Entry<Integer, ColonyBorderOverlay> colonyEntry : overlays.getOrDefault(dimension, Collections.emptyMap()).entrySet())
        {
            colonyEntry.getValue().updatePending(jmap, dimension, colonyEntry.getKey(), colonyManager);
        }
    }

    private static int getOwningColonyForChunk(@NotNull final LevelChunk chunk)
    {
        final IColonyTagCapability cap = chunk.getCapability(CLOSE_COLONY_CAP).resolve().orElse(null);
        if (cap == null || cap.getOwningColony() < 1)
        {
            return 0;
        }
        return cap.getOwningColony();
    }

    private static class ColonyBorderOverlay
    {
        private final ResourceKey<Level> dimension;
        private final String name;
        private final Set<ChunkPos> chunks;
        private final List<PolygonOverlay> overlays = new ArrayList<>();
        private final List<PolygonOverlay> borders = new ArrayList<>();
        private final ShapeProperties fill;
        private final ShapeProperties stroke;
        private final TextProperties text;

        private boolean dirty = false;
        private String colonyName = "";

        public ColonyBorderOverlay(@NotNull final ResourceKey<Level> dimension,
                                   final int id)
        {
            this.dimension = dimension;
            this.name = String.format("colony_%s_%d", dimension.location(), id);
            this.chunks = new HashSet<>();

            this.stroke = new ShapeProperties()
                    .setStrokeWidth(4).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                    .setFillColor(0xff00ff).setFillOpacity(0);
            this.fill = new ShapeProperties()
                    .setStrokeWidth(2).setStrokeColor(0xff00ff).setStrokeOpacity(0)
                    .setFillColor(0x00ff00).setFillOpacity(.2f);

            this.text = new TextProperties()
                    .setBackgroundColor(0x000022)
                    .setBackgroundOpacity(.5f)
                    .setColor(0x00ff00)
                    .setOpacity(1f)
                    .setMinZoom(0)
                    .setMaxZoom(2)
                    .setScale(2f)
                    .setActiveUIs(EnumSet.of(Context.UI.Fullscreen, Context.UI.Webmap))
                    .setFontShadow(true);
        }

        public void updateChunks(@NotNull final Set<ChunkPos> addChunks,
                                 @NotNull final Set<ChunkPos> removeChunks)
        {
            this.dirty |= this.chunks.addAll(addChunks);               // new owned chunks
            this.dirty |= this.chunks.removeAll(removeChunks);         // new disowned chunks
        }

        public void updateInfo(@Nullable final IColonyView colony)
        {
            if (colony != null)
            {
                //noinspection ConstantConditions
                final int colour = colony.getTeamColonyColor().getColor();
                this.fill.setFillColor(colour);
                this.stroke.setStrokeColor(colour);
                this.text.setColor(colour);
                //noinspection ConstantConditions
                this.text.setBackgroundColor(colour == ChatFormatting.BLACK.getColor() ? 0xDDDDDD : 0x000022);

                this.colonyName = colony.getName();
                for (final PolygonOverlay overlay : this.borders)
                {
                    overlay.setLabel(this.colonyName);
                }
            }
        }

        public void updatePending(@NotNull final IClientAPI jmap,
                                  @NotNull final ResourceKey<Level> dimension,
                                  final int id,
                                  @NotNull final IColonyManager colonyManager)
        {
            final IColonyView colony = colonyManager.getColonyView(id, dimension);
            final boolean permitted = colony != null && colony.getPermissions().hasPermission(Minecraft.getInstance().player, Action.MAP_BORDER);
            final boolean enabled = permitted && MinecoloniesAPIProxy.getInstance().getConfig().getClient().mapColonyBorders.get();

            this.dirty |= !enabled && !this.overlays.isEmpty();                         // freshly disabled; remove
            this.dirty |= enabled && this.overlays.isEmpty() && !this.chunks.isEmpty(); // freshly enabled; add

            if (this.dirty)
            {
                this.dirty = false;

                unload(jmap);

                if (!this.chunks.isEmpty() && enabled && jmap.playerAccepts(MOD_ID, DisplayType.Polygon))
                {
                    final List<PolygonUtils.PolygonWithHoles> polygons = PolygonUtils.createPolygonFromArea(PolygonUtils.createChunksArea(this.chunks), 256);

                    int index = 0;
                    for (final PolygonUtils.PolygonWithHoles polygon : polygons)
                    {
                        // unfortunately JourneyMap can't actually handle concave polygons with holes at present,
                        // so we need to break them up for it...
                        final List<MapPolygon> triangles = PolygonUtils.triangulate(polygon);
                        for (final MapPolygon triangle : triangles)
                        {
                            final PolygonOverlay overlay = new PolygonOverlay(MOD_ID, String.format("%s_%s", this.name, ++index), this.dimension, this.fill, triangle);
                            overlay.setOverlayGroupName(this.name)
                                    .setActiveUIs(EnumSet.of(Context.UI.Fullscreen, Context.UI.Webmap));
                            this.overlays.add(overlay);
                            showOverlay(jmap, overlay);
                        }

                        // also add a stroked border, since we can't do that with the triangles
                        final PolygonOverlay border = new PolygonOverlay(MOD_ID, String.format("%s_%s", this.name, ++index), this.dimension, this.stroke, polygon.hull);
                        border.setOverlayGroupName(this.name)
                                .setTextProperties(this.text)
                                .setLabel(this.colonyName);
                        this.borders.add(border);
                        showOverlay(jmap, border);

                        // also add borders around the holes
                        for (final MapPolygon hole : polygon.holes)
                        {
                            final PolygonOverlay border2 = new PolygonOverlay(MOD_ID, String.format("%s_%s", this.name, ++index), this.dimension, this.stroke, hole);
                            border2.setOverlayGroupName(this.name);
                            this.overlays.add(border2);     // not "borders", because we don't want text
                            showOverlay(jmap, border2);
                        }
                    }
                }
            }
        }

        private static void showOverlay(@NotNull final IClientAPI jmap,
                                        @NotNull final PolygonOverlay overlay)
        {
            try
            {
                jmap.show(overlay);
            }
            catch (final Throwable t)
            {
                // this is already logged by JourneyMap but the API still wants us to catch
            }
        }

        public void unload(@NotNull final IClientAPI jmap)
        {
            for (final PolygonOverlay overlay : this.overlays)
            {
                jmap.remove(overlay);
            }
            for (final PolygonOverlay overlay : this.borders)
            {
                jmap.remove(overlay);
            }
            this.overlays.clear();
            this.borders.clear();
        }
    }
}
