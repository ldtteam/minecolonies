package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.compatibility.CompatibilityManager;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.DefaultBuildingInstance;
import com.minecolonies.coremod.colony.buildings.modules.*;
import com.minecolonies.coremod.colony.buildings.modules.settings.*;
import com.minecolonies.coremod.colony.buildings.moduleviews.*;
import com.minecolonies.coremod.colony.buildings.views.EmptyView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.colony.buildings.AbstractBuildingFurnaceUser.FUEL_LIST;
import static com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards.HOSTILE_LIST;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.composter.EntityAIWorkComposter.COMPOSTABLE_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack.SAPLINGS_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.smelter.EntityAIWorkSmelter.ORE_LIST;

public final class ModBuildingsInitializer
{

    private ModBuildingsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModBuildingsInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<BuildingEntry> event)
    {
        final IForgeRegistry<BuildingEntry> reg = event.getRegistry();

        ModBuildings.archery = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutArchery)
                                 .setBuildingProducer(BuildingArchery::new)
                                 .setBuildingViewProducer(() -> BuildingArchery.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.ARCHERY_ID))
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .addBuildingModuleProducer(BedHandlingModule::new)
                                 .createBuildingEntry();

        ModBuildings.bakery = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutBaker)
                                .setBuildingProducer(BuildingBaker::new)
                                .setBuildingViewProducer(() -> BuildingBaker.View::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BAKERY_ID))
                                .addBuildingModuleProducer(BuildingBaker.CraftingModule::new, () -> CraftingModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, COM_MINECOLONIES_REQUESTS_BURNABLE, false,
                                  (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                .createBuildingEntry();

        ModBuildings.barracks = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutBarracks)
                                  .setBuildingProducer(BuildingBarracks::new)
                                  .setBuildingViewProducer(() -> BuildingBarracks.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BARRACKS_ID))
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .createBuildingEntry();

        ModBuildings.barracksTower = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutBarracksTower)
                                       .setBuildingProducer(BuildingBarracksTower::new)
                                       .setBuildingViewProducer(() -> BuildingBarracksTower.View::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BARRACKS_TOWER_ID))
                                       .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                       .addBuildingModuleProducer(BedHandlingModule::new)
                                       .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterGuard))
                                       .addBuildingModuleProducer(() -> new EntityListModule(HOSTILE_LIST), () -> () -> new EntityListModuleView(HOSTILE_LIST, COM_MINECOLONIES_HOSTILES, true))
                                       .addBuildingModuleProducer(() -> new SettingsModule()
                                                                          .with(AbstractBuildingGuards.JOB, new GuardJobSetting())
                                                                          .with(AbstractBuildingGuards.GUARD_TASK, new GuardTaskSetting(GuardTaskSetting.PATROL, GuardTaskSetting.GUARD, GuardTaskSetting.FOLLOW))
                                                                          .with(AbstractBuildingGuards.RETREAT, new BoolSetting(true))
                                                                          .with(AbstractBuildingGuards.HIRE_TRAINEE, new BoolSetting(true))
                                                                          .with(AbstractBuildingGuards.PATROL_MODE, new PatrolModeSetting())
                                                                          .with(AbstractBuildingGuards.FOLLOW_MODE, new FollowModeSetting()), () -> SettingsModuleView::new)
                                       .createBuildingEntry();

        ModBuildings.blacksmith = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutBlacksmith)
                                    .setBuildingProducer(BuildingBlacksmith::new)
                                    .setBuildingViewProducer(() -> BuildingBlacksmith.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BLACKSMITH_ID))
                                    .addBuildingModuleProducer(BuildingBlacksmith.CraftingModule::new, () -> CraftingModuleView::new)
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry();

        ModBuildings.builder = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutBuilder)
                                 .setBuildingProducer(BuildingBuilder::new)
                                 .setBuildingViewProducer(() -> BuildingBuilder.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BUILDER_ID))
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .addBuildingModuleProducer(() -> new SettingsModule()
                                                                    .with(BuildingBuilder.MODE, new StringSetting(BuildingBuilder.AUTO_SETTING, BuildingBuilder.MANUAL_SETTING))
                                                                    .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                 .addBuildingModuleProducer(SimpleCraftingModule::new, () -> CraftingModuleView::new)
                                 .addBuildingModuleViewProducer(() -> WorkOrderListModuleView::new)
                                 .addBuildingModuleProducer(BuildingResourcesModule::new, () -> BuildingResourcesModuleView::new)
                                 .createBuildingEntry();

        ModBuildings.chickenHerder = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutChickenHerder)
                                       .setBuildingProducer(BuildingChickenHerder::new)
                                       .setBuildingViewProducer(() -> BuildingChickenHerder.View::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.CHICKENHERDER_ID))
                                       .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                       .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuildingWorker.BREEDING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                       .createBuildingEntry();

        ModBuildings.combatAcademy = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutCombatAcademy)
                                       .setBuildingProducer(BuildingCombatAcademy::new)
                                       .setBuildingViewProducer(() -> BuildingCombatAcademy.View::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COMBAT_ACADEMY_ID))
                                       .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                       .addBuildingModuleProducer(BedHandlingModule::new)
                                       .createBuildingEntry();

        ModBuildings.composter = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutComposter)
                                   .setBuildingProducer(BuildingComposter::new)
                                   .setBuildingViewProducer(() -> BuildingComposter.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COMPOSTER_ID))
                                   .addBuildingModuleProducer(() -> new ItemListModule(COMPOSTABLE_LIST), () -> () -> new ItemListModuleView(COMPOSTABLE_LIST, COM_MINECOLONIES_REQUESTS_COMPOSTABLE_UI, false,
                                     (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getCompostInputs()))
                                   .addBuildingModuleProducer(() -> new SettingsModule().with(BuildingComposter.PRODUCE_DIRT, new BoolSetting(false)).with(BuildingComposter.MIN, new IntSetting(16)), () -> SettingsModuleView::new)
                                   .createBuildingEntry();

        ModBuildings.cook = new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutCook)
                              .setBuildingProducer(BuildingCook::new)
                              .setBuildingViewProducer(() -> BuildingCook.View::new)
                              .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COOK_ID))
                              .addBuildingModuleProducer(BuildingCook.CraftingModule::new, () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(BuildingCook.SmeltingModule::new, () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                              .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, COM_MINECOLONIES_REQUESTS_BURNABLE, false,
                                (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                              .addBuildingModuleProducer(() -> new ItemListModule(FOOD_EXCLUSION_LIST), () -> () -> new ItemListModuleView(FOOD_EXCLUSION_LIST, COM_MINECOLONIES_REQUESTS_FOOD, true,
                                (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getEdibles()))
                              .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                              .createBuildingEntry();

        ModBuildings.cowboy = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutCowboy)
                                .setBuildingProducer(BuildingCowboy::new)
                                .setBuildingViewProducer(() -> BuildingCowboy.View::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COWBOY_ID))
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuildingWorker.BREEDING, new BoolSetting(true))
                                                                                     .with(BuildingCowboy.MILKING, new BoolSetting(false)), () -> SettingsModuleView::new)
                                .createBuildingEntry();

        ModBuildings.crusher = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutCrusher)
                                 .setBuildingProducer(BuildingCrusher::new)
                                 .setBuildingViewProducer(() -> BuildingCrusher.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.CRUSHER_ID))
                                 .addBuildingModuleProducer(BuildingCrusher.CraftingModule::new, () -> CraftingModuleView::new)
                                 .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                 .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                 .createBuildingEntry();

        ModBuildings.deliveryman = new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutDeliveryman)
                                     .setBuildingProducer(BuildingDeliveryman::new)
                                     .setBuildingViewProducer(() -> BuildingDeliveryman.View::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.DELIVERYMAN_ID))
                                     .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                     .createBuildingEntry();

        ModBuildings.farmer = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutFarmer)
                                .setBuildingProducer(BuildingFarmer::new)
                                .setBuildingViewProducer(() -> BuildingFarmer.View::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FARMER_ID))
                                .addBuildingModuleProducer(BuildingFarmer.CraftingModule::new, () -> CraftingModuleView::new)
                                .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                .addBuildingModuleProducer(() -> new SettingsModule()
                                                                   .with(BuildingFarmer.FERTILIZE, new BoolSetting(true))
                                                                   .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                .addBuildingModuleProducer(FarmerFieldModule::new, () -> FarmerFieldModuleView::new)
                                .createBuildingEntry();

        ModBuildings.fisherman = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutFisherman)
                                   .setBuildingProducer(BuildingFisherman::new)
                                   .setBuildingViewProducer(() -> BuildingFisherman.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FISHERMAN_ID))
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .createBuildingEntry();

        ModBuildings.guardTower = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutGuardTower)
                                    .setBuildingProducer(BuildingGuardTower::new)
                                    .setBuildingViewProducer(() -> BuildingGuardTower.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.GUARD_TOWER_ID))
                                    .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                    .addBuildingModuleProducer(BedHandlingModule::new)
                                    .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterGuard))
                                    .addBuildingModuleProducer(() -> new EntityListModule(HOSTILE_LIST), () -> () -> new EntityListModuleView(HOSTILE_LIST, COM_MINECOLONIES_HOSTILES, true))
                                    .addBuildingModuleProducer(() -> new SettingsModule()
                                                                       .with(AbstractBuildingGuards.JOB, new GuardJobSetting())
                                                                       .with(AbstractBuildingGuards.GUARD_TASK, new GuardTaskSetting())
                                                                       .with(AbstractBuildingGuards.RETREAT, new BoolSetting(true))
                                                                       .with(AbstractBuildingGuards.HIRE_TRAINEE, new BoolSetting(true))
                                                                       .with(AbstractBuildingGuards.PATROL_MODE, new PatrolModeSetting())
                                                                       .with(AbstractBuildingGuards.FOLLOW_MODE, new FollowModeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry();

        ModBuildings.home = new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutHome)
                              .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, "citizen", 5, ModBuildings.home))
                              .setBuildingViewProducer(() -> HomeBuildingModule.View::new)
                              .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.HOME_ID))
                              .addBuildingModuleProducer(BedHandlingModule::new)
                              .addBuildingModuleProducer(HomeBuildingModule::new)
                              .addBuildingModuleProducer(LivingBuildingModule::new)
                              .createBuildingEntry();

        ModBuildings.library = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutLibrary)
                                 .setBuildingProducer(BuildingLibrary::new)
                                 .setBuildingViewProducer(() -> BuildingLibrary.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.LIBRARY_ID))
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .createBuildingEntry();

        ModBuildings.lumberjack = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutLumberjack)
                                    .setBuildingProducer(BuildingLumberjack::new)
                                    .setBuildingViewProducer(() -> BuildingLumberjack.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.LUMBERJACK_ID))
                                    .addBuildingModuleProducer(BuildingLumberjack.CraftingModule::new, () -> CraftingModuleView::new)
                                    .addBuildingModuleProducer(() -> new ItemListModule(SAPLINGS_LIST), () -> () -> new ItemListModuleView(SAPLINGS_LIST, COM_MINECOLONIES_REQUESTS_SAPLINGS, true,
                                      (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getCopyOfSaplings()))
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule()
                                                                       .with(BuildingLumberjack.REPLANT, new BoolSetting(true))
                                                                       .with(BuildingLumberjack.RESTRICT, new BoolSetting(false))
                                                                       .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterLumberjack))
                                    .createBuildingEntry();

        ModBuildings.miner = new BuildingEntry.Builder()
                               .setBuildingBlock(ModBlocks.blockHutMiner)
                               .setBuildingProducer(BuildingMiner::new)
                               .setBuildingViewProducer(() -> BuildingMiner.View::new)
                               .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MINER_ID))
                               .addBuildingModuleProducer(SimpleCraftingModule::new, () -> CraftingModuleView::new)
                               .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                               .addBuildingModuleProducer(BuildingResourcesModule::new, () -> BuildingResourcesModuleView::new)
                               .createBuildingEntry();

        ModBuildings.sawmill = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutSawmill)
                                 .setBuildingProducer(BuildingSawmill::new)
                                 .setBuildingViewProducer(() -> BuildingSawmill.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SAWMILL_ID))
                                 .addBuildingModuleProducer(BuildingSawmill.CraftingModule::new, () -> CraftingModuleView::new)
                                 .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                 .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)

                                 .createBuildingEntry();

        ModBuildings.shepherd = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutShepherd)
                                  .setBuildingProducer(BuildingShepherd::new)
                                  .setBuildingViewProducer(() -> BuildingShepherd.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SHEPHERD_ID))
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuildingWorker.BREEDING, new BoolSetting(true))
                                                                                       .with(BuildingShepherd.DYEING, new BoolSetting(true))
                                                                                       .with(BuildingShepherd.SHEARING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                  .createBuildingEntry();

        ModBuildings.sifter = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutSifter)
                                .setBuildingProducer(BuildingSifter::new)
                                .setBuildingViewProducer(() -> BuildingSifter.View::new)
                                .addBuildingModuleProducer(BuildingSifter.CraftingModule::new, () -> CraftingModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SIFTER_ID))
                                .createBuildingEntry();

        ModBuildings.smeltery = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutSmeltery)
                                  .setBuildingProducer(BuildingSmeltery::new)
                                  .setBuildingViewProducer(() -> BuildingSmeltery.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SMELTERY_ID))
                                  .addBuildingModuleProducer(BuildingSmeltery.SmeltingModule::new, () -> CraftingModuleView::new)
                                  .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, COM_MINECOLONIES_REQUESTS_BURNABLE, false,
                                    (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .addBuildingModuleProducer(() -> new ItemListModule(ORE_LIST), () -> () -> new ItemListModuleView(ORE_LIST, COM_MINECOLONIES_REQUESTS_SMELTABLE_ORE, true,
                                    (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres()))
                                  .createBuildingEntry();

        ModBuildings.stoneMason = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutStonemason)
                                    .setBuildingProducer(BuildingStonemason::new)
                                    .setBuildingViewProducer(() -> BuildingStonemason.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.STONE_MASON_ID))
                                    .addBuildingModuleProducer(BuildingStonemason.CraftingModule::new, () -> CraftingModuleView::new)
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry();

        ModBuildings.stoneSmelter = new BuildingEntry.Builder()
                                      .setBuildingBlock(ModBlocks.blockHutStoneSmeltery)
                                      .setBuildingProducer(BuildingStoneSmeltery::new)
                                      .setBuildingViewProducer(() -> BuildingStoneSmeltery.View::new)
                                      .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.STONE_SMELTERY_ID))
                                      .addBuildingModuleProducer(BuildingStoneSmeltery.SmeltingModule::new, () -> CraftingModuleView::new)
                                      .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, COM_MINECOLONIES_REQUESTS_BURNABLE, false,
                                        (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                      .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                      .createBuildingEntry();

        ModBuildings.swineHerder = new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutSwineHerder)
                                     .setBuildingProducer(BuildingSwineHerder::new)
                                     .setBuildingViewProducer(() -> BuildingSwineHerder.View::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SWINE_HERDER_ID))
                                     .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuildingWorker.BREEDING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                     .createBuildingEntry();

        ModBuildings.townHall = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutTownHall)
                                  .setBuildingProducer(BuildingTownHall::new)
                                  .setBuildingViewProducer(() -> BuildingTownHall.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.TOWNHALL_ID))
                                  .createBuildingEntry();

        ModBuildings.wareHouse = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutWareHouse)
                                   .setBuildingProducer(BuildingWareHouse::new)
                                   .setBuildingViewProducer(() -> BuildingWareHouse.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.WAREHOUSE_ID))
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .createBuildingEntry();

        ModBuildings.postBox = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockPostBox)
                                 .setBuildingProducer(PostBox::new)
                                 .setBuildingViewProducer(() -> PostBox.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.POSTBOX_ID))
                                 .createBuildingEntry();

        ModBuildings.florist = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutFlorist)
                                 .setBuildingProducer(BuildingFlorist::new)
                                 .setBuildingViewProducer(() -> BuildingFlorist.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FLORIST_ID))
                                 .addBuildingModuleProducer(() -> new ItemListModule(BUILDING_FLOWER_LIST), () -> FloristFlowerListModuleView::new)
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .createBuildingEntry();

        ModBuildings.enchanter = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutEnchanter)
                                   .setBuildingProducer(BuildingEnchanter::new)
                                   .setBuildingViewProducer(() -> BuildingEnchanter.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.ENCHANTER_ID))
                                   .addBuildingModuleProducer(BuildingEnchanter.CraftingModule::new, () -> CraftingModuleView::new)
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .createBuildingEntry();

        ModBuildings.university = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutUniversity)
                                    .setBuildingProducer(BuildingUniversity::new)
                                    .setBuildingViewProducer(() -> BuildingUniversity.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.UNIVERSITY_ID))
                                    .addBuildingModuleViewProducer(() -> UniversityResearchModuleView::new)
                                    .createBuildingEntry();

        ModBuildings.hospital = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutHospital)
                                  .setBuildingProducer(BuildingHospital::new)
                                  .setBuildingViewProducer(() -> BuildingHospital.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.HOSPITAL_ID))
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .createBuildingEntry();

        ModBuildings.stash = new BuildingEntry.Builder()
                               .setBuildingBlock(ModBlocks.blockStash)
                               .setBuildingProducer(Stash::new)
                               .setBuildingViewProducer(() -> EmptyView::new)
                               .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.STASH_ID))
                               .createBuildingEntry();

        ModBuildings.school = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutSchool)
                                .setBuildingProducer(BuildingSchool::new)
                                .setBuildingViewProducer(() -> BuildingSchool.View::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SCHOOL_ID))
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .createBuildingEntry();

        ModBuildings.glassblower = new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutGlassblower)
                                     .setBuildingProducer(BuildingGlassblower::new)
                                     .setBuildingViewProducer(() -> BuildingGlassblower.View::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.GLASSBLOWER_ID))
                                     .addBuildingModuleProducer(BuildingGlassblower.CraftingModule::new, () -> CraftingModuleView::new)
                                     .addBuildingModuleProducer(BuildingGlassblower.SmeltingModule::new, () -> CraftingModuleView::new)
                                     .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, COM_MINECOLONIES_REQUESTS_BURNABLE, false,
                                       (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                     .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                     .createBuildingEntry();

        ModBuildings.dyer = new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutDyer)
                              .setBuildingProducer(BuildingDyer::new)
                              .setBuildingViewProducer(() -> BuildingDyer.View::new)
                              .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.DYER_ID))
                              .addBuildingModuleProducer(BuildingDyer.CraftingModule::new, () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(BuildingDyer.SmeltingModule::new, () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, COM_MINECOLONIES_REQUESTS_BURNABLE, false,
                                (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                              .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                              .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                              .createBuildingEntry();

        ModBuildings.fletcher = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutFletcher)
                                  .setBuildingProducer(BuildingFletcher::new)
                                  .setBuildingViewProducer(() -> BuildingFletcher.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FLETCHER_ID))
                                  .addBuildingModuleProducer(BuildingFletcher.CraftingModule::new, () -> CraftingModuleView::new)
                                  .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                  .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                  .createBuildingEntry();

        ModBuildings.tavern = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutTavern)
                                .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, "tavern", 3, ModBuildings.tavern))
                                .setBuildingViewProducer(() -> TavernBuildingModule.View::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.TAVERN_ID))
                                .addBuildingModuleProducer(BedHandlingModule::new)
                                .addBuildingModuleProducer(LivingBuildingModule::new)
                                .addBuildingModuleProducer(TavernBuildingModule::new)
                                .createBuildingEntry();

        ModBuildings.mechanic = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutMechanic)
                                  .setBuildingProducer(BuildingMechanic::new)
                                  .setBuildingViewProducer(() -> BuildingMechanic.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MECHANIC_ID))
                                  .addBuildingModuleProducer(BuildingMechanic.CraftingModule::new, () -> CraftingModuleView::new)
                                  .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                  .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                  .createBuildingEntry();

        ModBuildings.plantation = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutPlantation)
                                    .setBuildingProducer(BuildingPlantation::new)
                                    .setBuildingViewProducer(() -> BuildingPlantation.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.PLANTATION_ID))
                                    .addBuildingModuleProducer(BuildingPlantation.CraftingModule::new, () -> CraftingModuleView::new)
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule()
                                                                       .with(BuildingPlantation.MODE, new PlantationSetting(Items.SUGAR_CANE.getDescriptionId(), Items.CACTUS.getDescriptionId(), Items.BAMBOO.getDescriptionId()))
                                                                       .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry();

        ModBuildings.rabbitHutch = new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutRabbitHutch)
                                     .setBuildingProducer(BuildingRabbitHutch::new)
                                     .setBuildingViewProducer(() -> BuildingRabbitHutch.View::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.RABBIT_ID))
                                     .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuildingWorker.BREEDING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                     .createBuildingEntry();

        //todo we want two here, one custom for the concrete placement, and one crafting for the normal crafting of the powder.
        ModBuildings.concreteMixer = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutConcreteMixer)
                                       .setBuildingProducer(BuildingConcreteMixer::new)
                                       .setBuildingViewProducer(() -> BuildingConcreteMixer.View::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.CONCRETE_ID))
                                       .addBuildingModuleProducer(BuildingConcreteMixer.CraftingModule::new, () -> CraftingModuleView::new)
                                       .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                       .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                       .createBuildingEntry();

        ModBuildings.beekeeper = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutBeekeeper)
                                   .setBuildingProducer(BuildingBeekeeper::new)
                                   .setBuildingViewProducer(() -> BuildingBeekeeper.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BEEKEEPER_ID))
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .addBuildingModuleProducer(() -> new SettingsModule()
                                                                      .with(AbstractBuildingWorker.BREEDING, new BoolSetting(true))
                                                                      .with(BuildingBeekeeper.MODE, new StringSetting(BuildingBeekeeper.HONEYCOMB, BuildingBeekeeper.HONEY, BuildingBeekeeper.BOTH)), () -> SettingsModuleView::new)
                                   .addBuildingModuleProducer(() -> new ItemListModule(BUILDING_FLOWER_LIST),  () -> () -> new ItemListModuleView(BUILDING_FLOWER_LIST, COM_MINECOLONIES_COREMOD_REQUEST_FLOWERS, false,
                                     (buildingView) -> CompatibilityManager.getAllBeekeeperFlowers()))
                                   .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterBeekeeper))
                                   .createBuildingEntry();

        ModBuildings.mysticalSite = new BuildingEntry.Builder()
                                      .setBuildingBlock(ModBlocks.blockHutMysticalSite)
                                      .setBuildingProducer(BuildingMysticalSite::new)
                                      .setBuildingViewProducer(() -> BuildingMysticalSite.View::new)
                                      .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MYSTICAL_SITE_ID))
                                      .createBuildingEntry();


        ModBuildings.graveyard = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutGraveyard)
                                    .setBuildingProducer(BuildingGraveyard::new)
                                    .setBuildingViewProducer(() -> BuildingGraveyard.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.GRAVEYARD_ID))
                                    .createBuildingEntry();

        reg.register(ModBuildings.archery);
        reg.register(ModBuildings.bakery);
        reg.register(ModBuildings.barracks);
        reg.register(ModBuildings.barracksTower);
        reg.register(ModBuildings.blacksmith);
        reg.register(ModBuildings.builder);
        reg.register(ModBuildings.chickenHerder);
        reg.register(ModBuildings.combatAcademy);
        reg.register(ModBuildings.composter);
        reg.register(ModBuildings.cook);
        reg.register(ModBuildings.cowboy);
        reg.register(ModBuildings.crusher);
        reg.register(ModBuildings.deliveryman);
        reg.register(ModBuildings.farmer);
        reg.register(ModBuildings.fisherman);
        reg.register(ModBuildings.guardTower);
        reg.register(ModBuildings.home);
        reg.register(ModBuildings.library);
        reg.register(ModBuildings.lumberjack);
        reg.register(ModBuildings.miner);
        reg.register(ModBuildings.sawmill);
        reg.register(ModBuildings.shepherd);
        reg.register(ModBuildings.sifter);
        reg.register(ModBuildings.smeltery);
        reg.register(ModBuildings.stoneMason);
        reg.register(ModBuildings.stoneSmelter);
        reg.register(ModBuildings.swineHerder);
        reg.register(ModBuildings.townHall);
        reg.register(ModBuildings.wareHouse);
        reg.register(ModBuildings.postBox);
        reg.register(ModBuildings.florist);
        reg.register(ModBuildings.enchanter);
        reg.register(ModBuildings.university);
        reg.register(ModBuildings.hospital);
        reg.register(ModBuildings.stash);
        reg.register(ModBuildings.school);
        reg.register(ModBuildings.glassblower);
        reg.register(ModBuildings.dyer);
        reg.register(ModBuildings.fletcher);
        reg.register(ModBuildings.mechanic);
        reg.register(ModBuildings.plantation);
        reg.register(ModBuildings.tavern);
        reg.register(ModBuildings.rabbitHutch);
        reg.register(ModBuildings.concreteMixer);
        reg.register(ModBuildings.beekeeper);
        reg.register(ModBuildings.mysticalSite);
        reg.register(ModBuildings.graveyard);
    }
}
