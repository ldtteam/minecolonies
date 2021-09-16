// Made with Blockbench 4.0.0-beta.0
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelEntityFemaleAristocrat extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFemaleAristocrat(final ModelPart part)
    {
        super(part);
        hat.visible = false;
        leftArm.visible = false;
    }

    public static LayerDefinition createMesh()
    {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                                                                                   .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition nobleHair = bipedHead.addOrReplaceChild("nobleHair", CubeListBuilder.create().texOffs(64, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 9.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition hairdetailfront = bipedHead.addOrReplaceChild("hairdetailfront", CubeListBuilder.create().texOffs(68, 10).addBox(-4.5F, 8.0F, -4.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                                                                                          .texOffs(64, 10).mirror().addBox(3.4F, 8.0F, -4.0F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -7.5F, 0.7F));

        PartDefinition aristocratHair = bipedHead.addOrReplaceChild("aristocratHair", CubeListBuilder.create().texOffs(82, 2).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                                                                                        .texOffs(82, 8).addBox(-1.0F, -4.0F, -2.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                                                                                        .texOffs(72, 11).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 2.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                                                                                   .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                                                                        .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.249F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition SkirtTop = bipedBody.addOrReplaceChild("SkirtTop", CubeListBuilder.create(), PartPose.offset(-36.8F, 9.6F, -2.0F));

        PartDefinition p1 = SkirtTop.addOrReplaceChild("p1", CubeListBuilder.create().texOffs(0, 64).addBox(21.9002F, -3.5408F, 26.6909F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-3.0F, -2.0F, -3.0F, -0.1745F, 0.7854F, 0.0F));

        PartDefinition p2 = SkirtTop.addOrReplaceChild("p2", CubeListBuilder.create().texOffs(16, 64).addBox(-28.8701F, -3.0268F, 23.9091F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(-3.0F, -2.0F, 3.0F, -0.1745F, 2.3562F, 0.0F));

        PartDefinition p3 = SkirtTop.addOrReplaceChild("p3", CubeListBuilder.create().texOffs(32, 64).addBox(-27.3144F, 6.0334F, -27.3652F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(3.0F, -2.0F, 3.0F, -0.1745F, -2.3562F, 0.0F));

        PartDefinition p4 = SkirtTop.addOrReplaceChild("p4", CubeListBuilder.create().texOffs(48, 64).addBox(26.2843F, 5.5701F, -24.7793F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(2.7F, -2.0F, -3.0F, -0.1745F, -0.7854F, 0.0F));

        PartDefinition p5 = SkirtTop.addOrReplaceChild("p5", CubeListBuilder.create().texOffs(64, 64).addBox(0.0F, 6.4733F, -41.3257F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, -1.5708F, 0.0F));

        PartDefinition p6 = SkirtTop.addOrReplaceChild("p6", CubeListBuilder.create().texOffs(80, 64).addBox(-4.0F, -6.3767F, 31.1501F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 1.5708F, 0.0F));

        PartDefinition p7 = SkirtTop.addOrReplaceChild("p7", CubeListBuilder.create().texOffs(96, 64).addBox(-2.0F, -3.6F, -1.5F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(36.9F, 3.271F, 4.3333F, 0.2443F, 0.0F, 0.0F));

        PartDefinition p8 = SkirtTop.addOrReplaceChild("p8", CubeListBuilder.create().texOffs(112, 64).addBox(-2.2F, -3.3F, -3.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(-0.2F)), PartPose.offsetAndRotation(36.9F, 3.0243F, -0.2986F, -0.2967F, 0.0F, 0.0F));

        PartDefinition SkirtMiddle = bipedBody.addOrReplaceChild("SkirtMiddle", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

        PartDefinition Middle2_r1 = SkirtMiddle.addOrReplaceChild("Middle2_r1", CubeListBuilder.create().texOffs(48, 75).addBox(-6.0F, -2.0F, -6.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition Middle1_r1 = SkirtMiddle.addOrReplaceChild("Middle1_r1", CubeListBuilder.create().texOffs(0, 75).addBox(-6.0F, -2.0F, -6.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition SkirtBottom = bipedBody.addOrReplaceChild("SkirtBottom", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));

        PartDefinition bottom2_r1 = SkirtBottom.addOrReplaceChild("bottom2_r1", CubeListBuilder.create().texOffs(52, 91).addBox(-6.5F, -6.5F, -6.5F, 13.0F, 6.0F, 13.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition bottom1_r1 = SkirtBottom.addOrReplaceChild("bottom1_r1", CubeListBuilder.create().texOffs(0, 91).addBox(-6.5F, -6.5F, -6.5F, 13.0F, 6.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                                                                                           .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                                                                                         .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition foldedLeftArm = bipedBody.addOrReplaceChild("FoldedLeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition shoulderArm = foldedLeftArm.addOrReplaceChild("shoulderArm", CubeListBuilder.create().texOffs(56, 20).addBox(-1.01F, -2.01F, -2.0F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                                                                                      .texOffs(56, 30).addBox(-1.011F, -2.0F, -2.01F, 3.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition foreArm = foldedLeftArm.addOrReplaceChild("foreArm", CubeListBuilder.create().texOffs(72, 20).addBox(9.99F, -1.5F, -2.5F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.01F))
                                                                              .texOffs(72, 31).addBox(9.99F, -1.45F, -2.5F, 3.0F, 7.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-11.0F, 4.5F, 0.5F, -1.5708F, 0.0F, 0.0F));

        PartDefinition umbrella = foldedLeftArm.addOrReplaceChild("umbrella", CubeListBuilder.create().texOffs(96, 10).addBox(-0.5F, -20.5F, -0.5F, 1.0F, 21.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 3.5F, -6.0F, -0.6109F, 0.0F, 0.0F));

        PartDefinition umbrella_r1 = umbrella.addOrReplaceChild("umbrella_r1", CubeListBuilder.create().texOffs(92, 0).addBox(-4.5F, -0.5F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -18.8075F, 0.0138F, -0.1309F, 0.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                                                                                           .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                                                                                         .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
	}
}
