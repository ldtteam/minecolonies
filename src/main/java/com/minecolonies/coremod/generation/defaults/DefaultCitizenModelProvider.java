package com.minecolonies.coremod.generation.defaults;

import com.minecolonies.api.client.render.modeltype.modularcitizen.AbstractCitizenModelProvider;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.client.render.modeltype.IModelType;
import com.minecolonies.coremod.client.model.*;
import com.minecolonies.coremod.client.model.basemodels.ModelEntityBaseDwarf;
import com.minecolonies.coremod.client.model.basemodels.ModelEntityBaseFemale;
import com.minecolonies.coremod.client.model.basemodels.ModelEntityBaseKobold;
import com.minecolonies.coremod.client.model.basemodels.ModelEntityBaseMale;
import net.minecraft.data.DataGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.client.render.modeltype.modularcitizen.ModularCitizenResourceContainer.STYLES;
public class DefaultCitizenModelProvider extends AbstractCitizenModelProvider
{
    // Patterns here are Style -> Setter -> Model.
    private final HashMap<String, HashMap<IModelType, List<CitizenModel>>>  maleModels   = new HashMap<>();
    private final  HashMap<String, HashMap<IModelType, List<CitizenModel>>> femaleModels = new HashMap<>();
    private final  HashMap<String, CitizenModel>                            baseModels   = new HashMap<>();
    private static String                                                   currentStyle;

    public DefaultCitizenModelProvider(final DataGenerator generator)
    {
        super(generator);

        registerBase(BaseModels.STEVE.id, new ModelEntityBaseMale(64, 64));
        registerBase(BaseModels.ALEX.id, new ModelEntityBaseFemale());
        registerBase(BaseModels.DWARF.id, new ModelEntityBaseDwarf());
        registerBase(BaseModels.KOBOLD.id, new ModelEntityBaseKobold());

        for(final String style : STYLES)
        {
            currentStyle = style;
            if(!maleModels.containsKey(style))
            {
                maleModels.put(style, new HashMap<>());
                femaleModels.put(style, new HashMap<>());
            }
            register(BipedModelType.BUILDER, new ModelEntityBaseMale(), new ModelEntityBuilderFemale());
            register(BipedModelType.DELIVERYMAN, new ModelEntityDeliverymanMale(), new ModelEntityDeliverymanFemale());
            register(BipedModelType.MINER, new ModelEntityMinerMale(), new ModelEntityMinerFemale());
            register(BipedModelType.LUMBERJACK, new ModelEntityLumberjackMale(), new ModelEntityLumberjackFemale());
            register(BipedModelType.FARMER, new ModelEntityFarmerMale(), new ModelEntityFarmerFemale());
            register(BipedModelType.UNDERTAKER, new ModelEntityUndertakerMale(), new ModelEntityUndertakerFemale());
            register(BipedModelType.FISHERMAN, new ModelEntityFishermanMale(), new ModelEntityFishermanFemale());
            register(BipedModelType.BAKER, new ModelEntityBakerMale(), new ModelEntityBakerFemale());
            register(BipedModelType.COMPOSTER, new ModelEntityComposterMale(), new ModelEntityComposterFemale());
            register(BipedModelType.COOK, new ModelEntityCookMale(), new ModelEntityCookFemale());
            register(BipedModelType.CHICKEN_FARMER, new ModelEntityChickenFarmerMale(), new ModelEntityChickenFarmerFemale());
            register(BipedModelType.SHEEP_FARMER, new ModelEntitySheepFarmerMale(), new ModelEntitySheepFarmerFemale());
            register(BipedModelType.PIG_FARMER, new ModelEntityPigFarmerMale(), new ModelEntityPigFarmerFemale());
            register(BipedModelType.COW_FARMER, new ModelEntityCowFarmerMale(), new ModelEntityCowFarmerFemale());
            register(BipedModelType.SMELTER, new ModelEntitySmelterMale(), new ModelEntitySmelterFemale());
            register(BipedModelType.CRAFTER, new ModelEntityCrafterMale(), new ModelEntityCrafterFemale());
            register(BipedModelType.ARCHER_GUARD, new ModelEntityBaseMale(), new ModelEntityBaseFemale());
            register(BipedModelType.KNIGHT_GUARD, new ModelEntityBaseMale(), new ModelEntityBaseFemale());
            register(BipedModelType.HEALER, new ModelEntityHealerMale(), new ModelEntityHealerFemale());
            register(BipedModelType.TEACHER, new ModelEntityTeacherMale(), new ModelEntityTeacherFemale());
            register(BipedModelType.GLASSBLOWER, new ModelEntityGlassblowerMale(), new ModelEntityGlassblowerFemale());
            register(BipedModelType.DYER, new ModelEntityDyerMale(), new ModelEntityDyerFemale());
            register(BipedModelType.PLANTER, new ModelEntityPlanterMale(), new ModelEntityPlanterFemale());
            register(BipedModelType.FLETCHER, new ModelEntityFletcherMale(), new ModelEntityFletcherFemale());
            register(BipedModelType.MECHANIST, new ModelEntityMechanistMale(), new ModelEntityMechanistFemale());
            register(BipedModelType.RABBIT_HERDER, new ModelEntityRabbitHerderMale(), new ModelEntityRabbitHerderFemale());
            register(BipedModelType.CONCRETE_MIXER, new ModelEntityConcreteMixerMale(), new ModelEntityConcreteMixerFemale());
            register(BipedModelType.BEEKEEPER, new ModelEntityBeekeeperMale(), new ModelEntityBeekeeperFemale());

            register(BipedModelType.STUDENT, new ModelEntityStudentMale(), new ModelEntityStudentFemale());
            register(BipedModelType.STUDENT_MONK, hideArms(new ModelEntityStudentMale()), hideArms(new ModelEntityStudentFemale()));
            register(BipedModelType.BLACKSMITH, new ModelEntityBlacksmithMale(), BaseModels.DWARF.id, new ModelEntityBlacksmithFemale(), BaseModels.KOBOLD.id);

            register(BipedModelType.NOBLE, new ModelEntityBaseMale(), new ModelEntityCitizenFemaleNoble());
            register(BipedModelType.ARISTOCRAT, new ModelEntityBaseMale(), new ModelEntityFemaleAristocrat());
            register(BipedModelType.SETTLER, new ModelEntityBaseMale(), new ModelEntityFemaleCitizen());
            register(BipedModelType.NOBLE, new ModelEntityBaseMale(), new ModelEntityFemaleCitizen());
            register(BipedModelType.CHILD, new ModelEntityChildMale(), new ModelEntityChildFemale());
        }
    }

