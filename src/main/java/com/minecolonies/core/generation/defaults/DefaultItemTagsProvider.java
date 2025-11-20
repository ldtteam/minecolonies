package com.minecolonies.core.generation.defaults;

import com.google.gson.JsonObject;
import com.ldtteam.domumornamentum.block.types.ExtraBlockType;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TagConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

@SuppressWarnings("unchecked")
public class DefaultItemTagsProvider extends ItemTagsProvider
{
    private final PackOutput.PathProvider langPath;
    private JsonObject langJson;

    public DefaultItemTagsProvider(
      @NotNull final PackOutput output,
      final CompletableFuture<HolderLookup.Provider> lookupProvider,
      @NotNull final BlockTagsProvider blockTagsProvider,
      @Nullable final ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockTagsProvider.contentsGetter(), MOD_ID, existingFileHelper);
        langPath = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "lang");
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull final CachedOutput output)
    {
        langJson = new JsonObject();
        return super.run(output).thenCompose(r ->
                DataProvider.saveStable(output, langJson, langPath.json(new ResourceLocation(Constants.MOD_ID, "tag.item"))));
    }

    /**
     * @deprecated Use {@link #tag(TagKey, String)} instead to provide a translation.
     */
    @Deprecated
    @NotNull
    @Override
    protected IntrinsicTagAppender<Item> tag(@NotNull final TagKey<Item> tagKey)
    {
        return super.tag(tagKey);
    }

    @NotNull
    protected IntrinsicTagAppender<Item> tag(@NotNull final TagKey<Item> tagKey, @NotNull final String description)
    {
        translate(tagKey, description);
        return super.tag(tagKey);
    }

    /**
     * @deprecated Use {@link #copy(TagKey, TagKey, String)} instead to provide a description.
     */
    @Deprecated
    @Override
    protected void copy(@NotNull final TagKey<Block> blockTag, @NotNull final TagKey<Item> itemTag)
    {
        super.copy(blockTag, itemTag);
    }

    protected void copy(@NotNull final TagKey<Block> blockTag, @NotNull final TagKey<Item> itemTag, @NotNull final String description)
    {
        translate(itemTag, description);
        super.copy(blockTag, itemTag);
    }

    private void translate(@NotNull final TagKey<Item> tagKey, @NotNull final String description)
    {
        final String translationKey = Tags.getTagTranslationKey(tagKey);
        if (!langJson.has(translationKey))
        {
            langJson.addProperty(translationKey, description);
        }
    }

    @Override
    protected void addTags(final @NotNull HolderLookup.Provider provider)
    {
        ModTags.init();     // apparently runData doesn't run work queued in common setup

        tag(ModTags.compostables_poor, "Poor-Quality Compostables")
                .addTags(Tags.Items.SEEDS, ItemTags.SAPLINGS)
                .add(Items.PITCHER_POD, Items.SMALL_DRIPLEAF);
        tag(ModTags.compostables, "Normal-Quality Compostables")
          .add(Items.ROTTEN_FLESH)
          .add(Items.FEATHER, Items.PUMPKIN, Items.CARVED_PUMPKIN)
          .add(Items.SHORT_GRASS, Items.TALL_GRASS, Items.FERN, Items.LARGE_FERN, Items.HAY_BLOCK)
          .add(Items.BIG_DRIPLEAF, Items.SPORE_BLOSSOM, Items.GLOW_LICHEN, ModItems.mistletoe)
          .add(Items.KELP, Items.DRIED_KELP_BLOCK, Items.SEAGRASS, Items.CACTUS, Items.SUGAR_CANE, Items.VINE, Items.TWISTING_VINES, Items.WEEPING_VINES)
          .add(Items.COCOA_BEANS, Items.LILY_PAD, Items.SEA_PICKLE)
          .add(Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.MUSHROOM_STEM)
          .add(Items.CAKE, Items.MELON, Items.RABBIT_FOOT, Items.FERMENTED_SPIDER_EYE)
          .add(Items.MOSS_BLOCK, Items.MOSS_CARPET, Items.SHROOMLIGHT)
          .add(Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK, Items.NETHER_SPROUTS, Items.MANGROVE_ROOTS, Items.HANGING_ROOTS, Items.CRIMSON_ROOTS, Items.WARPED_ROOTS)
          .addTags(Tags.Items.CROPS, Tags.Items.EGGS, ItemTags.FLOWERS, ItemTags.FISHES, ItemTags.LEAVES, ItemTags.WOOL)
          .addTags(Tags.Items.FOODS_RAW_FISH, Tags.Items.FOODS_RAW_MEAT, Tags.Items.MUSHROOMS, ModTags.fungi);
        tag(ModTags.compostables_rich, "Rich-Quality Compostables")
                .add(Items.PODZOL, ModBlocks.blockCompostedDirt.asItem());

        // these tags are just for backwards compatibility and could be removed in a future Minecraft version
        copy(ModTags.concreteBlocks, ModTags.concreteItems, "Concrete");
        tag(ModTags.concretePowderItems, "Concrete Powder").addTag(Tags.Items.CONCRETE_POWDERS);
        tag(ModTags.rawMeat, "Raw Meat").addTag(Tags.Items.FOODS_RAW_MEAT);

        // this tag is just for backwards compatibility and could be removed in a future Minecraft version
        final TagKey<Item> shulkerBoxes = ItemTags.create(new ResourceLocation(MOD_ID, "shulker_boxes"));
        tag(shulkerBoxes, "Shulker Boxes").addTag(Tags.Items.SHULKER_BOXES);

        // this tag is just for backwards compatibility and could be removed in a future Minecraft version
        final TagKey<Item> glazedTerracotta = ItemTags.create(new ResourceLocation(MOD_ID, "glazed_terracotta"));
        tag(glazedTerracotta, "Glazed Terracotta").addTag(Tags.Items.GLAZED_TERRACOTTAS);

        final TagKey<Item> storageBlocks = ItemTags.create(new ResourceLocation(MOD_ID, "storage_blocks"));
        tag(storageBlocks, "Storage Blocks")
          .addTag(Tags.Items.STORAGE_BLOCKS)
          .add(Items.BONE_BLOCK)
          .add(Items.HAY_BLOCK)
          .add(Items.DRIED_KELP_BLOCK)
          .add(Items.HONEY_BLOCK)
          .add(Items.HONEYCOMB_BLOCK)
          .add(Items.SNOW_BLOCK)
          .add(Items.COPPER_BLOCK)
          .add(Items.RAW_COPPER_BLOCK)
          .add(Items.RAW_GOLD_BLOCK)
          .add(Items.RAW_IRON_BLOCK);

        // Be careful adding tags to this, as some, especially #minecraft:small_flowers, have logical impacts that
        // has made them used heavily in mods, including many where high availability has severe balance ramifications.
        tag(ModTags.floristFlowers, "Florist Flowers")
          .add(Items.SUNFLOWER)
          .add(Items.LILAC)
          .add(Items.ROSE_BUSH)
          .add(Items.PEONY)
          .add(Items.TALL_GRASS)
          .add(Items.LARGE_FERN)
          .add(Items.FERN)
          .add(Items.DANDELION)
          .add(Items.POPPY)
          .add(Items.BLUE_ORCHID)
          .add(Items.ALLIUM)
          .add(Items.AZURE_BLUET)
          .add(Items.RED_TULIP)
          .add(Items.ORANGE_TULIP)
          .add(Items.WHITE_TULIP)
          .add(Items.PINK_TULIP)
          .add(Items.OXEYE_DAISY)
          .add(Items.CORNFLOWER)
          .add(Items.LILY_OF_THE_VALLEY);

        copy(ModTags.fungiBlocks, ModTags.fungi, "Fungi");

        tag(ModTags.meshes, "Meshes")
          .add(ModItems.sifterMeshString)
          .add(ModItems.sifterMeshFlint)
          .add(ModItems.sifterMeshIron)
          .add(ModItems.sifterMeshDiamond);

        tag(ModTags.excludedFood, "Excluded Food")
          .add(Items.ENCHANTED_GOLDEN_APPLE)
          .add(Items.POISONOUS_POTATO)
          .add(Items.ROTTEN_FLESH)
          .add(Items.SPIDER_EYE)
          .add(Items.TROPICAL_FISH)
          .add(Items.PUFFERFISH)
          .add(Items.SUSPICIOUS_STEW)
          .add(ModItems.chorusBread)
          .add(ModItems.goldenBread);

        tag(ModTags.breakable_ore, "Breakable Ore")
          .addTag(ItemTags.COAL_ORES)
          .addTag(ItemTags.IRON_ORES)
          .addTag(ItemTags.COPPER_ORES)
          .addTag(ItemTags.GOLD_ORES)
          .addTag(ItemTags.REDSTONE_ORES)
          .addTag(ItemTags.EMERALD_ORES)
          .addTag(ItemTags.LAPIS_ORES)
          .addTag(ItemTags.DIAMOND_ORES)
          .add(Items.NETHER_QUARTZ_ORE);

        tag(ModTags.raw_ore, "Raw Ore")
          .add(Items.RAW_IRON)
          .add(Items.RAW_COPPER)
          .add(Items.RAW_GOLD);

        tag(ModTags.poisonous_food, "Poisonous Food")
          .add(Items.POISONOUS_POTATO)
          .add(Items.CHICKEN)
          .add(Items.SPIDER_EYE)
          .add(Items.ROTTEN_FLESH);

        final Item[] paperExtras = getDomumExtra(ExtraBlockType.BASE_PAPER, ExtraBlockType.LIGHT_PAPER);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_BAKER), "Baker Crafting Ingredients")
          .addTag(Tags.Items.CROPS_WHEAT);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_BAKER), "Baker Crafting Excluded Ingredients");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_BAKER), "Baker Crafting Products")
          .add(ModItems.cornmeal)
          .add(ModItems.cheese_pizza)
          .add(ModItems.plain_cheesecake)
          .add(ModItems.apple_pie);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_BAKER), "Baker Crafting Excluded Products")
          .add(Items.BREAD, Items.CAKE, Items.COOKIE, Items.PUMPKIN_PIE)
          .add(Items.PACKED_MUD)
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_COOK));

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_BLACKSMITH), "Blacksmith Crafting Ingredients")
          .add(Items.DIAMOND_BLOCK, Items.EMERALD_BLOCK)
          .addTags(Tags.Items.NUGGETS, Tags.Items.INGOTS);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_BLACKSMITH), "Blacksmith Crafting Excluded Ingredients")
          .addTag(Tags.Items.CROPS)
          .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_DYER))
          .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_MECHANIC))
          .add(Items.BRICK, Items.NETHER_BRICK);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_BLACKSMITH), "Blacksmith Crafting Products")
          .add(Items.SHEARS, Items.LIGHTNING_ROD, Items.MACE)
          .addTags(Tags.Items.NUGGETS, Tags.Items.INGOTS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_BLACKSMITH), "Blacksmith Crafting Excluded Products")
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER))
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC))
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_SAWMILL))
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONEMASON))
          .add(Items.FIREWORK_STAR)
          .add(Items.GLISTERING_MELON_SLICE)
          .add(Items.BOW, Items.CROSSBOW);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_COOK), "Chef Crafting Ingredients")
          .addTag(ItemTags.FISHES)
          .add(Items.BEEF)
          .add(Items.MUTTON)
          .add(Items.CHICKEN)
          .add(Items.PORKCHOP)
          .add(Items.RABBIT)
          .add(Items.POTATO)
          .add(Items.KELP)
          .add(Items.DRIED_KELP)
          .add(Items.DRIED_KELP_BLOCK)
          .add(Items.EGG)
          .add(ModItems.large_milk_bottle)
          .add(ModItems.large_soy_milk_bottle)
          .add(ModItems.large_water_bottle)
          .add(Items.MILK_BUCKET);

        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_COOK), "Chef Crafting Excluded Ingredients")
          .addTag(Tags.Items.CROPS_WHEAT);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_COOK), "Chef Crafting Products")
          .add(ModItems.baked_salmon)
          .add(ModItems.butter)
          .add(ModItems.cabochis)
          .add(ModItems.cheddar_cheese)
          .add(ModItems.congee)
          .add(ModItems.cooked_rice)
          .add(ModItems.eggplant_dolma)
          .add(ModItems.feta_cheese)
          .add(ModItems.lamb_stew)
          .add(ModItems.pasta_plain)
          .add(ModItems.pasta_tomato)
          .add(ModItems.pepper_hummus)
          .add(ModItems.pottage)
          .add(ModItems.raw_noodle)
          .add(ModItems.rice_ball)
          .add(ModItems.tofu)
          .add(ModItems.creamcheese)
          .add(ModItems.soysauce)
          .add(ModItems.cheese_ravioli)
          .add(ModItems.chicken_broth)
          .add(ModItems.corn_chowder)
          .add(ModItems.spicy_grilled_chicken)
          .add(ModItems.kebab)
          .add(ModItems.meat_ravioli)
          .add(ModItems.mint_jelly)
          .add(ModItems.mint_tea)
          .add(ModItems.pea_soup)
          .add(ModItems.polenta)
          .add(ModItems.potato_soup)
          .add(ModItems.squash_soup)
          .add(ModItems.veggie_ravioli)
          .add(ModItems.yogurt)
          .add(ModItems.baked_salmon)
          .add(ModItems.eggdrop_soup)
          .add(ModItems.fish_n_chips)
          .add(ModItems.kimchi)
          .add(ModItems.pierogi)
          .add(ModItems.veggie_quiche)
          .add(ModItems.veggie_soup)
          .add(ModItems.yogurt_with_berries)
          .add(ModItems.mutton_dinner)
          .add(ModItems.tortillas)
          .add(ModItems.spicy_eggplant);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_COOK), "Chef Crafting Excluded Products")
          .add(Items.BREAD, Items.CAKE, Items.COOKIE, Items.PUMPKIN_PIE, ModItems.cheese_pizza, ModItems.plain_cheesecake, ModItems.apple_pie, ModItems.cornmeal);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_DYER), "Dyer Crafting Ingredients")
          .addTag(Tags.Items.DYES);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_DYER), "Dyer Crafting Excluded Ingredients");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER), "Dyer Crafting Products")
          .addTag(Tags.Items.DYES)
          .add(Items.FIREWORK_STAR)
          .add(Items.RED_NETHER_BRICKS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_DYER), "Dyer Crafting Excluded Products")
          .addTags(ModTags.concretePowderItems);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER_SMELTING), "Dyer Smelting Products")
          .addTag(Tags.Items.DYES);

        com.ldtteam.domumornamentum.block.ModBlocks.getInstance().getExtraTopBlocks().stream()
          .filter(f -> f.getType().getColor() != null)
          .map(Block::asItem)
          .forEach(item -> super.tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER)).add(item));

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_FARMER), "Farmer Crafting Ingredients")
                .add(Items.HAY_BLOCK)
                .add(Items.SHORT_GRASS)
                .add(Items.FERN);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_FARMER), "Farmer Crafting Excluded Ingredients");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_FARMER), "Farmer Crafting Products")
          .add(Items.HAY_BLOCK)
          .addTag(Tags.Items.SEEDS)
          .add(ModBlocks.blockCompostedDirt.asItem())
          .add(Items.MELON)
          .add(Items.COARSE_DIRT)
          .add(Items.FERMENTED_SPIDER_EYE)
          .add(Items.GLISTERING_MELON_SLICE)
          .add(Items.MUD_BRICKS, Items.PACKED_MUD, Items.MUDDY_MANGROVE_ROOTS);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_FARMER), "Farmer Crafting Excluded Products");

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_FLETCHER), "Fletcher Crafting Ingredients")
          .addTag(Tags.Items.STRINGS)
          .addTag(ItemTags.LEAVES)
          .addTag(ItemTags.WOOL)
          .add(Items.RABBIT_HIDE)
          .add(Items.LEATHER)
          .add(Items.FISHING_ROD);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_FLETCHER), "Fletcher Crafting Excluded Ingredients")
          .addTag(Tags.Items.DYES);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_FLETCHER), "Fletcher Crafting Products")
          .addTag(Tags.Items.STRINGS)
          .add(Items.MOSS_CARPET);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_FLETCHER), "Fletcher Crafting Excluded Products")
          .add(Items.BOOK)
          .add(Items.ITEM_FRAME);
        tag(ModTags.crafterDoIngredient.get(TagConstants.CRAFTING_FLETCHER), "Fletcher Domum Ingredients")
          .add(Items.AZALEA_LEAVES, Items.FLOWERING_AZALEA_LEAVES)
          .add(Items.COARSE_DIRT, Items.ROOTED_DIRT, Items.GRASS_BLOCK, Items.HAY_BLOCK, Items.MOSS_BLOCK, Items.DRIED_KELP_BLOCK)
          .add(Items.MUD, Items.PACKED_MUD, Items.MUD_BRICKS, Items.MUDDY_MANGROVE_ROOTS, Items.PODZOL, Items.MYCELIUM)
          .add(Items.BROWN_MUSHROOM_BLOCK, Items.RED_MUSHROOM_BLOCK, Items.NETHER_WART_BLOCK, Items.WARPED_WART_BLOCK);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_GLASSBLOWER), "Glassblower Crafting Ingredients")
          .addTag(Tags.Items.GLASS_BLOCKS)
          .addTag(Tags.Items.GLASS_PANES);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_GLASSBLOWER), "Glassblower Crafting Excluded Ingredients")
          .addTag(Tags.Items.DYES);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_GLASSBLOWER), "Glassblower Crafting Products");
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_GLASSBLOWER), "Glassblower Crafting Excluded Products");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_GLASSBLOWER_SMELTING), "Glassblower Smelting Products")
          .addTag(Tags.Items.GLASS_BLOCKS);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_MECHANIC), "Mechanic Crafting Ingredients")
          .addTag(Tags.Items.DUSTS_REDSTONE)
          .addTag(Tags.Items.ORES_REDSTONE)
          .addTag(Tags.Items.STORAGE_BLOCKS_REDSTONE)
          .addTag(storageBlocks)
          .add(Items.BLAZE_ROD)
          .add(Items.SLIME_BALL)
          .add(Items.GUNPOWDER)
          .add(Items.ENDER_PEARL)
          .add(Items.ENDER_EYE)
          .add(Items.REDSTONE_TORCH)
          .add(Items.GLOWSTONE_DUST)
          .add(Items.DRIED_KELP_BLOCK)
          .add(Items.AMETHYST_SHARD);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_MECHANIC), "Mechanic Crafting Excluded Ingredients");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC), "Mechanic Crafting Products")
          .addTag(storageBlocks)
          .addTag(ItemTags.RAILS)
          .addTag(ItemTags.BUTTONS)
          .addTag(ItemTags.WOODEN_PRESSURE_PLATES)
          .add(Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
          .add(Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
          .add(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
          .add(Items.STONE_PRESSURE_PLATE)
          .add(Items.BLUE_ICE)
          .add(Items.PACKED_ICE)
          .add(Items.NETHER_WART_BLOCK)
          .add(Items.DAYLIGHT_DETECTOR)
          .add(Items.COMPARATOR)
          .add(Items.LEVER)
          .add(Items.PISTON)
          .add(Items.STICKY_PISTON)
          .add(Items.TRIPWIRE_HOOK)
          .add(Items.ENCHANTING_TABLE)
          .add(Items.JACK_O_LANTERN)
          .add(Items.LANTERN)
          .add(Items.SEA_LANTERN)
          .add(Items.SOUL_LANTERN)
          .add(Items.SOUL_TORCH)
          .add(Items.END_ROD)
          .add(Items.TORCH)
          .add(Items.ENDER_CHEST)
          .add(Items.TRAPPED_CHEST)
          .add(Items.FIRE_CHARGE)
          .add(Items.CONDUIT)
          .add(Items.RESPAWN_ANCHOR)
          .add(Items.RECOVERY_COMPASS)
          .add(Items.SHULKER_BOX)
          .add(Items.SLIME_BALL)
          .add(Items.GLOW_ITEM_FRAME)
          .add(Items.SPYGLASS)
          .add(Items.WAXED_COPPER_DOOR, Items.WAXED_COPPER_TRAPDOOR)
          .add(Items.WAXED_EXPOSED_COPPER_DOOR, Items.WAXED_EXPOSED_COPPER_TRAPDOOR)
          .add(Items.WAXED_OXIDIZED_COPPER_DOOR, Items.WAXED_OXIDIZED_COPPER_TRAPDOOR)
          .add(Items.WAXED_WEATHERED_COPPER_DOOR, Items.WAXED_WEATHERED_COPPER_TRAPDOOR);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_MECHANIC), "Mechanic Crafting Excluded Products")
          .add(Items.SPECTRAL_ARROW)
          .add(Items.HAY_BLOCK, Items.WHEAT)
          .add(Items.LEAD);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_PLANTATION), "Plantation Crafting Ingredients")
          .add(Items.BAMBOO)
          .add(paperExtras);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_PLANTATION), "Plantation Crafting Excluded Ingredients");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_PLANTATION), "Plantation Crafting Products")
          .add(Items.BOOK)
          .add(Items.PAPER)
          .add(Items.SUGAR)
          .add(Items.WRITABLE_BOOK)
          .add(paperExtras);
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_PLANTATION), "Plantation Crafting Excluded Products");

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_SAWMILL), "Sawmill Crafting Ingredients")
          .addTag(ItemTags.LOGS)
          .add(Items.CACTUS);
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_SAWMILL), "Sawmill Crafting Excluded Ingredients")
          .addTag(Tags.Items.INGOTS)
          .addTag(Tags.Items.STONES)
          .addTag(Tags.Items.DUSTS_REDSTONE)
          .addTag(Tags.Items.STRINGS);
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_SAWMILL), "Sawmill Crafting Products")
          .addTag(ItemTags.PLANKS)
          .addTags(ItemTags.WOODEN_SLABS, ItemTags.WOODEN_STAIRS)
          .add(Items.BAMBOO_MOSAIC, Items.BAMBOO_MOSAIC_SLAB, Items.BAMBOO_MOSAIC_STAIRS, Items.BAMBOO_HANGING_SIGN)
          .addTags(ItemTags.BOATS, ItemTags.CHEST_BOATS)
          .add(ModBlocks.blockBarrel.asItem());
        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_SAWMILL), "Sawmill Crafting Excluded Products")
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC))
          .add(Items.MAGMA_CREAM);
        tag(ModTags.crafterDoIngredient.get(TagConstants.CRAFTING_SAWMILL), "Sawmill Crafting Domum Ingredients")
          .add(Items.BAMBOO_BLOCK, Items.BAMBOO_MOSAIC, Items.BAMBOO_PLANKS, Items.STRIPPED_BAMBOO_BLOCK)
          .add(Items.CRIMSON_NYLIUM, Items.WARPED_NYLIUM);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_STONEMASON), "Stonemason Crafting Ingredients")
          .add(Items.BRICK, Items.BRICKS, Items.STONE_BRICKS, Items.CHISELED_STONE_BRICKS, Items.CRACKED_STONE_BRICKS, Items.MOSSY_STONE_BRICKS)
          .add(Items.NETHER_BRICK, Items.NETHERRACK, Items.NETHER_BRICKS, Items.CHISELED_NETHER_BRICKS, Items.RED_NETHER_BRICKS)
          .add(Items.DEEPSLATE_BRICKS, Items.DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_BRICKS, Items.CRACKED_DEEPSLATE_TILES)
          .add(Items.POPPED_CHORUS_FRUIT)
          .add(Items.PURPUR_BLOCK, Items.PURPUR_SLAB, Items.PURPUR_PILLAR)
          .add(Items.PRISMARINE_SHARD, Items.PRISMARINE_CRYSTALS)
          .add(Items.SMOOTH_STONE, Items.OBSIDIAN, Items.CRYING_OBSIDIAN)
          .add(Items.DEEPSLATE, Items.CHISELED_DEEPSLATE, Items.COBBLED_DEEPSLATE, Items.POLISHED_DEEPSLATE)
          .add(Items.BLACKSTONE, Items.GILDED_BLACKSTONE)
          .add(Items.POLISHED_BLACKSTONE, Items.POLISHED_BLACKSTONE_BRICKS)
          .add(Items.EXPOSED_COPPER, Items.OXIDIZED_COPPER, Items.WEATHERED_COPPER)
          .add(Items.WAXED_COPPER_BLOCK, Items.WAXED_EXPOSED_COPPER, Items.WAXED_OXIDIZED_COPPER, Items.WAXED_WEATHERED_COPPER)
          .add(Items.CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER, Items.WEATHERED_CUT_COPPER)
          .add(Items.WAXED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER)
          .add(Items.BASALT, Items.POLISHED_BASALT, Items.SMOOTH_BASALT, Items.TUFF)
          .addTags(ItemTags.TERRACOTTA, glazedTerracotta)
          .addTags(Tags.Items.STONES, Tags.Items.COBBLESTONES, Tags.Items.END_STONES)
          .addTags(Tags.Items.SANDSTONE_BLOCKS, ModTags.concreteItems)
          .addTags(com.ldtteam.domumornamentum.tag.ModTags.BRICK_ITEMS)
          .addTags(com.ldtteam.domumornamentum.tag.ModTags.EXTRA_BLOCK_ITEMS)
          .addTags(ItemTags.STAIRS, ItemTags.SLABS, ItemTags.WALLS);

        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_STONEMASON), "Stonemason Crafting Excluded Ingredients")
          .add(Items.STICK)
          .addTags(ItemTags.LOGS, ItemTags.PLANKS)
          .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_MECHANIC))
          .addTag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_DYER));

        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONEMASON), "Stonemason Crafting Products")
          .add(Items.POLISHED_DEEPSLATE, Items.DEEPSLATE_BRICKS, Items.DEEPSLATE_TILES)
          .add(Items.CRACKED_DEEPSLATE_BRICKS, Items.CRACKED_DEEPSLATE_TILES)
          .add(Items.BRICKS, Items.POLISHED_BLACKSTONE_BRICKS, Items.TUFF_BRICKS)
          .add(Items.NETHER_BRICKS, Items.CHISELED_NETHER_BRICKS)
          .add(Items.DRIPSTONE_BLOCK)
          .add(Items.QUARTZ_BLOCK, Items.CHISELED_QUARTZ_BLOCK)
          .add(Items.QUARTZ_PILLAR)
          .add(Items.QUARTZ_BRICKS)
          .add(Items.CUT_COPPER, Items.EXPOSED_CUT_COPPER, Items.OXIDIZED_CUT_COPPER, Items.WEATHERED_CUT_COPPER)
          .add(Items.WAXED_COPPER_BLOCK, Items.WAXED_EXPOSED_COPPER, Items.WAXED_OXIDIZED_COPPER, Items.WAXED_WEATHERED_COPPER)
          .add(Items.WAXED_CUT_COPPER, Items.WAXED_EXPOSED_CUT_COPPER, Items.WAXED_OXIDIZED_CUT_COPPER, Items.WAXED_WEATHERED_CUT_COPPER)
          .add(Items.MAGMA_BLOCK)
          .add(Items.SNOW)
          .addTag(com.ldtteam.domumornamentum.tag.ModTags.BRICK_ITEMS)
          .addTag(com.ldtteam.domumornamentum.tag.ModTags.EXTRA_BLOCK_ITEMS)
          .addTags(Tags.Items.STONES, Tags.Items.COBBLESTONES, Tags.Items.SANDSTONE_BLOCKS)
          .addTags(ItemTags.STONE_BRICKS, ItemTags.SLABS, ItemTags.STAIRS, ItemTags.WALLS);

        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_STONEMASON), "Stonemason Crafting Excluded Products")
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_MECHANIC))
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_DYER))
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_SAWMILL))
          .addTag(ItemTags.TRIM_TEMPLATES)
          .add(Items.LECTERN, Items.PISTON, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)
          .add(Items.PRISMARINE, Items.PRISMARINE_BRICKS)
          .add(paperExtras);

        tag(ModTags.crafterDoIngredient.get(TagConstants.CRAFTING_STONEMASON), "Stonemason Crafting Domum Ingredients")
          .add(Items.STONE, Items.CALCITE, Items.POLISHED_ANDESITE, Items.POLISHED_DIORITE, Items.POLISHED_GRANITE)
          .add(Items.QUARTZ_BLOCK, Items.SMOOTH_QUARTZ, Items.QUARTZ_BRICKS, Items.QUARTZ_PILLAR, Items.CHISELED_QUARTZ_BLOCK)
          .add(Items.NETHERRACK)
          .add(Items.BLACKSTONE, Items.CHISELED_POLISHED_BLACKSTONE, Items.CRACKED_POLISHED_BLACKSTONE_BRICKS)
          .add(Items.PRISMARINE, Items.PRISMARINE_BRICKS, Items.DARK_PRISMARINE)
          .add(Items.END_STONE_BRICKS);

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_STONE_SMELTERY), "Stonesmelter Crafting Ingredients")
          .addTag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONEMASON));
        tag(ModTags.crafterIngredientExclusions.get(TagConstants.CRAFTING_STONE_SMELTERY), "Stonesmelter Crafting Excluded Ingredients");
        tag(ModTags.crafterProduct.get(TagConstants.CRAFTING_STONE_SMELTERY), "Stonesmelter Crafting Products")
          .add(Items.BRICK)
          .add(Items.SMOOTH_BASALT)
          .add(Items.SMOOTH_QUARTZ)
          .add(Items.POPPED_CHORUS_FRUIT)
          .add(Items.SPONGE)
          .add(Items.SMOOTH_SANDSTONE)
          .add(Items.SMOOTH_RED_SANDSTONE)
          .add(Items.COAL)
          .add(Items.CHARCOAL)
          .add(Items.NETHER_BRICK)
          .addTag(Tags.Items.STONES)
          .add(Items.SMOOTH_STONE)
          .add(Items.DEEPSLATE)
          .addTags(ItemTags.TERRACOTTA, glazedTerracotta)
          .addTag(ItemTags.STONE_BRICKS);

        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_STONE_SMELTERY), "Stonesmelter Crafting Excluded Products");

        tag(ModTags.crafterIngredient.get(TagConstants.CRAFTING_REDUCEABLE), "Reduceable Ingredients")

          .add(Items.BOOK, Items.PAPER, Items.SUGAR)
          .addTag(ItemTags.FISHES)
          .add(Items.BEEF)
          .add(Items.MUTTON)
          .add(Items.CHICKEN)
          .add(Items.PORKCHOP)
          .add(Items.RABBIT)
          .add(Items.POTATO)
          .add(Items.STICK)
          .add(Items.LEATHER)
          .add(Items.RABBIT_HIDE)
          .add(Items.NETHER_BRICK)
          .add(Items.POPPED_CHORUS_FRUIT)
          .add(Items.PRISMARINE_SHARD)
          .add(Items.PRISMARINE_CRYSTALS)
          .addTags(Tags.Items.GLASS_BLOCKS, Tags.Items.GLASS_PANES)
          .addTag(Tags.Items.CROPS_WHEAT)
          .addTag(Tags.Items.STRINGS)
          .addTags(Tags.Items.NUGGETS, Tags.Items.INGOTS)
          .addTags(Tags.Items.STONES, Tags.Items.COBBLESTONES)
          .addTags(Tags.Items.GRAVELS, Tags.Items.SANDS)
          .addTags(Tags.Items.DUSTS, Tags.Items.GEMS)
          .addTag(ItemTags.WOOL)
          .addTags(ItemTags.LOGS, ItemTags.PLANKS, ItemTags.STONE_BRICKS);


        tag(ModTags.crafterProductExclusions.get(TagConstants.CRAFTING_REDUCEABLE), "Not Reduceable Ingredients")
          .add(Items.GLOWSTONE)
          .add(ModItems.breadDough)
          .add(ModItems.cookieDough)
          .add(ModItems.rawPumpkinPie)
          .add(ModItems.cakeBatter)
          .addTags(Tags.Items.STONES, Tags.Items.COBBLESTONES)
          .addTags(Tags.Items.GRAVELS, Tags.Items.SANDS)
          .addTags(Tags.Items.INGOTS, storageBlocks);

        tag(ModTags.ignoreNBT, "Ignore NBT")
          .addTag(ItemTags.BANNERS);

        super.tag(Tags.Items.FOODS).add(ModItems.getAllFoods());

        super.tag(Tags.Items.FOODS_BREAD)
          .add(ModItems.milkyBread,
            ModItems.sugaryBread,
            ModItems.goldenBread,
            ModItems.chorusBread,
            ModItems.flatbread,
            ModItems.hand_pie,
            ModItems.lembas_scone,
            ModItems.manchet_bread,
            ModItems.muffin,
            ModItems.stew_trencher,
            ModItems.stuffed_pita);

        super.tag(Tags.Items.FOODS_CANDY)
          .add(ModItems.hand_pie)
          .add(ModItems.muffin);

        super.tag(Tags.Items.FOODS_COOKED_MEAT)
          .add(ModItems.lamb_stew);

        super.tag(Tags.Items.FOODS_GOLDEN)
          .add(ModItems.goldenBread);

        super.tag(Tags.Items.FOODS_SOUP)
          .add(ModItems.cabochis)
          .add(ModItems.lamb_stew)
          .add(ModItems.pottage);

        super.tag(Tags.Items.FOODS_VEGETABLE)
          .add(ModItems.cabochis)
          .add(ModItems.eggplant_dolma)
          .add(ModItems.pottage)
          .add(ModItems.stuffed_pepper)
          .add(ModItems.stuffed_pita);

        tag(ItemTags.BOOKSHELF_BOOKS)
            .add(ModItems.ancientTome)
            .add(ModItems.colonyMap)
            .add(ModItems.clipboard)
            .add(ModItems.questLog)
            .add(ModItems.resourceScroll);
    }

    @NotNull
    private static Item[] getDomumExtra(@NotNull final ExtraBlockType... types)
    {
        final Set<ExtraBlockType> typesSet = new HashSet<>(Arrays.asList(types));
        return com.ldtteam.domumornamentum.block.ModBlocks.getInstance().getExtraTopBlocks().stream()
                 .filter(extra -> typesSet.contains(extra.getType()))
                 .map(Block::asItem)
                 .toArray(Item[]::new);
    }
}
