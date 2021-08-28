package com.minecolonies.coremod.client.model.raiders;// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.model.geom.ModelPart;

public class ModelShieldmaiden extends NorsemenModel
{
    public ModelShieldmaiden()
    {
        ModelPart skirt;
        ModelPart chest;

        ModelPart shieldA;
        ModelPart shieldB;

        texWidth = 124;
        texHeight = 64;

        head = new ModelPart(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        body = new ModelPart(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        skirt = new ModelPart(this);
        skirt.setPos(-0.95F, 12.25F, 0.5F);
        body.addChild(skirt);
        skirt.texOffs(67, 11).addBox(-3.5F, -0.25F, -3.5F, 9.0F, 6.0F, 6.0F, 0.0F, false);
        skirt.texOffs(67, 0).addBox(-3.0F, -1.0F, -3.0F, 8.0F, 6.0F, 5.0F, 0.0F, false);

        chest = new ModelPart(this);
        chest.setPos(0.0F, 24.0F, -6.0F);
        body.addChild(chest);
        setRotationAngle(chest, -0.4363F, 0.0F, 0.0F);
        chest.texOffs(67, 49).addBox(-4.0F, -23.8558F, -5.7275F, 8.0F, 5.0F, 4.0F, 0.25F, false);

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

        shieldA = new ModelPart(this);
        body.addChild(shieldA);
        shieldA.setPos(9.25F, 15.0F, -4.75F);
        shieldA.texOffs(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
        shieldA.texOffs(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
        shieldA.texOffs(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);

        shieldB = new ModelPart(this);
        body.addChild(shieldB);
        shieldB.setPos(5.0F, 12.0F, 4.0F);
        setRotationAngle(shieldB, 0.0F, -1.5708F, 0.0F);
        shieldB.texOffs(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
        shieldB.texOffs(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
        shieldB.texOffs(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
        hat.visible = false;
    }

    private void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}