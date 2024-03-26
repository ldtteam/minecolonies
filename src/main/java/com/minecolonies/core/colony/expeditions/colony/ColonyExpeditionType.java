package com.minecolonies.core.colony.expeditions.colony;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.util.constant.IToolType;
import com.minecolonies.api.util.constant.ToolType;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionFoodRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionItemRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionToolRequirement;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON based class for defining colony expedition types.
 */
public class ColonyExpeditionType
{
    /**
     * The json property keys.
     */
    private static final String PROP_NAME                 = "name";
    private static final String PROP_TO_TEXT              = "to-text";
    private static final String PROP_DIFFICULTY           = "difficulty";
    private static final String PROP_DIMENSION            = "dimension";
    private static final String PROP_LOOT_TABLE           = "loot-table";
    private static final String PROP_REQUIREMENTS         = "requirements";
    private static final String PROP_REQUIREMENT_TYPE     = "type";
    private static final String PROP_REQUIREMENT_AMOUNT   = "amount";
    private static final String PROP_REQUIREMENT_TOOL_KEY = "tool";
    private static final String PROP_REQUIREMENT_ITEM_KEY = "item";
    private static final String PROP_GUARDS               = "guards";

    /**
     * Requirement types
     */
    private static final String REQUIREMENT_TYPE_TOOL = "tool";
    private static final String REQUIREMENT_TYPE_FOOD = "food";
    private static final String REQUIREMENT_TYPE_ITEM = "item";

    /**
     * The id of the expedition.
     */
    private final ResourceLocation id;

    /**
     * The name of the expedition, may be a translation string or a fixed text.
     */
    private final Component name;

    /**
     * The "to text" of the expedition, used as part of the interaction inquiry to give a real quick indication of what to expect from the expedition.
     */
    private final Component toText;

    /**
     * The difficulty of the expedition.
     */
    private final Difficulty difficulty;

    /**
     * The target dimension this expedition would go to.
     */
    private final ResourceKey<Level> dimension;

    /**
     * The loot table to use for rewards generation.
     */
    private final ResourceLocation lootTable;

    /**
     * The list of requirements for this expedition type to be sent.
     */
    private final List<ColonyExpeditionRequirement> requirements;

    /**
     * The minimum amount of guards needed for this expedition.
     */
    private final int guards;

    /**
     * Default constructor.
     */
    public ColonyExpeditionType(
      final ResourceLocation id,
      final Component name,
      final Component toText,
      final @NotNull Difficulty difficulty,
      final ResourceKey<Level> dimension,
      final ResourceLocation lootTable,
      final List<ColonyExpeditionRequirement> requirements,
      final int guards)
    {
        this.id = id;
        this.name = name;
        this.toText = toText;
        this.difficulty = difficulty;
        this.dimension = dimension;
        this.lootTable = lootTable;
        this.requirements = Collections.unmodifiableList(requirements);
        this.guards = guards;
    }

    /**
     * Attempt to parse a colony expedition type instance from a json object.
     *
     * @param id     the id of the expedition type.
     * @param object the input json object.
     * @return the colony expedition type instance, or null.
     * @throws JsonParseException when a fault is found during parsing the json.
     */
    @NotNull
    public static ColonyExpeditionType parse(final ResourceLocation id, final JsonObject object) throws JsonParseException
    {
        final Component name = Component.translatable(object.getAsJsonPrimitive(PROP_NAME).getAsString());
        final Component toText = Component.translatable(object.getAsJsonPrimitive(PROP_TO_TEXT).getAsString());
        final Difficulty difficulty = Difficulty.fromKey(object.getAsJsonPrimitive(PROP_DIFFICULTY).getAsString());
        final ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(object.getAsJsonPrimitive(PROP_DIMENSION).getAsString()));
        final ResourceLocation lootTable = new ResourceLocation(object.getAsJsonPrimitive(PROP_LOOT_TABLE).getAsString());

