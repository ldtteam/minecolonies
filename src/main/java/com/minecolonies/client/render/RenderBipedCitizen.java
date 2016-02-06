package com.minecolonies.client.render;

import com.minecolonies.client.model.*;
import com.minecolonies.entity.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RenderBipedCitizen extends RenderBiped
{
    public enum Model
    {
        SETTLER     ( "Settler",        3 ),
        CITIZEN     ( "Citizen",        3 ),
        NOBLE       ( "Noble",          3 ),
        ARISTOCRAT  ( "Aristocrat",     3 ),
        BUILDER     ( "Builder",        1 ),
        DELIVERYMAN ( "Deliveryman",    1 ),
        MINER       ( "Miner",          1 ),
        LUMBERJACK  ( "Lumberjack",     1 ),//4 male, 1 female
        FARMER      ( "Farmer",         1 );

        Model(String textureBase, int numTextures)
        {
            this.textureBase = textureBase;
            this.numTextures = numTextures;
        }

        public final String textureBase;
        public final int numTextures;
    }

    private static final ModelBiped defaultModelMale = new ModelBiped();
    private static final ModelBiped defaultModelFemale = new ModelEntityCitizenFemaleCitizen();
    private static final Map<Model, ModelBiped> idToMaleModelMap = new HashMap<Model, ModelBiped>();
    private static final Map<Model, ModelBiped> idToFemaleModelMap = new HashMap<Model, ModelBiped>();

    static
    {
        idToMaleModelMap.put(Model.DELIVERYMAN,     new ModelEntityDeliverymanMale());
        idToMaleModelMap.put(Model.LUMBERJACK,      new ModelEntityLumberjackMale());
        idToMaleModelMap.put(Model.FARMER,          new ModelEntityFarmerMale());

        idToFemaleModelMap.put(Model.NOBLE,         new ModelEntityCitizenFemaleNoble());
        idToFemaleModelMap.put(Model.ARISTOCRAT,    new ModelEntityCitizenFemaleAristocrat());
        idToFemaleModelMap.put(Model.BUILDER,       new ModelEntityBuilderFemale());
        idToFemaleModelMap.put(Model.DELIVERYMAN,   new ModelEntityDeliverymanMale());//TODO female
        idToFemaleModelMap.put(Model.MINER,         new ModelEntityMinerFemale());
        idToFemaleModelMap.put(Model.LUMBERJACK,    new ModelEntityLumberjackFemale());
        idToFemaleModelMap.put(Model.FARMER,        new ModelEntityFarmerFemale());
    }

    public RenderBipedCitizen()
    {
        super(defaultModelMale, 0.5f);
    }

    @Override
    public void doRender(EntityLiving entity, double d, double d1, double d2, float f, float f1)
    {
        if (entity instanceof EntityCitizen)
        {
            EntityCitizen citizen = (EntityCitizen) entity;

            modelBipedMain = citizen.isFemale() ?
                    idToFemaleModelMap.get(citizen.getModelID()) :
                    idToMaleModelMap.get(citizen.getModelID());

            if (modelBipedMain == null)
            {
                modelBipedMain = citizen.isFemale() ? defaultModelFemale : defaultModelMale;
            }

            mainModel = modelBipedMain;
        }
        super.doRender(entity, d, d1, d2, f, f1);
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        EntityCitizen entityCitizen = (EntityCitizen) entity;
        return entityCitizen.getTexture();
    }
}
