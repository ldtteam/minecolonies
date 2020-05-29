package com.minecolonies.coremod.client.model.raiders;// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


public class VikingShieldmaiden extends EntityModel<Entity> {
	private final ModelRenderer Head;
	private final ModelRenderer Body;
	private final ModelRenderer Skirt;
	private final ModelRenderer Breast;
	private final ModelRenderer RightArm;
	private final ModelRenderer LeftArm;
	private final ModelRenderer RightLeg;
	private final ModelRenderer LeftLeg;
	private final ModelRenderer Shield;
	private final ModelRenderer Shield2;

	public VikingShieldmaiden() {
		textureWidth = 124;
		textureHeight = 64;

		Head = new ModelRenderer(this);
		Head.setRotationPoint(0.0F, 0.0F, 0.0F);
		Head.setTextureOffset(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
		Head.setTextureOffset(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

		Body = new ModelRenderer(this);
		Body.setRotationPoint(0.0F, 0.0F, 0.0F);
		Body.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
		Body.setTextureOffset(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

		Skirt = new ModelRenderer(this);
		Skirt.setRotationPoint(-0.95F, 12.25F, 0.5F);
		Body.addChild(Skirt);
		Skirt.setTextureOffset(67, 11).addBox(-3.5F, -0.25F, -3.5F, 9.0F, 6.0F, 6.0F, 0.0F, false);
		Skirt.setTextureOffset(67, 0).addBox(-3.0F, -1.0F, -3.0F, 8.0F, 6.0F, 5.0F, 0.0F, false);

		Breast = new ModelRenderer(this);
		Breast.setRotationPoint(0.0F, 24.0F, -6.0F);
		Body.addChild(Breast);
		setRotationAngle(Breast, -0.4363F, 0.0F, 0.0F);
		Breast.setTextureOffset(67, 49).addBox(-4.0F, -23.8558F, -5.7275F, 8.0F, 5.0F, 4.0F, 0.25F, false);

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

		Shield = new ModelRenderer(this);
		Shield.setRotationPoint(9.25F, 15.0F, -4.75F);
		Shield.setTextureOffset(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
		Shield.setTextureOffset(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
		Shield.setTextureOffset(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);

		Shield2 = new ModelRenderer(this);
		Shield2.setRotationPoint(5.0F, 12.0F, 4.0F);
		setRotationAngle(Shield2, 0.0F, -1.5708F, 0.0F);
		Shield2.setTextureOffset(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F, 0.0F, false);
		Shield2.setTextureOffset(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
		Shield2.setTextureOffset(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F, 0.0F, false);
	}

	@Override
	public void setRotationAngles(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch){
		//previously the render function, render code was moved to a method below
	}

	@Override
	public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha){
		Head.render(matrixStack, buffer, packedLight, packedOverlay);
		Body.render(matrixStack, buffer, packedLight, packedOverlay);
		RightArm.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftArm.render(matrixStack, buffer, packedLight, packedOverlay);
		RightLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		LeftLeg.render(matrixStack, buffer, packedLight, packedOverlay);
		Shield.render(matrixStack, buffer, packedLight, packedOverlay);
		Shield2.render(matrixStack, buffer, packedLight, packedOverlay);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}