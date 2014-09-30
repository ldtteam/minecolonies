package com.minecolonies.client.render;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLiving;

public class RenderBipedCitizenMulti extends RenderBipedCitizen
{
    ModelBiped modelBase, femaleModelCitizenAndSettler, femaleModelNoble, femaleModelAristocrat;

    public RenderBipedCitizenMulti(ModelBiped modelBase, ModelBiped femaleModelCitizenAndSettler, ModelBiped femaleModelNoble, ModelBiped femaleModelAristocrat)
    {
        super(modelBase);
        this.femaleModelCitizenAndSettler = femaleModelCitizenAndSettler;
        this.femaleModelNoble = femaleModelNoble;
        this.femaleModelAristocrat = femaleModelAristocrat;
        this.modelBase = modelBase;
    }

    @Override
    public void doRender(EntityLiving entityliving, double d, double d1, double d2, float f, float f1)
    {
        if(entityliving instanceof EntityCitizen)
        {
            EntityCitizen entityCitizen = (EntityCitizen) entityliving;

            if(entityCitizen.getIsFemale())
            {
                switch(entityCitizen.getLevel())
                {
                    case 0:
                    case 1:
                        modelBipedMain = femaleModelCitizenAndSettler;
                        break;
                    case 2:
                        modelBipedMain = femaleModelNoble;
                        break;
                    case 3:
                        modelBipedMain = femaleModelAristocrat;
                        break;
                }
            }
            else
            {
                modelBipedMain = modelBase;
            }
            mainModel = modelBipedMain;
        }
        super.doRender(entityliving, d, d1, d2, f, f1);
    }
}