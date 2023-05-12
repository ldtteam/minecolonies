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
import org.jetbrains.annotations.NotNull;

public class MaleNobleModel extends CitizenModel<AbstractEntityCitizen>
{
    public MaleNobleModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition SailorHat1 = bipedHead.addOrReplaceChild("SailorHat1", CubeListBuilder.create().texOffs(102, 0).addBox(-4.0F, 1.33F, -5.318F, 8.0F, 0.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(112, 32).addBox(-4.0F, -2.65F, -5.318F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
          .texOffs(80, 0).addBox(-4.0F, -0.85F, -4.218F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.2F))
          .texOffs(88, 10).addBox(-3.0F, -2.05F, -3.218F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
          .texOffs(106, 44).addBox(-5.0F, -2.9F, 0.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.55F, 0.318F, -0.1047F, 0.0F, 0.0F));

        PartDefinition feather2_r1 = SailorHat1.addOrReplaceChild("feather2_r1", CubeListBuilder.create().texOffs(110, 48).addBox(-1.5F, -6.3F, -2.3F, 0.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.6F, -1.7F, 1.0F, -0.2182F, -0.7418F, 0.0F));

        PartDefinition flap_r1 = SailorHat1.addOrReplaceChild("flap_r1", CubeListBuilder.create().texOffs(112, 40).addBox(0.0F, -4.0F, -5.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
          .texOffs(101, 21).addBox(0.0F, 0.0F, -5.0F, 8.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0942F, 1.35F, -2.2385F, 0.0F, -0.9163F, 0.0F));

        PartDefinition flap_r2 = SailorHat1.addOrReplaceChild("flap_r2", CubeListBuilder.create().texOffs(90, 32).addBox(7.9668F, -4.0F, -5.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0739F, 1.35F, -2.2121F, 0.0F, -0.9163F, 0.0F));

        PartDefinition flap_r3 = SailorHat1.addOrReplaceChild("flap_r3", CubeListBuilder.create().texOffs(112, 36).addBox(-7.0F, -4.0F, -5.0F, 8.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
          .texOffs(101, 10).addBox(-7.0F, -0.001F, -5.0F, 8.0F, 0.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.483F, 1.35F, -1.4451F, 0.0F, 0.9163F, 0.0F));

        PartDefinition flap_r4 = SailorHat1.addOrReplaceChild("flap_r4", CubeListBuilder.create().texOffs(90, 17).addBox(-7.045F, -4.0F, -5.0F, 0.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.455F, 1.35F, -1.4816F, 0.0F, 0.9163F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
	}

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.getChild("SailorHat1").visible = displayHat(entity);
    }
}
