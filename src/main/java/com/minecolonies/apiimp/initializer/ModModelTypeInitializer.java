package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.SimpleModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.model.*;
import com.minecolonies.coremod.event.ClientRegistryHandler;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class ModModelTypeInitializer
{
    private ModModelTypeInitializer()
    {
        throw new IllegalStateException("Tried to initialize: ModModelTypeInitializer but this is a Utility class.");
    }

    public static void init(final EntityRendererProvider.Context context)
    {
        final IModelTypeRegistry reg = IModelTypeRegistry.getInstance();

        ModModelTypes.SETTLER = new SimpleModelType(ModModelTypes.SETTLER_ID, 3, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleCitizen(context.bakeLayer(ClientRegistryHandler.FEMALE_CITIZEN)));
        reg.register(ModModelTypes.SETTLER);

        ModModelTypes.CUSTOM = new SimpleModelType(ModModelTypes.CUSTOM_ID, 1, new CitizenModel<>(context.bakeLayer(ModelLayers.PLAYER)), null);
        reg.register(ModModelTypes.CUSTOM);

        ModModelTypes.CITIZEN = new SimpleModelType(ModModelTypes.CITIZEN_ID, 3, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleCitizen(context.bakeLayer(ClientRegistryHandler.FEMALE_SETTLER)));
        reg.register(ModModelTypes.CITIZEN);

        ModModelTypes.NOBLE = new SimpleModelType(ModModelTypes.NOBLE_ID, 3, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new ModelEntityFemaleNoble(context.bakeLayer(ClientRegistryHandler.FEMALE_CITIZENNOBLE)));
        reg.register(ModModelTypes.NOBLE);

        ModModelTypes.ARISTOCRAT = new SimpleModelType(ModModelTypes.ARISTOCRAT_ID, 3, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleAristocrat(context.bakeLayer(ClientRegistryHandler.FEMALE_ARISTOCRAT)));
        reg.register(ModModelTypes.ARISTOCRAT);

        ModModelTypes.BUILDER = new SimpleModelType(ModModelTypes.BUILDER_ID, 1, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new ModelEntityBuilderFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BUILDER)));
        reg.register(ModModelTypes.BUILDER);

        ModModelTypes.DELIVERYMAN = new SimpleModelType(ModModelTypes.DELIVERYMAN_ID, 1, new ModelEntityCourierMale(context.bakeLayer(ClientRegistryHandler.MALE_COURIER)), new ModelEntityCourierFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COURIER)));
        reg.register(ModModelTypes.DELIVERYMAN);

        ModModelTypes.MINER = new SimpleModelType(ModModelTypes.MINER_ID, 1, new ModelEntityMinerMale(context.bakeLayer(ClientRegistryHandler.MALE_MINER)), new ModelEntityMinerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_MINER)));
        reg.register(ModModelTypes.MINER);

        // Lumberjack: 4 male, 1 female
        ModModelTypes.LUMBERJACK = new SimpleModelType(ModModelTypes.LUMBERJACK_ID, 1, new ModelEntityForesterMale(context.bakeLayer(ClientRegistryHandler.MALE_FORESTER)), new ModelEntityForesterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FORESTER)));
        reg.register(ModModelTypes.LUMBERJACK);

        ModModelTypes.FARMER = new SimpleModelType(ModModelTypes.FARMER_ID, 1, new ModelEntityFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_FARMER)), new ModelEntityFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FARMER)));
        reg.register(ModModelTypes.FARMER);

        ModModelTypes.FISHERMAN = new SimpleModelType(ModModelTypes.FISHERMAN_ID, 1, new ModelEntityFisherMale(context.bakeLayer(ClientRegistryHandler.MALE_FISHER)), new ModelEntityFisherFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FISHER)));
        reg.register(ModModelTypes.FISHERMAN);

        ModModelTypes.UNDERTAKER = new SimpleModelType(ModModelTypes.UNDERTAKER_ID, 1, new ModelEntityUndertakerMale(context.bakeLayer(ClientRegistryHandler.MALE_UNDERTAKER)), new ModelEntityUndertakerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_UNDERTAKER)));
        reg.register(ModModelTypes.UNDERTAKER);

        ModModelTypes.ARCHER_GUARD = new SimpleModelType(ModModelTypes.ARCHER_GUARD_ID, 1, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)));
        reg.register(ModModelTypes.ARCHER_GUARD);

        ModModelTypes.KNIGHT_GUARD = new SimpleModelType(ModModelTypes.KNIGHT_GUARD_ID, 1, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)));
        reg.register(ModModelTypes.KNIGHT_GUARD);

        ModModelTypes.BAKER = new SimpleModelType(ModModelTypes.BAKER_ID, 1, new ModelEntityBakerMale(context.bakeLayer(ClientRegistryHandler.MALE_BAKER)), new ModelEntityBakerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BAKER)));
        reg.register(ModModelTypes.BAKER);

        ModModelTypes.SHEEP_FARMER = new SimpleModelType(ModModelTypes.SHEEP_FARMER_ID, 1, new ModelEntitySheepFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_SHEEPFARMER)), new ModelEntitySheepFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_SHEEPFARMER)));
        reg.register(ModModelTypes.SHEEP_FARMER);

        ModModelTypes.COW_FARMER = new SimpleModelType(ModModelTypes.COW_FARMER_ID, 1, new ModelEntityCowFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_COWFARMER)), new ModelEntityCowFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COWFARMER)));
        reg.register(ModModelTypes.COW_FARMER);

        ModModelTypes.PIG_FARMER = new SimpleModelType(ModModelTypes.PIG_FARMER_ID, 1, new ModelEntityPigFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_PIGFARMER)), new ModelEntityPigFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_PIGFARMER)));
        reg.register(ModModelTypes.PIG_FARMER);

        ModModelTypes.CHICKEN_FARMER = new SimpleModelType(ModModelTypes.CHICKEN_FARMER_ID, 1, new ModelEntityChickenFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_CHICKENFARMER)), new ModelEntityChickenFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CHICKENFARMER)));
        reg.register(ModModelTypes.CHICKEN_FARMER);

        ModModelTypes.COMPOSTER = new SimpleModelType(ModModelTypes.COMPOSTER_ID, 1, new ModelEntityComposterMale(context.bakeLayer(ClientRegistryHandler.MALE_COMPOSTER)), new ModelEntityComposterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COMPOSTER)));
        reg.register(ModModelTypes.COMPOSTER);

        ModModelTypes.SMELTER = new SimpleModelType(ModModelTypes.SMELTER_ID, 1, new ModelEntitySmelterMale(context.bakeLayer(ClientRegistryHandler.MALE_SMELTER)), new ModelEntitySmelterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_SMELTER)));
        reg.register(ModModelTypes.SMELTER);

        ModModelTypes.COOK = new SimpleModelType(ModModelTypes.COOK_ID, 1, new ModelEntityCookMale(context.bakeLayer(ClientRegistryHandler.MALE_COOK)), new ModelEntityCookFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COOK)));
        reg.register(ModModelTypes.COOK);

        ModModelTypes.STUDENT = new SimpleModelType(ModModelTypes.STUDENT_ID, 6, new ModelEntityStudentMale(context.bakeLayer(ClientRegistryHandler.MALE_STUDENT)), new ModelEntityStudentFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_STUDENT)));
        reg.register(ModModelTypes.STUDENT);

        ModModelTypes.CRAFTER = new SimpleModelType(ModModelTypes.CRAFTER_ID, 1, new ModelEntityCrafterMale(context.bakeLayer(ClientRegistryHandler.MALE_CRAFTER)), new ModelEntityCrafterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CRAFTER)));
        reg.register(ModModelTypes.CRAFTER);

        ModModelTypes.BLACKSMITH = new SimpleModelType(ModModelTypes.BLACKSMITH_ID, 1, new ModelEntityBlacksmithMale(context.bakeLayer(ClientRegistryHandler.MALE_BLACKSMITH)), new ModelEntityBlacksmithFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BLACKSMITH)));
        reg.register(ModModelTypes.BLACKSMITH);

        ModModelTypes.CHILD = new SimpleModelType(ModModelTypes.CHILD_ID, 4, new ModelEntityChildMale(context.bakeLayer(ClientRegistryHandler.MALE_CHILD)), new ModelEntityChildFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CHILD)));
        reg.register(ModModelTypes.CHILD);

        ModModelTypes.HEALER = new SimpleModelType(ModModelTypes.HEALER_ID, 1, new ModelEntityHealerMale(context.bakeLayer(ClientRegistryHandler.MALE_HEALER)), new ModelEntityHealerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_HEALER)));
        reg.register(ModModelTypes.HEALER);

        ModModelTypes.TEACHER = new SimpleModelType(ModModelTypes.TEACHER_ID, 1, new ModelEntityTeacherMale(context.bakeLayer(ClientRegistryHandler.MALE_TEACHER)), new ModelEntityTeacherFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_TEACHER)));
        reg.register(ModModelTypes.TEACHER);

        ModModelTypes.GLASSBLOWER = new SimpleModelType(ModModelTypes.GLASSBLOWER_ID, 3, new ModelEntityGlassblowerMale(context.bakeLayer(ClientRegistryHandler.MALE_GLASSBLOWER)), new ModelEntityGlassblowerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_GLASSBLOWER)));
        reg.register(ModModelTypes.GLASSBLOWER);

        ModModelTypes.DYER = new SimpleModelType(ModModelTypes.DYER_ID, 3, new ModelEntityDyerMale(context.bakeLayer(ClientRegistryHandler.MALE_DYER)), new ModelEntityDyerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_DYER)));
        reg.register(ModModelTypes.DYER);

        ModModelTypes.MECHANIST = new SimpleModelType(ModModelTypes.MECHANIST_ID, 1, new ModelEntityMechanistMale(context.bakeLayer(ClientRegistryHandler.MALE_MECHANIST)), new ModelEntityMechanistFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_MECHANIST)));
        reg.register(ModModelTypes.MECHANIST);

        ModModelTypes.FLETCHER = new SimpleModelType(ModModelTypes.FLETCHER_ID, 1, new ModelEntityFletcherMale(context.bakeLayer(ClientRegistryHandler.MALE_FLETCHER)), new ModelEntityFletcherFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FLETCHER)));
        reg.register(ModModelTypes.FLETCHER);

        ModModelTypes.CONCRETE_MIXER = new SimpleModelType(ModModelTypes.CONCRETE_MIXER_ID, 1, new ModelEntityConcreteMixerMale(context.bakeLayer(ClientRegistryHandler.MALE_CONCRETEMIXER)), new ModelEntityConcreteMixerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CONCRETEMIXER)));
        reg.register(ModModelTypes.CONCRETE_MIXER);

        ModModelTypes.RABBIT_HERDER = new SimpleModelType(ModModelTypes.RABBIT_HERDER_ID, 1, new ModelEntityRabbitHerderMale(context.bakeLayer(ClientRegistryHandler.MALE_RABBITHERDER)), new ModelEntityRabbitHerderFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_RABBITHERDER)));
        reg.register(ModModelTypes.RABBIT_HERDER);

        ModModelTypes.PLANTER = new SimpleModelType(ModModelTypes.PLANTER_ID, 1, new ModelEntityPlanterMale(context.bakeLayer(ClientRegistryHandler.MALE_PLANTER)), new ModelEntityPlanterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_PLANTER)));
        reg.register(ModModelTypes.PLANTER);

        ModModelTypes.BEEKEEPER = new SimpleModelType(ModModelTypes.BEEKEEPER_ID, 1, new ModelEntityBeekeeperMale(context.bakeLayer(ClientRegistryHandler.MALE_BEEKEEPER)), new ModelEntityBeekeeperFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BEEKEEPER)));
        reg.register(ModModelTypes.BEEKEEPER);

        ModModelTypes.NETHERWORKER = new SimpleModelType(ModModelTypes.NETHERWORKER_ID, 1, new ModelEntityNetherWorkerMale(context.bakeLayer(ClientRegistryHandler.MALE_NETHERWORKER)), new ModelEntityNetherWorkerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_NETHERWORKER)));
        reg.register(ModModelTypes.NETHERWORKER);

        ModModelTypes.DRUID = new SimpleModelType(ModModelTypes.DRUID_ID, 1, new ModelEntityDruidMale(context.bakeLayer(ClientRegistryHandler.MALE_DRUID)), new ModelEntityDruidFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_DRUID)));
        reg.register(ModModelTypes.DRUID);

        ModModelTypes.ARCHEOLOGIST = new SimpleModelType(ModModelTypes.ARCHEOLOGIST_ID, 1, new ModelEntityArcheologistMale(context.bakeLayer(ClientRegistryHandler.MALE_ARCHEOLOGIST)), new ModelEntityArcheologistFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_ARCHEOLOGIST)));
        reg.register(ModModelTypes.ARCHEOLOGIST);
    }
}
