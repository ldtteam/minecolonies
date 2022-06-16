package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.colony.jobs.views.CrafterJobView;
import com.minecolonies.coremod.colony.jobs.views.DefaultJobView;
import com.minecolonies.coremod.colony.jobs.views.DmanJobView;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

public final class ModJobsInitializer
{

    private ModJobsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModJobsInitializer but this is a Utility class.");
    }

    @SuppressWarnings("PMD.ExcessiveMethodLength")
    public static void init(final RegisterEvent event)
    {
        final IForgeRegistry<JobEntry> reg = event.getForgeRegistry();

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

        ModJobs.cookassistant = new JobEntry.Builder()
          .setJobProducer(JobCookAssistant::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.COOKASSISTANT_ID)
          .createJobEntry();

        ModJobs.netherworker = new JobEntry.Builder()
                              .setJobProducer(JobNetherWorker::new)
                              .setJobViewProducer(() -> CrafterJobView::new)
                              .setRegistryName(ModJobs.NETHERWORKER_ID)
                              .createJobEntry();

        ModJobs.quarrier = new JobEntry.Builder()
          .setJobProducer(JobQuarrier::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.QUARRY_MINER_ID)
          .createJobEntry();

        ModJobs.druid = new JobEntry.Builder()
          .setJobProducer(JobDruid::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.DRUID_ID)
          .createJobEntry();

        ModJobs.alchemist = new JobEntry.Builder()
          .setJobProducer(JobAlchemist::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.ALCHEMIST_ID)
          .createJobEntry();

        register(reg, ModJobs.placeHolder);
        register(reg, ModJobs.builder);
        register(reg, ModJobs.delivery);
        register(reg, ModJobs.miner);
        register(reg, ModJobs.lumberjack);
        register(reg, ModJobs.farmer);
        register(reg, ModJobs.undertaker);
        register(reg, ModJobs.fisherman);
        register(reg, ModJobs.baker);
        register(reg, ModJobs.cook);
        register(reg, ModJobs.shepherd);
        register(reg, ModJobs.cowboy);
        register(reg, ModJobs.swineHerder);
        register(reg, ModJobs.chickenHerder);
        register(reg, ModJobs.smelter);
        register(reg, ModJobs.ranger);
        register(reg, ModJobs.knight);
        register(reg, ModJobs.composter);
        register(reg, ModJobs.student);
        register(reg, ModJobs.archer);
        register(reg, ModJobs.combat);
        register(reg, ModJobs.sawmill);
        register(reg, ModJobs.blacksmith);
        register(reg, ModJobs.stoneMason);
        register(reg, ModJobs.stoneSmeltery);
        register(reg, ModJobs.crusher);
        register(reg, ModJobs.sifter);
        register(reg, ModJobs.florist);
        register(reg, ModJobs.enchanter);
        register(reg, ModJobs.researcher);
        register(reg, ModJobs.healer);
        register(reg, ModJobs.pupil);
        register(reg, ModJobs.teacher);
        register(reg, ModJobs.glassblower);
        register(reg, ModJobs.dyer);
        register(reg, ModJobs.fletcher);
        register(reg, ModJobs.mechanic);
        register(reg, ModJobs.planter);
        register(reg, ModJobs.concreteMixer);
        register(reg, ModJobs.rabbitHerder);
        register(reg, ModJobs.beekeeper);
        register(reg, ModJobs.cookassistant);
        register(reg, ModJobs.netherworker);
        register(reg, ModJobs.quarrier);
        register(reg, ModJobs.druid);
        register(reg, ModJobs.alchemist);
    }

    /**
     * Register the building entry.
     * @param reg the registry to register it to.
     * @param entry the entry to register.
     */
    private static void register(final IForgeRegistry<JobEntry> reg, final JobEntry entry)
    {
        reg.register(entry.getKey(), entry);
    }
}
