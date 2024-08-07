package com.minecolonies.core.event;

import com.google.gson.*;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;
import static com.minecolonies.core.generation.DataGeneratorConstants.COLONY_STORIES_DIR;
import static net.neoforged.fml.common.EventBusSubscriber.Bus.MOD;

/**
 * Loads and listens to colony story changes.
 */
@EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = MOD)
public class ColonyStoryListener extends SimpleJsonResourceReloadListener
{
    /**
     * Gson instance
     */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Names for abandoned colonies.
     */
    public static final ResourceLocation ABANDONED_COLONY_NAME = new ResourceLocation(MOD_ID, "abandoned_name");
    /**
     * Stories for abandoned colonies.
     */
    public static final ResourceLocation ABANDONED_COLONY_STORY = new ResourceLocation(MOD_ID, "abandoned");
    /**
     * Stories for supply camps.
     */
    public static final ResourceLocation SUPPLY_CAMP_STORY = new ResourceLocation(MOD_ID, "camp");
    /**
     * Stories for supply ships.
     */
    public static final ResourceLocation SUPPLY_SHIP_STORY = new ResourceLocation(MOD_ID, "ship");

    /**
     * List of story and lore elements loaded from data.
     */
    public static Set<StoryText> abandonedColonyNames = new HashSet<>();
    public static Set<StoryText> abandonedColonyStories = new HashSet<>();

    public static Set<StoryText> supplyShipStories = new HashSet<>();
    public static Set<StoryText> supplyCampStories = new HashSet<>();

    @SubscribeEvent
    public static void modInitClient(final RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(new ColonyStoryListener());
    }

    /**
     * Create a new listener.
     */
    public ColonyStoryListener()
    {
        super(GSON, COLONY_STORIES_DIR);
    }

    /**
     * Pick a random entry from the given set of story texts.
     * @param stories the set of story texts to pick from.
     * @param biome   biomes to filter the stories against.
     * @param rng     random number source.
     * @return        one of the stories, at random.
     */
    public static String pickRandom(final Collection<StoryText> stories, final Holder<Biome> biome, final Random rng)
    {
        final List<String> matches = StoryText.allMatches(stories, biome);
        if (matches.isEmpty())
        {
            return "";
        }

        return matches.get(rng.nextInt(matches.size()));
    }

    @Override
    protected void apply(final Map<ResourceLocation, JsonElement> jsonElementMap, final @NotNull ResourceManager resourceManager, final @NotNull ProfilerFiller profiler)
    {
        abandonedColonyNames.clear();
        abandonedColonyStories.clear();
        supplyShipStories.clear();
        supplyCampStories.clear();

        for (final Map.Entry<ResourceLocation, JsonElement> entry : jsonElementMap.entrySet())
        {
            try
            {
                if (entry.getValue().isJsonArray())
                {
                    entry.getValue().getAsJsonArray().forEach(e -> parseStory(e.getAsJsonObject()));
                }
                else
                {
                    parseStory(entry.getValue().getAsJsonObject());
                }
            }
            catch (final Throwable e)
            {
                Log.getLogger().error("Error parsing story " + entry.getKey().toString(), e);
            }
        }
    }

    private void parseStory(final JsonObject json)
    {
        final ResourceLocation type = ResourceLocation.parse(Objects.requireNonNullElse(json.get("type"), new JsonPrimitive("")).getAsString());
        if (type.equals(ABANDONED_COLONY_NAME))
        {
            abandonedColonyNames.addAll(parseStoryText(json));
        }
        else if (type.equals(ABANDONED_COLONY_STORY))
        {
            abandonedColonyStories.addAll(parseStoryText(json));
        }
        else if (type.equals(SUPPLY_CAMP_STORY))
        {
            supplyCampStories.addAll(parseStoryText(json));
        }
        else if (type.equals(SUPPLY_SHIP_STORY))
        {
            supplyShipStories.addAll(parseStoryText(json));
        }
        else
        {
            // unrecognised type; ignore without warning in case it's something intended for a different mod
        }
    }

    private List<StoryText> parseStoryText(final JsonObject json)
    {
        BiomeFilter biomeFilter = BiomeFilter.ALL;

        final JsonElement biomesJson = json.get("biomes");
        if (biomesJson != null)
        {
            if (biomesJson.isJsonArray())
            {
                for (final JsonElement element : biomesJson.getAsJsonArray())
                {
                    biomeFilter = biomeFilter.or(BiomeFilter.parse(element.getAsString()));
                }
            }
            else
            {
                biomeFilter = BiomeFilter.parse(biomesJson.getAsString());
            }
        }

        final JsonElement contentJson = json.get("content");
        if (contentJson.isJsonArray())
        {
            final List<StoryText> stories = new ArrayList<>();
            for (final JsonElement element : contentJson.getAsJsonArray())
            {
                stories.add(new StoryText(biomeFilter, element.getAsString()));
            }
            return stories;
        }

        return Collections.singletonList(new StoryText(biomeFilter, contentJson.getAsString()));
    }

    /**
     * A biome filter; either by id or by tag.
     */
    private record BiomeFilter(Predicate<Holder<Biome>> filter) implements Predicate<Holder<Biome>>
    {
        /**
         * A filter that accepts any biome.
         */
        public static final BiomeFilter ALL = new BiomeFilter(b -> true);

        /**
         * Combine two {@link BiomeFilter} with an OR condition.
         * @param other the other filter.
         * @return      a filter that returns {@code this || other}.
         */
        public BiomeFilter or(final BiomeFilter other)
        {
            return (this == ALL) ? other : (other == ALL) ? this : new BiomeFilter(this.filter().or(other.filter()));
        }

        @Override
        public boolean test(final Holder<Biome> biome)
        {
            return this.filter.test(biome);
        }

        /**
         * Parses a {@link BiomeFilter} from a string.
         *
         * @param value either "#ns:biome_tag" or as "ns:biome_id"
         * @return the biome filter.
         * @throws IllegalStateException for invalid values.
         */
        public static BiomeFilter parse(final String value)
        {
            if (value.startsWith("#"))
            {
                final TagKey<Biome> tagKey = TagKey.create(Registries.BIOME, ResourceLocation.parse(value.substring(1)));
                return new BiomeFilter(b -> b.is(tagKey));
            }
            else
            {
                final RegistryAccess registryAccess = ServerLifecycleHooks.getCurrentServer().registryAccess();
                final ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, ResourceLocation.parse(value));
                final Holder<Biome> biome = registryAccess.registryOrThrow(Registries.BIOME).getHolderOrThrow(key);
                return new BiomeFilter(b -> b.equals(biome));
            }
        }
    }

    /**
     * A bit of story text that is filterable by biome.
     * @param biomeFilter the filter that passes any biome where this story is valid.
     * @param content     the text content.
     */
    public record StoryText(BiomeFilter biomeFilter, String content)
    {
        /**
         * Check if the given biome is valid for this story text.
         * @param biome the biome to check.
         * @return      true if valid.
         */
        public boolean matches(final Holder<Biome> biome)
        {
            return biomeFilter.test(biome);
        }

        /**
         * Builds a list of all possible matches from a collection of {@link StoryText}.
         * @param stories the collection.
         * @param biome   the biome to match against.
         * @return        a list of matching content.
         */
        public static List<String> allMatches(final Collection<StoryText> stories, final Holder<Biome> biome)
        {
            return stories.stream().filter(s -> s.matches(biome)).map(StoryText::content).toList();
        }
    }
}
