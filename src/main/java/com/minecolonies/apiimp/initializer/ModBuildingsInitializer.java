package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.buildings.ISchematicProvider;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.guardtype.registry.ModGuardTypes;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.research.util.ResearchConstants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.translation.RequestSystemTranslationConstants;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards;
import com.minecolonies.coremod.colony.buildings.BuildingMysticalSite;
import com.minecolonies.coremod.colony.buildings.DefaultBuildingInstance;
import com.minecolonies.coremod.colony.buildings.modules.*;
import com.minecolonies.coremod.colony.buildings.modules.settings.*;
import com.minecolonies.coremod.colony.buildings.moduleviews.*;
import com.minecolonies.coremod.colony.buildings.views.EmptyView;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.DeferredRegister;

import static com.minecolonies.api.util.constant.BuildingConstants.BUILDING_FLOWER_LIST;
import static com.minecolonies.api.util.constant.BuildingConstants.FUEL_LIST;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_HOSTILES;
import static com.minecolonies.coremod.colony.buildings.AbstractBuildingGuards.HOSTILE_LIST;
import static com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingCook.FOOD_EXCLUSION_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.composter.EntityAIWorkComposter.COMPOSTABLE_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.lumberjack.EntityAIWorkLumberjack.SAPLINGS_LIST;
import static com.minecolonies.coremod.entity.ai.citizen.smelter.EntityAIWorkSmelter.ORE_LIST;

