package com.minecolonies.core.colony.expeditions.colony.types;

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
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parser for converting expedition types to and from json
 */
public class ColonyExpeditionTypeParser
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
     * Hidden constructor.
     */
    private ColonyExpeditionTypeParser() {}

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
        final ColonyExpeditionTypeDifficulty difficulty = ColonyExpeditionTypeDifficulty.fromKey(object.getAsJsonPrimitive(PROP_DIFFICULTY).getAsString());
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
              Arrays.stream(ColonyExpeditionTypeDifficulty.values()).map(ColonyExpeditionTypeDifficulty::getKey).collect(Collectors.joining(", "))));
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
     * Turns an expedition type instance into JSON format.
     *
     * @param expeditionTypeBuilder the expedition type builder instance.
     * @return the json object.
     */
    public static JsonObject toJson(final ColonyExpeditionTypeBuilder expeditionTypeBuilder)
    {
        final JsonObject object = new JsonObject();
        object.addProperty(PROP_NAME, expeditionTypeBuilder.getName());
        object.addProperty(PROP_TO_TEXT, expeditionTypeBuilder.getToText());
        object.addProperty(PROP_DIFFICULTY, expeditionTypeBuilder.getDifficulty().getKey());
        object.addProperty(PROP_DIMENSION, expeditionTypeBuilder.getDimension().location().toString());
        object.addProperty(PROP_LOOT_TABLE, expeditionTypeBuilder.getLootTable().toString());
        final JsonArray requirements = new JsonArray();
        for (final ColonyExpeditionRequirement requirement : expeditionTypeBuilder.getRequirements())
        {
            final JsonObject requirementObject = new JsonObject();
            requirementObject.addProperty(PROP_REQUIREMENT_AMOUNT, requirement.getAmount());

            if (requirement instanceof ColonyExpeditionToolRequirement toolRequirement)
            {
                requirementObject.addProperty(PROP_REQUIREMENT_TYPE, REQUIREMENT_TYPE_TOOL);
                requirementObject.addProperty(PROP_REQUIREMENT_TOOL_KEY, toolRequirement.getToolType().getName());
            }
            else if (requirement instanceof ColonyExpeditionFoodRequirement)
            {
                requirementObject.addProperty(PROP_REQUIREMENT_TYPE, REQUIREMENT_TYPE_FOOD);
            }
            else if (requirement instanceof ColonyExpeditionItemRequirement itemRequirement)
            {
                requirementObject.addProperty(PROP_REQUIREMENT_TYPE, REQUIREMENT_TYPE_ITEM);
                requirementObject.addProperty(PROP_REQUIREMENT_ITEM_KEY, itemRequirement.getItem().toString());
            }
            else
            {
                continue;
            }

            requirements.add(requirementObject);
        }
        object.add(PROP_REQUIREMENTS, requirements);
        object.addProperty(PROP_GUARDS, expeditionTypeBuilder.getGuards());
        return object;
    }
}