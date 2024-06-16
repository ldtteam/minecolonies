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

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(encounter));
    }

    public static ResourceLocation getEncounterId(final String id)
    {
        return new ResourceLocation(Constants.MOD_ID, id);
    }

    public static ResourceLocation getStructureId(final String id)
    {
        return new ResourceLocation(Constants.MOD_ID, "expeditions/structures/" + id);
    }
}
