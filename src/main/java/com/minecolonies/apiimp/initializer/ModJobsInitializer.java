package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.apiimp.CommonMinecoloniesAPIImpl;
import com.minecolonies.core.colony.jobs.*;
import com.minecolonies.core.colony.jobs.views.CrafterJobView;
import com.minecolonies.core.colony.jobs.views.DefaultJobView;
import com.minecolonies.core.colony.jobs.views.DmanJobView;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ModJobsInitializer
{
    public final static DeferredRegister<JobEntry> DEFERRED_REGISTER = DeferredRegister.create(CommonMinecoloniesAPIImpl.JOBS, Constants.MOD_ID);

    private ModJobsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModJobsInitializer but this is a Utility class.");
    }

    static
    {
        ModJobs.placeHolder = register(DEFERRED_REGISTER, ModJobs.PLACEHOLDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobPlaceholder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.PLACEHOLDER_ID)
          .createJobEntry());

        ModJobs.builder = register(DEFERRED_REGISTER, ModJobs.BUILDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBuilder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.BUILDER_ID)
          .createJobEntry());

        ModJobs.delivery = register(DEFERRED_REGISTER, ModJobs.DELIVERY_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobDeliveryman::new)
          .setJobViewProducer(() -> DmanJobView::new)
          .setRegistryName(ModJobs.DELIVERY_ID)
          .createJobEntry());

        ModJobs.miner = register(DEFERRED_REGISTER, ModJobs.MINER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobMiner::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.MINER_ID)
          .createJobEntry());

        ModJobs.lumberjack = register(DEFERRED_REGISTER, ModJobs.LUMBERJACK_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobLumberjack::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.LUMBERJACK_ID)
          .createJobEntry());

        ModJobs.farmer = register(DEFERRED_REGISTER, ModJobs.FARMER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFarmer::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.FARMER_ID)
          .createJobEntry());

        ModJobs.undertaker = register(DEFERRED_REGISTER, ModJobs.UNDERTAKER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobUndertaker::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.UNDERTAKER_ID)
          .createJobEntry());

        ModJobs.fisherman = register(DEFERRED_REGISTER, ModJobs.FISHERMAN_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFisherman::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.FISHERMAN_ID)
          .createJobEntry());

        ModJobs.baker = register(DEFERRED_REGISTER, ModJobs.BAKER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBaker::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.BAKER_ID)
          .createJobEntry());

        ModJobs.cook = register(DEFERRED_REGISTER, ModJobs.COOK_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCook::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COOK_ID)
          .createJobEntry());

        ModJobs.shepherd = register(DEFERRED_REGISTER, ModJobs.SHEPHERD_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobShepherd::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.SHEPHERD_ID)
          .createJobEntry());

        ModJobs.cowboy = register(DEFERRED_REGISTER, ModJobs.COWBOY_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCowboy::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COWBOY_ID)
          .createJobEntry());

        ModJobs.swineHerder = register(DEFERRED_REGISTER, ModJobs.SWINE_HERDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSwineHerder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.SWINE_HERDER_ID)
          .createJobEntry());

        ModJobs.chickenHerder = register(DEFERRED_REGISTER, ModJobs.CHICKEN_HERDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobChickenHerder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.CHICKEN_HERDER_ID)
          .createJobEntry());

        ModJobs.smelter = register(DEFERRED_REGISTER, ModJobs.SMELTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSmelter::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.SMELTER_ID)
          .createJobEntry());

        ModJobs.archer = register(DEFERRED_REGISTER, ModJobs.ARCHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobRanger::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.ARCHER_ID)
          .createJobEntry());

        ModJobs.knight = register(DEFERRED_REGISTER, ModJobs.KNIGHT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobKnight::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.KNIGHT_ID)
          .createJobEntry());

        ModJobs.composter = register(DEFERRED_REGISTER, ModJobs.COMPOSTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobComposter::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COMPOSTER_ID)
          .createJobEntry());

        ModJobs.student = register(DEFERRED_REGISTER, ModJobs.STUDENT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobStudent::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.STUDENT_ID)
          .createJobEntry());

        ModJobs.archerInTraining = register(DEFERRED_REGISTER, ModJobs.ARCHER_TRAINING_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobArcherTraining::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.ARCHER_TRAINING_ID)
          .createJobEntry());

        ModJobs.knightInTraining = register(DEFERRED_REGISTER, ModJobs.KNIGHT_TRAINING_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCombatTraining::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.KNIGHT_TRAINING_ID)
          .createJobEntry());

        ModJobs.sawmill = register(DEFERRED_REGISTER, ModJobs.SAWMILL_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSawmill::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.SAWMILL_ID)
          .createJobEntry());

        ModJobs.blacksmith = register(DEFERRED_REGISTER, ModJobs.BLACKSMITH_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBlacksmith::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.BLACKSMITH_ID)
          .createJobEntry());

        ModJobs.stoneMason = register(DEFERRED_REGISTER, ModJobs.STONEMASON_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobStonemason::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.STONEMASON_ID)
          .createJobEntry());

        ModJobs.stoneSmeltery = register(DEFERRED_REGISTER, ModJobs.STONE_SMELTERY_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobStoneSmeltery::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.STONE_SMELTERY_ID)
          .createJobEntry());

        ModJobs.crusher = register(DEFERRED_REGISTER, ModJobs.CRUSHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCrusher::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.CRUSHER_ID)
          .createJobEntry());

        ModJobs.sifter = register(DEFERRED_REGISTER, ModJobs.SIFTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSifter::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.SIFTER_ID)
          .createJobEntry());

        ModJobs.florist = register(DEFERRED_REGISTER, ModJobs.FLORIST_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFlorist::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.FLORIST_ID)
          .createJobEntry());

        ModJobs.enchanter = register(DEFERRED_REGISTER, ModJobs.ENCHANTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobEnchanter::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.ENCHANTER_ID)
          .createJobEntry());

        ModJobs.researcher = register(DEFERRED_REGISTER, ModJobs.RESEARCHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobResearch::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.RESEARCHER_ID)
          .createJobEntry());

        ModJobs.healer = register(DEFERRED_REGISTER, ModJobs.HEALER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobHealer::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.HEALER_ID)
          .createJobEntry());

        ModJobs.pupil = register(DEFERRED_REGISTER, ModJobs.PUPIL_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobPupil::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.PUPIL_ID)
          .createJobEntry());

        ModJobs.teacher = register(DEFERRED_REGISTER, ModJobs.TEACHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobTeacher::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.TEACHER_ID)
          .createJobEntry());

        ModJobs.glassblower = register(DEFERRED_REGISTER, ModJobs.GLASSBLOWER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobGlassblower::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.GLASSBLOWER_ID)
          .createJobEntry());

        ModJobs.dyer = register(DEFERRED_REGISTER, ModJobs.DYER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobDyer::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.DYER_ID)
          .createJobEntry());

        ModJobs.fletcher = register(DEFERRED_REGISTER, ModJobs.FLETCHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFletcher::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.FLETCHER_ID)
          .createJobEntry());

        ModJobs.mechanic = register(DEFERRED_REGISTER, ModJobs.MECHANIC_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobMechanic::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.MECHANIC_ID)
          .createJobEntry());

        ModJobs.planter = register(DEFERRED_REGISTER, ModJobs.PLANTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobPlanter::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.PLANTER_ID)
          .createJobEntry());

        ModJobs.rabbitHerder = register(DEFERRED_REGISTER, ModJobs.RABBIT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobRabbitHerder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.RABBIT_ID)
          .createJobEntry());

        ModJobs.concreteMixer = register(DEFERRED_REGISTER, ModJobs.CONCRETE_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobConcreteMixer::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.CONCRETE_ID)
          .createJobEntry());

        ModJobs.beekeeper = register(DEFERRED_REGISTER, ModJobs.BEEKEEPER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBeekeeper::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.BEEKEEPER_ID)
          .createJobEntry());

        ModJobs.cookassistant = register(DEFERRED_REGISTER, ModJobs.COOKASSISTANT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCookAssistant::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.COOKASSISTANT_ID)
          .createJobEntry());

        ModJobs.netherworker = register(DEFERRED_REGISTER, ModJobs.NETHERWORKER_ID.getPath(), () -> new JobEntry.Builder()
                              .setJobProducer(JobNetherWorker::new)
                              .setJobViewProducer(() -> CrafterJobView::new)
                              .setRegistryName(ModJobs.NETHERWORKER_ID)
                              .createJobEntry());

        ModJobs.quarrier = register(DEFERRED_REGISTER, ModJobs.QUARRY_MINER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobQuarrier::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.QUARRY_MINER_ID)
          .createJobEntry());

        ModJobs.druid = register(DEFERRED_REGISTER, ModJobs.DRUID_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobDruid::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.DRUID_ID)
          .createJobEntry());

        ModJobs.alchemist = register(DEFERRED_REGISTER, ModJobs.ALCHEMIST_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobAlchemist::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.ALCHEMIST_ID)
          .createJobEntry());
    }

    /**
     * Register a job at the deferred registry and store the job token in the job list.
     * @param deferredRegister the registry,
     * @param path the path.
     * @param supplier the supplier of the entry.
     * @return the registry object.
     */
    private static DeferredHolder<JobEntry, JobEntry> register(final DeferredRegister<JobEntry> deferredRegister, final String path, final Supplier<JobEntry> supplier)
    {
        ModJobs.jobs.add(new ResourceLocation(Constants.MOD_ID, path));
        return deferredRegister.register(path, supplier);
    }
}
