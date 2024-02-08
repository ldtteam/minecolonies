package com.minecolonies.core.generation.defaults;

import com.minecolonies.api.advancements.AllTowersTrigger.AllTowersTriggerInstance;
import com.minecolonies.api.advancements.ArmyPopulationTrigger.ArmyPopulationTriggerInstance;
import com.minecolonies.api.advancements.BuildingAddRecipeTrigger.BuildingAddRecipeTriggerInstance;
import com.minecolonies.api.advancements.CitizenBuryTrigger.CitizenBuryTriggerInstance;
import com.minecolonies.api.advancements.CitizenEatFoodTrigger.CitizenEatFoodTriggerInstance;
import com.minecolonies.api.advancements.CitizenResurrectTrigger.CitizenResurrectTriggerInstance;
import com.minecolonies.api.advancements.ClickGuiButtonTrigger.ClickGuiButtonTriggerInstance;
import com.minecolonies.api.advancements.ColonyPopulationTrigger.ColonyPopulationTriggerInstance;
import com.minecolonies.api.advancements.CompleteBuildRequestTrigger.CompleteBuildRequestTriggerInstance;
import com.minecolonies.api.advancements.CreateBuildRequestTrigger.CreateBuildRequestTriggerInstance;
import com.minecolonies.api.advancements.DeepMineTrigger.DeepMineTriggerInstance;
import com.minecolonies.api.advancements.MaxFieldsTrigger.MaxFieldsTriggerInstance;
import com.minecolonies.api.advancements.PlaceStructureTrigger.PlaceStructureTriggerInstance;
import com.minecolonies.api.advancements.PlaceSupplyTrigger.PlaceSupplyTriggerInstance;
import com.minecolonies.api.advancements.UndertakerTotemTrigger.UndertakerTotemTriggerInstance;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.WindowConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for advancements
 */
@SuppressWarnings("unused") // copy-paste issue
public class DefaultAdvancementsProvider extends AdvancementProvider
{
    public static AdvancementGenerator generator = (registries, consumer, fileHelper) -> {
        // todo: the achievement ids are a bit weird, in particular the folder organisation;
        //       at some major MC version update we should probably reorganise them.

        // this is mostly redundant with the standard root, but it lets people see a Minecolonies
        // advancement before that tab is visible...
        Advancement.Builder.advancement()
          .display(ModItems.supplyChest,
            Component.translatable("advancements.minecolonies.root.title"),
            Component.translatable("advancements.minecolonies.root.description"),
            null,
            AdvancementType.TASK, false, false, false)
          .addCriterion("supply_ship", PlaceSupplyTriggerInstance.placeSupply())
          .save(consumer, new ResourceLocation(MOD_ID, "minecraft/craft_supply"), fileHelper);

        addStandardAdvancements(consumer, fileHelper);
        addProductionAdvancements(consumer, fileHelper);
        addMilitaryAdvancements(consumer, fileHelper);
    };

