// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityChildMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityChildMale()
    {
        ModelRenderer overRightLeg;
        ModelRenderer overLeftLeg;
        ModelRenderer pouch;

        texWidth = 128;
        texHeight = 64;

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        overRightLeg = new ModelRenderer(this);
        overRightLeg.setPos(-4.5F, 12.0F, -2.5F);
        rightLeg.addChild(overRightLeg);
        overRightLeg.texOffs(0, 33).addBox(2.0F, -12.0F, 0.0F, 5.0F, 12.0F, 5.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        overLeftLeg = new ModelRenderer(this);
        overLeftLeg.setPos(-0.5F, 12.0F, -2.5F);
        leftLeg.addChild(overLeftLeg);
        overLeftLeg.texOffs(0, 33).addBox(-2.0F, -12.0F, 0.0F, 5.0F, 12.0F, 5.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.5F, 0.0F, -2.5F, 9.0F, 12.0F, 5.0F, 0.0F, true);

        pouch = new ModelRenderer(this);
        pouch.setPos(-4.0F, 9.5F, -3.5F);
        body.addChild(pouch);
        pouch.texOffs(20, 33).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 1.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(44, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(44, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
