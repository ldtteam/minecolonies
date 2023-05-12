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
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.NotNull;

public class MaleHealerModel extends CitizenModel<AbstractEntityCitizen>
{

    public MaleHealerModel(final ModelPart part)
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

        PartDefinition Hat = bipedHead.addOrReplaceChild("Hat", CubeListBuilder.create().texOffs(90, 0).addBox(-4.5F, -0.6F, -6.5F, 9.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.6F, 0.0F, -0.0349F, 0.0F, 0.0F));

        PartDefinition hatpartbasecornerleftback_r1 = Hat.addOrReplaceChild("hatpartbasecornerleftback_r1", CubeListBuilder.create().texOffs(104, 8).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -0.1F, 4.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition hatpartbasecornerrightback_r1 = Hat.addOrReplaceChild("hatpartbasecornerrightback_r1", CubeListBuilder.create().texOffs(82, 8).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.1F, 4.5F, 0.0F, 1.5708F, 0.0F));

        PartDefinition hatpartbasecornerleftfront_r1 = Hat.addOrReplaceChild("hatpartbasecornerleftfront_r1", CubeListBuilder.create().texOffs(111, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -0.1F, -4.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition hatpartbasecornerrightfront_r1 = Hat.addOrReplaceChild("hatpartbasecornerrightfront_r1", CubeListBuilder.create().texOffs(111, 0).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.1F, -4.5F, 0.0F, 1.5708F, 0.0F));

        PartDefinition hatpartbaseright_r1 = Hat.addOrReplaceChild("hatpartbaseright_r1", CubeListBuilder.create().texOffs(86, 8).addBox(-3.5F, -0.5F, -2.0F, 7.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5F, -0.1F, 0.0F, 0.0F, 1.5708F, 0.0F));

        PartDefinition hatpartbaseleft_r1 = Hat.addOrReplaceChild("hatpartbaseleft_r1", CubeListBuilder.create().texOffs(64, 8).addBox(-3.5F, -0.5F, -2.0F, 7.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(100, 13).addBox(-3.5F, -3.3F, 2.0F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, -0.1F, 0.0F, 0.0F, -1.5708F, 0.0F));

        PartDefinition hatpartbaseback_r1 = Hat.addOrReplaceChild("hatpartbaseback_r1", CubeListBuilder.create().texOffs(90, 4).addBox(-4.5F, -0.5F, -1.5F, 9.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1F, 5.0F, 0.0F, 3.1416F, 0.0F));

        PartDefinition hatpartmiddleleft_r1 = Hat.addOrReplaceChild("hatpartmiddleleft_r1", CubeListBuilder.create().texOffs(74, 4).addBox(-3.5F, -1.56F, -0.5F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -2.0F, 0.0F, 0.0F, 1.5708F, -0.0873F));

        PartDefinition hatpartmiddleright_r1 = Hat.addOrReplaceChild("hatpartmiddleright_r1", CubeListBuilder.create().texOffs(91, 13).addBox(-3.5F, -1.56F, -0.5F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -2.0F, 0.0F, 0.0F, -1.5708F, 0.0873F));

        PartDefinition hatpartmiddleback_r1 = Hat.addOrReplaceChild("hatpartmiddleback_r1", CubeListBuilder.create().texOffs(74, 0).addBox(-3.5F, -1.5F, -0.5F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.1F, 3.5F, -0.0873F, 0.0F, 0.0F));

        PartDefinition hatpartmiddlefront_r1 = Hat.addOrReplaceChild("hatpartmiddlefront_r1", CubeListBuilder.create().texOffs(75, 13).addBox(-3.5F, -1.5F, -0.5F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.1F, -3.5F, 0.0873F, 0.0F, 0.0F));

        PartDefinition plagueMask = bipedHead.addOrReplaceChild("plagueMask", CubeListBuilder.create().texOffs(64, 17).mirror().addBox(-4.0F, -7.5F, -5.0F, 8.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(56, 3).mirror().addBox(-4.0F, -5.5F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(56, 0).mirror().addBox(-3.0F, -5.5F, -5.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
          .texOffs(60, 3).mirror().addBox(-1.0F, -5.5F, -5.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(62, 0).mirror().addBox(1.0F, -5.5F, -5.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false)
          .texOffs(66, 3).mirror().addBox(3.0F, -5.5F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(82, 17).mirror().addBox(-4.0F, -3.5F, -5.0F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition beak = plagueMask.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(70, 20).addBox(-1.0F, -1.5F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, -4.5F, 0.3054F, 0.0F, 0.0F));

        PartDefinition beak5_r1 = beak.addOrReplaceChild("beak5_r1", CubeListBuilder.create().texOffs(66, 22).addBox(-0.49F, -0.6186F, -4.7003F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.11F))
          .texOffs(83, 21).addBox(-0.99F, -0.6086F, -3.8003F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.08F)), PartPose.offsetAndRotation(-0.01F, -1.5014F, -1.9997F, 0.7505F, 0.0F, 0.0F));

        PartDefinition beak3_r1 = beak.addOrReplaceChild("beak3_r1", CubeListBuilder.create().texOffs(86, 21).addBox(-0.99F, 0.0014F, -3.0003F, 2.0F, 1.0F, 3.0F, new CubeDeformation(-0.001F)), PartPose.offsetAndRotation(-0.01F, -1.5014F, -1.9997F, 0.5672F, 0.0F, 0.0F));

        PartDefinition beak2_r1 = beak.addOrReplaceChild("beak2_r1", CubeListBuilder.create().texOffs(73, 20).addBox(-1.0F, -0.47F, -3.0F, 2.0F, 1.0F, 5.0F, new CubeDeformation(-0.005F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.192F, 0.0F, 0.0F));

        PartDefinition glasses = bipedHead.addOrReplaceChild("glasses", CubeListBuilder.create().texOffs(66, 41).addBox(-5.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(66, 49).addBox(-1.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, new CubeDeformation(-2.0F))
          .texOffs(82, 47).addBox(-1.0F, -0.6F, -2.55F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.3F))
          .texOffs(77, 57).addBox(-4.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F))
          .texOffs(63, 57).addBox(2.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.6F)), PartPose.offsetAndRotation(0.0F, -3.7F, -2.1F, 0.0873F, 0.0F, 0.0F));

        PartDefinition shortBeard = bipedHead.addOrReplaceChild("shortBeard", CubeListBuilder.create().texOffs(24, 0).addBox(-3.5F, -24.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
          .texOffs(24, 4).addBox(-3.5F, -23.0F, -4.0F, 7.0F, 3.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition bipedBody = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition coatCenter = bipedBody.addOrReplaceChild("coatCenter", CubeListBuilder.create().texOffs(100, 28).mirror().addBox(0.7F, 3.0F, -2.5F, 9.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-5.2F, -4.0F, 0.0F));

        PartDefinition bag = bipedBody.addOrReplaceChild("bag", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition strapback_r1 = bag.addOrReplaceChild("strapback_r1", CubeListBuilder.create().texOffs(104, 47).addBox(-0.5F, -15.3F, 0.0F, 1.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.5665F, 9.9968F, 4.2551F, 0.0913F, 0.0015F, 0.7592F));

        PartDefinition strapfrontb_r1 = bag.addOrReplaceChild("strapfrontb_r1", CubeListBuilder.create().texOffs(101, 47).addBox(-0.5F, -1.8F, 0.3F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.1229F, -1.2595F, 1.0612F, -1.5708F, 0.0F, 0.7854F));

        PartDefinition strapfronta_r1 = bag.addOrReplaceChild("strapfronta_r1", CubeListBuilder.create().texOffs(106, 47).addBox(-0.5F, -15.0F, 0.0F, 1.0F, 16.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.676F, 9.5395F, -4.0545F, -0.0611F, 0.0F, 0.7854F));

        PartDefinition bagLid_r1 = bag.addOrReplaceChild("bagLid_r1", CubeListBuilder.create().texOffs(72, 31).addBox(-1.9F, -3.0F, -3.95F, 3.0F, 2.0F, 8.0F, new CubeDeformation(0.03F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition bag_r1 = bag.addOrReplaceChild("bag_r1", CubeListBuilder.create().texOffs(86, 33).addBox(-1.9F, -3.0F, -3.95F, 3.0F, 6.0F, 8.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(-5.5F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0873F));

        PartDefinition bipedRightArm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-5.5F, 2.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

        PartDefinition rightarmcoat = bipedRightArm.addOrReplaceChild("rightarmcoat", CubeListBuilder.create().texOffs(108, 40).mirror().addBox(-3.1F, 3.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offset(-0.4F, -6.0F, 0.0F));

        PartDefinition bipedLeftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition leftarmcoat = bipedLeftArm.addOrReplaceChild("leftarmcoat", CubeListBuilder.create().texOffs(108, 52).mirror().addBox(1.7F, -2.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(-0.01F)).mirror(false), PartPose.offset(-3.2F, -1.0F, 0.0F));

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
        head.getChild("Hat").visible = displayHat(entity);
        head.getChild("plagueMask").visible = entity.getPose() != Pose.SLEEPING && isWorking(entity);
        body.getChild("bag").visible = entity.getPose() != Pose.SLEEPING  && isWorking(entity);
    }
}
