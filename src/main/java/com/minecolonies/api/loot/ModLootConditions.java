package com.minecolonies.api.loot;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/** Container class for registering custom loot conditions */
public final class ModLootConditions
{
    public final static DeferredRegister<LootItemConditionType> DEFERRED_REGISTER = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Constants.MOD_ID);

    public static final ResourceLocation ENTITY_IN_BIOME_TAG_ID = new ResourceLocation(MOD_ID, "entity_in_biome_tag");
    public static final ResourceLocation RESEARCH_UNLOCKED_ID = new ResourceLocation(MOD_ID, "research_unlocked");

    public static final RegistryObject<LootItemConditionType> entityInBiomeTag;
    public static final RegistryObject<LootItemConditionType> researchUnlocked;

    // also some convenience definitions for existing conditions; some stolen from BlockLootSubProvider
    public static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
    public static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    public static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    public static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
    public static final LootItemCondition.Builder HAS_HOE = MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.HOES));

    public static void init()
    {
        // just for classloading
    }

    static
    {
        entityInBiomeTag = DEFERRED_REGISTER.register(ModLootConditions.ENTITY_IN_BIOME_TAG_ID.getPath(),
          () -> new LootItemConditionType(new EntityInBiomeTag.Serializer()));

        researchUnlocked = DEFERRED_REGISTER.register(ModLootConditions.RESEARCH_UNLOCKED_ID.getPath(),
          () -> new LootItemConditionType(new ResearchUnlocked.Serializer()));
    }


    private ModLootConditions()
    {
        throw new IllegalStateException("Tried to initialize: ModLootConditions but this is a Utility class.");
    }
}
