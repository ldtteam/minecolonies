// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityBakerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBakerMale()
    {
        ModelRenderer headdetail;
        ModelRenderer hatPiece;
        ModelRenderer base;
        ModelRenderer middle;
        ModelRenderer top;

        texWidth = 128;
        texHeight = 64;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headdetail = new ModelRenderer(this);
        headdetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hatPiece = new ModelRenderer(this);
        hatPiece.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatPiece);


        base = new ModelRenderer(this);
        base.setPos(0.0F, 0.0F, 0.0F);
        hatPiece.addChild(base);
        setRotationAngle(base, -0.1859F, 0.0F, 0.0F);
        base.texOffs(0, 33).addBox(-4.5F, -9.0F, -5.8F, 9.0F, 2.0F, 9.0F, 0.0F, true);

        middle = new ModelRenderer(this);
        middle.setPos(0.0F, 0.0F, 0.0F);
        hatPiece.addChild(middle);
        setRotationAngle(middle, -0.1859F, 0.0F, 0.0F);
        middle.texOffs(0, 44).addBox(-3.5F, -10.0F, -5.0F, 7.0F, 1.0F, 8.0F, 0.0F, true);

        top = new ModelRenderer(this);
        top.setPos(0.0F, 0.0F, 0.0F);
        hatPiece.addChild(top);
        setRotationAngle(top, -0.1859F, 0.0F, 0.0F);
        top.texOffs(0, 53).addBox(-2.5F, -11.0F, -4.6F, 5.0F, 1.0F, 7.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
