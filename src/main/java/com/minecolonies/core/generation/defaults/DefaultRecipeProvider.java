package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.items.ModTags;
import com.minecolonies.api.util.constant.TagConstants;
import com.minecolonies.core.generation.CompostRecipeBuilder;
import com.minecolonies.core.recipes.FoodIngredient;
import com.minecolonies.core.recipes.PlantIngredient;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static com.ldtteam.structurize.items.ModItems.buildTool;
import static com.ldtteam.structurize.items.ModItems.shapeTool;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for standard crafting recipes
 */
public class DefaultRecipeProvider extends RecipeProvider
{

    public DefaultRecipeProvider(final PackOutput output, final CompletableFuture<Provider> lookupProvider)
    {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(@NotNull final RecipeOutput consumer)
    {
        buildHutRecipes(consumer);
        buildOtherBlocks(consumer);
        buildOtherItems(consumer);

        buildFood(consumer);

        CompostRecipeBuilder.strength(2)
                .input(new FoodIngredient.Builder().maxSaturation(0.5f).build())
                .input(Ingredient.of(ModTags.compostables_poor))
                .save(consumer, TagConstants.COMPOSTABLES_POOR);
        CompostRecipeBuilder.strength(4)
                .input(new FoodIngredient.Builder().minSaturation(0.5f).maxSaturation(1.0f).build())
                .input(PlantIngredient.getInstance())
                .input(Ingredient.of(ModTags.compostables))
                .save(consumer, TagConstants.COMPOSTABLES);
        CompostRecipeBuilder.strength(8)
                .input(new FoodIngredient.Builder().minSaturation(1.0f).build())
                .input(Ingredient.of(ModTags.compostables_rich))
                .save(consumer, TagConstants.COMPOSTABLES_RICH);
    }

    private void buildHutRecipes(@NotNull final RecipeOutput consumer)
    {
        registerHutRecipe3(consumer, ModBlocks.blockHutArchery, Items.BOW);
        registerHutRecipe1(consumer, ModBlocks.blockHutBaker, Items.WHEAT);
        registerHutRecipe1(consumer, ModBlocks.blockHutBarracks, Items.IRON_BLOCK);
        registerHutRecipe1(consumer, ModBlocks.blockHutBeekeeper, Items.BEEHIVE);
        registerHutRecipe3(consumer, ModBlocks.blockHutBlacksmith, Items.IRON_INGOT);
        registerHutRecipe1(consumer, ModBlocks.blockHutBuilder, ItemTags.WOODEN_DOORS);
        registerHutRecipe1(consumer, ModBlocks.blockHutChickenHerder, Items.EGG);
        registerHutRecipe1(consumer, ModBlocks.blockHutHome, Items.TORCH);
        registerHutRecipe3(consumer, ModBlocks.blockHutCombatAcademy, Items.IRON_SWORD);
        registerHutRecipe1(consumer, ModBlocks.blockHutComposter, ModBlocks.blockBarrel);
        registerHutRecipe1(consumer, ModBlocks.blockHutConcreteMixer, Items.WHITE_CONCRETE_POWDER);
        registerHutRecipe1(consumer, ModBlocks.blockHutCook, Items.APPLE);
        registerHutRecipe1(consumer, ModBlocks.blockHutCowboy, Items.BEEF);
        registerHutRecipe1(consumer, ModBlocks.blockHutDeliveryman, Items.LEATHER_BOOTS);
        registerHutRecipe1x2(consumer, ModBlocks.blockHutDeliveryman, Items.IRON_BOOTS, "iron");
        registerHutRecipe1(consumer, ModBlocks.blockHutDyer, Items.RED_DYE);
        registerHutRecipe1(consumer, ModBlocks.blockHutEnchanter, Items.ENCHANTING_TABLE);
        registerHutRecipe1(consumer, ModBlocks.blockHutFarmer, Items.WOODEN_HOE);
        registerHutRecipe1x2(consumer, ModBlocks.blockHutFarmer, Items.STONE_HOE, "stone");
        registerHutRecipe1(consumer, ModBlocks.blockHutFisherman, Items.FISHING_ROD);
        registerHutRecipe1(consumer, ModBlocks.blockHutFletcher, Items.STRING);
        registerHutRecipe1(consumer, ModBlocks.blockHutFlorist, Items.POPPY);
        registerHutRecipe1(consumer, ModBlocks.blockHutGlassblower, Items.GLASS);
        registerHutRecipe1x2(consumer, ModBlocks.blockHutGuardTower, Items.BOW, "");
        registerHutRecipe1(consumer, ModBlocks.blockHutHospital, Items.CARROT);
        registerHutRecipe1(consumer, ModBlocks.blockHutLibrary, Items.BOOK);
        registerHutRecipe1(consumer, ModBlocks.blockHutLumberjack, Items.WOODEN_AXE);
        registerHutRecipe1x2(consumer, ModBlocks.blockHutLumberjack, Items.STONE_AXE, "stone");
        registerHutRecipe1(consumer, ModBlocks.blockHutMechanic, Items.REDSTONE_BLOCK);
        registerHutRecipe1(consumer, ModBlocks.blockHutMiner, Items.WOODEN_PICKAXE);
        registerHutRecipe1x2(consumer, ModBlocks.blockHutMiner, Items.STONE_PICKAXE, "stone");
        registerHutRecipe1(consumer, ModBlocks.blockHutMysticalSite, Items.DIAMOND);
        registerHutRecipe1(consumer, ModBlocks.blockHutPlantation, Items.CACTUS);
        registerHutRecipe1(consumer, ModBlocks.blockHutRabbitHutch, Items.RABBIT);
        registerHutRecipe3(consumer, ModBlocks.blockHutSawmill, Items.WOODEN_AXE);
        registerHutRecipe1(consumer, ModBlocks.blockHutShepherd, Items.SHEARS);
        registerHutRecipe3(consumer, ModBlocks.blockHutSifter, Items.STRING);
        registerHutRecipe1(consumer, ModBlocks.blockHutSmeltery, Items.IRON_INGOT);
        registerHutRecipe3(consumer, ModBlocks.blockHutStonemason, Items.STONE_BRICKS);
        registerHutRecipe1(consumer, ModBlocks.blockHutSwineHerder, Items.PORKCHOP);
        registerHutRecipe1(consumer, ModBlocks.blockHutTavern, Items.BARREL);
        registerHutRecipe1(consumer, ModBlocks.blockHutTownHall, buildTool.get());
        registerHutRecipe1(consumer, ModBlocks.blockHutWareHouse, Tags.Items.CHESTS);
        registerHutRecipe1(consumer, ModBlocks.blockHutNetherWorker, Items.OBSIDIAN);
        registerHutRecipe1(consumer, ModBlocks.blockHutAlchemist, Items.BREWING_STAND);
        registerHutRecipe1(consumer, ModBlocks.blockHutKitchen, Items.SMOKER);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockHutCrusher)
                .pattern("XTX")
                .pattern("CBC")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('C', Items.COBBLESTONE)
                .define('B', Items.IRON_INGOT)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockHutGraveyard)
                .pattern("XTX")
                .pattern("XBX")
                .pattern("XXX")
                .define('X', Items.STONE)
                .define('B', Items.BONE)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), Items.BONE))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockHutSchool)
                .pattern("XTX")
                .pattern("XBX")
                .pattern("XBX")
                .define('X', ItemTags.PLANKS)
                .define('B', Items.FEATHER)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), Items.FEATHER))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockHutUniversity)
                .pattern("XTX")
                .pattern("XBX")
                .pattern("XBX")
                .define('X', ItemTags.PLANKS)
                .define('B', Items.BOOK)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), Items.BOOK))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockHutStoneSmeltery)
                .pattern("XTX")
                .pattern("AFA")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('A', Items.STONE_BRICKS)
                .define('F', Items.FURNACE)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), Items.STONE_BRICKS))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockScarecrow)
                .pattern(" H ")
                .pattern("SLS")
                .pattern(" S ")
                .define('L', Items.LEATHER)
                .define('S', Items.STICK)
                .define('H', Ingredient.of(Items.HAY_BLOCK, Items.PUMPKIN))
                .unlockedBy("has_items", hasAllOf(buildTool.get(), ModBlocks.blockHutFarmer))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockPlantationField)
          .pattern(" T ")
          .pattern("SIS")
          .pattern("PPP")
          .define('S', Items.STICK)
          .define('P', ItemTags.PLANKS)
          .define('I', Items.IRON_INGOT)
          .define('T', buildTool.get())
          .unlockedBy("has_items", hasAllOf(buildTool.get(), ModBlocks.blockHutPlantation))
          .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockSimpleQuarry)
                .pattern("XTX")
                .pattern("XDX")
                .pattern("XBX")
                .define('X', ItemTags.PLANKS)
                .define('D', Items.IRON_PICKAXE)
                .define('B', Items.BARREL)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), ModBlocks.blockHutMiner))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockMediumQuarry)
                .pattern("XTX")
                .pattern("XDX")
                .pattern("XBX")
                .define('X', ItemTags.PLANKS)
                .define('D', Items.DIAMOND_PICKAXE)
                .define('B', Items.BARREL)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), ModBlocks.blockHutMiner))
                .save(consumer);

