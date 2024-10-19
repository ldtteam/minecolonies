package com.minecolonies.core.colony.expeditions.colony.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.equipment.ModEquipmentTypes;
import com.minecolonies.api.equipment.registry.EquipmentTypeEntry;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionEquipmentRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionFoodRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionItemRequirement;
import com.minecolonies.core.colony.expeditions.colony.requirements.ColonyExpeditionRequirement;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Parser for converting expedition types to and from json
 */
public class ColonyExpeditionTypeParser
{
    /**
     * The json property keys.
     */
    private static final String PROP_NAME                      = "name";
    private static final String PROP_TO_TEXT                   = "to-text";
    private static final String PROP_DIFFICULTY                = "difficulty";
    private static final String PROP_DIMENSION                 = "dimension";
    private static final String PROP_LOOT_TABLE                = "loot-table";
    private static final String PROP_REQUIREMENTS              = "requirements";
    private static final String PROP_REQUIREMENT_TYPE          = "type";
    private static final String PROP_REQUIREMENT_AMOUNT        = "amount";
    private static final String PROP_REQUIREMENT_EQUIPMENT_KEY = "equipment";
    private static final String PROP_REQUIREMENT_ITEM_KEY      = "item";
    private static final String PROP_GUARDS                    = "guards";

    /**
     * Requirement types
     */
    private static final String REQUIREMENT_TYPE_EQUIPMENT = "equipment";
    private static final String REQUIREMENT_TYPE_FOOD      = "food";
    private static final String REQUIREMENT_TYPE_ITEM      = "item";

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
            case REQUIREMENT_TYPE_EQUIPMENT ->
            {
                final ResourceLocation equipmentTypeId = new ResourceLocation(requirement.getAsJsonPrimitive(PROP_REQUIREMENT_EQUIPMENT_KEY).getAsString());
                final EquipmentTypeEntry equipmentTypeEntry = IMinecoloniesAPI.getInstance().getEquipmentTypeRegistry().getValue(equipmentTypeId);
                if (equipmentTypeEntry == null)
                {
                    yield null;
                }
                yield new ColonyExpeditionEquipmentRequirement(equipmentTypeEntry, amount);
            }
            case REQUIREMENT_TYPE_FOOD -> new ColonyExpeditionFoodRequirement(amount);
            case REQUIREMENT_TYPE_ITEM ->
            {
                final ResourceLocation itemId = new ResourceLocation(requirement.getAsJsonPrimitive(PROP_REQUIREMENT_ITEM_KEY).getAsString());
                final Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null)
                {
                    yield null;
                }
                yield new ColonyExpeditionItemRequirement(item, amount);
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

            if (requirement instanceof ColonyExpeditionEquipmentRequirement toolRequirement)
            {
                requirementObject.addProperty(PROP_REQUIREMENT_TYPE, REQUIREMENT_TYPE_EQUIPMENT);
                requirementObject.addProperty(PROP_REQUIREMENT_EQUIPMENT_KEY, toolRequirement.getEquipmentType().getRegistryName().toString());
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

    /**
     * Turns an expedition type instance into NBT format.
     *
     * @param expeditionType the expedition type instance.
     * @param buf            the buf to write into.
     */
    public static void toBuffer(final ColonyExpeditionType expeditionType, final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(expeditionType.id());
        buf.writeComponent(expeditionType.name());
        buf.writeComponent(expeditionType.toText());
        buf.writeEnum(expeditionType.difficulty());
        buf.writeResourceKey(expeditionType.dimension());
        buf.writeResourceLocation(expeditionType.lootTable());
        buf.writeInt(expeditionType.requirements().size());
        for (final ColonyExpeditionRequirement requirement : expeditionType.requirements())
        {
            if (requirement instanceof ColonyExpeditionEquipmentRequirement toolRequirement)
            {
                buf.writeUtf(REQUIREMENT_TYPE_EQUIPMENT);
                buf.writeInt(requirement.getAmount());
                buf.writeResourceLocation(toolRequirement.getEquipmentType().getRegistryName());
            }
            else if (requirement instanceof ColonyExpeditionFoodRequirement)
            {
                buf.writeUtf(REQUIREMENT_TYPE_FOOD);
                buf.writeInt(requirement.getAmount());
            }
            else if (requirement instanceof ColonyExpeditionItemRequirement itemRequirement)
            {
                buf.writeUtf(REQUIREMENT_TYPE_ITEM);
                buf.writeInt(requirement.getAmount());
                buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(itemRequirement.getItem()));
            }
        }
        buf.writeInt(expeditionType.guards());
    }

    /**
     * Attempt to parse a colony expedition type instance from a network buffer.
     *
     * @param buf the network buffer.
     * @return the colony expedition type instance, or null.
     */
    public static ColonyExpeditionType fromBuffer(final FriendlyByteBuf buf)
    {
        final ResourceLocation id = buf.readResourceLocation();
        final Component name = buf.readComponent();
        final Component toText = buf.readComponent();
        final ColonyExpeditionTypeDifficulty difficulty = buf.readEnum(ColonyExpeditionTypeDifficulty.class);
        final ResourceKey<Level> dimension = buf.readResourceKey(Registries.DIMENSION);
        final ResourceLocation lootTable = buf.readResourceLocation();

        final List<ColonyExpeditionRequirement> requirements = new ArrayList<>();
        final int requirementCount = buf.readInt();
        for (int i = 0; i < requirementCount; i++)
        {
            final String requirementType = buf.readUtf();
            final int amount = buf.readInt();
            switch (requirementType)
            {
                case REQUIREMENT_TYPE_EQUIPMENT:
                    final EquipmentTypeEntry equipment = ModEquipmentTypes.getRegistry().getValue(buf.readResourceLocation());
                    requirements.add(new ColonyExpeditionEquipmentRequirement(equipment, amount));
                    break;
                case REQUIREMENT_TYPE_FOOD:
                    requirements.add(new ColonyExpeditionFoodRequirement(amount));
                    break;
                case REQUIREMENT_TYPE_ITEM:
                    final Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
                    requirements.add(new ColonyExpeditionItemRequirement(item, amount));
                    break;
            }
        }

        final int guards = buf.readInt();

        return new ColonyExpeditionType(id, name, toText, difficulty, dimension, lootTable, requirements, guards);
    }
}