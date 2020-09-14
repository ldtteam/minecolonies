// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityCrafterFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityCrafterFemale()
    {
        ModelRenderer headdetail;
        ModelRenderer strap;
        ModelRenderer back;
        ModelRenderer lens1;
        ModelRenderer lens2;
        ModelRenderer HairBack1;
        ModelRenderer HairBack2;
        ModelRenderer HairBack3;
        ModelRenderer HairBack4;
        ModelRenderer HairBack5;
        ModelRenderer HairBack6;
        ModelRenderer HairBack7;
        ModelRenderer HairBack8;
        ModelRenderer HairBack9;
        ModelRenderer HairBack10;
        ModelRenderer HairBack11;
        ModelRenderer HairBack12;
        ModelRenderer HairBack13;
        ModelRenderer HairBack14;
        ModelRenderer skirtBa;
        ModelRenderer skirtF;
        ModelRenderer skirtR;
        ModelRenderer skirtBo1;
        ModelRenderer skirtT1;
        ModelRenderer skirtL;
        ModelRenderer skirtBo2;
        ModelRenderer chest;
        ModelRenderer core;
        ModelRenderer frontBeam;
        ModelRenderer SideBeam;

        textureWidth = 256;
        textureHeight = 128;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headdetail = new ModelRenderer(this);
        headdetail.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(headdetail);
        headdetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        strap = new ModelRenderer(this);
        strap.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(strap);
        setRotationAngle(strap, 0.0F, 0.0F, 0.7854F);
        strap.setTextureOffset(0, 85).addBox(-1.2F, -7.5F, -4.2F, 1.0F, 2.0F, 9.0F, 0.0F, true);

        back = new ModelRenderer(this);
        back.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(back);
        back.setTextureOffset(12, 96).addBox(0.5F, -5.5F, -4.4F, 3.0F, 2.0F, 1.0F, 0.0F, true);

        lens1 = new ModelRenderer(this);
        lens1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(lens1);
        lens1.setTextureOffset(0, 96).addBox(1.0F, -5.0F, -4.6F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        lens2 = new ModelRenderer(this);
        lens2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(lens2);
        lens2.setTextureOffset(0, 98).addBox(1.25F, -5.0F, -5.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        HairBack1 = new ModelRenderer(this);
        HairBack1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack1);
        HairBack1.setTextureOffset(0, 80).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        HairBack2 = new ModelRenderer(this);
        HairBack2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack2);
        setRotationAngle(HairBack2, 0.6109F, 0.0F, 0.0F);
        HairBack2.setTextureOffset(5, 80).addBox(3.5F, -7.1F, -0.55F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        HairBack3 = new ModelRenderer(this);
        HairBack3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack3);
        HairBack3.setTextureOffset(0, 69).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, true);

        HairBack4 = new ModelRenderer(this);
        HairBack4.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack4);
        HairBack4.setTextureOffset(12, 75).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        HairBack5 = new ModelRenderer(this);
        HairBack5.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack5);
        HairBack5.setTextureOffset(0, 75).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        HairBack6 = new ModelRenderer(this);
        HairBack6.setRotationPoint(0.0F, 1.0F, 0.0F);
        bipedHead.addChild(HairBack6);
        setRotationAngle(HairBack6, 0.0F, 1.9333F, 0.0F);
        HairBack6.setTextureOffset(0, 48).addBox(-0.1F, -1.0F, 5.1F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        HairBack7 = new ModelRenderer(this);
        HairBack7.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack7);
        HairBack7.setTextureOffset(0, 62).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, true);

        HairBack8 = new ModelRenderer(this);
        HairBack8.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack8);
        HairBack8.setTextureOffset(4, 48).addBox(0.5F, -0.5F, 2.6F, 4.0F, 1.0F, 2.0F, 0.0F, true);

        HairBack9 = new ModelRenderer(this);
        HairBack9.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack9);
        HairBack9.setTextureOffset(0, 39).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 6.0F, 3.0F, 0.0F, true);

        HairBack10 = new ModelRenderer(this);
        HairBack10.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack10);
        HairBack10.setTextureOffset(0, 51).addBox(-2.5F, -2.5F, 1.5F, 7.0F, 1.0F, 3.0F, 0.0F, true);

        HairBack11 = new ModelRenderer(this);
        HairBack11.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(HairBack11);
        HairBack11.setTextureOffset(0, 55).addBox(-0.5F, -1.5F, 1.55F, 5.0F, 1.0F, 3.0F, 0.0F, true);

        HairBack12 = new ModelRenderer(this);
        HairBack12.setRotationPoint(0.0F, 1.0F, 0.0F);
        bipedHead.addChild(HairBack12);
        setRotationAngle(HairBack12, 0.0F, 0.4833F, 0.0F);
        HairBack12.setTextureOffset(16, 55).addBox(0.5F, -2.5F, 3.35F, 4.0F, 2.0F, 2.0F, 0.0F, true);

        HairBack13 = new ModelRenderer(this);
        HairBack13.setRotationPoint(0.0F, 1.0F, 0.0F);
        bipedHead.addChild(HairBack13);
        setRotationAngle(HairBack13, 0.0F, 1.3384F, 0.0F);
        HairBack13.setTextureOffset(0, 59).addBox(-1.0F, -2.0F, 4.8F, 4.0F, 1.0F, 2.0F, 0.0F, true);

        HairBack14 = new ModelRenderer(this);
        HairBack14.setRotationPoint(0.0F, 1.0F, 0.0F);
        bipedHead.addChild(HairBack14);
        setRotationAngle(HairBack14, 0.0F, 1.9333F, 0.0F);
        HairBack14.setTextureOffset(12, 59).addBox(-1.1F, -2.0F, 5.1F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        skirtBa = new ModelRenderer(this);
        skirtBa.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtBa);
        setRotationAngle(skirtBa, 0.4712F, 0.0F, 0.0F);
        skirtBa.setTextureOffset(47, 41).addBox(-4.5F, 11.1F, -4.5F, 9.0F, 6.0F, 2.0F, 0.0F, true);

        skirtF = new ModelRenderer(this);
        skirtF.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtF);
        setRotationAngle(skirtF, -0.4712F, 0.0F, 0.0F);
        skirtF.setTextureOffset(25, 41).addBox(-4.5F, 11.1F, 2.5F, 9.0F, 6.0F, 2.0F, 0.0F, true);

        skirtR = new ModelRenderer(this);
        skirtR.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtR);
        setRotationAngle(skirtR, 0.0F, 0.0F, -1.3963F);
        skirtR.setTextureOffset(53, 49).addBox(-17.9F, -2.2F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        skirtBo1 = new ModelRenderer(this);
        skirtBo1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtBo1);
        skirtBo1.setTextureOffset(29, 56).addBox(-4.5F, 15.4F, -4.0F, 9.0F, 1.0F, 8.0F, 0.0F, true);

        skirtT1 = new ModelRenderer(this);
        skirtT1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtT1);
        skirtT1.setTextureOffset(25, 33).addBox(-4.5F, 11.0F, -3.0F, 9.0F, 2.0F, 6.0F, 0.0F, true);

        skirtL = new ModelRenderer(this);
        skirtL.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtL);
        setRotationAngle(skirtL, 0.0F, 0.0F, 1.3963F);
        skirtL.setTextureOffset(29, 49).addBox(11.9F, -2.2F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        skirtBo2 = new ModelRenderer(this);
        skirtBo2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(skirtBo2);
        skirtBo2.setTextureOffset(29, 65).addBox(-4.5F, 16.4F, -5.5F, 9.0F, 1.0F, 11.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, 0.9341F, 0.0F, 0.0F);
        chest.setTextureOffset(0, 32).addBox(-3.5F, -0.5F, -5.5F, 7.0F, 3.0F, 3.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        core = new ModelRenderer(this);
        core.setRotationPoint(-4.6F, 2.0F, 0.0F);
        bipedRightArm.addChild(core);
        setRotationAngle(core, 0.0F, 0.75F, 0.0F);
        core.setTextureOffset(38, 93).addBox(1.75F, 7.0F, 1.5F, 2.0F, 2.0F, 2.0F, 0.0F, true);

        frontBeam = new ModelRenderer(this);
        frontBeam.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(frontBeam);
        frontBeam.setTextureOffset(25, 78).addBox(3.7F, -4.56F, -2.5F, 1.0F, 13.0F, 5.0F, 0.0F, true);

        SideBeam = new ModelRenderer(this);
        SideBeam.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.addChild(SideBeam);
        SideBeam.setTextureOffset(38, 78).addBox(2.5F, -4.5F, -0.5F, 4.0F, 13.0F, 1.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
