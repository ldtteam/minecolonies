package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCookFemale extends CitizenModel<AbstractEntityCitizen>
{
    //fields
    public ModelEntityCookFemale()
    {
        ModelRenderer dress1;
        ModelRenderer dress2;
        ModelRenderer dress3;
        ModelRenderer chest;
        ModelRenderer hair;

        textureWidth = 128;
        textureHeight = 128;

        dress1 = new ModelRenderer(this, 0, 49);
        dress1.addBox(0F, 0F, 1F, 8, 1, 6);
        dress1.setRotationPoint(-4F, 12F, -4F);
        dress1.setTextureSize(128, 128);
        dress1.mirror = true;
        setRotation(dress1, 0F, 0F, 0F);

        dress2 = new ModelRenderer(this, 0, 56);
        dress2.addBox(0F, 0F, 0F, 10, 4, 8);
        dress2.setRotationPoint(-5F, 13F, -4F);
        dress2.setTextureSize(128, 128);
        dress2.mirror = true;
        setRotation(dress2, 0F, 0F, 0F);

        dress3 = new ModelRenderer(this, 0, 68);
        dress3.addBox(0F, 0F, 0F, 12, 3, 10);
        dress3.setRotationPoint(-6F, 17F, -5F);
        dress3.setTextureSize(128, 128);
        dress3.mirror = true;
        setRotation(dress3, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-5F, 2F, 0F);
        bipedRightArm.setTextureSize(128, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(5F, 2F, 0F);
        bipedLeftArm.setTextureSize(128, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 17, 32);
        chest.addBox(-3.5F, 2.7F, -0.5F, 7, 3, 4);
        chest.setRotationPoint(0F, 0F, 0F);
        chest.setTextureSize(128, 128);
        chest.mirror = true;
        setRotation(chest, -0.5934119F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(128, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(128, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -2F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 0F, 0F);
        bipedBody.setTextureSize(128, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0F, 0F, 0F);

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 0F, 0F);
        bipedHead.setTextureSize(128, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0F, 0F, 0F);

        hair = new ModelRenderer(this, 0, 39);
        hair.addBox(0F, 0F, 0F, 9, 2, 8);
        hair.setRotationPoint(-4.5F, -9.2F, 0F);
        hair.setTextureSize(128, 128);
        hair.mirror = true;
        setRotation(hair, -0.8551081F, 0F, 0F);

        this.bipedHeadwear.addChild(hair);
        this.bipedBody.addChild(chest);
        bipedBody.addChild(dress1);
        bipedBody.addChild(dress2);
        bipedBody.addChild(dress3);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

}
