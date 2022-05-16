package com.minecolonies.api.loot;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Container class for registering custom loot conditions */
public final class ModLootConditions
{
    public static final ResourceLocation ENTITY_IN_BIOME_CATEGORY_ID = new ResourceLocation(MOD_ID, "entity_in_biome_category");
    public static final ResourceLocation RESEARCH_UNLOCKED_ID = new ResourceLocation(MOD_ID, "research_unlocked");

    public static final LootItemConditionType entityInBiomeCategory = register(ModLootConditions.ENTITY_IN_BIOME_CATEGORY_ID, new EntityInBiomeCategory.Serializer());
    public static final LootItemConditionType researchUnlocked = register(ModLootConditions.RESEARCH_UNLOCKED_ID, new ResearchUnlocked.Serializer());

    public static void init()
    {
        // just for classloading
    }

    private static LootItemConditionType register(@NotNull final ResourceLocation id,
                                                  @NotNull final Serializer<? extends LootItemCondition> serializer)
    {
        return Registry.register(Registry.LOOT_CONDITION_TYPE, id, new LootItemConditionType(serializer));
    }

    private ModLootConditions()
    {
        throw new IllegalStateException("Tried to initialize: ModLootConditions but this is a Utility class.");
    }
}
