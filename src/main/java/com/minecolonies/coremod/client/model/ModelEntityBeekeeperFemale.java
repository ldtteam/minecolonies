// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityBeekeeperFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBeekeeperFemale()
    {
        ModelRenderer chest;
        ModelRenderer hatBottom;
        ModelRenderer hatTop;
        ModelRenderer hatRight;
        ModelRenderer hatNeck;
        ModelRenderer hatM;

        texWidth = 128;
        texHeight = 64;

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setPos(2.0F, 3.0F, -2.0F);
        body.addChild(chest);
        setRotationAngle(chest, 0.6109F, 0.0F, 0.0F);
        chest.texOffs(17, 33).addBox(-5.0F, -1.0F, -2.0F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(38, 50).addBox(-1.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F, 0.0F, false);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        rightArm.texOffs(38, 41).addBox(-3.25F, 6.0F, -2.25F, 4.5F, 0.5F, 4.5F, 0.0F, false);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hatBottom = new ModelRenderer(this);
        hatBottom.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatBottom);
        setRotationAngle(hatBottom, -0.0349F, 0.0F, 0.0F);
        hatBottom.texOffs(57, 26).addBox(-5.5F, -5.4856F, -5.457F, 11.25F, 1.0F, 10.0F, 0.0F, true);

        hatTop = new ModelRenderer(this);
        hatTop.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatTop);
        setRotationAngle(hatTop, -0.0349F, 0.0F, 0.0F);
        hatTop.texOffs(64, 4).addBox(-3.5F, -9.0358F, -2.9483F, 7.0F, 1.0F, 5.0F, 0.0F, true);

        hatRight = new ModelRenderer(this);
        hatRight.setPos(0.0F, 0.0F, 2.0F);
        head.addChild(hatRight);
        setRotationAngle(hatRight, -0.0349F, 0.0F, 0.0F);
        hatRight.texOffs(81, 41).addBox(-5.5F, -4.3454F, 1.5018F, 11.0F, 5.0F, 1.0F, 0.0F, true);
        hatRight.texOffs(58, 47).addBox(4.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F, 0.0F, true);
        hatRight.texOffs(75, 47).addBox(-5.5F, -4.4159F, -6.4558F, 1.0F, 5.0F, 8.0F, 0.0F, true);
        hatRight.texOffs(57, 41).addBox(-5.5F, -4.4849F, -7.5006F, 11.0F, 5.0F, 1.0F, 0.0F, true);

        hatNeck = new ModelRenderer(this);
        hatNeck.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatNeck);
        setRotationAngle(hatNeck, -0.3491F, 0.0F, 0.0F);
        hatNeck.texOffs(98, 14).addBox(-4.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F, 0.0F, true);
        hatNeck.texOffs(98, 14).addBox(3.5F, -6.7F, -2.7F, 0.75F, 7.75F, 1.0F, 0.0F, true);
        hatNeck.texOffs(98, 14).addBox(-4.5F, 1.05F, -2.7F, 8.75F, 0.75F, 1.0F, 0.0F, true);

        hatM = new ModelRenderer(this);
        hatM.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatM);
        setRotationAngle(hatM, -0.0349F, 0.0F, 0.0F);
        hatM.texOffs(58, 13).addBox(-4.75F, -8.3358F, -4.6983F, 9.5F, 3.0F, 8.5F, 0.0F, true);
        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
