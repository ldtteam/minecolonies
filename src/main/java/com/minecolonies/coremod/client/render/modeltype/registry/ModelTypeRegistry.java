package com.minecolonies.coremod.client.render.modeltype.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.client.model.*;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.entity.model.BipedModel;

import java.util.Collections;
import java.util.Map;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final Map<IModelType, CitizenModel> maleMap   = Maps.newHashMap();
    private final Map<IModelType, CitizenModel> femaleMap = Maps.newHashMap();

    public ModelTypeRegistry()
    {
        register(BipedModelType.NOBLE, new CitizenModel(), new ModelEntityCitizenFemaleNoble());
        register(BipedModelType.ARISTOCRAT, new CitizenModel(), new ModelEntityCitizenFemaleAristocrat());
        register(BipedModelType.BUILDER, new CitizenModel(), new ModelEntityBuilderFemale());
        register(BipedModelType.DELIVERYMAN, new ModelEntityDeliverymanMale(), new ModelEntityDeliverymanFemale());
        register(BipedModelType.MINER, new CitizenModel(), new ModelEntityMinerFemale());
        register(BipedModelType.LUMBERJACK, new ModelEntityLumberjackMale(), new ModelEntityLumberjackFemale());
        register(BipedModelType.FARMER, new ModelEntityFarmerMale(), new ModelEntityFarmerFemale());
        register(BipedModelType.FISHERMAN, new ModelEntityFishermanMale(), new ModelEntityFishermanFemale());
        register(BipedModelType.BAKER, new ModelEntityBakerMale(), new ModelEntityBakerFemale());
        register(BipedModelType.COMPOSTER, new ModelEntityComposterMale(), new ModelEntityComposterFemale());
        register(BipedModelType.COOK, new ModelEntityCookMale(), new ModelEntityCookFemale());
        register(BipedModelType.CHICKEN_FARMER, new ModelEntityChickenFarmerMale(), new ModelEntityChickenFarmerFemale());
        register(BipedModelType.SHEEP_FARMER, new ModelEntitySheepFarmerMale(), new ModelEntitySheepFarmerFemale());
        register(BipedModelType.PIG_FARMER, new ModelEntityPigFarmerMale(), new ModelEntityPigFarmerFemale());
        register(BipedModelType.COW_FARMER, new ModelEntityCowFarmerMale(), new ModelEntityCowFarmerFemale());
        register(BipedModelType.SMELTER, new ModelEntitySmelterMale(), new ModelEntitySmelterFemale());
        register(BipedModelType.STUDENT, new ModelEntityStudentMale(), new ModelEntityStudentFemale());
        register(BipedModelType.CRAFTER, new ModelEntityCrafterMale(), new ModelEntityCrafterFemale());
        register(BipedModelType.BLACKSMITH, new ModelEntityBlacksmithMale(), new ModelEntityBlacksmithFemale());
        register(BipedModelType.ARCHER_GUARD, new CitizenModel(), new CitizenModel());
        register(BipedModelType.KNIGHT_GUARD, new CitizenModel(), new CitizenModel());
        register(BipedModelType.CHILD, new ModelEntityChildMale(), new ModelEntityChildFemale());
    }

    @Override
    public IModelTypeRegistry register(final IModelType type, final CitizenModel maleModel, final CitizenModel femaleModel)
    {
        this.maleMap.put(type, maleModel);
        this.femaleMap.put(type, femaleModel);

        return this;
    }

    @Override
    public Map<IModelType, CitizenModel> getMaleMap()
    {
        return Collections.unmodifiableMap(maleMap);
    }

    @Override
    public Map<IModelType, CitizenModel> getFemaleMap()
    {
        return Collections.unmodifiableMap(femaleMap);
    }
}
