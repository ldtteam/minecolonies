// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityComposterMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityComposterMale()
    {
        ModelRenderer bootL;
        ModelRenderer bootR;
        ModelRenderer gloveL;
        ModelRenderer gloveR;
        ModelRenderer headDetail;

        texWidth = 128;
        texHeight = 64;

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bootL = new ModelRenderer(this);
        bootL.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.addChild(bootL);
        bootL.texOffs(0, 38).addBox(-4.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bootR = new ModelRenderer(this);
        bootR.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.addChild(bootR);
        bootR.texOffs(20, 38).addBox(-0.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        gloveL = new ModelRenderer(this);
        gloveL.setPos(5.0F, 2.0F, 0.0F);
        leftArm.addChild(gloveL);
        gloveL.texOffs(0, 32).addBox(-6.5F, 3.0F, -2.5F, 5.0F, 1.0F, 5.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        gloveR = new ModelRenderer(this);
        gloveR.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.addChild(gloveR);
        gloveR.texOffs(20, 32).addBox(1.5F, 3.0F, -2.5F, 5.0F, 1.0F, 5.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
