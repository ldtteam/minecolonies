package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.advancements.all_towers.AllTowersCriterionInstance;
import com.minecolonies.api.advancements.army_population.ArmyPopulationCriterionInstance;
import com.minecolonies.api.advancements.building_add_recipe.BuildingAddRecipeCriterionInstance;
import com.minecolonies.api.advancements.citizen_bury.CitizenBuryCriterionInstance;
import com.minecolonies.api.advancements.citizen_eat_food.CitizenEatFoodCriterionInstance;
import com.minecolonies.api.advancements.citizen_resurrect.CitizenResurrectCriterionInstance;
import com.minecolonies.api.advancements.click_gui_button.ClickGuiButtonCriterionInstance;
import com.minecolonies.api.advancements.colony_population.ColonyPopulationCriterionInstance;
import com.minecolonies.api.advancements.complete_build_request.CompleteBuildRequestCriterionInstance;
import com.minecolonies.api.advancements.create_build_request.CreateBuildRequestCriterionInstance;
import com.minecolonies.api.advancements.deep_mine.DeepMineCriterionInstance;
import com.minecolonies.api.advancements.max_fields.MaxFieldsCriterionInstance;
import com.minecolonies.api.advancements.place_structure.PlaceStructureCriterionInstance;
import com.minecolonies.api.advancements.place_supply.PlaceSupplyCriterionInstance;
import com.minecolonies.api.advancements.undertaker_totem.UndertakerTotemCriterionInstance;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.WindowConstants;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.Constants.MOD_ID;

/**
 * Datagen for advancements
 */
public class DefaultAdvancementsProvider extends AdvancementProvider
{
    public DefaultAdvancementsProvider(@NotNull final DataGenerator generatorIn,
                                       @NotNull final ExistingFileHelper fileHelperIn)
    {
        super(generatorIn, fileHelperIn);
    }

    @Override
    public String getName()
    {
        return "DefaultAdvancementsProvider";
    }

    @Override
    protected void registerAdvancements(@NotNull final Consumer<Advancement> consumer,
                                        @NotNull final ExistingFileHelper fileHelper)
    {
        // todo: the achievement ids are a bit weird, in particular the folder organisation;
        //       at some major MC version update we should probably reorganise them.

        // this is mostly redundant with the standard root, but it lets people see a Minecolonies
        // advancement before that tab is visible...
        Advancement.Builder.advancement()
                .parent(new ResourceLocation("story/root"))
                .display(ModItems.supplyChest,
                        new TranslatableComponent("advancements.minecolonies.root.title"),
                        new TranslatableComponent("advancements.minecolonies.root.description"),
                        null,
                        FrameType.TASK, false, false, false)
                .addCriterion("supply_ship", new PlaceSupplyCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, "minecraft/craft_supply"), fileHelper);

