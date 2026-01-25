package com.minecolonies.core.generation;

import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.expeditions.colony.types.ColonyExpeditionTypeDifficulty;
import com.minecolonies.core.loot.ExpeditionDifficultyCondition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer.Builder;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import static com.minecolonies.core.colony.events.ColonyExpeditionEvent.*;

/**
 * Manages creation of resource ids and other common blocks of information, for example for loot pools.
 */
public class ExpeditionResourceManager
{
    /**
     * Expedition difficulties
     */
    public static final ColonyExpeditionTypeDifficulty DIFF_2 = ColonyExpeditionTypeDifficulty.MEDIUM;
    public static final ColonyExpeditionTypeDifficulty DIFF_3 = ColonyExpeditionTypeDifficulty.HARD;

    /**
     * Number providers.
     */
    private static final NumberProvider COMMON_ITEM_COUNT   = UniformGenerator.between(4, 6);
    private static final NumberProvider UNCOMMON_ITEM_COUNT = UniformGenerator.between(2, 4);
    private static final NumberProvider RARE_ITEM_COUNT     = UniformGenerator.between(1, 2);
    private static final NumberProvider LOW_TOOL_DAMAGE     = UniformGenerator.between(0.75f, 0.95f);
    private static final NumberProvider MEDIUM_TOOL_DAMAGE  = UniformGenerator.between(0.5f, 0.75f);

    /**
     * Builder instance for simple expedition items.
     *
     * @param builder the underlying loot builder.
     */
    public record SimpleItemBuilder(Builder<?> builder) implements ToolItemBuilder<SimpleItemBuilder>
    {
        @Override
        public SimpleItemBuilder common()
        {
            this.builder.apply(SetItemCountFunction.setCount(COMMON_ITEM_COUNT));
            return this;
        }

        @Override
        public SimpleItemBuilder uncommon()
        {
            this.builder.apply(SetItemCountFunction.setCount(UNCOMMON_ITEM_COUNT));
            return this;
        }

        @Override
        public SimpleItemBuilder rare()
        {
            this.builder.apply(SetItemCountFunction.setCount(RARE_ITEM_COUNT));
            return this;
        }

        @Override
        public SimpleItemBuilder diffOnly(@NotNull final ColonyExpeditionTypeDifficulty difficulty)
        {
            this.builder.when(ExpeditionDifficultyCondition.forDifficulty(difficulty));
            return this;
        }

        @Override
        public SimpleItemBuilder diffBefore(@NotNull final ColonyExpeditionTypeDifficulty difficulty)
        {
            final ColonyExpeditionTypeDifficulty[] difficulties = Arrays.stream(ColonyExpeditionTypeDifficulty.values())
                                                                    .filter(f -> f.getLevel() <= difficulty.getLevel())
                                                                    .toArray(ColonyExpeditionTypeDifficulty[]::new);
            this.builder.when(ExpeditionDifficultyCondition.forDifficulty(difficulties));
            return this;
        }

        @Override
        public SimpleItemBuilder diffAfter(@NotNull final ColonyExpeditionTypeDifficulty difficulty)
        {
            final ColonyExpeditionTypeDifficulty[] difficulties = Arrays.stream(ColonyExpeditionTypeDifficulty.values())
                                                                    .filter(f -> f.getLevel() >= difficulty.getLevel())
                                                                    .toArray(ColonyExpeditionTypeDifficulty[]::new);
            this.builder.when(ExpeditionDifficultyCondition.forDifficulty(difficulties));
            return this;
        }

        @Override
        public SimpleItemBuilder damageLow()
        {
            this.builder.apply(SetItemDamageFunction.setDamage(LOW_TOOL_DAMAGE));
            return this;
        }

        @Override
        public SimpleItemBuilder damageMid()
        {
            this.builder.apply(SetItemDamageFunction.setDamage(MEDIUM_TOOL_DAMAGE));
            return this;
        }

