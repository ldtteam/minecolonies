// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMechanistFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMechanistFemale()
    {
        ModelRenderer belt;
        ModelRenderer torch1;
        ModelRenderer torch2;
        ModelRenderer torch3;
        ModelRenderer chest;
        ModelRenderer gloveleft;
        ModelRenderer gloveright;
        ModelRenderer headDetail;
        ModelRenderer hair;
        ModelRenderer mask;

        texWidth = 128;
        texHeight = 64;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        belt = new ModelRenderer(this);
        belt.setPos(0.0F, 24.0F, 0.0F);
        body.addChild(belt);
        belt.texOffs(0, 42).addBox(-0.5F, -14.0F, -4.0F, 4.0F, 3.0F, 2.0F, 0.0F, true);
        belt.texOffs(13, 45).addBox(1.0F, -13.45F, -4.2F, 1.0F, 1.0F, 1.0F, 0.0F, true);
        belt.texOffs(0, 33).addBox(-4.5F, -13.0F, -3.0F, 9.0F, 2.0F, 6.0F, 0.0F, true);

        torch1 = new ModelRenderer(this);
        torch1.setPos(0.0F, 0.0F, 0.0F);
        belt.addChild(torch1);
        torch1.texOffs(0, 56).addBox(-0.25F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        torch1.texOffs(0, 62).addBox(-0.25F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        torch1.texOffs(1, 54).addBox(0.0F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        torch1.texOffs(1, 57).addBox(0.0F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);

        torch2 = new ModelRenderer(this);
        torch2.setPos(0.0F, 0.0F, 0.0F);
        belt.addChild(torch2);
        torch2.texOffs(5, 62).addBox(1.0F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        torch2.texOffs(5, 56).addBox(1.0F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        torch2.texOffs(6, 57).addBox(1.25F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        torch2.texOffs(6, 54).addBox(1.25F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);

        torch3 = new ModelRenderer(this);
        torch3.setPos(0.0F, 0.0F, 0.0F);
        belt.addChild(torch3);
        torch3.texOffs(11, 57).addBox(2.5F, -15.25F, -3.75F, 0.5F, 3.0F, 0.5F, 0.0F, false);
        torch3.texOffs(10, 56).addBox(2.25F, -15.5F, -4.0F, 1.0F, 0.75F, 1.0F, 0.0F, false);
        torch3.texOffs(10, 62).addBox(2.25F, -16.0F, -4.0F, 1.0F, 0.5F, 1.0F, 0.0F, false);
        torch3.texOffs(11, 54).addBox(2.5F, -16.25F, -3.75F, 0.5F, 0.75F, 0.5F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setPos(0.0F, 2.0F, -11.0F);
        body.addChild(chest);
        setRotationAngle(chest, 0.7854F, 0.0F, 0.0F);
        chest.texOffs(53, 29).addBox(-3.0F, 5.5566F, 4.181F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        gloveleft = new ModelRenderer(this);
        gloveleft.setPos(-5.0F, 22.0F, 0.0F);
        leftArm.addChild(gloveleft);
        gloveleft.texOffs(96, 17).addBox(3.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);
        gloveleft.texOffs(76, 16).addBox(3.5F, -15.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        gloveright = new ModelRenderer(this);
        gloveright.setPos(5.0F, 22.0F, 0.0F);
        rightArm.addChild(gloveright);
        gloveright.texOffs(56, 16).addBox(-8.5F, -15.0F, -2.5F, 5.0F, 4.0F, 5.0F, 0.0F, true);
        gloveright.texOffs(96, 10).addBox(-9.0F, -16.0F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(hair);
        hair.texOffs(22, 36).addBox(-4.5F, -32.0F, -4.25F, 1.0F, 4.25F, 8.5F, 0.0F, false);
        hair.texOffs(41, 35).addBox(3.5F, -32.0F, -4.25F, 1.0F, 4.25F, 8.5F, 0.0F, false);
        hair.texOffs(16, 47).addBox(-4.5F, -28.0F, 1.75F, 1.0F, 3.0F, 2.5F, 0.0F, false);
        hair.texOffs(24, 49).addBox(3.5F, -28.0F, 1.75F, 1.0F, 3.0F, 2.5F, 0.0F, false);
        hair.texOffs(33, 36).addBox(-4.5F, -28.0F, -2.0F, 1.0F, 2.25F, 3.75F, 0.0F, false);
        hair.texOffs(53, 36).addBox(3.5F, -28.0F, -2.75F, 1.0F, 2.25F, 4.5F, 0.0F, false);
        hair.texOffs(65, 36).addBox(-4.25F, -32.0F, 3.75F, 8.5F, 8.0F, 0.75F, 0.0F, false);
        hair.texOffs(33, 48).addBox(-4.25F, -32.25F, -4.25F, 8.5F, 0.5F, 8.5F, 0.0F, false);
        hair.texOffs(34, 53).addBox(-4.25F, -32.0F, -4.75F, 2.5F, 1.75F, 0.5F, 0.0F, false);
        hair.texOffs(61, 46).addBox(1.75F, -32.0F, -4.75F, 2.5F, 1.75F, 0.5F, 0.0F, false);
        hair.texOffs(60, 49).addBox(-1.75F, -32.0F, -4.75F, 3.5F, 1.0F, 0.5F, 0.0F, false);
        hair.texOffs(60, 52).addBox(-1.75F, -31.5F, -4.75F, 1.0F, 1.0F, 0.5F, 0.0F, false);
        hair.texOffs(64, 52).addBox(-4.25F, -30.75F, -4.75F, 2.5F, 1.25F, 0.5F, 0.0F, false);
        hair.texOffs(35, 50).addBox(2.5F, -30.75F, -4.75F, 1.75F, 1.25F, 0.5F, 0.0F, false);

        mask = new ModelRenderer(this);
        mask.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(mask);
        mask.texOffs(64, 8).addBox(3.5F, -30.0F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);
        mask.texOffs(56, 0).addBox(-4.5F, -32.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);
        mask.texOffs(76, 10).addBox(-4.5F, -27.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.texOffs(77, 1).addBox(-4.5F, -32.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.texOffs(82, 2).addBox(-4.5F, -30.0F, -4.5F, 1.0F, 3.0F, 5.0F, 0.0F, true);
        mask.texOffs(56, 4).addBox(-4.5F, -25.0F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);
        mask.texOffs(24, 0).addBox(1.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        mask.texOffs(82, 10).addBox(-4.0F, -28.0F, -5.0F, 8.0F, 3.0F, 1.0F, 0.0F, true);
        mask.texOffs(72, 9).addBox(-1.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        mask.texOffs(77, 0).addBox(3.0F, -30.0F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        mask.texOffs(30, 0).addBox(-3.0F, -30.0F, -5.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        mask.texOffs(94, 7).addBox(-4.0F, -32.0F, -5.0F, 8.0F, 2.0F, 1.0F, 0.0F, true);
        mask.texOffs(89, 1).addBox(3.5F, -27.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.texOffs(99, 1).addBox(3.5F, -32.0F, -4.5F, 1.0F, 2.0F, 4.0F, 0.0F, true);
        mask.texOffs(83, 0).addBox(-4.0F, -30.0F, -5.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