        final Set<ColonyExpeditionRequirement> requirements = new HashSet<>();
        if (object.has(PROP_REQUIREMENTS) && object.get(PROP_REQUIREMENTS).isJsonArray())
        {
            final JsonArray jsonRequirements = object.getAsJsonArray(PROP_REQUIREMENTS);
            for (final JsonElement jsonRequirement : jsonRequirements)
            {
                if (!jsonRequirement.isJsonObject())
                {
                    continue;
                }

                final ColonyExpeditionRequirement requirement = parseRequirement(jsonRequirement.getAsJsonObject());
                if (requirement != null)
                {
                    requirements.add(requirement);
                }
            }
        }

        final int guards = object.has(PROP_GUARDS) ? object.getAsJsonPrimitive(PROP_GUARDS).getAsInt() : 1;

        if (difficulty == null)
        {
            throw new JsonParseException(String.format("Provided difficulty does not exist, must be one of: [%s]",
              Arrays.stream(Difficulty.values()).map(m -> m.key).collect(Collectors.joining(", "))));
        }

        return new ColonyExpeditionType(id, name, toText, difficulty, dimension, lootTable, requirements.stream().toList(), guards);
    }

    /**
     * Parse an individual requirement from a json object.
     *
     * @param requirement the input json object.
     * @return a requirement instance or null.
     */
    @Nullable
    private static ColonyExpeditionRequirement parseRequirement(final JsonObject requirement)
    {
        final int amount = Math.max(requirement.has(PROP_REQUIREMENT_AMOUNT) ? requirement.getAsJsonPrimitive(PROP_REQUIREMENT_AMOUNT).getAsInt() : 1, 1);
        return switch (requirement.get(PROP_REQUIREMENT_TYPE).getAsString())
        {
            case REQUIREMENT_TYPE_TOOL ->
            {
                final IToolType toolType = ToolType.getToolType(requirement.getAsJsonPrimitive(PROP_REQUIREMENT_TOOL_KEY).getAsString());
                yield new ColonyExpeditionToolRequirement(toolType, amount);
            }
            case REQUIREMENT_TYPE_FOOD -> new ColonyExpeditionFoodRequirement(amount);
            case REQUIREMENT_TYPE_ITEM ->
            {
                final ResourceLocation itemId = new ResourceLocation(requirement.getAsJsonPrimitive(PROP_REQUIREMENT_ITEM_KEY).getAsString());
                yield new ColonyExpeditionItemRequirement(ForgeRegistries.ITEMS.getValue(itemId), amount);
            }
            default -> null;
        };
    }

    /**
     * Get the id of the expedition.
     *
     * @return the id.
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * Get the name of the expedition.
     *
     * @return the component.
     */
    public Component getName()
    {
        return name;
    }

    /**
     * Get the difficulty of the expedition.
     *
     * @return the difficulty enum.
     */
    public Difficulty getDifficulty()
    {
        return difficulty;
    }

    /**
     * Get the "to text" of the expedition, used inside the interaction.
     *
     * @return the component.
     */
    public Component getToText()
    {
        return toText;
    }

    /**
     * Get the target dimension this expedition would go to.
     *
     * @return the level resource key.
     */
    public ResourceKey<Level> getDimension()
    {
        return dimension;
    }

    /**
     * Get the loot table to use for rewards generation.
     *
     * @return the resloc for the loot table.
     */
    public ResourceLocation getLootTable()
    {
        return lootTable;
    }

    /**
     * Get the list of requirements for this expedition type to be sent.
     *
     * @return the unmodifiable list.
     */
    public List<ColonyExpeditionRequirement> getRequirements()
    {
        return requirements;
    }

    /**
     * Get the required guard count for this expedition type.
     *
     * @return the amount of guards required.
     */
    public int getGuards()
    {
        return guards;
    }

    /**
     * The expedition difficulty.
     */
    public enum Difficulty
    {
        EASY("easy", 1, 0, Items.IRON_SWORD, false, Style.EMPTY, 30, 5, 1f, 1f),
        MEDIUM("medium", 2, 1, Items.IRON_SWORD, false, Style.EMPTY, 45, 10, 1.5f, 1.2f),
        HARD("hard", 3, 3, Items.IRON_SWORD, false, Style.EMPTY, 60, 15, 2f, 1.5f),
        NIGHTMARE("nightmare", 4, 5, Items.NETHERITE_SWORD, true, Style.EMPTY.withColor(ChatFormatting.DARK_RED).withItalic(true), 120, 30, 4f, 2f);

        /**
         * The key of the difficulty, used in the json files.
         */
        private final String key;

        /**
         * The level of the difficulty.
         */
        private final int level;

        /**
         * The luck level an expedition of the given difficulty will have.
         */
        private final int luckLevel;

        /**
         * The sword item which should be rendered for the difficulty icons.
         */
        private final Item icon;

        /**
         * Whether the icon should by default be hidden, only shown if the difficulty is selected.
         */
        private final boolean hidden;

        /**
         * The style for the hover pane to display.
         */
        private final Style style;

        /**
         * The base amount of ticks the expedition will take.
         */
        private final int baseTime;

        /**
         * The amount of random time that can be added/removed from the base time.
         */
        private final int randomTime;

        /**
         * A multiplier that will spawn more mobs during encounters.
         */
        private final float mobEncounterMultiplier;

        /**
         * A multiplier that will increase the damage for mobs during encounters.
         */
        private final float mobDamageMultiplier;

        /**
         * Internal constructor.
         */
        Difficulty(
          final String key,
          final int level,
          final int luckLevel,
          final Item icon,
          final boolean hidden,
          final Style style,
          final int baseTime,
          final int randomTime,
          float mobEncounterMultiplier,
          float mobDamageMultiplier)
        {
            this.key = key;
            this.level = level;
            this.luckLevel = luckLevel;
            this.icon = icon;
            this.hidden = hidden;
            this.style = style;
            this.baseTime = baseTime;
            this.randomTime = randomTime;
            this.mobEncounterMultiplier = mobEncounterMultiplier;
            this.mobDamageMultiplier = mobDamageMultiplier;
        }

        /**
         * Get the difficulty from its key value.
         *
         * @param key the input key.
         * @return the difficulty, or none if the key is incorrect.
         */
        @Nullable
        public static Difficulty fromKey(final String key)
        {
            for (final Difficulty item : Difficulty.values())
            {
                if (item.key.equals(key))
                {
                    return item;
                }
            }
            return null;
        }

        /**
         * Get the key for this difficulty instance.
         *
         * @return the key for the difficulty.
         */
        public String getKey()
        {
            return key;
        }

        /**
         * Get the level of the expedition.
         *
         * @return the level.
         */
        public int getLevel()
        {
            return level;
        }

        /**
         * Get the icon of the expedition difficulty.
         *
         * @return the item.
         */
        public Item getIcon()
        {
            return icon;
        }

        /**
         * Whether this difficulty is hidden.
         *
         * @return true if so.
         */
        public boolean isHidden()
        {
            return hidden;
        }

        /**
         * Get the style for the hover pane to display.
         *
         * @return the custom style.
         */
        public Style getStyle()
        {
            return style;
        }

        /**
         * Get the luck level an expedition of the given difficulty will have.
         *
         * @return the luck level.
         */
        public int getLuckLevel()
        {
            return luckLevel;
        }

        /**
         * Get the base amount of ticks the expedition will take.
         *
         * @return the amount of time.
         */
        public int getBaseTime()
        {
            return baseTime;
        }

        /**
         * Get the amount of random time that can be added/removed from the base time.
         *
         * @return the amount of time.
         */
        public int getRandomTime()
        {
            return randomTime;
        }

        /**
         * Get a multiplier that will spawn more mobs during encounters.
         *
         * @return the multiplier.
         */
        public float getMobEncounterMultiplier()
        {
            return mobEncounterMultiplier;
        }

        /**
         * Get a multiplier that will increase the damage for mobs during encounters.
         *
         * @return the multiplier.
         */
        public float getMobDamageMultiplier()
        {
            return mobDamageMultiplier;
        }
    }
}