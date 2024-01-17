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

import static com.minecolonies.core.entity.ai.workers.production.agrilculture.EntityAIWorkFisherman.RENDER_META_FISH;
import static com.minecolonies.core.entity.ai.workers.production.agrilculture.EntityAIWorkFisherman.RENDER_META_ROD;

public class MaleFisherModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleFisherModel(final ModelPart part)
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

        PartDefinition FishermanCap = bipedHead.addOrReplaceChild("FishermanCap", CubeListBuilder.create().texOffs(88, 0).mirror().addBox(-5.0F, -2.1F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -7.0F, 0.0F, -0.1222F, 0.0F, 0.0F));

        PartDefinition detail1_r1 = FishermanCap.addOrReplaceChild("detail1_r1", CubeListBuilder.create().texOffs(118, 5).addBox(-1.6F, -1.6F, 0.3F, 4.0F, 4.0F, 1.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(5.0F, -2.9F, -5.6F, -0.5847F, -0.6981F, 0.0F));

        PartDefinition middle_r1 = FishermanCap.addOrReplaceChild("middle_r1", CubeListBuilder.create().texOffs(96, 15).mirror().addBox(-4.0F, -1.4F, -3.2F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.2F, 0.0F, -0.1047F, 0.0F, 0.0F));

        PartDefinition LeftLid_r1 = FishermanCap.addOrReplaceChild("LeftLid_r1", CubeListBuilder.create().texOffs(66, 5).mirror().addBox(-1.6F, -0.9F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.0F, -0.2F, 0.0F, 0.0F, 0.0F, -0.3491F));

        PartDefinition BackLid_r1 = FishermanCap.addOrReplaceChild("BackLid_r1", CubeListBuilder.create().texOffs(80, 12).mirror().addBox(-5.0F, -0.9F, -1.6F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.2F, -5.0F, 0.3491F, 0.0F, 0.0F));

        PartDefinition RightLid_r1 = FishermanCap.addOrReplaceChild("RightLid_r1", CubeListBuilder.create().texOffs(82, 15).mirror().addBox(-0.4F, -0.9F, -5.0F, 2.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(5.0F, -0.2F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition FrontLid_r1 = FishermanCap.addOrReplaceChild("FrontLid_r1", CubeListBuilder.create().texOffs(104, 12).mirror().addBox(-5.0F, -0.9F, -0.4F, 10.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.2F, 5.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition fishingPole = bipedBody.addOrReplaceChild("fishingPole", CubeListBuilder.create().texOffs(122, 25).mirror().addBox(-4.3F, -4.4F, 2.0F, 1.0F, 16.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(122, 42).mirror().addBox(-6.3F, 6.6F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(126, 26).mirror().addBox(-4.8F, -4.15F, 2.5F, 1.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7436F));

        PartDefinition lures = bipedBody.addOrReplaceChild("lures", CubeListBuilder.create().texOffs(108, 25).addBox(-5.0F, 0.0F, -3.41F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-1.0F))
          .texOffs(108, 32).addBox(-3.0F, 2.0F, -3.43F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-1.0F))
          .texOffs(108, 39).addBox(-1.0F, 4.0F, -3.45F, 5.0F, 5.0F, 2.0F, new CubeDeformation(-1.0F)), PartPose.offset(0.5F, 2.8F, 0.0F));

        PartDefinition fish = bipedBody.addOrReplaceChild("fish", CubeListBuilder.create().texOffs(92, 28).addBox(-1.5F, -4.6F, -2.5F, 3.0F, 8.0F, 5.0F, new CubeDeformation(-1.1F))
          .texOffs(85, 26).addBox(-1.0F, -5.7F, -2.0F, 2.0F, 3.0F, 4.0F, new CubeDeformation(-0.8F))
          .texOffs(78, 34).addBox(-1.0F, -3.1F, -3.0F, 2.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F))
          .texOffs(83, 36).addBox(-1.0F, 1.3F, -2.6F, 2.0F, 6.0F, 5.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-2.6F, 10.2F, -2.4F, 0.0F, 1.5708F, 0.0F));

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
        body.getChild("fishingPole").visible = entity.getRenderMetadata().contains(RENDER_META_ROD);
        body.getChild("fish").visible = entity.getRenderMetadata().contains(RENDER_META_FISH);
    }
}
