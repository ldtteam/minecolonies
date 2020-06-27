package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public final class ModJobs
{

    public static final ResourceLocation PLACEHOLDER_ID    = new ResourceLocation(Constants.MOD_ID, "placeholder");
    public static final ResourceLocation BUILDER_ID        = new ResourceLocation(Constants.MOD_ID,"builder");
    public static final ResourceLocation DELIVERY_ID       = new ResourceLocation(Constants.MOD_ID,"deliveryman");
    public static final ResourceLocation MINER_ID          = new ResourceLocation(Constants.MOD_ID,"miner");
    public static final ResourceLocation LUMBERJACK_ID     = new ResourceLocation(Constants.MOD_ID,"lumberjack");
    public static final ResourceLocation FARMER_ID         = new ResourceLocation(Constants.MOD_ID,"farmer");
    public static final ResourceLocation FISHERMAN_ID      = new ResourceLocation(Constants.MOD_ID,"fisherman");
    public static final ResourceLocation BAKER_ID          = new ResourceLocation(Constants.MOD_ID,"baker");
    public static final ResourceLocation COOK_ID           = new ResourceLocation(Constants.MOD_ID,"cook");
    public static final ResourceLocation SHEPHERD_ID       = new ResourceLocation(Constants.MOD_ID,"shepherd");
    public static final ResourceLocation COWBOY_ID         = new ResourceLocation(Constants.MOD_ID,"cowboy");
    public static final ResourceLocation SWINE_HERDER_ID   = new ResourceLocation(Constants.MOD_ID,"swineherder");
    public static final ResourceLocation CHICKEN_HERDER_ID = new ResourceLocation(Constants.MOD_ID,"chickenherder");
    public static final ResourceLocation SMELTER_ID        = new ResourceLocation(Constants.MOD_ID,"smelter");
    public static final ResourceLocation RANGER_ID         = new ResourceLocation(Constants.MOD_ID,"ranger");
    public static final ResourceLocation KNIGHT_ID         = new ResourceLocation(Constants.MOD_ID,"knight");
    public static final ResourceLocation COMPOSTER_ID      = new ResourceLocation(Constants.MOD_ID,"composter");
    public static final ResourceLocation STUDENT_ID        = new ResourceLocation(Constants.MOD_ID,"student");
    public static final ResourceLocation ARCHER_ID         = new ResourceLocation(Constants.MOD_ID,"archertraining");
    public static final ResourceLocation COMBAT_ID         = new ResourceLocation(Constants.MOD_ID,"combattraining");
    public static final ResourceLocation SAWMILL_ID        = new ResourceLocation(Constants.MOD_ID,"sawmill");
    public static final ResourceLocation BLACKSMITH_ID     = new ResourceLocation(Constants.MOD_ID,"blacksmith");
    public static final ResourceLocation STONEMASON_ID     = new ResourceLocation(Constants.MOD_ID,"stonemason");
    public static final ResourceLocation STONE_SMELTERY_ID = new ResourceLocation(Constants.MOD_ID,"stonesmeltery");
    public static final ResourceLocation CRUSHER_ID        = new ResourceLocation(Constants.MOD_ID,"crusher");
    public static final ResourceLocation SIFTER_ID         = new ResourceLocation(Constants.MOD_ID,"sifter");
    public static final ResourceLocation FLORIST_ID        = new ResourceLocation(Constants.MOD_ID,"florist");
    public static final ResourceLocation ENCHANTER_ID      = new ResourceLocation(Constants.MOD_ID,"enchanter");
    public static final ResourceLocation RESEARCHER_ID     = new ResourceLocation(Constants.MOD_ID,"researcher");
    public static final ResourceLocation HEALER_ID         = new ResourceLocation(Constants.MOD_ID,"healer");
    public static final ResourceLocation PUPIL_ID          = new ResourceLocation(Constants.MOD_ID,"pupil");
    public static final ResourceLocation TEACHER_ID        = new ResourceLocation(Constants.MOD_ID,"teacher");
    public static final ResourceLocation GLASSBLOWER_ID    = new ResourceLocation(Constants.MOD_ID,"glassblower");
    public static final ResourceLocation DYER_ID           = new ResourceLocation(Constants.MOD_ID,"dyer");
    public static final ResourceLocation FLETCHER_ID       = new ResourceLocation(Constants.MOD_ID,"fletcher");
    public static final ResourceLocation MECHANIC_ID       = new ResourceLocation(Constants.MOD_ID,"mechanic");
    public static final ResourceLocation PLANTER_ID        = new ResourceLocation(Constants.MOD_ID,"planter");
    public static final ResourceLocation RABBIT_ID         = new ResourceLocation(Constants.MOD_ID,"rabbitherder");
    public static final ResourceLocation CONCRETE_ID       = new ResourceLocation(Constants.MOD_ID,"concretemixer");
    public static final ResourceLocation BEEKEEPER_ID      = new ResourceLocation(Constants.MOD_ID,"beekeeper");

    public static       JobEntry         placeHolder;
    public static       JobEntry         builder;
    public static       JobEntry         delivery;
    public static       JobEntry         miner;
    public static       JobEntry         lumberjack;
    public static       JobEntry         farmer;
    public static       JobEntry         fisherman;
    public static       JobEntry         baker;
    public static       JobEntry         cook;
    public static       JobEntry         shepherd;
    public static       JobEntry         cowboy;
    public static       JobEntry         swineHerder;
    public static       JobEntry         chickenHerder;
    public static       JobEntry         smelter;
    public static       JobEntry         ranger;
    public static       JobEntry         knight;
    public static       JobEntry         composter;
    public static       JobEntry         student;
    public static       JobEntry         archer;
    public static       JobEntry         combat;
    public static       JobEntry         sawmill;
    public static       JobEntry         blacksmith;
    public static       JobEntry         stoneMason;
    public static       JobEntry         stoneSmeltery;
    public static       JobEntry         crusher;
    public static       JobEntry         sifter;
    public static       JobEntry         florist;
    public static       JobEntry         enchanter;
    public static       JobEntry         researcher;
    public static       JobEntry         healer;
    public static       JobEntry         pupil;
    public static       JobEntry         teacher;
    public static       JobEntry         glassblower;
    public static       JobEntry         dyer;
    public static       JobEntry         fletcher;
    public static       JobEntry         mechanic;
    public static       JobEntry         planter;
    public static       JobEntry         rabbitHerder;
    public static       JobEntry         concreteMixer;
    public static       JobEntry         beekeeper;

    private ModJobs()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