    private CitizenModel hideArms(CitizenModel model)
    {
        model.bipedLeftArm.showModel = false;
        model.bipedRightArm.showModel = false;
        return model;
    }

    private void register(final IModelType type, final CitizenModel male, final CitizenModel female)
    {
        male.baseModel = BaseModels.STEVE.id;
        female.baseModel = BaseModels.ALEX.id;
        if(!femaleModels.get(currentStyle).containsKey(type))
        {
            femaleModels.get(currentStyle).put(type, new ArrayList<>());
        }
        if(!maleModels.get(currentStyle).containsKey(type))
        {
            maleModels.get(currentStyle).put(type, new ArrayList<>());
        }
        maleModels.get(currentStyle).get(type).add(male);
        femaleModels.get(currentStyle).get(type).add(female);
    }

    private void register(final IModelType type, final CitizenModel male, final String maleBaseModel, final CitizenModel female, final String femaleBaseModel)
    {
        male.baseModel = maleBaseModel;
        female.baseModel = femaleBaseModel;
        if(!femaleModels.get(currentStyle).containsKey(type))
        {
            femaleModels.get(currentStyle).put(type, new ArrayList<>());
        }
        if(!maleModels.get(currentStyle).containsKey(type))
        {
            maleModels.get(currentStyle).put(type, new ArrayList<>());
        }
        maleModels.get(currentStyle).get(type).add(male);
        femaleModels.get(currentStyle).get(type).add(female);
    }

    private void registerBase(final String style, final CitizenModel base)
    {
        baseModels.put(style, base);
    }

    @Override
    public Map<String, CitizenModel> getBaseModels()
    {
        return baseModels;
    }

    @Override
    public Map<IModelType, List<CitizenModel>> getMaleModels(final String style)
    {
        return maleModels.get(style);
    }

    @Override
    public Map<IModelType, List<CitizenModel>> getFemaleModels(final String style)
    {
        return femaleModels.get(style);
    }

    private enum BaseModels
    {
        STEVE("steve"),
        ALEX("alex"),
        DWARF("dwarf"),
        KOBOLD("kobold");

        final String id;

        BaseModels(final String id)
        {
            this.id = id;
        }
    }
}
