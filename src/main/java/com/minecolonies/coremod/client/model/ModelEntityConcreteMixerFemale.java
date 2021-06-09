// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityConcreteMixerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityConcreteMixerFemale()
    {
        ModelRenderer hair;
        ModelRenderer mask;
        ModelRenderer headDetail;
        ModelRenderer breast;

        texWidth = 128;
        texHeight = 64;

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(hair);
        hair.texOffs(70, 49).addBox(-4.35F, -26.0F, 3.02F, 0.35F, 6.0F, 1.0F, 0.0F, true);
        hair.texOffs(74, 46).addBox(-4.25F, -25.0F, 1.02F, 0.25F, 3.25F, 2.0F, 0.0F, false);
        hair.texOffs(28, 56).addBox(4.0F, -25.0F, 1.02F, 0.25F, 3.25F, 2.0F, 0.0F, false);
        hair.texOffs(16, 56).addBox(-4.25F, -26.0F, 1.02F, 0.25F, 1.0F, 1.0F, 0.0F, false);
        hair.texOffs(12, 56).addBox(4.0F, -26.0F, 1.02F, 0.25F, 1.0F, 1.0F, 0.0F, false);
        hair.texOffs(29, 45).addBox(4.0F, -26.0F, 3.02F, 0.35F, 6.0F, 1.0F, 0.0F, false);
        hair.texOffs(51, 46).addBox(4.0F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F, 0.0F, false);
        hair.texOffs(12, 60).addBox(2.75F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F, 0.0F, false);
        hair.texOffs(18, 58).addBox(4.0F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F, 0.0F, false);
        hair.texOffs(23, 57).addBox(4.0F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F, 0.0F, false);
        hair.texOffs(26, 46).addBox(4.0F, -26.0F, 2.02F, 0.35F, 1.0F, 1.0F, 0.0F, false);
        hair.texOffs(24, 49).addBox(-4.35F, -26.0F, 2.02F, 0.35F, 1.0F, 1.0F, 0.0F, true);
        hair.texOffs(0, 46).addBox(-4.35F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F, 0.0F, true);
        hair.texOffs(0, 55).addBox(-4.35F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F, 0.0F, true);
        hair.texOffs(6, 59).addBox(-4.35F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F, 0.0F, true);
        hair.texOffs(51, 58).addBox(-2.75F, -32.25F, -4.48F, 5.5F, 2.25F, 1.0F, 0.0F, true);
        hair.texOffs(0, 46).addBox(-4.0F, -32.25F, -3.48F, 8.0F, 2.25F, 7.5F, 0.0F, true);
        hair.texOffs(33, 46).addBox(-4.25F, -32.25F, 4.02F, 8.5F, 6.75F, 0.25F, 0.0F, true);
        hair.texOffs(74, 52).addBox(3.75F, -25.5F, 4.02F, 0.5F, 5.5F, 0.25F, 0.0F, true);
        hair.texOffs(76, 52).addBox(-4.25F, -25.5F, 4.02F, 0.5F, 5.5F, 0.25F, 0.0F, true);
        hair.texOffs(33, 54).addBox(-3.75F, -25.5F, 3.02F, 7.5F, 7.75F, 1.25F, 0.0F, true);
        hair.texOffs(61, 46).addBox(-4.35F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F, 0.0F, true);

        mask = new ModelRenderer(this);
        mask.setPos(6.0F, -0.5F, -5.0F);
        head.addChild(mask);
        mask.texOffs(72, 0).addBox(-10.28F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F, 0.0F, false);
        mask.texOffs(87, 0).addBox(-10.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F, 0.0F, false);
        mask.texOffs(87, 4).addBox(-7.5F, -3.0F, 0.76F, 3.0F, 4.0F, 0.75F, 0.0F, false);
        mask.texOffs(87, 10).addBox(-5.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F, 0.0F, false);
        mask.texOffs(94, 0).addBox(-9.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F, 0.0F, false);
        mask.texOffs(95, 3).addBox(-5.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F, 0.0F, false);
        mask.texOffs(95, 6).addBox(-2.0F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);
        leftArm.texOffs(21, 32).addBox(-1.5F, 5.0F, -2.25F, 4.0F, 0.75F, 4.5F, 0.0F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(1, 32).addBox(-2.5F, 5.0F, -2.25F, 4.0F, 0.75F, 4.5F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        breast = new ModelRenderer(this);
        breast.setPos(3.0F, 2.5F, -2.0F);
        body.addChild(breast);
        setRotationAngle(breast, 0.7854F, 0.0F, 0.0F);
        breast.texOffs(41, 32).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
