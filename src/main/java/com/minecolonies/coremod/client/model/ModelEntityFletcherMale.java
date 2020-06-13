package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFletcherMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFletcherMale()
    {
        final ModelRenderer knifeblade;

        textureWidth = 128;
        textureHeight = 64;

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(73, 17).addBox(-4.25F, 10.0F, -3.0F, 8.5F, 1.0F, 5.25F, 0.0F, true);
        bipedBody.setTextureOffset(10, 39).addBox(-2.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        bipedBody.setTextureOffset(10, 39).addBox(-0.5F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, true);
        bipedBody.setTextureOffset(10, 39).addBox(2.0F, 8.5F, -2.75F, 0.5F, 2.5F, 0.5F, 0.0F, false);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

        knifeblade = new ModelRenderer(this);
        knifeblade.setRotationPoint(2.5F, 26.0F, 0.5F);
        bipedBody.addChild(knifeblade);
        knifeblade.setTextureOffset(0, 32).addBox(-5.0F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(0, 32).addBox(-3.25F, -15.5F, -3.25F, 0.5F, 3.25F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(0, 32).addBox(-0.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(0, 32).addBox(-4.75F, -15.5F, -3.25F, 0.25F, 2.0F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(0, 32).addBox(-2.75F, -15.5F, -3.25F, 0.5F, 3.0F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(0, 32).addBox(-0.5F, -15.5F, -3.25F, 0.25F, 2.5F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(0, 32).addBox(-4.5F, -15.5F, -3.25F, 0.25F, 1.5F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(0, 32).addBox(-2.25F, -15.5F, -3.25F, 0.25F, 2.75F, 0.5F, 0.0F, true);
        knifeblade.setTextureOffset(0, 32).addBox(-0.75F, -15.5F, -3.25F, 0.25F, 2.25F, 0.5F, 0.0F, false);
        knifeblade.setTextureOffset(0, 32).addBox(-1.0F, -15.5F, -3.25F, 0.25F, 1.75F, 0.5F, 0.0F, false);

        bipedLeftArm = new ModelRenderer(this);
        bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
        bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        bipedLeftArm.setTextureOffset(73, 17).addBox(-1.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        bipedRightLeg = new ModelRenderer(this);
        bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
        bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedLeftLeg = new ModelRenderer(this);
        bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
        bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

        bipedRightArm = new ModelRenderer(this);
        bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
        bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
        bipedRightArm.setTextureOffset(72, 16).addBox(-3.25F, 7.0F, -2.25F, 4.5F, 1.0F, 4.5F, 0.0F, true);

        bipedHead = new ModelRenderer(this);
        bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -8.0F, -1.5F, 0.25F, 5.25F, 5.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 46).addBox(4.0F, -8.0F, -1.5F, 0.25F, 5.25F, 5.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -8.0F, -3.0F, 0.25F, 4.25F, 1.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(4.0F, -8.0F, -3.0F, 0.25F, 4.5F, 1.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -8.0F, -4.25F, 0.25F, 3.0F, 1.25F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(4.0F, -8.0F, -4.25F, 0.25F, 4.0F, 1.25F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -3.0F, 1.75F, 0.25F, 4.0F, 2.25F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(4.0F, -3.0F, 1.75F, 0.25F, 3.75F, 2.25F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(-4.25F, -3.0F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(4.0F, -3.0F, -0.25F, 0.25F, 2.0F, 2.0F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(1.75F, -8.0F, -4.5F, 2.25F, 2.25F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(2.75F, -6.0F, -4.5F, 1.25F, 1.5F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(0, 44).addBox(2.25F, -6.0F, -4.5F, 0.5F, 0.75F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 48).addBox(-4.0F, -8.0F, -4.5F, 5.75F, 1.0F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 49).addBox(-4.0F, -7.25F, -4.5F, 5.75F, 1.25F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 49).addBox(-4.0F, -6.0F, -4.5F, 3.0F, 1.0F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 49).addBox(-1.0F, -6.0F, -4.5F, 1.0F, 0.75F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 49).addBox(0.0F, -6.0F, -4.5F, 1.0F, 0.5F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 49).addBox(1.0F, -6.0F, -4.5F, 1.25F, 0.25F, 0.5F, 0.0F, false);
        bipedHead.setTextureOffset(1, 49).addBox(-4.0F, -8.25F, -4.0F, 8.0F, 0.25F, 8.0F, 0.0F, false);
        bipedHead.setTextureOffset(1, 44).addBox(-4.0F, -8.0F, 3.0F, 8.0F, 9.5F, 1.25F, 0.0F, false);

        bipedHeadwear.showModel = false;
    }
}
