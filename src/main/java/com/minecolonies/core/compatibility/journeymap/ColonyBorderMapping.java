package com.minecolonies.core.compatibility.journeymap;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.claim.IChunkClaimData;
import com.minecolonies.api.colony.permissions.Action;
import com.minecolonies.api.util.ColonyUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.AbstractInt2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import journeymap.api.v2.client.display.Context;
import journeymap.api.v2.client.display.DisplayType;
import journeymap.api.v2.client.display.PolygonOverlay;
import journeymap.api.v2.client.model.MapPolygonWithHoles;
import journeymap.api.v2.client.model.ShapeProperties;
import journeymap.api.v2.client.model.TextProperties;
import journeymap.api.v2.client.util.PolygonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Utility class to manage colony border mapping.
 */
public class ColonyBorderMapping
{
    /**
     * Number of chunk update entries to process per tick, to avoid over-processing.
     */
    private static final int UPDATES_PER_TICK = 250;

    private static final Map<ResourceKey<Level>, Int2ObjectRBTreeMap<ColonyBorderOverlay>> overlays = new HashMap<>();
    private static final Queue<ChunkOwnership> pendingClaims = new ArrayListDeque<>();
    private static ResourceKey<Level> pendingClaimsDimension;

    private static final Codec<List<ColonyBorderOverlay>> DIM_BORDER_CODEC = ColonyBorderOverlay.CODEC.listOf();

    /**
     * Static utility class
     */
    private ColonyBorderMapping()
    {
    }

    /**
     * Report the colony that the current player is in, if any.
     *
     * @return The colony name, or an empty string.
     */
    public static String getCurrentColony()
    {
        final BlockPos pos = Minecraft.getInstance().player.blockPosition();
        final IColony colony = IColonyManager.getInstance().getIColony(Minecraft.getInstance().level, pos);
        return colony != null ? colony.getName() : "";
    }

    /**
     * Loads cached colony data, if any.  Also starts tracking data for a dimension.
     */
    public static void load(@NotNull final Journeymap jmap,
                            @NotNull final ResourceKey<Level> dimension)
    {
        if (overlays.containsKey(dimension)) return;    // don't bother reloading

        final AbstractInt2ObjectMap<ColonyBorderOverlay> dimensionOverlays =
                overlays.computeIfAbsent(dimension, k -> new Int2ObjectRBTreeMap<>());

        final Path dataPath = jmap.getDataPath(dimension).resolve("border.json");
        jmap.loadData(dataPath, "colony border data", DIM_BORDER_CODEC)
                .ifPresent(saved ->
                {
                    for (final ColonyBorderOverlay overlay : saved)
                    {
                        dimensionOverlays.put(overlay.id, overlay);
                    }
                });
    }

    /**
     * Stops tracking data for a dimension and clears any related overlays.
     */
    public static void unload(@NotNull final Journeymap jmap,
                              @NotNull final ResourceKey<Level> dimension)
    {
        final AbstractInt2ObjectMap<ColonyBorderOverlay> dimensionOverlays = overlays.remove(dimension);

        if (dimensionOverlays != null)
        {
            for (final ColonyBorderOverlay overlay : dimensionOverlays.values())
            {
                overlay.unload(jmap);
            }

            final Path dataPath = jmap.getDataPath(dimension).resolve("border.json");
            jmap.saveData(dataPath, "colony border data", DIM_BORDER_CODEC,
                    new ArrayList<>(dimensionOverlays.values()));
        }

        if (pendingClaimsDimension == dimension)
        {
            pendingClaims.clear();
        }
    }

    /**
     * Flags the colony border overlay for update, if needed for a single just-loaded chunk.
     *
     * @param jmap The JourneyMap API
     * @param dimension The dimension of the world.  Nothing happens unless this is the client world.
     * @param chunk The chunk that was just loaded.
     */
    public static void updateChunk(@NotNull final Journeymap jmap,
                                   @NotNull final ResourceKey<Level> dimension,
                                   @NotNull final ChunkAccess chunk)
    {
        updateChunk(jmap, dimension, ColonyUtils.getOwningColony(chunk), chunk.getPos());
    }