public final class ModBuildingsInitializer
{
    public final static DeferredRegister<BuildingEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "buildings"), Constants.MOD_ID);

    private ModBuildingsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModBuildingsInitializer but this is a Utility class.");
    }

    static
    {
        ModBuildings.archery = DEFERRED_REGISTER.register(ModBuildings.ARCHERY_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutArchery)
                                 .setBuildingProducer(BuildingArchery::new)
                                 .setBuildingViewProducer(() -> EmptyView::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.ARCHERY_ID))
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .addBuildingModuleProducer(() -> new WorkAtHomeBuildingModule(ModJobs.archer.get(), Skill.Agility, Skill.Adaptability, false, ISchematicProvider::getBuildingLevel), () -> ArcherSquireModuleView::new)
                                 .addBuildingModuleProducer(BedHandlingModule::new)
                                 .createBuildingEntry());

        ModBuildings.bakery = DEFERRED_REGISTER.register(ModBuildings.BAKERY_ID, () -> new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutBaker)
                                .setBuildingProducer(BuildingBaker::new)
                                .setBuildingViewProducer(() -> EmptyView::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BAKERY_ID))
                                .addBuildingModuleProducer(() -> new BuildingBaker.CraftingModule(ModJobs.baker.get()), () -> CraftingModuleView::new)
                                .addBuildingModuleProducer(() -> new BuildingBaker.SmeltingModule(ModJobs.baker.get()), () -> CraftingModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .addBuildingModuleProducer(FurnaceUserModule::new)
                                .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.baker.get(), Skill.Knowledge, Skill.Dexterity, false, (b) -> 1, Skill.Dexterity, Skill.Knowledge), () -> WorkerBuildingModuleView::new)
                                .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, false,
                                  (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                .createBuildingEntry());

        ModBuildings.barracks = DEFERRED_REGISTER.register(ModBuildings.BARRACKS_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutBarracks)
                                  .setBuildingProducer(BuildingBarracks::new)
                                  .setBuildingViewProducer(() -> BuildingBarracks.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BARRACKS_ID))
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .createBuildingEntry());

        ModBuildings.barracksTower = DEFERRED_REGISTER.register(ModBuildings.BARRACKS_TOWER_ID, () -> new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutBarracksTower)
                                       .setBuildingProducer(BuildingBarracksTower::new)
                                       .setBuildingViewProducer(() -> BuildingBarracksTower.View::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BARRACKS_TOWER_ID))
                                       .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                       .addBuildingModuleProducer(BedHandlingModule::new)
                                       .addBuildingModuleProducer(() -> new GuardBuildingModule(ModGuardTypes.knight.get(), true, ISchematicProvider::getBuildingLevel), () -> CombinedHiringLimitModuleView::new)
                                       .addBuildingModuleProducer(() -> new GuardBuildingModule(ModGuardTypes.ranger.get(), true, ISchematicProvider::getBuildingLevel), () -> CombinedHiringLimitModuleView::new)
                                       .addBuildingModuleProducer(() -> new GuardBuildingModule(ModGuardTypes.druid.get(), true, ISchematicProvider::getBuildingLevel), () -> CombinedHiringLimitModuleView::new)
                                       .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterGuard))
                                       .addBuildingModuleProducer(() -> new EntityListModule(HOSTILE_LIST), () -> () -> new EntityListModuleView(HOSTILE_LIST, COM_MINECOLONIES_HOSTILES, true))
                                       .addBuildingModuleProducer(() -> new SettingsModule()
                                                                          .with(AbstractBuildingGuards.GUARD_TASK, new GuardTaskSetting(GuardTaskSetting.PATROL, GuardTaskSetting.GUARD, GuardTaskSetting.FOLLOW))
                                                                          .with(AbstractBuildingGuards.RETREAT, new BoolSetting(true))
                                                                          .with(AbstractBuildingGuards.HIRE_TRAINEE, new BoolSetting(true))
                                                                          .with(AbstractBuildingGuards.PATROL_MODE, new PatrolModeSetting())
                                                                          .with(AbstractBuildingGuards.FOLLOW_MODE, new FollowModeSetting()), () -> SettingsModuleView::new)
                                       .createBuildingEntry());

        ModBuildings.blacksmith = DEFERRED_REGISTER.register(ModBuildings.BLACKSMITH_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutBlacksmith)
                                    .setBuildingProducer(BuildingBlacksmith::new)
                                    .setBuildingViewProducer(() -> EmptyView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BLACKSMITH_ID))
                                    .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.blacksmith.get(), Skill.Strength, Skill.Focus, false, (b) -> 1, Skill.Strength, Skill.Focus), () -> WorkerBuildingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingBlacksmith.CraftingModule(ModJobs.blacksmith.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.builder = DEFERRED_REGISTER.register(ModBuildings.BUILDER_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutBuilder)
                                 .setBuildingProducer(BuildingBuilder::new)
                                 .setBuildingViewProducer(() -> BuildingBuilder.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BUILDER_ID))
                                 .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.builder.get(), Skill.Adaptability, Skill.Athletics, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .addBuildingModuleProducer(() -> new SettingsModule()
                                                                    .with(BuildingBuilder.MODE, new StringSetting(BuildingBuilder.AUTO_SETTING, BuildingBuilder.MANUAL_SETTING))
                                                                    .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
                                                                    .with(BuildingBuilder.BUILDING_MODE, new BuilderModeSetting())
                                                                    .with(AbstractBuilding.USE_SHEARS, new BoolSetting(true))
                                                                    .with(BuildingMiner.FILL_BLOCK, new BlockSetting((BlockItem) Items.DIRT)), () -> SettingsModuleView::new)
                                 .addBuildingModuleProducer(() -> new SimpleCraftingModule(ModJobs.builder.get()), () -> CraftingModuleView::new)
                                 .addBuildingModuleViewProducer(() -> WorkOrderListModuleView::new)
                                 .addBuildingModuleProducer(BuildingResourcesModule::new, () -> BuildingResourcesModuleView::new)
                                 .createBuildingEntry());

        ModBuildings.chickenHerder = DEFERRED_REGISTER.register(ModBuildings.CHICKENHERDER_ID, () -> new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutChickenHerder)
                                       .setBuildingProducer(BuildingChickenHerder::new)
                                       .setBuildingViewProducer(() -> EmptyView::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.CHICKENHERDER_ID))
                                       .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.chickenHerder.get(), Skill.Adaptability, Skill.Agility, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                       .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                       .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
                                                                                            .with(AbstractBuilding.FEEDING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                       .addBuildingModuleProducer(BuildingChickenHerder.HerdingModule::new)
                                       .createBuildingEntry());

        ModBuildings.combatAcademy = DEFERRED_REGISTER.register(ModBuildings.COMBAT_ACADEMY_ID, () -> new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutCombatAcademy)
                                       .setBuildingProducer(BuildingCombatAcademy::new)
                                       .setBuildingViewProducer(() -> EmptyView::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COMBAT_ACADEMY_ID))
                                       .addBuildingModuleProducer(() -> new WorkAtHomeBuildingModule(ModJobs.combat.get(), Skill.Adaptability, Skill.Stamina, false, ISchematicProvider::getBuildingLevel), () -> KnightSquireBuildingModuleView::new)
                                       .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                       .addBuildingModuleProducer(BedHandlingModule::new)
                                       .createBuildingEntry());

        ModBuildings.composter = DEFERRED_REGISTER.register(ModBuildings.COMPOSTER_ID, () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutComposter)
                                   .setBuildingProducer(BuildingComposter::new)
                                   .setBuildingViewProducer(() -> EmptyView::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COMPOSTER_ID))
                                   .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.composter.get(), Skill.Stamina, Skill.Athletics, false, (b) -> 1), () -> WorkerBuildingModuleView::new)

                                   .addBuildingModuleProducer(() -> new ItemListModule(COMPOSTABLE_LIST), () -> () -> new ItemListModuleView(COMPOSTABLE_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_COMPOSTABLE_UI, false,
                                     (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getCompostInputs()))
                                   .addBuildingModuleProducer(() -> new SettingsModule().with(BuildingComposter.PRODUCE_DIRT, new BoolSetting(false)).with(BuildingComposter.MIN, new IntSetting(16)), () -> SettingsModuleView::new)
                                   .createBuildingEntry());

        ModBuildings.cook = DEFERRED_REGISTER.register(ModBuildings.COOK_ID, () -> new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutCook)
                              .setBuildingProducer(BuildingCook::new)
                              .setBuildingViewProducer(() -> EmptyView::new)
                              .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COOK_ID))
                              .addBuildingModuleProducer(() -> new NoPrivateCrafterWorkerModule(ModJobs.cook.get(), Skill.Adaptability, Skill.Knowledge, true, (b) -> 1), () -> WorkerBuildingModuleView::new)
                              .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.cookassistant.get(), Skill.Creativity, Skill.Knowledge, false, (b) -> b.getBuildingLevel() >= 3 ? 1 : 0, Skill.Knowledge, Skill.Creativity), () -> WorkerBuildingModuleView::new)
                              .addBuildingModuleProducer(() -> new BuildingCook.CraftingModule(ModJobs.cookassistant.get()), () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(() -> new BuildingCook.SmeltingModule(ModJobs.cookassistant.get()), () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                              .addBuildingModuleProducer(FurnaceUserModule::new)
                              .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, false,
                                (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                              .addBuildingModuleProducer(() -> new ItemListModule(FOOD_EXCLUSION_LIST).onResetToDefaults(BuildingCook::onResetFoodExclusionList), () -> () -> new ItemListModuleView(FOOD_EXCLUSION_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_FOOD, true,
                                (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getEdibles(buildingView.getBuildingLevel() - 1)))
                              .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                              .createBuildingEntry());

        ModBuildings.cowboy = DEFERRED_REGISTER.register(ModBuildings.COWBOY_ID, () -> new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutCowboy)
                                .setBuildingProducer(BuildingCowboy::new)
                                .setBuildingViewProducer(() -> EmptyView::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.COWBOY_ID))
                                .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.cowboy.get(), Skill.Athletics, Skill.Stamina, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
                                                                                     .with(AbstractBuilding.FEEDING, new BoolSetting(true))
                                                                                     .with(BuildingCowboy.MILKING, new BoolSetting(false)), () -> SettingsModuleView::new)
                                .addBuildingModuleProducer(() -> new AnimalHerdingModule(ModJobs.cowboy.get(), EntityType.COW, new ItemStack(Items.WHEAT, 2)))
                                .addBuildingModuleProducer(BuildingCowboy.MilkingModule::new)
                                .createBuildingEntry());

        ModBuildings.crusher = DEFERRED_REGISTER.register(ModBuildings.CRUSHER_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutCrusher)
                                 .setBuildingProducer(BuildingCrusher::new)
                                 .setBuildingViewProducer(() -> EmptyView::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.CRUSHER_ID))
                                 .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.crusher.get(), Skill.Stamina, Skill.Strength, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                 .addBuildingModuleProducer(() -> new BuildingCrusher.CraftingModule(ModJobs.crusher.get()), () -> CraftingModuleView::new)
                                 .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                 .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
                                                                                      .with(BuildingCrusher.MODE, new RecipeSetting("custom"))
                                                                                      .with(BuildingCrusher.DAILY_LIMIT, new IntSetting(0)), () -> SettingsModuleView::new)
                                 .createBuildingEntry());

        ModBuildings.deliveryman = DEFERRED_REGISTER.register(ModBuildings.DELIVERYMAN_ID, () -> new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutDeliveryman)
                                     .setBuildingProducer(BuildingDeliveryman::new)
                                     .setBuildingViewProducer(() -> EmptyView::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.DELIVERYMAN_ID))
                                     .addBuildingModuleProducer(() -> new DeliverymanAssignmentModule(ModJobs.delivery.get(), Skill.Agility, Skill.Adaptability, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                     .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                     .createBuildingEntry());

        ModBuildings.farmer = DEFERRED_REGISTER.register(ModBuildings.FARMER_ID, () -> new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutFarmer)
                                .setBuildingProducer(BuildingFarmer::new)
                                .setBuildingViewProducer(() -> EmptyView::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FARMER_ID))
                                .addBuildingModuleProducer(() -> new BuildingFarmer.CraftingModule(ModJobs.farmer.get()), () -> CraftingModuleView::new)
                                .addBuildingModuleProducer(() -> new FarmerAssignmentModule(ModJobs.farmer.get(), Skill.Stamina, Skill.Athletics, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                .addBuildingModuleProducer(() -> new SettingsModule()
                                                                   .with(BuildingFarmer.FERTILIZE, new BoolSetting(true))
                                                                   .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                .addBuildingModuleProducer(FarmerFieldModule::new, () -> FarmerFieldModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .createBuildingEntry());

        ModBuildings.fisherman = DEFERRED_REGISTER.register(ModBuildings.FISHERMAN_ID, () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutFisherman)
                                   .setBuildingProducer(BuildingFisherman::new)
                                   .setBuildingViewProducer(() -> EmptyView::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FISHERMAN_ID))
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.fisherman.get(), Skill.Focus, Skill.Agility, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                   .createBuildingEntry());

        ModBuildings.guardTower = DEFERRED_REGISTER.register(ModBuildings.GUARD_TOWER_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutGuardTower)
                                    .setBuildingProducer(BuildingGuardTower::new)
                                    .setBuildingViewProducer(() -> BuildingGuardTower.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.GUARD_TOWER_ID))
                                    .addBuildingModuleProducer(() -> new GuardBuildingModule(ModGuardTypes.knight.get(), true, (b) -> 1), () -> CombinedHiringLimitModuleView::new)
                                    .addBuildingModuleProducer(() -> new GuardBuildingModule(ModGuardTypes.ranger.get(), true, (b) -> 1), () -> CombinedHiringLimitModuleView::new)
                                    .addBuildingModuleProducer(() -> new GuardBuildingModule(ModGuardTypes.druid.get(), true, (b) -> 1), () -> CombinedHiringLimitModuleView::new)
                                    .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                    .addBuildingModuleProducer(BedHandlingModule::new)
                                    .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterGuard))
                                    .addBuildingModuleProducer(() -> new EntityListModule(HOSTILE_LIST), () -> () -> new EntityListModuleView(HOSTILE_LIST, COM_MINECOLONIES_HOSTILES, true))
                                    .addBuildingModuleProducer(() -> new SettingsModule()
                                                                       .with(AbstractBuildingGuards.GUARD_TASK, new GuardTaskSetting())
                                                                       .with(AbstractBuildingGuards.RETREAT, new BoolSetting(true))
                                                                       .with(AbstractBuildingGuards.HIRE_TRAINEE, new BoolSetting(true))
                                                                       .with(AbstractBuildingGuards.PATROL_MODE, new PatrolModeSetting())
                                                                       .with(AbstractBuildingGuards.FOLLOW_MODE, new FollowModeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.home = DEFERRED_REGISTER.register(ModBuildings.HOME_ID, () -> new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutHome)
                              .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, "residence", 5))
                              .setBuildingViewProducer(() -> HomeBuildingModule.View::new)
                              .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.HOME_ID))
                              .addBuildingModuleProducer(BedHandlingModule::new)
                              .addBuildingModuleProducer(HomeBuildingModule::new)
                              .addBuildingModuleProducer(LivingBuildingModule::new)
                              .createBuildingEntry());

        ModBuildings.library = DEFERRED_REGISTER.register(ModBuildings.LIBRARY_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutLibrary)
                                 .setBuildingProducer(BuildingLibrary::new)
                                 .setBuildingViewProducer(() -> EmptyView::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.LIBRARY_ID))
                                 .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.student.get(), Skill.Intelligence, Skill.Intelligence, true, (b) -> 2 * b.getBuildingLevel()), () -> StudentBuildingModuleView::new)
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .createBuildingEntry());

        ModBuildings.lumberjack = DEFERRED_REGISTER.register(ModBuildings.LUMBERJACK_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutLumberjack)
                                    .setBuildingProducer(BuildingLumberjack::new)
                                    .setBuildingViewProducer(() -> BuildingLumberjack.View::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.LUMBERJACK_ID))
                                    .addBuildingModuleProducer(() -> new BuildingLumberjack.CraftingModule(ModJobs.lumberjack.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleProducer(() -> new ItemListModule(SAPLINGS_LIST), () -> () -> new ItemListModuleView(SAPLINGS_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_SAPLINGS, true,
                                      (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getCopyOfSaplings()))
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new LumberjackAssignmentModule(ModJobs.lumberjack.get(), Skill.Strength, Skill.Focus, false, (b) -> 1, Skill.Focus, Skill.Strength), () -> WorkerBuildingModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule()
                                                                       .with(BuildingLumberjack.REPLANT, new BoolSetting(true))
                                                                       .with(BuildingLumberjack.RESTRICT, new BoolSetting(false))
                                                                       .with(BuildingLumberjack.DEFOLIATE, new BoolSetting(false))
                                                                       .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
                                                                       .with(BuildingLumberjack.DYNAMIC_TREES_SIZE, new DynamicTreesSetting())
                                                                       .with(AbstractBuilding.USE_SHEARS, new BoolSetting(true)), () -> SettingsModuleView::new)
                                    .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterLumberjack))
                                    .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.miner = DEFERRED_REGISTER.register(ModBuildings.MINER_ID, () -> new BuildingEntry.Builder()
                               .setBuildingBlock(ModBlocks.blockHutMiner)
                               .setBuildingProducer(BuildingMiner::new)
                               .setBuildingViewProducer(() -> EmptyView::new)
                               .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MINER_ID))
                               .addBuildingModuleProducer(() -> new MinerBuildingModule(ModJobs.miner.get(), Skill.Strength, Skill.Stamina, false, (b) -> 1), () -> CombinedHiringLimitModuleView::new)
                               .addBuildingModuleProducer(() -> new MinerBuildingModule(ModJobs.quarrier.get(), Skill.Strength, Skill.Stamina, false, (b) -> 1), () -> CombinedHiringLimitModuleView::new)
          .addBuildingModuleProducer(() -> new SimpleCraftingModule(ModJobs.miner.get()), () -> CraftingModuleView::new)
                               .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                               .addBuildingModuleProducer(BuildingResourcesModule::new, () -> BuildingResourcesModuleView::new)
                               .addBuildingModuleProducer(MinerLevelManagementModule::new, () -> MinerLevelManagementModuleView::new)
                               .addBuildingModuleProducer(() -> new SettingsModule()
                                                                  .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
                                                                  .with(BuildingMiner.FILL_BLOCK, new BlockSetting((BlockItem) Items.COBBLESTONE))
                                                                  .with(BuildingMiner.MAX_DEPTH, new IntSetting(-100))
                                                                  .with(AbstractBuilding.USE_SHEARS, new BoolSetting(true)), () -> SettingsModuleView::new)
                               .addBuildingModuleViewProducer(() -> MinerGuardAssignModuleView::new)
                               .createBuildingEntry());

        ModBuildings.sawmill = DEFERRED_REGISTER.register(ModBuildings.SAWMILL_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutSawmill)
                                 .setBuildingProducer(BuildingSawmill::new)
                                 .setBuildingViewProducer(() -> EmptyView::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SAWMILL_ID))
                                 .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.sawmill.get(), Skill.Knowledge, Skill.Dexterity, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                 .addBuildingModuleProducer(() -> new BuildingSawmill.CraftingModule(ModJobs.sawmill.get()), () -> CraftingModuleView::new)
                                 .addBuildingModuleProducer(() -> new BuildingSawmill.DOCraftingModule(ModJobs.sawmill.get()), () -> () -> new DOCraftingModuleView(BuildingSawmill.DOCraftingModule::getStaticIngredientValidator))
                                 .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                 .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)

                                 .createBuildingEntry());

        ModBuildings.shepherd = DEFERRED_REGISTER.register(ModBuildings.SHEPHERD_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutShepherd)
                                  .setBuildingProducer(BuildingShepherd::new)
                                  .setBuildingViewProducer(() -> EmptyView::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SHEPHERD_ID))
                                  .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.shepherd.get(), Skill.Focus, Skill.Strength, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
                                                                                       .with(AbstractBuilding.FEEDING, new BoolSetting(true))
                                                                                       .with(BuildingShepherd.DYEING, new BoolSetting(true))
                                                                                       .with(BuildingShepherd.SHEARING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                  .addBuildingModuleProducer(BuildingShepherd.HerdingModule::new)
                                  .createBuildingEntry());

        ModBuildings.sifter = DEFERRED_REGISTER.register(ModBuildings.SIFTER_ID, () -> new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutSifter)
                                .setBuildingProducer(BuildingSifter::new)
                                .setBuildingViewProducer(() -> EmptyView::new)
                                .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.sifter.get(), Skill.Focus, Skill.Strength, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                .addBuildingModuleProducer(() -> new BuildingSifter.CraftingModule(ModJobs.sifter.get()), () -> CraftingModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SIFTER_ID))
                                .createBuildingEntry());

        ModBuildings.smeltery = DEFERRED_REGISTER.register(ModBuildings.SMELTERY_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutSmeltery)
                                  .setBuildingProducer(BuildingSmeltery::new)
                                  .setBuildingViewProducer(() -> EmptyView::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SMELTERY_ID))
                                  .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.smelter.get(), Skill.Athletics, Skill.Strength, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                  .addBuildingModuleProducer(() -> new BuildingSmeltery.SmeltingModule(ModJobs.smelter.get()), () -> CraftingModuleView::new)
                                  .addBuildingModuleProducer(FurnaceUserModule::new)
                                  .addBuildingModuleProducer(() -> new BuildingSmeltery.OreBreakingModule(ModJobs.smelter.get()), () -> CraftingModuleView::new)
                                  .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, false,
                                    (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .addBuildingModuleProducer(() -> new ItemListModule(ORE_LIST), () -> () -> new ItemListModuleView(ORE_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_SMELTABLE_ORE, true,
                                    (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getSmeltableOres()))
                                  .createBuildingEntry());

        ModBuildings.stoneMason = DEFERRED_REGISTER.register(ModBuildings.STONE_MASON_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutStonemason)
                                    .setBuildingProducer(BuildingStonemason::new)
                                    .setBuildingViewProducer(() -> EmptyView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.STONE_MASON_ID))
                                    .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.stoneMason.get(), Skill.Creativity, Skill.Dexterity, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingStonemason.CraftingModule(ModJobs.stoneMason.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingStonemason.DOCraftingModule(ModJobs.stoneMason.get()), () -> () -> new DOCraftingModuleView(BuildingStonemason.DOCraftingModule::getStaticIngredientValidator))
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.stoneSmelter = DEFERRED_REGISTER.register(ModBuildings.STONE_SMELTERY_ID, () -> new BuildingEntry.Builder()
                                      .setBuildingBlock(ModBlocks.blockHutStoneSmeltery)
                                      .setBuildingProducer(BuildingStoneSmeltery::new)
                                      .setBuildingViewProducer(() -> EmptyView::new)
                                      .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.STONE_SMELTERY_ID))
                                      .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.stoneSmeltery.get(), Skill.Athletics, Skill.Dexterity, false, (b) -> 1, Skill.Dexterity, Skill.Athletics), () -> WorkerBuildingModuleView::new)
                                      .addBuildingModuleProducer(() -> new BuildingStoneSmeltery.SmeltingModule(ModJobs.stoneSmeltery.get()), () -> CraftingModuleView::new)
                                      .addBuildingModuleProducer(FurnaceUserModule::new)
                                      .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, false,
                                        (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                      .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                      .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                      .createBuildingEntry());

        ModBuildings.swineHerder = DEFERRED_REGISTER.register(ModBuildings.SWINE_HERDER_ID, () -> new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutSwineHerder)
                                     .setBuildingProducer(BuildingSwineHerder::new)
                                     .setBuildingViewProducer(() -> EmptyView::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SWINE_HERDER_ID))
                                     .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.swineHerder.get(), Skill.Strength, Skill.Athletics, true, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                     .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
                                                                                          .with(AbstractBuilding.FEEDING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                     .addBuildingModuleProducer(() -> new AnimalHerdingModule(ModJobs.swineHerder.get(), EntityType.PIG, new ItemStack(Items.CARROT, 2)))
                                     .createBuildingEntry());

        ModBuildings.townHall = DEFERRED_REGISTER.register(ModBuildings.TOWNHALL_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutTownHall)
                                  .setBuildingProducer(BuildingTownHall::new)
                                  .setBuildingViewProducer(() -> BuildingTownHall.View::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.TOWNHALL_ID))
                                  .createBuildingEntry());

        ModBuildings.wareHouse = DEFERRED_REGISTER.register(ModBuildings.WAREHOUSE_ID, () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutWareHouse)
                                   .setBuildingProducer(BuildingWareHouse::new)
                                   .setBuildingViewProducer(() -> BuildingWareHouse.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.WAREHOUSE_ID))
                                   .addBuildingModuleProducer(CourierAssignmentModule::new, () -> CourierAssignmentModuleView::new)
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .addBuildingModuleProducer(WarehouseModule::new, () -> WarehouseOptionsModuleView::new)
                                   .createBuildingEntry());

        ModBuildings.postBox = DEFERRED_REGISTER.register(ModBuildings.POSTBOX_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockPostBox)
                                 .setBuildingProducer(PostBox::new)
                                 .setBuildingViewProducer(() -> PostBox.View::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.POSTBOX_ID))
                                 .createBuildingEntry());

        ModBuildings.florist = DEFERRED_REGISTER.register(ModBuildings.FLORIST_ID, () -> new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutFlorist)
                                 .setBuildingProducer(BuildingFlorist::new)
                                 .setBuildingViewProducer(() -> EmptyView::new)
                                 .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FLORIST_ID))
                                 .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.florist.get(), Skill.Dexterity, Skill.Agility, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                 .addBuildingModuleProducer(() -> new ItemListModule(BUILDING_FLOWER_LIST), () -> FloristFlowerListModuleView::new)
                                 .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                 .createBuildingEntry());

        ModBuildings.enchanter = DEFERRED_REGISTER.register(ModBuildings.ENCHANTER_ID, () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutEnchanter)
                                   .setBuildingProducer(BuildingEnchanter::new)
                                   .setBuildingViewProducer(() -> EmptyView::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.ENCHANTER_ID))
                                   .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.enchanter.get(), Skill.Mana, Skill.Knowledge, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                   .addBuildingModuleProducer(() -> new BuildingEnchanter.CraftingModule(ModJobs.enchanter.get()), () -> CraftingModuleView::new)
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .addBuildingModuleProducer(EnchanterStationsModule::new, () -> EnchanterStationsModuleView::new)
                                   .createBuildingEntry());

        ModBuildings.university = DEFERRED_REGISTER.register(ModBuildings.UNIVERSITY_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutUniversity)
                                    .setBuildingProducer(BuildingUniversity::new)
                                    .setBuildingViewProducer(() -> EmptyView::new)
                                    .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.researcher.get(), Skill.Knowledge, Skill.Mana, true, ISchematicProvider::getBuildingLevel), () -> WorkerBuildingModuleView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.UNIVERSITY_ID))
                                    .addBuildingModuleViewProducer(() -> UniversityResearchModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.hospital = DEFERRED_REGISTER.register(ModBuildings.HOSPITAL_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutHospital)
                                  .setBuildingProducer(BuildingHospital::new)
                                  .setBuildingViewProducer(() -> EmptyView::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.HOSPITAL_ID))
                                  .addBuildingModuleProducer(() -> new HospitalAssignmentModule(ModJobs.healer.get(), Skill.Mana, Skill.Knowledge, true, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                  .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                  .createBuildingEntry());

        ModBuildings.stash = DEFERRED_REGISTER.register(ModBuildings.STASH_ID, () -> new BuildingEntry.Builder()
                               .setBuildingBlock(ModBlocks.blockStash)
                               .setBuildingProducer(Stash::new)
                               .setBuildingViewProducer(() -> EmptyView::new)
                               .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.STASH_ID))
                               .createBuildingEntry());

        ModBuildings.school = DEFERRED_REGISTER.register(ModBuildings.SCHOOL_ID, () -> new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutSchool)
                                .setBuildingProducer(BuildingSchool::new)
                                .setBuildingViewProducer(() -> EmptyView::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SCHOOL_ID))
                                .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.teacher.get(), Skill.Knowledge, Skill.Mana, true, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                .addBuildingModuleProducer(() -> new ChildrenBuildingModule(ModJobs.pupil.get(), Skill.Knowledge, Skill.Mana, true, (b) -> 2 * b.getBuildingLevel()), () -> PupilBuildingModuleView::new)
                                .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                .createBuildingEntry());

        ModBuildings.glassblower = DEFERRED_REGISTER.register(ModBuildings.GLASSBLOWER_ID, () -> new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutGlassblower)
                                     .setBuildingProducer(BuildingGlassblower::new)
                                     .setBuildingViewProducer(() -> EmptyView::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.GLASSBLOWER_ID))
                                     .addBuildingModuleProducer(() -> new BuildingGlassblower.CraftingModule(ModJobs.glassblower.get()), () -> CraftingModuleView::new)
                                     .addBuildingModuleProducer(() -> new BuildingGlassblower.SmeltingModule(ModJobs.glassblower.get()), () -> CraftingModuleView::new)
                                     .addBuildingModuleProducer(() -> new BuildingGlassblower.DOCraftingModule(ModJobs.glassblower.get()), () -> () -> new DOCraftingModuleView(BuildingGlassblower.DOCraftingModule::getStaticIngredientValidator))
                                     .addBuildingModuleProducer(FurnaceUserModule::new)
                                     .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.glassblower.get(), Skill.Creativity, Skill.Focus, false, (b) -> 1, Skill.Focus, Skill.Creativity), () -> WorkerBuildingModuleView::new)
                                     .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, false,
                                       (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                                     .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                     .createBuildingEntry());

        ModBuildings.dyer = DEFERRED_REGISTER.register(ModBuildings.DYER_ID, () -> new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutDyer)
                              .setBuildingProducer(BuildingDyer::new)
                              .setBuildingViewProducer(() -> EmptyView::new)
                              .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.DYER_ID))
                              .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.dyer.get(), Skill.Creativity, Skill.Dexterity, false, (b) -> 1, Skill.Dexterity, Skill.Creativity), () -> WorkerBuildingModuleView::new)
                              .addBuildingModuleProducer(() -> new BuildingDyer.CraftingModule(ModJobs.dyer.get()), () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(() -> new BuildingDyer.SmeltingModule(ModJobs.dyer.get()), () -> CraftingModuleView::new)
                              .addBuildingModuleProducer(FurnaceUserModule::new)
                              .addBuildingModuleProducer(() -> new ItemListModule(FUEL_LIST), () -> () -> new ItemListModuleView(FUEL_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_BURNABLE, false,
                                (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getFuel()))
                              .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                              .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                              .createBuildingEntry());

        ModBuildings.fletcher = DEFERRED_REGISTER.register(ModBuildings.FLETCHER_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutFletcher)
                                  .setBuildingProducer(BuildingFletcher::new)
                                  .setBuildingViewProducer(() -> EmptyView::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.FLETCHER_ID))
                                  .addBuildingModuleProducer(() -> new BuildingFletcher.CraftingModule(ModJobs.fletcher.get()), () -> CraftingModuleView::new)
                                  .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.fletcher.get(), Skill.Dexterity, Skill.Creativity, true, (b) -> 1, Skill.Creativity, Skill.Dexterity), () -> WorkerBuildingModuleView::new)
                                  .addBuildingModuleProducer(() -> new BuildingFletcher.DOCraftingModule(ModJobs.fletcher.get()), () -> () -> new DOCraftingModuleView(BuildingFletcher.DOCraftingModule::getStaticIngredientValidator))
                                  .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                  .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                  .createBuildingEntry());

        ModBuildings.tavern = DEFERRED_REGISTER.register(ModBuildings.TAVERN_ID, () -> new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutTavern)
                                .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, "tavern", 3))
                                .setBuildingViewProducer(() -> TavernBuildingModule.View::new)
                                .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.TAVERN_ID))
                                .addBuildingModuleProducer(BedHandlingModule::new)
                                .addBuildingModuleProducer(TavernLivingBuildingModule::new)
                                .addBuildingModuleProducer(TavernBuildingModule::new)
                                .createBuildingEntry());

        ModBuildings.mechanic = DEFERRED_REGISTER.register(ModBuildings.MECHANIC_ID, () -> new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutMechanic)
                                  .setBuildingProducer(BuildingMechanic::new)
                                  .setBuildingViewProducer(() -> EmptyView::new)
                                  .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MECHANIC_ID))
                                  .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.mechanic.get(), Skill.Knowledge, Skill.Agility, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                  .addBuildingModuleProducer(() -> new BuildingMechanic.CraftingModule(ModJobs.mechanic.get()), () -> CraftingModuleView::new)
                                  .addBuildingModuleProducer(() -> new BuildingMechanic.DOCraftingModule(ModJobs.mechanic.get()), () -> () -> new DOCraftingModuleView(BuildingMechanic.DOCraftingModule::getStaticIngredientValidator))
                                  .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                  .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                  .createBuildingEntry());

        ModBuildings.plantation = DEFERRED_REGISTER.register(ModBuildings.PLANTATION_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutPlantation)
                                    .setBuildingProducer(BuildingPlantation::new)
                                    .setBuildingViewProducer(() -> EmptyView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.PLANTATION_ID))
                                    .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.planter.get(), Skill.Agility, Skill.Dexterity, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingPlantation.CraftingModule(ModJobs.planter.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule()
                                                                       .with(BuildingPlantation.MODE, new PlantationSetting(
                                                                         Items.SUGAR_CANE.getDescriptionId(),
                                                                         Items.CACTUS.getDescriptionId(),
                                                                         Items.BAMBOO.getDescriptionId(),
                                                                         PlantationSetting.SUGAR_CANE_AND_CACTUS,
                                                                         PlantationSetting.CACTUS_AND_BAMBOO,
                                                                         PlantationSetting.BAMBOO_AND_SUGAR_CANE))
                                                                       .with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.rabbitHutch = DEFERRED_REGISTER.register(ModBuildings.RABBIT_ID, () -> new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutRabbitHutch)
                                     .setBuildingProducer(BuildingRabbitHutch::new)
                                     .setBuildingViewProducer(() -> EmptyView::new)
                                     .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.RABBIT_ID))
                                     .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.rabbitHerder.get(), Skill.Agility, Skill.Athletics, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                     .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                     .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractBuilding.BREEDING, new BoolSetting(true))
                                                                                          .with(AbstractBuilding.FEEDING, new BoolSetting(true)), () -> SettingsModuleView::new)
                                     .addBuildingModuleProducer(() -> new AnimalHerdingModule(ModJobs.rabbitHerder.get(), EntityType.RABBIT, new ItemStack(Items.CARROT, 2)))
                                     .createBuildingEntry());

        //todo we want two here, one custom for the concrete placement, and one crafting for the normal crafting of the powder.
        ModBuildings.concreteMixer = DEFERRED_REGISTER.register(ModBuildings.CONCRETE_ID, () -> new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutConcreteMixer)
                                       .setBuildingProducer(BuildingConcreteMixer::new)
                                       .setBuildingViewProducer(() -> EmptyView::new)
                                       .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.CONCRETE_ID))
                                       .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.concreteMixer.get(), Skill.Stamina, Skill.Dexterity, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                       .addBuildingModuleProducer(() -> new BuildingConcreteMixer.CraftingModule(ModJobs.concreteMixer.get()), () -> CraftingModuleView::new)
                                       .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                       .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting()), () -> SettingsModuleView::new)
                                       .createBuildingEntry());

        ModBuildings.beekeeper = DEFERRED_REGISTER.register(ModBuildings.BEEKEEPER_ID, () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutBeekeeper)
                                   .setBuildingProducer(BuildingBeekeeper::new)
                                   .setBuildingViewProducer(() -> BuildingBeekeeper.View::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.BEEKEEPER_ID))
                                   .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                   .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.beekeeper.get(), Skill.Dexterity, Skill.Adaptability, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                   .addBuildingModuleProducer(() -> new SettingsModule()
                                                                      .with(AbstractBuilding.BREEDING, new BoolSetting(true))
                                                                      .with(BuildingBeekeeper.MODE, new BeekeeperCollectionSetting(BuildingBeekeeper.HONEYCOMB, BuildingBeekeeper.HONEY, BuildingBeekeeper.BOTH)), () -> SettingsModuleView::new)
                                   .addBuildingModuleProducer(() -> new ItemListModule(BUILDING_FLOWER_LIST),  () -> () -> new ItemListModuleView(BUILDING_FLOWER_LIST, RequestSystemTranslationConstants.REQUEST_TYPE_FLOWERS, false,
                                     (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getImmutableFlowers()))
                                   .addBuildingModuleViewProducer(() -> () -> new ToolModuleView(ModItems.scepterBeekeeper))
                                   .addBuildingModuleProducer(BuildingBeekeeper.HerdingModule::new)
                                   .createBuildingEntry());

        ModBuildings.mysticalSite = DEFERRED_REGISTER.register( ModBuildings.MYSTICAL_SITE_ID, () -> new BuildingEntry.Builder()
                                      .setBuildingBlock(ModBlocks.blockHutMysticalSite)
                                      .setBuildingProducer(BuildingMysticalSite::new)
                                      .setBuildingViewProducer(() -> BuildingMysticalSite.View::new)
                                      .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MYSTICAL_SITE_ID))
                                      .createBuildingEntry());


        ModBuildings.graveyard = DEFERRED_REGISTER.register(ModBuildings.GRAVEYARD_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutGraveyard)
                                    .setBuildingProducer(BuildingGraveyard::new)
                                    .setBuildingViewProducer(() -> EmptyView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.GRAVEYARD_ID))
                                    .addBuildingModuleProducer(GraveyardManagementModule::new, () -> GraveyardManagementModuleView::new)
                                    .addBuildingModuleProducer(() -> new WorkerBuildingModule(ModJobs.undertaker.get(), Skill.Strength, Skill.Mana, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.netherWorker = DEFERRED_REGISTER.register(ModBuildings.NETHERWORKER_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutNetherWorker)
                                    .setBuildingProducer(BuildingNetherWorker::new)
                                    .setBuildingViewProducer(() -> EmptyView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.NETHERWORKER_ID))
                                    .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.netherworker.get(), Skill.Adaptability, Skill.Strength, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingNetherWorker.CraftingModule(ModJobs.netherworker.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleProducer(MinimumStockModule::new, () -> MinimumStockModuleView::new)
                                    .addBuildingModuleProducer(() -> new SettingsModule().with(AbstractCraftingBuildingModule.RECIPE_MODE, new CrafterRecipeSetting())
                                                                        .with(BuildingNetherWorker.CLOSE_PORTAL, new BoolSetting(true))
                                                                        , () -> SettingsModuleView::new)
                                    .addBuildingModuleProducer(() -> new ItemListModule(FOOD_EXCLUSION_LIST).onResetToDefaults(BuildingNetherWorker::onResetFoodExclusionList), () -> () -> new ItemListModuleView(FOOD_EXCLUSION_LIST, RequestSystemTranslationConstants.REQUESTS_TYPE_FOOD, true,
                                        (buildingView) -> IColonyManager.getInstance().getCompatibilityManager().getEdibles(buildingView.getBuildingLevel() - 1)))
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .addBuildingModuleProducer(() -> new ExpeditionLogModule(ResearchConstants.NETHER_LOG), () -> ExpeditionLogModuleView::new)
                                    .createBuildingEntry());

        ModBuildings.simpleQuarry = DEFERRED_REGISTER.register(ModBuildings.SIMPLE_QUARRY_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockSimpleQuarry)
                                    .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, ModBuildings.SIMPLE_QUARRY_ID, 1)).setBuildingViewProducer(() -> EmptyView::new)
                                    .addBuildingModuleProducer(() -> new QuarryModule(32), () -> MinerAssignmentModuleView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.SIMPLE_QUARRY_ID))
                                    .createBuildingEntry());

        ModBuildings.mediumQuarry = DEFERRED_REGISTER.register(ModBuildings.MEDIUM_QUARRY_ID, () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockMediumQuarry)
                                   .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, ModBuildings.MEDIUM_QUARRY_ID, 1)).setBuildingViewProducer(() -> EmptyView::new)
                                   .addBuildingModuleProducer(() -> new QuarryModule(64), () -> MinerAssignmentModuleView::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.MEDIUM_QUARRY_ID))
                                   .createBuildingEntry());

        /*ModBuildings.largeQuarry = DEFERRED_REGISTER.register( , () -> new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockLargeQuarry)
                                   .setBuildingProducer((colony, blockPos) -> new DefaultBuildingInstance(colony, blockPos, "largequarry", 1)).setBuildingViewProducer(() -> EmptyView::new)
                                   .addBuildingModuleProducer(QuarryModule::new, () -> MinerAssignmentModuleView::new)
                                   .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.LARGE_QUARRY_ID))
                                   .createBuildingEntry());*/

        ModBuildings.alchemist = DEFERRED_REGISTER.register(ModBuildings.ALCHEMIST_ID, () -> new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutAlchemist)
                                    .setBuildingProducer(BuildingAlchemist::new).setBuildingViewProducer(() -> EmptyView::new)
                                    .setRegistryName(new ResourceLocation(Constants.MOD_ID, ModBuildings.ALCHEMIST_ID))
                                    .addBuildingModuleProducer(() -> new CraftingWorkerBuildingModule(ModJobs.alchemist.get(), Skill.Dexterity, Skill.Mana, false, (b) -> 1), () -> WorkerBuildingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingAlchemist.CraftingModule(ModJobs.alchemist.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleProducer(() -> new BuildingAlchemist.BrewingModule(ModJobs.alchemist.get()), () -> CraftingModuleView::new)
                                    .addBuildingModuleViewProducer(() -> CrafterTaskModuleView::new)
                                    .createBuildingEntry());
    }
}
