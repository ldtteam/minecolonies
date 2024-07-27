package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.ArrayList;
import java.util.List;

public final class ModJobs
{
    public static final ResourceLocation PLACEHOLDER_ID    = new ResourceLocation(Constants.MOD_ID, "placeholder");
    public static final ResourceLocation BUILDER_ID        = new ResourceLocation(Constants.MOD_ID, "builder");
    public static final ResourceLocation DELIVERY_ID       = new ResourceLocation(Constants.MOD_ID, "deliveryman");
    public static final ResourceLocation MINER_ID          = new ResourceLocation(Constants.MOD_ID, "miner");
    public static final ResourceLocation LUMBERJACK_ID     = new ResourceLocation(Constants.MOD_ID, "lumberjack");
    public static final ResourceLocation FARMER_ID         = new ResourceLocation(Constants.MOD_ID, "farmer");
    public static final ResourceLocation UNDERTAKER_ID     = new ResourceLocation(Constants.MOD_ID, "undertaker");
    public static final ResourceLocation FISHERMAN_ID      = new ResourceLocation(Constants.MOD_ID, "fisherman");
    public static final ResourceLocation BAKER_ID          = new ResourceLocation(Constants.MOD_ID, "baker");
    public static final ResourceLocation COOK_ID           = new ResourceLocation(Constants.MOD_ID, "cook");
    public static final ResourceLocation SHEPHERD_ID       = new ResourceLocation(Constants.MOD_ID, "shepherd");
    public static final ResourceLocation COWBOY_ID         = new ResourceLocation(Constants.MOD_ID, "cowboy");
    public static final ResourceLocation SWINE_HERDER_ID   = new ResourceLocation(Constants.MOD_ID, "swineherder");
    public static final ResourceLocation CHICKEN_HERDER_ID = new ResourceLocation(Constants.MOD_ID, "chickenherder");
    public static final ResourceLocation SMELTER_ID = new ResourceLocation(Constants.MOD_ID, "smelter");
    public static final ResourceLocation ARCHER_ID  = new ResourceLocation(Constants.MOD_ID, "ranger");
    public static final ResourceLocation KNIGHT_ID  = new ResourceLocation(Constants.MOD_ID, "knight");
    public static final ResourceLocation COMPOSTER_ID      = new ResourceLocation(Constants.MOD_ID, "composter");
    public static final ResourceLocation STUDENT_ID         = new ResourceLocation(Constants.MOD_ID, "student");
    public static final ResourceLocation ARCHER_TRAINING_ID = new ResourceLocation(Constants.MOD_ID, "archertraining");
    public static final ResourceLocation KNIGHT_TRAINING_ID = new ResourceLocation(Constants.MOD_ID, "combattraining");
    public static final ResourceLocation SAWMILL_ID         = new ResourceLocation(Constants.MOD_ID, "sawmill");
    public static final ResourceLocation BLACKSMITH_ID     = new ResourceLocation(Constants.MOD_ID, "blacksmith");
    public static final ResourceLocation STONEMASON_ID     = new ResourceLocation(Constants.MOD_ID, "stonemason");
    public static final ResourceLocation STONE_SMELTERY_ID = new ResourceLocation(Constants.MOD_ID, "stonesmeltery");
    public static final ResourceLocation CRUSHER_ID        = new ResourceLocation(Constants.MOD_ID, "crusher");
    public static final ResourceLocation SIFTER_ID         = new ResourceLocation(Constants.MOD_ID, "sifter");
    public static final ResourceLocation FLORIST_ID        = new ResourceLocation(Constants.MOD_ID, "florist");
    public static final ResourceLocation ENCHANTER_ID      = new ResourceLocation(Constants.MOD_ID, "enchanter");
    public static final ResourceLocation RESEARCHER_ID     = new ResourceLocation(Constants.MOD_ID, "researcher");
    public static final ResourceLocation HEALER_ID         = new ResourceLocation(Constants.MOD_ID, "healer");
    public static final ResourceLocation PUPIL_ID          = new ResourceLocation(Constants.MOD_ID, "pupil");
    public static final ResourceLocation TEACHER_ID        = new ResourceLocation(Constants.MOD_ID, "teacher");
    public static final ResourceLocation GLASSBLOWER_ID    = new ResourceLocation(Constants.MOD_ID, "glassblower");
    public static final ResourceLocation DYER_ID           = new ResourceLocation(Constants.MOD_ID, "dyer");
    public static final ResourceLocation FLETCHER_ID       = new ResourceLocation(Constants.MOD_ID, "fletcher");
    public static final ResourceLocation MECHANIC_ID       = new ResourceLocation(Constants.MOD_ID, "mechanic");
    public static final ResourceLocation PLANTER_ID        = new ResourceLocation(Constants.MOD_ID, "planter");
    public static final ResourceLocation RABBIT_ID         = new ResourceLocation(Constants.MOD_ID, "rabbitherder");
    public static final ResourceLocation CONCRETE_ID       = new ResourceLocation(Constants.MOD_ID, "concretemixer");
    public static final ResourceLocation BEEKEEPER_ID      = new ResourceLocation(Constants.MOD_ID, "beekeeper");
    public static final ResourceLocation NETHERWORKER_ID   = new ResourceLocation(Constants.MOD_ID, "netherworker");
    public static final ResourceLocation QUARRY_MINER_ID   = new ResourceLocation(Constants.MOD_ID, "quarrier");
    public static final ResourceLocation DRUID_ID          = new ResourceLocation(Constants.MOD_ID, "druid");
    public static final ResourceLocation ALCHEMIST_ID      = new ResourceLocation(Constants.MOD_ID, "alchemist");
    public static final ResourceLocation CHEF_ID           = new ResourceLocation(Constants.MOD_ID, "chef");