    private static void updateChunk(@NotNull final Journeymap jmap,
                                    @NotNull final ResourceKey<Level> dimension,
                                    final int id,
                                    @NotNull final ChunkPos pos)
    {
        final Level world = Minecraft.getInstance().level;
        if (world == null || !dimension.equals(world.dimension())) return;

        final AbstractInt2ObjectMap<ColonyBorderOverlay> dimensionOverlays = overlays.get(dimension);
        if (dimensionOverlays == null) return;  // not ready yet

        boolean changed = false;
        if (id == 0)
        {
            for (final AbstractInt2ObjectMap<ColonyBorderOverlay> overlayMap : overlays.values())
            {
                for (final ColonyBorderOverlay overlay : overlayMap.values())
                {
                    changed |= overlay.updateChunks(Collections.emptySet(), Collections.singleton(pos));
                }
            }
        }
        else
        {
            final ColonyBorderOverlay overlay = dimensionOverlays
                    .computeIfAbsent(id, k -> new ColonyBorderOverlay(dimension, id));
            changed |= overlay.updateChunks(Collections.singleton(pos), Collections.emptySet());
        }
    }

    public static void queueChunks(@NotNull final Journeymap jmap,
                                   @NotNull final ResourceKey<Level> dimension)
    {
        final Level world = Minecraft.getInstance().level;
        if (world == null || !dimension.equals(world.dimension())) return;

        if (pendingClaimsDimension != world.dimension())
        {
            pendingClaims.clear();
            pendingClaimsDimension = world.dimension();
        }
        else if (!pendingClaims.isEmpty())
        {
            // we haven't finished processing the last set yet; to avoid leaking memory and
            // getting even further behind, let's let that finish before we queue any more work.
            return;
        }

        final IColonyManager colonyManager = MinecoloniesAPIProxy.getInstance().getColonyManager();
        final Map<ChunkPos, IChunkClaimData> claims = colonyManager.getClaimData(dimension);
        final IntRBTreeSet colonies = new IntRBTreeSet();

        for (final Map.Entry<ChunkPos, IChunkClaimData> entry : claims.entrySet())
        {
            final int id = entry.getValue().getOwningColony();
            pendingClaims.add(new ChunkOwnership(entry.getKey(), id));
            if (id != 0)
            {
                colonies.add(id);
            }
        }

        final AbstractInt2ObjectMap<ColonyBorderOverlay> dimensionOverlays = overlays.get(dimension);
        if (dimensionOverlays == null) return;  // not ready yet

        for (final int id : colonies)
        {
            final ColonyBorderOverlay overlay = dimensionOverlays
                    .computeIfAbsent(id, k -> new ColonyBorderOverlay(dimension, id));
            final IColonyView colony = colonyManager.getColonyView(id, dimension);
            overlay.updateInfo(colony, JourneymapOptions.getShowColonyName(jmap.getOptions()));
        }
    }

    /**
     * Check if any colony border overlays need to be updated.
     *
     * @param jmap The Journeymap API
     * @param dimension The dimension to check
     */
    public static void updatePending(@NotNull final Journeymap jmap,
                                     @NotNull final ResourceKey<Level> dimension)
    {
        if (dimension == pendingClaimsDimension)
        {
            for (int i = 0; i < UPDATES_PER_TICK && !pendingClaims.isEmpty(); ++i)
            {
                final ChunkOwnership entry = pendingClaims.poll();
                updateChunk(jmap, dimension, entry.id(), entry.pos());
            }
        }

        for (final Int2ObjectMap.Entry<ColonyBorderOverlay> colonyEntry : overlays.getOrDefault(dimension, new Int2ObjectRBTreeMap<>()).int2ObjectEntrySet())
        {
            colonyEntry.getValue().updatePending(jmap);
        }
    }

    /** Chunk -> owning colony mapping entry. */
    private record ChunkOwnership(@NotNull ChunkPos pos, int id) { }

