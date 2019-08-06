package com.minecolonies.coremod.client.render.modeltype.registry;

import com.google.common.collect.Maps;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.coremod.client.model.*;
import net.minecraft.client.renderer.entity.model.BipedModel;

import java.util.Collections;
import java.util.Map;

public class ModelTypeRegistry implements IModelTypeRegistry
{
    private final Map<IModelType, BipedModel> maleMap   = Maps.newHashMap();
    private final Map<IModelType, BipedModel> femaleMap = Maps.newHashMap();

    public ModelTypeRegistry()
    {
        register(BipedModelType.NOBLE, new BipedModel(), new ModelEntityCitizenFemaleNoble());
        register(BipedModelType.ARISTOCRAT, new BipedModel(), new ModelEntityCitizenFemaleAristocrat());
        register(BipedModelType.BUILDER, new BipedModel(), new ModelEntityBuilderFemale());
        register(BipedModelType.DELIVERYMAN, new ModelEntityDeliverymanMale(), new ModelEntityDeliverymanFemale());
        register(BipedModelType.MINER, new BipedModel(), new ModelEntityMinerFemale());
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
        register(BipedModelType.ARCHER_GUARD, new BipedModel(), new BipedModel());
        register(BipedModelType.KNIGHT_GUARD, new BipedModel(), new BipedModel());
    }

    @Override
    public IModelTypeRegistry register(final IModelType type, final BipedModel maleModel, final BipedModel femaleModel)
    {
        this.maleMap.put(type, maleModel);
        this.femaleMap.put(type, femaleModel);

        return this;
    }

    @Override
    public Map<IModelType, BipedModel> getMaleMap()
    {
        return Collections.unmodifiableMap(maleMap);
    }

    @Override
    public Map<IModelType, BipedModel> getFemaleMap()
    {
        return Collections.unmodifiableMap(femaleMap);
    }
}
