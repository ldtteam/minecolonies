// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityBlacksmithMale extends CitizenModel<AbstractEntityCitizen> 
{

    public ModelEntityBlacksmithMale() 
    {
        ModelPart headdetail;
        ModelPart hair;
        ModelPart beard;
        ModelPart beardB;
        ModelPart beardMMB;
        ModelPart beardMBB;
        ModelPart beardring1;
        ModelPart beardBBB2;
        ModelPart beardMB;
        ModelPart moustacheLB;
        ModelPart beardRM;
        ModelPart beardTM;
        ModelPart beardTT;
        ModelPart moustacheTM;
        ModelPart moustacheLM;
        ModelPart moustacheRM;
        ModelPart moustacheRB;
        ModelPart hairM;
        ModelPart beardLM;

        texWidth = 128;
        texHeight = 128;

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 6.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, false);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 6.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 14.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 14.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        body = new ModelPart(this);
        body.setPos(0.0F, 4.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 4.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelPart(this);
        headdetail.setPos(0.0F, 20.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelPart(this);
        hair.setPos(0.0F, 20.7F, -0.4F);
        head.addChild(hair);
        hair.texOffs(0, 39).addBox(3.3F, -23.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        hair.texOffs(4, 40).addBox(-4.2F, -23.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(8, 40).addBox(-4.0F, -24.5F, -4.5F, 8.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(19, 35).addBox(-3.5F, -25.15F, -3.75F, 7.0F, 2.0F, 7.0F, 0.0F, true);
        hair.texOffs(40, 39).addBox(-4.5F, -24.5F, -3.5F, 9.0F, 2.0F, 1.0F, 0.0F, true);
        hair.texOffs(40, 36).addBox(-4.5F, -24.5F, -2.5F, 9.0F, 2.0F, 1.0F, 0.0F, true);
        hair.texOffs(60, 38).addBox(-4.5F, -24.5F, -1.5F, 9.0F, 2.0F, 2.0F, 0.0F, true);
        hair.texOffs(82, 36).addBox(-4.5F, -24.5F, 0.5F, 9.0F, 4.0F, 2.0F, 0.0F, true);
        hair.texOffs(47, 42).addBox(-4.5F, -24.5F, 2.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(67, 42).addBox(-4.5F, -22.5F, -0.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(0, 44).addBox(-4.5F, -23.5F, 2.5F, 9.0F, 6.0F, 2.0F, 0.0F, true);
        hair.texOffs(87, 42).addBox(-4.5F, -17.5F, 3.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(9, 38).addBox(-3.5F, -16.5F, 3.5F, 7.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(20, 44).addBox(-3.5F, -15.5F, 3.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(26, 44).addBox(-2.5F, -14.5F, 3.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(22, 46).addBox(0.5F, -15.5F, 3.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(22, 48).addBox(1.5F, -14.5F, 3.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        hair.texOffs(30, 44).addBox(-4.5F, -20.5F, -0.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        beard = new ModelPart(this);
        beard.setPos(0.0F, 2.7F, -0.4F);
        head.addChild(beard);


        beardB = new ModelPart(this);
        beardB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardB);
        beardB.texOffs(0, 54).addBox(-0.5F, 7.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        beardMMB = new ModelPart(this);
        beardMMB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMMB);
        beardMMB.texOffs(4, 54).addBox(-2.5F, 3.5F, -4.5F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        beardMBB = new ModelPart(this);
        beardMBB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMBB);
        beardMBB.texOffs(16, 54).addBox(-3.5F, 2.5F, -4.5F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        beardring1 = new ModelPart(this);
        beardring1.setPos(0.0F, 1.0F, 0.0F);
        beard.addChild(beardring1);
        beardring1.texOffs(0, 56).addBox(-1.5F, 4.5F, -5.0F, 3.0F, 1.0F, 2.0F, 0.0F, true);

        beardBBB2 = new ModelPart(this);
        beardBBB2.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardBBB2);
        beardBBB2.texOffs(10, 56).addBox(-1.0F, 4.5F, -4.5F, 2.0F, 3.0F, 1.0F, 0.0F, true);

        beardMB = new ModelPart(this);
        beardMB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMB);
        beardMB.texOffs(16, 56).addBox(-4.5F, 1.5F, -4.5F, 9.0F, 1.0F, 2.0F, 0.0F, true);

        moustacheLB = new ModelPart(this);
        moustacheLB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheLB);
        moustacheLB.texOffs(0, 59).addBox(2.7F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        beardRM = new ModelPart(this);
        beardRM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardRM);
        beardRM.texOffs(1, 60).addBox(-4.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        beardTM = new ModelPart(this);
        beardTM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardTM);
        beardTM.texOffs(13, 59).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 4.0F, 0.0F, true);

        beardTT = new ModelPart(this);
        beardTT.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardTT);
        beardTT.texOffs(32, 52).addBox(-3.5F, -1.5F, -4.5F, 7.0F, 1.0F, 3.0F, 0.0F, true);

        moustacheTM = new ModelPart(this);
        moustacheTM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheTM);
        moustacheTM.texOffs(36, 56).addBox(-2.5F, -1.0F, -5.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        moustacheLM = new ModelPart(this);
        moustacheLM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheLM);
        moustacheLM.texOffs(35, 59).addBox(2.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        moustacheRM = new ModelPart(this);
        moustacheRM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheRM);
        moustacheRM.texOffs(39, 59).addBox(-3.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        moustacheRB = new ModelPart(this);
        moustacheRB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheRB);
        moustacheRB.texOffs(43, 59).addBox(-3.7333F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairM = new ModelPart(this);
        hairM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(hairM);
        hairM.texOffs(43, 59).addBox(-4.5F, -1.5F, -2.5F, 9.0F, 1.0F, 4.0F, 0.0F, true);

        beardLM = new ModelPart(this);
        beardLM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardLM);
        beardLM.texOffs(65, 59).addBox(1.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
