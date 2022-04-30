package com.minecolonies.api.loot;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class ModLootContextParamSets
{

    /**
     * The loot parameter set for recipes.
     */
    public static final LootContextParamSet CITIZEN_PERFORMS_LOOTING = LootContextParamSets.register(
      new ResourceLocation(Constants.MOD_ID, "citizen_performs_looting").toString(),
      builder -> builder
        .required(LootContextParams.ORIGIN)
        .required(LootContextParams.THIS_ENTITY)
        .required(LootContextParams.TOOL)
        .required(ModLootContextParams.CITIZEN_PRIMARY_SKILL)
        .required(ModLootContextParams.CITIZEN_SECONDARY_SKILL)
        .optional(LootContextParams.DAMAGE_SOURCE)
        .optional(LootContextParams.KILLER_ENTITY)
        .optional(LootContextParams.DIRECT_KILLER_ENTITY)
    );

    private ModLootContextParamSets()
    {
        throw new IllegalStateException("Tried to initialize: ModLootContextParamSets but this is a Utility class.");
    }
}
