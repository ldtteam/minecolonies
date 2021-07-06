// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFishermanMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFishermanMale()
    {
        ModelRenderer rightBoot;
        ModelRenderer leftBoot;
        ModelRenderer Hat;
        ModelRenderer shape6;
        ModelRenderer shape7;
        ModelRenderer shape5;
        ModelRenderer shape4;
        ModelRenderer shape3;
        ModelRenderer shape2;
        ModelRenderer shape1;
        ModelRenderer string;
        ModelRenderer pole;
        ModelRenderer reel;
        ModelRenderer line;
        ModelRenderer hookTie1;
        ModelRenderer fish1;
        ModelRenderer hookTie2;
        ModelRenderer fish2;
        ModelRenderer hookTie3;
        ModelRenderer fish3;

        texWidth = 256;
        texHeight = 128;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightBoot = new ModelRenderer(this);
        rightBoot.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.addChild(rightBoot);
        rightBoot.texOffs(20, 79).addBox(-0.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftBoot = new ModelRenderer(this);
        leftBoot.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.addChild(leftBoot);
        leftBoot.texOffs(0, 79).addBox(-4.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        Hat = new ModelRenderer(this);
        Hat.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(Hat);

        shape6 = new ModelRenderer(this);
        shape6.setPos(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape6);
        setRotationAngle(shape6, -0.0744F, 0.0F, 0.0F);
        shape6.texOffs(0, 33).addBox(-5.0F, -9.0F, -5.5F, 10.0F, 2.0F, 10.0F, 0.0F, true);

        shape7 = new ModelRenderer(this);
        shape7.setPos(0.0F, 0.0F, 0.0F);
        shape6.addChild(shape7);
        setRotationAngle(shape7, -0.0744F, 0.0F, 0.0F);
        shape7.texOffs(0, 59).addBox(-4.0F, -10.4F, -4.5F, 8.0F, 2.0F, 8.0F, 0.0F, true);

        shape5 = new ModelRenderer(this);
        shape5.setPos(0.0F, -0.5432F, 0.5612F);
        shape6.addChild(shape5);
        setRotationAngle(shape5, -0.0045F, 0.0F, -0.1487F);
        shape5.texOffs(24, 48).addBox(-5.7509F, -8.2682F, -6.0492F, 2.0F, 1.0F, 10.0F, 0.0F, true);

        shape4 = new ModelRenderer(this);
        shape4.setPos(0.0F, -23.0F, 0.0F);
        Hat.addChild(shape4);
        setRotationAngle(shape4, -0.1616F, 0.0F, 0.0F);
        shape4.texOffs(0, 69).addBox(-3.0F, -12.0F, -3.5F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        shape3 = new ModelRenderer(this);
        shape3.setPos(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape3);
        setRotationAngle(shape3, 0.0744F, 0.0F, 0.0F);
        shape3.texOffs(0, 45).addBox(-5.0F, -8.7F, -6.2F, 10.0F, 1.0F, 2.0F, 0.0F, true);

        shape2 = new ModelRenderer(this);
        shape2.setPos(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape2);
        setRotationAngle(shape2, -0.0744F, 0.0F, 0.1487F);
        shape2.texOffs(0, 48).addBox(3.7F, -8.65F, -5.5F, 2.0F, 1.0F, 10.0F, 0.0F, true);

        shape1 = new ModelRenderer(this);
        shape1.setPos(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape1);
        setRotationAngle(shape1, -0.2231F, 0.0F, 0.0F);
        shape1.texOffs(24, 45).addBox(-5.0F, -8.6F, 3.2F, 10.0F, 1.0F, 2.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        string = new ModelRenderer(this);
        string.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(string);
        setRotationAngle(string, 0.0F, 0.0F, -0.7436F);
        string.texOffs(53, 38).addBox(-5.0F, -0.5F, -2.3F, 1.0F, 12.0F, 1.0F, 0.0F, true);

        pole = new ModelRenderer(this);
        pole.setPos(0.0F, 0.0F, 0.0F);
        string.addChild(pole);
        pole.texOffs(57, 52).addBox(-4.0F, -5.0F, 2.0F, 1.0F, 16.0F, 1.0F, 0.0F, true);

        reel = new ModelRenderer(this);
        reel.setPos(0.0F, 0.0F, 0.0F);
        pole.addChild(reel);
        reel.texOffs(62, 64).addBox(-6.0F, 6.0F, 2.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        line = new ModelRenderer(this);
        line.setPos(0.0F, 0.0F, 0.0F);
        pole.addChild(line);
        line.texOffs(62, 52).addBox(-4.5F, -4.75F, 2.5F, 1.0F, 11.0F, 0.0F, 0.0F, true);

        hookTie1 = new ModelRenderer(this);
        hookTie1.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hookTie1);
        hookTie1.texOffs(58, 38).addBox(-3.5F, 3.5F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish1 = new ModelRenderer(this);
        fish1.setPos(0.0F, 0.0F, 0.0F);
        hookTie1.addChild(fish1);
        fish1.texOffs(61, 38).addBox(-4.4F, 5.5F, -2.2F, 2.0F, 4.0F, 0.0F, 0.0F, true);

        hookTie2 = new ModelRenderer(this);
        hookTie2.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hookTie2);
        hookTie2.texOffs(58, 42).addBox(-1.5F, 5.5F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish2 = new ModelRenderer(this);
        fish2.setPos(0.0F, 0.0F, 0.0F);
        hookTie2.addChild(fish2);
        fish2.texOffs(61, 42).addBox(-2.0F, 7.5F, -2.2F, 2.0F, 4.0F, 0.0F, 0.0F, true);

        hookTie3 = new ModelRenderer(this);
        hookTie3.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hookTie3);
        hookTie3.texOffs(58, 46).addBox(0.5F, 8.0F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish3 = new ModelRenderer(this);
        fish3.setPos(0.0F, 0.0F, 0.0F);
        hookTie3.addChild(fish3);
        fish3.texOffs(61, 46).addBox(0.4F, 10.0F, -2.2F, 2.0F, 4.0F, 0.0F, 0.0F, true);

        hat.visible = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
