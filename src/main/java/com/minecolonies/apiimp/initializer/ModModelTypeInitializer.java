package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.model.*;

public class ModModelTypeInitializer
{
    private ModModelTypeInitializer() {
        throw new IllegalStateException("Tried to initialize: ModModelTypeInitializer but this is a Utility class.");
    }

    public static void init() {
        final IModelTypeRegistry reg = IModelTypeRegistry.getInstance();

        ModModelTypes.settler = new BipedModelType(ModModelTypes.SETTLER_ID,3, new CitizenModel<>(), new ModelEntityFemaleCitizen());
        reg.register(ModModelTypes.settler);

        ModModelTypes.citizen = new BipedModelType(ModModelTypes.CITIZEN_ID, 3, new CitizenModel<>(), new ModelEntityFemaleCitizen());
        reg.register(ModModelTypes.citizen);

        ModModelTypes.noble = new BipedModelType(ModModelTypes.NOBLE_ID, 3, new CitizenModel<>(), new ModelEntityCitizenFemaleNoble());
        reg.register(ModModelTypes.noble);

        ModModelTypes.aristocrat = new BipedModelType(ModModelTypes.ARISTOCRAT_ID, 3, new CitizenModel<>(), new ModelEntityFemaleAristocrat());
        reg.register(ModModelTypes.aristocrat);

        ModModelTypes.builder = new BipedModelType(ModModelTypes.BUILDER_ID, 1, new CitizenModel<>(), new ModelEntityBuilderFemale());
        reg.register(ModModelTypes.builder);

        ModModelTypes.deliveryman = new BipedModelType(ModModelTypes.DELIVERYMAN_ID, 1, new ModelEntityDeliverymanMale(), new ModelEntityDeliverymanFemale());
        reg.register(ModModelTypes.deliveryman);

        ModModelTypes.miner = new BipedModelType(ModModelTypes.MINER_ID, 1, new ModelEntityMinerMale(), new ModelEntityMinerFemale());
        reg.register(ModModelTypes.miner);

        // Lumberjack: 4 male, 1 female
        ModModelTypes.lumberjack = new BipedModelType(ModModelTypes.LUMBERJACK_ID, 1, new ModelEntityLumberjackMale(), new ModelEntityLumberjackFemale());
        reg.register(ModModelTypes.lumberjack);

        ModModelTypes.farmer = new BipedModelType(ModModelTypes.FARMER_ID, 1, new ModelEntityFarmerMale(), new ModelEntityFarmerFemale());
        reg.register(ModModelTypes.farmer);

        ModModelTypes.fisherman = new BipedModelType(ModModelTypes.FISHERMAN_ID, 1, new ModelEntityFishermanMale(), new ModelEntityFishermanFemale());
        reg.register(ModModelTypes.fisherman);

        ModModelTypes.undertaker = new BipedModelType(ModModelTypes.UNDERTAKER_ID, 1, new ModelEntityUndertakerMale(), new ModelEntityUndertakerFemale());
        reg.register(ModModelTypes.undertaker);

        ModModelTypes.archerGuard = new BipedModelType(ModModelTypes.ARCHER_GUARD_ID, 1, new CitizenModel<>(), new CitizenModel<>());
        reg.register(ModModelTypes.archerGuard);

        ModModelTypes.knightGuard = new BipedModelType(ModModelTypes.KNIGHT_GUARD_ID, 1, new CitizenModel<>(), new CitizenModel<>());
        reg.register(ModModelTypes.knightGuard);

        ModModelTypes.baker = new BipedModelType(ModModelTypes.BAKER_ID, 1, new ModelEntityBakerMale(), new ModelEntityBakerFemale());
        reg.register(ModModelTypes.baker);

        ModModelTypes.sheepFarmer = new BipedModelType(ModModelTypes.SHEEP_FARMER_ID, 1, new ModelEntitySheepFarmerMale(), new ModelEntitySheepFarmerFemale());
        reg.register(ModModelTypes.sheepFarmer);

        ModModelTypes.cowFarmer = new BipedModelType(ModModelTypes.COW_FARMER_ID, 1, new ModelEntityCowFarmerMale(), new ModelEntityCowFarmerFemale());
        reg.register(ModModelTypes.cowFarmer);

        ModModelTypes.pigFarmer = new BipedModelType(ModModelTypes.PIG_FARMER_ID, 1, new ModelEntityPigFarmerMale(), new ModelEntityPigFarmerFemale());
        reg.register(ModModelTypes.pigFarmer);

        ModModelTypes.chickenFarmer = new BipedModelType(ModModelTypes.CHICKEN_FARMER_ID, 1, new ModelEntityChickenFarmerMale(), new ModelEntityChickenFarmerFemale());
        reg.register(ModModelTypes.chickenFarmer);

        ModModelTypes.composter = new BipedModelType(ModModelTypes.COMPOSTER_ID, 1, new ModelEntityComposterMale(), new ModelEntityComposterFemale());
        reg.register(ModModelTypes.composter);

        ModModelTypes.smelter = new BipedModelType(ModModelTypes.SMELTER_ID, 1, new ModelEntitySmelterMale(), new ModelEntitySmelterFemale());
        reg.register(ModModelTypes.smelter);

        ModModelTypes.cook = new BipedModelType(ModModelTypes.COOK_ID, 1, new ModelEntityCookMale(), new ModelEntityCookFemale());
        reg.register(ModModelTypes.cook);

        ModModelTypes.student = new BipedModelType(ModModelTypes.STUDENT_ID, 6, new ModelEntityStudentMale(), new ModelEntityStudentFemale());
        reg.register(ModModelTypes.student);

        ModModelTypes.crafter = new BipedModelType(ModModelTypes.CRAFTER_ID, 1, new ModelEntityCrafterMale(), new ModelEntityCrafterFemale());
        reg.register(ModModelTypes.crafter);

        ModModelTypes.blacksmith = new BipedModelType(ModModelTypes.BLACKSMITH_ID, 1, new ModelEntityBlacksmithMale(), new ModelEntityBlacksmithFemale());
        reg.register(ModModelTypes.blacksmith);

        ModModelTypes.child = new BipedModelType(ModModelTypes.CHILD_ID, 4, new ModelEntityChildMale(), new ModelEntityChildFemale());
        reg.register(ModModelTypes.child);

        ModModelTypes.healer = new BipedModelType(ModModelTypes.HEALER_ID, 1, new ModelEntityHealerMale(), new ModelEntityHealerFemale());
        reg.register(ModModelTypes.healer);

        ModModelTypes.teacher = new BipedModelType(ModModelTypes.TEACHER_ID, 1, new ModelEntityTeacherMale(), new ModelEntityTeacherFemale());
        reg.register(ModModelTypes.teacher);

        ModModelTypes.glassblower = new BipedModelType(ModModelTypes.GLASSBLOWER_ID, 3, new ModelEntityGlassblowerMale(), new ModelEntityGlassblowerFemale());
        reg.register(ModModelTypes.glassblower);

        ModModelTypes.dyer = new BipedModelType(ModModelTypes.DYER_ID, 3, new ModelEntityDyerMale(), new ModelEntityDyerFemale());
        reg.register(ModModelTypes.dyer);

        ModModelTypes.mechanist = new BipedModelType(ModModelTypes.MECHANIST_ID, 1, new ModelEntityMechanistMale(), new ModelEntityMechanistFemale());
        reg.register(ModModelTypes.mechanist);

        ModModelTypes.fletcher = new BipedModelType(ModModelTypes.FLETCHER_ID, 1, new ModelEntityFletcherMale(), new ModelEntityFletcherFemale());
        reg.register(ModModelTypes.fletcher);

        ModModelTypes.concreteMixer = new BipedModelType(ModModelTypes.CONCRETE_MIXER_ID, 1, new ModelEntityConcreteMixerMale(), new ModelEntityConcreteMixerFemale());
        reg.register(ModModelTypes.concreteMixer);

        ModModelTypes.rabbitHerder = new BipedModelType(ModModelTypes.RABBIT_HERDER_ID, 1, new ModelEntityRabbitHerderMale(), new ModelEntityRabbitHerderFemale());
        reg.register(ModModelTypes.rabbitHerder);

        ModModelTypes.planter = new BipedModelType(ModModelTypes.PLANTER_ID, 1, new ModelEntityPlanterMale(), new ModelEntityPlanterFemale());
        reg.register(ModModelTypes.planter);

        ModModelTypes.beekeeper = new BipedModelType(ModModelTypes.BEEKEEPER_ID, 1, new ModelEntityBeekeeperMale(), new ModelEntityBeekeeperFemale());
        reg.register(ModModelTypes.beekeeper);
    }
}
