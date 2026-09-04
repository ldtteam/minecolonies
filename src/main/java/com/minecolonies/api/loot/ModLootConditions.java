package com.minecolonies.api.loot;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.predicates.DataComponentMatchers;
import net.minecraft.advancements.predicates.EnchantmentPredicate;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.*;
import net.minecraft.core.component.predicates.DataComponentPredicates;
import net.minecraft.core.component.predicates.EnchantmentsPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.world.item.Item;
import net.minecraft.advancements.predicates.MinMaxBounds;
import net.minecraft.world.level.ItemLike;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Container class for registering custom loot conditions */
public final class ModLootConditions
{
    public final static DeferredRegister<MapCodec<? extends LootItemCondition>> DEFERRED_REGISTER = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Constants.MOD_ID);

    public static final Identifier ENTITY_IN_BIOME_TAG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "entity_in_biome_tag");
    public static final Identifier RESEARCH_UNLOCKED_ID = Identifier.fromNamespaceAndPath(MOD_ID, "research_unlocked");
    public static final Identifier GENERATE_SUPPLY_LOOT_ID = Identifier.fromNamespaceAndPath(MOD_ID, "generate_supply_loot");

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> entityInBiomeTag;
    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> researchUnlocked;
    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<? extends LootItemCondition>> generateSupplyLoot;


    // also some convenience definitions for existing conditions; some stolen from BlockLootSubProvider
    private static ItemPredicate itemPredicate(final ItemLike... items)
    {
        return new ItemPredicate(
            Optional.of(HolderSet.direct(item -> item.asItem().builtInRegistryHolder(), items)),
            MinMaxBounds.Ints.ANY,
            DataComponentMatchers.ANY
        );
    }

    private static LootItemCondition.Builder toolMatches(final ItemPredicate predicate)
    {
        return () -> new MatchTool(Optional.of(predicate));
    }

    public static final LootItemCondition.Builder HAS_SHEARS = toolMatches(itemPredicate(Items.SHEARS));
    public static final LootItemCondition.Builder HAS_NETHERITE_HOE = toolMatches(itemPredicate(Items.NETHERITE_HOE));
    public static final LootItemCondition.Builder HAS_DIAMOND_HOE = toolMatches(itemPredicate(Items.DIAMOND_HOE));
    public static final LootItemCondition.Builder HAS_IRON_HOE = toolMatches(itemPredicate(Items.IRON_HOE));
    public static final LootItemCondition.Builder HAS_GOLDEN_HOE = toolMatches(itemPredicate(Items.GOLDEN_HOE));
    public static LootItemCondition.Builder hasShears()
    {
        return HAS_SHEARS;
    }

    public static LootItemCondition.Builder hasHoe()
    {
        // Minecraft 26.2 keeps the ItemTags.HOES constant in the mapped API,
        // but no longer ships the corresponding vanilla tag data. Resolving
        // that tag therefore throws during loot-table datagen (and on reload).
        // Keep the old vanilla semantics with an explicit holder set instead.
        return toolMatches(itemPredicate(
                Items.WOODEN_HOE,
                Items.STONE_HOE,
                Items.COPPER_HOE,
                Items.IRON_HOE,
                Items.GOLDEN_HOE,
                Items.DIAMOND_HOE,
                Items.NETHERITE_HOE));
    }

    public static LootItemCondition.Builder hasSilkTouch(@NotNull final HolderLookup.RegistryLookup<Enchantment> enchantments)
    {
        return MatchTool.toolMatches(
                ItemPredicate.Builder.item()
                        .withComponents(DataComponentMatchers.Builder.components()
                                .partial(
                                        DataComponentPredicates.ENCHANTMENTS,
                                        EnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                                )
                        )
                        .build())
        );
    }

    public static LootItemCondition.Builder hasShearsOrSilkTouch(@NotNull final HolderLookup.RegistryLookup<Enchantment> enchantments)
    {
        return hasShears().or(hasSilkTouch(enchantments));
    }

    public static LootItemCondition.Builder doesNotHaveShearsOrSilkTouch(@NotNull final HolderLookup.RegistryLookup<Enchantment> enchantments)
    {
        return hasShearsOrSilkTouch(enchantments).invert();
    }

    public static void init()
    {
        // just for classloading
    }

    static
    {
        entityInBiomeTag = DEFERRED_REGISTER.register(ModLootConditions.ENTITY_IN_BIOME_TAG_ID.getPath(),
          () -> EntityInBiomeTag.CODEC);

        researchUnlocked = DEFERRED_REGISTER.register(ModLootConditions.RESEARCH_UNLOCKED_ID.getPath(),
          () -> ResearchUnlocked.CODEC);

        generateSupplyLoot = DEFERRED_REGISTER.register(ModLootConditions.GENERATE_SUPPLY_LOOT_ID.getPath(),
                () -> GenerateSupplyLoot.CODEC);
    }


    private ModLootConditions()
    {
        throw new IllegalStateException("Tried to initialize: ModLootConditions but this is a Utility class.");
    }
}
