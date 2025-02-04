package com.minecolonies.api.loot;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Container class for registering custom loot conditions */
public final class ModLootConditions
{
    public final static DeferredRegister<LootItemConditionType> DEFERRED_REGISTER = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Constants.MOD_ID);

    public static final ResourceLocation ENTITY_IN_BIOME_TAG_ID = new ResourceLocation(MOD_ID, "entity_in_biome_tag");
    public static final ResourceLocation RESEARCH_UNLOCKED_ID = new ResourceLocation(MOD_ID, "research_unlocked");
    public static final ResourceLocation GENERATE_SUPPLY_LOOT_ID = new ResourceLocation(MOD_ID, "generate_supply_loot");

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> entityInBiomeTag;
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> researchUnlocked;
    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> generateSupplyLoot;


    // also some convenience definitions for existing conditions; some stolen from BlockLootSubProvider
    public static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    public static final LootItemCondition.Builder HAS_NETHERITE_HOE = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.NETHERITE_HOE));
    public static final LootItemCondition.Builder HAS_DIAMOND_HOE   = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.DIAMOND_HOE));
    public static final LootItemCondition.Builder HAS_IRON_HOE      = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.IRON_HOE));
    public static final LootItemCondition.Builder HAS_GOLDEN_HOE    = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.GOLDEN_HOE));
    public static final LootItemCondition.Builder HAS_HOE = MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.HOES));

    public static LootItemCondition.Builder hasShears()
    {
        return HAS_SHEARS;
    }

    public static LootItemCondition.Builder hasHoe()
    {
        return HAS_HOE;
    }

    public static LootItemCondition.Builder hasSilkTouch(@NotNull final HolderLookup.RegistryLookup<Enchantment> enchantments)
    {
        return MatchTool.toolMatches(
                ItemPredicate.Builder.item()
                        .withSubPredicate(
                                ItemSubPredicates.ENCHANTMENTS,
                                ItemEnchantmentsPredicate.enchantments(
                                        List.of(new EnchantmentPredicate(enchantments.getOrThrow(Enchantments.SILK_TOUCH), MinMaxBounds.Ints.atLeast(1)))
                                )
                        )
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
          () -> new LootItemConditionType(EntityInBiomeTag.CODEC));

        researchUnlocked = DEFERRED_REGISTER.register(ModLootConditions.RESEARCH_UNLOCKED_ID.getPath(),
          () -> new LootItemConditionType(ResearchUnlocked.CODEC));

        generateSupplyLoot = DEFERRED_REGISTER.register(ModLootConditions.GENERATE_SUPPLY_LOOT_ID.getPath(),
                () -> new LootItemConditionType(GenerateSupplyLoot.CODEC));
    }


    private ModLootConditions()
    {
        throw new IllegalStateException("Tried to initialize: ModLootConditions but this is a Utility class.");
    }
}
