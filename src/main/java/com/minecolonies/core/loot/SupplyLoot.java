package com.minecolonies.core.loot;

import com.google.common.base.Suppliers;
import com.minecolonies.core.MineColonies;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Helper class for supply camp loot
 */
public class SupplyLoot extends LootModifier
{
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);

    public static final Supplier<MapCodec<SupplyLoot>>    SHIP_CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> LootModifier.codecStart(inst).apply(inst, (co) -> new SupplyLoot(co, false))));
    public static final Supplier<MapCodec<SupplyLoot>> CAMP_CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> LootModifier.codecStart(inst).apply(inst, (co) -> new SupplyLoot(co, true))));

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SupplyLoot>> SUPPLYSHIP_LOOT = GLM.register("supplyship_loot", SupplyLoot.SHIP_CODEC);
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SupplyLoot>> SUPPLYCAMP_LOOT = GLM.register("supplycamp_loot", SupplyLoot.CAMP_CODEC);


    /**
     * Resource locations, path and names must fit the existing json file.
     */
    public final static ResourceLocation SUPPLY_CAMP_LT = new ResourceLocation(MOD_ID, "chests/supplycamp");
    public final static ResourceLocation SUPPLY_SHIP_LT = new ResourceLocation(MOD_ID, "chests/supplyship");

    // If we need more we need a static block and put things into a map ResourceLocation-codec
    private final boolean camp;

    /**
     * Maps vanilla lootable resource location to our loot pool to add.
     */
    private Map<ResourceLocation, ResourceLocation> lootTables = new HashMap<>();

    public SupplyLoot(final LootItemCondition[] conditionsIn, final boolean camp)
    {
        super(conditionsIn);
        this.camp = camp;
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext lootContext)
    {
        if (MineColonies.getConfig().getCommon().generateSupplyLoot.get() && !lootContext.getQueriedLootTableId().getNamespace().equals(MOD_ID))
        {
            if (camp)
            {
                LootTable stingerLootTable = lootContext.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, SUPPLY_CAMP_LT));
                stingerLootTable.getRandomItemsRaw(lootContext, generatedLoot::add);
            }
            else
            {
                LootTable stingerLootTable = lootContext.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, SUPPLY_SHIP_LT));
                stingerLootTable.getRandomItemsRaw(lootContext, generatedLoot::add);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec()
    {
        return camp ? CAMP_CODEC.get() : SHIP_CODEC.get();
    }
}