    @Deprecated
    public static final ResourceLocation COOKASSISTANT_ID  = new ResourceLocation(Constants.MOD_ID, "cookassistant");

    public static DeferredHolder<JobEntry, JobEntry> placeHolder;
    public static DeferredHolder<JobEntry, JobEntry> builder;
    public static DeferredHolder<JobEntry, JobEntry> delivery;
    public static DeferredHolder<JobEntry, JobEntry> miner;
    public static DeferredHolder<JobEntry, JobEntry> lumberjack;
    public static DeferredHolder<JobEntry, JobEntry> farmer;
    public static DeferredHolder<JobEntry, JobEntry> fisherman;
    public static DeferredHolder<JobEntry, JobEntry> undertaker;
    public static DeferredHolder<JobEntry, JobEntry> baker;
    public static DeferredHolder<JobEntry, JobEntry> cook;
    public static DeferredHolder<JobEntry, JobEntry> shepherd;
    public static DeferredHolder<JobEntry, JobEntry> cowboy;
    public static DeferredHolder<JobEntry, JobEntry> swineHerder;
    public static DeferredHolder<JobEntry, JobEntry> chickenHerder;
    public static DeferredHolder<JobEntry, JobEntry> smelter;
    public static DeferredHolder<JobEntry, JobEntry> archer;
    public static DeferredHolder<JobEntry, JobEntry> knight;
    public static DeferredHolder<JobEntry, JobEntry> composter;
    public static DeferredHolder<JobEntry, JobEntry> student;
    public static DeferredHolder<JobEntry, JobEntry> archerInTraining;
    public static DeferredHolder<JobEntry, JobEntry> knightInTraining;
    public static DeferredHolder<JobEntry, JobEntry> sawmill;
    public static DeferredHolder<JobEntry, JobEntry> blacksmith;
    public static DeferredHolder<JobEntry, JobEntry> stoneMason;
    public static DeferredHolder<JobEntry, JobEntry> stoneSmeltery;
    public static DeferredHolder<JobEntry, JobEntry> crusher;
    public static DeferredHolder<JobEntry, JobEntry> sifter;
    public static DeferredHolder<JobEntry, JobEntry> florist;
    public static DeferredHolder<JobEntry, JobEntry> enchanter;
    public static DeferredHolder<JobEntry, JobEntry> researcher;
    public static DeferredHolder<JobEntry, JobEntry> healer;
    public static DeferredHolder<JobEntry, JobEntry> pupil;
    public static DeferredHolder<JobEntry, JobEntry> teacher;
    public static DeferredHolder<JobEntry, JobEntry> glassblower;
    public static DeferredHolder<JobEntry, JobEntry> dyer;
    public static DeferredHolder<JobEntry, JobEntry> fletcher;
    public static DeferredHolder<JobEntry, JobEntry> mechanic;
    public static DeferredHolder<JobEntry, JobEntry> planter;
    public static DeferredHolder<JobEntry, JobEntry> rabbitHerder;
    public static DeferredHolder<JobEntry, JobEntry> concreteMixer;
    public static DeferredHolder<JobEntry, JobEntry> beekeeper;
    public static DeferredHolder<JobEntry, JobEntry> cookassistant;
    public static DeferredHolder<JobEntry, JobEntry> netherworker;
    public static DeferredHolder<JobEntry, JobEntry> quarrier;
    public static DeferredHolder<JobEntry, JobEntry> druid;
    public static DeferredHolder<JobEntry, JobEntry> alchemist;
    public static DeferredHolder<JobEntry, JobEntry> chef;

    @Deprecated
    public static DeferredHolder<JobEntry, JobEntry> cookassistant;

    /**
     * List of all jobs.
     */
    public static List<ResourceLocation> jobs = new ArrayList<>() { };

    private ModJobs()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }

    public static List<ResourceLocation> getJobs()
    {
        return jobs;
    }
}