    /** Overlay tracking information for one entire colony */
    private static class ColonyBorderOverlay
    {
        private final ResourceKey<Level> dimension;
        private final int id;
        private final String name;
        private final Set<ChunkPos>        chunks;
        private final List<PolygonOverlay> overlays = new ArrayList<>();
        private final ShapeProperties      fill;
        private final ShapeProperties stroke;
        private final TextProperties  text;
        private final TextProperties  noText;

        private boolean dirty = false;
        private boolean permitted = true;
        private String colonyName = "";
        private JourneymapOptions.BorderStyle fullscreenStyle = JourneymapOptions.BorderStyle.HIDDEN;
        private JourneymapOptions.BorderStyle minimapStyle = JourneymapOptions.BorderStyle.HIDDEN;

        private static final Codec<Set<ChunkPos>> CODEC_SET_CHUNKPOSLONG =
                Codec.LONG.xmap(ChunkPos::new, ChunkPos::toLong)
                        .listOf().xmap(HashSet::new, ArrayList::new);
        static final Codec<ColonyBorderOverlay> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(o -> o.dimension),
                        Codec.INT.fieldOf("id").forGetter(o -> o.id),
                        Codec.STRING.optionalFieldOf("colony_name", null).forGetter(o -> o.colonyName),
                        Codec.INT.optionalFieldOf("colour", -1).forGetter(o -> o.text.getColor()),
                        Codec.BOOL.optionalFieldOf("licet", true).forGetter(o -> o.permitted),
                        CODEC_SET_CHUNKPOSLONG.optionalFieldOf("chunks", Collections.emptySet()).forGetter(o -> o.chunks)
                ).apply(instance, ColonyBorderOverlay::new));

        /** Deserialization */
        private ColonyBorderOverlay(@NotNull final ResourceKey<Level> dimension,
                                    final int id,
                                    final String colonyName,
                                    final int colour,
                                    final boolean permitted,
                                    @NotNull final Set<ChunkPos> chunks)
        {
            this(dimension, id);
            this.chunks.addAll(chunks);
            updateInfo(colonyName, colour, permitted, true);
            this.dirty = true;
        }

        /** Normal construction */
        public ColonyBorderOverlay(@NotNull final ResourceKey<Level> dimension,
                                   final int id)
        {
            this.dimension = dimension;
            this.id = id;
            this.name = String.format("colony_%s_%d", dimension.location(), id);
            this.chunks = new HashSet<>();

            this.fill = new ShapeProperties()
                    .setStrokeWidth(4).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                    .setFillColor(0x00ff00).setFillOpacity(.2f);
            this.stroke = new ShapeProperties()
                    .setStrokeWidth(4).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                    .setFillColor(0x00ff00).setFillOpacity(0);

            this.text = new TextProperties()
                    .setBackgroundColor(0x000022)
                    .setBackgroundOpacity(.5f)
                    .setColor(0x00ff00)
                    .setOpacity(1f)
                    .setMinZoom(0)
                    .setMaxZoom(2)
                    .setScale(2f)
                    .setFontShadow(true);

            this.noText = new TextProperties().setActiveUIs();
        }

        /** Add or remove chunks from this overlay */
        public boolean updateChunks(@NotNull final Set<ChunkPos> addChunks,
                                    @NotNull final Set<ChunkPos> removeChunks)
        {
            boolean changed;
            changed = this.chunks.addAll(addChunks);                // new owned chunks
            changed |= this.chunks.removeAll(removeChunks);         // new disowned chunks
            this.dirty |= changed;
            return changed;
        }

        /** Update colony-specific data if needed. */
        public boolean updateInfo(@Nullable final IColonyView colony, final boolean showColonyName)
        {
            boolean changed = false;
            if (colony != null)
            {
                final boolean permitted = colony.getPermissions().hasPermission(Minecraft.getInstance().player, Action.MAP_BORDER);

                //noinspection ConstantConditions
                changed |= updateInfo(colony.getName(), colony.getTeamColonyColor().getColor(), permitted, showColonyName);
            }
            return changed;
        }

        private boolean updateInfo(@Nullable final String colonyName,
                                   final int colour,
                                   final boolean permitted,
                                   final boolean showColonyName)
        {
            final boolean changed = !Objects.equals(colonyName, this.colonyName) ||
                    this.text.getColor() != colour || this.permitted != permitted;

            this.fill.setFillColor(colour).setStrokeColor(colour);
            this.stroke.setStrokeColor(colour);
            this.text.setColor(colour);
            //noinspection ConstantConditions
            this.text.setBackgroundColor(colour == ChatFormatting.BLACK.getColor() ? 0xDDDDDD : 0x000022);

            this.colonyName = colonyName;
            this.permitted = permitted;

            for (final PolygonOverlay overlay : this.overlays)
            {
                overlay.setLabel(showColonyName ? this.colonyName : "");
            }

            return changed;
        }

        /** Update the map overlays if needed */
        public void updatePending(@NotNull final Journeymap jmap)
        {
            final JourneymapOptions.BorderStyle fullscreenStyle = JourneymapOptions.getBorderFullscreenStyle(jmap.getOptions());
            final JourneymapOptions.BorderStyle minimapStyle = JourneymapOptions.getBorderMinimapStyle(jmap.getOptions());
            final boolean enabled = this.permitted
                    && !(JourneymapOptions.BorderStyle.HIDDEN.equals(fullscreenStyle)
                            && JourneymapOptions.BorderStyle.HIDDEN.equals(minimapStyle));

            this.dirty |= !enabled && !this.overlays.isEmpty();                         // freshly disabled; remove
            this.dirty |= enabled && this.overlays.isEmpty() && !this.chunks.isEmpty(); // freshly enabled; add
            this.dirty |= !fullscreenStyle.equals(this.fullscreenStyle);
            this.dirty |= !minimapStyle.equals(this.minimapStyle);

            if (this.dirty)
            {
                this.fullscreenStyle = fullscreenStyle;
                this.minimapStyle = minimapStyle;

                unload(jmap);

                if (!this.chunks.isEmpty() && enabled && jmap.getApi().playerAccepts(MOD_ID, DisplayType.Polygon))
                {
                    this.dirty = false;

                    final List<MapPolygonWithHoles> polygons = PolygonHelper.createChunksPolygon(this.chunks, 256);

                    for (final MapPolygonWithHoles polygon : polygons)
                    {
                        // fullscreen map
                        if (!JourneymapOptions.BorderStyle.HIDDEN.equals(fullscreenStyle))
                        {
                            final ShapeProperties shape = JourneymapOptions.BorderStyle.FILLED.equals(fullscreenStyle)
                                    ? this.fill : this.stroke;

                            final PolygonOverlay overlay = new PolygonOverlay(MOD_ID, this.dimension, shape, polygon.hull, polygon.holes);
                            overlay.setOverlayGroupName(this.name)
                                    .setActiveUIs(Context.UI.Fullscreen, Context.UI.Webmap)
                                    .setTextProperties(this.text)
                                    .setLabel(this.colonyName);
                            this.overlays.add(overlay);
                            jmap.show(overlay);
                        }

                        // minimap
                        if (!JourneymapOptions.BorderStyle.HIDDEN.equals(minimapStyle))
                        {
                            final ShapeProperties shape = JourneymapOptions.BorderStyle.FILLED.equals(minimapStyle)
                                    ? this.fill : this.stroke;

                            final PolygonOverlay mini = new PolygonOverlay(MOD_ID, this.dimension, shape, polygon.hull, polygon.holes);
                            mini.setOverlayGroupName(this.name)
                                    .setActiveUIs(Context.UI.Minimap)
                                    .setTextProperties(this.noText);
                            this.overlays.add(mini);
                            jmap.show(mini);
                        }
                    }
                }
            }
        }

        /** Removes any existing overlays (since we're about to make some new ones). */
        public void unload(@NotNull final Journeymap jmap)
        {
            for (final PolygonOverlay overlay : this.overlays)
            {
                jmap.getApi().remove(overlay);
            }
            this.overlays.clear();
        }
    }
}
