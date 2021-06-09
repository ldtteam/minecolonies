// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFletcherFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFletcherFemale()
    {
        ModelRenderer knifeblade;
        ModelRenderer chest;
        ModelRenderer hair;
        ModelRenderer headDetail;

        texWidth = 128;
        texHeight = 64;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(83, 20).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F, 0.0F, true);
        body.texOffs(5, 40).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        body.texOffs(11, 40).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        body.texOffs(26, 40).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, false);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        knifeblade = new ModelRenderer(this);
        knifeblade.setPos(2.5F, 26.0F, 0.5F);
        body.addChild(knifeblade);
        knifeblade.texOffs(73, 28).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, true);
        knifeblade.texOffs(80, 28).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F, 0.0F, true);
        knifeblade.texOffs(87, 28).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, false);
        knifeblade.texOffs(70, 28).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F, 0.0F, true);
        knifeblade.texOffs(77, 28).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F, 0.0F, true);
        knifeblade.texOffs(84, 28).addBox(-0.5F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, false);
        knifeblade.texOffs(70, 28).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F, 0.0F, true);
        knifeblade.texOffs(77, 28).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, true);
        knifeblade.texOffs(84, 28).addBox(-0.75F, -15.5F, -3.25F, 0.25F, 2.25F, 0.5F, 0.0F, false);
        knifeblade.texOffs(84, 28).addBox(-1.0F, -15.5F, -3.25F, 0.25F, 1.75F, 0.5F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setPos(3.0F, -3.0F, 5.0F);
        body.addChild(chest);
        setRotationAngle(chest, 0.8727F, 0.0F, 0.0F);
        chest.texOffs(38, 32).addBox(-6.0F, -2.5179F, -10.5745F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(69, 12).addBox(-1.25F, 7.0F, -2.25F, 3.5F, 1.0F, 4.5F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);
        rightArm.texOffs(69, 18).addBox(-2.25F, 7.0F, -2.25F, 3.5F, 1.0F, 4.5F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(hair);
        hair.texOffs(32, 47).addBox(4.0F, -32.0F, -1.5F, 0.25F, 5.75F, 5.5F, 0.0F, false);
        hair.texOffs(11, 49).addBox(-4.25F, -32.0F, -0.25F, 0.25F, 5.5F, 4.25F, 0.0F, false);
        hair.texOffs(111, 48).addBox(-4.25F, -32.0F, -3.0F, 0.25F, 6.0F, 1.5F, 0.0F, false);
        hair.texOffs(116, 47).addBox(-4.25F, -32.0F, -1.5F, 0.25F, 6.75F, 1.25F, 0.0F, false);
        hair.texOffs(24, 51).addBox(4.0F, -32.0F, -3.0F, 0.25F, 5.25F, 1.5F, 0.0F, false);
        hair.texOffs(52, 52).addBox(-2.5F, -19.5F, 3.0F, 5.0F, 3.0F, 1.25F, 0.0F, false);
        hair.texOffs(49, 47).addBox(-3.25F, -22.5F, 3.0F, 6.75F, 3.0F, 1.25F, 0.0F, false);
        hair.texOffs(66, 47).addBox(-4.0F, -32.0F, 3.0F, 8.0F, 9.5F, 1.25F, 0.0F, false);
        hair.texOffs(86, 49).addBox(-4.0F, -32.25F, -4.0F, 8.0F, 0.25F, 8.0F, 0.0F, false);
        hair.texOffs(6, 49).addBox(1.0F, -30.0F, -4.5F, 1.25F, 1.25F, 0.5F, 0.0F, false);
        hair.texOffs(39, 48).addBox(0.0F, -30.0F, -4.5F, 1.0F, 2.0F, 0.5F, 0.0F, false);
        hair.texOffs(26, 48).addBox(-1.0F, -30.0F, -4.5F, 1.0F, 2.5F, 0.5F, 0.0F, false);
        hair.texOffs(0, 55).addBox(-4.0F, -30.0F, -4.5F, 3.0F, 3.0F, 0.5F, 0.0F, false);
        hair.texOffs(0, 51).addBox(-4.0F, -31.25F, -4.5F, 5.75F, 1.25F, 0.5F, 0.0F, false);
        hair.texOffs(0, 46).addBox(-4.0F, -32.0F, -4.5F, 5.75F, 1.0F, 0.5F, 0.0F, false);
        hair.texOffs(11, 49).addBox(2.25F, -30.0F, -4.5F, 0.5F, 1.0F, 0.5F, 0.0F, false);
        hair.texOffs(7, 55).addBox(2.75F, -30.0F, -4.5F, 1.25F, 2.25F, 0.5F, 0.0F, false);
        hair.texOffs(0, 48).addBox(1.75F, -32.0F, -4.5F, 2.25F, 2.0F, 0.5F, 0.0F, false);
        hair.texOffs(45, 54).addBox(4.0F, -26.25F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.texOffs(32, 47).addBox(-4.25F, -26.5F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        hair.texOffs(43, 47).addBox(4.0F, -26.25F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        hair.texOffs(18, 46).addBox(-4.25F, -26.5F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        hair.texOffs(28, 52).addBox(4.0F, -32.0F, -4.25F, 0.25F, 4.75F, 1.25F, 0.0F, false);
        hair.texOffs(90, 49).addBox(-4.25F, -32.0F, -4.25F, 0.25F, 5.5F, 1.25F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
