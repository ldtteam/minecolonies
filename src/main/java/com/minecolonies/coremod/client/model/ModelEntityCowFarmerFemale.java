// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


public class custom_model extends EntityModel<Entity> {
	private final ModelRenderer bipedRightLeg;
	private final ModelRenderer bipedRightArm;
	private final ModelRenderer bipedHead;
	private final ModelRenderer headDetail;
	private final ModelRenderer hair;
	private final ModelRenderer left_top_1;
	private final ModelRenderer backhair;
	private final ModelRenderer hairbackTop_2;
	private final ModelRenderer hairbackTop_3;
	private final ModelRenderer hairBackTop_4;
	private final ModelRenderer hairTop_1;
	private final ModelRenderer hairTop_2;
	private final ModelRenderer hairLeftTop_1;
	private final ModelRenderer hairLeftTop_2;
	private final ModelRenderer hairLeftTop_3;
	private final ModelRenderer hairLeftTop_4;
	private final ModelRenderer hairLeftTop_5;
	private final ModelRenderer hairbackbuttom1;
	private final ModelRenderer ponytail_1;
	private final ModelRenderer ponytail_2;
	private final ModelRenderer ponytail_3;
	private final ModelRenderer hairRightTop_1;
	private final ModelRenderer hairfrontTop_1;
	private final ModelRenderer hairfrontTop_2;
	private final ModelRenderer hairfrontTop_3;
	private final ModelRenderer bipedBody;
	private final ModelRenderer backpack;
	private final ModelRenderer bagR;
	private final ModelRenderer bagL;
	private final ModelRenderer bagBack;
	private final ModelRenderer bagFront;
	private final ModelRenderer bagWheat;
	private final ModelRenderer bagBot;
	private final ModelRenderer bipedChest;
	private final ModelRenderer bipedLeftLeg;
	private final ModelRenderer bipedLeftArm;

	public custom_model() {
		textureWidth = 128;
		textureHeight = 64;

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, true);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

		headDetail = new ModelRenderer(this);
		headDetail.setRotationPoint(0.0F, 0.0F, 1.0F);
		bipedHead.addChild(headDetail);
		headDetail.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		hair = new ModelRenderer(this);
		hair.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(hair);
		

		left_top_1 = new ModelRenderer(this);
		left_top_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(left_top_1);
		left_top_1.setTextureOffset(0, 32).addBox(-4.5F, -7.5F, -4.5F, 3.0F, 2.0F, 9.0F, 0.0F, true);

		backhair = new ModelRenderer(this);
		backhair.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(backhair);
		backhair.setTextureOffset(74, 51).addBox(-2.5F, -7.5F, 3.5F, 5.0F, 7.0F, 1.0F, 0.0F, true);