    public DefaultAdvancementsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper)
    {
        super(output, registries, existingFileHelper, List.of(generator));
    }

    private static void addStandardAdvancements(@NotNull final Consumer<AdvancementHolder> consumer,
                                         @NotNull final ExistingFileHelper fileHelper)
    {
        final String GROUP = "minecolonies/";

        final AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.supplyChest,
                        Component.translatable("advancements.minecolonies.root.title"),
                        Component.translatable("advancements.minecolonies.root.description"),
                        new ResourceLocation("textures/block/light_gray_wool.png"),
                        AdvancementType.TASK, true, true, false)
                .addCriterion("supply_ship_placed", PlaceSupplyTriggerInstance.placeSupply())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root"), fileHelper);

        final AdvancementHolder placeTownHall = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutTownHall,"place_townhall"))
                .addCriterion("build_tool_place_town_hall", placeStructure(ModBuildings.townHall.get()))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "place_townhall"), fileHelper);

        final AdvancementHolder startBuilder = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(AdvancementType.TASK, ModBlocks.blockConstructionTape,"create_build_request_1_builder"))
                .addCriterion("builder_request", createBuildRequest(ModBuildings.builder.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "start_builder"), fileHelper);

        final AdvancementHolder fulfillRequest = Advancement.Builder.advancement()
                .parent(startBuilder)
                .display(make(AdvancementType.TASK, ModItems.resourceScroll,"click_gui_button_fulfill"))
                .addCriterion("click_gui_button_fulfill",
                        ClickGuiButtonTriggerInstance.clickGuiButton(WindowConstants.REQUEST_FULLFIL, MOD_ID + WindowConstants.CITIZEN_REQ_RESOURCE_SUFFIX))
                .addCriterion("click_request_button_fulfill",
                        ClickGuiButtonTriggerInstance.clickGuiButton(WindowConstants.REQUEST_FULLFIL, MOD_ID + WindowConstants.CITIZEN_REQ_DETAIL_SUFFIX))
                .requirements(Strategy.OR)
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "fulfill_request"), fileHelper);

        final AdvancementHolder buildBuilder = Advancement.Builder.advancement()
                .parent(fulfillRequest)
                .display(make(AdvancementType.GOAL, ModBlocks.blockHutBuilder,"build.builder"))
                .addCriterion("builders_hut", completeBuildRequest(ModBuildings.builder.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_builder"), fileHelper);

        final AdvancementHolder buildBuilder2 = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(AdvancementType.GOAL, ModBlocks.blockHutBuilder, "complete_build_request_2_builder"))
                .addCriterion("builders_hut", completeBuildRequest(ModBuildings.builder.get(), 2))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_builder_2"), fileHelper);

        final AdvancementHolder buildGuardTower = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutGuardTower, "build.guard_tower"))
                .addCriterion("guard_tower", completeBuildRequest(ModBuildings.guardTower.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_guard_tower"), fileHelper);

        final AdvancementHolder buildMysticalSite = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutMysticalSite, "build.mysticalsite"))
                .addCriterion("mysticalsite", completeBuildRequest(ModBuildings.mysticalSite.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_mysticalsite"), fileHelper);

        final AdvancementHolder buildMysticalSite5 = Advancement.Builder.advancement()
                .parent(buildMysticalSite)
                .display(make(AdvancementType.CHALLENGE, ModBlocks.blockHutMysticalSite, "build.mysticalsite_5"))
                .addCriterion("mysticalsite", completeBuildRequest(ModBuildings.mysticalSite.get(), 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_mysticalsite_5"), fileHelper);

        final AdvancementHolder buildTavern = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutTavern, "build.tavern"))
                .addCriterion("tavern", completeBuildRequest(ModBuildings.tavern.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_tavern"), fileHelper);

        final AdvancementHolder buildCitizen = Advancement.Builder.advancement()
                .parent(buildTavern)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutHome, "build.citizen"))
                .addCriterion("citizens_hut", completeBuildRequest(ModBuildings.home.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_citizen"), fileHelper);

        final AdvancementHolder buildTownHall = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutTownHall, "build.town_hall"))
                .addCriterion("town_hall", completeBuildRequest(ModBuildings.townHall.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_town_hall"), fileHelper);

        final AdvancementHolder buildTownHall5 = Advancement.Builder.advancement()
                .parent(buildTownHall)
                .display(make(AdvancementType.CHALLENGE, ModBlocks.blockHutTownHall, "complete_build_request_5_town_hall"))
                .addCriterion("town_hall", completeBuildRequest(ModBuildings.townHall.get(), 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_town_hall_5"), fileHelper);

        final AdvancementHolder buildingAddRecipeTorch = Advancement.Builder.advancement()
                .parent(startBuilder)
                .display(make(AdvancementType.TASK, Items.TORCH, "building_add_recipe_torch"))
                .addCriterion("add_recipe_torch",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.TORCH), 2))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "building_add_recipe_torch"), fileHelper);

        final AdvancementHolder checkOutGuide = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutBuilder, "check_out_guide"))
                .addCriterion("click_gui_button_close",
                        ClickGuiButtonTriggerInstance.clickGuiButton(WindowConstants.GUIDE_CONFIRM, MOD_ID + WindowConstants.GUIDE_RESOURCE_SUFFIX))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "check_out_guide"), fileHelper);

        final AdvancementHolder citizenEatFood = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(AdvancementType.TASK, Items.COOKED_BEEF, "citizen_eat_food_first"))
                .addCriterion("citizen_eat_anything", CitizenEatFoodTriggerInstance.citizenEatFood())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_eat_food_first"), fileHelper);

        final AdvancementHolder citizenEatFoodRottenFlesh = Advancement.Builder.advancement()
                .parent(citizenEatFood)
                .display(makeHidden(AdvancementType.TASK, Items.ROTTEN_FLESH, "citizen_eat_food_rotten_flesh"))
                .addCriterion("citizen_eat_rotten_flesh",
                        CitizenEatFoodTriggerInstance.citizenEatFood(item(Items.ROTTEN_FLESH)))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_eat_food_rotten_flesh"), fileHelper);

        final AdvancementHolder colonyPopulation5 = Advancement.Builder.advancement()
                .parent(buildCitizen)
                .display(make(AdvancementType.GOAL, Items.RED_BED, "colony_population_5"))
                .addCriterion("population_5", ColonyPopulationTriggerInstance.colonyPopulation(5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_5"), fileHelper);

        final AdvancementHolder colonyPopulation10 = Advancement.Builder.advancement()
                .parent(colonyPopulation5)
                .display(make(AdvancementType.TASK, Items.BRICK, "colony_population_10"))
                .addCriterion("population_10", ColonyPopulationTriggerInstance.colonyPopulation(10))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_10"), fileHelper);

        final AdvancementHolder colonyPopulation25 = Advancement.Builder.advancement()
                .parent(colonyPopulation10)
                .display(make(AdvancementType.GOAL, Items.GOLD_INGOT, "colony_population_25"))
                .addCriterion("population_25", ColonyPopulationTriggerInstance.colonyPopulation(25))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_25"), fileHelper);

        final AdvancementHolder colonyPopulation50 = Advancement.Builder.advancement()
                .parent(colonyPopulation25)
                .display(make(AdvancementType.GOAL, Items.DIAMOND, "colony_population_50"))
                .addCriterion("population_50", ColonyPopulationTriggerInstance.colonyPopulation(50))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_50"), fileHelper);

        final AdvancementHolder colonyPopulation75 = Advancement.Builder.advancement()
                .parent(colonyPopulation50)
                .display(make(AdvancementType.CHALLENGE, Items.EMERALD, "colony_population_75"))
                .addCriterion("population_75", ColonyPopulationTriggerInstance.colonyPopulation(75))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_75"), fileHelper);

        final AdvancementHolder citizenBury = Advancement.Builder.advancement()
                .parent(buildTavern)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutGraveyard, "citizen.bury"))
                .addCriterion("buried", CitizenBuryTriggerInstance.citizenBury())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_bury"), fileHelper);

        final AdvancementHolder citizenResurrect = Advancement.Builder.advancement()
                .parent(citizenBury)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutGraveyard, "citizen.resurrect"))
                .addCriterion("resurrected", CitizenResurrectTriggerInstance.citizenResurrect())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_resurrect"), fileHelper);

        final AdvancementHolder undertakerTotem = Advancement.Builder.advancement()
                .parent(citizenResurrect)
                .display(make(AdvancementType.TASK, Items.TOTEM_OF_UNDYING, "undertaker.totem"))
                .addCriterion("totem", UndertakerTotemTriggerInstance.undertakerTotem())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "undertaker_totem"), fileHelper);
    }

    private static void addProductionAdvancements(@NotNull final Consumer<AdvancementHolder> consumer,
                                           @NotNull final ExistingFileHelper fileHelper)
    {
        final String GROUP = "production/";

        final AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModBlocks.blockHutBuilder,
                        Component.translatable("advancements.minecolonies.root.production.title"),
                        Component.translatable("advancements.minecolonies.root.production.description"),
                        new ResourceLocation("structurize:textures/block/cactus/cactus_planks.png"),
                        AdvancementType.TASK, false, false, false)
                .addCriterion("builders_hut", completeBuildRequest(ModBuildings.builder.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root"), fileHelper);

        // --- food ---

        final AdvancementHolder buildCook = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutCook, "build.cook"))
                .addCriterion("cook", completeBuildRequest(ModBuildings.cook.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_cook"), fileHelper);

        final AdvancementHolder buildBaker = Advancement.Builder.advancement()
                .parent(buildCook)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutBaker, "build.baker"))
                .addCriterion("bakery", completeBuildRequest(ModBuildings.bakery.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_baker"), fileHelper);

        // --- logistics ---

        final AdvancementHolder buildWarehouse = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutWareHouse, "build.warehouse"))
                .addCriterion("warehouse", completeBuildRequest(ModBuildings.wareHouse.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_warehouse"), fileHelper);

        final AdvancementHolder buildDeliveryPerson = Advancement.Builder.advancement()
                .parent(buildWarehouse)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutDeliveryman, "build.delivery_person"))
                .addCriterion("delivery_person", completeBuildRequest(ModBuildings.deliveryman.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_delivery_person"), fileHelper);

        final AdvancementHolder postAndStash = Advancement.Builder.advancement()
                .parent(buildDeliveryPerson)
                .display(make(AdvancementType.TASK, ModBlocks.blockPostBox, "post_and_stash"))
                .addCriterion("postbox", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.blockPostBox))
                .addCriterion("stash", ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(ModBlocks.blockStash))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "post_and_stash"), fileHelper);

        // --- education ---

        final AdvancementHolder buildLibrary = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutLibrary, "build.library"))
                .addCriterion("library", completeBuildRequest(ModBuildings.library.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_library"), fileHelper);

        final AdvancementHolder buildEnchanter = Advancement.Builder.advancement()
                .parent(buildLibrary)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutEnchanter, "build.enchanter"))
                .addCriterion("enchanter", completeBuildRequest(ModBuildings.enchanter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_enchanter"), fileHelper);

        // --- crafting ---

        final AdvancementHolder buildSawmill = Advancement.Builder.advancement()
                .parent(buildDeliveryPerson)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutSawmill, "build.sawmill"))
                .addCriterion("sawmill", completeBuildRequest(ModBuildings.sawmill.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_sawmill"), fileHelper);

        final AdvancementHolder buildBlacksmith = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutBlacksmith, "build.blacksmith"))
                .addCriterion("blacksmith", completeBuildRequest(ModBuildings.blacksmith.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_blacksmith"), fileHelper);

        final AdvancementHolder buildSmeltery = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery"))
                .addCriterion("smeltery", completeBuildRequest(ModBuildings.smeltery.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery"), fileHelper);

        final AdvancementHolder buildSmeltery3 = Advancement.Builder.advancement()
                .parent(buildSmeltery)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery_3"))
                .addCriterion("smeltery", completeBuildRequest(ModBuildings.smeltery.get(), 3))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery_3"), fileHelper);

        final AdvancementHolder buildSmeltery5 = Advancement.Builder.advancement()
                .parent(buildSmeltery3)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery_5"))
                .addCriterion("smeltery", completeBuildRequest(ModBuildings.smeltery.get(), 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery_5"), fileHelper);

        final AdvancementHolder buildStoneSmeltery = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutStoneSmeltery, "build.stone_smeltery"))
                .addCriterion("stone_smeltery", completeBuildRequest(ModBuildings.stoneSmelter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_stone_smeltery"), fileHelper);

        final AdvancementHolder buildStoneMason = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutStonemason, "build.stonemason"))
                .addCriterion("stonemason", completeBuildRequest(ModBuildings.stoneMason.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_stonemason"), fileHelper);

        final AdvancementHolder buildCrusher = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutCrusher, "build.crusher"))
                .addCriterion("crusher", completeBuildRequest(ModBuildings.crusher.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_crusher"), fileHelper);

        final AdvancementHolder buildSifter = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutSifter, "build.sifter"))
                .addCriterion("sifter", completeBuildRequest(ModBuildings.sifter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_sifter"), fileHelper);

        final AdvancementHolder buildingAddRecipeDoor = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(AdvancementType.TASK, Items.OAK_DOOR, "building_add_recipe_door"))
                .addCriterion("add_recipe_oak_door",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.OAK_DOOR), 3))
                .addCriterion("add_recipe_birch_door",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.BIRCH_DOOR), 3))
                .addCriterion("add_recipe_dark_oak_door",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.DARK_OAK_DOOR), 3))
                .addCriterion("add_recipe_spruce_door",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.SPRUCE_DOOR), 3))
                .addCriterion("add_recipe_jungle_door",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.JUNGLE_DOOR), 3))
                .addCriterion("add_recipe_acacia_door",
                        BuildingAddRecipeTriggerInstance.buildingAddRecipe(item(Items.ACACIA_DOOR), 3))
                .save(consumer, new ResourceLocation(MOD_ID, "minecolonies/building_add_recipe_door"), fileHelper);

        // --- farming ---

        final AdvancementHolder buildFarmer = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutFarmer, "build.farmer"))
                .addCriterion("farmer", completeBuildRequest(ModBuildings.farmer.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_farmer"), fileHelper);

        final AdvancementHolder maxFields = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(AdvancementType.TASK, ModBlocks.blockScarecrow, "max_fields"))
                .addCriterion("fields", MaxFieldsTriggerInstance.maxFields())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "max_fields"), fileHelper);

        final AdvancementHolder buildFisherman = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutFisherman, "build.fisherman"))
                .addCriterion("fisherman", completeBuildRequest(ModBuildings.fisherman.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_fisherman"), fileHelper);

        final AdvancementHolder buildLumberjack = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutLumberjack, "build.lumberjack"))
                .addCriterion("lumberjack", completeBuildRequest(ModBuildings.lumberjack.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_lumberjack"), fileHelper);

        final AdvancementHolder buildMiner = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutMiner, "build.miner"))
                .addCriterion("miner", completeBuildRequest(ModBuildings.miner.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_miner"), fileHelper);

        final AdvancementHolder deepMine = Advancement.Builder.advancement()
                .parent(buildMiner)
                .display(make(AdvancementType.TASK, Items.BEDROCK, "deep_mine"))
                .addCriterion("mineshaft", DeepMineTriggerInstance.deepMine())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "deep_mine"), fileHelper);

        final AdvancementHolder buildComposter = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutComposter, "build.composter"))
                .addCriterion("composter", completeBuildRequest(ModBuildings.composter.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_composter"), fileHelper);

        final AdvancementHolder buildFlorist = Advancement.Builder.advancement()
                .parent(buildComposter)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutFlorist, "build.florist"))
                .addCriterion("florist", completeBuildRequest(ModBuildings.florist.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_florist"), fileHelper);

        final AdvancementHolder buildAllHerders = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(AdvancementType.GOAL, ModBlocks.blockHutShepherd, "build.herders"))
                .addCriterion("sheep", completeBuildRequest(ModBuildings.shepherd.get(), 1))
                .addCriterion("cows", completeBuildRequest(ModBuildings.cowboy.get(), 1))
                .addCriterion("chickens", completeBuildRequest(ModBuildings.chickenHerder.get(), 1))
                .addCriterion("pigs", completeBuildRequest(ModBuildings.swineHerder.get(), 1))
                .addCriterion("rabbits", completeBuildRequest(ModBuildings.rabbitHutch.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_all_herders"), fileHelper);
    }

    private static void addMilitaryAdvancements(@NotNull final Consumer<AdvancementHolder> consumer,
                                         @NotNull final ExistingFileHelper fileHelper)
    {
        final String GROUP = "military/";

        final AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModBlocks.blockHutBarracks,
                        Component.translatable("advancements.minecolonies.root.military.title"),
                        Component.translatable("advancements.minecolonies.root.military.description"),
                        new ResourceLocation("textures/block/stone_bricks.png"),
                        AdvancementType.TASK, true, false, false)
                .addCriterion("guardtower", completeBuildRequest(ModBuildings.guardTower.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root"), fileHelper);

        final AdvancementHolder buildArchery = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutArchery, "build.archery"))
                .addCriterion("archery", completeBuildRequest(ModBuildings.archery.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_archery"), fileHelper);

        final AdvancementHolder buildCombatAcademy = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutCombatAcademy, "build.combat_academy"))
                .addCriterion("combat_academy", completeBuildRequest(ModBuildings.combatAcademy.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_combat_academy"), fileHelper);

        final AdvancementHolder buildBarracks = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutBarracks, "build.barracks"))
                .addCriterion("barracks", completeBuildRequest(ModBuildings.barracks.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_barracks"), fileHelper);

        final AdvancementHolder buildBarracksTower = Advancement.Builder.advancement()
                .parent(buildBarracks)
                .display(make(AdvancementType.TASK, ModBlocks.blockHutBarracksTower, "build.barracks_tower"))
                .addCriterion("barracks_tower", completeBuildRequest(ModBuildings.barracksTower.get(), 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_barracks_tower"), fileHelper);

        final AdvancementHolder buildAllBarracksTowers = Advancement.Builder.advancement()
                .parent(buildBarracksTower)
                .display(make(AdvancementType.GOAL, ModBlocks.blockHutBarracksTower, "build.all_towers"))
                .addCriterion("towers", AllTowersTriggerInstance.allTowers())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_all_barracks_towers"), fileHelper);

        final AdvancementHolder army8 = Advancement.Builder.advancement()
                .parent(root)
                .display(make(AdvancementType.TASK, Items.STONE_SWORD, "army_8"))
                .addCriterion("population_5", ArmyPopulationTriggerInstance.armyPopulation(8))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_8"), fileHelper);

        final AdvancementHolder army30 = Advancement.Builder.advancement()
                .parent(army8)
                .display(make(AdvancementType.TASK, Items.IRON_SWORD, "army_30"))
                .addCriterion("population_5", ArmyPopulationTriggerInstance.armyPopulation(30))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_30"), fileHelper);

        final AdvancementHolder army80 = Advancement.Builder.advancement()
                .parent(army30)
                .display(make(AdvancementType.CHALLENGE, Items.DIAMOND_SWORD, "army_80"))
                .addCriterion("population_5", ArmyPopulationTriggerInstance.armyPopulation(80))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_80"), fileHelper);
    }

    private static DisplayInfo make(@NotNull final AdvancementType frame,
                                    @NotNull final ItemLike icon,
                                    @NotNull final String name)
    {
        return new DisplayInfo(new ItemStack(icon),
                Component.translatable("advancements.minecolonies." + name + ".title"),
                Component.translatable("advancements.minecolonies." + name + ".description"),
                null, frame, true, true, false);
    }

    private static DisplayInfo makeHidden(@NotNull final AdvancementType frame,
                                          @NotNull final ItemLike icon,
                                          @NotNull final String name)
    {
        return new DisplayInfo(new ItemStack(icon),
                Component.translatable("advancements.minecolonies." + name + ".title"),
                Component.translatable("advancements.minecolonies." + name + ".description"),
                null, frame, true, true, true);
    }

    private static List<ItemPredicate> item(@NotNull final ItemLike item)
    {
        return List.of(ItemPredicate.Builder.item().of(item).build());
    }

    @NotNull
    private static Criterion<PlaceStructureTriggerInstance> placeStructure(@NotNull final BuildingEntry building)
    {
        return PlaceStructureTriggerInstance.placeStructure(building.getBuildingBlock().getBlueprintName());
    }

    @NotNull
    private static Criterion<CreateBuildRequestTriggerInstance> createBuildRequest(@NotNull final BuildingEntry building, final int level)
    {
        return CreateBuildRequestTriggerInstance.createBuildRequest(building.getBuildingBlock().getBlueprintName(), level);
    }

    @NotNull
    private static Criterion<CompleteBuildRequestTriggerInstance> completeBuildRequest(@NotNull final BuildingEntry building, final int level)
    {
        return CompleteBuildRequestTriggerInstance.completeBuildRequest(building.getBuildingBlock().getBlueprintName(), level);
    }
}
