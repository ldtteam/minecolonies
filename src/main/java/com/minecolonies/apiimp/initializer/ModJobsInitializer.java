package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.colony.jobs.views.CrafterJobView;
import com.minecolonies.coremod.colony.jobs.views.DefaultJobView;
import com.minecolonies.coremod.colony.jobs.views.DmanJobView;
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
                                .setJobViewProducer(() -> DefaultJobView::new)
                                .setRegistryName(ModJobs.PLACEHOLDER_ID)
                                .createJobEntry();

        ModJobs.builder = new JobEntry.Builder()
                            .setJobProducer(JobBuilder::new)
                            .setJobViewProducer(() -> DefaultJobView::new)
                            .setRegistryName(ModJobs.BUILDER_ID)
                            .createJobEntry();

        ModJobs.delivery = new JobEntry.Builder()
                             .setJobProducer(JobDeliveryman::new)
                             .setJobViewProducer(() -> DmanJobView::new)
                             .setRegistryName(ModJobs.DELIVERY_ID)
                             .createJobEntry();

        ModJobs.miner = new JobEntry.Builder()
                          .setJobProducer(JobMiner::new)
                          .setJobViewProducer(() -> DefaultJobView::new)
                          .setRegistryName(ModJobs.MINER_ID)
                          .createJobEntry();

        ModJobs.lumberjack = new JobEntry.Builder()
                               .setJobProducer(JobLumberjack::new)
                               .setJobViewProducer(() -> CrafterJobView::new)
                               .setRegistryName(ModJobs.LUMBERJACK_ID)
                               .createJobEntry();

        ModJobs.farmer = new JobEntry.Builder()
                           .setJobProducer(JobFarmer::new)
                           .setJobViewProducer(() -> CrafterJobView::new)
                           .setRegistryName(ModJobs.FARMER_ID)
                           .createJobEntry();

        ModJobs.undertaker = new JobEntry.Builder()
                           .setJobProducer(JobUndertaker::new)
                           .setJobViewProducer(() -> CrafterJobView::new)
                           .setRegistryName(ModJobs.UNDERTAKER_ID)
                           .createJobEntry();

        ModJobs.fisherman = new JobEntry.Builder()
                              .setJobProducer(JobFisherman::new)
                              .setJobViewProducer(() -> DefaultJobView::new)
                              .setRegistryName(ModJobs.FISHERMAN_ID)
                              .createJobEntry();

        ModJobs.baker = new JobEntry.Builder()
                          .setJobProducer(JobBaker::new)
                          .setJobViewProducer(() -> CrafterJobView::new)
                          .setRegistryName(ModJobs.BAKER_ID)
                          .createJobEntry();

        ModJobs.cook = new JobEntry.Builder()
                         .setJobProducer(JobCook::new)
                         .setJobViewProducer(() -> DefaultJobView::new)
                         .setRegistryName(ModJobs.COOK_ID)
                         .createJobEntry();

        ModJobs.shepherd = new JobEntry.Builder()
                             .setJobProducer(JobShepherd::new)
                             .setJobViewProducer(() -> DefaultJobView::new)
                             .setRegistryName(ModJobs.SHEPHERD_ID)
                             .createJobEntry();

        ModJobs.cowboy = new JobEntry.Builder()
                           .setJobProducer(JobCowboy::new)
                           .setJobViewProducer(() -> DefaultJobView::new)
                           .setRegistryName(ModJobs.COWBOY_ID)
                           .createJobEntry();

        ModJobs.swineHerder = new JobEntry.Builder()
                                .setJobProducer(JobSwineHerder::new)
                                .setJobViewProducer(() -> DefaultJobView::new)
                                .setRegistryName(ModJobs.SWINE_HERDER_ID)
                                .createJobEntry();

        ModJobs.chickenHerder = new JobEntry.Builder()
                                  .setJobProducer(JobChickenHerder::new)
                                  .setJobViewProducer(() -> DefaultJobView::new)
                                  .setRegistryName(ModJobs.CHICKEN_HERDER_ID)
                                  .createJobEntry();

        ModJobs.smelter = new JobEntry.Builder()
                            .setJobProducer(JobSmelter::new)
                            .setJobViewProducer(() -> DefaultJobView::new)
                            .setRegistryName(ModJobs.SMELTER_ID)
                            .createJobEntry();

        ModJobs.ranger = new JobEntry.Builder()
                           .setJobProducer(JobRanger::new)
                           .setJobViewProducer(() -> DefaultJobView::new)
                           .setRegistryName(ModJobs.RANGER_ID)
                           .createJobEntry();

        ModJobs.knight = new JobEntry.Builder()
                           .setJobProducer(JobKnight::new)
                           .setJobViewProducer(() -> DefaultJobView::new)
                           .setRegistryName(ModJobs.KNIGHT_ID)
                           .createJobEntry();

        ModJobs.composter = new JobEntry.Builder()
                              .setJobProducer(JobComposter::new)
                              .setJobViewProducer(() -> DefaultJobView::new)
                              .setRegistryName(ModJobs.COMPOSTER_ID)
                              .createJobEntry();

        ModJobs.student = new JobEntry.Builder()
                            .setJobProducer(JobStudent::new)
                            .setJobViewProducer(() -> DefaultJobView::new)
                            .setRegistryName(ModJobs.STUDENT_ID)
                            .createJobEntry();

        ModJobs.archer = new JobEntry.Builder()
                           .setJobProducer(JobArcherTraining::new)
                           .setJobViewProducer(() -> DefaultJobView::new)
                           .setRegistryName(ModJobs.ARCHER_ID)
                           .createJobEntry();

        ModJobs.combat = new JobEntry.Builder()
                           .setJobProducer(JobCombatTraining::new)
                           .setJobViewProducer(() -> DefaultJobView::new)
                           .setRegistryName(ModJobs.COMBAT_ID)
                           .createJobEntry();

        ModJobs.sawmill = new JobEntry.Builder()
                            .setJobProducer(JobSawmill::new)
                            .setJobViewProducer(() -> CrafterJobView::new)
                            .setRegistryName(ModJobs.SAWMILL_ID)
                            .createJobEntry();

        ModJobs.blacksmith = new JobEntry.Builder()
                               .setJobProducer(JobBlacksmith::new)
                               .setJobViewProducer(() -> CrafterJobView::new)
                               .setRegistryName(ModJobs.BLACKSMITH_ID)
                               .createJobEntry();

        ModJobs.stoneMason = new JobEntry.Builder()
                               .setJobProducer(JobStonemason::new)
                               .setJobViewProducer(() -> CrafterJobView::new)
                               .setRegistryName(ModJobs.STONEMASON_ID)
                               .createJobEntry();

        ModJobs.stoneSmeltery = new JobEntry.Builder()
                                  .setJobProducer(JobStoneSmeltery::new)
                                  .setJobViewProducer(() -> CrafterJobView::new)
                                  .setRegistryName(ModJobs.STONE_SMELTERY_ID)
                                  .createJobEntry();

        ModJobs.crusher = new JobEntry.Builder()
                            .setJobProducer(JobCrusher::new)
                            .setJobViewProducer(() -> CrafterJobView::new)
                            .setRegistryName(ModJobs.CRUSHER_ID)
                            .createJobEntry();

        ModJobs.sifter = new JobEntry.Builder()
                           .setJobProducer(JobSifter::new)
                           .setJobViewProducer(() -> CrafterJobView::new)
                           .setRegistryName(ModJobs.SIFTER_ID)
                           .createJobEntry();

        ModJobs.florist = new JobEntry.Builder()
                            .setJobProducer(JobFlorist::new)
                            .setJobViewProducer(() -> DefaultJobView::new)
                            .setRegistryName(ModJobs.FLORIST_ID)
                            .createJobEntry();

        ModJobs.enchanter = new JobEntry.Builder()
                              .setJobProducer(JobEnchanter::new)
                              .setJobViewProducer(() -> CrafterJobView::new)
                              .setRegistryName(ModJobs.ENCHANTER_ID)
                              .createJobEntry();

        ModJobs.researcher = new JobEntry.Builder()
                               .setJobProducer(JobResearch::new)
                               .setJobViewProducer(() -> DefaultJobView::new)
                               .setRegistryName(ModJobs.RESEARCHER_ID)
                               .createJobEntry();

        ModJobs.healer = new JobEntry.Builder()
                           .setJobProducer(JobHealer::new)
                           .setJobViewProducer(() -> DefaultJobView::new)
                           .setRegistryName(ModJobs.HEALER_ID)
                           .createJobEntry();

        ModJobs.pupil = new JobEntry.Builder()
                          .setJobProducer(JobPupil::new)
                          .setJobViewProducer(() -> DefaultJobView::new)
                          .setRegistryName(ModJobs.PUPIL_ID)
                          .createJobEntry();

        ModJobs.teacher = new JobEntry.Builder()
                            .setJobProducer(JobTeacher::new)
                            .setJobViewProducer(() -> DefaultJobView::new)
                            .setRegistryName(ModJobs.TEACHER_ID)
                            .createJobEntry();

        ModJobs.glassblower = new JobEntry.Builder()
                                .setJobProducer(JobGlassblower::new)
                                .setJobViewProducer(() -> CrafterJobView::new)
                                .setRegistryName(ModJobs.GLASSBLOWER_ID)
                                .createJobEntry();

        ModJobs.dyer = new JobEntry.Builder()
                         .setJobProducer(JobDyer::new)
                         .setJobViewProducer(() -> CrafterJobView::new)
                         .setRegistryName(ModJobs.DYER_ID)
                         .createJobEntry();

        ModJobs.fletcher = new JobEntry.Builder()
                             .setJobProducer(JobFletcher::new)
                             .setJobViewProducer(() -> CrafterJobView::new)
                             .setRegistryName(ModJobs.FLETCHER_ID)
                             .createJobEntry();

        ModJobs.mechanic = new JobEntry.Builder()
                             .setJobProducer(JobMechanic::new)
                             .setJobViewProducer(() -> CrafterJobView::new)
                             .setRegistryName(ModJobs.MECHANIC_ID)
                             .createJobEntry();

        ModJobs.planter = new JobEntry.Builder()
                            .setJobProducer(JobPlanter::new)
                            .setJobViewProducer(() -> CrafterJobView::new)
                            .setRegistryName(ModJobs.PLANTER_ID)
                            .createJobEntry();

        ModJobs.rabbitHerder = new JobEntry.Builder()
                                 .setJobProducer(JobRabbitHerder::new)
                                 .setJobViewProducer(() -> DefaultJobView::new)
                                 .setRegistryName(ModJobs.RABBIT_ID)
                                 .createJobEntry();

        ModJobs.concreteMixer = new JobEntry.Builder()
                                  .setJobProducer(JobConcreteMixer::new)
                                  .setJobViewProducer(() -> CrafterJobView::new)
                                  .setRegistryName(ModJobs.CONCRETE_ID)
                                  .createJobEntry();

        ModJobs.beekeeper = new JobEntry.Builder()
                              .setJobProducer(JobBeekeeper::new)
                              .setJobViewProducer(() -> DefaultJobView::new)
                              .setRegistryName(ModJobs.BEEKEEPER_ID)
                              .createJobEntry();

        ModJobs.cookassistant= new JobEntry.Builder()
                              .setJobProducer(JobCookAssistant::new)
                              .setJobViewProducer(() -> CrafterJobView::new)
                              .setRegistryName(ModJobs.COOKASSISTANT_ID)
                              .createJobEntry();

        reg.register(ModJobs.placeHolder);
        reg.register(ModJobs.builder);
        reg.register(ModJobs.delivery);
        reg.register(ModJobs.miner);
        reg.register(ModJobs.lumberjack);
        reg.register(ModJobs.farmer);
        reg.register(ModJobs.undertaker);
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
        reg.register(ModJobs.researcher);
        reg.register(ModJobs.healer);
        reg.register(ModJobs.pupil);
        reg.register(ModJobs.teacher);
        reg.register(ModJobs.glassblower);
        reg.register(ModJobs.dyer);
        reg.register(ModJobs.fletcher);
        reg.register(ModJobs.mechanic);
        reg.register(ModJobs.planter);
        reg.register(ModJobs.concreteMixer);
        reg.register(ModJobs.rabbitHerder);
        reg.register(ModJobs.beekeeper);
        reg.register(ModJobs.cookassistant);
    }
}
