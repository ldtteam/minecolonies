// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityFletcherMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFletcherMale()
    {
        ModelPart knifeblade;
        ModelPart hair;
        ModelPart headDetail;

        texWidth = 128;
        texHeight = 64;

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(87, 18).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F, 0.0F, true);
        body.texOffs(5, 38).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        body.texOffs(8, 39).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        body.texOffs(12, 39).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, false);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        knifeblade = new ModelPart(this);
        knifeblade.setPos(2.5F, 26.0F, 0.5F);
        body.addChild(knifeblade);
        knifeblade.texOffs(72, 26).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, true);
        knifeblade.texOffs(79, 26).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F, 0.0F, true);
        knifeblade.texOffs(86, 26).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, false);
        knifeblade.texOffs(69, 26).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F, 0.0F, true);
        knifeblade.texOffs(76, 26).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F, 0.0F, true);
        knifeblade.texOffs(83, 26).addBox(-0.5F, -15.5F, -3.35F, 0.25F, 2.5F, 0.6F, 0.0F, false);
        knifeblade.texOffs(69, 26).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F, 0.0F, true);
        knifeblade.texOffs(76, 26).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, true);
        knifeblade.texOffs(83, 26).addBox(-0.75F, -15.5F, -3.35F, 0.25F, 2.25F, 0.6F, 0.0F, false);
        knifeblade.texOffs(83, 26).addBox(-1.0F, -15.5F, -3.35F, 0.25F, 1.75F, 0.6F, 0.0F, false);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(69, 19).addBox(-1.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        rightArm.texOffs(69, 13).addBox(-3.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelPart(this);
        hair.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(hair);
        hair.texOffs(101, 47).addBox(-4.0F, -32.0F, 3.0F, 8.0F, 9.5F, 1.25F, 0.0F, false);
        hair.texOffs(0, 45).addBox(-4.0F, -32.25F, -4.0F, 8.0F, 0.25F, 8.0F, 0.0F, false);
        hair.texOffs(52, 52).addBox(1.0F, -30.0F, -4.5F, 1.25F, 0.25F, 0.5F, 0.0F, false);
        hair.texOffs(45, 56).addBox(0.0F, -30.0F, -4.5F, 1.0F, 0.5F, 0.5F, 0.0F, false);
        hair.texOffs(33, 47).addBox(-1.0F, -30.0F, -4.5F, 1.0F, 0.75F, 0.5F, 0.0F, false);
        hair.texOffs(41, 47).addBox(-4.0F, -30.0F, -4.5F, 3.0F, 1.0F, 0.5F, 0.0F, false);
        hair.texOffs(58, 52).addBox(-4.0F, -31.25F, -4.5F, 5.75F, 1.25F, 0.5F, 0.0F, false);
        hair.texOffs(58, 55).addBox(-4.0F, -32.0F, -4.5F, 5.75F, 1.0F, 0.5F, 0.0F, false);
        hair.texOffs(36, 54).addBox(2.25F, -29.8F, -4.5F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        hair.texOffs(50, 47).addBox(2.75F, -29.8F, -4.5F, 1.25F, 1.5F, 0.5F, 0.0F, false);
        hair.texOffs(54, 47).addBox(1.75F, -32.0F, -4.5F, 2.25F, 2.25F, 0.5F, 0.0F, false);
        hair.texOffs(77, 46).addBox(4.0F, -27.0F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.texOffs(88, 48).addBox(-4.25F, -27.0F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.texOffs(83, 45).addBox(4.0F, -27.0F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        hair.texOffs(95, 45).addBox(-4.25F, -27.0F, 1.75F, 0.25F, 4.0F, 2.25F, 0.0F, false);
        hair.texOffs(73, 46).addBox(4.0F, -32.0F, -4.25F, 0.25F, 4.0F, 1.25F, 0.0F, false);
        hair.texOffs(61, 46).addBox(-4.25F, -32.0F, -4.25F, 0.25F, 3.0F, 1.25F, 0.0F, false);
        hair.texOffs(69, 45).addBox(4.0F, -32.0F, -3.0F, 0.25F, 4.5F, 1.5F, 0.0F, false);
        hair.texOffs(65, 45).addBox(-4.25F, -32.0F, -3.0F, 0.25F, 4.25F, 1.5F, 0.0F, false);
        hair.texOffs(76, 47).addBox(4.0F, -32.0F, -1.5F, 0.25F, 5.25F, 5.5F, 0.0F, false);
        hair.texOffs(88, 47).addBox(-4.25F, -32.0F, -1.5F, 0.25F, 5.25F, 5.5F, 0.0F, false);

        headDetail = new ModelPart(this);
        headDetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
