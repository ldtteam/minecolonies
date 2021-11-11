package com.minecolonies.api.loot;

import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Container class for registering custom loot conditions */
public final class ModLootConditions
{
    public static final ResourceLocation ENTITY_IN_BIOME_CATEGORY_ID = new ResourceLocation(MOD_ID, "entity_in_biome_category");

    public static final LootConditionType entityInBiomeCategory = register(ModLootConditions.ENTITY_IN_BIOME_CATEGORY_ID, new EntityInBiomeCategory.Serializer());

    public static void init()
    {
        // just for classloading
    }

    private static LootConditionType register(@NotNull final ResourceLocation id,
                                              @NotNull final ILootSerializer<? extends ILootCondition> serializer)
    {
        return Registry.register(Registry.LOOT_CONDITION_TYPE, id, new LootConditionType(serializer));
    }

    private ModLootConditions()
    {
        throw new IllegalStateException("Tried to initialize: ModLootConditions but this is a Utility class.");
    }
}
