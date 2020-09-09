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

        textureWidth = 128;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 6.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 6.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, 2.0F, -2.0F, 4.0F, 11.0F, 4.0F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 14.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 14.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 2.0F, -2.0F, 4.0F, 10.0F, 4.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 4.0F, -2.0F, 8.0F, 10.0F, 4.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 4.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headdetail = new ModelRenderer(this);
        headdetail.setRotationPoint(0.0F, 20.0F, 0.0F);
        bipedHead.addChild(headdetail);
        headdetail.setTextureOffset(32, 0).addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 20.7F, -0.4F);
        bipedHead.addChild(hair);
        hair.setTextureOffset(0, 39).addBox(3.3F, -23.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(4, 40).addBox(-4.2F, -23.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(8, 40).addBox(-4.0F, -24.5F, -4.5F, 8.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(19, 35).addBox(-3.5F, -25.15F, -3.75F, 7.0F, 2.0F, 7.0F, 0.0F, true);
        hair.setTextureOffset(40, 39).addBox(-4.5F, -24.5F, -3.5F, 9.0F, 2.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(40, 36).addBox(-4.5F, -24.5F, -2.5F, 9.0F, 2.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(60, 38).addBox(-4.5F, -24.5F, -1.5F, 9.0F, 2.0F, 2.0F, 0.0F, true);
        hair.setTextureOffset(82, 36).addBox(-4.5F, -24.5F, 0.5F, 9.0F, 4.0F, 2.0F, 0.0F, true);
        hair.setTextureOffset(47, 42).addBox(-4.5F, -24.5F, 2.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(67, 42).addBox(-4.5F, -22.5F, -0.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(0, 44).addBox(-4.5F, -23.5F, 2.5F, 9.0F, 6.0F, 2.0F, 0.0F, true);
        hair.setTextureOffset(87, 42).addBox(-4.5F, -17.5F, 3.5F, 9.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(9, 38).addBox(-3.5F, -16.5F, 3.5F, 7.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(20, 44).addBox(-3.5F, -15.5F, 3.5F, 2.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(26, 44).addBox(-2.5F, -14.5F, 3.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(22, 46).addBox(0.5F, -15.5F, 3.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(22, 48).addBox(1.5F, -14.5F, 3.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(30, 44).addBox(-4.5F, -20.5F, -0.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        beard = new ModelRenderer(this);
        beard.setRotationPoint(0.0F, 2.7F, -0.4F);
        bipedHead.addChild(beard);
        

        beardB = new ModelRenderer(this);
        beardB.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardB);
        beardB.setTextureOffset(0, 54).addBox(-0.5F, 7.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

        beardMMB = new ModelRenderer(this);
        beardMMB.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMMB);
        beardMMB.setTextureOffset(4, 54).addBox(-2.5F, 3.5F, -4.5F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        beardMBB = new ModelRenderer(this);
        beardMBB.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMBB);
        beardMBB.setTextureOffset(16, 54).addBox(-3.5F, 2.5F, -4.5F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        beardring1 = new ModelRenderer(this);
        beardring1.setRotationPoint(0.0F, 1.0F, 0.0F);
        beard.addChild(beardring1);
        beardring1.setTextureOffset(0, 56).addBox(-1.5F, 4.5F, -5.0F, 3.0F, 1.0F, 2.0F, 0.0F, true);

        beardBBB2 = new ModelRenderer(this);
        beardBBB2.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardBBB2);
        beardBBB2.setTextureOffset(10, 56).addBox(-1.0F, 4.5F, -4.5F, 2.0F, 3.0F, 1.0F, 0.0F, true);

        beardMB = new ModelRenderer(this);
        beardMB.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardMB);
        beardMB.setTextureOffset(16, 56).addBox(-4.5F, 1.5F, -4.5F, 9.0F, 1.0F, 2.0F, 0.0F, true);

        moustacheLB = new ModelRenderer(this);
        moustacheLB.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheLB);
        moustacheLB.setTextureOffset(0, 59).addBox(2.7F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        beardRM = new ModelRenderer(this);
        beardRM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardRM);
        beardRM.setTextureOffset(1, 60).addBox(-4.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        beardTM = new ModelRenderer(this);
        beardTM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardTM);
        beardTM.setTextureOffset(13, 59).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 4.0F, 0.0F, true);

        beardTT = new ModelRenderer(this);
        beardTT.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardTT);
        beardTT.setTextureOffset(32, 52).addBox(-3.5F, -1.5F, -4.5F, 7.0F, 1.0F, 3.0F, 0.0F, true);

        moustacheTM = new ModelRenderer(this);
        moustacheTM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheTM);
        moustacheTM.setTextureOffset(36, 56).addBox(-2.5F, -1.0F, -5.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        moustacheLM = new ModelRenderer(this);
        moustacheLM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheLM);
        moustacheLM.setTextureOffset(35, 59).addBox(2.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        moustacheRM = new ModelRenderer(this);
        moustacheRM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheRM);
        moustacheRM.setTextureOffset(39, 59).addBox(-3.0F, 0.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        moustacheRB = new ModelRenderer(this);
        moustacheRB.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(moustacheRB);
        moustacheRB.setTextureOffset(43, 59).addBox(-3.7333F, 3.0F, -5.0F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairM = new ModelRenderer(this);
        hairM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(hairM);
        hairM.setTextureOffset(43, 59).addBox(-4.5F, -1.5F, -2.5F, 9.0F, 1.0F, 4.0F, 0.0F, true);

        beardLM = new ModelRenderer(this);
        beardLM.setRotationPoint(0.0F, 0.0F, 0.0F);
        beard.addChild(beardLM);
        beardLM.setTextureOffset(65, 59).addBox(1.5F, 0.5F, -4.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
