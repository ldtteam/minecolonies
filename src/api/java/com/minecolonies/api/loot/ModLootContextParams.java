package com.minecolonies.api.loot;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import org.jetbrains.annotations.NotNull;

public final class ModLootContextParams
{
    public static final LootContextParam<Integer> CITIZEN_PRIMARY_SKILL = create("citizen_primary_skill");
    public static final LootContextParam<Integer> CITIZEN_SECONDARY_SKILL = create("citizen_secondary_skill");

    @SuppressWarnings("SameParameterValue")
    private static <T> LootContextParam<T> create(@NotNull String name) {
        return new LootContextParam<>(new ResourceLocation(Constants.MOD_ID, name));
    }


    private ModLootContextParams()
    {
        throw new IllegalStateException("Tried to initialize: ModLootContextParams but this is a Utility class.");
    }
}
