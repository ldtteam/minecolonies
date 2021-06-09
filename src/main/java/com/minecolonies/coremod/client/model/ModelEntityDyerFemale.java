package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityDyerFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityDyerFemale()
    {
        texWidth = 128;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.3333F, -5.0F, 0.0F, 1.0F, 1.0F, 4.0667F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.2667F, -5.0F, 0.0F, 1.0F, 1.0F, 4.0667F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.4F, -5.8F, 0.0F, 1.0F, 1.0F, 4.0667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.6F, -5.8F, 0.0F, 1.0F, 1.0F, 4.0667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.7333F, -6.7333F, 2.1333F, 1.1333F, 1.0F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.4F, -6.7333F, 2.1333F, 1.1333F, 1.0F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.3667F, -6.7333F, 0.4F, 1.1333F, 1.0F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.6333F, -6.7333F, 0.4F, 1.1333F, 1.0F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.5F, -6.7333F, -1.55F, 1.0F, 2.7333F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.3667F, -6.7333F, -1.55F, 1.0F, 2.7333F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.3667F, -8.2F, -4.0833F, 8.7333F, 1.5333F, 8.3333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.2333F, -7.2667F, -4.2167F, 1.2667F, 2.0667F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.2433F, -8.4667F, -3.2833F, 1.2667F, 2.0667F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.2433F, -8.7333F, -2.35F, 1.2667F, 1.5333F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.3767F, -7.5333F, -2.75F, 1.2667F, 1.5333F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.51F, -6.7333F, -3.55F, 1.2667F, 1.5333F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.1333F, -8.3333F, -4.26F, 1.2667F, 1.5333F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.2667F, -7.2667F, -2.3933F, 1.2667F, 1.5333F, 1.0F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.2667F, -6.0667F, -2.9267F, 1.4F, 2.2F, 1.5333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.18F, -6.7333F, -1.46F, 1.4F, 2.2F, 1.5333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.08F, -8.6F, -1.46F, 1.4F, 3.2667F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(3.08F, -8.7333F, -0.1267F, 1.5333F, 1.9333F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.9867F, -8.4667F, 1.43F, 1.5333F, 1.9333F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.9867F, -8.6F, 2.8967F, 1.6667F, 2.2F, 1.5333F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.4533F, -4.6F, 2.7633F, 1.6667F, 2.2F, 1.5333F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.72F, -4.6F, -1.77F, 1.6667F, 1.8F, 1.5333F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.5333F, -4.2F, -0.3033F, 1.6667F, 1.4F, 3.4F, 0.0F, false);
        head.texOffs(62, 1).addBox(0.9667F, -4.4667F, 2.6F, 2.3333F, 2.0667F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(1.6333F, -7.5333F, 2.51F, 2.3333F, 3.5333F, 1.9333F, 0.0F, false);
        head.texOffs(62, 1).addBox(0.3F, -6.2F, 2.51F, 2.3333F, 2.2F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(0.9667F, -8.4667F, 2.3767F, 2.3333F, 2.6F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(-0.5F, -8.6F, 2.52F, 2.3333F, 2.6F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(-3.0333F, -2.8667F, 2.12F, 5.9333F, 2.7333F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.6333F, -7.1333F, 2.12F, 2.8667F, 3.1333F, 2.4667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-2.5F, -8.6F, 0.6533F, 2.8667F, 4.2F, 3.8F, 0.0F, false);
        head.texOffs(62, 1).addBox(-2.9F, -8.7333F, 2.0F, 2.8667F, 2.4667F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.5F, -8.6F, 1.9867F, 2.3333F, 2.3333F, 2.4667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.5F, -4.6F, 2.125F, 2.3333F, 2.6F, 2.4667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.3667F, -4.6F, -0.1417F, 2.3333F, 2.6F, 2.4667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.6133F, -4.8667F, 1.325F, 2.3333F, 1.8F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.7667F, -7.2667F, -2.5417F, 2.3333F, 1.8F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.5833F, -8.3333F, 0.3917F, 2.3333F, 1.8F, 2.2F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.6333F, -8.4667F, -0.8083F, 2.3333F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.6833F, -8.6F, -2.4F, 2.3333F, 1.4F, 1.8F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.7033F, -8.4667F, -4.1333F, 2.3333F, 2.3333F, 2.0667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.7033F, -6.7333F, -2.4F, 2.3333F, 2.3333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-4.4367F, -4.7333F, -1.0667F, 2.0667F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-2.7033F, -8.7333F, -1.0667F, 2.0667F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(0.0967F, -8.7333F, 0.0F, 2.0667F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(1.83F, -8.5333F, 0.6667F, 2.0667F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(1.83F, -8.6833F, -1.4667F, 2.0667F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-0.57F, -8.6333F, -1.4667F, 2.3333F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(1.2967F, -8.5933F, -3.7333F, 2.3333F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-2.7033F, -8.5933F, -3.6F, 2.3333F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-0.7033F, -8.46F, -3.6F, 2.3333F, 1.9333F, 2.6F, 0.0F, false);
        head.texOffs(62, 1).addBox(-0.17F, -8.46F, -4.5333F, 1.8F, 1.9333F, 0.8667F, 0.0F, false);
        head.texOffs(62, 1).addBox(1.43F, -8.46F, -4.4F, 1.8F, 1.9333F, 0.8667F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.23F, -7.7933F, -4.35F, 1.8F, 1.9333F, 0.8667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-3.1033F, -7.7933F, -4.35F, 1.8F, 1.9333F, 0.8667F, 0.0F, false);
        head.texOffs(62, 1).addBox(-1.77F, -8.3267F, -4.38F, 1.8F, 1.9333F, 0.8667F, 0.0F, false);
        head.texOffs(62, 1).addBox(2.4333F, -3.1333F, -1.1033F, 1.6667F, 1.4F, 5.5333F, 0.0F, false);
        head.texOffs(62, 1).addBox(-3.0333F, -4.6F, 2.255F, 5.9333F, 2.7333F, 2.2F, 0.0F, false);

        ModelRenderer fronttail2 = new ModelRenderer(this);
        fronttail2.setPos(2.8F, -6.2667F, -9.3333F);
        head.addChild(fronttail2);
        setRotationAngle(fronttail2, 0.0F, 0.0F, -0.1745F);
        fronttail2.texOffs(97, 40).addBox(-6.5656F, -0.7096F, 4.8233F, 3.5333F, 2.3333F, 0.8667F, 0.0F, false);

        ModelRenderer fronttail = new ModelRenderer(this);
        fronttail.setPos(2.8F, -6.2667F, -9.2F);
        head.addChild(fronttail);
        setRotationAngle(fronttail, 0.0F, 0.0F, 0.5236F);
        fronttail.texOffs(97, 40).addBox(-3.0772F, 0.259F, 4.6867F, 1.8F, 2.8667F, 0.8667F, 0.0F, false);
        fronttail.texOffs(97, 40).addBox(-4.7735F, 1.8542F, 4.66F, 1.8F, 2.2F, 0.8667F, 0.0F, false);
        fronttail.texOffs(97, 40).addBox(-5.9282F, 2.5209F, 4.68F, 1.8F, 2.8667F, 0.8667F, 0.0F, false);
        fronttail.texOffs(97, 40).addBox(-4.6319F, 0.2328F, 4.68F, 1.8F, 1.9333F, 0.8667F, 0.0F, false);
        fronttail.texOffs(97, 40).addBox(-4.6188F, -0.3136F, 4.7F, 1.9333F, 1.9333F, 0.8667F, 0.0F, false);
        fronttail.texOffs(97, 40).addBox(-5.8711F, 0.7174F, 4.7F, 1.9333F, 1.9333F, 0.8667F, 0.0F, false);

        ModelRenderer ponytail1 = new ModelRenderer(this);
        ponytail1.setPos(2.8F, -0.9333F, 1.4667F);
        head.addChild(ponytail1);
        setRotationAngle(ponytail1, 0.6109F, 0.0F, 0.0F);
        ponytail1.texOffs(99, 17).addBox(-4.3033F, -2.7267F, 4.5533F, 2.2F, 5.5333F, 1.4F, 0.0F, false);

        ModelRenderer ponytail2 = new ModelRenderer(this);
        ponytail2.setPos(2.8F, -0.5333F, 1.4667F);
        head.addChild(ponytail2);
        setRotationAngle(ponytail2, -0.1745F, 0.0F, 0.0F);
        ponytail2.texOffs(101, 18).addBox(-4.17F, -2.773F, 5.0826F, 1.8F, 5.2667F, 0.8667F, 0.0F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

        ModelRenderer chest = new ModelRenderer(this);
        chest.setPos(3.0667F, 8.8F, -1.7333F);
        body.addChild(chest);
        setRotationAngle(chest, 0.7854F, 0.0F, 0.0F);
        chest.texOffs(18, 21).addBox(-6.0F, -5.2627F, 2.2627F, 6.0F, 3.0F, 3.0F, 0.0F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLeg.texOffs(0, 16).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        hat.visible = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
