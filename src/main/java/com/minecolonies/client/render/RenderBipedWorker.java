package com.minecolonies.client.render;

import com.minecolonies.entity.EntityCitizen;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLiving;

public class RenderBipedWorker extends RenderBipedCitizen
{
    ModelBiped maleModel, femaleModel;

    public RenderBipedWorker(ModelBiped maleModel, ModelBiped femaleModel)
    {
        super(maleModel);
        this.maleModel = maleModel;
        this.femaleModel = femaleModel;
    }

    @Override
    public void doRender(EntityLiving entityliving, double d, double d1, double d2, float f, float f1)
    {
        if(entityliving instanceof EntityCitizen)
        {
            EntityCitizen entityCitizen = (EntityCitizen) entityliving;

            if(entityCitizen.isFemale())
            {
                modelBipedMain = femaleModel;
            }
            else
            {
                modelBipedMain = maleModel;
            }
            mainModel = modelBipedMain;
        }
        super.doRender(entityliving, d, d1, d2, f, f1);
    }
}