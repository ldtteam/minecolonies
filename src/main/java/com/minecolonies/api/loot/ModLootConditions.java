package com.minecolonies.api.loot;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.loot.ExpeditionDifficultyCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Container class for registering custom loot conditions
 */
public final class ModLootConditions
{
    public final static DeferredRegister<LootItemConditionType> DEFERRED_REGISTER = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Constants.MOD_ID);

    public static final ResourceLocation ENTITY_IN_BIOME_TAG_ID   = new ResourceLocation(MOD_ID, "entity_in_biome_tag");
    public static final ResourceLocation RESEARCH_UNLOCKED_ID     = new ResourceLocation(MOD_ID, "research_unlocked");
    public static final ResourceLocation EXPEDITION_DIFFICULTY_ID = new ResourceLocation(MOD_ID, "expedition_difficulty");

    public static final LootContextParam<ColonyExpeditionTypeDifficulty> EXPEDITION_DIFFICULTY_PARAM = new LootContextParam<>(EXPEDITION_DIFFICULTY_ID);
    public static final LootContextParamSet                              EXPEDITION_PARAMS           = LootContextParamSet.builder()
                                                                                                         .required(EXPEDITION_DIFFICULTY_PARAM)
                                                                                                         .build();

    public static final RegistryObject<LootItemConditionType> entityInBiomeTag;
    public static final RegistryObject<LootItemConditionType> researchUnlocked;
    public static final RegistryObject<LootItemConditionType> expeditionDifficulty;
    static
    {
        entityInBiomeTag = DEFERRED_REGISTER.register(ModLootConditions.ENTITY_IN_BIOME_TAG_ID.getPath(),
          () -> new LootItemConditionType(new EntityInBiomeTag.Serializer()));

        researchUnlocked = DEFERRED_REGISTER.register(ModLootConditions.RESEARCH_UNLOCKED_ID.getPath(),
          () -> new LootItemConditionType(new ResearchUnlocked.Serializer()));

        expeditionDifficulty = DEFERRED_REGISTER.register(ModLootConditions.EXPEDITION_DIFFICULTY_ID.getPath(),
          () -> new LootItemConditionType(new ExpeditionDifficultyCondition.Serializer()));
    }
    private ModLootConditions()
    {
        throw new IllegalStateException("Tried to initialize: ModLootConditions but this is a Utility class.");
    }

    public static void init()
    {
        // just for classloading
    }
}
