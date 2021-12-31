// Made with Blockbench 4.1.1
// Exported for Minecraft version 1.15 - 1.16 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityNetherworkerFemale extends CitizenModel<AbstractEntityCitizen>  
{

	public ModelEntityNetherworkerFemale() 
	{
		ModelRenderer glasses;
		ModelRenderer crystalLeft;
		ModelRenderer crystalRight;
		ModelRenderer HairExtension;
		ModelRenderer Ponytail;
		ModelRenderer ponyTailTip_r1;
		ModelRenderer ponytailBase_r1;
		ModelRenderer breast;
		ModelRenderer backpack;
		ModelRenderer roll_r1;
		ModelRenderer lid_r1;
		ModelRenderer shovel;
		
		texWidth = 128;
		texHeight = 64;

		head = new ModelRenderer(this);
		head.setPos(0.0F, 0.0F, 0.0F);
		head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		glasses = new ModelRenderer(this);
		glasses.setPos(0.0F, -0.2F, 0.0F);
		head.addChild(glasses);
		glasses.texOffs(98, 6).addBox(-4.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, 0.05F, false);
		glasses.texOffs(108, 6).addBox(3.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, 0.05F, false);
		glasses.texOffs(104, 0).addBox(4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, 0.05F, false);
		glasses.texOffs(88, 0).addBox(-4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, 0.05F, false);
		glasses.texOffs(96, 9).addBox(-4.0F, -4.0F, 4.01F, 8.0F, 1.0F, 0.0F, 0.05F, false);
		glasses.texOffs(101, 2).addBox(-1.0F, -4.1F, -4.8F, 2.0F, 1.0F, 1.0F, -0.21F, false);

		crystalLeft = new ModelRenderer(this);
		crystalLeft.setPos(4.0F, 1.5F, 0.0F);
		glasses.addChild(crystalLeft);
		crystalLeft.texOffs(111, 2).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, -0.2F, false);
		crystalLeft.texOffs(111, 0).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, -0.2F, false);
		crystalLeft.texOffs(117, 1).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, -0.2F, false);
		crystalLeft.texOffs(107, 1).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, -0.2F, false);
		crystalLeft.texOffs(111, 5).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, -0.2F, false);

		crystalRight = new ModelRenderer(this);
		crystalRight.setPos(0.0F, 1.5F, 0.0F);
		glasses.addChild(crystalRight);
		crystalRight.texOffs(91, 2).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, -0.2F, false);
		crystalRight.texOffs(91, 0).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, -0.2F, false);
		crystalRight.texOffs(97, 1).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, -0.2F, false);
		crystalRight.texOffs(87, 1).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, -0.2F, false);
		crystalRight.texOffs(91, 5).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, -0.2F, false);

		HairExtension = new ModelRenderer(this);
		HairExtension.setPos(0.0F, 1.0F, 0.0F);
		head.addChild(HairExtension);
		HairExtension.texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, 0.5F, false);

		Ponytail = new ModelRenderer(this);
		Ponytail.setPos(0.0F, -4.5403F, 4.0061F);
		head.addChild(Ponytail);
		setRotationAngle(Ponytail, 0.5236F, 0.0F, 0.0F);
		

		ponyTailTip_r1 = new ModelRenderer(this);
		ponyTailTip_r1.setPos(-0.5F, 3.5403F, 0.7939F);
		Ponytail.addChild(ponyTailTip_r1);
		setRotationAngle(ponyTailTip_r1, 0.2231F, 0.0F, 0.0F);
		ponyTailTip_r1.texOffs(88, 55).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, 0.1F, true);

		ponytailBase_r1 = new ModelRenderer(this);
		ponytailBase_r1.setPos(-1.0F, 0.5403F, -2.0061F);
		Ponytail.addChild(ponytailBase_r1);
		setRotationAngle(ponytailBase_r1, 0.5577F, 0.0F, 0.0F);
		ponytailBase_r1.texOffs(86, 48).addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, 0.0F, true);

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

		backpack = new ModelRenderer(this);
		backpack.setPos(0.0F, 5.9F, 2.3F);
		body.addChild(backpack);
		backpack.texOffs(100, 48).addBox(-4.0F, -2.7F, 0.0F, 8.0F, 8.0F, 6.0F, 0.3F, false);
		backpack.texOffs(116, 30).addBox(-5.4F, -1.6F, 1.0F, 2.0F, 6.0F, 4.0F, 0.0F, false);
		backpack.texOffs(104, 30).addBox(3.4F, -1.6F, 1.0F, 2.0F, 6.0F, 4.0F, 0.0F, false);

		roll_r1 = new ModelRenderer(this);
		roll_r1.setPos(0.0F, -6.4F, 3.2F);
		backpack.addChild(roll_r1);
		setRotationAngle(roll_r1, 0.5585F, 0.0F, 0.0F);
		roll_r1.texOffs(102, 24).addBox(-5.0F, -1.0F, -2.1F, 10.0F, 3.0F, 3.0F, 0.0F, false);

		lid_r1 = new ModelRenderer(this);
		lid_r1.setPos(0.0F, -2.1F, 0.6F);
		backpack.addChild(lid_r1);
		setRotationAngle(lid_r1, 0.0436F, 0.0F, 0.0F);
		lid_r1.texOffs(100, 40).addBox(-4.0F, -2.2F, -0.5F, 8.0F, 2.0F, 6.0F, 0.4F, false);

		shovel = new ModelRenderer(this);
		shovel.setPos(-5.1F, -1.875F, 6.095F);
		backpack.addChild(shovel);
		setRotationAngle(shovel, 0.1165F, 0.5925F, 0.1288F);
		shovel.texOffs(124, 17).addBox(-0.5F, -4.825F, -0.495F, 1.0F, 6.0F, 1.0F, -0.1F, false);
		shovel.texOffs(120, 13).addBox(-0.5F, -4.825F, -1.495F, 1.0F, 1.0F, 3.0F, -0.11F, false);
		shovel.texOffs(116, 18).addBox(-0.5F, 0.575F, -1.495F, 1.0F, 3.0F, 3.0F, 0.0F, false);
		shovel.texOffs(114, 15).addBox(-0.5F, 3.575F, -1.015F, 1.0F, 1.0F, 2.0F, 0.0F, false);

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
		leftLeg.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);
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