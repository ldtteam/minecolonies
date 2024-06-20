package com.minecolonies.core.generation;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;

import static com.minecolonies.core.colony.events.ColonyExpeditionEvent.*;

/**
 * Manages creation of resource ids and other common blocks of information, for example for loot pools.
 */
public class ExpeditionResourceManager
{
    /**
     * Create an adventure token loot item structure starts.
     *
     * @param structureId the structure id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureStartItem(final String structureId)
    {
        final CompoundTag structureStart = new CompoundTag();
        structureStart.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_START);
        structureStart.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, new ResourceLocation(structureId).toString());

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureStart));
    }

    /**
     * Create an adventure token loot item structure ends.
     *
     * @param structureId the structure id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureEndItem(final String structureId)
    {
        final CompoundTag structureEnd = new CompoundTag();
        structureEnd.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_END);
        structureEnd.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, new ResourceLocation(structureId).toString());

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureEnd));
    }

    /**
     * Create an adventure token loot item for encounters.
     *
     * @param encounterId the encounter id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createEncounterLootItem(final ResourceLocation encounterId)
    {
        return createEncounterLootItem(encounterId, 1, true);
    }

    /**
     * Create an adventure token loot item for encounters.
     *
     * @param encounterId the encounter id.
     * @param amount      the amount of encounters that will spawn.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createEncounterLootItem(final ResourceLocation encounterId, int amount)
    {
        return createEncounterLootItem(encounterId, amount, true);
    }

    /**
     * Create an adventure token loot item for encounters.
     *
     * @param encounterId the encounter id.
     * @param amount      the amount of encounters that will spawn.
     * @param scale       whether to scale the encounters with difficulty.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createEncounterLootItem(final ResourceLocation encounterId, int amount, boolean scale)
    {
        final CompoundTag encounter = new CompoundTag();
        encounter.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_ENCOUNTER);
        encounter.putString(TOKEN_TAG_EXPEDITION_ENCOUNTER, encounterId.toString());
        encounter.putInt(TOKEN_TAG_EXPEDITION_ENCOUNTER_AMOUNT, amount);
        encounter.putBoolean(TOKEN_TAG_EXPEDITION_ENCOUNTER_SCALE, scale);

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(encounter));
    }

    /**
     * Generate a resource id for an encounter.
     *
     * @param id the id of the encounter.
     * @return the resource id.
     */
    public static ResourceLocation getEncounterId(final String id)
    {
        return new ResourceLocation(Constants.MOD_ID, id);
    }

    /**
     * Generate a resource id for a structure.
     *
     * @param id the id of the structure.
     * @return the resource id.
     */
    public static ResourceLocation getStructureId(final String id)
    {
        return new ResourceLocation(Constants.MOD_ID, "expeditions/structures/" + id);
    }
}
