// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

public class MaleFletcherModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleFletcherModel(final ModelPart part)
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

        PartDefinition WoodsmanHat = bipedHead.addOrReplaceChild("WoodsmanHat", CubeListBuilder.create().texOffs(94, 1).addBox(-3.3822F, -0.7436F, -3.9128F, 8.0F, 2.0F, 9.0F, new CubeDeformation(0.11F)), PartPose.offsetAndRotation(-0.6178F, -7.3564F, 0.0128F, -0.0436F, 0.0F, 0.0F));

        PartDefinition feather2_r1 = WoodsmanHat.addOrReplaceChild("feather2_r1", CubeListBuilder.create().texOffs(110, 48).addBox(-1.1F, -6.6F, -2.5F, 0.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9822F, 0.0064F, 1.4053F, -0.1123F, -0.2882F, -0.2376F));

        PartDefinition middle_r1 = WoodsmanHat.addOrReplaceChild("middle_r1", CubeListBuilder.create().texOffs(98, 12).addBox(-3.5F, -3.6F, -3.62F, 7.0F, 3.0F, 8.0F, new CubeDeformation(0.175F)), PartPose.offsetAndRotation(0.6178F, 0.6564F, 0.2872F, 0.1571F, 0.0F, 0.0F));

        PartDefinition flapBase_r1 = WoodsmanHat.addOrReplaceChild("flapBase_r1", CubeListBuilder.create().texOffs(108, 23).addBox(-2.0F, -3.45F, -2.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.6178F, 1.1564F, -4.2128F, 0.2204F, -0.7732F, -0.1552F));

        PartDefinition flapRight_r1 = WoodsmanHat.addOrReplaceChild("flapRight_r1", CubeListBuilder.create().texOffs(102, 32).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0299F, 1.3564F, -6.0018F, 0.0F, -0.6981F, 0.0F));

        PartDefinition flapRight_r2 = WoodsmanHat.addOrReplaceChild("flapRight_r2", CubeListBuilder.create().texOffs(116, 32).addBox(0.001F, -1.5F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-3.3521F, 0.7664F, -4.0357F, 0.0F, -0.6981F, 0.0F));

        PartDefinition flapLeft_r1 = WoodsmanHat.addOrReplaceChild("flapLeft_r1", CubeListBuilder.create().texOffs(102, 40).addBox(-0.5F, -2.01F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.2655F, 1.3564F, -6.0018F, 0.0F, 0.6981F, 0.0F));

        PartDefinition flapLeft_r2 = WoodsmanHat.addOrReplaceChild("flapLeft_r2", CubeListBuilder.create().texOffs(116, 40).addBox(0.0F, -1.5F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(4.5869F, 0.7664F, -4.0351F, 0.0F, 0.6981F, 0.0F));

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
        head.getChild("WoodsmanHat").visible = displayHat(entity);
    }
}
