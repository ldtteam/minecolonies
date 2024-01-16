package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.ModModelTypes;
import com.minecolonies.api.client.render.modeltype.SimpleModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.core.client.model.*;
import com.minecolonies.core.event.ClientRegistryHandler;
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

        ModModelTypes.SETTLER = new SimpleModelType(ModModelTypes.SETTLER_ID, 3, new MaleSettlerModel(context.bakeLayer(ClientRegistryHandler.MALE_SETTLER)), new FemaleSettlerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_SETTLER)));
        reg.register(ModModelTypes.SETTLER);

        ModModelTypes.CUSTOM = new SimpleModelType(ModModelTypes.CUSTOM_ID, 1, new CitizenModel<>(context.bakeLayer(ModelLayers.PLAYER)), null);
        reg.register(ModModelTypes.CUSTOM);

        ModModelTypes.CITIZEN = new SimpleModelType(ModModelTypes.CITIZEN_ID, 3, new MaleCitizenModel(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new FemaleCitizenModel(context.bakeLayer(ClientRegistryHandler.FEMALE_CITIZEN)));
        reg.register(ModModelTypes.CITIZEN);

        ModModelTypes.NOBLE = new SimpleModelType(ModModelTypes.NOBLE_ID, 3, new MaleNobleModel(context.bakeLayer(ClientRegistryHandler.MALE_CITIZENNOBLE)), new FemaleNobleModle(context.bakeLayer(ClientRegistryHandler.FEMALE_CITIZENNOBLE)));
        reg.register(ModModelTypes.NOBLE);

        ModModelTypes.ARISTOCRAT = new SimpleModelType(ModModelTypes.ARISTOCRAT_ID, 3, new MaleAristocratModel(context.bakeLayer(ClientRegistryHandler.MALE_ARISTOCRAT)), new FemaleAristocratModel(context.bakeLayer(ClientRegistryHandler.FEMALE_ARISTOCRAT)));
        reg.register(ModModelTypes.ARISTOCRAT);

        ModModelTypes.BUILDER = new SimpleModelType(ModModelTypes.BUILDER_ID, 1, new MaleBuilderModel(context.bakeLayer(ClientRegistryHandler.MALE_BUILDER)), new FemaleBuilderModel(context.bakeLayer(ClientRegistryHandler.FEMALE_BUILDER)));
        reg.register(ModModelTypes.BUILDER);

        ModModelTypes.DELIVERYMAN = new SimpleModelType(ModModelTypes.COURIER_ID, 1, new MaleCourierModel(context.bakeLayer(ClientRegistryHandler.MALE_COURIER)), new FemaleCourierModel(context.bakeLayer(ClientRegistryHandler.FEMALE_COURIER)));
        reg.register(ModModelTypes.DELIVERYMAN);

        ModModelTypes.MINER = new SimpleModelType(ModModelTypes.MINER_ID, 1, new MaleMinerModel(context.bakeLayer(ClientRegistryHandler.MALE_MINER)), new FemaleMinerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_MINER)));
        reg.register(ModModelTypes.MINER);

        ModModelTypes.LUMBERJACK = new SimpleModelType(ModModelTypes.LUMBERJACK_ID, 1, new MaleForesterModel(context.bakeLayer(ClientRegistryHandler.MALE_FORESTER)), new FemaleForesterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_FORESTER)));
        reg.register(ModModelTypes.LUMBERJACK);

        ModModelTypes.FARMER = new SimpleModelType(ModModelTypes.FARMER_ID, 1, new MaleFarmerModel(context.bakeLayer(ClientRegistryHandler.MALE_FARMER)), new FemaleFarmerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_FARMER)));
        reg.register(ModModelTypes.FARMER);

        ModModelTypes.FISHERMAN = new SimpleModelType(ModModelTypes.FISHERMAN_ID, 1, new MaleFisherModel(context.bakeLayer(ClientRegistryHandler.MALE_FISHER)), new FemaleFisherModel(context.bakeLayer(ClientRegistryHandler.FEMALE_FISHER)));
        reg.register(ModModelTypes.FISHERMAN);

        ModModelTypes.UNDERTAKER = new SimpleModelType(ModModelTypes.UNDERTAKER_ID, 1, new MaleUndertakerModel(context.bakeLayer(ClientRegistryHandler.MALE_UNDERTAKER)), new FemaleUndertakerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_UNDERTAKER)));
        reg.register(ModModelTypes.UNDERTAKER);

        ModModelTypes.ARCHER_GUARD = new SimpleModelType(ModModelTypes.ARCHER_GUARD_ID, 1, new MaleArcherModel(context.bakeLayer(ClientRegistryHandler.MALE_ARCHER)), new FemaleArcherModel(context.bakeLayer(ClientRegistryHandler.FEMALE_ARCHER)));
        reg.register(ModModelTypes.ARCHER_GUARD);

        ModModelTypes.KNIGHT_GUARD = new SimpleModelType(ModModelTypes.KNIGHT_ID, 1, new MaleKnightModel(context.bakeLayer(ClientRegistryHandler.MALE_KNIGHT)), new FemaleKnightModel(context.bakeLayer(ClientRegistryHandler.FEMALE_KNIGHT)));
        reg.register(ModModelTypes.KNIGHT_GUARD);

        ModModelTypes.BAKER = new SimpleModelType(ModModelTypes.BAKER_ID, 1, new MaleBakerModel(context.bakeLayer(ClientRegistryHandler.MALE_BAKER)), new FemaleBakerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_BAKER)));
        reg.register(ModModelTypes.BAKER);

        ModModelTypes.SHEEP_FARMER = new SimpleModelType(ModModelTypes.SHEEP_FARMER_ID, 1, new MaleShepherdModel(context.bakeLayer(ClientRegistryHandler.MALE_SHEEPFARMER)), new FemaleShepherdModel(context.bakeLayer(ClientRegistryHandler.FEMALE_SHEEPFARMER)));
        reg.register(ModModelTypes.SHEEP_FARMER);

        ModModelTypes.COW_FARMER = new SimpleModelType(ModModelTypes.COW_FARMER_ID, 1, new MaleCowHerderModel(context.bakeLayer(ClientRegistryHandler.MALE_COWFARMER)), new FemaleCowHerderModel(context.bakeLayer(ClientRegistryHandler.FEMALE_COWFARMER)));
        reg.register(ModModelTypes.COW_FARMER);

        ModModelTypes.PIG_FARMER = new SimpleModelType(ModModelTypes.PIG_FARMER_ID, 1, new MaleSwineHerderModel(context.bakeLayer(ClientRegistryHandler.MALE_PIGFARMER)), new FemaleSwineHerderModel(context.bakeLayer(ClientRegistryHandler.FEMALE_PIGFARMER)));
        reg.register(ModModelTypes.PIG_FARMER);

        ModModelTypes.CHICKEN_FARMER = new SimpleModelType(ModModelTypes.CHICKEN_FARMER_ID, 1, new MaleChickenHerderModel(context.bakeLayer(ClientRegistryHandler.MALE_CHICKENFARMER)), new FemaleChickenHerderModel(context.bakeLayer(ClientRegistryHandler.FEMALE_CHICKENFARMER)));
        reg.register(ModModelTypes.CHICKEN_FARMER);

        ModModelTypes.COMPOSTER = new SimpleModelType(ModModelTypes.COMPOSTER_ID, 1, new MaleComposterModel(context.bakeLayer(ClientRegistryHandler.MALE_COMPOSTER)), new FemaleComposterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_COMPOSTER)));
        reg.register(ModModelTypes.COMPOSTER);

        ModModelTypes.SMELTER = new SimpleModelType(ModModelTypes.SMELTER_ID, 1, new MaleSmelterModel(context.bakeLayer(ClientRegistryHandler.MALE_SMELTER)), new FemaleSmelterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_SMELTER)));
        reg.register(ModModelTypes.SMELTER);

        ModModelTypes.COOK = new SimpleModelType(ModModelTypes.COOK_ID, 1, new MaleCookModel(context.bakeLayer(ClientRegistryHandler.MALE_COOK)), new FemaleCookModel(context.bakeLayer(ClientRegistryHandler.FEMALE_COOK)));
        reg.register(ModModelTypes.COOK);

        ModModelTypes.STUDENT = new SimpleModelType(ModModelTypes.STUDENT_ID, 3, new MaleStudentModel(context.bakeLayer(ClientRegistryHandler.MALE_STUDENT)), new FemaleStudentModel(context.bakeLayer(ClientRegistryHandler.FEMALE_STUDENT)));
        reg.register(ModModelTypes.STUDENT);

        ModModelTypes.CRAFTER = new SimpleModelType(ModModelTypes.CRAFTER_ID, 1, new MaleCrafterModel(context.bakeLayer(ClientRegistryHandler.MALE_CRAFTER)), new FemaleCrafterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_CRAFTER)));
        reg.register(ModModelTypes.CRAFTER);

        ModModelTypes.BLACKSMITH = new SimpleModelType(ModModelTypes.BLACKSMITH_ID, 1, new MaleBlacksmithModel(context.bakeLayer(ClientRegistryHandler.MALE_BLACKSMITH)), new FemaleBlacksmithModel(context.bakeLayer(ClientRegistryHandler.FEMALE_BLACKSMITH)));
        reg.register(ModModelTypes.BLACKSMITH);

        ModModelTypes.CHILD = new SimpleModelType(ModModelTypes.CHILD_ID, 3, new MaleChildModel(context.bakeLayer(ClientRegistryHandler.MALE_CHILD)), new FemaleChildModel(context.bakeLayer(ClientRegistryHandler.FEMALE_CHILD)));
        reg.register(ModModelTypes.CHILD);

        ModModelTypes.HEALER = new SimpleModelType(ModModelTypes.HEALER_ID, 1, new MaleHealerModel(context.bakeLayer(ClientRegistryHandler.MALE_HEALER)), new FemaleHealerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_HEALER)));
        reg.register(ModModelTypes.HEALER);

        ModModelTypes.TEACHER = new SimpleModelType(ModModelTypes.TEACHER_ID, 1, new MaleTeacherModel(context.bakeLayer(ClientRegistryHandler.MALE_TEACHER)), new FemaleTeacherModel(context.bakeLayer(ClientRegistryHandler.FEMALE_TEACHER)));
        reg.register(ModModelTypes.TEACHER);

        ModModelTypes.GLASSBLOWER = new SimpleModelType(ModModelTypes.GLASSBLOWER_ID, 1, new MaleGlassblowerModel(context.bakeLayer(ClientRegistryHandler.MALE_GLASSBLOWER)), new FemaleGlassblowerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_GLASSBLOWER)));
        reg.register(ModModelTypes.GLASSBLOWER);

        ModModelTypes.DYER = new SimpleModelType(ModModelTypes.DYER_ID, 1, new MaleDyerModel(context.bakeLayer(ClientRegistryHandler.MALE_DYER)), new FemaleDyerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_DYER)));
        reg.register(ModModelTypes.DYER);

        ModModelTypes.MECHANIST = new SimpleModelType(ModModelTypes.MECHANIST_ID, 1, new MaleMechanistModel(context.bakeLayer(ClientRegistryHandler.MALE_MECHANIST)), new FemaleMechanistModel(context.bakeLayer(ClientRegistryHandler.FEMALE_MECHANIST)));
        reg.register(ModModelTypes.MECHANIST);

        ModModelTypes.FLETCHER = new SimpleModelType(ModModelTypes.FLETCHER_ID, 1, new MaleFletcherModel(context.bakeLayer(ClientRegistryHandler.MALE_FLETCHER)), new FemaleFletcherModel(context.bakeLayer(ClientRegistryHandler.FEMALE_FLETCHER)));
        reg.register(ModModelTypes.FLETCHER);

        ModModelTypes.CONCRETE_MIXER = new SimpleModelType(ModModelTypes.CONCRETE_MIXER_ID, 1, new MaleConcreteMixerModel(context.bakeLayer(ClientRegistryHandler.MALE_CONCRETEMIXER)), new FemaleConcreteMixerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_CONCRETEMIXER)));
        reg.register(ModModelTypes.CONCRETE_MIXER);

        ModModelTypes.RABBIT_HERDER = new SimpleModelType(ModModelTypes.RABBIT_HERDER_ID, 1, new MaleRabbitHerderModel(context.bakeLayer(ClientRegistryHandler.MALE_RABBITHERDER)), new FemaleRabbitHerderModel(context.bakeLayer(ClientRegistryHandler.FEMALE_RABBITHERDER)));
        reg.register(ModModelTypes.RABBIT_HERDER);

        ModModelTypes.PLANTER = new SimpleModelType(ModModelTypes.PLANTER_ID, 1, new MalePlanterModel(context.bakeLayer(ClientRegistryHandler.MALE_PLANTER)), new FemalePlanterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_PLANTER)));
        reg.register(ModModelTypes.PLANTER);

        ModModelTypes.BEEKEEPER = new SimpleModelType(ModModelTypes.BEEKEEPER_ID, 1, new MaleApiaryModel(context.bakeLayer(ClientRegistryHandler.MALE_BEEKEEPER)), new FemaleApiaryModel(context.bakeLayer(ClientRegistryHandler.FEMALE_BEEKEEPER)));
        reg.register(ModModelTypes.BEEKEEPER);

        ModModelTypes.NETHERWORKER = new SimpleModelType(ModModelTypes.NETHERWORKER_ID, 1, new MaleNetherWorkerModel(context.bakeLayer(ClientRegistryHandler.MALE_NETHERWORKER)), new FemaleNetherWorkerModel(context.bakeLayer(ClientRegistryHandler.FEMALE_NETHERWORKER)));
        reg.register(ModModelTypes.NETHERWORKER);

        ModModelTypes.DRUID = new SimpleModelType(ModModelTypes.DRUID_ID, 1, new MaleDruidModel(context.bakeLayer(ClientRegistryHandler.MALE_DRUID)), new FemaleDruidModel(context.bakeLayer(ClientRegistryHandler.FEMALE_DRUID)));
        reg.register(ModModelTypes.DRUID);

        ModModelTypes.ENCHANTER = new SimpleModelType(ModModelTypes.ENCHANTER_ID, 1, new MaleEnchanterModel(context.bakeLayer(ClientRegistryHandler.MALE_ENCHANTER)), new FemaleEnchanterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_ENCHANTER)));
        reg.register(ModModelTypes.ENCHANTER);

        ModModelTypes.FLORIST = new SimpleModelType(ModModelTypes.FLORIST_ID, 1, new MaleFloristModel(context.bakeLayer(ClientRegistryHandler.MALE_FLORIST)), new FemaleFloristModel(context.bakeLayer(ClientRegistryHandler.FEMALE_FLORIST)));
        reg.register(ModModelTypes.FLORIST);

        ModModelTypes.CARPENTER = new SimpleModelType(ModModelTypes.CARPENTER_ID, 1, new MaleCarpenterModel(context.bakeLayer(ClientRegistryHandler.MALE_CARPENTER)), new FemaleCarpenterModel(context.bakeLayer(ClientRegistryHandler.FEMALE_CARPENTER)));
        reg.register(ModModelTypes.CARPENTER);

        ModModelTypes.ALCHEMIST = new SimpleModelType(ModModelTypes.ALCHEMIST_ID, 1, new MaleAlchemistModel(context.bakeLayer(ClientRegistryHandler.MALE_ALCHEMIST)), new FemaleAlchemistModel(context.bakeLayer(ClientRegistryHandler.FEMALE_ALCHEMIST)));
        reg.register(ModModelTypes.ALCHEMIST);
    }
}
