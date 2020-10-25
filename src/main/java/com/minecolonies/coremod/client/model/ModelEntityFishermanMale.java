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

        textureWidth = 256;
        textureHeight = 128;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightBoot = new ModelRenderer(this);
        rightBoot.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.addChild(rightBoot);
        rightBoot.setTextureOffset(20, 79).addBox(-0.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftBoot = new ModelRenderer(this);
        leftBoot.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.addChild(leftBoot);
        leftBoot.setTextureOffset(0, 79).addBox(-4.5F, -8.0F, -2.5F, 5.0F, 2.0F, 5.0F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        Hat = new ModelRenderer(this);
        Hat.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(Hat);

        shape6 = new ModelRenderer(this);
        shape6.setRotationPoint(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape6);
        setRotationAngle(shape6, -0.0744F, 0.0F, 0.0F);
        shape6.setTextureOffset(0, 33).addBox(-5.0F, -9.0F, -5.5F, 10.0F, 2.0F, 10.0F, 0.0F, true);

        shape7 = new ModelRenderer(this);
        shape7.setRotationPoint(0.0F, 0.0F, 0.0F);
        shape6.addChild(shape7);
        setRotationAngle(shape7, -0.0744F, 0.0F, 0.0F);
        shape7.setTextureOffset(0, 59).addBox(-4.0F, -10.4F, -4.5F, 8.0F, 2.0F, 8.0F, 0.0F, true);

        shape5 = new ModelRenderer(this);
        shape5.setRotationPoint(0.0F, -0.5432F, 0.5612F);
        shape6.addChild(shape5);
        setRotationAngle(shape5, -0.0045F, 0.0F, -0.1487F);
        shape5.setTextureOffset(24, 48).addBox(-5.7509F, -8.2682F, -6.0492F, 2.0F, 1.0F, 10.0F, 0.0F, true);

        shape4 = new ModelRenderer(this);
        shape4.setRotationPoint(0.0F, -23.0F, 0.0F);
        Hat.addChild(shape4);
        setRotationAngle(shape4, -0.1616F, 0.0F, 0.0F);
        shape4.setTextureOffset(0, 69).addBox(-3.0F, -12.0F, -3.5F, 6.0F, 1.0F, 6.0F, 0.0F, true);

        shape3 = new ModelRenderer(this);
        shape3.setRotationPoint(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape3);
        setRotationAngle(shape3, 0.0744F, 0.0F, 0.0F);
        shape3.setTextureOffset(0, 45).addBox(-5.0F, -8.7F, -6.2F, 10.0F, 1.0F, 2.0F, 0.0F, true);

        shape2 = new ModelRenderer(this);
        shape2.setRotationPoint(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape2);
        setRotationAngle(shape2, -0.0744F, 0.0F, 0.1487F);
        shape2.setTextureOffset(0, 48).addBox(3.7F, -8.65F, -5.5F, 2.0F, 1.0F, 10.0F, 0.0F, true);

        shape1 = new ModelRenderer(this);
        shape1.setRotationPoint(0.0F, -24.0F, 0.0F);
        Hat.addChild(shape1);
        setRotationAngle(shape1, -0.2231F, 0.0F, 0.0F);
        shape1.setTextureOffset(24, 45).addBox(-5.0F, -8.6F, 3.2F, 10.0F, 1.0F, 2.0F, 0.0F, true);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        string = new ModelRenderer(this);
        string.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(string);
        setRotationAngle(string, 0.0F, 0.0F, -0.7436F);
        string.setTextureOffset(53, 38).addBox(-5.0F, -0.5F, -2.3F, 1.0F, 12.0F, 1.0F, 0.0F, true);

        pole = new ModelRenderer(this);
        pole.setRotationPoint(0.0F, 0.0F, 0.0F);
        string.addChild(pole);
        pole.setTextureOffset(57, 52).addBox(-4.0F, -5.0F, 2.0F, 1.0F, 16.0F, 1.0F, 0.0F, true);

        reel = new ModelRenderer(this);
        reel.setRotationPoint(0.0F, 0.0F, 0.0F);
        pole.addChild(reel);
        reel.setTextureOffset(62, 64).addBox(-6.0F, 6.0F, 2.0F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        line = new ModelRenderer(this);
        line.setRotationPoint(0.0F, 0.0F, 0.0F);
        pole.addChild(line);
        line.setTextureOffset(62, 52).addBox(-4.5F, -4.75F, 2.5F, 1.0F, 11.0F, 0.0F, 0.0F, true);

        hookTie1 = new ModelRenderer(this);
        hookTie1.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(hookTie1);
        hookTie1.setTextureOffset(58, 38).addBox(-3.5F, 3.5F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish1 = new ModelRenderer(this);
        fish1.setRotationPoint(0.0F, 0.0F, 0.0F);
        hookTie1.addChild(fish1);
        fish1.setTextureOffset(61, 38).addBox(-4.4F, 5.5F, -2.2F, 2.0F, 4.0F, 0.0F, 0.0F, true);

        hookTie2 = new ModelRenderer(this);
        hookTie2.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(hookTie2);
        hookTie2.setTextureOffset(58, 42).addBox(-1.5F, 5.5F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish2 = new ModelRenderer(this);
        fish2.setRotationPoint(0.0F, 0.0F, 0.0F);
        hookTie2.addChild(fish2);
        fish2.setTextureOffset(61, 42).addBox(-2.0F, 7.5F, -2.2F, 2.0F, 4.0F, 0.0F, 0.0F, true);

        hookTie3 = new ModelRenderer(this);
        hookTie3.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.addChild(hookTie3);
        hookTie3.setTextureOffset(58, 46).addBox(0.5F, 8.0F, -2.2F, 1.0F, 2.0F, 0.0F, 0.0F, true);

        fish3 = new ModelRenderer(this);
        fish3.setRotationPoint(0.0F, 0.0F, 0.0F);
        hookTie3.addChild(fish3);
        fish3.setTextureOffset(61, 46).addBox(0.4F, 10.0F, -2.2F, 2.0F, 4.0F, 0.0F, 0.0F, true);

        bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
