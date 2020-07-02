package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMinerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMinerFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(28, 57).addBox(-4.3333F, -4.3333F, -4.6667F, 2.2F, 0.8667F, 3.5333F, 0.0F, false);
        bipedHead.setTextureOffset(26, 55).addBox(0.8667F, -4.3333F, -4.6667F, 3.5333F, 1.0F, 3.5333F, 0.0F, false);
        bipedHead.setTextureOffset(26, 55).addBox(-1.0F, -4.3333F, -4.6667F, 1.9333F, 1.0F, 2.3333F, 0.0F, false);
        bipedHead.setTextureOffset(26, 55).addBox(-3.0F, -4.4667F, -4.9333F, 2.0667F, 1.8F, 2.3333F, 0.0F, false);
        bipedHead.setTextureOffset(26, 55).addBox(1.0F, -4.4667F, -4.9333F, 2.0667F, 1.8F, 2.3333F, 0.0F, false);
        bipedHead.setTextureOffset(15, 39).addBox(1.2667F, -4.2F, -5.2F, 1.4F, 1.1333F, 0.4F, 0.0F, false);
        bipedHead.setTextureOffset(15, 39).addBox(-2.7333F, -4.2F, -5.2F, 1.4F, 1.1333F, 0.4F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-4.4667F, -4.0667F, 0.2667F, 1.0F, 1.0F, 3.6667F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-4.4667F, -5.0F, -1.0667F, 1.0F, 1.0F, 5.0F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-4.4667F, -6.8667F, -4.1333F, 1.1333F, 1.8F, 8.0667F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(3.4F, -6.8667F, -4.1333F, 1.1F, 1.8F, 8.0667F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-3.2667F, -6.8667F, -4.2F, 6.4333F, 1.8F, 3.4F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-3.9333F, -6.8667F, 0.7333F, 7.9F, 3.8F, 3.4F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-3.2667F, -3.1334F, 0.7333F, 6.4333F, 1.4F, 3.4F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-2.3333F, -1.8F, 0.7333F, 4.4333F, 1.0F, 3.4F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(-0.6F, -1.1334F, 6.4379F, 0.7F, 5.0F, 0.7334F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(3.5333F, -5.1333F, -1.0667F, 1.0F, 1.4F, 5.0F, 0.0F, false);
        bipedHead.setTextureOffset(3, 48).addBox(3.5333F, -3.8F, 0.2667F, 1.0F, 0.8667F, 3.6667F, 0.0F, false);

        ModelRenderer helmet = new ModelRenderer(this);
        helmet.setRotationPoint(2.2667F, -9.8667F, 0.0F);
        bipedHead.addChild(helmet);
        setRotationAngle(helmet, -0.1745F, 0.0F, 0.0F);
        helmet.setTextureOffset(42, 61).addBox(-3.2333F, 1.4047F, -3.9653F, 1.9333F, 1.7667F, 0.6F, 0.0F, false);
        helmet.setTextureOffset(50, 45).addBox(-6.8667F, 1.938F, -3.6986F, 4.6F, 1.8F, 9.2667F, 0.0F, false);
        helmet.setTextureOffset(50, 45).addBox(-2.3F, 1.938F, -3.6986F, 4.6F, 1.8F, 9.2667F, 0.0F, false);
        helmet.setTextureOffset(50, 45).addBox(-5.1F, 0.838F, -3.6986F, 5.6667F, 1.1333F, 9.2667F, 0.0F, false);
        helmet.setTextureOffset(50, 45).addBox(-6.4333F, 0.8713F, -3.032F, 8.3333F, 1.1F, 7.9333F, 0.0F, false);
        helmet.setTextureOffset(50, 45).addBox(-5.2333F, -0.1953F, -3.032F, 5.9333F, 1.1F, 7.9333F, 0.0F, false);

        ModelRenderer ponytail = new ModelRenderer(this);
        ponytail.setRotationPoint(3.3333F, -4.0F, 4.9333F);
        bipedHead.addChild(ponytail);
        setRotationAngle(ponytail, 0.6109F, 0.0F, 0.0F);
        ponytail.setTextureOffset(3, 48).addBox(-4.0667F, -1.5334F, -0.6287F, 0.9667F, 5.2667F, 0.8667F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(35, 0).addBox(-4.3333F, 9.4F, -2.2667F, 9.0F, 1.0F, 4.6F, 0.0F, false);
        bipedBody.setTextureOffset(44, 51).addBox(-0.8667F, 9.0F, -2.5333F, 2.2F, 1.9333F, 0.6F, 0.0F, false);

        ModelRenderer backpack = new ModelRenderer(this);
        backpack.setRotationPoint(0.0F, 22.6667F, 0.0F);
        bipedBody.addChild(backpack);
        backpack.setTextureOffset(85, 45).addBox(-3.0F, -21.9333F, 5.0F, 6.0F, 7.4F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(81, 60).addBox(-3.0F, -14.5333F, 2.0F, 5.9333F, 0.4667F, 3.0F, 0.0F, false);
        backpack.setTextureOffset(91, 45).addBox(3.0F, -21.2667F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        backpack.setTextureOffset(91, 45).addBox(-4.0F, -21.2667F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        backpack.setTextureOffset(70, 30).addBox(3.1167F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        backpack.setTextureOffset(102, 36).addBox(3.25F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        backpack.setTextureOffset(101, 32).addBox(2.9833F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        backpack.setTextureOffset(82, 61).addBox(2.9833F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        backpack.setTextureOffset(101, 32).addBox(-4.0167F, -21.9367F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        backpack.setTextureOffset(82, 62).addBox(-4.0167F, -20.6033F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        backpack.setTextureOffset(70, 30).addBox(-3.9F, -22.6167F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        backpack.setTextureOffset(102, 36).addBox(-3.7083F, -21.4167F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-1.3481F, -22.0152F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-1.3481F, -22.0152F, 2.2418F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-2.0148F, -21.7485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-2.0148F, -21.7485F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-1.3481F, -22.1485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-0.2814F, -22.1485F, 3.5752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-0.6814F, -21.6152F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-0.6814F, -21.6152F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-0.6814F, -22.0152F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(-0.6814F, -22.0152F, 1.8667F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 2).addBox(-2.4514F, -21.8953F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 2).addBox(-2.4514F, -21.8953F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-2.9975F, -21.5487F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-2.8642F, -21.6821F, 2.025F, 0.8667F, 1.2667F, 1.1333F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-2.9975F, -21.8154F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-0.3308F, -21.5487F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-0.3308F, -21.5487F, 2.025F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 2).addBox(0.2153F, -21.8953F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 2).addBox(0.2153F, -21.8953F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(1.9852F, -21.8819F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(1.9852F, -21.6152F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(1.9852F, -21.6152F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(1.3186F, -22.1485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(0.7852F, -22.1485F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(0.6519F, -21.7485F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(0.6519F, -21.7485F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(1.3186F, -22.0152F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(66, 3).addBox(1.3186F, -22.0152F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-0.3308F, -21.8154F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        backpack.setTextureOffset(68, 3).addBox(-0.3308F, -21.8154F, 2.825F, 1.0F, 1.1333F, 1.0F, 0.0F, false);

        ModelRenderer chest = new ModelRenderer(this);
        chest.setRotationPoint(3.2F, 1.7333F, -4.9333F);
        bipedBody.addChild(chest);
        setRotationAngle(chest, -0.7854F, 0.0F, 0.0F);
        chest.setTextureOffset(19, 17).addBox(-6.1333F, -2.6114F, 1.5219F, 6.0F, 2.8F, 3.0F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        ModelRenderer rightglove = new ModelRenderer(this);
        rightglove.setRotationPoint(0.0F, 10.4F, 0.0F);
        bipedRightArm.addChild(rightglove);
        rightglove.setTextureOffset(73, 13).addBox(-3.5333F, -4.8667F, -2.2667F, 5.1333F, 0.8667F, 4.6F, 0.0F, false);
        rightglove.setTextureOffset(76, 14).addBox(-3.2667F, -4.2F, -2.1333F, 4.4667F, 4.2F, 4.3333F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        ModelRenderer leftglove = new ModelRenderer(this);
        leftglove.setRotationPoint(2.0F, 9.7333F, 0.0F);
        bipedLeftArm.addChild(leftglove);
        leftglove.setTextureOffset(76, 14).addBox(-3.2667F, -3.5333F, -2.1333F, 4.4667F, 4.2F, 4.3333F, 0.0F, false);
        leftglove.setTextureOffset(68, 16).addBox(-3.5333F, -4.2F, -2.2667F, 5.1333F, 0.8667F, 4.6F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
