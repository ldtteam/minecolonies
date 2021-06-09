// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityBlacksmithMale extends CitizenModel<AbstractEntityCitizen> 
{

    public ModelEntityBlacksmithMale() 
    {
        ModelRenderer headdetail;
        ModelRenderer hair;
        ModelRenderer beard;
        ModelRenderer beardB;
        ModelRenderer beardMMB;
        ModelRenderer beardMBB;
        ModelRenderer beardring1;
        ModelRenderer beardBBB2;
        ModelRenderer beardMB;
        ModelRenderer moustacheLB;
        ModelRenderer beardRM;
        ModelRenderer beardTM;
        ModelRenderer beardTT;
        ModelRenderer moustacheTM;
        ModelRenderer moustacheLM;
        ModelRenderer moustacheRM;
        ModelRenderer moustacheRB;
        ModelRenderer hairM;
        ModelRenderer beardLM;

        texWidth = 128;
        texHeight = 128;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 6.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 6.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 14.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 14.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 4.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 4.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelRenderer(this);
        headdetail.setPos(0.0F, 20.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelRenderer(this);
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

        beard = new ModelRenderer(this);
        beard.setPos(0.0F, 2.7F, -0.4F);
        head.addChild(beard);


        beardB = new ModelRenderer(this);
        beardB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardB);
        beardB.texOffs(0, 54).addBox(-0.5F, 7.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        beardMMB = new ModelRenderer(this);
        beardMMB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMMB);
        beardMMB.texOffs(4, 54).addBox(-2.5F, 3.5F, -4.5F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        beardMBB = new ModelRenderer(this);
        beardMBB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMBB);
        beardMBB.texOffs(16, 54).addBox(-3.5F, 2.5F, -4.5F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        beardring1 = new ModelRenderer(this);
        beardring1.setPos(0.0F, 1.0F, 0.0F);
        beard.addChild(beardring1);
        beardring1.texOffs(0, 56).addBox(-1.5F, 4.5F, -5.0F, 3.0F, 1.0F, 2.0F, 0.0F, true);

        beardBBB2 = new ModelRenderer(this);
        beardBBB2.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardBBB2);
        beardBBB2.texOffs(10, 56).addBox(-1.0F, 4.5F, -4.5F, 2.0F, 3.0F, 1.0F, 0.0F, true);

        beardMB = new ModelRenderer(this);
        beardMB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMB);
        beardMB.texOffs(16, 56).addBox(-4.5F, 1.5F, -4.5F, 9.0F, 1.0F, 2.0F, 0.0F, true);

        moustacheLB = new ModelRenderer(this);
        moustacheLB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheLB);
        moustacheLB.texOffs(0, 59).addBox(2.7F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        beardRM = new ModelRenderer(this);
        beardRM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardRM);
        beardRM.texOffs(1, 60).addBox(-4.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        beardTM = new ModelRenderer(this);
        beardTM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardTM);
        beardTM.texOffs(13, 59).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 4.0F, 0.0F, true);

        beardTT = new ModelRenderer(this);
        beardTT.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardTT);
        beardTT.texOffs(32, 52).addBox(-3.5F, -1.5F, -4.5F, 7.0F, 1.0F, 3.0F, 0.0F, true);

        moustacheTM = new ModelRenderer(this);
        moustacheTM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheTM);
        moustacheTM.texOffs(36, 56).addBox(-2.5F, -1.0F, -5.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        moustacheLM = new ModelRenderer(this);
        moustacheLM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheLM);
        moustacheLM.texOffs(35, 59).addBox(2.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        moustacheRM = new ModelRenderer(this);
        moustacheRM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheRM);
        moustacheRM.texOffs(39, 59).addBox(-3.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        moustacheRB = new ModelRenderer(this);
        moustacheRB.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheRB);
        moustacheRB.texOffs(43, 59).addBox(-3.7333F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairM = new ModelRenderer(this);
        hairM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(hairM);
        hairM.texOffs(43, 59).addBox(-4.5F, -1.5F, -2.5F, 9.0F, 1.0F, 4.0F, 0.0F, true);

        beardLM = new ModelRenderer(this);
        beardLM.setPos(0.0F, 0.0F, 0.0F);
        beard.addChild(beardLM);
        beardLM.texOffs(65, 59).addBox(1.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
