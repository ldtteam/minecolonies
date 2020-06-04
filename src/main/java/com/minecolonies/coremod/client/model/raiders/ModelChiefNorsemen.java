package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ModelChiefNorsemen extends NorsemenModel
{
	public ModelChiefNorsemen()
    {
        ModelRenderer Horn_L;
        ModelRenderer bone5;
        ModelRenderer bone6;
        ModelRenderer bone7;
        ModelRenderer bone8;
        ModelRenderer Horn_R;
        ModelRenderer bone4;
        ModelRenderer bone3;
        ModelRenderer bone;
        ModelRenderer bone2;
        ModelRenderer Robe2;
        ModelRenderer Robe;
        ModelRenderer Belt;
        ModelRenderer Robe3;
        ModelRenderer Robe4;


        textureWidth = 113;
        textureHeight = 105;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(69, 36).addBox(-4.0F, -3.0F, -3.7F, 8.0F, 6.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(69, 15).addBox(-4.0F, -7.5F, -4.3F, 8.0F, 12.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        Horn_L = new ModelRenderer(this);
        Horn_L.setRotationPoint(-12.4F, 0.5F, 24.7F);
        bipedHead.addChild(Horn_L);
        Horn_L.setTextureOffset(53, 65).addBox(7.4F, -8.5F, -27.7F, 1.0F, 4.0F, 4.0F, 0.0F, true);
        Horn_L.setTextureOffset(34, 98).addBox(6.4F, -8.0F, -27.2F, 1.0F, 3.0F, 3.0F, 0.0F, true);

        bone5 = new ModelRenderer(this);
        bone5.setRotationPoint(0.0F, 0.0F, 0.0F);
        Horn_L.addChild(bone5);
        setRotationAngle(bone5, 1.0472F, 0.0F, 0.0F);
        bone5.setTextureOffset(38, 99).addBox(2.7586F, -24.7417F, -3.7232F, 1.0F, 1.0F, 3.0F, 0.0F, true);

        bone6 = new ModelRenderer(this);
        bone6.setRotationPoint(0.0F, 2.0F, -0.6F);
        Horn_L.addChild(bone6);
        setRotationAngle(bone6, 1.0472F, 0.0F, 0.0F);
        bone6.setTextureOffset(36, 98).addBox(2.7586F, -25.7417F, -3.7232F, 1.0F, 2.0F, 3.0F, 0.0F, true);

        bone7 = new ModelRenderer(this);
        bone7.setRotationPoint(-3.2F, 16.0F, -23.7F);
        Horn_L.addChild(bone7);
        setRotationAngle(bone7, 0.0F, 0.0F, 0.2618F);
        bone7.setTextureOffset(33, 97).addBox(0.8586F, -25.7551F, -3.5F, 3.0F, 3.0F, 3.0F, 0.0F, true);

        bone8 = new ModelRenderer(this);
        bone8.setRotationPoint(-6.1F, 14.3F, -25.0F);
        Horn_L.addChild(bone8);
        setRotationAngle(bone8, -0.2618F, 1.0472F, 0.2618F);
        bone8.setTextureOffset(32, 98).addBox(-0.1414F, -24.7551F, -3.5F, 4.0F, 2.0F, 2.0F, 0.0F, true);

        Horn_R = new ModelRenderer(this);
        Horn_R.setRotationPoint(12.4F, 0.5F, 24.7F);
        bipedHead.addChild(Horn_R);
        Horn_R.setTextureOffset(54, 66).addBox(-8.4F, -8.5F, -27.7F, 1.0F, 4.0F, 4.0F, 0.0F, false);
        Horn_R.setTextureOffset(34, 98).addBox(-7.4F, -8.0F, -27.2F, 1.0F, 3.0F, 3.0F, 0.0F, false);

        bone4 = new ModelRenderer(this);
        bone4.setRotationPoint(0.0F, 0.0F, 0.0F);
        Horn_R.addChild(bone4);
        setRotationAngle(bone4, 1.0472F, 0.0F, 0.0F);
        bone4.setTextureOffset(38, 99).addBox(-3.7586F, -24.7417F, -3.7232F, 1.0F, 1.0F, 3.0F, 0.0F, false);

        bone3 = new ModelRenderer(this);
        bone3.setRotationPoint(0.0F, 2.0F, -0.6F);
        Horn_R.addChild(bone3);
        setRotationAngle(bone3, 1.0472F, 0.0F, 0.0F);
        bone3.setTextureOffset(36, 98).addBox(-3.7586F, -25.7417F, -3.7232F, 1.0F, 2.0F, 3.0F, 0.0F, false);

        bone = new ModelRenderer(this);
        bone.setRotationPoint(3.2F, 16.0F, -23.7F);
        Horn_R.addChild(bone);
        setRotationAngle(bone, 0.0F, 0.0F, -0.2618F);
        bone.setTextureOffset(33, 97).addBox(-3.8586F, -25.7551F, -3.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

        bone2 = new ModelRenderer(this);
        bone2.setRotationPoint(6.1F, 14.3F, -25.0F);
        Horn_R.addChild(bone2);
        setRotationAngle(bone2, -0.2618F, -1.0472F, -0.2618F);
        bone2.setTextureOffset(32, 98).addBox(-3.8586F, -24.7551F, -3.5F, 4.0F, 2.0F, 2.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        Robe2 = new ModelRenderer(this);
        Robe2.setRotationPoint(12.0F, 1.6F, -3.1F);
        bipedBody.addChild(Robe2);
        Robe2.setTextureOffset(0, 81).addBox(-21.0F, 2.3442F, -2.0417F, 18.0F, 6.0F, 6.0F, 0.0F, false);

        Robe = new ModelRenderer(this);
        Robe.setRotationPoint(-5.0F, 10.4F, -1.3F);
        Robe2.addChild(Robe);
        setRotationAngle(Robe, -0.4363F, 0.0F, 0.0F);
        Robe.setTextureOffset(0, 64).addBox(-16.0F, -14.0F, -4.0F, 18.0F, 7.0F, 6.0F, 0.0F, false);

        Belt = new ModelRenderer(this);
        Belt.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(Belt);
        Belt.setTextureOffset(64, 0).addBox(-5.0F, 10.2F, -3.0F, 10.0F, 4.0F, 6.0F, 0.0F, false);
        Belt.setTextureOffset(96, 0).addBox(-3.0F, 13.2F, 1.8F, 6.0F, 9.0F, 1.0F, 0.0F, false);

        Robe3 = new ModelRenderer(this);
        Robe3.setRotationPoint(12.6F, 14.1F, -3.6F);
        bipedBody.addChild(Robe3);
        Robe3.setTextureOffset(70, 90).addBox(-17.0F, 1.6442F, -1.7417F, 9.0F, 4.0F, 2.0F, 0.0F, false);
        Robe3.setTextureOffset(71, 93).addBox(-17.0F, 3.6442F, -1.2417F, 9.0F, 4.0F, 1.0F, 0.0F, false);

        Robe4 = new ModelRenderer(this);
        Robe4.setRotationPoint(-5.0F, 10.4F, -1.3F);
        Robe3.addChild(Robe4);
        setRotationAngle(Robe4, -0.4363F, 0.0F, 0.0F);
        Robe4.setTextureOffset(68, 88).addBox(-12.0F, -13.7892F, -4.1084F, 9.0F, 6.0F, 4.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(0, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedLeftArm.setTextureOffset(40, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(32, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightLeg.setTextureOffset(48, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedLeftLeg.setTextureOffset(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        bipedHeadwear.showModel = false;
	}

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
