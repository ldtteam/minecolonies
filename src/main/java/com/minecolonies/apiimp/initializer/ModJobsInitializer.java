package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.jobs.*;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class ModJobsInitializer
{

    private ModJobsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModJobsInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegistryEvent.Register<JobEntry> event)
    {
        final IForgeRegistry<JobEntry> reg = event.getRegistry();

        ModJobs.placeHolder = new JobEntry.Builder()
                                .setJobProducer(JobPlaceholder::new)
                                .setRegistryName(ModJobs.PLACEHOLDER_ID)
                                .createJobEntry();

        ModJobs.builder = new JobEntry.Builder()
                            .setJobProducer(JobBuilder::new)
                            .setRegistryName(ModJobs.BUILDER_ID)
                            .createJobEntry();

        ModJobs.delivery = new JobEntry.Builder()
                             .setJobProducer(JobDeliveryman::new)
                             .setRegistryName(ModJobs.DELIVERY_ID)
                             .createJobEntry();

        ModJobs.miner = new JobEntry.Builder()
                          .setJobProducer(JobMiner::new)
                          .setRegistryName(ModJobs.MINER_ID)
                          .createJobEntry();

        ModJobs.lumberjack = new JobEntry.Builder()
                               .setJobProducer(JobLumberjack::new)
                               .setRegistryName(ModJobs.LUMBERJACK_ID)
                               .createJobEntry();

        ModJobs.farmer = new JobEntry.Builder()
                           .setJobProducer(JobFarmer::new)
                           .setRegistryName(ModJobs.FARMER_ID)
                           .createJobEntry();

        ModJobs.fisherman = new JobEntry.Builder()
                              .setJobProducer(JobFisherman::new)
                              .setRegistryName(ModJobs.FISHERMAN_ID)
                              .createJobEntry();

        ModJobs.baker = new JobEntry.Builder()
                          .setJobProducer(JobBaker::new)
                          .setRegistryName(ModJobs.BAKER_ID)
                          .createJobEntry();

        ModJobs.cook = new JobEntry.Builder()
                         .setJobProducer(JobCook::new)
                         .setRegistryName(ModJobs.COOK_ID)
                         .createJobEntry();

        ModJobs.shepherd = new JobEntry.Builder()
                             .setJobProducer(JobShepherd::new)
                             .setRegistryName(ModJobs.SHEPHERD_ID)
                             .createJobEntry();

        ModJobs.cowboy = new JobEntry.Builder()
                           .setJobProducer(JobCowboy::new)
                           .setRegistryName(ModJobs.COWBOY_ID)
                           .createJobEntry();

        ModJobs.swineHerder = new JobEntry.Builder()
                                .setJobProducer(JobSwineHerder::new)
                                .setRegistryName(ModJobs.SWINE_HERDER_ID)
                                .createJobEntry();

        ModJobs.chickenHerder = new JobEntry.Builder()
                                  .setJobProducer(JobChickenHerder::new)
                                  .setRegistryName(ModJobs.CHICKEN_HERDER_ID)
                                  .createJobEntry();

        ModJobs.smelter = new JobEntry.Builder()
                            .setJobProducer(JobSmelter::new)
                            .setRegistryName(ModJobs.SMELTER_ID)
                            .createJobEntry();

        ModJobs.ranger = new JobEntry.Builder()
                           .setJobProducer(JobRanger::new)
                           .setRegistryName(ModJobs.RANGER_ID)
                           .createJobEntry();

        ModJobs.knight = new JobEntry.Builder()
                           .setJobProducer(JobKnight::new)
                           .setRegistryName(ModJobs.KNIGHT_ID)
                           .createJobEntry();

        ModJobs.composter = new JobEntry.Builder()
                              .setJobProducer(JobComposter::new)
                              .setRegistryName(ModJobs.COMPOSTER_ID)
                              .createJobEntry();

        ModJobs.student = new JobEntry.Builder()
                            .setJobProducer(JobStudent::new)
                            .setRegistryName(ModJobs.STUDENT_ID)
                            .createJobEntry();

        ModJobs.archer = new JobEntry.Builder()
                           .setJobProducer(JobArcherTraining::new)
                           .setRegistryName(ModJobs.ARCHER_ID)
                           .createJobEntry();

        ModJobs.combat = new JobEntry.Builder()
                           .setJobProducer(JobCombatTraining::new)
                           .setRegistryName(ModJobs.COMBAT_ID)
                           .createJobEntry();

        ModJobs.sawmill = new JobEntry.Builder()
                            .setJobProducer(JobSawmill::new)
                            .setRegistryName(ModJobs.SAWMILL_ID)
                            .createJobEntry();

        ModJobs.blacksmith = new JobEntry.Builder()
                               .setJobProducer(JobBlacksmith::new)
                               .setRegistryName(ModJobs.BLACKSMITH_ID)
                               .createJobEntry();

        ModJobs.stoneMason = new JobEntry.Builder()
                               .setJobProducer(JobStonemason::new)
                               .setRegistryName(ModJobs.STONEMASON_ID)
                               .createJobEntry();

        ModJobs.stoneSmeltery = new JobEntry.Builder()
                                  .setJobProducer(JobStoneSmeltery::new)
                                  .setRegistryName(ModJobs.STONE_SMELTERY_ID)
                                  .createJobEntry();

        ModJobs.crusher = new JobEntry.Builder()
                            .setJobProducer(JobCrusher::new)
                            .setRegistryName(ModJobs.CRUSHER_ID)
                            .createJobEntry();

        ModJobs.sifter = new JobEntry.Builder()
                           .setJobProducer(JobSifter::new)
                           .setRegistryName(ModJobs.SIFTER_ID)
                           .createJobEntry();

        ModJobs.florist = new JobEntry.Builder()
                           .setJobProducer(JobFlorist::new)
                           .setRegistryName(ModJobs.FLORIST_ID)
                           .createJobEntry();

        ModJobs.enchanter = new JobEntry.Builder()
                            .setJobProducer(JobEnchanter::new)
                            .setRegistryName(ModJobs.ENCHANTER_ID)
                            .createJobEntry();


        reg.register(ModJobs.placeHolder);
        reg.register(ModJobs.builder);
        reg.register(ModJobs.delivery);
        reg.register(ModJobs.miner);
        reg.register(ModJobs.lumberjack);
        reg.register(ModJobs.farmer);
        reg.register(ModJobs.fisherman);
        reg.register(ModJobs.baker);
        reg.register(ModJobs.cook);
        reg.register(ModJobs.shepherd);
        reg.register(ModJobs.cowboy);
        reg.register(ModJobs.swineHerder);
        reg.register(ModJobs.chickenHerder);
        reg.register(ModJobs.smelter);
        reg.register(ModJobs.ranger);
        reg.register(ModJobs.knight);
        reg.register(ModJobs.composter);
        reg.register(ModJobs.student);
        reg.register(ModJobs.archer);
        reg.register(ModJobs.combat);
        reg.register(ModJobs.sawmill);
        reg.register(ModJobs.blacksmith);
        reg.register(ModJobs.stoneMason);
        reg.register(ModJobs.stoneSmeltery);
        reg.register(ModJobs.crusher);
        reg.register(ModJobs.sifter);
        reg.register(ModJobs.florist);
        reg.register(ModJobs.enchanter);

    }
}
