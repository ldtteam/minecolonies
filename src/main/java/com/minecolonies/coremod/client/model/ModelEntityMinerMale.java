// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMinerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMinerMale()
    {
        ModelRenderer headDetail;
        ModelRenderer hair;
        ModelRenderer lamp;
        ModelRenderer gloveleft;
        ModelRenderer gloveright;
        ModelRenderer backpack;
        ModelRenderer stones;
        ModelRenderer torchleft;
        ModelRenderer pack;
        ModelRenderer torchright;

        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, -0.4F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -7.6F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.4F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 24.4F, 0.0F);
        bipedHead.addChild(hair);
        hair.setTextureOffset(19, 39).addBox(-2.5667F, -31.8667F, -4.5F, 4.0667F, 1.0F, 8.0F, 0.0F, true);
        hair.setTextureOffset(31, 52).addBox(2.5F, -28.9F, -3.5F, 2.0F, 1.0F, 7.0F, 0.0F, true);
        hair.setTextureOffset(0, 33).addBox(-4.5F, -31.9333F, -4.5F, 2.0F, 3.0667F, 9.0F, 0.0F, true);
        hair.setTextureOffset(0, 46).addBox(-4.5F, -24.9F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);
        hair.setTextureOffset(41, 40).addBox(2.5F, -27.9F, 0.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);
        hair.setTextureOffset(11, 46).addBox(3.5F, -24.9F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);
        hair.setTextureOffset(21, 50).addBox(3.5F, -29.9F, -3.5667F, 1.0F, 1.0F, 7.3333F, 0.0F, true);
        hair.setTextureOffset(43, 53).addBox(-4.5F, -28.9F, 0.4333F, 2.0F, 1.0F, 4.0667F, 0.0F, true);
        hair.setTextureOffset(12, 49).addBox(3.5F, -30.9F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);
        hair.setTextureOffset(0, 46).addBox(3.5F, -31.9F, -4.5F, 1.0F, 1.0F, 8.0F, 0.0F, true);
        hair.setTextureOffset(36, 41).addBox(3.5F, -30.9F, -4.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(0, 33).addBox(-4.5F, -28.9F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(0, 37).addBox(1.5F, -31.9F, -4.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(52, 57).addBox(-4.5F, -27.9F, 0.5F, 2.0F, 3.0F, 4.0F, 0.0F, true);
        hair.setTextureOffset(23, 33).addBox(-2.5333F, -30.9F, -4.5F, 1.0667F, 1.0F, 4.0F, 0.0F, true);
        hair.setTextureOffset(14, 35).addBox(-4.5F, -28.9F, -3.5F, 2.0F, 2.0F, 4.0F, 0.0F, true);
        hair.setTextureOffset(1, 58).addBox(-3.5F, -24.9F, 3.5F, 7.0F, 3.0F, 1.0F, 0.0F, true);
        hair.setTextureOffset(65, 56).addBox(-2.5F, -31.9F, 3.5F, 7.0F, 7.0F, 1.0F, 0.0F, true);

        lamp = new ModelRenderer(this);
        lamp.setRotationPoint(0.0F, 24.4F, 0.0F);
        bipedHead.addChild(lamp);
        lamp.setTextureOffset(89, 50).addBox(-0.6333F, -32.45F, -4.9F, 1.1333F, 1.9333F, 9.6667F, 0.0F, true);
        lamp.setTextureOffset(84, 53).addBox(-1.0333F, -30.5167F, 0.4333F, 1.9333F, 0.7333F, 4.3333F, 0.0F, true);
        lamp.setTextureOffset(102, 41).addBox(0.9133F, -30.5167F, -4.9F, 3.4F, 0.7333F, 9.6667F, 0.0F, true);
        lamp.setTextureOffset(102, 53).addBox(-4.42F, -30.5167F, -4.9F, 3.4F, 0.7333F, 9.6667F, 0.0F, true);
        lamp.setTextureOffset(75, 41).addBox(-5.3F, -30.65F, -5.0333F, 1.1333F, 1.0F, 9.9333F, 0.0F, true);
        lamp.setTextureOffset(51, 39).addBox(4.0333F, -30.65F, -5.0333F, 1.2667F, 1.0F, 9.8F, 0.0F, true);
        lamp.setTextureOffset(62, 52).addBox(-1.5667F, -30.9667F, -5.0333F, 3.1333F, 1.4F, 0.7333F, 0.0F, true);
        lamp.setTextureOffset(54, 52).addBox(-1.4F, -30.7333F, -5.3333F, 2.7333F, 1.0F, 0.6F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        gloveleft = new ModelRenderer(this);
        gloveleft.setRotationPoint(-5.0F, 22.0F, 0.0F);
        bipedLeftArm.addChild(gloveleft);
        gloveleft.setTextureOffset(75, 9).addBox(3.4667F, -16.3333F, -2.6667F, 5.0F, 1.0F, 5.2667F, 0.0F, false);
        gloveleft.setTextureOffset(78, 0).addBox(3.6F, -15.3333F, -2.4F, 4.7333F, 4.0F, 4.7333F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        gloveright = new ModelRenderer(this);
        gloveright.setRotationPoint(5.0F, 22.0F, 0.0F);
        bipedRightArm.addChild(gloveright);
        gloveright.setTextureOffset(96, 9).addBox(-8.5333F, -16.3333F, -2.6667F, 5.0F, 1.0F, 5.2667F, 0.0F, false);
        gloveright.setTextureOffset(97, 0).addBox(-8.4F, -15.3333F, -2.4F, 4.7333F, 4.0F, 4.7333F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        backpack = new ModelRenderer(this);
        backpack.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(backpack);


        stones = new ModelRenderer(this);
        stones.setRotationPoint(0.0F, 0.0F, 0.0F);
        backpack.addChild(stones);
        stones.setTextureOffset(98, 26).addBox(-1.3481F, -22.0152F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(102, 26).addBox(-1.3481F, -22.0152F, 2.2418F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(106, 26).addBox(-2.0148F, -21.7485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(110, 26).addBox(-2.0148F, -21.7485F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(114, 26).addBox(-1.3481F, -22.1485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(118, 26).addBox(-0.2814F, -22.1485F, 3.5752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(122, 26).addBox(-0.6814F, -21.6152F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(98, 28).addBox(-0.6814F, -21.6152F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(102, 28).addBox(-0.6814F, -22.0152F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(106, 28).addBox(-0.6814F, -22.0152F, 1.8667F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(110, 28).addBox(-2.4514F, -21.8953F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(114, 28).addBox(-2.4514F, -21.8953F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(118, 28).addBox(-2.9975F, -21.5487F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(122, 28).addBox(-2.8642F, -21.6821F, 2.025F, 0.8667F, 1.2667F, 1.1333F, 0.0F, false);
        stones.setTextureOffset(98, 30).addBox(-2.9975F, -21.8154F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(102, 30).addBox(-0.3308F, -21.5487F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(106, 30).addBox(-0.3308F, -21.5487F, 2.025F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(110, 30).addBox(0.2153F, -21.8953F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(114, 30).addBox(0.2153F, -21.8953F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(118, 30).addBox(1.9852F, -21.8819F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(98, 32).addBox(1.9852F, -21.6152F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(102, 32).addBox(1.9852F, -21.6152F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(106, 32).addBox(1.3186F, -22.1485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(110, 32).addBox(0.7852F, -22.1485F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(114, 32).addBox(0.6519F, -21.7485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(118, 32).addBox(0.6519F, -21.7485F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(122, 32).addBox(1.3186F, -22.0152F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(98, 34).addBox(1.3186F, -22.0152F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        stones.setTextureOffset(102, 34).addBox(-0.3308F, -21.8154F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        stones.setTextureOffset(106, 34).addBox(-0.3308F, -21.8154F, 2.825F, 1.0F, 1.1333F, 1.0F, 0.0F, false);

        torchleft = new ModelRenderer(this);
        torchleft.setRotationPoint(0.0F, 0.0F, 0.0F);
        backpack.addChild(torchleft);
        torchleft.setTextureOffset(80, 25).addBox(3.1167F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        torchleft.setTextureOffset(88, 25).addBox(3.25F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        torchleft.setTextureOffset(88, 23).addBox(2.9833F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);

        pack = new ModelRenderer(this);
        pack.setRotationPoint(0.0F, 0.0F, 0.0F);
        backpack.addChild(pack);
        pack.setTextureOffset(64, 16).addBox(-3.0F, -21.9333F, 5.0F, 6.0F, 7.4F, 1.0F, 0.0F, false);
        pack.setTextureOffset(79, 17).addBox(-3.0F, -14.5333F, 2.0F, 6.3333F, 1.0F, 3.0F, 0.0F, false);
        pack.setTextureOffset(98, 16).addBox(3.0F, -21.2667F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        pack.setTextureOffset(107, 16).addBox(-4.0F, -21.2667F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        pack.setTextureOffset(79, 22).addBox(2.9833F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        pack.setTextureOffset(83, 22).addBox(-4.0167F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);

        torchright = new ModelRenderer(this);
        torchright.setRotationPoint(0.0F, 0.0F, 0.0F);
        backpack.addChild(torchright);
        torchright.setTextureOffset(93, 22).addBox(-4.0167F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        torchright.setTextureOffset(84, 25).addBox(-3.9F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        torchright.setTextureOffset(91, 25).addBox(-3.7083F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
		
		bipedHeadwear.showModel = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
