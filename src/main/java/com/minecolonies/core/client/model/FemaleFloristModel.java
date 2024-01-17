// Made with Blockbench 4.0.0-beta.0
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.core.entity.ai.citizen.florist.EntityAIWorkFlorist.RENDER_META_FLOWERS;

public class FemaleFloristModel extends CitizenModel<AbstractEntityCitizen>
{
    public FemaleFloristModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition HairExtension = Head.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition Ponytail = Head.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition WoodsmanHat = Head.addOrReplaceChild("WoodsmanHat", CubeListBuilder.create().texOffs(94, 1).addBox(-3.3822F, -0.7436F, -3.9128F, 8.0F, 2.0F, 9.0F, new CubeDeformation(0.21F)), PartPose.offsetAndRotation(-0.6178F, -7.3564F, 0.0128F, -0.0436F, 0.0F, 0.0F));

        PartDefinition middle_r1 = WoodsmanHat.addOrReplaceChild("middle_r1", CubeListBuilder.create().texOffs(98, 12).addBox(-3.5F, -3.6F, -3.62F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.175F)), PartPose.offsetAndRotation(0.6178F, 0.6564F, 0.2872F, 0.1571F, 0.0F, 0.0F));

        PartDefinition flapBase_r1 = WoodsmanHat.addOrReplaceChild("flapBase_r1", CubeListBuilder.create().texOffs(108, 23).addBox(-2.0F, -3.45F, -2.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.6178F, 1.1564F, -4.2128F, 0.2204F, -0.7732F, -0.1552F));

        PartDefinition flapRight_r1 = WoodsmanHat.addOrReplaceChild("flapRight_r1", CubeListBuilder.create().texOffs(102, 32).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0299F, 1.3564F, -6.0018F, 0.0F, -0.6981F, 0.0F));

        PartDefinition flapRight_r2 = WoodsmanHat.addOrReplaceChild("flapRight_r2", CubeListBuilder.create().texOffs(116, 32).addBox(0.001F, -1.5F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(-3.3521F, 0.7664F, -4.0357F, 0.0F, -0.6981F, 0.0F));

        PartDefinition flapLeft_r1 = WoodsmanHat.addOrReplaceChild("flapLeft_r1", CubeListBuilder.create().texOffs(102, 40).addBox(-0.5F, -2.01F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2655F, 1.3564F, -6.0018F, 0.0F, 0.6981F, 0.0F));

        PartDefinition flapLeft_r2 = WoodsmanHat.addOrReplaceChild("flapLeft_r2", CubeListBuilder.create().texOffs(116, 40).addBox(0.0F, -1.5F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.2F)), PartPose.offsetAndRotation(4.5869F, 0.7664F, -4.0351F, 0.0F, 0.6981F, 0.0F));

        PartDefinition flowers = WoodsmanHat.addOrReplaceChild("flowers", CubeListBuilder.create(), PartPose.offset(-3.4132F, -1.5622F, -1.6245F));

        PartDefinition flower3_r1 = flowers.addOrReplaceChild("flower3_r1", CubeListBuilder.create().texOffs(74, 6).addBox(0.0F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.1606F, 0.1409F, 2.8047F, 0.0355F, 0.014F, 0.1216F));

        PartDefinition flower2_r1 = flowers.addOrReplaceChild("flower2_r1", CubeListBuilder.create().texOffs(74, 3).addBox(0.1F, -1.5F, -1.5F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2812F, -0.0196F, 0.0102F, 0.4719F, 0.014F, 0.1216F));

        PartDefinition flower1_r1 = flowers.addOrReplaceChild("flower1_r1", CubeListBuilder.create().texOffs(74, 0).addBox(0.2F, -1.1F, -1.9F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1691F, -0.5813F, -2.5883F, -0.0752F, -0.5187F, 0.1509F));

        PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = Body.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition dress = Body.addOrReplaceChild("dress", CubeListBuilder.create().texOffs(64, 30).addBox(-5.0F, -11.0F, -4.0F, 10.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
          .texOffs(66, 22).addBox(-5.0F, -12.0F, -3.0F, 10.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
          .texOffs(70, 16).addBox(-4.0F, -13.0F, -2.0F, 8.0F, 1.0F, 5.0F, new CubeDeformation(0.251F)), PartPose.offset(0.0F, 24.0F, -0.5F));

        PartDefinition Right_Arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Left_Arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition Right_Leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition Left_Leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        body.getChild("dress").visible = entity.getPose() != Pose.SLEEPING;
        head.getChild("WoodsmanHat").visible = displayHat(entity);
        head.getChild("WoodsmanHat").getChild("flowers").visible = entity.getRenderMetadata().contains(RENDER_META_FLOWERS);
    }
}
