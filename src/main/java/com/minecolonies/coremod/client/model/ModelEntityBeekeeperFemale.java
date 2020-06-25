package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

/**
 * Class used for the beekeeper entity model.
 */
public class ModelEntityBeekeeperFemale extends CitizenModel<AbstractEntityCitizen>
{
    private final ModelRenderer bipedbody;
    private final ModelRenderer chest;
    private final ModelRenderer bipedleftleg;
    private final ModelRenderer bipedrightleg;
    private final ModelRenderer bipedleftarm;
    private final ModelRenderer bipedrightarm;
    private final ModelRenderer bipedhead;
    private final ModelRenderer hatbuttom;
    private final ModelRenderer hattop;
    private final ModelRenderer hatright;
    private final ModelRenderer hatneck;
    private final ModelRenderer hatm;

    public ModelEntityBeekeeperFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedbody = new ModelRenderer(this);
        bipedbody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedbody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(2.0F, 3.0F, -2.0F);
        bipedbody.addChild(chest);
        setRotationAngle(chest, 0.6109F, 0.0F, 0.0F);
        chest.setTextureOffset(18, 20).addBox(-5.0F, -1.0F, -2.0F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        bipedleftleg = new ModelRenderer(this);
        bipedleftleg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedleftleg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedrightleg = new ModelRenderer(this);
        bipedrightleg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedrightleg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedleftarm = new ModelRenderer(this);
        bipedleftarm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedleftarm.setTextureOffset(36, 42).addBox(-1.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F, 0.0F, false);
        bipedleftarm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedrightarm = new ModelRenderer(this);
        bipedrightarm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedrightarm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedrightarm.setTextureOffset(36, 42).addBox(-3.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F, 0.0F, false);

        bipedhead = new ModelRenderer(this);
        bipedhead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedhead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        hatbuttom = new ModelRenderer(this);
        hatbuttom.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedhead.addChild(hatbuttom);
        setRotationAngle(hatbuttom, -0.0349F, 0.0F, 0.0F);
        hatbuttom.setTextureOffset(61, 48).addBox(-5.5F, -5.4856F, -5.457F, 11.25F, 1.0F, 10.0F, 0.0F, true);

        hattop = new ModelRenderer(this);
        hattop.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedhead.addChild(hattop);
        setRotationAngle(hattop, -0.0349F, 0.0F, 0.0F);
        hattop.setTextureOffset(70, 53).addBox(-3.5F, -9.0358F, -2.9483F, 7.0F, 1.0F, 5.0F, 0.0F, true);

        hatright = new ModelRenderer(this);
        hatright.setRotationPoint(0.0F, 0.0F, 2.0F);
        bipedhead.addChild(hatright);
        setRotationAngle(hatright, -0.0349F, 0.0F, 0.0F);
        hatright.setTextureOffset(56, 33).addBox(-5.5F, -4.3454F, 1.5018F, 11.0F, 5.0F, 1.0F, 0.0F, true);
        hatright.setTextureOffset(56, 24).addBox(4.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F, 0.0F, true);
        hatright.setTextureOffset(56, 24).addBox(-5.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F, 0.0F, true);
        hatright.setTextureOffset(57, 31).addBox(-5.5F, -4.4849F, -7.5006F, 11.0F, 5.0F, 1.0F, 0.0F, true);

        hatneck = new ModelRenderer(this);
        hatneck.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedhead.addChild(hatneck);
        setRotationAngle(hatneck, -0.3491F, 0.0F, 0.0F);
        hatneck.setTextureOffset(98, 14).addBox(-4.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F, 0.0F, true);
        hatneck.setTextureOffset(98, 14).addBox(3.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F, 0.0F, true);
        hatneck.setTextureOffset(98, 14).addBox(-4.5F, 1.05F, -2.7F, 8.75F, 0.75F, 1.0F, 0.0F, true);

        hatm = new ModelRenderer(this);
        hatm.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedhead.addChild(hatm);
        setRotationAngle(hatm, -0.0349F, 0.0F, 0.0F);
        hatm.setTextureOffset(58, 11).addBox(-4.75F, -8.3358F, -4.6983F, 9.5F, 3.0F, 8.5F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
