package com.minecolonies.api.colony.jobs;

import com.minecolonies.api.colony.jobs.registry.JobEntry;
import net.minecraft.util.ResourceLocation;

public final class ModJobs
{

    public static final ResourceLocation PLACEHOLDER_ID    = new ResourceLocation("Placeholder");
    public static final ResourceLocation BUILDER_ID        = new ResourceLocation("Builder");
    public static final ResourceLocation DELIVERY_ID       = new ResourceLocation("Deliveryman");
    public static final ResourceLocation MINER_ID          = new ResourceLocation("Miner");
    public static final ResourceLocation LUMBERJACK_ID     = new ResourceLocation("Lumberjack");
    public static final ResourceLocation FARMER_ID         = new ResourceLocation("Farmer");
    public static final ResourceLocation FISHERMAN_ID      = new ResourceLocation("Fisherman");
    public static final ResourceLocation BAKER_ID          = new ResourceLocation("Baker");
    public static final ResourceLocation COOK_ID           = new ResourceLocation("Cook");
    public static final ResourceLocation SHEPHERD_ID       = new ResourceLocation("Shepherd");
    public static final ResourceLocation COWBOY_ID         = new ResourceLocation("Cowboy");
    public static final ResourceLocation SWINE_HERDER_ID   = new ResourceLocation("SwineHerder");
    public static final ResourceLocation CHICKEN_HERDER_ID = new ResourceLocation("ChickenHerder");
    public static final ResourceLocation SMELTER_ID        = new ResourceLocation("Smelter");
    public static final ResourceLocation RANGER_ID         = new ResourceLocation("Ranger");
    public static final ResourceLocation KNIGHT_ID         = new ResourceLocation("Knight");
    public static final ResourceLocation COMPOSTER_ID      = new ResourceLocation("Composter");
    public static final ResourceLocation STUDENT_ID        = new ResourceLocation("Student");
    public static final ResourceLocation ARCHER_ID         = new ResourceLocation("ArcherTraining");
    public static final ResourceLocation COMBAT_ID         = new ResourceLocation("CombatTraining");
    public static final ResourceLocation SAWMILL_ID        = new ResourceLocation("Sawmill");
    public static final ResourceLocation BLACKSMITH_ID     = new ResourceLocation("Blacksmith");
    public static final ResourceLocation STONEMASON_ID     = new ResourceLocation("Stonemason");
    public static final ResourceLocation STONE_SMELTERY_ID = new ResourceLocation("StoneSmeltery");
    public static final ResourceLocation CRUSHER_ID        = new ResourceLocation("Crusher");
    public static final ResourceLocation SIFTER_ID         = new ResourceLocation("Sifter");
    public static final ResourceLocation FLORIST_ID        = new ResourceLocation("Florist");
    public static final ResourceLocation ENCHANTER_ID      = new ResourceLocation("enchanter");

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

    private ModJobs()
    {
        throw new IllegalStateException("Tried to initialize: ModJobs but this is a Utility class.");
    }
}
