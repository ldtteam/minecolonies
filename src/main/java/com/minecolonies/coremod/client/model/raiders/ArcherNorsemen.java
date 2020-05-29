package com.minecolonies.coremod.client.model.raiders;// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ArcherNorsemen extends NorsemenModel
{
	public ArcherNorsemen() 
    {
        ModelRenderer Quiver;
        ModelRenderer Head;
        ModelRenderer Hood;
        ModelRenderer Body;
        ModelRenderer RightArm;
        ModelRenderer LeftArm;
        ModelRenderer RightLeg;
        ModelRenderer LeftLeg;
		textureWidth = 124;
		textureHeight = 64;

		Quiver = new ModelRenderer(this);
		Quiver.setRotationPoint(-4.9F, 2.0F, 6.0F);
		setRotationAngle(Quiver, 0.0F, 0.0F, -0.6109F);
		Quiver.setTextureOffset(99, 46).addBox(-0.979F, -4.9528F, -1.25F, 3.0F, 14.0F, 0.0F, 0.0F, false);
		Quiver.setTextureOffset(90, 45).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, 0.0F, false);
		Quiver.setTextureOffset(79, 46).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, 0.25F, false);

		Head = new ModelRenderer(this);
		Head.setRotationPoint(0.0F, 0.0F, 0.0F);
		Head.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		Head.setTextureOffset(33, 1).addBox(-4.0F, -8.0F, -3.75F, 8.0F, 8.0F, 7.0F, 0.5F, false);

		Hood = new ModelRenderer(this);
		Hood.setRotationPoint(-0.5F, 24.5F, 0.0F);
		Head.addChild(Hood);
		Hood.setTextureOffset(59, 25).addBox(-4.0F, -33.0F, -4.0F, 9.0F, 9.0F, 8.0F, 0.5F, false);
		Hood.setTextureOffset(64, 0).addBox(-8.0F, -24.75F, -2.25F, 17.0F, 20.0F, 5.0F, 0.25F, false);

		Body = new ModelRenderer(this);
		Body.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		Body.setTextureOffset(75, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		Body.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

		RightArm = new ModelRenderer(this);
		RightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		RightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		RightArm.setTextureOffset(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		LeftArm = new ModelRenderer(this);
		LeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		LeftArm.setTextureOffset(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		LeftArm.setTextureOffset(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		RightLeg = new ModelRenderer(this);
		RightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
		RightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		RightLeg.setTextureOffset(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		LeftLeg = new ModelRenderer(this);
		LeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
		LeftLeg.setTextureOffset(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		LeftLeg.setTextureOffset(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}