		hairbackTop_2 = new ModelRenderer(this);
		hairbackTop_2.setRotationPoint(0.0F, 0.0F, -3.0F);
		hair.addChild(hairbackTop_2);
		hairbackTop_2.setTextureOffset(53, 55).addBox(-4.5F, -5.5F, -0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

		hairbackTop_3 = new ModelRenderer(this);
		hairbackTop_3.setRotationPoint(0.0F, 0.0F, -4.0F);
		hair.addChild(hairbackTop_3);
		hairbackTop_3.setTextureOffset(33, 55).addBox(-4.5F, -4.5F, 0.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

		hairBackTop_4 = new ModelRenderer(this);
		hairBackTop_4.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairBackTop_4);
		hairBackTop_4.setTextureOffset(30, 42).addBox(-4.5F, -3.5F, 1.5F, 2.0F, 3.0F, 3.0F, 0.0F, true);

		hairTop_1 = new ModelRenderer(this);
		hairTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairTop_1);
		hairTop_1.setTextureOffset(0, 43).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 1.0F, 9.0F, 0.0F, true);

		hairTop_2 = new ModelRenderer(this);
		hairTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairTop_2);
		hairTop_2.setTextureOffset(0, 54).addBox(2.5F, -7.5F, -4.5F, 2.0F, 1.0F, 9.0F, 0.0F, true);

		hairLeftTop_1 = new ModelRenderer(this);
		hairLeftTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairLeftTop_1);
		hairLeftTop_1.setTextureOffset(13, 54).addBox(2.5F, -6.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

		hairLeftTop_2 = new ModelRenderer(this);
		hairLeftTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairLeftTop_2);
		hairLeftTop_2.setTextureOffset(17, 32).addBox(2.5F, -5.5F, -3.5F, 2.0F, 1.0F, 8.0F, 0.0F, true);

		hairLeftTop_3 = new ModelRenderer(this);
		hairLeftTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairLeftTop_3);
		hairLeftTop_3.setTextureOffset(17, 35).addBox(3.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

		hairLeftTop_4 = new ModelRenderer(this);
		hairLeftTop_4.setRotationPoint(0.0F, -1.0F, 0.0F);
		hair.addChild(hairLeftTop_4);
		hairLeftTop_4.setTextureOffset(30, 32).addBox(2.5F, -3.5F, 1.5F, 2.0F, 4.0F, 3.0F, 0.0F, true);

		hairLeftTop_5 = new ModelRenderer(this);
		hairLeftTop_5.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairLeftTop_5);
		hairLeftTop_5.setTextureOffset(15, 32).addBox(-1.5F, -7.5F, -4.5F, 4.0F, 1.0F, 1.0F, 0.0F, true);

		hairbackbuttom1 = new ModelRenderer(this);
		hairbackbuttom1.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairbackbuttom1);
		hairbackbuttom1.setTextureOffset(58, 51).addBox(-3.5F, -0.5F, 3.5F, 7.0F, 3.0F, 1.0F, 0.0F, true);

		ponytail_1 = new ModelRenderer(this);
		ponytail_1.setRotationPoint(7.0F, 4.2F, 2.0F);
		hair.addChild(ponytail_1);
		setRotationAngle(ponytail_1, -1.4486F, 0.0F, 0.0F);
		

		ponytail_2 = new ModelRenderer(this);
		ponytail_2.setRotationPoint(6.0F, -1.0F, 0.0F);
		hair.addChild(ponytail_2);
		setRotationAngle(ponytail_2, -1.0647F, 0.0F, 0.0F);
		

		ponytail_3 = new ModelRenderer(this);
		ponytail_3.setRotationPoint(6.5F, 0.9F, 0.7F);
		hair.addChild(ponytail_3);
		setRotationAngle(ponytail_3, -1.3613F, 0.0F, 0.0F);
		

		hairRightTop_1 = new ModelRenderer(this);
		hairRightTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairRightTop_1);
		hairRightTop_1.setTextureOffset(1, 54).addBox(-4.5F, -0.5F, 2.5F, 1.0F, 2.0F, 2.0F, 0.0F, true);

		hairfrontTop_1 = new ModelRenderer(this);
		hairfrontTop_1.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairfrontTop_1);
		hairfrontTop_1.setTextureOffset(5, 32).addBox(2.5F, -6.5F, -4.5F, 1.0F, 1.0F, 1.0F, 0.0F, true);

		hairfrontTop_2 = new ModelRenderer(this);
		hairfrontTop_2.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairfrontTop_2);
		hairfrontTop_2.setTextureOffset(0, 32).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

		hairfrontTop_3 = new ModelRenderer(this);
		hairfrontTop_3.setRotationPoint(0.0F, 0.0F, 0.0F);
		hair.addChild(hairfrontTop_3);
		hairfrontTop_3.setTextureOffset(0, 37).addBox(3.5F, -6.5F, -4.5F, 1.0F, 3.0F, 1.0F, 0.0F, true);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);

		backpack = new ModelRenderer(this);
		backpack.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(backpack);
		

		bagR = new ModelRenderer(this);
		bagR.setRotationPoint(0.0F, 0.0F, 0.0F);
		backpack.addChild(bagR);
		bagR.setTextureOffset(94, 4).addBox(3.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

		bagL = new ModelRenderer(this);
		bagL.setRotationPoint(0.0F, 0.0F, 0.0F);
		backpack.addChild(bagL);
		bagL.setTextureOffset(94, 16).addBox(-4.0F, 0.0F, 3.0F, 1.0F, 9.0F, 3.0F, 0.0F, true);

		bagBack = new ModelRenderer(this);
		bagBack.setRotationPoint(0.0F, 0.0F, 0.0F);
		backpack.addChild(bagBack);
		bagBack.setTextureOffset(102, 13).addBox(-3.0F, 0.0F, 2.0F, 6.0F, 9.0F, 1.0F, 0.0F, true);

		bagFront = new ModelRenderer(this);
		bagFront.setRotationPoint(0.0F, 0.0F, 0.0F);
		backpack.addChild(bagFront);
		bagFront.setTextureOffset(102, 4).addBox(-3.0F, 1.0F, 6.0F, 6.0F, 8.0F, 1.0F, 0.0F, true);

		bagWheat = new ModelRenderer(this);
		bagWheat.setRotationPoint(0.0F, 0.0F, 0.0F);
		backpack.addChild(bagWheat);
		bagWheat.setTextureOffset(94, 0).addBox(-3.0F, 1.5F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

		bagBot = new ModelRenderer(this);
		bagBot.setRotationPoint(0.0F, 0.0F, 0.0F);
		backpack.addChild(bagBot);
		bagBot.setTextureOffset(102, 23).addBox(-3.0F, 9.0F, 3.0F, 6.0F, 1.0F, 3.0F, 0.0F, true);

		bipedChest = new ModelRenderer(this);
		bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(bipedChest);
		setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
		bipedChest.setTextureOffset(40, 32).addBox(-3.5F, 2.7F, -0.5F, 7.0F, 3.0F, 4.0F, 0.0F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		bipedRightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		bipedRightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		bipedHead.render(matrixStack, buffer, packedLight, packedOverlay);
		headDetail.render(matrixStack, buffer, packedLight, packedOverlay);
		hair.render(matrixStack, buffer, packedLight, packedOverlay);
		left_top_1.render(matrixStack, buffer, packedLight, packedOverlay);
		backhair.render(matrixStack, buffer, packedLight, packedOverlay);
		hairbackTop_2.render(matrixStack, buffer, packedLight, packedOverlay);
		hairbackTop_3.render(matrixStack, buffer, packedLight, packedOverlay);
		hairBackTop_4.render(matrixStack, buffer, packedLight, packedOverlay);
		hairTop_1.render(matrixStack, buffer, packedLight, packedOverlay);
		hairTop_2.render(matrixStack, buffer, packedLight, packedOverlay);
		hairLeftTop_1.render(matrixStack, buffer, packedLight, packedOverlay);
		hairLeftTop_2.render(matrixStack, buffer, packedLight, packedOverlay);
		hairLeftTop_3.render(matrixStack, buffer, packedLight, packedOverlay);
		hairLeftTop_4.render(matrixStack, buffer, packedLight, packedOverlay);
		hairLeftTop_5.render(matrixStack, buffer, packedLight, packedOverlay);
		hairbackbuttom1.render(matrixStack, buffer, packedLight, packedOverlay);
		ponytail_1.render(matrixStack, buffer, packedLight, packedOverlay);
		ponytail_2.render(matrixStack, buffer, packedLight, packedOverlay);
		ponytail_3.render(matrixStack, buffer, packedLight, packedOverlay);
		hairRightTop_1.render(matrixStack, buffer, packedLight, packedOverlay);
		hairfrontTop_1.render(matrixStack, buffer, packedLight, packedOverlay);
		hairfrontTop_2.render(matrixStack, buffer, packedLight, packedOverlay);
		hairfrontTop_3.render(matrixStack, buffer, packedLight, packedOverlay);
		bipedBody.render(matrixStack, buffer, packedLight, packedOverlay);
		backpack.render(matrixStack, buffer, packedLight, packedOverlay);
		bagR.render(matrixStack, buffer, packedLight, packedOverlay);
		bagL.render(matrixStack, buffer, packedLight, packedOverlay);
		bagBack.render(matrixStack, buffer, packedLight, packedOverlay);
		bagFront.render(matrixStack, buffer, packedLight, packedOverlay);
		bagWheat.render(matrixStack, buffer, packedLight, packedOverlay);
		bagBot.render(matrixStack, buffer, packedLight, packedOverlay);
		bipedChest.render(matrixStack, buffer, packedLight, packedOverlay);
		bipedLeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		bipedLeftArm.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}