package com.minecolonies.core.colony.expeditions.encounters;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

/**
 * Encounters are mob encounters that can be found on expeditions.
 */
public class ExpeditionEncounter
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
     * The id of the encounter.
     */
    private final ResourceLocation id;

    /**
     * The entity type of the encounter.
     */
    private final EntityType<?> entityType;

    /**
     * The damage this encounter will deal.
     */
    private final float damage;

    /**
     * The damage this encounter will reflect upon itself after dealing damage.
     */
    private final float reflectingDamage;

    /**
     * The health this encounter has.
     */
    private final double health;

    /**
     * The armor level this encounter has.
     */
    private final int armor;

    /**
     * The loot table killing this encounter will give.
     */
    private final ResourceLocation lootTable;

    /**
     * The experience killing this encounter will give.
     */
    private final double xp;

    /**
     * Internal constructor.
     */
    private ExpeditionEncounter(
      final ResourceLocation id,
      final EntityType<?> entityType,
      final float damage,
      final float reflectingDamage,
      final double health,
      final int armor,
      final ResourceLocation lootTable,
      final double xp)
    {
        this.id = id;
        this.entityType = entityType;
        this.damage = damage;
        this.reflectingDamage = reflectingDamage;
        this.health = health;
        this.armor = armor;
        this.lootTable = lootTable;
        this.xp = xp;
    }

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
     * Get the id of the encounter.
     *
     * @return the id.
     */
    public ResourceLocation getId()
    {
        return id;
    }

    /**
     * Get the name of the encounter.
     *
     * @return the display name.
     */
    public Component getName()
    {
        return entityType.getDescription();
    }

    /**
     * Get the entity type of the encounter.
     *
     * @return the entity type.
     */
    public EntityType<?> getEntityType()
    {
        return entityType;
    }

    /**
     * Get the damage this encounter will deal.
     *
     * @return the damage amount.
     */
    public float getDamage()
    {
        return damage;
    }

    /**
     * Get the damage this encounter will reflect upon itself after dealing damage.
     *
     * @return the reflecting damage amount.
     */
    public float getReflectingDamage()
    {
        return reflectingDamage;
    }

    /**
     * Get the health this encounter has.
     *
     * @return the health amount.
     */
    public double getHealth()
    {
        return health;
    }

    /**
     * Get the armor level this encounter has.
     *
     * @return the armor amount.
     */
    public int getArmor()
    {
        return armor;
    }

    /**
     * Get the loot table killing this encounter will give.
     *
     * @return the loot table id.
     */
    public ResourceLocation getLootTable()
    {
        return lootTable;
    }

    /**
     * Get the experience killing this encounter will give.
     *
     * @return the experience amount.
     */
    public double getXp()
    {
        return xp;
    }
}