        @Override
        public SimpleItemBuilder enchant(final Enchantment... enchantments)
        {
            if (enchantments.length > 0)
            {
                final EnchantRandomlyFunction.Builder enchantmentBuilder = EnchantRandomlyFunction.randomEnchantment();
                for (final Enchantment enchantment : enchantments)
                {
                    enchantmentBuilder.withEnchantment(enchantment);
                }
                this.builder.apply(enchantmentBuilder);
            }
            else
            {
                this.builder.apply(EnchantRandomlyFunction.randomApplicableEnchantment());
            }
            return this;
        }

        @Override
        public Builder<?> build()
        {
            return this.builder;
        }
    }

    /**
     * Get the correct structure ID for the input base structure name.
     *
     * @param structureId the base structure name.
     * @return the structure ID.
     */
    public static ResourceLocation getStructureId(final ResourceLocation structureId)
    {
        return new ResourceLocation(Constants.MOD_ID, structureId.withPrefix("expeditions/structures/").getPath());
    }

    /**
     * Create an adventure token loot item structure starts.
     *
     * @param structureId the structure id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureStartItem(final ResourceLocation structureId)
    {
        final CompoundTag structureStart = new CompoundTag();
        structureStart.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_START);
        structureStart.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, structureId.toString());

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureStart));
    }

    /**
     * Create an adventure token loot item structure ends.
     *
     * @param structureId the structure id.
     * @return the item builder.
     */
    public static LootPoolSingletonContainer.Builder<?> createStructureEndItem(final ResourceLocation structureId)
    {
        final CompoundTag structureEnd = new CompoundTag();
        structureEnd.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_STRUCTURE_END);
        structureEnd.putString(TOKEN_TAG_EXPEDITION_STRUCTURE, structureId.toString());

