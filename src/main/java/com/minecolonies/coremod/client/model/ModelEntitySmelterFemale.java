// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntitySmelterFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntitySmelterFemale()
    {
        ModelRenderer toolHandle1;
        ModelRenderer toolHandle2;
        ModelRenderer pocket;
        ModelRenderer bipedChest;
        ModelRenderer headDetail;
        ModelRenderer ponytailB;
        ModelRenderer ponytailT;

        texWidth = 128;
        texHeight = 64;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        toolHandle1 = new ModelRenderer(this);
        toolHandle1.setPos(-2.0F, 8.0F, -3.0F);
        body.addChild(toolHandle1);
        toolHandle1.texOffs(0, 32).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 1.0F, 0.0F, true);

        toolHandle2 = new ModelRenderer(this);
        toolHandle2.setPos(-1.0F, 6.0F, -3.0F);
        body.addChild(toolHandle2);
        toolHandle2.texOffs(10, 32).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        pocket = new ModelRenderer(this);
        pocket.setPos(1.0F, 6.0F, -3.0F);
        body.addChild(pocket);
        pocket.texOffs(10, 32).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        bipedChest = new ModelRenderer(this);
        bipedChest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bipedChest);
        setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
        bipedChest.texOffs(0, 55).addBox(-3.5F, 2.7F, -0.6F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, -1.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setPos(0.0F, 25.0F, 0.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

        ponytailB = new ModelRenderer(this);
        ponytailB.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytailB);
        setRotationAngle(ponytailB, 0.1047F, 0.0F, 0.0F);
        ponytailB.texOffs(80, 40).addBox(-0.5F, 2.4F, 3.7F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        ponytailT = new ModelRenderer(this);
        ponytailT.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytailT);
        setRotationAngle(ponytailT, 0.2269F, 0.0F, 0.0F);
        ponytailT.texOffs(79, 33).addBox(-1.0F, -2.0F, 3.4F, 2.0F, 5.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
