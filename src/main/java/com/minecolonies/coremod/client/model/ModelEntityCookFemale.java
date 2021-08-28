// Made with Blockbench 3.6.5
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityCookFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityCookFemale() {
        ModelPart skirt;
        ModelPart dress1;
        ModelPart dress2;
        ModelPart dress3;
        ModelPart bipedChest;
        ModelPart headDetail;
        ModelPart hair;

        texWidth = 128;
        texHeight = 128;

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        skirt = new ModelPart(this);
        skirt.setPos(-4.0F, 12.0F, -4.0F);
        body.addChild(skirt);


        dress1 = new ModelPart(this);
        dress1.setPos(0.0F, 0.0F, 0.0F);
        skirt.addChild(dress1);
        dress1.texOffs(0, 49).addBox(0.0F, 0.0F, 1.0F, 8.0F, 1.0F, 6.0F, 0.0F, true);

        dress2 = new ModelPart(this);
        dress2.setPos(-1.0F, 1.0F, 0.0F);
        skirt.addChild(dress2);
        dress2.texOffs(0, 56).addBox(0.0F, 0.0F, 0.0F, 10.0F, 4.0F, 8.0F, 0.0F, true);

        dress3 = new ModelPart(this);
        dress3.setPos(-2.0F, 5.0F, -1.0F);
        skirt.addChild(dress3);
        dress3.texOffs(0, 68).addBox(0.0F, 0.0F, 0.0F, 12.0F, 3.0F, 10.0F, 0.0F, true);

        bipedChest = new ModelPart(this);
        bipedChest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(bipedChest);
        setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
        bipedChest.texOffs(17, 32).addBox(-3.5F, 2.7F, -0.5F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelPart(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelPart(this);
        hair.setPos(-4.5F, -9.2F, 0.0F);
        head.addChild(hair);
        setRotationAngle(hair, -0.8551F, 0.0F, 0.0F);
        hair.texOffs(0, 39).addBox(0.0F, 0.0F, 0.0F, 9.0F, 2.0F, 8.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z) {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}