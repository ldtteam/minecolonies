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

import static com.minecolonies.core.entity.ai.citizen.deliveryman.EntityAIWorkDeliveryman.RENDER_META_BACKPACK;

public class MaleCourierModel extends CitizenModel<AbstractEntityCitizen>
{
    public MaleCourierModel(ModelPart root)
    {
        super(root);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -3.6148F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -7.0F, -3.6148F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -0.1F, -2.3F, 0.1309F, 0.0F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -23.1F, -5.9F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -22.1F, -5.9F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.1F, 2.3F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, -11.35F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, -11.35F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition backpack = bipedBody.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(100, 50).addBox(-4.0F, -1.7F, 0.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, -7.4F, 2.3F));

        PartDefinition lid_r1 = backpack.addOrReplaceChild("lid_r1", CubeListBuilder.create().texOffs(100, 42).addBox(-4.0F, -1.2F, -0.5F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, -2.1F, 0.6F, 0.0436F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -1.5F, -2.3F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -1.5F, -2.3F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.5F, -1.6F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -1.5F, -2.3F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -1.5F, -2.3F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.5F, -1.6F));

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
        body.getChild("backpack").visible = showBackPack;
        leftArm.y = showBackPack ? 2f : 2.5f;
        rightArm.y = showBackPack ? 2f : 2.5f;
    }
}