//        ShapedRecipeBuilder.shaped(ModBlocks.blockLargeQuarry)
//                .pattern("XTX")
//                .pattern("XDX")
//                .pattern("XBX")
//                .define('X', ItemTags.PLANKS)
//                .define('D', Items.NETHERITE_PICKAXE)
//                .define('B', Items.BARREL)
//                .define('T', buildTool.get())
//                .unlockedBy("has_items", hasAllOf(buildTool.get(), ModBlocks.blockHutMiner))
//                .save(consumer);
    }

    private static Criterion<?> hasAllOf(ItemLike... items)
    {
        return InventoryChangeTrigger.TriggerInstance.hasItems(items);
    }

    private static Criterion<?> hasAllOf(ItemPredicate... predicates)
    {
        return InventoryChangeTrigger.TriggerInstance.hasItems(predicates);
    }

    private static ResourceLocation append(@NotNull final ResourceLocation base,
                                           @NotNull final String text1,
                                           @NotNull final String text2)
    {
        if (text2.isEmpty()) return base;
        return new ResourceLocation(MOD_ID, base.getPath() + text1 + text2);
    }

    private static ResourceLocation append(@NotNull final ItemLike item,
                                           @NotNull final String text)
    {
        return append(BuiltInRegistries.ITEM.getKey(item.asItem()), "", text);
    }

    /**
     * Standard hut block recipe pattern, using build tool and one unique item surrounded by planks.
     * @param consumer the recipe consumer.
     * @param output   the resulting hut block.
     * @param input    the unique input item.
     */
    private static void registerHutRecipe1(@NotNull final RecipeOutput consumer,
                                           @NotNull final ItemLike output,
                                           @NotNull final ItemLike input)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .pattern("XTX")
                .pattern("XBX")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('B', input)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), input))
                .save(consumer);
    }

    /**
     * Standard hut block recipe pattern, using build tool and one unique tag surrounded by planks.
     * @param consumer the recipe consumer.
     * @param output   the resulting hut block.
     * @param input    the unique input tag.
     */
    private static void registerHutRecipe1(@NotNull final RecipeOutput consumer,
                                           @NotNull final ItemLike output,
                                           @NotNull final TagKey<Item> input)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .pattern("XTX")
                .pattern("XBX")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('B', input)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(
                        ItemPredicate.Builder.item().of(buildTool.get()).build(),
                        ItemPredicate.Builder.item().of(input).build()))
                .save(consumer);
    }

    /**
     * Cheap hut block recipe pattern, using build tool and one unique item surrounded by planks.
     * @param consumer the recipe consumer.
     * @param output   the resulting hut block (produces 2).
     * @param input    the unique input item.
     * @param name     additional suffix for recipe name to avoid colliding with {@link #registerHutRecipe1}.
     */
    private static void registerHutRecipe1x2(@NotNull final RecipeOutput consumer,
                                             @NotNull final ItemLike output,
                                             @NotNull final ItemLike input,
                                             @NotNull final String name)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output, 2)
                .pattern("XTX")
                .pattern("XBX")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('B', input)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), input))
                .save(consumer, append(output, name));
    }

    /**
     * Expensive hut block recipe pattern, using build tool and one unique item (thrice) surrounded by planks.
     * @param consumer the recipe consumer.
     * @param output   the resulting hut block.
     * @param input    the unique input item (used three times).
     */
    private static void registerHutRecipe3(@NotNull final RecipeOutput consumer,
                                           @NotNull final ItemLike output,
                                           @NotNull final ItemLike input)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, output)
                .pattern("XTX")
                .pattern("BBB")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('B', input)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), input))
                .save(consumer);
    }

    private void buildOtherBlocks(@NotNull final RecipeOutput consumer)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockBarrel)
                .pattern("WTW")
                .pattern("WGW")
                .pattern("WSW")
                .define('W', ItemTags.PLANKS)
                .define('S', Items.IRON_INGOT)
                .define('G', Items.DIRT)
                .define('T', buildTool.get())
                .unlockedBy("has_items", hasAllOf(buildTool.get(), Items.IRON_INGOT))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockCompostedDirt)
                .pattern("XXX")
                .pattern("XPX")
                .pattern("XXX")
                .define('X', Items.DIRT)
                .define('P', ModItems.compost)
                .unlockedBy("has_compost", has(ModItems.compost))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockConstructionTape)
                .pattern("SWS")
                .pattern("S S")
                .pattern("S S")
                .define('W', ItemTags.WOOL)
                .define('S', Items.STICK)
                .unlockedBy("has_wool", has(ItemTags.WOOL))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockPostBox)
                .pattern("XSX")
                .pattern("III")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('I', Tags.Items.CHESTS)
                .define('S', buildTool.get())
                .unlockedBy("has_items", hasAllOf(
                        ItemPredicate.Builder.item().of(buildTool.get()).build(),
                        ItemPredicate.Builder.item().of(Tags.Items.CHESTS).build()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockStash)
                .pattern("XSX")
                .pattern("IXI")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('I', Tags.Items.CHESTS)
                .define('S', buildTool.get())
                .unlockedBy("has_items", hasAllOf(
                        ItemPredicate.Builder.item().of(buildTool.get()).build(),
                        ItemPredicate.Builder.item().of(Tags.Items.CHESTS).build()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockRack)
                .pattern("XXX")
                .pattern("ISI")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('I', Items.IRON_NUGGET)
                .define('S', buildTool.get())
                .unlockedBy("has_build_tool", has(buildTool.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.blockWayPoint, 16)
                .pattern("XXX")
                .pattern("XBX")
                .pattern("XXX")
                .define('X', ItemTags.PLANKS)
                .define('B', buildTool.get())
                .unlockedBy("has_build_tool", has(buildTool.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LARGE_FERN)
                .requires(Items.FERN)
                .requires(Items.FERN)
                .unlockedBy("has_fern", has(Items.FERN))
                .save(consumer, new ResourceLocation(MOD_ID, "doublefern"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.TALL_GRASS)
                .requires(Items.GRASS_BLOCK)
                .requires(Items.GRASS_BLOCK)
                .unlockedBy("has_grass", has(Items.GRASS_BLOCK))
                .save(consumer, new ResourceLocation(MOD_ID, "doublegrass"));
    }

    private void buildFood(@NotNull final RecipeOutput consumer)
    {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.breadDough), RecipeCategory.FOOD,
            Items.BREAD, 0.35f, 300)
                .unlockedBy("has_dough", has(ModItems.breadDough))
                .save(consumer, append(new ResourceLocation(MOD_ID, "baked_bread"), "_", ""));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.cakeBatter), RecipeCategory.FOOD,
                Items.CAKE, 0.35f, 300)
                .unlockedBy("has_dough", has(ModItems.cakeBatter))
                .save(consumer, append(new ResourceLocation(MOD_ID, "baked_cake"), "_", ""));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.cookieDough),  RecipeCategory.FOOD,
                Items.COOKIE, 0.0475f, 300 / 8)
                .unlockedBy("has_dough", has(ModItems.cookieDough))
                .save(consumer, append(new ResourceLocation(MOD_ID, "baked_cookies"), "_", ""));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.rawPumpkinPie),  RecipeCategory.FOOD,
                Items.PUMPKIN_PIE, 0.35f, 300)
                .unlockedBy("has_dough", has(ModItems.rawPumpkinPie))
                .save(consumer, append(new ResourceLocation(MOD_ID, "baked_pumpkin_pie"), "_", ""));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.manchet_dough),  RecipeCategory.FOOD,
            ModItems.manchet_bread, 0.35f, 300)
          .unlockedBy("has_dough", has(ModItems.manchet_dough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_manchet_bread"), "_", ""));

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.muffin_dough),  RecipeCategory.FOOD,
            ModItems.muffin, 0.35f, 300)
          .unlockedBy("has_dough", has(ModItems.muffin_dough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_muffin"), "_", ""));

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.breadDough), RecipeCategory.FOOD,
            Items.BREAD, 0.35f, 100)
          .unlockedBy("has_dough", has(ModItems.breadDough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_bread"), "_", "smoking"));

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.cakeBatter), RecipeCategory.FOOD,
            Items.CAKE, 0.35f, 100)
          .unlockedBy("has_dough", has(ModItems.cakeBatter))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_cake"), "_", "smoking"));

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.cookieDough),  RecipeCategory.FOOD,
            Items.COOKIE, 0.0475f, 100 / 8)
          .unlockedBy("has_dough", has(ModItems.cookieDough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_cookies"), "_", "smoking"));

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.rawPumpkinPie),  RecipeCategory.FOOD,
            Items.PUMPKIN_PIE, 0.35f, 100)
          .unlockedBy("has_dough", has(ModItems.rawPumpkinPie))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_pumpkin_pie"), "_", "smoking"));

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.manchet_dough),  RecipeCategory.FOOD,
            ModItems.manchet_bread, 0.35f, 100)
          .unlockedBy("has_dough", has(ModItems.manchet_dough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_manchet_bread"), "_", "smoking"));

        SimpleCookingRecipeBuilder.smoking(Ingredient.of(ModItems.muffin_dough),  RecipeCategory.FOOD,
            ModItems.muffin, 0.35f, 100)
          .unlockedBy("has_dough", has(ModItems.muffin_dough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_muffin"), "_", "smoking"));

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.breadDough), RecipeCategory.FOOD,
            Items.BREAD, 0.35f, 600)
          .unlockedBy("has_dough", has(ModItems.breadDough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_bread"), "_", "campfire_cooking"));

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.cakeBatter), RecipeCategory.FOOD,
            Items.CAKE, 0.35f, 600)
          .unlockedBy("has_dough", has(ModItems.cakeBatter))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_cake"), "_", "campfire_cooking"));

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.cookieDough),  RecipeCategory.FOOD,
            Items.COOKIE, 0.0475f, 600 / 8)
          .unlockedBy("has_dough", has(ModItems.cookieDough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_cookies"), "_", "campfire_cooking"));

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.rawPumpkinPie),  RecipeCategory.FOOD,
            Items.PUMPKIN_PIE, 0.35f, 600)
          .unlockedBy("has_dough", has(ModItems.rawPumpkinPie))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_pumpkin_pie"), "_", "campfire_cooking"));

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.manchet_dough),  RecipeCategory.FOOD,
            ModItems.manchet_bread, 0.35f, 600)
          .unlockedBy("has_dough", has(ModItems.manchet_dough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_manchet_bread"), "_", "campfire_cooking"));

        SimpleCookingRecipeBuilder.campfireCooking(Ingredient.of(ModItems.muffin_dough),  RecipeCategory.FOOD,
            ModItems.muffin, 0.35f, 600)
          .unlockedBy("has_dough", has(ModItems.muffin_dough))
          .save(consumer, append(new ResourceLocation(MOD_ID, "baked_muffin"), "_", "campfire_cooking"));
    }

    private void buildOtherItems(@NotNull final RecipeOutput consumer)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.flagBanner)
                .pattern(" W ")
                .pattern(" W ")
                .pattern(" B ")
                .define('W', ItemTags.WOOL)
                .define('B', buildTool.get())
                .unlockedBy("has_items", hasAllOf(
                        ItemPredicate.Builder.item().of(buildTool.get()).build(),
                        ItemPredicate.Builder.item().of(ItemTags.WOOL).build()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.bannerRallyGuards)
                .pattern("AAA")
                .pattern("BXB")
                .pattern("CCC")
                .define('A', Items.IRON_SWORD)
                .define('B', ModBlocks.blockHutGuardTower)
                .define('X', Items.YELLOW_BANNER)
                .define('C', Items.BOW)
                .unlockedBy("has_items", hasAllOf(buildTool.get(), ModBlocks.blockHutGuardTower))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Items.CHAINMAIL_HELMET)
                .pattern("NNN")
                .pattern("NIN")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(consumer, new ResourceLocation(MOD_ID, "chainmailhelmet"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Items.CHAINMAIL_CHESTPLATE)
                .pattern("I I")
                .pattern("NNN")
                .pattern("NNN")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(consumer, new ResourceLocation(MOD_ID, "chainmailchestplate"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Items.CHAINMAIL_LEGGINGS)
                .pattern("III")
                .pattern("N N")
                .pattern("N N")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(consumer, new ResourceLocation(MOD_ID, "chainmailleggings"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,Items.CHAINMAIL_BOOTS)
                .pattern("I I")
                .pattern("N N")
                .define('I', Items.IRON_INGOT)
                .define('N', Items.IRON_NUGGET)
                .unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(consumer, new ResourceLocation(MOD_ID, "chainmailboots"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.clipboard)
                .pattern("XTX")
                .pattern("XPX")
                .pattern("XXX")
                .define('X', Items.STICK)
                .define('P', Items.LEATHER)
                .define('T', buildTool.get())
                .unlockedBy("has_build_tool", has(buildTool.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.questLog)
          .pattern("XTX")
          .pattern("LPL")
          .pattern("XPX")
          .define('P', Items.PAPER)
          .define('L', Items.LEATHER)
          .define('X', Items.STICK)
          .define('T', buildTool.get())
          .unlockedBy("has_build_tool", has(buildTool.get()))
          .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.resourceScroll)
                .pattern("XTX")
                .pattern("XPX")
                .pattern("XPX")
                .define('X', Items.STICK)
                .define('P', Items.LEATHER)
                .define('T', buildTool.get())
                .unlockedBy("has_build_tool", has(buildTool.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.buildGoggles)
                .pattern("NIN")
                .pattern("GTG")
                .pattern("L L")
                .define('N', Items.GOLD_NUGGET)
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GLASS_PANE)
                .define('L', Items.LEATHER)
                .define('T', buildTool.get())
                .unlockedBy("has_build_tool", has(buildTool.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,shapeTool.get())
                .pattern("  X")
                .pattern(" S ")
                .pattern("S  ")
                .define('X', Items.EMERALD)
                .define('S', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(consumer, new ResourceLocation(MOD_ID, "shapetool"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.supplyCamp)
                .pattern("   ")
                .pattern("C C")
                .pattern("CCC")
                .define('C', Tags.Items.CHESTS)
                .unlockedBy("has_chest", has(Tags.Items.CHESTS))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC,ModItems.supplyChest)
                .pattern("   ")
                .pattern("B B")
                .pattern("BBB")
                .define('B', ItemTags.BOATS)
                .unlockedBy("has_boat", has(ItemTags.BOATS))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.large_empty_bottle, 4)
          .pattern("PSP")
          .pattern("P P")
          .pattern("PGP")
          .define('P', Items.GLASS_PANE)
          .define('G', Items.GLASS)
          .define('S', ItemTags.WOODEN_SLABS)
          .unlockedBy("has_glass", has(Tags.Items.GLASS))
          .save(consumer);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModItems.scimitar), RecipeCategory.MISC, Items.IRON_NUGGET, 0.1f, 200)
                .unlockedBy("has_scimitar", has(ModItems.scimitar))
                .save(consumer, new ResourceLocation(MOD_ID, "iron_nugget_from_iron_scimitar_smelting"));
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(ModItems.scimitar), RecipeCategory.MISC, Items.IRON_NUGGET, 0.1f, 100)
                .unlockedBy("has_scimitar", has(ModItems.scimitar))
                .save(consumer, new ResourceLocation(MOD_ID, "iron_nugget_from_iron_scimitar_blasting"));
    }
}
