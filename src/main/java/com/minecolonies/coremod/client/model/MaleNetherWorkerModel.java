// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.coremod.entity.ai.citizen.miner.EntityAIStructureMiner.*;

public class MaleNetherWorkerModel extends CitizenModel<AbstractEntityCitizen>
{
    public MaleNetherWorkerModel(final ModelPart part)
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

        PartDefinition glasses = Head.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(98, 6).addBox(-4.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(108, 6).addBox(3.0F, -4.0F, -4.01F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(104, 0).addBox(4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.05F))
          .texOffs(88, 0).addBox(-4.01F, -4.0F, -4.0F, 0.0F, 1.0F, 8.0F, new CubeDeformation(0.05F))
          .texOffs(96, 9).addBox(-4.0F, -4.0F, 4.01F, 8.0F, 1.0F, 0.0F, new CubeDeformation(0.05F))
          .texOffs(101, 2).addBox(-1.0F, -4.1F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.21F)), PartPose.offset(0.0F, -0.2F, 0.0F));

        PartDefinition crystalLeft = glasses.addOrReplaceChild("crystalLeft", CubeListBuilder.create().texOffs(111, 2).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(111, 0).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(117, 1).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(107, 1).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(111, 5).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offset(4.0F, 1.5F, 0.0F));

        PartDefinition crystalRight = glasses.addOrReplaceChild("crystalRight", CubeListBuilder.create().texOffs(91, 2).addBox(-3.0F, -6.0F, -4.8F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(91, 0).addBox(-3.0F, -6.6F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(97, 1).addBox(-1.4F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(87, 1).addBox(-3.6F, -6.5F, -4.8F, 1.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
          .texOffs(91, 5).addBox(-3.0F, -4.4F, -4.8F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 1.5F, 0.0F));

        PartDefinition shortBeard = Head.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(15, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition backpack = Body.addOrReplaceChild("backpack", CubeListBuilder.create().texOffs(100, 48).addBox(-4.0F, -2.7F, 0.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.3F))
          .texOffs(116, 30).addBox(-5.4F, -1.6F, 1.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(104, 30).addBox(3.4F, -1.6F, 1.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.9F, 2.3F));

        PartDefinition roll_r1 = backpack.addOrReplaceChild("roll_r1", CubeListBuilder.create().texOffs(102, 24).addBox(-5.0F, -1.0F, -2.1F, 10.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.4F, 3.2F, 0.5585F, 0.0F, 0.0F));

        PartDefinition lid_r1 = backpack.addOrReplaceChild("lid_r1", CubeListBuilder.create().texOffs(100, 40).addBox(-4.0F, -2.2F, -0.5F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, -2.1F, 0.6F, 0.0436F, 0.0F, 0.0F));

        PartDefinition pick = backpack.addOrReplaceChild("pick", CubeListBuilder.create().texOffs(110, 16).addBox(-1.1F, -2.275F, 1.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(-0.1F))
          .texOffs(102, 14).addBox(-3.1F, -1.575F, 1.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(106, 16).addBox(1.9F, -0.575F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(106, 18).addBox(-4.1F, -0.575F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -2.125F, 4.7F, 0.0532F, 0.613F, 0.1697F));

        PartDefinition shovel = backpack.addOrReplaceChild("shovel", CubeListBuilder.create().texOffs(124, 17).addBox(-0.5F, -4.825F, -0.495F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.1F))
          .texOffs(120, 13).addBox(-0.5F, -4.825F, -1.495F, 1.0F, 1.0F, 3.0F, new CubeDeformation(-0.11F))
          .texOffs(116, 18).addBox(-0.5F, 0.575F, -1.495F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(114, 15).addBox(-0.5F, 3.575F, -1.015F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.1F, -1.875F, 6.095F, 0.1165F, 0.5925F, 0.1288F));

        PartDefinition torches = backpack.addOrReplaceChild("torches", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.9F, -0.8372F, 3.1844F, 0.3054F, 0.0F, 0.0F));

        PartDefinition torch2_r1 = torches.addOrReplaceChild("torch2_r1", CubeListBuilder.create().texOffs(100, 33).addBox(-0.5F, -2.3F, 0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -0.3628F, -0.2844F, -0.0436F, 0.0F, 0.0F));

        PartDefinition torch1_r1 = torches.addOrReplaceChild("torch1_r1", CubeListBuilder.create().texOffs(96, 33).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -0.3628F, -0.2844F, 0.0873F, 0.0F, 0.0F));

        PartDefinition stones = backpack.addOrReplaceChild("stones", CubeListBuilder.create().texOffs(78, 18).addBox(-0.3F, -4.5F, 0.4F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.6F))
          .texOffs(78, 24).mirror().addBox(-1.7F, -4.2F, 1.2F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.6F)).mirror(false), PartPose.offset(0.0F, -0.2F, 1.9F));

        PartDefinition stone5_r1 = stones.addOrReplaceChild("stone5_r1", CubeListBuilder.create().texOffs(90, 12).addBox(-1.8F, -1.5F, -1.6F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.7F)), PartPose.offsetAndRotation(-2.6F, -3.0F, 1.5F, 0.0866F, 0.0106F, -0.1217F));

        PartDefinition stone3_r1 = stones.addOrReplaceChild("stone3_r1", CubeListBuilder.create().texOffs(90, 24).mirror().addBox(-1.5F, -1.5F, -1.3F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.4F)).mirror(false), PartPose.offsetAndRotation(-1.8F, -3.0F, 2.2F, 0.0F, 0.0F, -0.0873F));

        PartDefinition stone1_r1 = stones.addOrReplaceChild("stone1_r1", CubeListBuilder.create().texOffs(90, 18).addBox(-1.2F, -1.5F, -1.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.3F)), PartPose.offsetAndRotation(2.2F, -2.8F, 2.2F, 0.0F, 0.0F, 0.1309F));

        PartDefinition Right_Arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition Left_Arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

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
        final ModelPart backpack = body.getChild("backpack");
        backpack.getChild("torches").visible = entity.getRenderMetadata().contains(RENDER_META_TORCH);
        backpack.getChild("shovel").visible = entity.getRenderMetadata().contains(RENDER_META_SHOVEL);
        backpack.getChild("pick").visible = entity.getRenderMetadata().contains(RENDER_META_PICKAXE);

        head.getChild("glasses").visible = isWorking(entity);
        backpack.visible = isWorking(entity);
    }
}
