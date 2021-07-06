// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityUndertakerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityUndertakerMale()
    {
        ModelRenderer hat;
        ModelRenderer hatPartLowLeft;
        ModelRenderer hatPartLowRight;

        texWidth = 128;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hat = new ModelRenderer(this);
        hat.setPos(0.0F, -5.6F, 0.0F);
        head.addChild(hat);
        setRotationAngle(hat, -0.0611F, 0.0F, 0.0F);
        hat.texOffs(64, 0).addBox(-3.9664F, -3.1854F, -5.0F, 8.0F, 2.0F, 10.0F, 0.0F, false);
        hat.texOffs(90, 0).addBox(-2.9664F, -10.6F, -3.0F, 6.0F, 1.0F, 6.0F, 0.0F, false);
        hat.texOffs(64, 12).addBox(-2.9664F, -11.3F, -4.0F, 6.0F, 10.0F, 1.0F, -0.101F, false);
        hat.texOffs(64, 22).addBox(2.5336F, -11.3F, -3.5F, 1.0F, 10.0F, 7.0F, -0.102F, false);
        hat.texOffs(80, 22).addBox(-3.4664F, -11.3F, -3.5F, 1.0F, 10.0F, 7.0F, -0.103F, false);
        hat.texOffs(78, 12).addBox(-2.9664F, -11.3F, 3.0F, 6.0F, 10.0F, 1.0F, -0.1F, false);

        hatPartLowLeft = new ModelRenderer(this);
        hatPartLowLeft.setPos(-3.1F, -2.2F, 0.0F);
        hat.addChild(hatPartLowLeft);
        setRotationAngle(hatPartLowLeft, 0.0F, 0.0F, 0.48F);
        hatPartLowLeft.texOffs(92, 7).addBox(-2.3F, -0.7F, -5.0F, 2.0F, 2.0F, 10.0F, 0.01F, false);

        hatPartLowRight = new ModelRenderer(this);
        hatPartLowRight.setPos(6.0079F, -4.9714F, 0.0F);
        hat.addChild(hatPartLowRight);
        setRotationAngle(hatPartLowRight, 0.0F, 0.0F, 1.0908F);
        hatPartLowRight.texOffs(96, 19).addBox(0.4466F, 1.4993F, -5.0F, 2.0F, 2.0F, 10.0F, 0.01F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLeg.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        leftLeg.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);
		
        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
