package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.resources.ResourceLocation;

public final class ModModelTypes
{
    public static final ResourceLocation SETTLER_ID        = new ResourceLocation(Constants.MOD_ID, "settler");
    public static final ResourceLocation CITIZEN_ID        = new ResourceLocation(Constants.MOD_ID, "citizen");
    public static final ResourceLocation NOBLE_ID          = new ResourceLocation(Constants.MOD_ID, "noble");
    public static final ResourceLocation ARISTOCRAT_ID     = new ResourceLocation(Constants.MOD_ID, "aristocrat");
    public static final ResourceLocation BUILDER_ID = new ResourceLocation(Constants.MOD_ID, "builder");
    public static final ResourceLocation COURIER_ID = new ResourceLocation(Constants.MOD_ID, "courier");
    public static final ResourceLocation MINER_ID   = new ResourceLocation(Constants.MOD_ID, "miner");
    public static final ResourceLocation LUMBERJACK_ID     = new ResourceLocation(Constants.MOD_ID, "forester");
    public static final ResourceLocation FARMER_ID         = new ResourceLocation(Constants.MOD_ID, "farmer");
    public static final ResourceLocation FISHERMAN_ID      = new ResourceLocation(Constants.MOD_ID, "fisherman");
    public static final ResourceLocation UNDERTAKER_ID     = new ResourceLocation(Constants.MOD_ID, "undertaker");
    public static final ResourceLocation ARCHER_GUARD_ID   = new ResourceLocation(Constants.MOD_ID, "archer");
    public static final ResourceLocation KNIGHT_GUARD_ID   = new ResourceLocation(Constants.MOD_ID, "knight");
    public static final ResourceLocation BAKER_ID          = new ResourceLocation(Constants.MOD_ID, "baker");
    public static final ResourceLocation SHEEP_FARMER_ID   = new ResourceLocation(Constants.MOD_ID, "sheepfarmer");
    public static final ResourceLocation COW_FARMER_ID     = new ResourceLocation(Constants.MOD_ID, "cowfarmer");
    public static final ResourceLocation PIG_FARMER_ID     = new ResourceLocation(Constants.MOD_ID, "pigfarmer");
    public static final ResourceLocation CHICKEN_FARMER_ID = new ResourceLocation(Constants.MOD_ID, "chickenfarmer");
    public static final ResourceLocation COMPOSTER_ID      = new ResourceLocation(Constants.MOD_ID, "composter");
    public static final ResourceLocation SMELTER_ID        = new ResourceLocation(Constants.MOD_ID, "smelter");
    public static final ResourceLocation COOK_ID           = new ResourceLocation(Constants.MOD_ID, "cook");
    public static final ResourceLocation STUDENT_ID        = new ResourceLocation(Constants.MOD_ID, "student");
    public static final ResourceLocation CRAFTER_ID        = new ResourceLocation(Constants.MOD_ID, "crafter");
    public static final ResourceLocation BLACKSMITH_ID     = new ResourceLocation(Constants.MOD_ID, "blacksmith");
    public static final ResourceLocation CHILD_ID          = new ResourceLocation(Constants.MOD_ID, "child");
    public static final ResourceLocation HEALER_ID         = new ResourceLocation(Constants.MOD_ID, "healer");
    public static final ResourceLocation TEACHER_ID        = new ResourceLocation(Constants.MOD_ID, "teacher");
    public static final ResourceLocation GLASSBLOWER_ID    = new ResourceLocation(Constants.MOD_ID, "glassblower");
    public static final ResourceLocation DYER_ID           = new ResourceLocation(Constants.MOD_ID, "dyer");
    public static final ResourceLocation MECHANIST_ID      = new ResourceLocation(Constants.MOD_ID, "mechanist");
    public static final ResourceLocation FLETCHER_ID       = new ResourceLocation(Constants.MOD_ID, "fletcher");
    public static final ResourceLocation CONCRETE_MIXER_ID = new ResourceLocation(Constants.MOD_ID, "concretemixer");
    public static final ResourceLocation RABBIT_HERDER_ID  = new ResourceLocation(Constants.MOD_ID, "rabbitherder");
    public static final ResourceLocation PLANTER_ID        = new ResourceLocation(Constants.MOD_ID, "planter");
    public static final ResourceLocation BEEKEEPER_ID      = new ResourceLocation(Constants.MOD_ID, "beekeeper");
    public static final ResourceLocation CUSTOM_ID         = new ResourceLocation(Constants.MOD_ID, "custom");
    public static final ResourceLocation NETHERWORKER_ID   = new ResourceLocation(Constants.MOD_ID, "netherworker");
    public static final ResourceLocation DRUID_ID          = new ResourceLocation(Constants.MOD_ID, "druid");
    public static final ResourceLocation ENCHANTER_ID      = new ResourceLocation(Constants.MOD_ID, "enchanter");
    public static final ResourceLocation FLORIST_ID        = new ResourceLocation(Constants.MOD_ID, "florist");
    public static final ResourceLocation KNIGHT_ID         = new ResourceLocation(Constants.MOD_ID, "knight");
    public static final ResourceLocation CARPENTER_ID      = new ResourceLocation(Constants.MOD_ID, "carpenter");
    public static final ResourceLocation ALCHEMIST_ID      = new ResourceLocation(Constants.MOD_ID, "alchemist");

    public static IModelType SETTLER;
    public static IModelType CITIZEN;
    public static IModelType NOBLE;
    public static IModelType ARISTOCRAT;
    public static IModelType BUILDER;
    public static IModelType DELIVERYMAN;
    public static IModelType MINER;
    public static IModelType LUMBERJACK;
    public static IModelType FARMER;
    public static IModelType FISHERMAN;
    public static IModelType UNDERTAKER;
    public static IModelType ARCHER_GUARD;
    public static IModelType KNIGHT_GUARD;
    public static IModelType BAKER;
    public static IModelType SHEEP_FARMER;
    public static IModelType COW_FARMER;
    public static IModelType PIG_FARMER;
    public static IModelType CHICKEN_FARMER;
    public static IModelType COMPOSTER;
    public static IModelType SMELTER;
    public static IModelType COOK;
    public static IModelType STUDENT;
    public static IModelType CRAFTER;
    public static IModelType BLACKSMITH;
    public static IModelType CHILD;
    public static IModelType HEALER;
    public static IModelType TEACHER;
    public static IModelType GLASSBLOWER;
    public static IModelType DYER;
    public static IModelType MECHANIST;
    public static IModelType FLETCHER;
    public static IModelType CONCRETE_MIXER;
    public static IModelType RABBIT_HERDER;
    public static IModelType PLANTER;
    public static IModelType BEEKEEPER;
    public static IModelType CUSTOM;
    public static IModelType NETHERWORKER;
    public static IModelType DRUID;
    public static IModelType FLORIST;
    public static IModelType ENCHANTER;
    public static IModelType KNIGHT;
    public static IModelType CARPENTER;
    public static IModelType ALCHEMIST;

    private ModModelTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModModelTypes but this is a Utility class.");
    }
}
