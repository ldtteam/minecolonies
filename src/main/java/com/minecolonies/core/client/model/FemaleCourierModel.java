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

import static com.minecolonies.core.entity.ai.workers.service.EntityAIWorkDeliveryman.RENDER_META_BACKPACK;

public class FemaleCourierModel extends CitizenModel<AbstractEntityCitizen>
{
    public FemaleCourierModel(ModelPart root)
    {
        super(root);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create()
          .texOffs(0, 0).addBox(-4.0F, -7.0F, -3.6148F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -7.0F, -3.6148F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.2F, -2.1F, 0.0873F, 0.0F, 0.0F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 1.3F, 4.9F, 0.48F, 0.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).addBox(0.0F, 1.0F, -0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-0.5F, -1.502F, 3.1709F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).addBox(0.0F, 1.0F, -0.3F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -4.502F, 0.3709F, 0.5577F, 0.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -11.35F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, -11.35F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition backpack = bipedBody.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(100, 50).addBox(-4.0F, -1.7F, 0.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, -6.9F, 2.3F));

        PartDefinition lid_r1 = backpack.addOrReplaceChild("lid_r1", CubeListBuilder.create().texOffs(100, 42).addBox(-4.0F, -1.2F, -0.5F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, -2.1F, 0.6F, 0.0436F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 2.2938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 2.2938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, -9.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -1.8F, -2.3F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.0F, -1.8F, -2.3F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.5F, -1.6F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -1.8F, -2.3F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -1.8F, -2.3F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.5F, -1.6F));

        PartDefinition bipedRightLeg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public float getActualRotation(@NotNull final AbstractEntityCitizen entity)
    {
        return entity.getPose() == Pose.SLEEPING || !entity.getRenderMetadata().contains(RENDER_META_BACKPACK) ? 0 : 0.1745F;
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        body.y += 12;

        final boolean showBackPack = entity.getPose() != Pose.SLEEPING && entity.getRenderMetadata().contains(RENDER_META_BACKPACK);
        head.z = showBackPack ? -2.1f : 0;
        head.getChild("Ponytail").xRot = showBackPack ? 0.48f : 0.1f;
        head.getChild("Ponytail").z = showBackPack ? 4.9f : 3f;
        body.getChild("backpack").visible = showBackPack;
        leftArm.y = showBackPack ? 2f : 2.5f;
        rightArm.y = showBackPack ? 2f : 2.5f;
    }
}
