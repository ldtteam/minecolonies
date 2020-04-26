package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityMinerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityMinerFemale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(68, 11).addBox(-3.6F, 6.0333F, -2.5333F, 5.2F, 0.5F, 5.1333F, 0.0F, false);
        bipedRightArm.setTextureOffset(68, 11).addBox(-3.2F, 6.5333F, -2.1333F, 4.4F, 4.0F, 4.3333F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedLeftArm.setTextureOffset(68, 11).addBox(-1.2F, 6.5333F, -2.1333F, 4.4F, 4.0F, 4.3333F, 0.0F, false);
        bipedLeftArm.setTextureOffset(68, 11).addBox(-1.6F, 6.0333F, -2.5333F, 5.2F, 0.5F, 5.1333F, 0.0F, false);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        bipedBody.setTextureOffset(44, 52).addBox(-1.0F, 9.0F, -2.7F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        bipedBody.setTextureOffset(0, 46).addBox(-4.5F, 9.5F, -2.5F, 9.0F, 1.0F, 5.0F, 0.0F, true);
        bipedBody.setTextureOffset(78, 46).addBox(-3.0F, 8.1334F, 2.0F, 6.3333F, 1.0F, 3.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 45).addBox(3.0F, 1.4F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        bipedBody.setTextureOffset(77, 45).addBox(-4.0F, 1.4F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        bipedBody.setTextureOffset(70, 30).addBox(3.1167F, 0.05F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        bipedBody.setTextureOffset(102, 36).addBox(3.25F, 1.25F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        bipedBody.setTextureOffset(101, 32).addBox(2.9833F, 0.73F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(81, 45).addBox(2.9833F, 2.0634F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(101, 32).addBox(-4.0167F, 0.73F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(81, 45).addBox(-4.0167F, 2.0634F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(70, 30).addBox(-3.9F, 0.05F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        bipedBody.setTextureOffset(102, 36).addBox(-3.7083F, 1.25F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-1.3481F, 0.6515F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-1.3481F, 0.6515F, 2.2418F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-2.0148F, 0.9182F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-2.0148F, 0.9182F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-1.3481F, 0.5182F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.2814F, 0.5182F, 3.5752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 1.0515F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 1.0515F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 0.6515F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 0.6515F, 1.8667F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(-2.4514F, 0.7714F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(-2.4514F, 0.7714F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-2.9975F, 1.118F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-2.8642F, 0.9846F, 2.025F, 0.8667F, 1.2667F, 1.1333F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-2.9975F, 0.8513F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 1.118F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 1.118F, 2.025F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(0.2153F, 0.7714F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(0.2153F, 0.7714F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.9852F, 0.7848F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.9852F, 1.0515F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.9852F, 1.0515F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.3186F, 0.5182F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(0.7852F, 0.5182F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(0.6519F, 0.9182F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(0.6519F, 0.9182F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.3186F, 0.6515F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.3186F, 0.6515F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 0.8513F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 0.8513F, 2.825F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 46).addBox(-3.0F, 0.7334F, 5.0F, 6.0F, 7.4F, 1.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(52, 50).addBox(3.6667F, -5.0F, -1.3333F, 1.0F, 1.0F, 2.3333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(3.6667F, -5.0F, 0.9333F, 1.0F, 2.0667F, 3.1333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(3.1333F, -7.2F, -4.0F, 1.5333F, 2.2F, 8.0667F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-4.61F, -7.2F, -4.1333F, 1.5333F, 2.2F, 8.2F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-4.6F, -5.0667F, -1.0667F, 1.0F, 1.0F, 5.1333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-4.61F, -4.1333F, 0.9333F, 1.0F, 1.1333F, 3.1333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-4.4767F, -5.4667F, 3.6F, 9.0F, 2.4667F, 1.1333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-2.8767F, -3.0667F, 3.61F, 5.8F, 1.0F, 1.1333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-2.21F, -2.1333F, 3.6F, 4.3333F, 1.0F, 1.1333F, 0.0F, false);
        bipedHead.setTextureOffset(52, 50).addBox(-3.01F, -6.1333F, -4.2667F, 6.2F, 1.0F, 1.2667F, 0.0F, false);
        bipedHead.setTextureOffset(0, 57).addBox(-5.1667F, -8.3893F, -2.8607F, 10.7333F, 1.0F, 6.9333F, 0.0F, true);
        bipedHead.setTextureOffset(0, 54).addBox(-3.6667F, -8.3893F, -4.7274F, 7.7333F, 1.0F, 9.9333F, 0.0F, true);
        bipedHead.setTextureOffset(0, 54).addBox(-5.1333F, -5.4F, -0.1941F, 10.6667F, 1.0F, 5.4F, 0.0F, true);
        bipedHead.setTextureOffset(0, 53).addBox(-5.1667F, -7.3641F, -4.7681F, 10.7333F, 2.0F, 9.9333F, 0.0F, true);
        bipedHead.setTextureOffset(0, 57).addBox(-3.6667F, -9.3333F, -3.8404F, 7.7333F, 1.0F, 7.8667F, 0.0F, true);
        bipedHead.setTextureOffset(40, 60).addBox(-1.6667F, -7.0803F, -5.2598F, 3.7333F, 1.6F, 2.1333F, 0.0F, true);
        bipedHead.setTextureOffset(15, 39).addBox(1.3F, -4.6333F, -4.6F, 1.4F, 1.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(7, 39).addBox(-2.7F, -4.6333F, -4.6F, 1.4F, 1.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 39).addBox(-3.0F, -5.1333F, -4.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 33).addBox(-4.5F, -5.0333F, -4.35F, 9.0F, 1.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(20, 39).addBox(1.0F, -5.1333F, -4.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);

        ModelRenderer ponytailT = new ModelRenderer(this);
        ponytailT.setRotationPoint(0.0F, 1.2F, 0.9333F);
        setRotationAngle(ponytailT, 0.4363F, 0.0F, 0.0F);
        ponytailT.setTextureOffset(52, 46).addBox(-1.0F, -2.905F, 4.6158F, 2.0F, 5.0F, 1.0F, 0.0F, true);
        bipedHead.addChild(ponytailT);

        ModelRenderer ponytailb = new ModelRenderer(this);
        ponytailb.setRotationPoint(0.0F, 0.0F, 0.0F);
        setRotationAngle(ponytailb, 0.1745F, 0.0F, 0.0F);
        ponytailb.setTextureOffset(55, 47).addBox(-0.5F, 1.7538F, 5.6879F, 1.0F, 4.6F, 1.0F, 0.0F, true);
        bipedHead.addChild(ponytailb);

        bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }
}
