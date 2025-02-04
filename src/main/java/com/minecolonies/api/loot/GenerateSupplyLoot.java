package com.minecolonies.api.loot;

import com.minecolonies.core.MineColonies;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

/**
 * Loot condition that checks whether supply loot is enabled in configuration.
 */
public class GenerateSupplyLoot implements LootItemCondition
{
    public static final MapCodec<GenerateSupplyLoot> CODEC = MapCodec.unit(GenerateSupplyLoot::new);

    private GenerateSupplyLoot()
    {
    }

    /**
     * Create a loot condition.
     */
    public static LootItemCondition.Builder when()
    {
        return GenerateSupplyLoot::new;
    }

    @NotNull
    @Override
    public LootItemConditionType getType()
    {
        return ModLootConditions.generateSupplyLoot.get();
    }

    @Override
    public boolean test(@NotNull final LootContext lootContext)
    {
        return MineColonies.getConfig().getCommon().generateSupplyLoot.get();
    }
}
