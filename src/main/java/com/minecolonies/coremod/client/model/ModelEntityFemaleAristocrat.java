// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFemaleAristocrat extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFemaleAristocrat()
    {
        ModelRenderer hair1;
        ModelRenderer hair2;
        ModelRenderer hair3;
        ModelRenderer hair4;
        ModelRenderer hair6;
        ModelRenderer hair7;
        ModelRenderer breast;
        ModelRenderer dressPart1;
        ModelRenderer dressPart2;
        ModelRenderer dressPart3;
        ModelRenderer dressPart4;
        ModelRenderer dressPart5;
        ModelRenderer SkirtPart2;
        ModelRenderer dressPart13;
        ModelRenderer dressPart12;
        ModelRenderer dressPart11;
        ModelRenderer dressPart10;
        ModelRenderer dressPart9;
        ModelRenderer dressPart8;
        ModelRenderer dressPart6;
        ModelRenderer dressPart7;
        ModelRenderer umbrella;
        ModelRenderer umbrellaHand;
        ModelRenderer leftArm1;
        ModelRenderer leftArm2;

        texWidth = 64;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 1.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair1 = new ModelRenderer(this);
        hair1.setPos(0.0F, -8.0F, 1.0F);
        head.addChild(hair1);
        hair1.texOffs(32, 0).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 3.0F, 6.0F, 0.0F, false);

        hair2 = new ModelRenderer(this);
        hair2.setPos(0.0F, -11.0F, 1.0F);
        head.addChild(hair2);
        hair2.texOffs(56, 0).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);

        hair3 = new ModelRenderer(this);
        hair3.setPos(0.0F, -13.0F, 1.0F);
        head.addChild(hair3);
        hair3.texOffs(32, 10).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 2.0F, 4.0F, 0.0F, false);

        hair4 = new ModelRenderer(this);
        hair4.setPos(0.0F, -8.0F, 1.0F);
        head.addChild(hair4);
        hair4.texOffs(48, 10).addBox(-4.0F, 8.0F, 2.0F, 4.0F, 10.0F, 1.0F, 0.0F, false);
        hair4.texOffs(48, 10).addBox(0.0F, 8.0F, 2.0F, 4.0F, 10.0F, 1.0F, 0.0F, true);

        hair6 = new ModelRenderer(this);
        hair6.setPos(0.0F, -8.0F, 1.0F);
        head.addChild(hair6);
        hair6.texOffs(54, 33).addBox(-4.0F, 8.0F, -4.0F, 1.0F, 9.0F, 1.0F, 0.0F, false);

        hair7 = new ModelRenderer(this);
        hair7.setPos(0.0F, -8.0F, 1.0F);
        head.addChild(hair7);
        hair7.texOffs(59, 33).addBox(3.0F, 8.0F, -4.0F, 1.0F, 9.0F, 1.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 1.0F);
        body.texOffs(12, 17).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 3.0F, 0.0F, false);

        breast = new ModelRenderer(this);
        breast.setPos(-1.0F, 3.0F, 1.0F);
        body.addChild(breast);
        setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
        breast.texOffs(0, 33).addBox(-3.0F, -0.7321F, -3.5F, 8.0F, 4.0F, 3.0F, 0.0F, false);

        dressPart1 = new ModelRenderer(this);
        dressPart1.setPos(0.9F, 16.0F, 1.0F);
        body.addChild(dressPart1);
        setRotationAngle(dressPart1, 0.0F, 0.8203F, 0.0F);
        dressPart1.texOffs(18, 33).addBox(-5.3227F, 0.9F, -7.5437F, 12.0F, 6.0F, 11.0F, 0.36F, false);

        dressPart2 = new ModelRenderer(this);
        dressPart2.setPos(0.0F, 16.0F, 0.0F);
        body.addChild(dressPart2);
        dressPart2.texOffs(18, 33).addBox(-5.9F, 0.925F, -6.45F, 12.0F, 6.0F, 11.0F, 0.36F, false);

        dressPart3 = new ModelRenderer(this);
        dressPart3.setPos(18.9849F, 11.875F, 6.2661F);
        body.addChild(dressPart3);
        setRotationAngle(dressPart3, 0.0F, 0.8203F, 0.0F);
        dressPart3.texOffs(30, 50).addBox(-12.7815F, 0.8F, -22.2562F, 10.0F, 4.0F, 7.0F, 0.4F, false);

        dressPart4 = new ModelRenderer(this);
        dressPart4.setPos(-1.2963F, 14.675F, -0.7978F);
        body.addChild(dressPart4);
        setRotationAngle(dressPart4, 0.0F, -1.5533F, 0.0F);
        dressPart4.texOffs(30, 50).addBox(-5.1087F, -2.0F, -3.0018F, 10.0F, 4.0F, 7.0F, 0.31F, false);

        dressPart5 = new ModelRenderer(this);
        dressPart5.setPos(2.0094F, 14.675F, -1.0005F);
        body.addChild(dressPart5);
        setRotationAngle(dressPart5, 0.0F, -1.5708F, 0.0F);
        dressPart5.texOffs(30, 50).addBox(-4.8F, -2.0F, -3.5F, 10.0F, 4.0F, 7.0F, 0.2F, false);

        SkirtPart2 = new ModelRenderer(this);
        SkirtPart2.setPos(-36.8F, 9.6F, -3.0F);
        body.addChild(SkirtPart2);

        dressPart13 = new ModelRenderer(this);
        dressPart13.setPos(-3.0F, -2.0F, -3.0F);
        SkirtPart2.addChild(dressPart13);
        setRotationAngle(dressPart13, -0.1745F, 0.7854F, 0.0F);
        dressPart13.texOffs(0, 40).addBox(21.9002F, -3.6408F, 28.2909F, 4.0F, 7.0F, 4.0F, -0.2F, false);

        dressPart12 = new ModelRenderer(this);
        dressPart12.setPos(-3.0F, -2.0F, 3.0F);
        SkirtPart2.addChild(dressPart12);
        setRotationAngle(dressPart12, -0.1745F, 2.3562F, 0.0F);
        dressPart12.texOffs(0, 40).addBox(-28.8701F, -3.0268F, 24.8091F, 4.0F, 7.0F, 4.0F, -0.2F, false);

        dressPart11 = new ModelRenderer(this);
        dressPart11.setPos(3.0F, -2.0F, 3.0F);
        SkirtPart2.addChild(dressPart11);
        setRotationAngle(dressPart11, -0.1745F, -2.3562F, 0.0F);
        dressPart11.texOffs(0, 40).addBox(-27.3144F, 6.1334F, -26.5652F, 4.0F, 7.0F, 4.0F, -0.2F, false);

        dressPart10 = new ModelRenderer(this);
        dressPart10.setPos(2.7F, -2.0F, -3.0F);
        SkirtPart2.addChild(dressPart10);
        setRotationAngle(dressPart10, -0.1745F, -0.7854F, 0.0F);
        dressPart10.texOffs(0, 40).addBox(26.2843F, 5.4701F, -23.3793F, 4.0F, 7.0F, 4.0F, -0.2F, false);

        dressPart9 = new ModelRenderer(this);
        dressPart9.setPos(0.0F, 0.0F, 0.0F);
        SkirtPart2.addChild(dressPart9);
        setRotationAngle(dressPart9, -0.1745F, -1.5708F, 0.0F);
        dressPart9.texOffs(0, 40).addBox(0.1F, 6.2733F, -40.8257F, 4.0F, 7.0F, 4.0F, 0.0F, false);

        dressPart8 = new ModelRenderer(this);
        dressPart8.setPos(0.0F, 0.0F, 0.0F);
        SkirtPart2.addChild(dressPart8);
        setRotationAngle(dressPart8, -0.1745F, 1.5708F, 0.0F);
        dressPart8.texOffs(0, 40).addBox(-4.1F, -6.5767F, 32.0501F, 4.0F, 7.0F, 4.0F, 0.0F, false);

        dressPart6 = new ModelRenderer(this);
        dressPart6.setPos(36.9F, 3.271F, 4.3333F);
        SkirtPart2.addChild(dressPart6);
        setRotationAngle(dressPart6, 0.2443F, 0.0F, 0.0F);
        dressPart6.texOffs(0, 40).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F, 0.0F, false);

        dressPart7 = new ModelRenderer(this);
        dressPart7.setPos(36.9F, 3.0243F, -0.2986F);
        SkirtPart2.addChild(dressPart7);
        setRotationAngle(dressPart7, -0.2967F, 0.0F, 0.0F);
        dressPart7.texOffs(0, 40).addBox(-2.0F, -3.5F, -2.0F, 4.0F, 7.0F, 4.0F, -0.2F, false);

        umbrella = new ModelRenderer(this);
        umbrella.setPos(5.05F, -10.8F, 5.5F);
        body.addChild(umbrella);
        setRotationAngle(umbrella, -0.6065F, 0.0F, 0.0F);
        umbrella.texOffs(0, 54).addBox(-4.0F, -0.0075F, -3.9862F, 9.0F, 1.0F, 9.0F, 0.0F, false);

        umbrellaHand = new ModelRenderer(this);
        umbrellaHand.setPos(5.0F, 6.0F, -5.0F);
        body.addChild(umbrellaHand);
        setRotationAngle(umbrellaHand, 2.5656F, 0.0F, 0.0F);
        umbrellaHand.texOffs(60, 10).addBox(0.0F, -2.0F, 0.0F, 1.0F, 21.0F, 1.0F, 0.0F, false);

        leftArm1 = new ModelRenderer(this);
        leftArm1.setPos(4.0F, 0.0F, 0.0F);
        body.addChild(leftArm1);
        leftArm1.texOffs(34, 17).addBox(0.0F, 0.0F, -1.0F, 3.0F, 6.0F, 3.0F, 0.0F, false);

        leftArm2 = new ModelRenderer(this);
        leftArm2.setPos(5.0F, 6.0F, 1.0F);
        body.addChild(leftArm2);
        setRotationAngle(leftArm2, -1.5708F, 0.0F, 0.0F);
        leftArm2.texOffs(46, 22).addBox(-0.99F, -1.01F, -1.0F, 3.0F, 7.0F, 3.0F, 0.0F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 0.0F, 0.0F);
        rightArm.texOffs(34, 17).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.0F, 12.0F, 1.0F);
        rightLeg.texOffs(0, 17).addBox(-1.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 1.0F);
        leftLeg.texOffs(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);
        hat.visible = false;
        leftArm.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
