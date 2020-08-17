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

        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightBoot = new ModelRenderer(this);
        rightBoot.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.addChild(rightBoot);
        rightBoot.setTextureOffset(20, 102).addBox(-0.5F, -7.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftBoot = new ModelRenderer(this);
        leftBoot.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.addChild(leftBoot);
        leftBoot.setTextureOffset(0, 102).addBox(-4.5F, -7.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hairBack1 = new ModelRenderer(this);
        hairBack1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack1);
        hairBack1.setTextureOffset(0, 74).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);

        hairBack2 = new ModelRenderer(this);
        hairBack2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack2);
        hairBack2.setTextureOffset(5, 74).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

        hairBack3 = new ModelRenderer(this);
        hairBack3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack3);
        hairBack3.setTextureOffset(0, 63).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, 0.0F, true);

        hairBack4 = new ModelRenderer(this);
        hairBack4.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack4);
        hairBack4.setTextureOffset(12, 69).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        hairBack5 = new ModelRenderer(this);
        hairBack5.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack5);
        hairBack5.setTextureOffset(0, 69).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, 0.0F, true);

        hairBack6 = new ModelRenderer(this);
        hairBack6.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack6);
        hairBack6.setTextureOffset(0, 33).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, 0.0F, true);

        hairBack7 = new ModelRenderer(this);
        hairBack7.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack7);
        hairBack7.setTextureOffset(0, 56).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, 0.0F, true);

        hairBack8 = new ModelRenderer(this);
        hairBack8.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack8);
        hairBack8.setTextureOffset(0, 45).addBox(-3.5F, 0.5F, 3.5F, 7.0F, 4.0F, 1.0F, 0.0F, true);

        hairBack9 = new ModelRenderer(this);
        hairBack9.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack9);
        hairBack9.setTextureOffset(0, 50).addBox(-2.5F, 4.5F, 3.5F, 5.0F, 2.0F, 1.0F, 0.0F, true);

        hairBack10 = new ModelRenderer(this);
        hairBack10.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.addChild(hairBack10);
        hairBack10.setTextureOffset(0, 53).addBox(-1.5F, 6.5F, 3.5F, 3.0F, 1.0F, 1.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        string = new ModelRenderer(this);
        string.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(string);
        setRotationAngle(string, 0.0F, 0.0F, -1.041F);
        string.setTextureOffset(53, 38).addBox(-8.0F, -0.5F, -2.3F, 1.0F, 5.0F, 1.0F, 0.0F, true);

        string2 = new ModelRenderer(this);
        string2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(string2);
        setRotationAngle(string2, 0.0F, 0.0F, -1.3756F);
        string2.setTextureOffset(53, 44).addBox(-9.05F, 1.65F, -2.3F, 1.0F, 4.0F, 1.0F, 0.0F, true);

        hookTie1 = new ModelRenderer(this);
        hookTie1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(hookTie1);
        hookTie1.setTextureOffset(58, 38).addBox(-3.5F, 7.0F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        hookTie2 = new ModelRenderer(this);
        hookTie2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(hookTie2);
        hookTie2.setTextureOffset(58, 42).addBox(-1.5F, 8.5F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        hookTie3 = new ModelRenderer(this);
        hookTie3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(hookTie3);
        hookTie3.setTextureOffset(58, 46).addBox(1.0F, 9.0F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish1 = new ModelRenderer(this);
        fish1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(fish1);
        fish1.setTextureOffset(61, 38).addBox(-4.4F, 9.0F, -2.2F, 2.0F, 3.0F, 0.0F, 0.0F, true);

        fish2 = new ModelRenderer(this);
        fish2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(fish2);
        fish2.setTextureOffset(61, 42).addBox(-2.0F, 10.5F, -2.2F, 2.0F, 3.0F, 0.0F, 0.0F, true);

        fish3 = new ModelRenderer(this);
        fish3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(fish3);
        fish3.setTextureOffset(61, 46).addBox(0.9F, 11.0F, -2.2F, 2.0F, 3.0F, 0.0F, 0.0F, true);

        reel = new ModelRenderer(this);
        reel.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(reel);
        setRotationAngle(reel, 0.0F, 0.0F, -0.7436F);
        reel.setTextureOffset(62, 64).addBox(-6.0F, 6.0F, 2.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        line = new ModelRenderer(this);
        line.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(line);
        setRotationAngle(line, 0.0F, 0.0F, -0.7436F);
        line.setTextureOffset(62, 52).addBox(-4.5F, -4.75F, 2.5F, 1.0F, 11.0F, 0.0F, 0.0F, true);

        pole = new ModelRenderer(this);
        pole.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(pole);
        setRotationAngle(pole, 0.0F, 0.0F, -0.7436F);
        pole.setTextureOffset(57, 52).addBox(-4.0F, -5.0F, 2.0F, 1.0F, 16.0F, 1.0F, 0.0F, true);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, -0.632F, 0.0F, 0.0F);
        chest.setTextureOffset(25, 32).addBox(-3.5F, 3.5F, 0.0F, 7.0F, 3.0F, 3.0F, 0.0F, true);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
