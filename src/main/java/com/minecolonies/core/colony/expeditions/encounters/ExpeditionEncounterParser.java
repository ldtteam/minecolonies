package com.minecolonies.core.colony.expeditions.encounters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Parser for encounters found on expeditions.
 */
public class ExpeditionEncounterParser
{
    /**
     * The json property keys.
     */
    private static final String PROP_ENTITY_TYPE       = "entity-type";
    private static final String PROP_DAMAGE            = "damage";
    private static final String PROP_REFLECTING_DAMAGE = "reflect";
    private static final String PROP_HEALTH            = "health";
    private static final String PROP_ARMOR             = "armor";
    private static final String PROP_LOOT_TABLE        = "loot-table";
    private static final String PROP_XP                = "xp";

    /**
     * Hidden constructor.
     */
    private ExpeditionEncounterParser() {}

    /**
     * Attempt to parse a colony expedition encounter instance from a json object.
     *
     * @param id     the id of the encounter.
     * @param object the input json object.
     * @return the colony expedition type instance, or null.
     * @throws JsonParseException when a fault is found during parsing the json.
     */
    @NotNull
    public static ExpeditionEncounter parse(final ResourceLocation id, final JsonObject object) throws JsonParseException
    {
        final EntityType<?> entityType = EntityType.byString(object.getAsJsonPrimitive(PROP_ENTITY_TYPE).getAsString())
                                           .orElseThrow(() -> new JsonParseException(String.format("Provided entity type '%s' does not exist.",
                                             object.getAsJsonPrimitive(PROP_ENTITY_TYPE).getAsString())));
        final float damage = object.getAsJsonPrimitive(PROP_DAMAGE).getAsFloat();
        final float reflectingDamage = object.has(PROP_REFLECTING_DAMAGE) ? object.getAsJsonPrimitive(PROP_REFLECTING_DAMAGE).getAsFloat() : 0;
        final double health = object.getAsJsonPrimitive(PROP_HEALTH).getAsDouble();
        final int armor = object.getAsJsonPrimitive(PROP_ARMOR).getAsInt();
        final ResourceLocation lootTable = new ResourceLocation(object.getAsJsonPrimitive(PROP_LOOT_TABLE).getAsString());
        final double xp = object.getAsJsonPrimitive(PROP_XP).getAsDouble();
        return new ExpeditionEncounter(id, entityType, damage, reflectingDamage, health, armor, lootTable, xp);
    }

    /**
     * Turns an expedition encounter instance into JSON format.
     *
     * @param encounter the expedition encounter instance.
     * @return the json object.
     */
    public static JsonObject toJson(final ExpeditionEncounter encounter)
    {
        final JsonObject object = new JsonObject();
        object.addProperty(PROP_ENTITY_TYPE, EntityType.getKey(encounter.entityType()).toString());
        object.addProperty(PROP_DAMAGE, encounter.damage());
        object.addProperty(PROP_REFLECTING_DAMAGE, encounter.reflectingDamage());
        object.addProperty(PROP_HEALTH, encounter.health());
        object.addProperty(PROP_ARMOR, encounter.armor());
        object.addProperty(PROP_LOOT_TABLE, encounter.lootTable().toString());
        object.addProperty(PROP_XP, encounter.xp());
        return object;
    }

    /**
     * Turns an expedition encounter instance into NBT format.
     *
     * @param encounter the expedition encounter instance.
     * @param buf       the buf to write into.
     */
    public static void toBuffer(final ExpeditionEncounter encounter, final FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(encounter.id());
        buf.writeRegistryId(ForgeRegistries.ENTITY_TYPES, encounter.entityType());
        buf.writeFloat(encounter.damage());
        buf.writeFloat(encounter.reflectingDamage());
        buf.writeDouble(encounter.health());
        buf.writeInt(encounter.armor());
        buf.writeResourceLocation(encounter.lootTable());
        buf.writeDouble(encounter.xp());
    }

    /**
     * Attempt to parse an expedition encounter instance from a network buffer.
     *
     * @param buf the network buffer.
     * @return the expedition encounter instance, or null.
     */
    public static ExpeditionEncounter fromBuffer(final FriendlyByteBuf buf)
    {
        final ResourceLocation id = buf.readResourceLocation();
        final EntityType<?> entityType = buf.readRegistryIdSafe(EntityType.class);
        final float damage = buf.readFloat();
        final float reflectingDamage = buf.readFloat();
        final double health = buf.readDouble();
        final int armor = buf.readInt();
        final ResourceLocation lootTable = buf.readResourceLocation();
        final double xp = buf.readDouble();
        return new ExpeditionEncounter(id, entityType, damage, reflectingDamage, health, armor, lootTable, xp);
    }
}
