// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFishermanFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFishermanFemale()
    {
        ModelRenderer rightBoot;
        ModelRenderer leftBoot;
        ModelRenderer hairBack1;
        ModelRenderer hairBack2;
        ModelRenderer hairBack3;
        ModelRenderer hairBack4;
        ModelRenderer hairBack5;
        ModelRenderer hairBack6;
        ModelRenderer hairBack7;
        ModelRenderer hairBack8;
        ModelRenderer hairBack9;
        ModelRenderer hairBack10;
        ModelRenderer string;
        ModelRenderer string2;
        ModelRenderer hookTie1;
        ModelRenderer hookTie2;
        ModelRenderer hookTie3;
        ModelRenderer fish1;
        ModelRenderer fish2;
        ModelRenderer fish3;
        ModelRenderer reel;
        ModelRenderer line;
        ModelRenderer pole;
        ModelRenderer chest;

        texWidth = 256;
        texHeight = 128;

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightBoot = new ModelRenderer(this);
        rightBoot.setPos(-2.0F, 12.0F, 0.0F);
        rightLeg.addChild(rightBoot);
        rightBoot.texOffs(20, 102).addBox(-0.5F, -7.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftBoot = new ModelRenderer(this);
        leftBoot.setPos(2.0F, 12.0F, 0.0F);
        leftLeg.addChild(leftBoot);
        leftBoot.texOffs(0, 102).addBox(-4.5F, -7.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack1);
        hairBack1.texOffs(0, 74).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairBack2 = new ModelRenderer(this);
        hairBack2.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack2);
        hairBack2.texOffs(5, 74).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairBack3 = new ModelRenderer(this);
        hairBack3.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack3);
        hairBack3.texOffs(0, 63).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, true);

        hairBack4 = new ModelRenderer(this);
        hairBack4.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack4);
        hairBack4.texOffs(12, 69).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        hairBack5 = new ModelRenderer(this);
        hairBack5.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack5);
        hairBack5.texOffs(0, 69).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        hairBack6 = new ModelRenderer(this);
        hairBack6.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack6);
        hairBack6.texOffs(0, 33).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, 0.0F, true);

        hairBack7 = new ModelRenderer(this);
        hairBack7.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack7);
        hairBack7.texOffs(0, 56).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, true);

        hairBack8 = new ModelRenderer(this);
        hairBack8.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack8);
        hairBack8.texOffs(0, 45).addBox(-3.5F, 0.5F, 3.5F, 7.0F, 4.0F, 1.0F, 0.0F, true);

        hairBack9 = new ModelRenderer(this);
        hairBack9.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack9);
        hairBack9.texOffs(0, 50).addBox(-2.5F, 4.5F, 3.5F, 5.0F, 2.0F, 1.0F, 0.0F, true);

        hairBack10 = new ModelRenderer(this);
        hairBack10.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hairBack10);
        hairBack10.texOffs(0, 53).addBox(-1.5F, 6.5F, 3.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        string = new ModelRenderer(this);
        string.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(string);
        setRotationAngle(string, 0.0F, 0.0F, -1.041F);
        string.texOffs(53, 38).addBox(-8.0F, -0.5F, -2.3F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        string2 = new ModelRenderer(this);
        string2.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(string2);
        setRotationAngle(string2, 0.0F, 0.0F, -1.3756F);
        string2.texOffs(53, 44).addBox(-9.05F, 1.65F, -2.3F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hookTie1 = new ModelRenderer(this);
        hookTie1.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hookTie1);
        hookTie1.texOffs(58, 38).addBox(-3.5F, 7.0F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        hookTie2 = new ModelRenderer(this);
        hookTie2.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hookTie2);
        hookTie2.texOffs(58, 42).addBox(-1.5F, 8.5F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        hookTie3 = new ModelRenderer(this);
        hookTie3.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(hookTie3);
        hookTie3.texOffs(58, 46).addBox(1.0F, 9.0F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish1 = new ModelRenderer(this);
        fish1.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(fish1);
        fish1.texOffs(61, 38).addBox(-4.4F, 9.0F, -2.2F, 2.0F, 3.0F, 0.0F, 0.0F, true);

        fish2 = new ModelRenderer(this);
        fish2.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(fish2);
        fish2.texOffs(61, 42).addBox(-2.0F, 10.5F, -2.2F, 2.0F, 3.0F, 0.0F, 0.0F, true);

        fish3 = new ModelRenderer(this);
        fish3.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(fish3);
        fish3.texOffs(61, 46).addBox(0.9F, 11.0F, -2.2F, 2.0F, 3.0F, 0.0F, 0.0F, true);

        reel = new ModelRenderer(this);
        reel.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(reel);
        setRotationAngle(reel, 0.0F, 0.0F, -0.7436F);
        reel.texOffs(62, 64).addBox(-6.0F, 6.0F, 2.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        line = new ModelRenderer(this);
        line.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(line);
        setRotationAngle(line, 0.0F, 0.0F, -0.7436F);
        line.texOffs(62, 52).addBox(-4.5F, -4.75F, 2.5F, 1.0F, 11.0F, 0.0F, 0.0F, true);

        pole = new ModelRenderer(this);
        pole.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(pole);
        setRotationAngle(pole, 0.0F, 0.0F, -0.7436F);
        pole.texOffs(57, 52).addBox(-4.0F, -5.0F, 2.0F, 1.0F, 16.0F, 1.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setPos(0.0F, 0.0F, 0.0F);
        body.addChild(chest);
        setRotationAngle(chest, -0.632F, 0.0F, 0.0F);
        chest.texOffs(25, 32).addBox(-3.5F, 3.5F, 0.0F, 7.0F, 3.0F, 3.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
