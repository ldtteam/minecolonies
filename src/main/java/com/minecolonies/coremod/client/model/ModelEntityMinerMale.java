package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityMinerMale extends CitizenModel<AbstractEntityCitizen>
{
    //fields

    public ModelEntityMinerMale()
    {
        textureWidth = 128;
        textureHeight = 64;

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, -0.4F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -7.6F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-2.5667F, -7.4667F, -4.5F, 4.0667F, 1.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(2.5F, -4.5F, -3.5F, 2.0F, 1.0F, 7.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-4.5F, -7.5333F, -4.5F, 2.0F, 3.0667F, 9.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-4.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);
        bipedHead.setTextureOffset(3, 45).addBox(2.5F, -3.5F, 0.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(3.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(3.5F, -5.5F, -3.5667F, 1.0F, 1.0F, 7.3333F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-4.5F, -4.5F, 0.4333F, 2.0F, 1.0F, 4.0667F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(3.5F, -6.5F, -3.5F, 1.0F, 1.0F, 7.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(3.5F, -7.5F, -4.5F, 1.0F, 1.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(3.5F, -6.5F, -4.5F, 1.0F, 4.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-4.5F, -4.5F, -4.5F, 1.0F, 2.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(1.5F, -7.5F, -4.5F, 2.0F, 2.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(5, 49).addBox(-4.5F, -3.5F, 0.5F, 2.0F, 3.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-2.5333F, -6.5F, -4.5F, 1.0667F, 1.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-4.5F, -4.5F, -3.5F, 2.0F, 2.0F, 4.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 45).addBox(-3.5F, -0.5F, 3.5F, 7.0F, 3.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(5, 49).addBox(-2.5F, -7.5F, 3.5F, 7.0F, 7.0F, 1.0F, 0.0F, true);
        bipedHead.setTextureOffset(29, 46).addBox(-0.6333F, -8.05F, -4.9F, 1.1333F, 1.9333F, 9.6667F, 0.0F, true);
        bipedHead.setTextureOffset(29, 46).addBox(-1.0333F, -6.1167F, 0.4333F, 1.9333F, 0.7333F, 4.3333F, 0.0F, true);
        bipedHead.setTextureOffset(29, 46).addBox(0.9133F, -6.1167F, -4.9F, 3.4F, 0.7333F, 9.6667F, 0.0F, true);
        bipedHead.setTextureOffset(29, 46).addBox(-4.42F, -6.1167F, -4.9F, 3.4F, 0.7333F, 9.6667F, 0.0F, true);
        bipedHead.setTextureOffset(29, 46).addBox(-5.3F, -6.25F, -5.0333F, 1.1333F, 1.0F, 9.9333F, 0.0F, true);
        bipedHead.setTextureOffset(29, 46).addBox(4.0333F, -6.25F, -5.0333F, 1.2667F, 1.0F, 9.8F, 0.0F, true);
        bipedHead.setTextureOffset(53, 50).addBox(-1.5667F, -6.5667F, -5.0333F, 3.1333F, 1.4F, 0.7333F, 0.0F, true);
        bipedHead.setTextureOffset(66, 53).addBox(-1.3F, -6.4333F, -5.4333F, 2.7333F, 1.0F, 0.6F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedLeftArm.setTextureOffset(66, 8).addBox(-1.4F, 6.6667F, -2.4F, 4.7333F, 4.0F, 4.7333F, 0.0F, false);
        bipedLeftArm.setTextureOffset(66, 8).addBox(-1.5333F, 5.6667F, -2.6667F, 5.0F, 1.0F, 5.2667F, 0.0F, false);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedRightArm.setTextureOffset(66, 8).addBox(-3.4F, 6.6667F, -2.4F, 4.7333F, 4.0F, 4.7333F, 0.0F, false);
        bipedRightArm.setTextureOffset(66, 8).addBox(-3.5333F, 5.6667F, -2.6667F, 5.0F, 1.0F, 5.2667F, 0.0F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);
        bipedBody.setTextureOffset(66, 3).addBox(-1.3481F, 1.9848F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-1.3481F, 1.9848F, 2.2418F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-2.0148F, 2.2515F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-2.0148F, 2.2515F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-1.3481F, 1.8515F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.2814F, 1.8515F, 3.5752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 2.3848F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 2.3848F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 1.9848F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(-0.6814F, 1.9848F, 1.8667F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(-2.4514F, 2.1047F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(-2.4514F, 2.1047F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-2.9975F, 2.4513F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-2.8642F, 2.3179F, 2.025F, 0.8667F, 1.2667F, 1.1333F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-2.9975F, 2.1846F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 2.4513F, 3.0916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 2.4513F, 2.025F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(0.2153F, 2.1047F, 2.93F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 2).addBox(0.2153F, 2.1047F, 1.8634F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.9852F, 2.1181F, 2.9333F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.9852F, 2.3848F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.9852F, 2.3848F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.3186F, 1.8515F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(0.7852F, 1.8515F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(0.6519F, 2.2515F, 3.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(0.6519F, 2.2515F, 2.9085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.3186F, 1.9848F, 3.3085F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(66, 3).addBox(1.3186F, 1.9848F, 1.9752F, 1.0F, 1.0F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 2.1846F, 3.8916F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(68, 3).addBox(-0.3308F, 2.1846F, 2.825F, 1.0F, 1.1333F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 46).addBox(-3.0F, 2.0667F, 5.0F, 6.0F, 7.4F, 1.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 46).addBox(-3.0F, 9.4667F, 2.0F, 6.3333F, 1.0F, 3.0F, 0.0F, false);
        bipedBody.setTextureOffset(78, 45).addBox(3.0F, 2.7333F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        bipedBody.setTextureOffset(77, 45).addBox(-4.0F, 2.7333F, 2.0F, 1.0F, 6.7333F, 3.0F, 0.0F, false);
        bipedBody.setTextureOffset(70, 30).addBox(3.1167F, 1.3833F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        bipedBody.setTextureOffset(102, 36).addBox(3.25F, 2.5833F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        bipedBody.setTextureOffset(101, 32).addBox(2.9833F, 2.0633F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(81, 45).addBox(2.9833F, 3.3967F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(101, 32).addBox(-4.0167F, 2.0633F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(81, 45).addBox(-4.0167F, 3.3967F, 5.0F, 1.0167F, 0.6167F, 1.0167F, 0.0F, false);
        bipedBody.setTextureOffset(70, 30).addBox(-3.9F, 1.3833F, 5.15F, 0.75F, 0.75F, 0.75F, 0.0F, false);
        bipedBody.setTextureOffset(102, 36).addBox(-3.7083F, 2.5833F, 5.2833F, 0.4833F, 3.55F, 0.4833F, 0.0F, false);
        bipedHeadwear.showModel = false;
    }
}
