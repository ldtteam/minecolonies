package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryObject;
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
    public static final ResourceLocation COOKASSISTANT_ID  = new ResourceLocation(Constants.MOD_ID, "cookassistant");
    public static final ResourceLocation NETHERWORKER_ID   = new ResourceLocation(Constants.MOD_ID, "netherworker");
    public static final ResourceLocation QUARRY_MINER_ID   = new ResourceLocation(Constants.MOD_ID, "quarrier");
    public static final ResourceLocation DRUID_ID          = new ResourceLocation(Constants.MOD_ID, "druid");
    public static final ResourceLocation ALCHEMIST_ID      = new ResourceLocation(Constants.MOD_ID, "alchemist");

    public static RegistryObject<JobEntry> placeHolder;
    public static RegistryObject<JobEntry> builder;
    public static RegistryObject<JobEntry> delivery;
    public static RegistryObject<JobEntry> miner;
    public static RegistryObject<JobEntry> lumberjack;
    public static RegistryObject<JobEntry> farmer;
    public static RegistryObject<JobEntry> fisherman;
    public static RegistryObject<JobEntry> undertaker;
    public static RegistryObject<JobEntry> baker;
    public static RegistryObject<JobEntry> cook;
    public static RegistryObject<JobEntry> shepherd;
    public static RegistryObject<JobEntry> cowboy;
    public static RegistryObject<JobEntry> swineHerder;
    public static RegistryObject<JobEntry> chickenHerder;
    public static RegistryObject<JobEntry> smelter;
    public static RegistryObject<JobEntry> archer;
    public static RegistryObject<JobEntry> knight;
    public static RegistryObject<JobEntry> composter;
    public static RegistryObject<JobEntry> student;
    public static RegistryObject<JobEntry> archerInTraining;
    public static RegistryObject<JobEntry> knightInTraining;
    public static RegistryObject<JobEntry> sawmill;
    public static RegistryObject<JobEntry> blacksmith;
    public static RegistryObject<JobEntry> stoneMason;
    public static RegistryObject<JobEntry> stoneSmeltery;
    public static RegistryObject<JobEntry> crusher;
    public static RegistryObject<JobEntry> sifter;
    public static RegistryObject<JobEntry> florist;
    public static RegistryObject<JobEntry> enchanter;
    public static RegistryObject<JobEntry> researcher;
    public static RegistryObject<JobEntry> healer;
    public static RegistryObject<JobEntry> pupil;
    public static RegistryObject<JobEntry> teacher;
    public static RegistryObject<JobEntry> glassblower;
    public static RegistryObject<JobEntry> dyer;
    public static RegistryObject<JobEntry> fletcher;
    public static RegistryObject<JobEntry> mechanic;
    public static RegistryObject<JobEntry> planter;
    public static RegistryObject<JobEntry> rabbitHerder;
    public static RegistryObject<JobEntry> concreteMixer;
    public static RegistryObject<JobEntry> beekeeper;
    public static RegistryObject<JobEntry> cookassistant;
    public static RegistryObject<JobEntry> netherworker;
    public static RegistryObject<JobEntry> quarrier;
    public static RegistryObject<JobEntry> druid;
    public static RegistryObject<JobEntry> alchemist;

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
