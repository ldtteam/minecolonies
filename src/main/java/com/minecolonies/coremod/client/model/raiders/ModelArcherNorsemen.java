package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelArcherNorsemen extends NorsemenModel
{
    public ModelArcherNorsemen()
    {
        ModelPart quiver;
        ModelPart hood;
        texWidth = 124;
        texHeight = 64;

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(33, 1).addBox(-4.0F, -8.0F, -3.75F, 8.0F, 8.0F, 7.0F, 0.5F, false);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        body.texOffs(75, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        hood = new ModelPart(this);
        hood.setPos(-0.5F, 24.5F, 0.0F);
        head.addChild(hood);
        hood.texOffs(59, 25).addBox(-4.0F, -33.0F, -4.0F, 9.0F, 9.0F, 8.0F, 0.5F, false);
        hood.texOffs(64, 0).addBox(-8.0F, -24.75F, -2.25F, 17.0F, 20.0F, 5.0F, 0.25F, false);

        quiver = new ModelPart(this);
        quiver.setPos(-4.9F, 2.0F, 6.0F);
        setRotationAngle(quiver, 0.0F, 0.0F, -0.6109F);
        body.addChild(quiver);
        quiver.texOffs(99, 46).addBox(-0.979F, -4.9528F, -1.25F, 3.0F, 14.0F, 0.0F, 0.0F, false);
        quiver.texOffs(90, 45).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, 0.0F, false);
        quiver.texOffs(79, 46).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, 0.25F, false);

        rightArm = new ModelPart(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        leftArm = new ModelPart(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        rightLeg = new ModelPart(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightLeg.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        leftLeg = new ModelPart(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLeg.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        leftLeg.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        hat.visible = false;
    }

    private void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}