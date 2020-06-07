package com.minecolonies.coremod.client.model.raiders;// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelShieldmaiden extends NorsemenModel
{
	public ModelShieldmaiden()
    {
        ModelRenderer skirt;
        ModelRenderer chest;

        ModelRenderer shieldA;
        ModelRenderer shieldB;
        
		textureWidth = 124;
		textureHeight = 64;

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		bipedHead.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		bipedBody.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

		skirt = new ModelRenderer(this);
		skirt.setRotationPoint(-0.95F, 12.25F, 0.5F);
		bipedBody.addChild(skirt);
		skirt.setTextureOffset(67, 11).addBox(-3.5F, -0.25F, -3.5F, 9.0F, 6.0F, 6.0F, 0.0F, false);
		skirt.setTextureOffset(67, 0).addBox(-3.0F, -1.0F, -3.0F, 8.0F, 6.0F, 5.0F, 0.0F, false);

		chest = new ModelRenderer(this);
		chest.setRotationPoint(0.0F, 24.0F, -6.0F);
		bipedBody.addChild(chest);
		setRotationAngle(chest, -0.4363F, 0.0F, 0.0F);
		chest.setTextureOffset(67, 49).addBox(-4.0F, -23.8558F, -5.7275F, 8.0F, 5.0F, 4.0F, 0.25F, false);

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

		shieldA = new ModelRenderer(this);
        bipedBody.addChild(shieldA);
		shieldA.setRotationPoint(9.25F, 15.0F, -4.75F);
		shieldA.setTextureOffset(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
		shieldA.setTextureOffset(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
		shieldA.setTextureOffset(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);

		shieldB = new ModelRenderer(this);
        bipedBody.addChild(shieldB);
		shieldB.setRotationPoint(5.0F, 12.0F, 4.0F);
		setRotationAngle(shieldB, 0.0F, -1.5708F, 0.0F);
		shieldB.setTextureOffset(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
		shieldB.setTextureOffset(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
		shieldB.setTextureOffset(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
	    bipedHeadwear.showModel = false;
    }

    private void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}