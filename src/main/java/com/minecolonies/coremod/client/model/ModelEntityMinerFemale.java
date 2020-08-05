// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMinerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMinerFemale()
    {
        ModelRenderer headDetail;
        ModelRenderer googles;
        ModelRenderer hair;
        ModelRenderer helmet;
        ModelRenderer ponytail;
        ModelRenderer ponytail2;
        ModelRenderer belt;
        ModelRenderer backpack;
        ModelRenderer stones;
        ModelRenderer torchleft;
        ModelRenderer pack;
        ModelRenderer torchright;
        ModelRenderer chest;
        ModelRenderer gloveright;
        ModelRenderer gloveleft;

        textureWidth = 128;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        headDetail = new ModelRenderer(this);
        headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
        bipedHead.addChild(headDetail);
        headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        googles = new ModelRenderer(this);
        googles.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(googles);
        googles.setTextureOffset(34, 32).addBox(-4.3333F, -28.3333F, -4.6667F, 2.2F, 0.8667F, 3.5333F, 0.0F, false);
        googles.setTextureOffset(33, 37).addBox(0.8667F, -28.3333F, -4.6667F, 3.5333F, 1.0F, 3.5333F, 0.0F, false);
        googles.setTextureOffset(45, 36).addBox(-1.0F, -28.3333F, -4.6667F, 1.9333F, 1.0F, 2.3333F, 0.0F, false);
        googles.setTextureOffset(46, 40).addBox(-3.0F, -28.4667F, -4.9333F, 2.0667F, 1.8F, 2.3333F, 0.0F, false);
        googles.setTextureOffset(36, 42).addBox(1.0F, -28.4667F, -4.9333F, 2.0667F, 1.8F, 2.3333F, 0.0F, false);
        googles.setTextureOffset(32, 33).addBox(1.2667F, -28.2F, -5.2F, 1.4F, 1.1333F, 0.4F, 0.0F, false);
        googles.setTextureOffset(43, 33).addBox(-2.7333F, -28.2F, -5.2F, 1.4F, 1.1333F, 0.4F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedHead.addChild(hair);
        hair.setTextureOffset(0, 33).addBox(-4.4667F, -28.0667F, 0.2667F, 1.0F, 1.0F, 3.6667F, 0.0F, false);
        hair.setTextureOffset(0, 38).addBox(-4.4667F, -29.0F, -1.0667F, 1.0F, 1.0F, 5.0F, 0.0F, false);
        hair.setTextureOffset(0, 45).addBox(-4.4667F, -30.8667F, -4.1333F, 1.1333F, 1.8F, 8.0667F, 0.0F, false);
        hair.setTextureOffset(11, 54).addBox(3.4F, -30.8667F, -4.1333F, 1.1F, 1.8F, 8.0667F, 0.0F, false);
        hair.setTextureOffset(10, 33).addBox(-3.2667F, -30.8667F, -4.2F, 6.4333F, 1.8F, 3.4F, 0.0F, false);
        hair.setTextureOffset(13, 39).addBox(-3.9333F, -30.8667F, 0.7333F, 7.9F, 3.8F, 3.4F, 0.0F, false);
        hair.setTextureOffset(11, 47).addBox(-3.2667F, -27.1334F, 0.7333F, 6.4333F, 1.4F, 3.4F, 0.0F, false);
        hair.setTextureOffset(1, 56).addBox(-2.3333F, -25.8F, 0.7333F, 4.4333F, 1.0F, 3.4F, 0.0F, false);
        hair.setTextureOffset(27, 48).addBox(3.5333F, -29.1333F, -1.0667F, 1.0F, 1.4F, 5.0F, 0.0F, false);
        hair.setTextureOffset(22, 56).addBox(3.5333F, -27.8F, 0.2667F, 1.0F, 0.8667F, 3.6667F, 0.0F, false);

        helmet = new ModelRenderer(this);
        helmet.setRotationPoint(2.2667F, -9.8667F, 0.0F);
        bipedHead.addChild(helmet);
        setRotationAngle(helmet, -0.1745F, 0.0F, 0.0F);
        helmet.setTextureOffset(55, 36).addBox(-3.2333F, 1.4047F, -3.9653F, 1.9333F, 1.7667F, 0.6F, 0.0F, false);
        helmet.setTextureOffset(52, 36).addBox(-6.8667F, 1.938F, -3.6986F, 4.6F, 1.8F, 9.2667F, 0.0F, false);
        helmet.setTextureOffset(71, 33).addBox(-2.3F, 1.938F, -3.6986F, 4.6F, 1.8F, 9.2667F, 0.0F, false);
        helmet.setTextureOffset(31, 47).addBox(-5.1F, 0.838F, -3.6986F, 5.6667F, 1.1333F, 9.2667F, 0.0F, false);
        helmet.setTextureOffset(54, 55).addBox(-6.4333F, 0.8713F, -3.032F, 8.3333F, 1.1F, 7.9333F, 0.0F, false);
        helmet.setTextureOffset(80, 53).addBox(-5.2333F, -0.1953F, -3.032F, 5.9333F, 1.1F, 7.9333F, 0.0F, false);

        ponytail = new ModelRenderer(this);
        ponytail.setRotationPoint(3.3333F, -4.0F, 4.9333F);
        bipedHead.addChild(ponytail);
        setRotationAngle(ponytail, 0.4364F, 0.0F, 0.0F);
        ponytail.setTextureOffset(66, 0).addBox(-4.0667F, -1.5334F, -0.6287F, 0.9667F, 5.2667F, 0.8667F, 0.0F, false);

        ponytail2 = new ModelRenderer(this);
        ponytail2.setRotationPoint(-3.5833F, 5.9396F, -0.9715F);
        ponytail.addChild(ponytail2);
        setRotationAngle(ponytail2, -0.3491F, 0.0F, 0.0F);
        ponytail2.setTextureOffset(66, 7).addBox(-0.35F, -2.5F, -0.3667F, 0.7F, 5.0F, 0.7334F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        belt = new ModelRenderer(this);
        belt.setRotationPoint(0.0F, 24.0F, 0.0F);
        bipedBody.addChild(belt);
        belt.setTextureOffset(100, 37).addBox(-4.3333F, -14.6F, -2.2667F, 9.0F, 1.0F, 4.6F, 0.0F, false);
        belt.setTextureOffset(101, 44).addBox(-0.8667F, -15.0F, -2.5333F, 2.2F, 1.9333F, 0.6F, 0.0F, false);

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
        pack.setTextureOffset(84, 22).addBox(-4.0167F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);

        torchright = new ModelRenderer(this);
        torchright.setRotationPoint(0.0F, 0.0F, 0.0F);
        backpack.addChild(torchright);
        torchright.setTextureOffset(93, 22).addBox(-4.0167F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        torchright.setTextureOffset(84, 25).addBox(-3.9F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        torchright.setTextureOffset(91, 25).addBox(-3.7083F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);

        chest = new ModelRenderer(this);
        chest.setRotationPoint(3.2F, 1.7333F, -4.9333F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, -0.7854F, 0.0F, 0.0F);
        chest.setTextureOffset(53, 29).addBox(-6.1333F, -2.6114F, 1.5219F, 6.0F, 2.8F, 3.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

        gloveright = new ModelRenderer(this);
        gloveright.setRotationPoint(5.0F, 22.0F, 0.0F);
        bipedRightArm.addChild(gloveright);
        gloveright.setTextureOffset(96, 9).addBox(-7.5333F, -16.3333F, -2.6667F, 4.0F, 1.0F, 5.2667F, 0.0F, false);
        gloveright.setTextureOffset(97, 0).addBox(-7.4F, -15.3333F, -2.4F, 3.7333F, 4.0F, 4.7333F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);

        gloveleft = new ModelRenderer(this);
        gloveleft.setRotationPoint(-5.0F, 22.0F, 0.0F);
        bipedLeftArm.addChild(gloveleft);
        gloveleft.setTextureOffset(75, 9).addBox(3.4667F, -16.3333F, -2.6667F, 4.0F, 1.0F, 5.2667F, 0.0F, false);
        gloveleft.setTextureOffset(78, 0).addBox(3.6F, -15.3333F, -2.4F, 3.7333F, 4.0F, 4.7333F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
