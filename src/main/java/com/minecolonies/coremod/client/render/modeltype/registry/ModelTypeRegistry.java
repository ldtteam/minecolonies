package com.minecolonies.coremod.client.render.modeltype.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.model.*;
import com.minecolonies.coremod.event.ClientRegistryHandler;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.Collections;
import java.util.Map;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final Map<IModelType, CitizenModel<AbstractEntityCitizen>> maleMap   = Maps.newHashMap();
    private final Map<IModelType, CitizenModel<AbstractEntityCitizen>> femaleMap = Maps.newHashMap();

    public ModelTypeRegistry()
    {

    }

    @Override
    public void setup(final EntityRendererProvider.Context context)
    {
        register(BipedModelType.BASE, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleCitizen(context.bakeLayer(ClientRegistryHandler.FEMALE_CITIZEN)));
        register(BipedModelType.SETTLER, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleSettler(context.bakeLayer(ClientRegistryHandler.FEMALE_SETTLER)));
        register(BipedModelType.CUSTOM, new CitizenModel<>(context.bakeLayer(ModelLayers.PLAYER)), null);
        register(BipedModelType.NOBLE, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleNoble(context.bakeLayer(ClientRegistryHandler.FEMALE_CITIZENNOBLE)));
        register(BipedModelType.ARISTOCRAT, new ModelEntityMaleCitizen(context.bakeLayer(ClientRegistryHandler.MALE_CITIZEN)), new ModelEntityFemaleAristocrat(context.bakeLayer(ClientRegistryHandler.FEMALE_ARISTOCRAT)));
        register(BipedModelType.BUILDER, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new ModelEntityBuilderFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BUILDER)));
        register(BipedModelType.COURIER, new ModelEntityCourierMale(context.bakeLayer(ClientRegistryHandler.MALE_COURIER)), new ModelEntityCourierFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COURIER)));
        register(BipedModelType.MINER, new ModelEntityMinerMale(context.bakeLayer(ClientRegistryHandler.MALE_MINER)), new ModelEntityMinerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_MINER)));
        register(BipedModelType.FORESTER, new ModelEntityForesterMale(context.bakeLayer(ClientRegistryHandler.MALE_FORESTER)), new ModelEntityForesterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FORESTER)));
        register(BipedModelType.FARMER, new ModelEntityFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_FARMER)), new ModelEntityFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FARMER)));
        register(BipedModelType.UNDERTAKER, new ModelEntityUndertakerMale(context.bakeLayer(ClientRegistryHandler.MALE_UNDERTAKER)), new ModelEntityUndertakerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_UNDERTAKER)));
        register(BipedModelType.FISHER, new ModelEntityFisherMale(context.bakeLayer(ClientRegistryHandler.MALE_FISHER)), new ModelEntityFisherFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FISHER)));
        register(BipedModelType.BAKER, new ModelEntityBakerMale(context.bakeLayer(ClientRegistryHandler.MALE_BAKER)), new ModelEntityBakerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BAKER)));
        register(BipedModelType.COMPOSTER, new ModelEntityComposterMale(context.bakeLayer(ClientRegistryHandler.MALE_COMPOSTER)), new ModelEntityComposterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COMPOSTER)));
        register(BipedModelType.COOK, new ModelEntityCookMale(context.bakeLayer(ClientRegistryHandler.MALE_COOK)), new ModelEntityCookFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COOK)));
        register(BipedModelType.CHICKEN_FARMER, new ModelEntityChickenFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_CHICKENFARMER)), new ModelEntityChickenFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CHICKENFARMER)));
        register(BipedModelType.SHEEP_FARMER, new ModelEntitySheepFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_SHEEPFARMER)), new ModelEntitySheepFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_SHEEPFARMER)));
        register(BipedModelType.PIG_FARMER, new ModelEntityPigFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_PIGFARMER)), new ModelEntityPigFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_PIGFARMER)));
        register(BipedModelType.COW_FARMER, new ModelEntityCowFarmerMale(context.bakeLayer(ClientRegistryHandler.MALE_COWFARMER)), new ModelEntityCowFarmerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_COWFARMER)));
        register(BipedModelType.SMELTER, new ModelEntitySmelterMale(context.bakeLayer(ClientRegistryHandler.MALE_SMELTER)), new ModelEntitySmelterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_SMELTER)));
        register(BipedModelType.STUDENT, new ModelEntityStudentMale(context.bakeLayer(ClientRegistryHandler.MALE_STUDENT)), new ModelEntityStudentFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_STUDENT)));
        register(BipedModelType.CRAFTER, new ModelEntityCrafterMale(context.bakeLayer(ClientRegistryHandler.MALE_CRAFTER)), new ModelEntityCrafterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CRAFTER)));
        register(BipedModelType.BLACKSMITH, new ModelEntityBlacksmithMale(context.bakeLayer(ClientRegistryHandler.MALE_BLACKSMITH)), new ModelEntityBlacksmithFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BLACKSMITH)));
        register(BipedModelType.ARCHER_GUARD, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)));
        register(BipedModelType.KNIGHT_GUARD, new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)), new CitizenModel<>(context.bakeLayer(ClientRegistryHandler.CITIZEN)));
        register(BipedModelType.CHILD, new ModelEntityChildMale(context.bakeLayer(ClientRegistryHandler.MALE_CHILD)), new ModelEntityChildFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CHILD)));
        register(BipedModelType.HEALER, new ModelEntityHealerMale(context.bakeLayer(ClientRegistryHandler.MALE_HEALER)), new ModelEntityHealerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_HEALER)));
        register(BipedModelType.TEACHER, new ModelEntityTeacherMale(context.bakeLayer(ClientRegistryHandler.MALE_TEACHER)), new ModelEntityTeacherFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_TEACHER)));
        register(BipedModelType.GLASSBLOWER, new ModelEntityGlassblowerMale(context.bakeLayer(ClientRegistryHandler.MALE_GLASSBLOWER)), new ModelEntityGlassblowerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_GLASSBLOWER)));
        register(BipedModelType.DYER, new ModelEntityDyerMale(context.bakeLayer(ClientRegistryHandler.MALE_DYER)), new ModelEntityDyerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_DYER)));
        register(BipedModelType.PLANTER, new ModelEntityPlanterMale(context.bakeLayer(ClientRegistryHandler.MALE_PLANTER)), new ModelEntityPlanterFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_PLANTER)));
        register(BipedModelType.FLETCHER, new ModelEntityFletcherMale(context.bakeLayer(ClientRegistryHandler.MALE_FLETCHER)), new ModelEntityFletcherFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_FLETCHER)));
        register(BipedModelType.MECHANIST, new ModelEntityMechanistMale(context.bakeLayer(ClientRegistryHandler.MALE_MECHANIST)), new ModelEntityMechanistFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_MECHANIST)));
        register(BipedModelType.RABBIT_HERDER, new ModelEntityRabbitHerderMale(context.bakeLayer(ClientRegistryHandler.MALE_RABBITHERDER)), new ModelEntityRabbitHerderFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_RABBITHERDER)));
        register(BipedModelType.CONCRETE_MIXER, new ModelEntityConcreteMixerMale(context.bakeLayer(ClientRegistryHandler.MALE_CONCRETEMIXER)), new ModelEntityConcreteMixerFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_CONCRETEMIXER)));
        register(BipedModelType.BEEKEEPER, new ModelEntityBeekeeperMale(context.bakeLayer(ClientRegistryHandler.MALE_BEEKEEPER)), new ModelEntityBeekeeperFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BEEKEEPER)));
        register(BipedModelType.BEEKEEPER, new ModelEntityBeekeeperMale(context.bakeLayer(ClientRegistryHandler.MALE_BEEKEEPER)), new ModelEntityBeekeeperFemale(context.bakeLayer(ClientRegistryHandler.FEMALE_BEEKEEPER)));

    }

    @Override
    public IModelTypeRegistry register(final IModelType type, final CitizenModel<AbstractEntityCitizen> maleModel, final CitizenModel<AbstractEntityCitizen> femaleModel)
    {
        this.maleMap.put(type, maleModel);
        this.femaleMap.put(type, femaleModel);

        return this;
    }

    @Override
    public Map<IModelType, CitizenModel<AbstractEntityCitizen>> getMaleMap()
    {
        return Collections.unmodifiableMap(maleMap);
    }

    @Override
    public Map<IModelType, CitizenModel<AbstractEntityCitizen>> getFemaleMap()
    {
        return Collections.unmodifiableMap(femaleMap);
    }
}
