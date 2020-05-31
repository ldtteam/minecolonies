package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.LivingEntity;

public class ModelChiefNorsemen extends NorsemenModel
{
	public ModelChiefNorsemen()
    {
        ModelRenderer FurWaist;
        ModelRenderer FurWaist2;
        ModelRenderer FurWaist3;
        ModelRenderer FurWaist4;
        ModelRenderer FurCape;
        ModelRenderer FurCape2;
        ModelRenderer Helmet;
        ModelRenderer Helmet_Horn_R;
        ModelRenderer bone2;
        ModelRenderer bone;
        ModelRenderer bone3;
        ModelRenderer bone4;
        ModelRenderer bone5;
        ModelRenderer bone6;
        ModelRenderer Helmet_Horn_R2;
        ModelRenderer bone13;
        ModelRenderer bone14;
        ModelRenderer bone15;
        ModelRenderer bone16;
        ModelRenderer bone17;
        ModelRenderer bone18;

        textureWidth = 128;
        textureHeight = 128;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 15.0F, 0.0F);
        bipedBody.setTextureOffset(30, 69).addBox(-4.0F, -15.5F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(0, 44).addBox(-4.1F, -16.03F, -2.5F, 8.0F, 13.0F, 5.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(0.0F, 15.0F, 0.0F);
        bipedRightArm.setTextureOffset(20, 69).addBox(-8.49F, -16.0F, -2.5F, 4.0F, 13.0F, 5.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(0, 80).addBox(-8.0F, -15.5F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(0.0F, 15.0F, 0.0F);
        bipedLeftArm.setTextureOffset(65, 66).addBox(3.5F, -16.0F, -2.5F, 5.0F, 13.0F, 5.0F, 0.0F, false);
        bipedLeftArm.setTextureOffset(72, 14).addBox(4.0F, -15.5F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(0.0F, 15.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 62).addBox(-4.5F, -4.0F, -2.5F, 5.0F, 13.0F, 5.0F, 0.0F, false);
        bipedRightLeg.setTextureOffset(74, 60).addBox(-4.0F, -3.5F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(0.0F, 15.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(50, 53).addBox(-0.5F, -4.0F, -2.5F, 5.0F, 13.0F, 5.0F, 0.0F, false);
        bipedLeftLeg.setTextureOffset(46, 85).addBox(0.0F, -3.5F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 15.0F, 0.0F);
        bipedHead.setTextureOffset(40, 18).addBox(-4.0F, -23.5F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(28, 36).addBox(-4.2F, -19.0F, -4.3F, 8.0F, 9.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(28, 36).addBox(-4.0F, -19.0F, -4.1F, 8.0F, 9.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(28, 36).addBox(-4.3F, -19.0F, -4.1F, 8.0F, 3.0F, 8.0F, 0.0F, false);

        FurWaist = new ModelRenderer(this);
        FurWaist.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(FurWaist);
        FurWaist.setTextureOffset(52, 34).addBox(-5.1F, -5.25F, -3.25F, 10.0F, 4.0F, 6.0F, 0.0F, false);

        FurWaist2 = new ModelRenderer(this);
        FurWaist2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(FurWaist2);
        FurWaist2.setTextureOffset(72, 0).addBox(-3.1F, -2.25F, 1.65F, 6.0F, 9.0F, 1.0F, 0.0F, false);

        FurWaist3 = new ModelRenderer(this);
        FurWaist3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(FurWaist3);
        FurWaist3.setTextureOffset(50, 0).addBox(-4.9F, -0.85F, -5.25F, 9.0F, 5.0F, 2.0F, 0.0F, false);
        FurWaist3.setTextureOffset(70, 61).addBox(-4.8F, 3.25F, -4.75F, 9.0F, 3.0F, 0.0F, 0.0F, false);

        FurWaist4 = new ModelRenderer(this);
        FurWaist4.setRotationPoint(0.0F, -0.4F, 0.0F);
        bipedBody.addChild(FurWaist4);
        setRotationAngle(FurWaist4, -0.3491F, 0.0F, 0.0F);
        FurWaist4.setTextureOffset(44, 7).addBox(-5.0F, -4.25F, -5.25F, 10.0F, 5.0F, 6.0F, 0.0F, false);

        FurCape = new ModelRenderer(this);
        FurCape.setRotationPoint(0.0F, -3.0F, -8.0F);
        bipedBody.addChild(FurCape);
        setRotationAngle(FurCape, -0.3491F, 0.0F, 0.0F);
        FurCape.setTextureOffset(0, 13).addBox(-9.4F, -11.7577F, -1.2585F, 18.0F, 7.0F, 6.0F, 0.0F, false);

        FurCape2 = new ModelRenderer(this);
        FurCape2.setRotationPoint(0.0F, -4.0F, -8.0F);
        bipedBody.addChild(FurCape2);
        setRotationAngle(FurCape2, -0.7854F, 0.0F, 0.0F);
        FurCape2.setTextureOffset(0, 0).addBox(-9.5F, -16.25F, -5.45F, 19.0F, 7.0F, 6.0F, 0.0F, false);

        Helmet = new ModelRenderer(this);
        Helmet.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(Helmet);
        Helmet.setTextureOffset(0, 26).addBox(-4.5F, -24.0F, -4.5F, 9.0F, 9.0F, 9.0F, 0.0F, false);

        Helmet_Horn_R = new ModelRenderer(this);
        Helmet_Horn_R.setRotationPoint(-11.0F, -22.0F, 2.0F);
        Helmet.addChild(Helmet_Horn_R);

        bone2 = new ModelRenderer(this);
        bone2.setRotationPoint(1.0F, -2.0F, -1.0F);
        Helmet_Horn_R.addChild(bone2);
        setRotationAngle(bone2, -1.0472F, 0.0F, 0.0F);
        bone2.setTextureOffset(4, 32).addBox(-0.3F, -1.916F, -0.2572F, 1.0F, 2.0F, 1.0F, 0.0F, false);

        bone = new ModelRenderer(this);
        bone.setRotationPoint(2.0F, -6.0F, -1.0F);
        Helmet_Horn_R.addChild(bone);
        setRotationAngle(bone, -1.0472F, 0.0F, 0.0F);
        bone.setTextureOffset(32, 29).addBox(-1.2F, -1.6873F, 3.7481F, 0.0F, 2.0F, 0.0F, 0.0F, false);

        bone3 = new ModelRenderer(this);
        bone3.setRotationPoint(0.0F, 0.0F, -4.0F);
        Helmet_Horn_R.addChild(bone3);
        setRotationAngle(bone3, 0.0F, -0.1745F, -0.3491F);
        bone3.setTextureOffset(0, 0).addBox(1.0F, -1.4F, 1.2F, 1.0F, 3.0F, 2.0F, 0.0F, false);

        bone4 = new ModelRenderer(this);
        bone4.setRotationPoint(3.4F, 0.2F, -2.4F);
        Helmet_Horn_R.addChild(bone4);
        setRotationAngle(bone4, 0.0F, 0.5236F, 0.0F);
        bone4.setTextureOffset(0, 29).addBox(-1.7F, -1.1F, -1.1F, 3.0F, 2.0F, 1.0F, 0.0F, false);

        bone5 = new ModelRenderer(this);
        bone5.setRotationPoint(4.7F, 0.3F, -4.0F);
        Helmet_Horn_R.addChild(bone5);
        setRotationAngle(bone5, 0.0F, 0.0F, 0.3491F);
        bone5.setTextureOffset(78, 30).addBox(-1.0F, -1.3F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

        bone6 = new ModelRenderer(this);
        bone6.setRotationPoint(11.0F, 22.2F, -2.0F);
        Helmet_Horn_R.addChild(bone6);
        bone6.setTextureOffset(54, 71).addBox(-5.0F, -23.3F, -3.0F, 1.0F, 4.0F, 4.0F, 0.0F, false);

        Helmet_Horn_R2 = new ModelRenderer(this);
        Helmet_Horn_R2.setRotationPoint(11.0F, -22.0F, 2.0F);
        Helmet.addChild(Helmet_Horn_R2);

        bone13 = new ModelRenderer(this);
        bone13.setRotationPoint(-1.0F, -2.0F, -1.0F);
        Helmet_Horn_R2.addChild(bone13);
        setRotationAngle(bone13, -1.0472F, 0.0F, 0.0F);
        bone13.setTextureOffset(4, 32).addBox(-0.8F, -1.916F, -0.2572F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        bone14 = new ModelRenderer(this);
        bone14.setRotationPoint(-2.0F, -6.0F, -1.0F);
        Helmet_Horn_R2.addChild(bone14);
        setRotationAngle(bone14, -1.0472F, 0.0F, 0.0F);
        bone14.setTextureOffset(32, 29).addBox(0.4F, -1.6873F, 3.7481F, 0.0F, 2.0F, 0.0F, 0.0F, true);

        bone15 = new ModelRenderer(this);
        bone15.setRotationPoint(0.0F, 0.0F, -4.0F);
        Helmet_Horn_R2.addChild(bone15);
        setRotationAngle(bone15, 0.0F, 0.1745F, 0.3491F);
        bone15.setTextureOffset(0, 0).addBox(-2.7F, -1.4F, 1.2F, 1.0F, 3.0F, 2.0F, 0.0F, true);

        bone16 = new ModelRenderer(this);
        bone16.setRotationPoint(-3.4F, 0.2F, -2.4F);
        Helmet_Horn_R2.addChild(bone16);
        setRotationAngle(bone16, 0.0F, -0.5236F, 0.0F);
        bone16.setTextureOffset(0, 29).addBox(-1.6F, -1.1F, -1.1F, 3.0F, 2.0F, 1.0F, 0.0F, true);

        bone17 = new ModelRenderer(this);
        bone17.setRotationPoint(-4.7F, 0.3F, -4.0F);
        Helmet_Horn_R2.addChild(bone17);
        setRotationAngle(bone17, 0.0F, 0.0F, -0.3491F);
        bone17.setTextureOffset(78, 30).addBox(-2.0F, -1.3F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, true);

        bone18 = new ModelRenderer(this);
        bone18.setRotationPoint(-11.0F, 22.2F, -2.0F);
        Helmet_Horn_R2.addChild(bone18);
        bone18.setTextureOffset(54, 71).addBox(4.0F, -23.3F, -3.0F, 1.0F, 4.0F, 4.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
	}

    @Override
    public void render(final LivingEntity entity, final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch)
    {
        super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        bipedHead.rotationPointY += 16;

        bipedBody.rotationPointY +=16;
        bipedRightArm.rotationPointY += 16;
        bipedLeftArm.rotationPointY += 16;

        bipedRightArm.rotationPointX += 3;
        bipedLeftArm.rotationPointX -= 3;

        bipedRightLeg.rotationPointY +=3;
        bipedLeftLeg.rotationPointY +=3;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
