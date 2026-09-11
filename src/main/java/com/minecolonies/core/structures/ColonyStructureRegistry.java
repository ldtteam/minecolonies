package com.minecolonies.core.structures;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Registry allowing addon mods to inject their colony structures into the minecolonies worldgen structure set.
 *
 * <p>Minecolonies does not ship a static {@code empty_colony.json} structure set file. Instead, this class
 * constructs the structure set programmatically at world load time via a synthetic in-memory datapack registered
 * through {@link AddPackFindersEvent}. This avoids overriding any file on disk and ensures all structures —
 * both built-in and addon — share the same placement grid and spawn frequency.</p>
 *
 * <p>Addon mods should call {@link #register(ResourceLocation)} during mod construction or early mod bus events,
 * before {@link AddPackFindersEvent} fires. All registered structures are assigned equal weight alongside the
 * built-in minecolonies styles.</p>
 *
 * <p>Example usage from an addon mod:</p>
 * <pre>{@code
 * // In your mod constructor or FMLCommonSetupEvent handler:
 * ColonyStructureRegistry.register(new ResourceLocation("myaddon", "my_colony"));
 * }</pre>
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ColonyStructureRegistry
{
    private static final List<ResourceLocation> ADDON_STRUCTURES = new ArrayList<>();

    /**
     * The built-in minecolonies colony structures, hardcoded here since the structure set JSON
     * is generated programmatically rather than loaded from disk.
     */
    private static final String[] MCOL_STRUCTURES =
        {"minecolonies:medieval_birch_colony", "minecolonies:medieval_oak_colony", "minecolonies:medieval_spruce_colony", "minecolonies:medieval_dark_oak_colony",
            "minecolonies:asian_colony", "minecolonies:caledonia_colony", "minecolonies:lostmesacity_colony", "minecolonies:desertoasis_colony", "minecolonies:shire_colony",
            "minecolonies:incan_colony", "minecolonies:colonial_colony", "minecolonies:warpednetherlands_colony",};

    /**
     * Register an addon colony structure to be included in the minecolonies empty_colony structure set.
     * All registered structures are assigned equal weight.
     * Must be called before or during {@link AddPackFindersEvent}.
     *
     * @param structure the resource location of the structure to register
     */
    public static void register(final ResourceLocation structure)
    {
        ADDON_STRUCTURES.add(structure);
    }

    /**
     * Registers a synthetic in-memory datapack that serves the merged {@code minecolonies:empty_colony}
     * structure set JSON, combining all built-in and addon structures into a single placement grid.
     * The pack is placed at {@link Pack.Position#BOTTOM} so the pack can still be overridden by other datapacks if so desired.
     */
    @SubscribeEvent
    public static void onAddPackFinders(final AddPackFindersEvent event)
    {
        if (event.getPackType() != PackType.SERVER_DATA)
        {
            return;
        }
        if (!MineColonies.getConfig().getCommon().spawnAbandonedColonies.get())
        {
            Log.getLogger().info("Abandoned colony worldgen is disabled, skipping.");
            return;
        }

        Log.getLogger().info("Registering abandoned colony worldgen structures.");

        event.addRepositorySource(infoConsumer -> {
            final Pack pack = Pack.readMetaAndCreate("minecolonies_structure_set_injection",
                Component.literal("Minecolonies Structure Set Injection"),
                true,
                id -> new InjectionPackResources(),
                PackType.SERVER_DATA,
                Pack.Position.BOTTOM,
                PackSource.BUILT_IN);
            if (pack != null)
            {
                infoConsumer.accept(pack);
            }
        });
    }

    /**
     * In-memory {@link PackResources} implementation that serves a single resource:
     * {@code minecolonies:worldgen/structure_set/empty_colony.json}, built dynamically from
     * {@link #MCOL_STRUCTURES} and any structures registered via {@link #register(ResourceLocation)}.
     *
     * <p>The {@code prefix + "/"} check in {@link #listResources} is intentional — it prevents this pack
     * from being picked up by the {@code minecraft:worldgen/structure} registry loader, whose prefix
     * ({@code "worldgen/structure"}) is a plain string prefix of {@code "worldgen/structure_set"}.</p>
     */
    private static class InjectionPackResources implements PackResources
    {
        private static final String PACK_META = "{\"pack\":{\"description\":\"Minecolonies structure set injection\",\"pack_format\":15}}";

        @Nullable
        @Override
        public IoSupplier<InputStream> getRootResource(@NotNull final String @NotNull ... elements)
        {
            if (elements.length == 1 && elements[0].equals("pack.mcmeta"))
            {
                return () -> new ByteArrayInputStream(PACK_META.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        }

        @Nullable
        @Override
        public IoSupplier<InputStream> getResource(@NotNull final PackType type, @NotNull final ResourceLocation location)
        {
            if (type != PackType.SERVER_DATA)
            {
                return null;
            }
            if (!location.getNamespace().equals("minecolonies"))
            {
                return null;
            }
            if (!location.getPath().equals("worldgen/structure_set/empty_colony.json"))
            {
                return null;
            }

            return this::buildStructureSet;
        }

        private InputStream buildStructureSet()
        {
            final JsonArray structures = new JsonArray();

            final List<String> allStructures = new ArrayList<>(List.of(MCOL_STRUCTURES));
            ADDON_STRUCTURES.stream().map(ResourceLocation::toString).forEach(allStructures::add);

            Log.getLogger().info("Discovering all abandoned colony structures.");
            for (final String structure : allStructures)
            {
                Log.getLogger().info("Adding abandoned colony structure: {}", structure);
                final JsonObject e = new JsonObject();
                e.addProperty("structure", structure);
                e.addProperty("weight", 1);
                structures.add(e);
            }

            final JsonObject placement = new JsonObject();
            placement.addProperty("salt", 1225566777);
            placement.addProperty("spacing", 95);
            placement.addProperty("separation", 45);
            placement.addProperty("type", "minecraft:random_spread");

            final JsonObject json = new JsonObject();
            json.add("structures", structures);
            json.add("placement", placement);

            return new ByteArrayInputStream(json.toString().getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void listResources(@NotNull final PackType type, @NotNull final String namespace, @NotNull final String prefix, @NotNull final ResourceOutput output)
        {
            if (type == PackType.SERVER_DATA
                && namespace.equals("minecolonies")
                && "worldgen/structure_set/empty_colony.json".startsWith(prefix + "/"))
            {
                output.accept(new ResourceLocation("minecolonies", "worldgen/structure_set/empty_colony.json"), this::buildStructureSet);
            }
        }

        @NotNull
        @Override
        public Set<String> getNamespaces(@NotNull final PackType type)
        {
            return type == PackType.SERVER_DATA ? Set.of("minecolonies") : Set.of();
        }

        @Nullable
        @Override
        public <T> T getMetadataSection(@NotNull final MetadataSectionSerializer<T> deserializer)
        {
            if (deserializer == PackMetadataSection.TYPE)
            {
                return deserializer.fromJson(JsonParser.parseString(PACK_META).getAsJsonObject().getAsJsonObject("pack"));
            }
            return null;
        }

        @NotNull
        @Override
        public String packId()
        {
            return "minecolonies_structure_set_injection";
        }

        @Override
        public void close() {}
    }
}
