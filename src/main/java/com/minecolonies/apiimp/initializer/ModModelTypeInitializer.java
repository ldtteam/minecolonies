package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.SimpleModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.model.*;

public class ModModelTypeInitializer
{
    private ModModelTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModModelTypeInitializer but this is a Utility class.");
    }

    public static void init()
    {
        final IModelTypeRegistry reg = IModelTypeRegistry.getInstance();

        ModModelTypes.SETTLER = new SimpleModelType(ModModelTypes.SETTLER_ID, 3, new CitizenModel(), new ModelEntityFemaleCitizen());
        reg.register(ModModelTypes.SETTLER);

        ModModelTypes.CITIZEN = new SimpleModelType(ModModelTypes.CITIZEN_ID, 3, new CitizenModel<>(), new ModelEntityFemaleCitizen());
        reg.register(ModModelTypes.CITIZEN);

        ModModelTypes.NOBLE = new SimpleModelType(ModModelTypes.NOBLE_ID, 3, new CitizenModel<>(), new ModelEntityCitizenFemaleNoble());
        reg.register(ModModelTypes.NOBLE);

        ModModelTypes.ARISTOCRAT = new SimpleModelType(ModModelTypes.ARISTOCRAT_ID, 3, new CitizenModel<>(), new ModelEntityFemaleAristocrat());
        reg.register(ModModelTypes.ARISTOCRAT);

        ModModelTypes.BUILDER = new SimpleModelType(ModModelTypes.BUILDER_ID, 1, new CitizenModel<>(), new ModelEntityBuilderFemale());
        reg.register(ModModelTypes.BUILDER);

        ModModelTypes.DELIVERYMAN = new SimpleModelType(ModModelTypes.DELIVERYMAN_ID, 1, new ModelEntityDeliverymanMale(), new ModelEntityDeliverymanFemale());
        reg.register(ModModelTypes.DELIVERYMAN);

        ModModelTypes.MINER = new SimpleModelType(ModModelTypes.MINER_ID, 1, new ModelEntityMinerMale(), new ModelEntityMinerFemale());
        reg.register(ModModelTypes.MINER);

        // Lumberjack: 4 male, 1 female
        ModModelTypes.LUMBERJACK = new SimpleModelType(ModModelTypes.LUMBERJACK_ID, 1, new ModelEntityLumberjackMale(), new ModelEntityLumberjackFemale());
        reg.register(ModModelTypes.LUMBERJACK);

        ModModelTypes.FARMER = new SimpleModelType(ModModelTypes.FARMER_ID, 1, new ModelEntityFarmerMale(), new ModelEntityFarmerFemale());
        reg.register(ModModelTypes.FARMER);

        ModModelTypes.FISHERMAN = new SimpleModelType(ModModelTypes.FISHERMAN_ID, 1, new ModelEntityFishermanMale(), new ModelEntityFishermanFemale());
        reg.register(ModModelTypes.FISHERMAN);

        ModModelTypes.UNDERTAKER = new SimpleModelType(ModModelTypes.UNDERTAKER_ID, 1, new ModelEntityUndertakerMale(), new ModelEntityUndertakerFemale());
        reg.register(ModModelTypes.UNDERTAKER);

        ModModelTypes.ARCHER_GUARD = new SimpleModelType(ModModelTypes.ARCHER_GUARD_ID, 1, new CitizenModel<>(), new CitizenModel<>());
        reg.register(ModModelTypes.ARCHER_GUARD);

        ModModelTypes.KNIGHT_GUARD = new SimpleModelType(ModModelTypes.KNIGHT_GUARD_ID, 1, new CitizenModel<>(), new CitizenModel<>());
        reg.register(ModModelTypes.KNIGHT_GUARD);

        ModModelTypes.BAKER = new SimpleModelType(ModModelTypes.BAKER_ID, 1, new ModelEntityBakerMale(), new ModelEntityBakerFemale());
        reg.register(ModModelTypes.BAKER);

        ModModelTypes.SHEEP_FARMER = new SimpleModelType(ModModelTypes.SHEEP_FARMER_ID, 1, new ModelEntitySheepFarmerMale(), new ModelEntitySheepFarmerFemale());
        reg.register(ModModelTypes.SHEEP_FARMER);

        ModModelTypes.COW_FARMER = new SimpleModelType(ModModelTypes.COW_FARMER_ID, 1, new ModelEntityCowFarmerMale(), new ModelEntityCowFarmerFemale());
        reg.register(ModModelTypes.COW_FARMER);

        ModModelTypes.PIG_FARMER = new SimpleModelType(ModModelTypes.PIG_FARMER_ID, 1, new ModelEntityPigFarmerMale(), new ModelEntityPigFarmerFemale());
        reg.register(ModModelTypes.PIG_FARMER);

        ModModelTypes.CHICKEN_FARMER = new SimpleModelType(ModModelTypes.CHICKEN_FARMER_ID, 1, new ModelEntityChickenFarmerMale(), new ModelEntityChickenFarmerFemale());
        reg.register(ModModelTypes.CHICKEN_FARMER);

        ModModelTypes.COMPOSTER = new SimpleModelType(ModModelTypes.COMPOSTER_ID, 1, new ModelEntityComposterMale(), new ModelEntityComposterFemale());
        reg.register(ModModelTypes.COMPOSTER);

        ModModelTypes.SMELTER = new SimpleModelType(ModModelTypes.SMELTER_ID, 1, new ModelEntitySmelterMale(), new ModelEntitySmelterFemale());
        reg.register(ModModelTypes.SMELTER);

        ModModelTypes.COOK = new SimpleModelType(ModModelTypes.COOK_ID, 1, new ModelEntityCookMale(), new ModelEntityCookFemale());
        reg.register(ModModelTypes.COOK);

        ModModelTypes.STUDENT = new SimpleModelType(ModModelTypes.STUDENT_ID, 6, new ModelEntityStudentMale(), new ModelEntityStudentFemale());
        reg.register(ModModelTypes.STUDENT);

        ModModelTypes.CRAFTER = new SimpleModelType(ModModelTypes.CRAFTER_ID, 1, new ModelEntityCrafterMale(), new ModelEntityCrafterFemale());
        reg.register(ModModelTypes.CRAFTER);

        ModModelTypes.BLACKSMITH = new SimpleModelType(ModModelTypes.BLACKSMITH_ID, 1, new ModelEntityBlacksmithMale(), new ModelEntityBlacksmithFemale());
        reg.register(ModModelTypes.BLACKSMITH);

        ModModelTypes.CHILD = new SimpleModelType(ModModelTypes.CHILD_ID, 4, new ModelEntityChildMale(), new ModelEntityChildFemale());
        reg.register(ModModelTypes.CHILD);

        ModModelTypes.HEALER = new SimpleModelType(ModModelTypes.HEALER_ID, 1, new ModelEntityHealerMale(), new ModelEntityHealerFemale());
        reg.register(ModModelTypes.HEALER);

        ModModelTypes.TEACHER = new SimpleModelType(ModModelTypes.TEACHER_ID, 1, new ModelEntityTeacherMale(), new ModelEntityTeacherFemale());
        reg.register(ModModelTypes.TEACHER);

        ModModelTypes.GLASSBLOWER = new SimpleModelType(ModModelTypes.GLASSBLOWER_ID, 3, new ModelEntityGlassblowerMale(), new ModelEntityGlassblowerFemale());
        reg.register(ModModelTypes.GLASSBLOWER);

        ModModelTypes.DYER = new SimpleModelType(ModModelTypes.DYER_ID, 3, new ModelEntityDyerMale(), new ModelEntityDyerFemale());
        reg.register(ModModelTypes.DYER);

        ModModelTypes.MECHANIST = new SimpleModelType(ModModelTypes.MECHANIST_ID, 1, new ModelEntityMechanistMale(), new ModelEntityMechanistFemale());
        reg.register(ModModelTypes.MECHANIST);

        ModModelTypes.FLETCHER = new SimpleModelType(ModModelTypes.FLETCHER_ID, 1, new ModelEntityFletcherMale(), new ModelEntityFletcherFemale());
        reg.register(ModModelTypes.FLETCHER);

        ModModelTypes.CONCRETE_MIXER = new SimpleModelType(ModModelTypes.CONCRETE_MIXER_ID, 1, new ModelEntityConcreteMixerMale(), new ModelEntityConcreteMixerFemale());
        reg.register(ModModelTypes.CONCRETE_MIXER);

        ModModelTypes.RABBIT_HERDER = new SimpleModelType(ModModelTypes.RABBIT_HERDER_ID, 1, new ModelEntityRabbitHerderMale(), new ModelEntityRabbitHerderFemale());
        reg.register(ModModelTypes.RABBIT_HERDER);

        ModModelTypes.PLANTER = new SimpleModelType(ModModelTypes.PLANTER_ID, 1, new ModelEntityPlanterMale(), new ModelEntityPlanterFemale());
        reg.register(ModModelTypes.PLANTER);

        ModModelTypes.BEEKEEPER = new SimpleModelType(ModModelTypes.BEEKEEPER_ID, 1, new ModelEntityBeekeeperMale(), new ModelEntityBeekeeperFemale());
        reg.register(ModModelTypes.BEEKEEPER);

        ModModelTypes.DRUID = new SimpleModelType(ModModelTypes.DRUID_ID, 1, new ModelEntityBeekeeperMale(), new ModelEntityBeekeeperFemale());
        reg.register(ModModelTypes.DRUID);
    }
}
