package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.jobs.*;
import com.minecolonies.coremod.colony.jobs.views.CrafterJobView;
import com.minecolonies.coremod.colony.jobs.views.DefaultJobView;
import com.minecolonies.coremod.colony.jobs.views.DmanJobView;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public final class ModJobsInitializer
{
    public final static DeferredRegister<JobEntry> DEFERRED_REGISTER = DeferredRegister.create(new ResourceLocation(Constants.MOD_ID, "jobs"), Constants.MOD_ID);

    private ModJobsInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModJobsInitializer but this is a Utility class.");
    }

    static
    {
        ModJobs.placeHolder = DEFERRED_REGISTER.register(ModJobs.PLACEHOLDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobPlaceholder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.PLACEHOLDER_ID)
          .createJobEntry());

        ModJobs.builder = DEFERRED_REGISTER.register(ModJobs.BUILDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBuilder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.BUILDER_ID)
          .createJobEntry());

        ModJobs.delivery = DEFERRED_REGISTER.register(ModJobs.DELIVERY_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobDeliveryman::new)
          .setJobViewProducer(() -> DmanJobView::new)
          .setRegistryName(ModJobs.DELIVERY_ID)
          .createJobEntry());

        ModJobs.miner = DEFERRED_REGISTER.register(ModJobs.MINER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobMiner::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.MINER_ID)
          .createJobEntry());

        ModJobs.lumberjack = DEFERRED_REGISTER.register(ModJobs.LUMBERJACK_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobLumberjack::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.LUMBERJACK_ID)
          .createJobEntry());

        ModJobs.farmer = DEFERRED_REGISTER.register(ModJobs.FARMER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFarmer::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.FARMER_ID)
          .createJobEntry());

        ModJobs.undertaker = DEFERRED_REGISTER.register(ModJobs.UNDERTAKER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobUndertaker::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.UNDERTAKER_ID)
          .createJobEntry());

        ModJobs.fisherman = DEFERRED_REGISTER.register(ModJobs.FISHERMAN_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFisherman::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.FISHERMAN_ID)
          .createJobEntry());

        ModJobs.baker = DEFERRED_REGISTER.register(ModJobs.BAKER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBaker::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.BAKER_ID)
          .createJobEntry());

        ModJobs.cook = DEFERRED_REGISTER.register(ModJobs.COOK_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCook::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COOK_ID)
          .createJobEntry());

        ModJobs.shepherd = DEFERRED_REGISTER.register(ModJobs.SHEPHERD_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobShepherd::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.SHEPHERD_ID)
          .createJobEntry());

        ModJobs.cowboy = DEFERRED_REGISTER.register(ModJobs.COWBOY_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCowboy::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COWBOY_ID)
          .createJobEntry());

        ModJobs.swineHerder = DEFERRED_REGISTER.register(ModJobs.SWINE_HERDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSwineHerder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.SWINE_HERDER_ID)
          .createJobEntry());

        ModJobs.chickenHerder = DEFERRED_REGISTER.register(ModJobs.CHICKEN_HERDER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobChickenHerder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.CHICKEN_HERDER_ID)
          .createJobEntry());

        ModJobs.smelter = DEFERRED_REGISTER.register(ModJobs.SMELTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSmelter::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.SMELTER_ID)
          .createJobEntry());

        ModJobs.ranger = DEFERRED_REGISTER.register(ModJobs.RANGER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobRanger::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.RANGER_ID)
          .createJobEntry());

        ModJobs.knight = DEFERRED_REGISTER.register(ModJobs.KNIGHT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobKnight::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.KNIGHT_ID)
          .createJobEntry());

        ModJobs.composter = DEFERRED_REGISTER.register(ModJobs.COMPOSTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobComposter::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COMPOSTER_ID)
          .createJobEntry());

        ModJobs.student = DEFERRED_REGISTER.register(ModJobs.STUDENT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobStudent::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.STUDENT_ID)
          .createJobEntry());

        ModJobs.archer = DEFERRED_REGISTER.register(ModJobs.ARCHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobArcherTraining::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.ARCHER_ID)
          .createJobEntry());

        ModJobs.combat = DEFERRED_REGISTER.register(ModJobs.COMBAT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCombatTraining::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.COMBAT_ID)
          .createJobEntry());

        ModJobs.sawmill = DEFERRED_REGISTER.register(ModJobs.SAWMILL_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSawmill::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.SAWMILL_ID)
          .createJobEntry());

        ModJobs.blacksmith = DEFERRED_REGISTER.register(ModJobs.BLACKSMITH_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBlacksmith::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.BLACKSMITH_ID)
          .createJobEntry());

        ModJobs.stoneMason = DEFERRED_REGISTER.register(ModJobs.STONEMASON_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobStonemason::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.STONEMASON_ID)
          .createJobEntry());

        ModJobs.stoneSmeltery = DEFERRED_REGISTER.register(ModJobs.STONE_SMELTERY_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobStoneSmeltery::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.STONE_SMELTERY_ID)
          .createJobEntry());

        ModJobs.crusher = DEFERRED_REGISTER.register(ModJobs.CRUSHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCrusher::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.CRUSHER_ID)
          .createJobEntry());

        ModJobs.sifter = DEFERRED_REGISTER.register(ModJobs.SIFTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobSifter::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.SIFTER_ID)
          .createJobEntry());

        ModJobs.florist = DEFERRED_REGISTER.register(ModJobs.FLORIST_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFlorist::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.FLORIST_ID)
          .createJobEntry());

        ModJobs.enchanter = DEFERRED_REGISTER.register(ModJobs.ENCHANTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobEnchanter::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.ENCHANTER_ID)
          .createJobEntry());

        ModJobs.researcher = DEFERRED_REGISTER.register(ModJobs.RESEARCHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobResearch::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.RESEARCHER_ID)
          .createJobEntry());

        ModJobs.healer = DEFERRED_REGISTER.register(ModJobs.HEALER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobHealer::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.HEALER_ID)
          .createJobEntry());

        ModJobs.pupil = DEFERRED_REGISTER.register(ModJobs.PUPIL_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobPupil::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.PUPIL_ID)
          .createJobEntry());

        ModJobs.teacher = DEFERRED_REGISTER.register(ModJobs.TEACHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobTeacher::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.TEACHER_ID)
          .createJobEntry());

        ModJobs.glassblower = DEFERRED_REGISTER.register(ModJobs.GLASSBLOWER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobGlassblower::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.GLASSBLOWER_ID)
          .createJobEntry());

        ModJobs.dyer = DEFERRED_REGISTER.register(ModJobs.DYER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobDyer::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.DYER_ID)
          .createJobEntry());

        ModJobs.fletcher = DEFERRED_REGISTER.register(ModJobs.FLETCHER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobFletcher::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.FLETCHER_ID)
          .createJobEntry());

        ModJobs.mechanic = DEFERRED_REGISTER.register(ModJobs.MECHANIC_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobMechanic::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.MECHANIC_ID)
          .createJobEntry());

        ModJobs.planter = DEFERRED_REGISTER.register(ModJobs.PLANTER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobPlanter::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.PLANTER_ID)
          .createJobEntry());

        ModJobs.rabbitHerder = DEFERRED_REGISTER.register(ModJobs.RABBIT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobRabbitHerder::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.RABBIT_ID)
          .createJobEntry());

        ModJobs.concreteMixer = DEFERRED_REGISTER.register(ModJobs.CONCRETE_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobConcreteMixer::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.CONCRETE_ID)
          .createJobEntry());

        ModJobs.beekeeper = DEFERRED_REGISTER.register(ModJobs.BEEKEEPER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobBeekeeper::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.BEEKEEPER_ID)
          .createJobEntry());

        ModJobs.cookassistant = DEFERRED_REGISTER.register(ModJobs.COOKASSISTANT_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobCookAssistant::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.COOKASSISTANT_ID)
          .createJobEntry());

        ModJobs.netherworker = DEFERRED_REGISTER.register(ModJobs.NETHERWORKER_ID.getPath(), () -> new JobEntry.Builder()
                              .setJobProducer(JobNetherWorker::new)
                              .setJobViewProducer(() -> CrafterJobView::new)
                              .setRegistryName(ModJobs.NETHERWORKER_ID)
                              .createJobEntry());

        ModJobs.quarrier = DEFERRED_REGISTER.register(ModJobs.QUARRY_MINER_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobQuarrier::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.QUARRY_MINER_ID)
          .createJobEntry());

        ModJobs.druid = DEFERRED_REGISTER.register(ModJobs.DRUID_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobDruid::new)
          .setJobViewProducer(() -> DefaultJobView::new)
          .setRegistryName(ModJobs.DRUID_ID)
          .createJobEntry());

        ModJobs.alchemist = DEFERRED_REGISTER.register(ModJobs.ALCHEMIST_ID.getPath(), () -> new JobEntry.Builder()
          .setJobProducer(JobAlchemist::new)
          .setJobViewProducer(() -> CrafterJobView::new)
          .setRegistryName(ModJobs.ALCHEMIST_ID)
          .createJobEntry());
    }
}
