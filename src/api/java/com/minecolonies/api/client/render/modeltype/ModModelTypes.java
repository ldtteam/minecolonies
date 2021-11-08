package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.util.ResourceLocation;

public class ModModelTypes
{
    public static final ResourceLocation SETTLER_ID        = new ResourceLocation(Constants.MOD_ID, "settler");
    public static final ResourceLocation CITIZEN_ID        = new ResourceLocation(Constants.MOD_ID, "citizen");
    public static final ResourceLocation NOBLE_ID          = new ResourceLocation(Constants.MOD_ID, "noble");
    public static final ResourceLocation ARISTOCRAT_ID     = new ResourceLocation(Constants.MOD_ID, "aristocrat");
    public static final ResourceLocation BUILDER_ID        = new ResourceLocation(Constants.MOD_ID, "builder");
    public static final ResourceLocation DELIVERYMAN_ID    = new ResourceLocation(Constants.MOD_ID, "deliveryman");
    public static final ResourceLocation MINER_ID          = new ResourceLocation(Constants.MOD_ID, "miner");
    public static final ResourceLocation LUMBERJACK_ID     = new ResourceLocation(Constants.MOD_ID, "lumberjack");
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

    public static BipedModelType SETTLER;
    public static BipedModelType CITIZEN;
    public static BipedModelType NOBLE;
    public static BipedModelType ARISTOCRAT;
    public static BipedModelType BUILDER;
    public static BipedModelType DELIVERYMAN;
    public static BipedModelType MINER;
    public static BipedModelType LUMBERJACK;
    public static BipedModelType FARMER;
    public static BipedModelType FISHERMAN;
    public static BipedModelType UNDERTAKER;
    public static BipedModelType ARCHER_GUARD;
    public static BipedModelType KNIGHT_GUARD;
    public static BipedModelType BAKER;
    public static BipedModelType SHEEP_FARMER;
    public static BipedModelType COW_FARMER;
    public static BipedModelType PIG_FARMER;
    public static BipedModelType CHICKEN_FARMER;
    public static BipedModelType COMPOSTER;
    public static BipedModelType SMELTER;
    public static BipedModelType COOK;
    public static BipedModelType STUDENT;
    public static BipedModelType CRAFTER;
    public static BipedModelType BLACKSMITH;
    public static BipedModelType CHILD;
    public static BipedModelType HEALER;
    public static BipedModelType TEACHER;
    public static BipedModelType GLASSBLOWER;
    public static BipedModelType DYER;
    public static BipedModelType MECHANIST;
    public static BipedModelType FLETCHER;
    public static BipedModelType CONCRETE_MIXER;
    public static BipedModelType RABBIT_HERDER;
    public static BipedModelType PLANTER;
    public static BipedModelType BEEKEEPER;

    private ModModelTypes()
    {
        throw new IllegalStateException("Tried to initialize: ModModelTypes but this is a Utility class.");
    }
}
