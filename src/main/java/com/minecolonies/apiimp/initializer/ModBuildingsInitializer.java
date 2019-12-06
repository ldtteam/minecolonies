package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.coremod.colony.buildings.workerbuildings.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

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
                                 .setRegistryName(new ResourceLocation(ModBuildings.ARCHERY_ID))
                                 .createBuildingEntry();

        ModBuildings.bakery = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutBaker)
                                .setBuildingProducer(BuildingBaker::new)
                                .setBuildingViewProducer(() -> BuildingBaker.View::new)
                                .setRegistryName(new ResourceLocation(ModBuildings.BAKERY_ID))
                                .createBuildingEntry();

        ModBuildings.barracks = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutBarracks)
                                  .setBuildingProducer(BuildingBarracks::new)
                                  .setBuildingViewProducer(() -> BuildingBarracks.View::new)
                                  .setRegistryName(new ResourceLocation(ModBuildings.BARRACKS_ID))
                                  .createBuildingEntry();

        ModBuildings.barracksTower = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutBarracksTower)
                                       .setBuildingProducer(BuildingBarracksTower::new)
                                       .setBuildingViewProducer(() -> BuildingBarracksTower.View::new)
                                       .setRegistryName(new ResourceLocation(ModBuildings.BARRACKS_TOWER_ID))
                                       .createBuildingEntry();

        ModBuildings.blacksmith = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutBlacksmith)
                                    .setBuildingProducer(BuildingBlacksmith::new)
                                    .setBuildingViewProducer(() -> BuildingBlacksmith.View::new)
                                    .setRegistryName(new ResourceLocation(ModBuildings.BLACKSMITH_ID))
                                    .createBuildingEntry();

        ModBuildings.builder = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutBuilder)
                                 .setBuildingProducer(BuildingBuilder::new)
                                 .setBuildingViewProducer(() -> BuildingBuilder.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.BUILDER_ID))
                                 .createBuildingEntry();

        ModBuildings.chickenHerder = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutChickenHerder)
                                       .setBuildingProducer(BuildingChickenHerder::new)
                                       .setBuildingViewProducer(() -> BuildingChickenHerder.View::new)
                                       .setRegistryName(new ResourceLocation(ModBuildings.CHICKENHERDER_ID))
                                       .createBuildingEntry();

        ModBuildings.combatAcademy = new BuildingEntry.Builder()
                                       .setBuildingBlock(ModBlocks.blockHutCombatAcademy)
                                       .setBuildingProducer(BuildingCombatAcademy::new)
                                       .setBuildingViewProducer(() -> BuildingCombatAcademy.View::new)
                                       .setRegistryName(new ResourceLocation(ModBuildings.COMBAT_ACADEMY_ID))
                                       .createBuildingEntry();

        ModBuildings.composter = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutComposter)
                                   .setBuildingProducer(BuildingComposter::new)
                                   .setBuildingViewProducer(() -> BuildingComposter.View::new)
                                   .setRegistryName(new ResourceLocation(ModBuildings.COMPOSTER_ID))
                                   .createBuildingEntry();

        ModBuildings.cook = new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutCook)
                              .setBuildingProducer(BuildingCook::new)
                              .setBuildingViewProducer(() -> BuildingCook.View::new)
                              .setRegistryName(new ResourceLocation(ModBuildings.COOK_ID))
                              .createBuildingEntry();

        ModBuildings.cowboy = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutCowboy)
                                .setBuildingProducer(BuildingCowboy::new)
                                .setBuildingViewProducer(() -> BuildingCowboy.View::new)
                                .setRegistryName(new ResourceLocation(ModBuildings.COWBOY_ID))
                                .createBuildingEntry();

        ModBuildings.crusher = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutCrusher)
                                 .setBuildingProducer(BuildingCrusher::new)
                                 .setBuildingViewProducer(() -> BuildingCrusher.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.CRUSHER_ID))
                                 .createBuildingEntry();

        ModBuildings.deliveryman = new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutDeliveryman)
                                     .setBuildingProducer(BuildingDeliveryman::new)
                                     .setBuildingViewProducer(() -> BuildingDeliveryman.View::new)
                                     .setRegistryName(new ResourceLocation(ModBuildings.DELIVERYMAN_ID))
                                     .createBuildingEntry();

        ModBuildings.farmer = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutFarmer)
                                .setBuildingProducer(BuildingFarmer::new)
                                .setBuildingViewProducer(() -> BuildingFarmer.View::new)
                                .setRegistryName(new ResourceLocation(ModBuildings.FARMER_ID))
                                .createBuildingEntry();

        ModBuildings.fisherman = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutFisherman)
                                   .setBuildingProducer(BuildingFisherman::new)
                                   .setBuildingViewProducer(() -> BuildingFisherman.View::new)
                                   .setRegistryName(new ResourceLocation(ModBuildings.FISHERMAN_ID))
                                   .createBuildingEntry();

        ModBuildings.guardTower = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutGuardTower)
                                    .setBuildingProducer(BuildingGuardTower::new)
                                    .setBuildingViewProducer(() -> BuildingGuardTower.View::new)
                                    .setRegistryName(new ResourceLocation(ModBuildings.GUARD_TOWER_ID))
                                    .createBuildingEntry();

        ModBuildings.home = new BuildingEntry.Builder()
                              .setBuildingBlock(ModBlocks.blockHutHome)
                              .setBuildingProducer(BuildingHome::new)
                              .setBuildingViewProducer(() -> BuildingHome.View::new)
                              .setRegistryName(new ResourceLocation(ModBuildings.HOME_ID))
                              .createBuildingEntry();

        ModBuildings.library = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutLibrary)
                                 .setBuildingProducer(BuildingLibrary::new)
                                 .setBuildingViewProducer(() -> BuildingLibrary.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.LIBRARY_ID))
                                 .createBuildingEntry();

        ModBuildings.lumberjack = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutLumberjack)
                                    .setBuildingProducer(BuildingLumberjack::new)
                                    .setBuildingViewProducer(() -> BuildingLumberjack.View::new)
                                    .setRegistryName(new ResourceLocation(ModBuildings.LUMBERJACK_ID))
                                    .createBuildingEntry();

        ModBuildings.miner = new BuildingEntry.Builder()
                               .setBuildingBlock(ModBlocks.blockHutMiner)
                               .setBuildingProducer(BuildingMiner::new)
                               .setBuildingViewProducer(() -> BuildingMiner.View::new)
                               .setRegistryName(new ResourceLocation(ModBuildings.MINER_ID))
                               .createBuildingEntry();

        ModBuildings.sawmill = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutSawmill)
                                 .setBuildingProducer(BuildingSawmill::new)
                                 .setBuildingViewProducer(() -> BuildingSawmill.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.SAWMILL_ID))
                                 .createBuildingEntry();

        ModBuildings.shepherd = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutShepherd)
                                  .setBuildingProducer(BuildingShepherd::new)
                                  .setBuildingViewProducer(() -> BuildingShepherd.View::new)
                                  .setRegistryName(new ResourceLocation(ModBuildings.SHEPHERD_ID))
                                  .createBuildingEntry();

        ModBuildings.sifter = new BuildingEntry.Builder()
                                .setBuildingBlock(ModBlocks.blockHutSifter)
                                .setBuildingProducer(BuildingSifter::new)
                                .setBuildingViewProducer(() -> BuildingSifter.View::new)
                                .setRegistryName(new ResourceLocation(ModBuildings.SIFTER_ID))
                                .createBuildingEntry();

        ModBuildings.smeltery = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutSmeltery)
                                  .setBuildingProducer(BuildingSmeltery::new)
                                  .setBuildingViewProducer(() -> BuildingSmeltery.View::new)
                                  .setRegistryName(new ResourceLocation(ModBuildings.SMELTERY_ID))
                                  .createBuildingEntry();

        ModBuildings.stoneMason = new BuildingEntry.Builder()
                                    .setBuildingBlock(ModBlocks.blockHutStonemason)
                                    .setBuildingProducer(BuildingStonemason::new)
                                    .setBuildingViewProducer(() -> BuildingStonemason.View::new)
                                    .setRegistryName(new ResourceLocation(ModBuildings.STONE_MASON_ID))
                                    .createBuildingEntry();

        ModBuildings.stoneSmelter = new BuildingEntry.Builder()
                                      .setBuildingBlock(ModBlocks.blockHutStoneSmeltery)
                                      .setBuildingProducer(BuildingStoneSmeltery::new)
                                      .setBuildingViewProducer(() -> BuildingStoneSmeltery.View::new)
                                      .setRegistryName(new ResourceLocation(ModBuildings.STONE_SMELTERY_ID))
                                      .createBuildingEntry();

        ModBuildings.swineHerder = new BuildingEntry.Builder()
                                     .setBuildingBlock(ModBlocks.blockHutSwineHerder)
                                     .setBuildingProducer(BuildingSwineHerder::new)
                                     .setBuildingViewProducer(() -> BuildingSwineHerder.View::new)
                                     .setRegistryName(new ResourceLocation(ModBuildings.SWINE_HERDER_ID))
                                     .createBuildingEntry();

        ModBuildings.townHall = new BuildingEntry.Builder()
                                  .setBuildingBlock(ModBlocks.blockHutTownHall)
                                  .setBuildingProducer(BuildingTownHall::new)
                                  .setBuildingViewProducer(() -> BuildingTownHall.View::new)
                                  .setRegistryName(new ResourceLocation(ModBuildings.TOWNHALL_ID))
                                  .createBuildingEntry();

        ModBuildings.wareHouse = new BuildingEntry.Builder()
                                   .setBuildingBlock(ModBlocks.blockHutWareHouse)
                                   .setBuildingProducer(BuildingWareHouse::new)
                                   .setBuildingViewProducer(() -> BuildingWareHouse.View::new)
                                   .setRegistryName(new ResourceLocation(ModBuildings.WAREHOUSE_ID))
                                   .createBuildingEntry();

        ModBuildings.postBox = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockPostBox)
                                 .setBuildingProducer(PostBox::new)
                                 .setBuildingViewProducer(() -> PostBox.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.POSTBOX_ID))
                                 .createBuildingEntry();

        ModBuildings.florist = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutFlorist)
                                 .setBuildingProducer(BuildingFlorist::new)
                                 .setBuildingViewProducer(() -> BuildingFlorist.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.FLORIST_ID))
                                 .createBuildingEntry();

        ModBuildings.enchanter = new BuildingEntry.Builder()
                                 .setBuildingBlock(ModBlocks.blockHutEnchanter)
                                 .setBuildingProducer(BuildingEnchanter::new)
                                 .setBuildingViewProducer(() -> BuildingEnchanter.View::new)
                                 .setRegistryName(new ResourceLocation(ModBuildings.ENCHANTER_ID))
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
    }
}
