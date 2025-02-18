package com.minecolonies.core.colony.buildings.modules;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingModuleView;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.core.colony.buildings.modules.settings.*;
import com.minecolonies.core.colony.buildings.moduleviews.*;
import com.minecolonies.core.colony.buildings.workerbuildings.*;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_HOSTILES;
import static com.minecolonies.core.colony.buildings.AbstractBuildingGuards.HOSTILE_LIST;
import static com.minecolonies.core.entity.ai.workers.crafting.EntityAIWorkSmelter.ORE_LIST;
import static com.minecolonies.core.entity.ai.workers.production.EntityAIWorkLumberjack.SAPLINGS_LIST;
import static com.minecolonies.core.entity.ai.workers.production.agriculture.EntityAIWorkComposter.COMPOSTABLE_LIST;

public class BuildingModules
{
    /**
     * Global
     */
    public static final BuildingEntry.ModuleProducer<MinimumStockModule,MinimumStockModuleView> MIN_STOCK =
      new BuildingEntry.ModuleProducer<>("min_stock", MinimumStockModule::new, () -> MinimumStockModuleView::new);
    public static final BuildingEntry.ModuleProducer<BedHandlingModule, IBuildingModuleView> BED             = new BuildingEntry.ModuleProducer<>("bed", BedHandlingModule::new, null);
    public static final BuildingEntry.ModuleProducer<FurnaceUserModule,IBuildingModuleView>  FURNACE                 = new BuildingEntry.ModuleProducer<>("furnace", FurnaceUserModule::new, null);
    public static final BuildingEntry.ModuleProducer<IBuildingModule, RequestTaskModuleView> CRAFT_TASK_VIEW         = new BuildingEntry.ModuleProducer<>("craft_task_view", null, () -> CrafterRequestTaskModuleView::new);
    public static final BuildingEntry.ModuleProducer<IBuildingModule, RequestTaskModuleView> COURIER_TASK_VIEW         = new BuildingEntry.ModuleProducer<>("courier_task_view", null, () -> CourierRequestTaskModuleView::new);

    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView>      SETTINGS_CRAFTER_RECIPE = new BuildingEntry.ModuleProducer<>("craft_settings",
      () -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()),
      () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<EntityListModule,EntityListModuleView> GUARD_ENTITY_LIST       = new BuildingEntry.ModuleProducer<>("guard_entity_list",
      () -> new EntityListModule(HOSTILE_LIST),
      () -> () -> new EntityListModuleView(HOSTILE_LIST, COM_MINECOLONIES_HOSTILES, true));

    public static final BuildingEntry.ModuleProducer<BuildingStatisticsModule, BuildingStatisticsModuleView> STATS_MODULE = new BuildingEntry.ModuleProducer<>(
      "stats_module", BuildingStatisticsModule::new,
      () -> BuildingStatisticsModuleView::new);