        addStandardAdvancements(consumer, fileHelper);
        addProductionAdvancements(consumer, fileHelper);
        addMilitaryAdvancements(consumer, fileHelper);
    }

    private void addStandardAdvancements(@NotNull final Consumer<Advancement> consumer,
                                         @NotNull final ExistingFileHelper fileHelper)
    {
        final String GROUP = "minecolonies/";

        final Advancement root = Advancement.Builder.advancement()
                .display(ModItems.supplyChest,
                        new TranslatableComponent("advancements.minecolonies.root.title"),
                        new TranslatableComponent("advancements.minecolonies.root.description"),
                        new ResourceLocation("textures/block/light_gray_wool.png"),
                        FrameType.TASK, true, true, false)
                .addCriterion("supply_ship_placed", new PlaceSupplyCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root"), fileHelper);

        final Advancement placeTownHall = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutTownHall,"place_townhall"))
                .addCriterion("build_tool_place_town_hall",
                        new PlaceStructureCriterionInstance(ModBuildings.TOWNHALL_ID))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "place_townhall"), fileHelper);

        final Advancement startBuilder = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(FrameType.TASK, ModBlocks.blockConstructionTape,"create_build_request_1_builder"))
                .addCriterion("builder_request",
                        new CreateBuildRequestCriterionInstance(ModBuildings.BUILDER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "start_builder"), fileHelper);

        final Advancement fulfillRequest = Advancement.Builder.advancement()
                .parent(startBuilder)
                .display(make(FrameType.TASK, ModItems.resourceScroll,"click_gui_button_fulfill"))
                .addCriterion("click_gui_button_fulfill",
                        new ClickGuiButtonCriterionInstance(WindowConstants.REQUEST_FULLFIL, MOD_ID + WindowConstants.CITIZEN_REQ_RESOURCE_SUFFIX))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "fulfill_request"), fileHelper);

        final Advancement buildBuilder = Advancement.Builder.advancement()
                .parent(fulfillRequest)
                .display(make(FrameType.GOAL, ModBlocks.blockHutBuilder,"build.builder"))
                .addCriterion("builders_hut",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BUILDER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_builder"), fileHelper);

        final Advancement buildBuilder2 = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.GOAL, ModBlocks.blockHutBuilder, "complete_build_request_2_builder"))
                .addCriterion("builders_hut",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BUILDER_ID, 2))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_builder_2"), fileHelper);

        final Advancement buildGuardTower = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutGuardTower, "build.guard_tower"))
                .addCriterion("guard_tower",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.GUARD_TOWER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_guard_tower"), fileHelper);

        final Advancement buildMysticalSite = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutMysticalSite, "build.mysticalsite"))
                .addCriterion("mysticalsite",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.MYSTICAL_SITE_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_mysticalsite"), fileHelper);

        final Advancement buildMysticalSite5 = Advancement.Builder.advancement()
                .parent(buildMysticalSite)
                .display(make(FrameType.CHALLENGE, ModBlocks.blockHutMysticalSite, "build.mysticalsite_5"))
                .addCriterion("mysticalsite",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.MYSTICAL_SITE_ID, 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_mysticalsite_5"), fileHelper);

        final Advancement buildTavern = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutTavern, "build.tavern"))
                .addCriterion("tavern",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.TAVERN_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_tavern"), fileHelper);

        final Advancement buildCitizen = Advancement.Builder.advancement()
                .parent(buildTavern)
                .display(make(FrameType.TASK, ModBlocks.blockHutHome, "build.citizen"))
                .addCriterion("citizens_hut",
                        new CompleteBuildRequestCriterionInstance("citizen", 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_citizen"), fileHelper);

        final Advancement buildTownHall = Advancement.Builder.advancement()
                .parent(buildBuilder)
                .display(make(FrameType.TASK, ModBlocks.blockHutTownHall, "build.town_hall"))
                .addCriterion("town_hall",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.TOWNHALL_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_town_hall"), fileHelper);

        final Advancement buildTownHall5 = Advancement.Builder.advancement()
                .parent(buildTownHall)
                .display(make(FrameType.CHALLENGE, ModBlocks.blockHutTownHall, "complete_build_request_5_town_hall"))
                .addCriterion("town_hall",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.TOWNHALL_ID, 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_town_hall_5"), fileHelper);

        final Advancement buildingAddRecipeTorch = Advancement.Builder.advancement()
                .parent(startBuilder)
                .display(make(FrameType.TASK, Items.TORCH, "building_add_recipe_torch"))
                .addCriterion("add_recipe_torch",
                        new BuildingAddRecipeCriterionInstance(item(Items.TORCH), 2))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "building_add_recipe_torch"), fileHelper);

        final Advancement checkOutGuide = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(FrameType.TASK, ModBlocks.blockHutBuilder, "check_out_guide"))
                .addCriterion("click_gui_button_close",
                        new ClickGuiButtonCriterionInstance(WindowConstants.GUIDE_CONFIRM, MOD_ID + WindowConstants.GUIDE_RESOURCE_SUFFIX))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "check_out_guide"), fileHelper);

        final Advancement citizenEatFood = Advancement.Builder.advancement()
                .parent(placeTownHall)
                .display(make(FrameType.TASK, Items.COOKED_BEEF, "citizen_eat_food_first"))
                .addCriterion("citizen_eat_anything", new CitizenEatFoodCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_eat_food_first"), fileHelper);

        final Advancement citizenEatFoodRottenFlesh = Advancement.Builder.advancement()
                .parent(citizenEatFood)
                .display(makeHidden(FrameType.TASK, Items.ROTTEN_FLESH, "citizen_eat_food_rotten_flesh"))
                .addCriterion("citizen_eat_rotten_flesh",
                        new CitizenEatFoodCriterionInstance(item(Items.ROTTEN_FLESH)))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_eat_food_rotten_flesh"), fileHelper);

        final Advancement colonyPopulation5 = Advancement.Builder.advancement()
                .parent(buildCitizen)
                .display(make(FrameType.GOAL, Items.RED_BED, "colony_population_5"))
                .addCriterion("population_5", new ColonyPopulationCriterionInstance(5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_5"), fileHelper);

        final Advancement colonyPopulation10 = Advancement.Builder.advancement()
                .parent(colonyPopulation5)
                .display(make(FrameType.TASK, Items.BRICK, "colony_population_10"))
                .addCriterion("population_10", new ColonyPopulationCriterionInstance(10))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_10"), fileHelper);

        final Advancement colonyPopulation25 = Advancement.Builder.advancement()
                .parent(colonyPopulation10)
                .display(make(FrameType.GOAL, Items.GOLD_INGOT, "colony_population_25"))
                .addCriterion("population_25", new ColonyPopulationCriterionInstance(25))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_25"), fileHelper);

        final Advancement colonyPopulation50 = Advancement.Builder.advancement()
                .parent(colonyPopulation25)
                .display(make(FrameType.GOAL, Items.DIAMOND, "colony_population_50"))
                .addCriterion("population_50", new ColonyPopulationCriterionInstance(50))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_50"), fileHelper);

        final Advancement colonyPopulation75 = Advancement.Builder.advancement()
                .parent(colonyPopulation50)
                .display(make(FrameType.CHALLENGE, Items.EMERALD, "colony_population_75"))
                .addCriterion("population_75", new ColonyPopulationCriterionInstance(75))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "colony_population_75"), fileHelper);

        final Advancement citizenBury = Advancement.Builder.advancement()
                .parent(buildTavern)
                .display(make(FrameType.TASK, ModBlocks.blockHutGraveyard, "citizen.bury"))
                .addCriterion("buried", new CitizenBuryCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_bury"), fileHelper);

        final Advancement citizenResurrect = Advancement.Builder.advancement()
                .parent(citizenBury)
                .display(make(FrameType.TASK, ModBlocks.blockHutGraveyard, "citizen.resurrect"))
                .addCriterion("resurrected", new CitizenResurrectCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "citizen_resurrect"), fileHelper);

        final Advancement undertakerTotem = Advancement.Builder.advancement()
                .parent(citizenResurrect)
                .display(make(FrameType.TASK, Items.TOTEM_OF_UNDYING, "undertaker.totem"))
                .addCriterion("totem", new UndertakerTotemCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "undertaker_totem"), fileHelper);
    }

    private void addProductionAdvancements(@NotNull final Consumer<Advancement> consumer,
                                           @NotNull final ExistingFileHelper fileHelper)
    {
        final String GROUP = "production/";

        final Advancement root = Advancement.Builder.advancement()
                .display(ModBlocks.blockHutBuilder,
                        new TranslatableComponent("advancements.minecolonies.root.production.title"),
                        new TranslatableComponent("advancements.minecolonies.root.production.description"),
                        new ResourceLocation("structurize:textures/blocks/cactus/cactus_planks.png"),
                        FrameType.TASK, false, false, false)
                .addCriterion("builders_hut",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BUILDER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root"), fileHelper);

        // --- food ---

        final Advancement buildCook = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutCook, "build.cook"))
                .addCriterion("cook",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.COOK_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_cook"), fileHelper);

        final Advancement buildBaker = Advancement.Builder.advancement()
                .parent(buildCook)
                .display(make(FrameType.TASK, ModBlocks.blockHutBaker, "build.baker"))
                .addCriterion("bakery",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BAKERY_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_baker"), fileHelper);

        // --- logistics ---

        final Advancement buildWarehouse = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutWareHouse, "build.warehouse"))
                .addCriterion("warehouse",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.WAREHOUSE_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_warehouse"), fileHelper);

        final Advancement buildDeliveryPerson = Advancement.Builder.advancement()
                .parent(buildWarehouse)
                .display(make(FrameType.TASK, ModBlocks.blockHutDeliveryman, "build.delivery_person"))
                .addCriterion("delivery_person",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.DELIVERYMAN_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_delivery_person"), fileHelper);

        final Advancement postAndStash = Advancement.Builder.advancement()
                .parent(buildDeliveryPerson)
                .display(make(FrameType.TASK, ModBlocks.blockPostBox, "post_and_stash"))
                .addCriterion("postbox", PlacedBlockTrigger.TriggerInstance.placedBlock(ModBlocks.blockPostBox))
                .addCriterion("stash", PlacedBlockTrigger.TriggerInstance.placedBlock(ModBlocks.blockStash))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "post_and_stash"), fileHelper);

        // --- education ---

        final Advancement buildLibrary = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutLibrary, "build.library"))
                .addCriterion("library",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.LIBRARY_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_library"), fileHelper);

        final Advancement buildEnchanter = Advancement.Builder.advancement()
                .parent(buildLibrary)
                .display(make(FrameType.TASK, ModBlocks.blockHutEnchanter, "build.enchanter"))
                .addCriterion("enchanter",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.ENCHANTER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_enchanter"), fileHelper);

        // --- crafting ---

        final Advancement buildSawmill = Advancement.Builder.advancement()
                .parent(buildDeliveryPerson)
                .display(make(FrameType.TASK, ModBlocks.blockHutSawmill, "build.sawmill"))
                .addCriterion("sawmill",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SAWMILL_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_sawmill"), fileHelper);

        final Advancement buildBlacksmith = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutBlacksmith, "build.blacksmith"))
                .addCriterion("blacksmith",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BLACKSMITH_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_blacksmith"), fileHelper);

        final Advancement buildSmeltery = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery"))
                .addCriterion("smeltery",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SMELTERY_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery"), fileHelper);

        final Advancement buildSmeltery3 = Advancement.Builder.advancement()
                .parent(buildSmeltery)
                .display(make(FrameType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery_3"))
                .addCriterion("smeltery",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SMELTERY_ID, 3))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery_3"), fileHelper);

        final Advancement buildSmeltery5 = Advancement.Builder.advancement()
                .parent(buildSmeltery3)
                .display(make(FrameType.TASK, ModBlocks.blockHutSmeltery, "build.smeltery_5"))
                .addCriterion("smeltery",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SMELTERY_ID, 5))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_smeltery_5"), fileHelper);

        final Advancement buildStoneSmeltery = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutStoneSmeltery, "build.stone_smeltery"))
                .addCriterion("stone_smeltery",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.STONE_SMELTERY_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_stone_smeltery"), fileHelper);

        final Advancement buildStoneMason = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutStonemason, "build.stonemason"))
                .addCriterion("stonemason",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.STONE_MASON_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_stonemason"), fileHelper);

        final Advancement buildCrusher = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutCrusher, "build.crusher"))
                .addCriterion("crusher",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.CRUSHER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_crusher"), fileHelper);

        final Advancement buildSifter = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, ModBlocks.blockHutSifter, "build.sifter"))
                .addCriterion("sifter",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SIFTER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_sifter"), fileHelper);

        final Advancement buildingAddRecipeDoor = Advancement.Builder.advancement()
                .parent(buildSawmill)
                .display(make(FrameType.TASK, Items.OAK_DOOR, "building_add_recipe_door"))
                .addCriterion("add_recipe_oak_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.OAK_DOOR), 3))
                .addCriterion("add_recipe_birch_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.BIRCH_DOOR), 3))
                .addCriterion("add_recipe_dark_oak_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.DARK_OAK_DOOR), 3))
                .addCriterion("add_recipe_spruce_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.SPRUCE_DOOR), 3))
                .addCriterion("add_recipe_jungle_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.JUNGLE_DOOR), 3))
                .addCriterion("add_recipe_acacia_door",
                        new BuildingAddRecipeCriterionInstance(item(Items.ACACIA_DOOR), 3))
                .save(consumer, new ResourceLocation(MOD_ID, "minecolonies/building_add_recipe_door"), fileHelper);

        // --- farming ---

        final Advancement buildFarmer = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutFarmer, "build.farmer"))
                .addCriterion("farmer",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.FARMER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_farmer"), fileHelper);

        final Advancement maxFields = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(FrameType.TASK, ModBlocks.blockScarecrow, "max_fields"))
                .addCriterion("fields", new MaxFieldsCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "max_fields"), fileHelper);

        final Advancement buildFisherman = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutFisherman, "build.fisherman"))
                .addCriterion("fisherman",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.FISHERMAN_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_fisherman"), fileHelper);

        final Advancement buildLumberjack = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutLumberjack, "build.lumberjack"))
                .addCriterion("lumberjack",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.LUMBERJACK_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_lumberjack"), fileHelper);

        final Advancement buildMiner = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutMiner, "build.miner"))
                .addCriterion("miner",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.MINER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_miner"), fileHelper);

        final Advancement deepMine = Advancement.Builder.advancement()
                .parent(buildMiner)
                .display(make(FrameType.TASK, Items.BEDROCK, "deep_mine"))
                .addCriterion("mineshaft", new DeepMineCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "deep_mine"), fileHelper);

        final Advancement buildComposter = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(FrameType.TASK, ModBlocks.blockHutComposter, "build.composter"))
                .addCriterion("composter",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.COMPOSTER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_composter"), fileHelper);

        final Advancement buildFlorist = Advancement.Builder.advancement()
                .parent(buildComposter)
                .display(make(FrameType.TASK, ModBlocks.blockHutFlorist, "build.florist"))
                .addCriterion("florist",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.FLORIST_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_florist"), fileHelper);

        final Advancement buildAllHerders = Advancement.Builder.advancement()
                .parent(buildFarmer)
                .display(make(FrameType.GOAL, ModBlocks.blockHutShepherd, "build.herders"))
                .addCriterion("sheep",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SHEPHERD_ID, 1))
                .addCriterion("cows",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.COWBOY_ID, 1))
                .addCriterion("chickens",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.CHICKENHERDER_ID, 1))
                .addCriterion("pigs",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.SWINE_HERDER_ID, 1))
                .addCriterion("rabbits",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.RABBIT_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_all_herders"), fileHelper);
    }

    private void addMilitaryAdvancements(@NotNull final Consumer<Advancement> consumer,
                                         @NotNull final ExistingFileHelper fileHelper)
    {
        final String GROUP = "military/";

        final Advancement root = Advancement.Builder.advancement()
                .display(ModBlocks.blockHutBarracks,
                        new TranslatableComponent("advancements.minecolonies.root.military.title"),
                        new TranslatableComponent("advancements.minecolonies.root.military.description"),
                        new ResourceLocation("textures/block/stone_bricks.png"),
                        FrameType.TASK, true, false, false)
                .addCriterion("guardtower",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.GUARD_TOWER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "root"), fileHelper);

        final Advancement buildArchery = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutArchery, "build.archery"))
                .addCriterion("archery",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.ARCHERY_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_archery"), fileHelper);

        final Advancement buildCombatAcademy = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutCombatAcademy, "build.combat_academy"))
                .addCriterion("combat_academy",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.COMBAT_ACADEMY_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_combat_academy"), fileHelper);

        final Advancement buildBarracks = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, ModBlocks.blockHutBarracks, "build.barracks"))
                .addCriterion("barracks",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BARRACKS_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_barracks"), fileHelper);

        final Advancement buildBarracksTower = Advancement.Builder.advancement()
                .parent(buildBarracks)
                .display(make(FrameType.TASK, ModBlocks.blockHutBarracksTower, "build.barracks_tower"))
                .addCriterion("barracks_tower",
                        new CompleteBuildRequestCriterionInstance(ModBuildings.BARRACKS_TOWER_ID, 1))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_barracks_tower"), fileHelper);

        final Advancement buildAllBarracksTowers = Advancement.Builder.advancement()
                .parent(buildBarracksTower)
                .display(make(FrameType.GOAL, ModBlocks.blockHutBarracksTower, "build.all_towers"))
                .addCriterion("towers", new AllTowersCriterionInstance())
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "build_all_barracks_towers"), fileHelper);

        final Advancement army8 = Advancement.Builder.advancement()
                .parent(root)
                .display(make(FrameType.TASK, Items.STONE_SWORD, "army_8"))
                .addCriterion("population_5", new ArmyPopulationCriterionInstance(8))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_8"), fileHelper);

        final Advancement army30 = Advancement.Builder.advancement()
                .parent(army8)
                .display(make(FrameType.TASK, Items.IRON_SWORD, "army_30"))
                .addCriterion("population_5", new ArmyPopulationCriterionInstance(30))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_30"), fileHelper);

        final Advancement army80 = Advancement.Builder.advancement()
                .parent(army30)
                .display(make(FrameType.CHALLENGE, Items.DIAMOND_SWORD, "army_80"))
                .addCriterion("population_5", new ArmyPopulationCriterionInstance(80))
                .save(consumer, new ResourceLocation(MOD_ID, GROUP + "army_80"), fileHelper);
    }

    private static DisplayInfo make(@NotNull final FrameType frame,
                                    @NotNull final ItemLike icon,
                                    @NotNull final String name)
    {
        return new DisplayInfo(new ItemStack(icon),
                new TranslatableComponent("advancements.minecolonies." + name + ".title"),
                new TranslatableComponent("advancements.minecolonies." + name + ".description"),
                null, frame, true, true, false);
    }

    private static DisplayInfo makeHidden(@NotNull final FrameType frame,
                                          @NotNull final ItemLike icon,
                                          @NotNull final String name)
    {
        return new DisplayInfo(new ItemStack(icon),
                new TranslatableComponent("advancements.minecolonies." + name + ".title"),
                new TranslatableComponent("advancements.minecolonies." + name + ".description"),
                null, frame, true, true, true);
    }

    private static ItemPredicate[] item(@NotNull final ItemLike item)
    {
        return new ItemPredicate[] { ItemPredicate.Builder.item().of(item).build() };
    }
}
