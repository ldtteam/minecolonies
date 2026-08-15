package com.minecolonies.core.compatibility.journeymap;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.permissions.Action;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import journeymap.client.api.display.Context;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.model.MapPolygonWithHoles;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.model.TextProperties;
import journeymap.client.api.util.PolygonHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
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
    private static final Map<ResourceKey<Level>, Map<Integer, ColonyBorderOverlay>> overlays = new HashMap<>();

    static final Codec<List<ColonyBorderOverlay>> DIM_BORDER_CODEC = ColonyBorderOverlay.CODEC.listOf();

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

        final Map<Integer, ColonyBorderOverlay> dimensionOverlays =
                overlays.computeIfAbsent(dimension, k -> new HashMap<>());

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
        final Map<Integer, ColonyBorderOverlay> dimensionOverlays = overlays.remove(dimension);

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
    }

    /**
     * Updates the colony border overlay for a single colony, from its full synced claimed-chunk set. Replaces the
     * previous loaded-chunk scanning approach entirely &mdash; a colony's territory is now known in full as soon as its
     * {@code ColonyView} syncs, regardless of which chunks the client has actually loaded.
     *
     * @param jmap   The JourneyMap API
     * @param colony The colony view that was just updated.
     */
    public static void updateColony(@NotNull final Journeymap jmap,
                                    @NotNull final IColonyView colony)
    {
        final ResourceKey<Level> dimension = colony.getDimension();
        final Map<Integer, ColonyBorderOverlay> dimensionOverlays = overlays.get(dimension);
        if (dimensionOverlays == null) return;  // not ready yet

        final int id = colony.getID();
        final ColonyBorderOverlay overlay = dimensionOverlays.computeIfAbsent(id, k -> new ColonyBorderOverlay(dimension, id));

        final Set<ChunkPos> claimed = new HashSet<>();
        for (final long chunkPos : colony.getClaimedChunks())
        {
            claimed.add(new ChunkPos(chunkPos));
        }
        final Set<ChunkPos> added = new HashSet<>(claimed);
        added.removeAll(overlay.chunks);
        final Set<ChunkPos> removed = new HashSet<>(overlay.chunks);
        removed.removeAll(claimed);
        overlay.updateChunks(added, removed);

        final Set<ChunkPos> loaded = new HashSet<>();
        for (final long chunkPos : colony.getTicketedChunks())
        {
            loaded.add(new ChunkPos(chunkPos));
        }
        final Set<ChunkPos> loadedAdded = new HashSet<>(loaded);
        loadedAdded.removeAll(overlay.loadedChunks);
        final Set<ChunkPos> loadedRemoved = new HashSet<>(overlay.loadedChunks);
        loadedRemoved.removeAll(loaded);
        overlay.updateLoadedChunks(loadedAdded, loadedRemoved);

        overlay.updateInfo(colony, JourneymapOptions.getShowColonyName(jmap.getOptions()),
            JourneymapOptions.BorderStyle.HIDDEN.equals(JourneymapOptions.getBorderFullscreenStyle(jmap.getOptions())));
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
        for (final Map.Entry<Integer, ColonyBorderOverlay> colonyEntry : overlays.getOrDefault(dimension, Collections.emptyMap()).entrySet())
        {
            colonyEntry.getValue().updatePending(jmap);
        }
    }

    /** Overlay tracking information for one entire colony */
    private static class ColonyBorderOverlay
    {
        private final ResourceKey<Level> dimension;
        private final int id;
        private final String name;
        private final Set<ChunkPos> chunks;
        private final Set<ChunkPos> loadedChunks;
        private final List<PolygonOverlay> overlays = new ArrayList<>();
        private final ShapeProperties fill;
        private final ShapeProperties stroke;
        private final ShapeProperties loadedFill;
        private final ShapeProperties loadedStroke;
        private final TextProperties text;
        private final TextProperties noText;

        private boolean dirty = false;
        private boolean permitted = true;
        private String colonyName = "";
        private JourneymapOptions.BorderStyle fullscreenStyle = JourneymapOptions.BorderStyle.HIDDEN;
        private JourneymapOptions.BorderStyle minimapStyle = JourneymapOptions.BorderStyle.HIDDEN;
        private JourneymapOptions.BorderStyle loadedStyle = JourneymapOptions.BorderStyle.HIDDEN;

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
                        CODEC_SET_CHUNKPOSLONG.optionalFieldOf("chunks", Collections.emptySet()).forGetter(o -> o.chunks),
                        CODEC_SET_CHUNKPOSLONG.optionalFieldOf("loaded", Collections.emptySet()).forGetter(o -> o.loadedChunks)
                ).apply(instance, ColonyBorderOverlay::new));

        /** Deserialization */
        private ColonyBorderOverlay(@NotNull final ResourceKey<Level> dimension,
                                    final int id,
                                    final String colonyName,
                                    final int colour,
                                    final boolean permitted,
                                    @NotNull final Set<ChunkPos> chunks,
                                    @NotNull final Set<ChunkPos> loadedChunks)
        {
            this(dimension, id);
            this.chunks.addAll(chunks);
            this.loadedChunks.addAll(loadedChunks);
            updateInfo(colonyName, colour, permitted, true, false);
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
            this.loadedChunks = new HashSet<>();

            this.fill = new ShapeProperties()
                    .setStrokeWidth(4).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                    .setFillColor(0x00ff00).setFillOpacity(.2f);
            this.stroke = new ShapeProperties()
                    .setStrokeWidth(4).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                    .setFillColor(0x00ff00).setFillOpacity(0);
            this.loadedFill = new ShapeProperties()
                .setStrokeWidth(2).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
                .setFillColor(0x00ff00).setFillOpacity(.2f);
            this.loadedStroke = new ShapeProperties()
                .setStrokeWidth(2).setStrokeColor(0x00ff00).setStrokeOpacity(.7f)
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

            this.noText = new TextProperties()
                    .setActiveUIs(EnumSet.noneOf(Context.UI.class));
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

        /** Add or remove loaded chunks from this overlay */
        public boolean updateLoadedChunks(@NotNull final Set<ChunkPos> addChunks,
                                          @NotNull final Set<ChunkPos> removeChunks)
        {
            boolean changed;
            changed = this.loadedChunks.addAll(addChunks);          // new loaded chunks
            changed |= this.loadedChunks.removeAll(removeChunks);   // new unloaded chunks
            this.dirty |= changed;
            return changed;
        }

        /** Update colony-specific data if needed. */
        public boolean updateInfo(@Nullable final IColonyView colony, final boolean showColonyName, final boolean mainBorderIsHidden)
        {
            boolean changed = false;
            if (colony != null)
            {
                final boolean permitted = colony.getPermissions().hasPermission(Minecraft.getInstance().player, Action.MAP_BORDER);

                //noinspection ConstantConditions
                changed |= updateInfo(colony.getName(), colony.getTeamColonyColor().getColor(), permitted, showColonyName, mainBorderIsHidden);
            }
            return changed;
        }

        private boolean updateInfo(@Nullable final String colonyName,
                                   final int colour,
                                   final boolean permitted,
                                   final boolean showColonyName,
                                   final boolean mainBorderIsHidden)
        {
            final boolean changed = !Objects.equals(colonyName, this.colonyName) ||
                    this.text.getColor() != colour || this.permitted != permitted;
            final int loadedColour = 0xFFFFFF & intensify(colour);
            final boolean showLoadedColonyName = showColonyName && mainBorderIsHidden;

            this.fill.setFillColor(colour).setStrokeColor(colour);
            this.stroke.setStrokeColor(colour);
            this.loadedFill.setFillColor(loadedColour).setStrokeColor(loadedColour);
            this.loadedStroke.setStrokeColor(loadedColour);
            this.text.setColor(colour);
            //noinspection ConstantConditions
            this.text.setBackgroundColor(colour == ChatFormatting.BLACK.getColor() ? 0xDDDDDD : 0x000022);

            this.colonyName = colonyName;
            this.permitted = permitted;

            for (final PolygonOverlay overlay : this.overlays)
            {
                final boolean loaded = overlay.getId().startsWith("L_");
                overlay.setLabel((loaded ? showLoadedColonyName : showColonyName) ? this.colonyName : "");
            }

            return changed;
        }

        private static int intensify(final int colour)
        {
            int a = FastColor.ARGB32.alpha(colour);
            int r = FastColor.ARGB32.red(colour);
            int g = FastColor.ARGB32.green(colour);
            int b = FastColor.ARGB32.blue(colour);

            int max = Math.max(r, Math.max(g, b));

            int pure = FastColor.ARGB32.color(a, r == max ? 255 : 0, g == max ? 255 : 0, b == max ? 255 : 0);

            return FastColor.ARGB32.lerp(0.25f, colour, pure);
        }

        /** Update the map overlays if needed */
        public void updatePending(@NotNull final Journeymap jmap)
        {
            final JourneymapOptions.BorderStyle fullscreenStyle = JourneymapOptions.getBorderFullscreenStyle(jmap.getOptions());
            final JourneymapOptions.BorderStyle minimapStyle = JourneymapOptions.getBorderMinimapStyle(jmap.getOptions());
            final JourneymapOptions.BorderStyle loadedStyle = JourneymapOptions.getBorderLoadedStyle(jmap.getOptions());
            final boolean enabled = this.permitted
                    && !(JourneymapOptions.BorderStyle.HIDDEN.equals(fullscreenStyle)
                            && JourneymapOptions.BorderStyle.HIDDEN.equals(minimapStyle)
                            && JourneymapOptions.BorderStyle.HIDDEN.equals(loadedStyle));

            this.dirty |= !enabled && !this.overlays.isEmpty();                         // freshly disabled; remove
            this.dirty |= enabled && this.overlays.isEmpty() && !this.chunks.isEmpty(); // freshly enabled; add
            this.dirty |= !fullscreenStyle.equals(this.fullscreenStyle);
            this.dirty |= !minimapStyle.equals(this.minimapStyle);
            this.dirty |= !loadedStyle.equals(this.loadedStyle);

            if (this.dirty)
            {
                this.fullscreenStyle = fullscreenStyle;
                this.minimapStyle = minimapStyle;
                this.loadedStyle = loadedStyle;

                unload(jmap);

                if (!this.chunks.isEmpty() && enabled && jmap.getApi().playerAccepts(MOD_ID, DisplayType.Polygon))
                {
                    this.dirty = false;

                    final List<MapPolygonWithHoles> polygons = PolygonHelper.createChunksPolygon(this.chunks, 256);

                    int index = 0;
                    for (final MapPolygonWithHoles polygon : polygons)
                    {
                        // fullscreen map
                        if (!JourneymapOptions.BorderStyle.HIDDEN.equals(fullscreenStyle))
                        {
                            final ShapeProperties shape = JourneymapOptions.BorderStyle.FILLED.equals(fullscreenStyle)
                                    ? this.fill : this.stroke;

                            final PolygonOverlay overlay = new PolygonOverlay(MOD_ID, String.format("%s_%s", this.name, ++index), this.dimension, shape, polygon);
                            overlay.setOverlayGroupName(this.name)
                                    .setActiveUIs(EnumSet.of(Context.UI.Fullscreen, Context.UI.Webmap))
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

                            final PolygonOverlay mini = new PolygonOverlay(MOD_ID, String.format("%s_%s", this.name, ++index), this.dimension, shape, polygon);
                            mini.setOverlayGroupName(this.name)
                                    .setActiveUIs(EnumSet.of(Context.UI.Minimap))
                                    .setTextProperties(this.noText);
                            this.overlays.add(mini);
                            jmap.show(mini);
                        }
                    }
                }

                if (!this.loadedChunks.isEmpty() && enabled && jmap.getApi().playerAccepts(MOD_ID, DisplayType.Polygon))
                {
                    this.dirty = false;

                    final List<MapPolygonWithHoles> polygons = PolygonHelper.createChunksPolygon(this.loadedChunks, 258);

                    int index = 0;
                    for (final MapPolygonWithHoles polygon : polygons)
                    {
                        // fullscreen map
                        if (!JourneymapOptions.BorderStyle.HIDDEN.equals(loadedStyle))
                        {
                            final ShapeProperties shape = JourneymapOptions.BorderStyle.FILLED.equals(loadedStyle)
                                ? this.loadedFill : this.loadedStroke;

                            final PolygonOverlay overlay = new PolygonOverlay(MOD_ID, String.format("L_%s_%s", this.name, ++index), this.dimension, shape, polygon);
                            overlay.setOverlayGroupName(this.name)
                                .setActiveUIs(EnumSet.of(Context.UI.Fullscreen, Context.UI.Webmap))
                                .setTextProperties(this.text);
                            if (JourneymapOptions.BorderStyle.HIDDEN.equals(fullscreenStyle))
                            {
                                overlay.setLabel(this.colonyName);
                            }
                            this.overlays.add(overlay);
                            jmap.show(overlay);
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
