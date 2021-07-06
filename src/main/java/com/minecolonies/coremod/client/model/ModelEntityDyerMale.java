package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityDyerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityDyerMale()
    {
        texWidth = 128;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(58, 0).addBox(-4.4667F, -6.8667F, 0.0F, 0.4667F, 4.3333F, 4.3333F, 0.0F, false);
        head.texOffs(58, 0).addBox(3.9333F, -6.8667F, 0.0F, 0.4667F, 4.3333F, 4.3333F, 0.0F, false);
        head.texOffs(58, 0).addBox(-4.0667F, -6.8667F, 4.0F, 8.0667F, 4.3333F, 0.3F, 0.0F, false);
        head.texOffs(59, 5).addBox(-4.0667F, -7.6667F, 4.0F, 8.0667F, 0.8667F, 0.4667F, 0.0F, false);
        head.texOffs(59, 5).addBox(3.9333F, -7.4F, 3.6F, 0.4667F, 0.6F, 0.7333F, 0.0F, false);
        head.texOffs(59, 5).addBox(-4.3333F, -7.4F, 3.6F, 0.4667F, 0.6F, 0.7333F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.6F, -6.8667F, 3.6F, 0.7333F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.8F, -6.8667F, 3.6F, 0.7333F, 1.0F, 1.0F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.5333F, -6.0667F, 3.2F, 1.1333F, 1.2667F, 1.5333F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.7333F, -6.0667F, 3.2F, 1.1333F, 1.2667F, 1.5333F, 0.0F, false);
        head.texOffs(60, 8).addBox(-5.0F, -5.5333F, 3.2F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(2.8667F, -5.5333F, 3.2F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(1.9333F, -6.7333F, 3.0667F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.2F, -6.7333F, 3.0667F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-3.9333F, -7.4F, 2.8F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(1.6667F, -7.4F, 2.8F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-0.0667F, -7.5333F, 2.9333F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-2.0667F, -7.6F, 3.0F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-3.0F, -6.1333F, 3.1333F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(0.8667F, -6.1333F, 3.1333F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-1.0F, -6.1333F, 3.1F, 2.0667F, 1.6667F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(1.1333F, -4.5333F, 3.1F, 2.0667F, 1.9333F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-3.4F, -4.5333F, 3.1F, 2.0667F, 1.9333F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-1.4F, -4.5333F, 3.1F, 1.4F, 1.2667F, 1.9333F, 0.0F, false);
        head.texOffs(60, 8).addBox(-0.0333F, -4.5267F, 3.11F, 1.4F, 1.2667F, 1.9333F, 0.0F, false);
        head.texOffs(60, 8).addBox(-0.0333F, -3.3267F, 3.11F, 1.4F, 0.6F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-1.3666F, -3.3267F, 3.12F, 1.4F, 0.6F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.5666F, -4.1267F, 3.12F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.1667F, -4.1267F, 3.12F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.2F, -4.5267F, 1.7867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.8F, -4.5267F, 1.7867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.6667F, -5.86F, 1.7867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.0667F, -5.86F, 1.7867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.1F, -6.7933F, 2.1867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.6333F, -6.7933F, 2.1867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.3667F, -7.1933F, 2.1867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(2.9667F, -7.1933F, 2.1867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(2.9667F, -7.06F, 0.4533F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.3667F, -7.06F, 0.4533F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.55F, -6.5267F, 0.4533F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.05F, -6.5267F, 0.4533F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.1F, -5.1933F, 0.4533F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.6333F, -5.1933F, 0.4533F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.7F, -3.86F, 1.3867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.1667F, -3.86F, 1.3867F, 1.4F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(60, 8).addBox(3.1667F, -3.86F, -0.2133F, 1.4F, 1.6667F, 1.6667F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.5667F, -3.86F, -0.2133F, 1.4F, 1.6667F, 1.6667F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.5F, -4.3933F, -0.7467F, 1.4F, 1.6667F, 1.2667F, 0.0F, false);
        head.texOffs(60, 8).addBox(2.9667F, -4.3933F, -0.7467F, 1.4F, 1.6667F, 1.2667F, 0.0F, false);
        head.texOffs(60, 8).addBox(2.9667F, -6.5267F, -0.6133F, 1.4F, 2.2F, 1.2667F, 0.0F, false);
        head.texOffs(60, 8).addBox(-4.3667F, -6.5267F, -0.6133F, 1.4F, 2.2F, 1.2667F, 0.0F, false);
        head.texOffs(75, 15).addBox(-3.5667F, -5.9933F, -4.2133F, 2.6F, 0.8667F, 0.8667F, 0.0F, false);
        head.texOffs(75, 15).addBox(0.9667F, -5.9933F, -4.2133F, 2.6F, 0.8667F, 0.8667F, 0.0F, false);
        head.texOffs(75, 15).addBox(-2.9F, -1.9933F, -4.2133F, 5.8F, 1.0F, 0.8667F, 0.0F, false);
        head.texOffs(75, 15).addBox(-2.1F, -2.9267F, -4.2F, 1.1333F, 1.0F, 0.8667F, 0.0F, false);
        head.texOffs(75, 15).addBox(0.9667F, -2.9267F, -4.2F, 1.1333F, 1.0F, 0.8667F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        hat.visible = false;
    }
}
