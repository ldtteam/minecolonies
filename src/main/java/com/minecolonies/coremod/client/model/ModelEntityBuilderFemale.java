// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;

public class ModelEntityBuilderFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityBuilderFemale()
    {
        ModelPart chest;
        ModelPart belt;
        ModelPart ruler;
        ModelPart hammerHandle;
        ModelPart hammerHead;
        ModelPart headDetail;
        ModelPart hatBase;
        ModelPart hatBottomMiddle;
        ModelPart hatBack;
        ModelPart hatFront;
        ModelPart hatTopMiddle;
        ModelPart hatBrimBase;
        ModelPart hatBrimFront;
        ModelPart hatBrimFrontTip;
        ModelPart ponytailBase;
        ModelPart ponytailTail;

        texWidth = 128;
        texHeight = 64;

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

        chest = new ModelPart(this);
        chest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, -0.5934F, 0.0F, 0.0F);
        chest.texOffs(17, 32).addBox(-3.5F, 1.7F, -1.0F, 7.0F, 3.0F, 4.0F, 0.0F, true);

        belt = new ModelPart(this);
        belt.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(belt);
        belt.texOffs(0, 40).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 1.0F, 5.0F, 0.0F, true);

        ruler = new ModelPart(this);
        ruler.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(ruler);
        ruler.texOffs(17, 47).addBox(2.0F, 7.3F, -2.2F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hammerHandle = new ModelPart(this);
        hammerHandle.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hammerHandle);
        setRotationAngle(hammerHandle, 0.0F, 0.0F, 0.3142F);
        hammerHandle.texOffs(2, 49).addBox(1.0F, 7.3F, -2.4F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hammerHead = new ModelPart(this);
        hammerHead.setPos(0.0F, 0.0F, 0.0F);
        hammerHandle.addChild(hammerHead);
        hammerHead.texOffs(0, 47).addBox(0.0F, 7.5F, -2.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelPart(this);
        headDetail.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(headDetail);
        headDetail.texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hatBase = new ModelPart(this);
        hatBase.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatBase);
        setRotationAngle(hatBase, -0.1396F, 0.0F, 0.0F);
        hatBase.texOffs(57, 19).addBox(-4.0F, -9.7F, -4.0F, 8.0F, 2.0F, 7.0F, 0.0F, true);

        hatBottomMiddle = new ModelPart(this);
        hatBottomMiddle.setPos(0.0F, 0.0F, 0.0F);
        hatBase.addChild(hatBottomMiddle);
        hatBottomMiddle.texOffs(57, 8).addBox(-3.0F, -10.0F, -5.0F, 6.0F, 2.0F, 9.0F, 0.0F, true);

        hatBack = new ModelPart(this);
        hatBack.setPos(0.0F, 0.0F, 0.0F);
        hatBottomMiddle.addChild(hatBack);
        hatBack.texOffs(64, 31).addBox(-3.5F, -8.0F, 4.0F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        hatFront = new ModelPart(this);
        hatFront.setPos(0.0F, 0.0F, 0.0F);
        hatBottomMiddle.addChild(hatFront);
        hatFront.texOffs(66, 28).addBox(-2.5F, -9.0F, -6.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        hatTopMiddle = new ModelPart(this);
        hatTopMiddle.setPos(0.0F, 0.0F, 0.0F);
        hatBottomMiddle.addChild(hatTopMiddle);
        hatTopMiddle.texOffs(61, 0).addBox(-2.0F, -11.0F, -4.0F, 4.0F, 1.0F, 7.0F, 0.0F, true);

        hatBrimBase = new ModelPart(this);
        hatBrimBase.setPos(0.0F, 0.0F, 0.0F);
        hatBase.addChild(hatBrimBase);
        hatBrimBase.texOffs(53, 33).addBox(-4.5F, -8.0F, -6.0F, 9.0F, 1.0F, 10.0F, 0.0F, true);

        hatBrimFront = new ModelPart(this);
        hatBrimFront.setPos(0.0F, 0.0F, 0.0F);
        hatBrimBase.addChild(hatBrimFront);
        hatBrimFront.texOffs(64, 44).addBox(-3.5F, -8.0F, -7.0F, 7.0F, 1.0F, 1.0F, 0.0F, true);

        hatBrimFrontTip = new ModelPart(this);
        hatBrimFrontTip.setPos(0.0F, 0.0F, 0.0F);
        hatBrimFront.addChild(hatBrimFrontTip);
        hatBrimFrontTip.texOffs(66, 46).addBox(-2.5F, -8.0F, -8.0F, 5.0F, 1.0F, 1.0F, 0.0F, true);

        ponytailBase = new ModelPart(this);
        ponytailBase.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(ponytailBase);
        setRotationAngle(ponytailBase, 0.2269F, 0.0F, 0.0F);
        ponytailBase.texOffs(24, 0).addBox(-1.0F, -2.2F, 3.5F, 2.0F, 5.0F, 1.0F, 0.0F, true);

        ponytailTail = new ModelPart(this);
        ponytailTail.setPos(0.0F, 0.0F, 0.0F);
        ponytailBase.addChild(ponytailTail);
        setRotationAngle(ponytailTail, -0.1222F, 0.0F, 0.0F);
        ponytailTail.texOffs(30, 0).addBox(-0.5F, 2.2F, 3.8F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        hat.visible = false;
    }

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
