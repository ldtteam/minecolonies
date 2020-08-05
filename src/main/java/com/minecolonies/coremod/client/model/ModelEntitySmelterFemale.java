// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports


public class ModelEntitySmelterFemale extends CitizenModel<AbstractEntityCitizen> {
	bipedChestModelRenderer toolHandle1;
	bipedChestModelRenderer toolHandle2;
	bipedChestModelRenderer pocket;
	bipedChestModelRenderer bipedChest;
	bipedChestModelRenderer headDetail;
	bipedChestModelRenderer ponytailB;
	bipedChestModelRenderer ponytailT;

	public ModelEntitySmelterFemale() {
		textureWidth = 128;
		textureHeight = 64;

		bipedRightArm = new ModelRenderer(this);
		bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		bipedRightArm.setTextureOffset(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedLeftArm = new ModelRenderer(this);
		bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		bipedLeftArm.setTextureOffset(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedRightLeg = new ModelRenderer(this);
		bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		bipedRightLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);

		bipedLeftLeg = new ModelRenderer(this);
		bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		bipedLeftLeg.setTextureOffset(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, true);

		bipedBody = new ModelRenderer(this);
		bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.setTextureOffset(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, true);

		toolHandle1 = new ModelRenderer(this);
		toolHandle1.setRotationPoint(-2.0F, 8.0F, -3.0F);
		bipedBody.addChild(toolHandle1);
		toolHandle1.setTextureOffset(0, 32).addBox(0.0F, 0.0F, 0.0F, 4.0F, 3.0F, 1.0F, 0.0F, true);

		toolHandle2 = new ModelRenderer(this);
		toolHandle2.setRotationPoint(-1.0F, 6.0F, -3.0F);
		bipedBody.addChild(toolHandle2);
		toolHandle2.setTextureOffset(10, 32).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

		pocket = new ModelRenderer(this);
		pocket.setRotationPoint(1.0F, 6.0F, -3.0F);
		bipedBody.addChild(pocket);
		pocket.setTextureOffset(10, 32).addBox(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F, 0.0F, true);

		bipedChest = new ModelRenderer(this);
		bipedChest.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedBody.addChild(bipedChest);
		setRotationAngle(bipedChest, -0.5934F, 0.0F, 0.0F);
		bipedChest.setTextureOffset(0, 55).addBox(-3.5F, 2.7F, -0.6F, 7.0F, 3.0F, 4.0F, 0.0F, true);

		bipedHead = new ModelRenderer(this);
		bipedHead.setRotationPoint(0.0F, -1.0F, 0.0F);
		bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, true);

		headDetail = new ModelRenderer(this);
		headDetail.setRotationPoint(0.0F, 25.0F, 0.0F);
		bipedHead.addChild(headDetail);
		headDetail.setTextureOffset(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, true);

		ponytailB = new ModelRenderer(this);
		ponytailB.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(ponytailB);
		setRotationAngle(ponytailB, 0.1047F, 0.0F, 0.0F);
		ponytailB.setTextureOffset(80, 40).addBox(-0.5F, 2.4F, 3.7F, 1.0F, 5.0F, 1.0F, 0.0F, true);

		ponytailT = new ModelRenderer(this);
		ponytailT.setRotationPoint(0.0F, 0.0F, 0.0F);
		bipedHead.addChild(ponytailT);
		setRotationAngle(ponytailT, 0.2269F, 0.0F, 0.0F);
		ponytailT.setTextureOffset(79, 33).addBox(-1.0F, -2.0F, 3.4F, 2.0F, 5.0F, 1.0F, 0.0F, true);
	}

	public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z) {
		modelRenderer.rotateAngleX = x;
		modelRenderer.rotateAngleY = y;
		modelRenderer.rotateAngleZ = z;
	}
}