        return LootItem.lootTableItem(ModItems.adventureToken).apply(SetNbtFunction.setTag(structureEnd));
    }

    /**
     * Create an adventure token loot item for encounters.
     *
     * @param encounterId the encounter id.
     * @param weight      the weight of the loot table entry.
     * @return the item builder.
     */
    public static DifficultyBuilder<?> createEncounterLootItem(final ResourceLocation encounterId, final int weight)
    {
        return createEncounterLootItem(encounterId, weight, 1, true);
    }

    /**
     * Create an adventure token loot item for encounters.
     *
     * @param encounterId the encounter id.
     * @param weight      the weight of the loot table entry.
     * @param amount      the amount of encounters that will spawn.
     * @param scale       whether to scale the encounters with difficulty.
     * @return the item builder.
     */
    public static DifficultyBuilder<?> createEncounterLootItem(final ResourceLocation encounterId, final int weight, final int amount, final boolean scale)
    {
        final CompoundTag encounter = new CompoundTag();
        encounter.putString(TOKEN_TAG_EXPEDITION_TYPE, TOKEN_TAG_EXPEDITION_TYPE_ENCOUNTER);
        encounter.putString(TOKEN_TAG_EXPEDITION_ENCOUNTER, encounterId.toString());
        encounter.putInt(TOKEN_TAG_EXPEDITION_ENCOUNTER_AMOUNT, amount);
        encounter.putBoolean(TOKEN_TAG_EXPEDITION_ENCOUNTER_SCALE, scale);

        return new SimpleItemBuilder(LootItem.lootTableItem(ModItems.adventureToken).setWeight(weight).apply(SetNbtFunction.setTag(encounter)));
    }

    /**
     * Create a loot table structure reference for the given structure name.
     *
     * @param structureId the base structure name.
     * @param weight      the weight of the loot table entry.
     * @return the loot entry builder.
     */
    public static DifficultyBuilder<?> createStructureRef(final ResourceLocation structureId, final int weight)
    {
        return new SimpleItemBuilder(LootTableReference.lootTableReference(getStructureId(structureId)).setWeight(weight));
    }

    /**
     * Create a potion loot item.
     *
     * @param potion the potion type.
     * @param weight the weight of the loot table entry.
     * @return the item builder.
     */
    public static DifficultyBuilder<?> createPotionItem(final Potion potion, final int weight)
    {
        final ItemStack stack = new ItemStack(Items.POTION);
        PotionUtils.setPotion(stack, potion);
        return new SimpleItemBuilder(LootItem.lootTableItem(Items.POTION).setWeight(weight).apply(SetNbtFunction.setTag(stack.getTag())));
    }

    /**
     * Generate a simple loot item.
     *
     * @param item   the item.
     * @param weight the weight of the loot table entry.
     * @return the builder
     */
    public static BasicItemBuilder<?> createSimpleItem(final Item item, final int weight)
    {
        return new SimpleItemBuilder(LootItem.lootTableItem(item).setWeight(weight));
    }

    /**
     * Generate a simple loot tool item.
     *
     * @param item   the item.
     * @param weight the weight of the loot table entry.
     * @return the builder
     */
    public static EnchantItemBuilder<?> createEnchantItem(final Item item, final int weight)
    {
        return new SimpleItemBuilder(LootItem.lootTableItem(item).setWeight(weight));
    }

    /**
     * Generate a simple loot tool item.
     *
     * @param item   the item.
     * @param weight the weight of the loot table entry.
     * @return the builder
     */
    public static ToolItemBuilder<?> createToolItem(final Item item, final int weight)
    {
        return new SimpleItemBuilder(LootItem.lootTableItem(item).setWeight(weight));
    }

    /**
     * Root builder for expedition items.
     */
    private interface RootBuilder
    {
        /**
         * Return the original loot item builder for appending to a loot table builder.
         *
         * @return the loot item builder.
         */
        Builder<?> build();
    }

    /**
     * Builder for difficulty based expedition items.
     *
     * @param <T> the underlying builder implementation.
     */
    public interface DifficultyBuilder<T extends DifficultyBuilder<T>> extends RootBuilder
    {
        /**
         * Tell the item to only spawn on a given difficulty.
         *
         * @param difficulty the input difficulty.
         * @return the builder
         */
        T diffOnly(@NotNull final ColonyExpeditionTypeDifficulty difficulty);

        /**
         * Tell the item to only spawn on a difficulties before or identical to the given difficulty.
         *
         * @param difficulty the input difficulty.
         * @return the builder
         */
        T diffBefore(@NotNull final ColonyExpeditionTypeDifficulty difficulty);

        /**
         * Tell the item to only spawn on a difficulties after or identical to the given difficulty.
         *
         * @param difficulty the input difficulty.
         * @return the builder
         */
        T diffAfter(@NotNull final ColonyExpeditionTypeDifficulty difficulty);
    }

    /**
     * Builder for count based expedition items.
     *
     * @param <T> the underlying builder implementation.
     */
    public interface RarityBuilder<T extends DifficultyBuilder<T>> extends RootBuilder
    {
        /**
         * Indicate the item is a common item, spawning many items in the output.
         *
         * @return the builder
         */
        T common();

        /**
         * Indicate the item is an uncommon item, spawning few items in the output.
         *
         * @return the builder
         */
        T uncommon();

        /**
         * Indicate the item is a rare item, spawning only one or two items in the output.
         *
         * @return the builder
         */
        T rare();
    }

    /**
     * Builder for basic expedition items.
     */
    public interface BasicItemBuilder<T extends BasicItemBuilder<T>> extends DifficultyBuilder<T>, RarityBuilder<T>
    {
    }

    /**
     * Builder for tool expedition items.
     */
    public interface EnchantItemBuilder<T extends ToolItemBuilder<T>> extends BasicItemBuilder<T>
    {
        /**
         * Provides random enchantments on the given item.
         *
         * @param enchantments all possible enchantments on the tool, provide nothing for a random enchantment.
         * @return the builder
         */
        T enchant(Enchantment... enchantments);
    }

    /**
     * Builder for tool expedition items.
     */
    public interface ToolItemBuilder<T extends ToolItemBuilder<T>> extends EnchantItemBuilder<T>
    {
        /**
         * Provide a low amount of damage to the tool, anywhere from 5% - 25%.
         *
         * @return the builder
         */
        T damageLow();

        /**
         * Provide a medium amount of damage to the tool, anywhere from 25% - 50%.
         *
         * @return the builder
         */
        T damageMid();
    }
}
