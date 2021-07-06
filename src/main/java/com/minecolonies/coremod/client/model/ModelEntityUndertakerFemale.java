// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityUndertakerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityUndertakerFemale()
    {
        ModelRenderer topHat;
        ModelRenderer hatPartLowLeft;
        ModelRenderer hatPartLowRight;
        ModelRenderer breast;

        texWidth = 128;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        topHat = new ModelRenderer(this);
        topHat.setPos(0.0F, -5.6F, 0.0F);
        head.addChild(topHat);
        setRotationAngle(topHat, -0.0611F, 0.0F, 0.0F);
        topHat.texOffs(72, 0).addBox(-3.9664F, -3.1854F, -5.0F, 8.0F, 2.0F, 10.0F, 0.0F, false);
        topHat.texOffs(98, 0).addBox(-2.9664F, -6.583F, -2.9551F, 6.0F, 1.0F, 6.0F, 0.0F, false);
        topHat.texOffs(72, 12).addBox(-2.9664F, -7.3F, -4.0F, 6.0F, 6.0F, 1.0F, -0.101F, true);
        topHat.texOffs(72, 18).addBox(2.5336F, -7.3F, -3.5F, 1.0F, 6.0F, 7.0F, -0.102F, false);
        topHat.texOffs(88, 18).addBox(-3.4664F, -7.3F, -3.5F, 1.0F, 6.0F, 7.0F, -0.103F, false);
        topHat.texOffs(86, 12).addBox(-2.9664F, -7.3F, 3.0F, 6.0F, 6.0F, 1.0F, -0.1F, false);

        hatPartLowLeft = new ModelRenderer(this);
        hatPartLowLeft.setPos(-3.1F, -2.2F, 0.0F);
        topHat.addChild(hatPartLowLeft);
        setRotationAngle(hatPartLowLeft, 0.0F, 0.0F, 0.48F);
        hatPartLowLeft.texOffs(100, 7).addBox(-2.3F, -0.7F, -5.0F, 2.0F, 2.0F, 10.0F, 0.01F, false);

        hatPartLowRight = new ModelRenderer(this);
        hatPartLowRight.setPos(6.0079F, -4.9714F, 0.0F);
        topHat.addChild(hatPartLowRight);
        setRotationAngle(hatPartLowRight, 0.0F, 0.0F, 1.0908F);
        hatPartLowRight.texOffs(104, 19).addBox(0.4466F, 1.4993F, -5.0F, 2.0F, 2.0F, 10.0F, 0.01F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        breast = new ModelRenderer(this);
        breast.setPos(-1.0F, 3.0F, 4.0F);
        body.addChild(breast);
        setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
        breast.texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, 0.0F, false);
        breast.texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, 0.25F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightLeg.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLeg.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
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
