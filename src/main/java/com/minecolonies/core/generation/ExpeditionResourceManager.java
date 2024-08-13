package com.minecolonies.core.generation;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;

import static com.minecolonies.core.colony.events.ColonyExpeditionEvent.*;

/**
 * Manages creation of resource ids and other common blocks of information, for example for loot pools.
 */
public class ExpeditionResourceManager
{
    /**
     * Get the correct structure ID for the input base structure name.
     *
     * @param structureId the base structure name.
     * @return the structure ID.
     */
    public static ResourceLocation getStructureId(final ResourceLocation structureId)
    {
        return new ResourceLocation(Constants.MOD_ID, structureId.withPrefix("expeditions/structures/").getPath());
    }

    /**
     * Create an adventure token loot item structure starts.
     *
     * @param structureId the structure id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureStartItem(final ResourceLocation structureId)
    {
        final CompoundTag structureStart = new CompoundTag();
        structureStart.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_START);
        structureStart.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, structureId.toString());

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureStart));
    }

    /**
     * Create an adventure token loot item structure ends.
     *
     * @param structureId the structure id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureEndItem(final ResourceLocation structureId)
    {
        final CompoundTag structureEnd = new CompoundTag();
        structureEnd.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_END);
        structureEnd.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, structureId.toString());

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
     * Create a loot table structure reference for the given structure name.
     *
     * @param structureId the base structure name.
     * @return the loot entry builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureLootReference(final ResourceLocation structureId)
    {
        return LootTableReference.lootTableReference(getStructureId(structureId));
    }

    /**
     * Create a potion loot item.
     *
     * @param potion the potion type.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createPotionItem(final Potion potion)
    {
        final ItemStack stack = new ItemStack(Items.POTION);
        PotionUtils.setPotion(stack, potion);
        return LootItem.lootTableItem(Items.POTION).apply(SetNbtFunction.setTag(stack.getTag()));
    }
}
