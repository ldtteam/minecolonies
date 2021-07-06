// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityBakerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBakerFemale()
    {
        ModelRenderer breast;
        ModelRenderer headdetail;
        ModelRenderer ponytail;
        ModelRenderer ponyTailB;
        ModelRenderer ponyTailT;
        ModelRenderer hat;
        ModelRenderer topL;
        ModelRenderer topF;
        ModelRenderer topR;
        ModelRenderer midR;
        ModelRenderer midL;
        ModelRenderer lipR;
        ModelRenderer lipT;
        ModelRenderer lipL;
        ModelRenderer lipB;
        ModelRenderer baseT;
        ModelRenderer baseB;
        ModelRenderer baseM;
        ModelRenderer botL;

        texWidth = 256;
        texHeight = 128;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        breast = new ModelRenderer(this);
        breast.setPos(-1.0F, 3.0F, 4.0F);
        body.addChild(breast);
        setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
        breast.texOffs(18, 33).addBox(-2.5F, 2.5F, -5.366F, 7.0F, 3.0F, 3.0F, 0.5F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headdetail = new ModelRenderer(this);
        headdetail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(headdetail);
        headdetail.texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        ponytail = new ModelRenderer(this);
        ponytail.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytail);


        ponyTailB = new ModelRenderer(this);
        ponyTailB.setPos(0.0F, 0.0F, 0.0F);
        ponytail.addChild(ponyTailB);
        setRotationAngle(ponyTailB, 0.1047F, 0.0F, 0.0F);
        ponyTailB.texOffs(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        ponyTailT = new ModelRenderer(this);
        ponyTailT.setPos(0.0F, 0.0F, 0.0F);
        ponytail.addChild(ponyTailT);
        setRotationAngle(ponyTailT, 0.2269F, 0.0F, 0.0F);
        ponyTailT.texOffs(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);

        hat = new ModelRenderer(this);
        hat.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hat);


        topL = new ModelRenderer(this);
        topL.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(topL);
        topL.texOffs(64, 4).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 5.0F, 0.0F, true);

        topF = new ModelRenderer(this);
        topF.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(topF);
        topF.texOffs(64, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 3.0F, 0.0F, true);

        topR = new ModelRenderer(this);
        topR.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(topR);
        topR.texOffs(78, 4).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 1.0F, 5.0F, 0.0F, true);

        midR = new ModelRenderer(this);
        midR.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(midR);
        midR.texOffs(76, 10).addBox(-4.5F, -6.5F, -2.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        midL = new ModelRenderer(this);
        midL.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(midL);
        midL.texOffs(64, 10).addBox(1.5F, -6.5F, -2.5F, 3.0F, 1.0F, 3.0F, 0.0F, true);

        lipR = new ModelRenderer(this);
        lipR.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(lipR);
        setRotationAngle(lipR, -0.1842F, -0.8754F, -1.2905F);
        lipR.texOffs(22, 70).addBox(2.0F, -6.2F, -7.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

        lipT = new ModelRenderer(this);
        lipT.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(lipT);
        setRotationAngle(lipT, 0.2231F, 0.0F, 0.0F);
        lipT.texOffs(0, 67).addBox(-5.0F, -9.2F, -1.0F, 10.0F, 1.0F, 2.0F, 0.0F, true);

        lipL = new ModelRenderer(this);
        lipL.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(lipL);
        setRotationAngle(lipL, -0.1844F, 0.8755F, 1.2904F);
        lipL.texOffs(0, 70).addBox(-4.0F, -6.2F, -7.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

        lipB = new ModelRenderer(this);
        lipB.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(lipB);
        setRotationAngle(lipB, -1.3756F, 0.0F, 0.0F);
        lipB.texOffs(0, 80).addBox(-5.0F, -5.1F, -1.5F, 10.0F, 1.0F, 2.0F, 0.0F, true);

        baseT = new ModelRenderer(this);
        baseT.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(baseT);
        setRotationAngle(baseT, -0.8923F, 0.0F, 0.0F);
        baseT.texOffs(0, 40).addBox(-4.5F, -8.2F, -6.5F, 9.0F, 1.0F, 6.0F, 0.0F, true);

        baseB = new ModelRenderer(this);
        baseB.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(baseB);
        setRotationAngle(baseB, -0.8923F, 0.0F, 0.0F);
        baseB.texOffs(0, 57).addBox(-5.0F, -5.2F, -8.0F, 10.0F, 1.0F, 9.0F, 0.0F, true);

        baseM = new ModelRenderer(this);
        baseM.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(baseM);
        setRotationAngle(baseM, -0.8923F, 0.0F, 0.0F);
        baseM.texOffs(0, 47).addBox(-4.5F, -7.2F, -7.5F, 9.0F, 2.0F, 8.0F, 0.0F, true);

        botL = new ModelRenderer(this);
        botL.setPos(0.0F, 0.0F, 0.0F);
        hat.addChild(botL);
        botL.texOffs(64, 14).addBox(1.5F, -5.5F, -1.5F, 3.0F, 2.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
