// Made with Blockbench 4.1.5
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityDruidFemale extends CitizenModel<AbstractEntityCitizen>
{
	public ModelEntityDruidFemale() 
	{
		ModelRenderer HairExtension;
		ModelRenderer Ponytail;
		ModelRenderer ponyTailTip_r1;
		ModelRenderer ponytailBase_r1;
		ModelRenderer Braid;
		ModelRenderer hair1;
		ModelRenderer hair2;
		ModelRenderer hair3;
		ModelRenderer hair4;
		ModelRenderer Horns;
		ModelRenderer righthorn_r1;
		ModelRenderer lefthorn_r1;
		ModelRenderer breast;
		ModelRenderer potionSatchet;
		ModelRenderer potions;
		ModelRenderer Potion1;
		ModelRenderer Potion2;
		ModelRenderer Potion3;
		ModelRenderer satchet;
		ModelRenderer lid;
		ModelRenderer locket_r1;
		ModelRenderer lid_r1;
		ModelRenderer straps;
		ModelRenderer rightStrap_r1;
		ModelRenderer leftStrap_r1;

		
		texWidth = 128;
		texHeight = 64;

		head = new ModelRenderer(this);
		head.setPos(0.0F, 0.0F, 0.0F);
		head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		HairExtension = new ModelRenderer(this);
		HairExtension.setPos(0.0F, 1.0F, 0.0F);
		head.addChild(HairExtension);
		HairExtension.texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, 0.5F, false);

		Ponytail = new ModelRenderer(this);
		Ponytail.setPos(0.0F, 24.0F, 0.0F);
		head.addChild(Ponytail);
		

		ponyTailTip_r1 = new ModelRenderer(this);
		ponyTailTip_r1.setPos(-0.5F, -25.0F, 4.8F);
		Ponytail.addChild(ponyTailTip_r1);
		setRotationAngle(ponyTailTip_r1, 0.2231F, 0.0F, 0.0F);
		ponyTailTip_r1.texOffs(88, 55).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, 0.1F, true);

		ponytailBase_r1 = new ModelRenderer(this);
		ponytailBase_r1.setPos(-1.0F, -28.0F, 2.0F);
		Ponytail.addChild(ponytailBase_r1);
		setRotationAngle(ponytailBase_r1, 0.5577F, 0.0F, 0.0F);
		ponytailBase_r1.texOffs(86, 48).addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, 0.0F, true);

		Braid = new ModelRenderer(this);
		Braid.setPos(0.0F, 0.0F, 0.0F);
		head.addChild(Braid);
		

		hair1 = new ModelRenderer(this);
		hair1.setPos(0.0F, 1.0F, 0.0F);
		Braid.addChild(hair1);
		setRotationAngle(hair1, 0.0F, 1.9333F, 0.0F);
		hair1.texOffs(74, 1).addBox(-0.1F, -1.0F, 5.1F, 1.0F, 2.0F, 1.0F, 0.0F, true);

		hair2 = new ModelRenderer(this);
		hair2.setPos(0.0F, 1.0F, 0.0F);
		Braid.addChild(hair2);
		setRotationAngle(hair2, 0.0F, 1.9333F, 0.0F);
		hair2.texOffs(78, 0).addBox(-1.1F, -2.0F, 5.1F, 2.0F, 1.0F, 1.0F, 0.0F, true);

		hair3 = new ModelRenderer(this);
		hair3.setPos(0.0F, 1.0F, 0.0F);
		Braid.addChild(hair3);
		setRotationAngle(hair3, 0.0F, 1.3384F, 0.0F);
		hair3.texOffs(76, 2).addBox(-1.0F, -2.0F, 4.8F, 4.0F, 1.0F, 2.0F, 0.0F, true);

		hair4 = new ModelRenderer(this);
		hair4.setPos(0.0F, 1.0F, 0.0F);
		Braid.addChild(hair4);
		setRotationAngle(hair4, 0.0F, 0.4833F, 0.0F);
		hair4.texOffs(86, 0).addBox(0.5F, -2.5F, 3.35F, 4.0F, 2.0F, 2.0F, 0.0F, true);

		Horns = new ModelRenderer(this);
		Horns.setPos(0.0F, 0.0F, 0.0F);
		head.addChild(Horns);
		

		righthorn_r1 = new ModelRenderer(this);
		righthorn_r1.setPos(4.5F, -6.0F, -3.0F);
		Horns.addChild(righthorn_r1);
		setRotationAngle(righthorn_r1, 0.0F, 0.48F, 0.0F);
		righthorn_r1.texOffs(80, 24).addBox(-0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, 0.0F, false);

		lefthorn_r1 = new ModelRenderer(this);
		lefthorn_r1.setPos(-4.5F, -6.0F, -3.0F);
		Horns.addChild(lefthorn_r1);
		setRotationAngle(lefthorn_r1, 0.0F, -0.48F, 0.0F);
		lefthorn_r1.texOffs(80, 10).addBox(0.6F, -7.0F, 0.0F, 0.0F, 7.0F, 7.0F, 0.0F, false);

		body = new ModelRenderer(this);
		body.setPos(0.0F, 0.0F, 0.0F);
		body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

		breast = new ModelRenderer(this);
		breast.setPos(-1.0F, 3.0F, 4.0F);
		body.addChild(breast);
		setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
		breast.texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, 0.0F, false);
		breast.texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, 0.25F, false);

		potionSatchet = new ModelRenderer(this);
		potionSatchet.setPos(0.0F, 24.0F, 0.0F);
		body.addChild(potionSatchet);
		

		potions = new ModelRenderer(this);
		potions.setPos(0.0F, -14.2F, -4.0F);
		potionSatchet.addChild(potions);
		

		Potion1 = new ModelRenderer(this);
		Potion1.setPos(1.8F, -5.22F, -0.2F);
		potions.addChild(Potion1);
		Potion1.texOffs(96, 7).addBox(-1.9152F, 1.4965F, -2.5F, 5.0F, 6.0F, 5.0F, -1.4F, false);
		Potion1.texOffs(112, 14).addBox(-1.4152F, 2.2965F, -2.0F, 4.0F, 5.0F, 4.0F, -1.3F, false);
		Potion1.texOffs(116, 10).addBox(-0.4152F, 1.6465F, -1.0F, 2.0F, 2.0F, 2.0F, -0.5F, false);
		Potion1.texOffs(97, 18).addBox(-1.9152F, 3.1465F, -2.5F, 5.0F, 3.0F, 5.0F, -1.3F, false);

		Potion2 = new ModelRenderer(this);
		Potion2.setPos(-0.467F, -6.1666F, -0.2F);
		potions.addChild(Potion2);
		Potion2.texOffs(97, 26).addBox(-2.1F, 2.42F, -2.5F, 5.0F, 6.0F, 5.0F, -1.4F, false);
		Potion2.texOffs(112, 33).addBox(-1.6F, 3.22F, -2.0F, 4.0F, 5.0F, 4.0F, -1.3F, false);
		Potion2.texOffs(116, 29).addBox(-0.6F, 2.57F, -1.0F, 2.0F, 2.0F, 2.0F, -0.5F, false);
		Potion2.texOffs(97, 37).addBox(-2.1F, 4.07F, -2.5F, 5.0F, 3.0F, 5.0F, -1.3F, false);

		Potion3 = new ModelRenderer(this);
		Potion3.setPos(-2.5233F, -2.9508F, -0.2F);
		potions.addChild(Potion3);
		Potion3.texOffs(96, 45).addBox(-2.4653F, -0.8009F, -2.5F, 5.0F, 6.0F, 5.0F, -1.4F, false);
		Potion3.texOffs(112, 52).addBox(-1.9653F, -0.0009F, -2.0F, 4.0F, 5.0F, 4.0F, -1.3F, false);
		Potion3.texOffs(116, 48).addBox(-0.9653F, -0.6509F, -1.0F, 2.0F, 2.0F, 2.0F, -0.5F, false);
		Potion3.texOffs(97, 56).addBox(-2.4653F, 0.8491F, -2.5F, 5.0F, 3.0F, 5.0F, -1.3F, false);

		satchet = new ModelRenderer(this);
		satchet.setPos(0.0F, -14.0F, -4.0F);
		potionSatchet.addChild(satchet);
		satchet.texOffs(60, 12).addBox(-4.5F, 0.8F, -2.2F, 9.0F, 1.0F, 4.0F, -0.3F, false);
		satchet.texOffs(59, 28).addBox(-4.5F, -3.6F, 0.8F, 9.0F, 5.0F, 1.0F, -0.31F, false);
		satchet.texOffs(60, 24).addBox(-4.0F, -1.5F, -2.09F, 8.0F, 3.0F, 1.0F, -0.2F, false);
		satchet.texOffs(70, 17).addBox(3.5F, -1.6F, -2.2F, 1.0F, 3.0F, 4.0F, -0.3F, false);
		satchet.texOffs(60, 17).addBox(-4.5F, -1.6F, -2.2F, 1.0F, 3.0F, 4.0F, -0.3F, false);

		lid = new ModelRenderer(this);
		lid.setPos(0.0F, 1.3F, -0.2F);
		satchet.addChild(lid);
		

		locket_r1 = new ModelRenderer(this);
		locket_r1.setPos(0.0F, -5.2F, -0.6F);
		lid.addChild(locket_r1);
		setRotationAngle(locket_r1, -0.3142F, 0.0F, 0.0F);
		locket_r1.texOffs(66, 18).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 1.0F, -0.4F, false);

		lid_r1 = new ModelRenderer(this);
		lid_r1.setPos(0.0F, -4.5F, 1.4F);
		lid.addChild(lid_r1);
		setRotationAngle(lid_r1, 1.0472F, 0.0F, 0.0F);
		lid_r1.texOffs(59, 34).addBox(-4.5F, -2.5F, -0.5F, 9.0F, 3.0F, 1.0F, -0.311F, false);

		straps = new ModelRenderer(this);
		straps.setPos(0.0F, 0.0F, 0.0F);
		potionSatchet.addChild(straps);
		

		rightStrap_r1 = new ModelRenderer(this);
		rightStrap_r1.setPos(-4.3F, -14.7F, -4.1F);
		straps.addChild(rightStrap_r1);
		setRotationAngle(rightStrap_r1, -0.2094F, 0.0F, 0.0F);
		rightStrap_r1.texOffs(58, 16).addBox(0.0F, -9.0F, -0.5F, 0.0F, 10.0F, 1.0F, -0.1F, false);

		leftStrap_r1 = new ModelRenderer(this);
		leftStrap_r1.setPos(4.3F, -14.7F, -4.1F);
		straps.addChild(leftStrap_r1);
		setRotationAngle(leftStrap_r1, -0.2007F, 0.0F, 0.0F);
		leftStrap_r1.texOffs(56, 16).addBox(0.0F, -9.0F, -0.5F, 0.0F, 10.0F, 1.0F, -0.1F, false);

		rightArm = new ModelRenderer(this);
		rightArm.setPos(-5.0F, 2.0F, 0.0F);
		rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
		rightArm.texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

		leftArm = new ModelRenderer(this);
		leftArm.setPos(5.0F, 2.0F, 0.0F);
		leftArm.texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
		leftArm.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

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
