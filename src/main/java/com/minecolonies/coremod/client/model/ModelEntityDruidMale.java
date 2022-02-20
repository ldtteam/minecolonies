// Made with Blockbench 4.1.5
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityDruidMale extends CitizenModel<AbstractEntityCitizen>
{
	public ModelEntityDruidMale() 
	{
		ModelRenderer shortBeard;
		ModelRenderer Horns;
		ModelRenderer righthorn_r1;
		ModelRenderer lefthorn_r1;
		ModelRenderer potionBag;
		ModelRenderer Potion1;
		ModelRenderer Potion2;
		ModelRenderer Potion3;
		ModelRenderer mainStrap;
		ModelRenderer mainStrap_r1;
		ModelRenderer symbol;
		ModelRenderer symbol_r1;
	 
		texWidth = 128;
		texHeight = 64;

		head = new ModelRenderer(this);
		head.setPos(0.0F, 0.0F, 0.0F);
		head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		shortBeard = new ModelRenderer(this);
		shortBeard.setPos(0.0F, 24.0F, 0.0F);
		head.addChild(shortBeard);
		shortBeard.texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, 0.0F, false);
		shortBeard.texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, 0.5F, false);

		Horns = new ModelRenderer(this);
		Horns.setPos(0.0F, 0.0F, 0.0F);
		head.addChild(Horns);
		

		righthorn_r1 = new ModelRenderer(this);
		righthorn_r1.setPos(4.5F, -6.0F, -3.0F);
		Horns.addChild(righthorn_r1);
		setRotationAngle(righthorn_r1, 0.0F, 0.48F, 0.0F);
		righthorn_r1.texOffs(82, 41).addBox(-0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, 0.0F, false);

		lefthorn_r1 = new ModelRenderer(this);
		lefthorn_r1.setPos(-4.5F, -6.0F, -3.0F);
		Horns.addChild(lefthorn_r1);
		setRotationAngle(lefthorn_r1, 0.0F, -0.48F, 0.0F);
		lefthorn_r1.texOffs(82, 27).addBox(0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, 0.0F, false);

		body = new ModelRenderer(this);
		body.setPos(0.0F, 0.0F, 0.0F);
		body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

		potionBag = new ModelRenderer(this);
		potionBag.setPos(0.0F, 24.0F, 0.0F);
		body.addChild(potionBag);
		

		Potion1 = new ModelRenderer(this);
		Potion1.setPos(2.1F, -21.42F, -4.2F);
		potionBag.addChild(Potion1);
		setRotationAngle(Potion1, 0.0F, 0.0F, -0.6196F);
		Potion1.texOffs(96, 7).addBox(-2.3152F, -2.1035F, -2.5F, 5.0F, 6.0F, 5.0F, -1.1F, false);
		Potion1.texOffs(112, 14).addBox(-1.8152F, -1.3035F, -2.0F, 4.0F, 5.0F, 4.0F, -1.0F, false);
		Potion1.texOffs(116, 10).addBox(-0.8152F, -2.4535F, -1.0F, 2.0F, 2.0F, 2.0F, -0.4F, false);
		Potion1.texOffs(97, 18).addBox(-2.3152F, -0.4535F, -2.5F, 5.0F, 3.0F, 5.0F, -1.0F, false);

		Potion2 = new ModelRenderer(this);
		Potion2.setPos(-0.667F, -18.8666F, -4.2F);
		potionBag.addChild(Potion2);
		setRotationAngle(Potion2, 0.0F, 0.0F, -0.6196F);
		Potion2.texOffs(96, 26).addBox(-2.5F, -2.18F, -2.5F, 5.0F, 6.0F, 5.0F, -1.1F, false);
		Potion2.texOffs(112, 33).addBox(-2.0F, -1.38F, -2.0F, 4.0F, 5.0F, 4.0F, -1.0F, false);
		Potion2.texOffs(116, 29).addBox(-1.0F, -2.53F, -1.0F, 2.0F, 2.0F, 2.0F, -0.4F, false);
		Potion2.texOffs(97, 37).addBox(-2.5F, -0.53F, -2.5F, 5.0F, 3.0F, 5.0F, -1.0F, false);

		Potion3 = new ModelRenderer(this);
		Potion3.setPos(-3.3233F, -15.9508F, -4.2F);
		potionBag.addChild(Potion3);
		setRotationAngle(Potion3, 0.0F, 0.0F, -0.6196F);
		Potion3.texOffs(96, 45).addBox(-2.4653F, -2.4009F, -2.5F, 5.0F, 6.0F, 5.0F, -1.1F, false);
		Potion3.texOffs(112, 52).addBox(-1.9653F, -1.6009F, -2.0F, 4.0F, 5.0F, 4.0F, -1.0F, false);
		Potion3.texOffs(116, 48).addBox(-0.9653F, -2.7509F, -1.0F, 2.0F, 2.0F, 2.0F, -0.4F, false);
		Potion3.texOffs(97, 56).addBox(-2.4653F, -0.7509F, -2.5F, 5.0F, 3.0F, 5.0F, -1.0F, false);

		mainStrap = new ModelRenderer(this);
		mainStrap.setPos(0.0F, -18.3F, -2.0F);
		potionBag.addChild(mainStrap);
		

		mainStrap_r1 = new ModelRenderer(this);
		mainStrap_r1.setPos(0.0F, 0.0F, -0.3F);
		mainStrap.addChild(mainStrap_r1);
		setRotationAngle(mainStrap_r1, 0.0F, 0.0F, -0.829F);
		mainStrap_r1.texOffs(63, 13).addBox(-7.0F, -1.0F, -0.5F, 14.0F, 2.0F, 5.0F, 0.1F, false);

		symbol = new ModelRenderer(this);
		symbol.setPos(4.5F, -23.2F, -3.1F);
		potionBag.addChild(symbol);
		

		symbol_r1 = new ModelRenderer(this);
		symbol_r1.setPos(0.0F, 0.0F, 0.2F);
		symbol.addChild(symbol_r1);
		setRotationAngle(symbol_r1, 0.0F, 0.0F, 0.7854F);
		symbol_r1.texOffs(83, 20).addBox(-2.5F, -2.5F, -1.0F, 5.0F, 5.0F, 2.0F, -1.4F, false);

		rightArm = new ModelRenderer(this);
		rightArm.setPos(-5.0F, 2.0F, 0.0F);
		rightArm.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		rightArm.texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		leftArm = new ModelRenderer(this);
		leftArm.setPos(5.0F, 2.0F, 0.0F);
		leftArm.texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		leftArm.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		rightLeg = new ModelRenderer(this);
		rightLeg.setPos(-1.9F, 12.0F, 0.0F);
		rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		rightLeg.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

		leftLeg = new ModelRenderer(this);
		leftLeg.setPos(1.9F, 12.0F, 0.0F);
		leftLeg.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
		leftLeg.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);
		
		hat.visible = false;
	}
	
    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
