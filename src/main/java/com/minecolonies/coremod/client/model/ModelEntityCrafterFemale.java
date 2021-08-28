// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityCrafterFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityCrafterFemale()
    {
        ModelPart headdetail;
        ModelPart strap;
        ModelPart back;
        ModelPart lens1;
        ModelPart lens2;
        ModelPart HairBack1;
        ModelPart HairBack2;
        ModelPart HairBack3;
        ModelPart HairBack4;
        ModelPart HairBack5;
        ModelPart HairBack6;
        ModelPart HairBack7;
        ModelPart HairBack8;
        ModelPart HairBack9;
        ModelPart HairBack10;
        ModelPart HairBack11;
        ModelPart HairBack12;
        ModelPart HairBack13;
        ModelPart HairBack14;
        ModelPart skirtBa;
        ModelPart skirtF;
        ModelPart skirtR;
        ModelPart skirtBo1;
        ModelPart skirtT1;
        ModelPart skirtL;
        ModelPart skirtBo2;
        ModelPart chest;
        ModelPart core;
        ModelPart frontBeam;
        ModelPart SideBeam;

        texWidth = 256;
        texHeight = 128;

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headdetail = new ModelPart(this);
        headdetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        strap = new ModelPart(this);
        strap.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(strap);
        setRotationAngle(strap, 0.0F, 0.0F, 0.7854F);
        strap.texOffs(0, 85).addBox(-1.2F, -7.5F, -4.2F, 1.0F, 2.0F, 9.0F, 0.0F, true);

        back = new ModelPart(this);
        back.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(back);
        back.texOffs(12, 96).addBox(0.5F, -5.5F, -4.4F, 3.0F, 2.0F, 1.0F, 0.0F, true);

        lens1 = new ModelPart(this);
        lens1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(lens1);
        lens1.texOffs(0, 96).addBox(1.0F, -5.0F, -4.6F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        lens2 = new ModelPart(this);
        lens2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(lens2);
        lens2.texOffs(0, 98).addBox(1.25F, -5.0F, -5.0F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        HairBack1 = new ModelPart(this);
        HairBack1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack1);
        HairBack1.texOffs(0, 80).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        HairBack2 = new ModelPart(this);
        HairBack2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack2);
        setRotationAngle(HairBack2, 0.6109F, 0.0F, 0.0F);
        HairBack2.texOffs(5, 80).addBox(3.5F, -7.1F, -0.55F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        HairBack3 = new ModelPart(this);
        HairBack3.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack3);
        HairBack3.texOffs(0, 69).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, true);

        HairBack4 = new ModelPart(this);
        HairBack4.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack4);
        HairBack4.texOffs(12, 75).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        HairBack5 = new ModelPart(this);
        HairBack5.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack5);
        HairBack5.texOffs(0, 75).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        HairBack6 = new ModelPart(this);
        HairBack6.setPos(0.0F, 1.0F, 0.0F);
        head.addChild(HairBack6);
        setRotationAngle(HairBack6, 0.0F, 1.9333F, 0.0F);
        HairBack6.texOffs(0, 48).addBox(-0.1F, -1.0F, 5.1F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        HairBack7 = new ModelPart(this);
        HairBack7.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack7);
        HairBack7.texOffs(0, 62).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, true);

        HairBack8 = new ModelPart(this);
        HairBack8.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack8);
        HairBack8.texOffs(4, 48).addBox(0.5F, -0.5F, 2.6F, 4.0F, 1.0F, 2.0F, 0.0F, true);

        HairBack9 = new ModelPart(this);
        HairBack9.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack9);
        HairBack9.texOffs(0, 39).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 6.0F, 3.0F, 0.0F, true);

        HairBack10 = new ModelPart(this);
        HairBack10.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack10);
        HairBack10.texOffs(0, 51).addBox(-2.5F, -2.5F, 1.5F, 7.0F, 1.0F, 3.0F, 0.0F, true);

        HairBack11 = new ModelPart(this);
        HairBack11.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(HairBack11);
        HairBack11.texOffs(0, 55).addBox(-0.5F, -1.5F, 1.55F, 5.0F, 1.0F, 3.0F, 0.0F, true);

        HairBack12 = new ModelPart(this);
        HairBack12.setPos(0.0F, 1.0F, 0.0F);
        head.addChild(HairBack12);
        setRotationAngle(HairBack12, 0.0F, 0.4833F, 0.0F);
        HairBack12.texOffs(16, 55).addBox(0.5F, -2.5F, 3.35F, 4.0F, 2.0F, 2.0F, 0.0F, true);

        HairBack13 = new ModelPart(this);
        HairBack13.setPos(0.0F, 1.0F, 0.0F);
        head.addChild(HairBack13);
        setRotationAngle(HairBack13, 0.0F, 1.3384F, 0.0F);
        HairBack13.texOffs(0, 59).addBox(-1.0F, -2.0F, 4.8F, 4.0F, 1.0F, 2.0F, 0.0F, true);

        HairBack14 = new ModelPart(this);
        HairBack14.setPos(0.0F, 1.0F, 0.0F);
        head.addChild(HairBack14);
        setRotationAngle(HairBack14, 0.0F, 1.9333F, 0.0F);
        HairBack14.texOffs(12, 59).addBox(-1.1F, -2.0F, 5.1F, 2.0F, 1.0F, 1.0F, 0.0F, true);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        skirtBa = new ModelPart(this);
        skirtBa.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtBa);
        setRotationAngle(skirtBa, 0.4712F, 0.0F, 0.0F);
        skirtBa.texOffs(47, 41).addBox(-4.5F, 11.1F, -4.5F, 9.0F, 6.0F, 2.0F, 0.0F, true);

        skirtF = new ModelPart(this);
        skirtF.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtF);
        setRotationAngle(skirtF, -0.4712F, 0.0F, 0.0F);
        skirtF.texOffs(25, 41).addBox(-4.5F, 11.1F, 2.5F, 9.0F, 6.0F, 2.0F, 0.0F, true);

        skirtR = new ModelPart(this);
        skirtR.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtR);
        setRotationAngle(skirtR, 0.0F, 0.0F, -1.3963F);
        skirtR.texOffs(53, 49).addBox(-17.9F, -2.2F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        skirtBo1 = new ModelPart(this);
        skirtBo1.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtBo1);
        skirtBo1.texOffs(29, 56).addBox(-4.5F, 15.4F, -4.0F, 9.0F, 1.0F, 8.0F, 0.0F, true);

        skirtT1 = new ModelPart(this);
        skirtT1.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtT1);
        skirtT1.texOffs(25, 33).addBox(-4.5F, 11.0F, -3.0F, 9.0F, 2.0F, 6.0F, 0.0F, true);

        skirtL = new ModelPart(this);
        skirtL.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtL);
        setRotationAngle(skirtL, 0.0F, 0.0F, 1.3963F);
        skirtL.texOffs(29, 49).addBox(11.9F, -2.2F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        skirtBo2 = new ModelPart(this);
        skirtBo2.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(skirtBo2);
        skirtBo2.texOffs(29, 65).addBox(-4.5F, 16.4F, -5.5F, 9.0F, 1.0F, 11.0F, 0.0F, true);

        chest = new ModelPart(this);
        chest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, 0.9341F, 0.0F, 0.0F);
        chest.texOffs(0, 32).addBox(-3.5F, -0.5F, -5.5F, 7.0F, 3.0F, 3.0F, 0.0F, true);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        core = new ModelPart(this);
        core.setPos(-4.6F, 2.0F, 0.0F);
        rightArm.addChild(core);
        setRotationAngle(core, 0.0F, 0.75F, 0.0F);
        core.texOffs(38, 93).addBox(1.75F, 7.0F, 1.5F, 2.0F, 2.0F, 2.0F, 0.0F, true);

        frontBeam = new ModelPart(this);
        frontBeam.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(frontBeam);
        frontBeam.texOffs(25, 78).addBox(3.7F, -4.56F, -2.5F, 1.0F, 13.0F, 5.0F, 0.0F, true);

        SideBeam = new ModelPart(this);
        SideBeam.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(SideBeam);
        SideBeam.texOffs(38, 78).addBox(2.5F, -4.5F, -0.5F, 4.0F, 13.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
