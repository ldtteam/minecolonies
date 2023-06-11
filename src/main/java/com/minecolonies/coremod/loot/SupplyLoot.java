package com.minecolonies.coremod.loot;

import com.google.common.base.Suppliers;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Helper class for supply camp loot
 */
public class SupplyLoot extends LootModifier
{
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final Supplier<Codec<SupplyLoot>> SHIP_CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, (co) -> new SupplyLoot(co, false))));
    public static final Supplier<Codec<SupplyLoot>> CAMP_CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, (co) -> new SupplyLoot(co, true))));

    public static final RegistryObject<Codec<SupplyLoot>> SUPPLYSHIP_LOOT = GLM.register("supplyship_loot", SupplyLoot.SHIP_CODEC);
    public static final RegistryObject<Codec<SupplyLoot>> SUPPLYCAMP_LOOT = GLM.register("supplycamp_loot", SupplyLoot.CAMP_CODEC);


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

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext lootContext)
    {
        if (MineColonies.getConfig().getCommon().generateSupplyLoot.get())
        {
            if (camp)
            {
                LootTable stingerLootTable = lootContext.getLevel().getServer().getLootData().getLootTable(SUPPLY_CAMP_LT);
                ObjectArrayList<ItemStack> newItems = new ObjectArrayList<>();
                stingerLootTable.getRandomItems(lootContext, newItems::add);
                generatedLoot.addAll(newItems);
            }
            else
            {
                LootTable stingerLootTable = lootContext.getLevel().getServer().getLootData().getLootTable(SUPPLY_SHIP_LT);
                ObjectArrayList<ItemStack> newItems = new ObjectArrayList<>();
                stingerLootTable.getRandomItems(lootContext, newItems::add);
                generatedLoot.addAll(newItems);
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec()
    {
        return camp ? CAMP_CODEC.get() : SHIP_CODEC.get();
    }
}