    /**
     * Item Lists
     */
    public static final BuildingEntry.ModuleProducer<ItemListModule,ItemListModuleView> ITEMLIST_FUEL = new BuildingEntry.ModuleProducer<>(
      "itemlist_fuel", () -> new ItemListModule(FUEL_LIST, new ItemStorage(Items.COAL), new ItemStorage(Items.CHARCOAL)),
      () -> () -> new ItemListModuleView(FUEL_LIST,
        RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE,
        false,
        (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()));

    public static final BuildingEntry.ModuleProducer<ItemListModule,ItemListModuleView> ITEMLIST_COMPOSTABLE =
      new BuildingEntry.ModuleProducer<>("itemlist_compostable", () -> new ItemListModule(COMPOSTABLE_LIST),
        () -> () -> new ItemListModuleView(COMPOSTABLE_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_COMPOSTABLE_UI, false,
          (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getCompostInputs()));

    public static final BuildingEntry.ModuleProducer<RestaurantMenuModule, RestaurantMenuModuleView> RESTAURANT_MENU =
      new BuildingEntry.ModuleProducer<>("restaurant_menu", () -> new RestaurantMenuModule(true, ISchematicProvider::getBuildingLevel), () -> RestaurantMenuModuleView::new);

    public static final BuildingEntry.ModuleProducer<RestaurantMenuModule, RestaurantMenuModuleView> NETHERMINER_MENU =
      new BuildingEntry.ModuleProducer<>("netherminer_menu", () -> new RestaurantMenuModule(false, building -> 1), () -> RestaurantMenuModuleView::new);

    public static final BuildingEntry.ModuleProducer<ItemListModule,ItemListModuleView> ITEMLIST_SAPLING =
      new BuildingEntry.ModuleProducer<>("itemlist_sapling", () -> new ItemListModule(SAPLINGS_LIST),
        () -> () -> new ItemListModuleView(SAPLINGS_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_SAPLINGS, true,
          (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getCopyOfSaplings()));

    public static final BuildingEntry.ModuleProducer<ItemListModule,ItemListModuleView> ITEMLIST_ORE =
      new BuildingEntry.ModuleProducer<>("itemlist_ore", () -> new ItemListModule(ORE_LIST),
        () -> () -> new ItemListModuleView(ORE_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_SMELTABLE_ORE, true,
          (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres()));

    public static final BuildingEntry.ModuleProducer<ItemListModule,ItemListModuleView> ITEMLIST_FLOWER =
      new BuildingEntry.ModuleProducer<>("itemlist_flower", () -> new ItemListModule(BUILDING_FLOWER_LIST),
        () -> () -> new ItemListModuleView(BUILDING_FLOWER_LIST, RequestSystemTranslationConstants.REQUEST_TYPE_FLOWERS, false,
          (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getImmutableFlowers()));

    /**
     * Workers
     */

    /**
     * Horticulture
     */
    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> COMPOSTER_WORK                  =
      new BuildingEntry.ModuleProducer<>("composter_work",
        () -> new WorkerBuildingModule(ModJobs.composter.get(), Skill.Stamina, Skill.Athletics, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> COMPOSTER_SETTINGS              =
      new BuildingEntry.ModuleProducer<>("composter_settings",
        () -> new SettingsModule().with(BuildingComposter.PRODUCE_DIRT, new BoolSetting(false)).with(BuildingComposter.MIN, new IntSetting(16)),
        () -> SettingsModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> FARMER_CRAFT        =
      new BuildingEntry.ModuleProducer<>("farmer_craft", () -> new CraftingWorkerBuildingModule(ModJobs.farmer.get(), Skill.Stamina, Skill.Athletics, false, b -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingFarmer.CraftingModule,CraftingModuleView> FARMER_WORK         =
      new BuildingEntry.ModuleProducer<>("farmer_work", () -> new BuildingFarmer.CraftingModule(ModJobs.farmer.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingFarmer.FarmerFieldsModule,BuildingFarmer.FarmerFieldsModuleView> FARMER_FIELDS       =
      new BuildingEntry.ModuleProducer<>("farmer_fields", BuildingFarmer.FarmerFieldsModule::new, () -> BuildingFarmer.FarmerFieldsModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> FARMER_SETTINGS     =
      new BuildingEntry.ModuleProducer<>("farmer_settings", () -> new SettingsModule()
        .with(BuildingFarmer.FERTILIZE, new BoolSetting(true))
        .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> PLANTATION_WORK       =
      new BuildingEntry.ModuleProducer<>("plantation_work", () -> new CraftingWorkerBuildingModule(ModJobs.planter.get(), Skill.Agility, Skill.Dexterity, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingPlantation.CraftingModule,CraftingModuleView> PLANTATION_CRAFT      =
      new BuildingEntry.ModuleProducer<>("plantation_craft", () -> new BuildingPlantation.CraftingModule(ModJobs.planter.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingPlantation.PlantationFieldsModule, BuildingPlantation.PlantationFieldsModuleView> PLANTATION_FIELDS     =
      new BuildingEntry.ModuleProducer<>("plantation_fields", BuildingPlantation.PlantationFieldsModule::new, () -> BuildingPlantation.PlantationFieldsModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> PLANTATION_SETTINGS   =
      new BuildingEntry.ModuleProducer<>("plantation_settings", () -> new SettingsModule()
        .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> FLORIST_WORK          =
      new BuildingEntry.ModuleProducer<>("florist_work",
        () -> new WorkerBuildingModule(ModJobs.florist.get(), Skill.Dexterity, Skill.Agility, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<ItemListModule,FloristFlowerListModuleView> FLORIST_ITEMS         =
      new BuildingEntry.ModuleProducer<>("florist_items", () -> new ItemListModule(BUILDING_FLOWER_LIST), () -> FloristFlowerListModuleView::new);

    /**
     * Husbandry
     */

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> BEEKEEPER_WORK        =
      new BuildingEntry.ModuleProducer<>("beekeeper_work", () -> new WorkerBuildingModule(ModJobs.beekeeper.get(), Skill.Dexterity, Skill.Adaptability, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> BEEKEEPER_SETTINGS    =
      new BuildingEntry.ModuleProducer<>("beekeeper_settings", () -> new SettingsModule()
        .with(AbstractBuilding.BREEDING, new BoolSetting(true))
        .with(BuildingBeekeeper.MODE, new BeekeeperCollectionSetting(BuildingBeekeeper.HONEYCOMB, BuildingBeekeeper.HONEY, BuildingBeekeeper.BOTH)),
        () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<IBuildingModule,ToolModuleView> BEEKEEPER_TOOL        =
      new BuildingEntry.ModuleProducer<>("beekeeper_tool", null, () -> () -> new ToolModuleView(ModItems.scepterBeekeeper));
    public static final BuildingEntry.ModuleProducer<BuildingBeekeeper.HerdingModule,IBuildingModuleView> BEEKEEPER_HERDING     =
      new BuildingEntry.ModuleProducer<>("beekeeper_herding", BuildingBeekeeper.HerdingModule::new, null);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> CHICKENHERDER_WORK              =
      new BuildingEntry.ModuleProducer<>("chickenherder_work", () -> new WorkerBuildingModule(ModJobs.chickenHerder.get(), Skill.Adaptability, Skill.Agility, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingChickenHerder.HerdingModule,IBuildingModuleView> CHICKENHERDER_HERDING           =
      new BuildingEntry.ModuleProducer<>("chickenherder_herding", BuildingChickenHerder.HerdingModule::new, null);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> CHICKENHERDER_SETTINGS_BREEDING =
      new BuildingEntry.ModuleProducer<>("chickenherder_breeding_settings",
        () -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true)),
        () -> SettingsModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> COWHERDER_WORK      =
      new BuildingEntry.ModuleProducer<>("cowherder_work",
        () -> new WorkerBuildingModule(ModJobs.cowboy.get(), Skill.Athletics, Skill.Stamina, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> COWHERDER_SETTINGS  =
      new BuildingEntry.ModuleProducer<>("cowherder_settings", () -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
        .with(BuildingCowboy.MILKING_AMOUNT, new IntSetting(1))
        .with(BuildingCowboy.STEWING_AMOUNT, new IntSetting(1))
        .with(BuildingCowboy.MILKING_DAYS, new IntSetting(1))
       .with(BuildingCowboy.MILK_ITEM, new StringSetting("item.minecolonies.large_milk_bottle", "item.minecraft.milk_bucket")), () -> SettingsModuleView::new);

    public static final BuildingEntry.ModuleProducer<BuildingCowboy.HerdingModule,IBuildingModuleView> COWHERDER_HERDING   =
      new BuildingEntry.ModuleProducer<>("cowherder_herding", BuildingCowboy.HerdingModule::new, null);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> FISHER_WORK         =
      new BuildingEntry.ModuleProducer<>("fisher_work",
        () -> new WorkerBuildingModule(ModJobs.fisherman.get(), Skill.Focus, Skill.Agility, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> RABBITHERDER_WORK     =
      new BuildingEntry.ModuleProducer<>("rabbitherder_work", () -> new WorkerBuildingModule(ModJobs.rabbitHerder.get(), Skill.Agility, Skill.Athletics, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule, SettingsModuleView>             RABBITHERDER_SETTINGS =
      new BuildingEntry.ModuleProducer<>("rabbitherder_settings", () -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true)), () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<AnimalHerdingModule, IBuildingModuleView>       RABBITHERDER_HERDING  =
      new BuildingEntry.ModuleProducer<>("rabbitherder_herding",
        () -> new AnimalHerdingModule(ModJobs.rabbitHerder.get(), a -> a instanceof Rabbit, new ItemStack(Items.CARROT, 2)),
        null);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> SHEPERD_WORK          =
      new BuildingEntry.ModuleProducer<>("sheperd_work",
        () -> new WorkerBuildingModule(ModJobs.shepherd.get(), Skill.Focus, Skill.Strength, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> SHEPERD_SETTINGS      =
      new BuildingEntry.ModuleProducer<>("sheperd_settings", () -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
        .with(BuildingShepherd.DYEING, new BoolSetting(true))
        .with(BuildingShepherd.SHEARING, new BoolSetting(true)), () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingShepherd.HerdingModule,IBuildingModuleView> SHEPERD_HERDING       =
      new BuildingEntry.ModuleProducer<>("sheperd_herding", BuildingShepherd.HerdingModule::new, null);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule, WorkerBuildingModuleView> SWINEHERDER_WORK     =
      new BuildingEntry.ModuleProducer<>("swineherder_work", () -> new WorkerBuildingModule(ModJobs.swineHerder.get(), Skill.Strength, Skill.Athletics, true, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule, SettingsModuleView>             SWINEHERDER_SETTINGS =
      new BuildingEntry.ModuleProducer<>("swineherder_settings", () -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true)), () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<AnimalHerdingModule, IBuildingModuleView>       SWINEHERDER_HERDING  =
      new BuildingEntry.ModuleProducer<>("swineherder_herding",
        () -> new AnimalHerdingModule(ModJobs.swineHerder.get(), a -> a instanceof Pig, new ItemStack(Items.CARROT, 2)),
        null);

    /**
     * Craftmanship
     */

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> ALCHEMIST_WORK          =
      new BuildingEntry.ModuleProducer<>("alchemist_work", () -> new CraftingWorkerBuildingModule(ModJobs.alchemist.get(), Skill.Dexterity, Skill.Mana, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingAlchemist.CraftingModule,CraftingModuleView> ALCHEMIST_CRAFT         =
      new BuildingEntry.ModuleProducer<>("alchemist_craft", () -> new BuildingAlchemist.CraftingModule(ModJobs.alchemist.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingAlchemist.BrewingModule,CraftingModuleView> ALCHEMIST_BREW          =
      new BuildingEntry.ModuleProducer<>("alchemist_brew", () -> new BuildingAlchemist.BrewingModule(ModJobs.alchemist.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<BuildingBaker.CraftingModule,CraftingModuleView> BAKER_CRAFT                     =
      new BuildingEntry.ModuleProducer<>("baker_craft", () -> new BuildingBaker.CraftingModule(ModJobs.baker.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingBaker.SmeltingModule,CraftingModuleView> BAKER_SMELT                     =
      new BuildingEntry.ModuleProducer<>("baker_smelt", () -> new BuildingBaker.SmeltingModule(ModJobs.baker.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> BAKER_WORK                      = new BuildingEntry.ModuleProducer<>(
      "baker_work", () -> new CraftingWorkerBuildingModule(ModJobs.baker.get(), Skill.Knowledge, Skill.Dexterity, false, (b) -> 1, Skill.Dexterity, Skill.Knowledge),
      () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> BLACKSMITH_WORK                 = new BuildingEntry.ModuleProducer<>(
      "blacksmith_work", () -> new CraftingWorkerBuildingModule(ModJobs.blacksmith.get(), Skill.Strength, Skill.Focus, false, (b) -> 1, Skill.Strength, Skill.Focus),
      () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingBlacksmith.CraftingModule,CraftingModuleView> BLACKSMITH_CRAFT                =
      new BuildingEntry.ModuleProducer<>("blacksmith_craft", () -> new BuildingBlacksmith.CraftingModule(ModJobs.blacksmith.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> CONCRETEMIXER_WORK    =
      new BuildingEntry.ModuleProducer<>("concretemixer_work", () -> new CraftingWorkerBuildingModule(ModJobs.concreteMixer.get(), Skill.Stamina, Skill.Dexterity, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingConcreteMixer.CraftingModule,CraftingModuleView> CONCRETEMIXER_CRAFT   =
      new BuildingEntry.ModuleProducer<>("concretemixer_craft", () -> new BuildingConcreteMixer.CraftingModule(ModJobs.concreteMixer.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> CRUSHER_WORK        =
      new BuildingEntry.ModuleProducer<>("crusher_work", () -> new CraftingWorkerBuildingModule(ModJobs.crusher.get(), Skill.Stamina, Skill.Strength, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingCrusher.CraftingModule,CraftingModuleView> CRUSHER_CRAFT       =
      new BuildingEntry.ModuleProducer<>("crusher_craft", () -> new BuildingCrusher.CraftingModule(ModJobs.crusher.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> CRUSHER_SETTINGS    =
      new BuildingEntry.ModuleProducer<>("crusher_settings", () -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
        .with(BuildingCrusher.MODE, new RecipeSetting("custom"))
        .with(BuildingCrusher.DAILY_LIMIT, new IntSetting(0)), () -> SettingsModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> DYER_WORK             =
      new BuildingEntry.ModuleProducer<>("dyer_work", () -> new CraftingWorkerBuildingModule(ModJobs.dyer.get(),
        Skill.Creativity,
        Skill.Dexterity,
        false,
        (b) -> 1,
        Skill.Dexterity,
        Skill.Creativity), () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingDyer.CraftingModule,CraftingModuleView> DYER_CRAFT            =
      new BuildingEntry.ModuleProducer<>("dyer_craft", () -> new BuildingDyer.CraftingModule(ModJobs.dyer.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingDyer.SmeltingModule,CraftingModuleView> DYER_SMELT            =
      new BuildingEntry.ModuleProducer<>("dyer_smelt", () -> new BuildingDyer.SmeltingModule(ModJobs.dyer.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> FLETCHER_WORK         =
      new BuildingEntry.ModuleProducer<>("fletcher_work", () -> new CraftingWorkerBuildingModule(ModJobs.fletcher.get(),
        Skill.Dexterity,
        Skill.Creativity,
        true,
        (b) -> 1,
        Skill.Creativity,
        Skill.Dexterity), () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingFletcher.CraftingModule,CraftingModuleView> FLETCHER_CRAFT        =
      new BuildingEntry.ModuleProducer<>("fletcher_craft", () -> new BuildingFletcher.CraftingModule(ModJobs.fletcher.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingFletcher.DOCraftingModule,DOCraftingModuleView> FLETCHER_DO_CRAFT     =
      new BuildingEntry.ModuleProducer<>("fletcher_do_craft", () -> new BuildingFletcher.DOCraftingModule(ModJobs.fletcher.get()),
        () -> () -> new DOCraftingModuleView(BuildingFletcher.DOCraftingModule::getStaticIngredientValidator));

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> GLASSBLOWER_WORK      =
      new BuildingEntry.ModuleProducer<>("glassblower_work", () -> new CraftingWorkerBuildingModule(ModJobs.glassblower.get(),
        Skill.Creativity,
        Skill.Focus,
        false,
        (b) -> 1,
        Skill.Focus,
        Skill.Creativity), () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingGlassblower.CraftingModule,CraftingModuleView> GLASSBLOWER_CRAFT     =
      new BuildingEntry.ModuleProducer<>("glassblower_craft", () -> new BuildingGlassblower.CraftingModule(ModJobs.glassblower.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingGlassblower.SmeltingModule,CraftingModuleView> GLASSBLOWER_SMELTING  =
      new BuildingEntry.ModuleProducer<>("glassblower_smelting", () -> new BuildingGlassblower.SmeltingModule(ModJobs.glassblower.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingGlassblower.DOCraftingModule,DOCraftingModuleView> GLASSBLOWER_DO_CRAFT  =
      new BuildingEntry.ModuleProducer<>("glassblower_do_craft", () -> new BuildingGlassblower.DOCraftingModule(ModJobs.glassblower.get()),
        () -> () -> new DOCraftingModuleView(BuildingGlassblower.DOCraftingModule::getStaticIngredientValidator));

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> MECHANIC_WORK         =
      new BuildingEntry.ModuleProducer<>("mechanic_work", () -> new CraftingWorkerBuildingModule(ModJobs.mechanic.get(), Skill.Knowledge, Skill.Agility, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingMechanic.CraftingModule,CraftingModuleView> MECHANIC_CRAFT        =
      new BuildingEntry.ModuleProducer<>("mechanic_craft", () -> new BuildingMechanic.CraftingModule(ModJobs.mechanic.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingMechanic.DOCraftingModule,DOCraftingModuleView> MECHANIC_DO_CRAFT     =
      new BuildingEntry.ModuleProducer<>("mechanic_do_craft", () -> new BuildingMechanic.DOCraftingModule(ModJobs.mechanic.get()),
        () -> () -> new DOCraftingModuleView(BuildingMechanic.DOCraftingModule::getStaticIngredientValidator));

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> SAWMILL_WORK          =
      new BuildingEntry.ModuleProducer<>("sawmill_work", () -> new CraftingWorkerBuildingModule(ModJobs.sawmill.get(), Skill.Knowledge, Skill.Dexterity, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingSawmill.CraftingModule,CraftingModuleView> SAWMILL_CRAFT         =
      new BuildingEntry.ModuleProducer<>("sawmill_craft", () -> new BuildingSawmill.CraftingModule(ModJobs.sawmill.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingSawmill.DOCraftingModule,DOCraftingModuleView> SAWMILL_DO_CRAFT      =
      new BuildingEntry.ModuleProducer<>("sawmill_do_craft", () -> new BuildingSawmill.DOCraftingModule(ModJobs.sawmill.get()),
        () -> () -> new DOCraftingModuleView(BuildingSawmill.DOCraftingModule::getStaticIngredientValidator));

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> SIFTER_WORK           =
      new BuildingEntry.ModuleProducer<>("sifter_work", () -> new CraftingWorkerBuildingModule(ModJobs.sifter.get(), Skill.Focus, Skill.Strength, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingSifter.CraftingModule,CraftingModuleView> SIFTER_CRAFT          =
      new BuildingEntry.ModuleProducer<>("sifter_craft", () -> new BuildingSifter.CraftingModule(ModJobs.sifter.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> SMELTER_WORK          =
      new BuildingEntry.ModuleProducer<>("smelter_work", () -> new CraftingWorkerBuildingModule(ModJobs.smelter.get(), Skill.Athletics, Skill.Strength, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingSmeltery.SmeltingModule,CraftingModuleView> SMELTER_SMELTING      =
      new BuildingEntry.ModuleProducer<>("smelter_smelting", () -> new BuildingSmeltery.SmeltingModule(ModJobs.smelter.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingSmeltery.OreBreakingModule,CraftingModuleView> SMELTER_OREBREAK      =
      new BuildingEntry.ModuleProducer<>("smelter_orebreak", () -> new BuildingSmeltery.OreBreakingModule(ModJobs.smelter.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> STONEMASON_WORK       =
      new BuildingEntry.ModuleProducer<>("stonemason_work", () -> new CraftingWorkerBuildingModule(ModJobs.stoneMason.get(), Skill.Creativity, Skill.Dexterity, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingStonemason.CraftingModule,CraftingModuleView> STONEMASON_CRAFT      =
      new BuildingEntry.ModuleProducer<>("stonemason_craft", () -> new BuildingStonemason.CraftingModule(ModJobs.stoneMason.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingStonemason.DOCraftingModule,DOCraftingModuleView> STONEMASON_DO_CRAFT   =
      new BuildingEntry.ModuleProducer<>("stonemason_do_craft", () -> new BuildingStonemason.DOCraftingModule(ModJobs.stoneMason.get()),
        () -> () -> new DOCraftingModuleView(BuildingStonemason.DOCraftingModule::getStaticIngredientValidator));

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> STONESMELTER_WORK     =
      new BuildingEntry.ModuleProducer<>("stonesmelter_work", () -> new CraftingWorkerBuildingModule(ModJobs.stoneSmeltery.get(),
        Skill.Athletics,
        Skill.Dexterity,
        false,
        (b) -> 1,
        Skill.Dexterity,
        Skill.Athletics), () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingStoneSmeltery.SmeltingModule,CraftingModuleView> STONESMELTER_SMELTING =
      new BuildingEntry.ModuleProducer<>("stonesmelter_smelting", () -> new BuildingStoneSmeltery.SmeltingModule(ModJobs.stoneSmeltery.get()), () -> CraftingModuleView::new);

    /**
     * Storage
     */

    public static final BuildingEntry.ModuleProducer<DeliverymanAssignmentModule,WorkerBuildingModuleView> COURIER_WORK        =
      new BuildingEntry.ModuleProducer<>("courier_work", () -> new DeliverymanAssignmentModule(ModJobs.delivery.get(), Skill.Agility, Skill.Adaptability, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CourierAssignmentModule,CourierAssignmentModuleView> WAREHOUSE_COURIERS    =
      new BuildingEntry.ModuleProducer<>("warehouse_couriers", CourierAssignmentModule::new, () -> CourierAssignmentModuleView::new);
    public static final BuildingEntry.ModuleProducer<WarehouseModule,WarehouseOptionsModuleView> WAREHOUSE_OPTIONS     =
      new BuildingEntry.ModuleProducer<>("warehouse_options", WarehouseModule::new, () -> WarehouseOptionsModuleView::new);
    public static final BuildingEntry.ModuleProducer<WarehouseRequestQueueModule, WarehouseRequestTaskModuleView> WAREHOUSE_REQUEST_QUEUE     =
      new BuildingEntry.ModuleProducer<>("warehouse_request_queue", WarehouseRequestQueueModule::new, () -> WarehouseRequestTaskModuleView::new);

    /**
     * Education
     */

    public static final BuildingEntry.ModuleProducer<ChildrenBuildingModule,PupilBuildingModuleView> PUPIL_WORK            =
      new BuildingEntry.ModuleProducer<>("pupil_work", () -> new ChildrenBuildingModule(ModJobs.pupil.get(), Skill.Knowledge, Skill.Mana, true, (b) -> 2 * b.getBuildingLevel()),
        () -> PupilBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,StudentBuildingModuleView> STUDENT_WORK          =
      new BuildingEntry.ModuleProducer<>("student_work",
        () -> new WorkerBuildingModule(ModJobs.student.get(), Skill.Intelligence, Skill.Intelligence, true, (b) -> 2 * b.getBuildingLevel()),
        () -> StudentBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> TEACHER_WORK          =
      new BuildingEntry.ModuleProducer<>("teacher_work",
        () -> new WorkerBuildingModule(ModJobs.teacher.get(), Skill.Knowledge, Skill.Mana, true, (b) -> 1),
        () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> UNIVERSITY_WORK       =
      new BuildingEntry.ModuleProducer<>("university_work",
        () -> new WorkerBuildingModule(ModJobs.researcher.get(), Skill.Knowledge, Skill.Mana, true, ISchematicProvider::getBuildingLevel),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<IBuildingModule,UniversityResearchModuleView> UNIVERSITY_RESEARCH   =
      new BuildingEntry.ModuleProducer<>("university_research", null, () -> UniversityResearchModuleView::new);

    /**
     * Fundamentals
     */

    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> BUILDER_WORK                    =
      new BuildingEntry.ModuleProducer<>("builder_work", () -> new WorkerBuildingModule(ModJobs.builder.get(), Skill.Adaptability, Skill.Athletics, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> BUILDER_SETTINGS                =
      new BuildingEntry.ModuleProducer<>("builder_settings", () -> new SettingsModule()
        .with(BuildingBuilder.MODE, new StringSetting(BuildingBuilder.AUTO_SETTING, BuildingBuilder.MANUAL_SETTING))
        .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
        .with(BuildingBuilder.BUILDING_MODE, new BuilderModeSetting())
        .with(AbstractBuilding.USE_SHEARS, new BoolSetting(false))
        .with(BuildingMiner.FILL_BLOCK, new BlockSetting((BlockItem) Items.DIRT)), () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<SimpleCraftingModule,CraftingModuleView> BUILDER_CRAFT                   =
      new BuildingEntry.ModuleProducer<>("builder_craft", () -> new SimpleCraftingModule(ModJobs.builder.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<IBuildingModule,WorkOrderListModuleView> WORKORDER_VIEW                  =
      new BuildingEntry.ModuleProducer<>("workorderlist", null, () -> WorkOrderListModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingResourcesModule,BuildingResourcesModuleView> BUILDING_RESOURCES              =
      new BuildingEntry.ModuleProducer<>("buildingresources", BuildingResourcesModule::new, () -> BuildingResourcesModuleView::new);

    public static final BuildingEntry.ModuleProducer<NoPrivateCrafterWorkerModule,WorkerBuildingModuleView> COOK_WORK           =
      new BuildingEntry.ModuleProducer<>("cook_craft", () -> new NoPrivateCrafterWorkerModule(ModJobs.cook.get(), Skill.Adaptability, Skill.Knowledge, true, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> CHEF_WORK           =
      new BuildingEntry.ModuleProducer<>("chef_work", () -> new CraftingWorkerBuildingModule(ModJobs.chef.get(),
        Skill.Creativity,
        Skill.Knowledge,
        false,
        (b) -> 1,
        Skill.Knowledge,
        Skill.Creativity), () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<BuildingKitchen.CraftingModule,CraftingModuleView> CHEF_CRAFT =
      new BuildingEntry.ModuleProducer<>("chef_craft", () -> new BuildingKitchen.CraftingModule(ModJobs.chef.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingKitchen.SmeltingModule,CraftingModuleView> CHEF_SMELT =
      new BuildingEntry.ModuleProducer<>("chef_smelt", () -> new BuildingKitchen.SmeltingModule(ModJobs.chef.get()), () -> CraftingModuleView::new);

    public static final BuildingEntry.ModuleProducer<BuildingLumberjack.CraftingModule,CraftingModuleView> FORESTER_CRAFT        =
      new BuildingEntry.ModuleProducer<>("forester_craft", () -> new BuildingLumberjack.CraftingModule(ModJobs.lumberjack.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<LumberjackAssignmentModule,WorkerBuildingModuleView> FORESTER_WORK         =
      new BuildingEntry.ModuleProducer<>("forester_work",
        () -> new LumberjackAssignmentModule(ModJobs.lumberjack.get(), Skill.Strength, Skill.Focus, false, (b) -> 1, Skill.Focus, Skill.Strength),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> FORESTER_SETTINGS =
      new BuildingEntry.ModuleProducer<>("forester_settings", () -> new SettingsModule()
        .with(BuildingLumberjack.REPLANT, new BoolSetting(true))
        .with(BuildingLumberjack.RESTRICT, new BoolSetting(false))
        .with(BuildingLumberjack.DEFOLIATE, new BoolSetting(false))
        .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
        .with(BuildingLumberjack.DYNAMIC_TREES_SIZE, new DynamicTreesSetting())
        .with(AbstractBuilding.USE_SHEARS, new BoolSetting(false)), () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<IBuildingModule,ToolModuleView> FORESTER_TOOL     =
      new BuildingEntry.ModuleProducer<>("forester_zone", null, () -> () -> new ToolModuleView(ModItems.scepterLumberjack));

    public static final BuildingEntry.ModuleProducer<HospitalAssignmentModule,WorkerBuildingModuleView> HEALER_WORK           =
      new BuildingEntry.ModuleProducer<>("healer_work",
        () -> new HospitalAssignmentModule(ModJobs.healer.get(), Skill.Mana, Skill.Knowledge, true, (b) -> 1),
        () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<HomeBuildingModule, IBuildingModuleView>        HOME   =
      new BuildingEntry.ModuleProducer<>("home", HomeBuildingModule::new, null);
    public static final BuildingEntry.ModuleProducer<LivingBuildingModule, LivingBuildingModuleView> LIVING =
      new BuildingEntry.ModuleProducer<>("living", LivingBuildingModule::new, () -> LivingBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<MinerBuildingModule,CombinedHiringLimitModuleView> MINER_WORK            =
      new BuildingEntry.ModuleProducer<>("miner_work",
        () -> new MinerBuildingModule(ModJobs.miner.get(), Skill.Strength, Skill.Stamina, false, (b) -> 1),
        () -> CombinedHiringLimitModuleView::new);
    public static final BuildingEntry.ModuleProducer<SimpleCraftingModule,CraftingModuleView> MINER_CRAFT           =
      new BuildingEntry.ModuleProducer<>("miner_craft", () -> new SimpleCraftingModule(ModJobs.miner.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<MinerLevelManagementModule,MinerLevelManagementModuleView> MINER_LEVELS          =
      new BuildingEntry.ModuleProducer<>("miner_levels", MinerLevelManagementModule::new, () -> MinerLevelManagementModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> MINER_SETTINGS        =
      new BuildingEntry.ModuleProducer<>("miner_settings", () -> new SettingsModule()
        .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
        .with(BuildingMiner.FILL_BLOCK, new BlockSetting((BlockItem) Items.COBBLESTONE))
        .with(BuildingMiner.MAX_DEPTH, new IntSetting(-100))
        .with(AbstractBuilding.USE_SHEARS, new BoolSetting(false)), () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<IBuildingModule,MinerGuardAssignModuleView> MINER_GUARD_ASSIGN    =
      new BuildingEntry.ModuleProducer<>("miner_guard_assign", null, () -> MinerGuardAssignModuleView::new);

    public static final BuildingEntry.ModuleProducer<MinerBuildingModule,CombinedHiringLimitModuleView> QUARRIER_WORK         =
      new BuildingEntry.ModuleProducer<>("quarrier_work", () -> new MinerBuildingModule(ModJobs.quarrier.get(), Skill.Strength, Skill.Stamina, false, (b) -> 1),
        () -> CombinedHiringLimitModuleView::new);
    public static final BuildingEntry.ModuleProducer<QuarryModule,MinerAssignmentModuleView> SIMPLE_QUARRY           =
      new BuildingEntry.ModuleProducer<>("simple_quarry", () -> new QuarryModule(32), () -> MinerAssignmentModuleView::new);
    public static final BuildingEntry.ModuleProducer<QuarryModule,MinerAssignmentModuleView> MEDIUM_QUARRY           =
      new BuildingEntry.ModuleProducer<>("medium_quarry", () -> new QuarryModule(64), () -> MinerAssignmentModuleView::new);

    public static final BuildingEntry.ModuleProducer<TavernLivingBuildingModule, IBuildingModuleView> TAVERN_LIVING  =
      new BuildingEntry.ModuleProducer<>("tavern_living", TavernLivingBuildingModule::new, () -> LivingBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<TavernBuildingModule, IBuildingModuleView>       TAVERN_VISITOR =
      new BuildingEntry.ModuleProducer<>("tavern_visitor", TavernBuildingModule::new, null);

    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> TOWNHALL_SETTINGS  =
      new BuildingEntry.ModuleProducer<>("townhall_settings", () -> new SettingsModule()
        .with(BuildingTownHall.MOVE_IN, new BoolSetting(true))
        .with(BuildingTownHall.ENTER_LEAVE_MESSAGES, new BoolSetting(true))
        .with(BuildingTownHall.AUTO_HOUSING_MODE, new BoolSetting(true))
        .with(BuildingTownHall.AUTO_HIRING_MODE, new BoolSetting(true))
        .with(BuildingTownHall.CONSTRUCTION_TAPE, new BoolSetting(true)),
        () -> TownHallSettingsModuleView::new);

    /**
     * Military
     */

    public static final BuildingEntry.ModuleProducer<WorkAtHomeBuildingModule,ArcherSquireModuleView> ARCHERY_WORK_HOME               = new BuildingEntry.ModuleProducer<>(
      "archery_work", () -> new WorkAtHomeBuildingModule(ModJobs.archerInTraining.get(), Skill.Agility, Skill.Adaptability, false, ISchematicProvider::getBuildingLevel),
      () -> ArcherSquireModuleView::new);

    public static final BuildingEntry.ModuleProducer<WorkAtHomeBuildingModule,KnightSquireBuildingModuleView> KNIGHT_TRAINING                 =
      new BuildingEntry.ModuleProducer<>("knight_training", () -> new WorkAtHomeBuildingModule(ModJobs.knightInTraining.get(),
        Skill.Adaptability,
        Skill.Stamina,
        false,
        ISchematicProvider::getBuildingLevel), () -> KnightSquireBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<GuardBuildingModule,CombinedHiringLimitModuleView> KNIGHT_BARRACKS_WORK = new BuildingEntry.ModuleProducer<>(
      "knight_barracks_work", () -> new GuardBuildingModule(ModGuardTypes.knight.get(), true, ISchematicProvider::getBuildingLevel),
      () -> CombinedHiringLimitModuleView::new);
    public static final BuildingEntry.ModuleProducer<GuardBuildingModule,CombinedHiringLimitModuleView> RANGER_BARRACKS_WORK = new BuildingEntry.ModuleProducer<>(
      "ranger_barracks_work", () -> new GuardBuildingModule(ModGuardTypes.ranger.get(), true, ISchematicProvider::getBuildingLevel),
      () -> CombinedHiringLimitModuleView::new);
    public static final BuildingEntry.ModuleProducer<GuardBuildingModule,CombinedHiringLimitModuleView> DRUID_BARRACKS_WORK  = new BuildingEntry.ModuleProducer<>(
      "druid_barracks_work", () -> new GuardBuildingModule(ModGuardTypes.druid.get(), true, ISchematicProvider::getBuildingLevel),
      () -> CombinedHiringLimitModuleView::new);

    public static final BuildingEntry.ModuleProducer<GuardBuildingModule,CombinedHiringLimitModuleView> KNIGHT_TOWER_WORK =
      new BuildingEntry.ModuleProducer<>("knight_tower_work", () -> new GuardBuildingModule(ModGuardTypes.knight.get(), true, (b) -> 1), () -> CombinedHiringLimitModuleView::new);
    public static final BuildingEntry.ModuleProducer<GuardBuildingModule,CombinedHiringLimitModuleView> RANGER_TOWER_WORK =
      new BuildingEntry.ModuleProducer<>("ranger_tower_work", () -> new GuardBuildingModule(ModGuardTypes.ranger.get(), true, (b) -> 1), () -> CombinedHiringLimitModuleView::new);
    public static final BuildingEntry.ModuleProducer<GuardBuildingModule,CombinedHiringLimitModuleView> DRUID_TOWER_WORK  =
      new BuildingEntry.ModuleProducer<>("druid_tower_work", () -> new GuardBuildingModule(ModGuardTypes.druid.get(), true, (b) -> 1), () -> CombinedHiringLimitModuleView::new);

    public static final BuildingEntry.ModuleProducer<IBuildingModule,ToolModuleView> GUARD_TOOL     =
      new BuildingEntry.ModuleProducer<>("tool_scepterguard_view", null, () -> () -> new ToolModuleView(
        ModItems.scepterGuard));
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> GUARD_SETTINGS = new BuildingEntry.ModuleProducer<>("guard_settings", () -> new SettingsModule()
      .with(AbstractBuildingGuards.GUARD_TASK, new GuardTaskSetting(GuardTaskSetting.PATROL, GuardTaskSetting.GUARD, GuardTaskSetting.FOLLOW, GuardTaskSetting.PATROL_MINE))
      .with(AbstractBuildingGuards.RETREAT, new BoolSetting(true))
      .with(AbstractBuildingGuards.HIRE_TRAINEE, new BoolSetting(true))
      .with(AbstractBuildingGuards.PATROL_MODE, new GuardPatrolModeSetting())
      .with(AbstractBuildingGuards.FOLLOW_MODE, new GuardFollowModeSetting()), () -> SettingsModuleView::new);

    /**
     * Mystic
     */

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> ENCHANTER_WORK        =
      new BuildingEntry.ModuleProducer<>("enchanter_work", () -> new CraftingWorkerBuildingModule(ModJobs.enchanter.get(), Skill.Mana, Skill.Knowledge, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingEnchanter.CraftingModule,CraftingModuleView> ENCHANTER_CRAFT       =
      new BuildingEntry.ModuleProducer<>("enchanter_craft", () -> new BuildingEnchanter.CraftingModule(ModJobs.enchanter.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<EnchanterStationsModule,EnchanterStationsModuleView> ENCHANTER_STATIONS    =
      new BuildingEntry.ModuleProducer<>("enchanter_stations", EnchanterStationsModule::new, () -> EnchanterStationsModuleView::new);

    public static final BuildingEntry.ModuleProducer<GraveyardManagementModule,GraveyardManagementModuleView> GRAVEYARD               =
      new BuildingEntry.ModuleProducer<>("graveyard", GraveyardManagementModule::new, () -> GraveyardManagementModuleView::new);
    public static final BuildingEntry.ModuleProducer<WorkerBuildingModule,WorkerBuildingModuleView> GRAVEYARD_WORK          =
      new BuildingEntry.ModuleProducer<>("graveyard_work",
        () -> new WorkerBuildingModule(ModJobs.undertaker.get(), Skill.Strength, Skill.Mana, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);

    public static final BuildingEntry.ModuleProducer<CraftingWorkerBuildingModule,WorkerBuildingModuleView> NETHERWORKER_WORK       =
      new BuildingEntry.ModuleProducer<>("netherworker_work", () -> new CraftingWorkerBuildingModule(ModJobs.netherworker.get(), Skill.Adaptability, Skill.Strength, false, (b) -> 1),
        () -> WorkerBuildingModuleView::new);
    public static final BuildingEntry.ModuleProducer<BuildingNetherWorker.CraftingModule,CraftingModuleView> NETHERWORKER_CRAFT      =
      new BuildingEntry.ModuleProducer<>("netherworker_craft", () -> new BuildingNetherWorker.CraftingModule(ModJobs.netherworker.get()), () -> CraftingModuleView::new);
    public static final BuildingEntry.ModuleProducer<SettingsModule,SettingsModuleView> NETHERWORKER_SETTINGS   =
      new BuildingEntry.ModuleProducer<>("netherworker_settings", () -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
        .with(BuildingNetherWorker.CLOSE_PORTAL, new BoolSetting(true))
        , () -> SettingsModuleView::new);
    public static final BuildingEntry.ModuleProducer<ExpeditionLogModule,ExpeditionLogModuleView> NETHERWORKER_EXPEDITION =
      new BuildingEntry.ModuleProducer<>("netherworker_expedition", () -> new ExpeditionLogModule(ResearchConstants.NETHER_LOG), () -> ExpeditionLogModuleView::new);
}
