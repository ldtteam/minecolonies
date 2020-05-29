package com.minecolonies.coremod.client.model.raiders;// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ChiefNorsemen extends NorsemenModel
{

	private final ModelRenderer bone;
	private final ModelRenderer FurWaist;
	private final ModelRenderer bone10;
	private final ModelRenderer bone11;
	private final ModelRenderer FurCape;
	private final ModelRenderer bone12;
	private final ModelRenderer Helmet;
	private final ModelRenderer Helmet_Horn_L;
	private final ModelRenderer bone5;
	private final ModelRenderer bone3;
	private final ModelRenderer bone2;
	private final ModelRenderer bone4;
	private final ModelRenderer Helmet_Horn_L2;
	private final ModelRenderer bone6;
	private final ModelRenderer bone7;
	private final ModelRenderer bone8;
	private final ModelRenderer bone9;

	public ChiefNorsemen()
    {
		textureWidth = 128;
		textureHeight = 128;

		Head = new ModelRenderer(this);
		Head.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(Head);
		Head.setTextureOffset(40, 18).addBox(4.0F, -23.5F, -12.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		Head.setTextureOffset(28, 36).addBox(3.8F, -19.0F, -12.3F, 8.0F, 9.0F, 8.0F, 0.0F, false);
		Head.setTextureOffset(30, 89).addBox(4.0F, -19.0F, -12.1F, 8.0F, 9.0F, 8.0F, 0.0F, false);
		Head.setTextureOffset(62, 86).addBox(3.7F, -19.0F, -12.1F, 8.0F, 3.0F, 8.0F, 0.0F, false);

		Left_Leg = new ModelRenderer(this);
		Left_Leg.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(Left_Leg);
		Left_Leg.setTextureOffset(0, 61).addBox(7.5F, -4.0F, -10.5F, 5.0F, 13.0F, 5.0F, 0.0F, false);
		Left_Leg.setTextureOffset(70, 44).addBox(8.0F, -3.5F, -10.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		Right_Leg = new ModelRenderer(this);
		Right_Leg.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(Right_Leg);
		Right_Leg.setTextureOffset(50, 53).addBox(3.5F, -4.0F, -10.5F, 5.0F, 13.0F, 5.0F, 0.0F, false);
		Right_Leg.setTextureOffset(38, 69).addBox(4.0F, -3.5F, -10.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		Left_Arm = new ModelRenderer(this);
		Left_Arm.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(Left_Arm);
		Left_Arm.setTextureOffset(65, 66).addBox(11.5F, -16.0F, -10.5F, 5.0F, 13.0F, 5.0F, 0.0F, false);
		Left_Arm.setTextureOffset(0, 80).addBox(12.0F, -15.5F, -10.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		Right_Arm = new ModelRenderer(this);
		Right_Arm.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(Right_Arm);
		Right_Arm.setTextureOffset(20, 69).addBox(-0.49F, -16.0F, -10.5F, 4.0F, 13.0F, 5.0F, 0.0F, false);
		Right_Arm.setTextureOffset(72, 14).addBox(0.0F, -15.5F, -10.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		Body = new ModelRenderer(this);
		Body.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(Body);
		Body.setTextureOffset(0, 44).addBox(3.9F, -16.03F, -10.5F, 8.0F, 13.0F, 5.0F, 0.0F, false);

		bone = new ModelRenderer(this);
		bone.setRotationPoint(8.0F, 0.0F, -8.0F);
		Body.addChild(bone);
		setRotationAngle(bone, 0.0F, 3.1416F, 0.0F);
		bone.setTextureOffset(26, 53).addBox(-4.0F, -15.5F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

		FurWaist = new ModelRenderer(this);
		FurWaist.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(FurWaist);
		FurWaist.setTextureOffset(52, 34).addBox(2.9F, -5.25F, -11.25F, 10.0F, 4.0F, 6.0F, 0.0F, false);
		FurWaist.setTextureOffset(72, 0).addBox(4.9F, -2.25F, -6.35F, 6.0F, 9.0F, 1.0F, 0.0F, false);

		bone10 = new ModelRenderer(this);
		bone10.setRotationPoint(8.0F, 0.0F, -7.9F);
		FurWaist.addChild(bone10);
		bone10.setTextureOffset(50, 0).addBox(-4.9F, -0.85F, -5.35F, 9.0F, 5.0F, 2.0F, 0.0F, false);
		bone10.setTextureOffset(52, 3).addBox(-4.8F, 3.25F, -4.85F, 9.0F, 3.0F, 0.0F, 0.0F, false);

		bone11 = new ModelRenderer(this);
		bone11.setRotationPoint(0.0F, 0.0F, 0.0F);
		bone10.addChild(bone11);
		setRotationAngle(bone11, -0.4363F, 0.0F, 0.0F);
		bone11.setTextureOffset(44, 7).addBox(-5.0F, -4.25F, -5.35F, 10.0F, 5.0F, 6.0F, 0.0F, false);

		FurCape = new ModelRenderer(this);
		FurCape.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.addChild(FurCape);
		FurCape.setTextureOffset(0, 13).addBox(-1.4F, -13.7F, -12.9F, 18.0F, 7.0F, 6.0F, 0.0F, false);

		bone12 = new ModelRenderer(this);
		bone12.setRotationPoint(8.0F, -3.3F, 1.4F);
		FurCape.addChild(bone12);
		setRotationAngle(bone12, -0.6981F, 0.0F, 0.0F);
		bone12.setTextureOffset(0, 0).addBox(-9.5F, -5.6633F, -17.6598F, 19.0F, 7.0F, 6.0F, 0.0F, false);

		Helmet = new ModelRenderer(this);
		Helmet.setRotationPoint(0.0F, 0.0F, 0.0F);
		steve.addChild(Helmet);
		Helmet.setTextureOffset(0, 26).addBox(3.5F, -24.0F, -12.5F, 9.0F, 9.0F, 9.0F, 0.0F, false);

		Helmet_Horn_L = new ModelRenderer(this);
		Helmet_Horn_L.setRotationPoint(-11.0F, -22.0F, 2.0F);
		Helmet.addChild(Helmet_Horn_L);
		Helmet_Horn_L.setTextureOffset(60, 44).addBox(23.0F, -1.1F, -13.0F, 1.0F, 4.0F, 4.0F, 0.0F, false);

		bone5 = new ModelRenderer(this);
		bone5.setRotationPoint(19.1F, 21.9F, -5.9F);
		Helmet_Horn_L.addChild(bone5);
		setRotationAngle(bone5, -0.6109F, 0.0F, 0.0F);
		bone5.setTextureOffset(3, 2).addBox(9.2F, -21.25F, -16.95F, 0.0F, 2.0F, 0.0F, 0.0F, false);
		bone5.setTextureOffset(1, 15).addBox(9.1F, -19.45F, -17.45F, 1.0F, 2.0F, 1.0F, 0.0F, false);

		bone3 = new ModelRenderer(this);
		bone3.setRotationPoint(30.0F, 2.25F, -12.0F);
		Helmet_Horn_L.addChild(bone3);
		setRotationAngle(bone3, 0.0F, 0.0F, 0.4363F);
		bone3.setTextureOffset(0, 0).addBox(-3.6063F, -3.1893F, 1.222F, 1.0F, 3.0F, 2.0F, 0.0F, false);

		bone2 = new ModelRenderer(this);
		bone2.setRotationPoint(25.0F, 1.05F, -12.3F);
		Helmet_Horn_L.addChild(bone2);
		setRotationAngle(bone2, 0.0F, -0.5236F, 0.0F);
		bone2.setTextureOffset(30, 30).addBox(0.2943F, -2.05F, -0.3266F, 3.0F, 2.0F, 1.0F, 0.0F, false);

		bone4 = new ModelRenderer(this);
		bone4.setRotationPoint(25.0F, 0.5F, -12.0F);
		Helmet_Horn_L.addChild(bone4);
		setRotationAngle(bone4, 0.0F, 0.0F, -0.4363F);
		bone4.setTextureOffset(27, 27).addBox(-1.9566F, -1.5462F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, false);

		Helmet_Horn_L2 = new ModelRenderer(this);
		Helmet_Horn_L2.setRotationPoint(27.0F, -22.0F, 2.0F);
		Helmet.addChild(Helmet_Horn_L2);
		Helmet_Horn_L2.setTextureOffset(60, 44).addBox(-24.0F, -1.1F, -13.0F, 1.0F, 4.0F, 4.0F, 0.0F, true);

		bone6 = new ModelRenderer(this);
		bone6.setRotationPoint(-19.1F, 21.9F, -5.9F);
		Helmet_Horn_L2.addChild(bone6);
		setRotationAngle(bone6, -0.6109F, 0.0F, 0.0F);
		bone6.setTextureOffset(3, 2).addBox(-10.0F, -21.25F, -16.95F, 0.0F, 2.0F, 0.0F, 0.0F, true);
		bone6.setTextureOffset(1, 15).addBox(-10.1F, -19.45F, -17.45F, 1.0F, 2.0F, 1.0F, 0.0F, true);

		bone7 = new ModelRenderer(this);
		bone7.setRotationPoint(-30.0F, 2.25F, -12.0F);
		Helmet_Horn_L2.addChild(bone7);
		setRotationAngle(bone7, 0.0F, 0.0F, -0.4363F);
		bone7.setTextureOffset(0, 0).addBox(1.9063F, -3.1893F, 1.222F, 1.0F, 3.0F, 2.0F, 0.0F, true);

		bone8 = new ModelRenderer(this);
		bone8.setRotationPoint(-25.0F, 1.05F, -12.3F);
		Helmet_Horn_L2.addChild(bone8);
		setRotationAngle(bone8, 0.0F, 0.5236F, 0.0F);
		bone8.setTextureOffset(30, 30).addBox(-3.5943F, -2.05F, -0.3266F, 3.0F, 2.0F, 1.0F, 0.0F, true);

		bone9 = new ModelRenderer(this);
		bone9.setRotationPoint(-25.0F, 0.5F, -12.0F);
		Helmet_Horn_L2.addChild(bone9);
		setRotationAngle(bone9, 0.0F, 0.0F, 0.4363F);
		bone9.setTextureOffset(27, 27).addBox(-1.0434F, -1.5462F, -0.5F, 3.0F, 3.0F, 3.0F, 0.0F, true);
	}
}
