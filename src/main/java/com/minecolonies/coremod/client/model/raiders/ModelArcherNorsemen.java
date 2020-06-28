package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelArcherNorsemen extends NorsemenModel
{
	public ModelArcherNorsemen()
    {
        ModelRenderer quiver;
        ModelRenderer hood;
		textureWidth = 124;
		textureHeight = 64;

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		bipedHead.setTextureOffset(33, 1).addBox(-4.0F, -8.0F, -3.75F, 8.0F, 8.0F, 7.0F, 0.5F, false);

        bipedBody = new ModelRenderer(this);
        bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
        bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(75, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        bipedBody.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        hood = new ModelRenderer(this);
		hood.setRotationPoint(-0.5F, 24.5F, 0.0F);
		bipedHead.addChild(hood);
		hood.setTextureOffset(59, 25).addBox(-4.0F, -33.0F, -4.0F, 9.0F, 9.0F, 8.0F, 0.5F, false);
		hood.setTextureOffset(64, 0).addBox(-8.0F, -24.75F, -2.25F, 17.0F, 20.0F, 5.0F, 0.25F, false);

        quiver = new ModelRenderer(this);
        quiver.setRotationPoint(-4.9F, 2.0F, 6.0F);
        setRotationAngle(quiver, 0.0F, 0.0F, -0.6109F);
        bipedBody.addChild(quiver);
        quiver.setTextureOffset(99, 46).addBox(-0.979F, -4.9528F, -1.25F, 3.0F, 14.0F, 0.0F, 0.0F, false);
        quiver.setTextureOffset(90, 45).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, 0.0F, false);
        quiver.setTextureOffset(79, 46).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, 0.25F, false);

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedRightArm.setTextureOffset(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedLeftArm.setTextureOffset(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedRightLeg.setTextureOffset(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		bipedLeftLeg.setTextureOffset(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        bipedHeadwear.showModel = false;
	}

	private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}