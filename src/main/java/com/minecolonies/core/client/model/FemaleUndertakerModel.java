// Made with Blockbench 3.8.4
// Exported for Minecraft version 1.15 - 1.16
// Paste this class into your mod and generate all required imports
package com.minecolonies.core.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

public class FemaleUndertakerModel extends CitizenModel<AbstractEntityCitizen>
{
    public FemaleUndertakerModel(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition bipedHead = partDefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition TopHat = bipedHead.addOrReplaceChild("TopHat", CubeListBuilder.create().texOffs(72, 0).addBox(-3.9664F, -3.1854F, -5.0F, 8.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
          .texOffs(98, 0).addBox(-2.9664F, -6.583F, -2.9551F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
          .texOffs(72, 12).mirror().addBox(-2.9664F, -7.3F, -4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(-0.101F)).mirror(false)
          .texOffs(72, 18).addBox(2.5336F, -7.3F, -3.5F, 1.0F, 6.0F, 7.0F, new CubeDeformation(-0.102F))
          .texOffs(88, 18).addBox(-3.4664F, -7.3F, -3.5F, 1.0F, 6.0F, 7.0F, new CubeDeformation(-0.103F))
          .texOffs(86, 12).addBox(-2.9664F, -7.3F, 3.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.0F, -5.6F, 0.0F, -0.0611F, 0.0F, 0.0F));

        PartDefinition hatpartlowleft = TopHat.addOrReplaceChild("hatpartlowleft", CubeListBuilder.create().texOffs(100, 7).addBox(-2.3F, -0.7F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(-3.1F, -2.2F, 0.0F, 0.0F, 0.0F, 0.48F));

        PartDefinition hatpartlowright = TopHat.addOrReplaceChild("hatpartlowright", CubeListBuilder.create().texOffs(104, 19).addBox(0.4466F, 1.4993F, -5.0F, 2.0F, 2.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(6.0079F, -4.9714F, 0.0F, 0.0F, 0.0F, 1.0908F));

        PartDefinition Ponytail = bipedHead.addOrReplaceChild("Ponytail", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition ponyTailTip_r1 = Ponytail.addOrReplaceChild("ponyTailTip_r1", CubeListBuilder.create().texOffs(88, 55).mirror().addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offsetAndRotation(-0.5F, -25.0F, 4.8F, 0.2231F, 0.0F, 0.0F));

        PartDefinition ponytailBase_r1 = Ponytail.addOrReplaceChild("ponytailBase_r1", CubeListBuilder.create().texOffs(86, 48).mirror().addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, -28.0F, 2.0F, 0.5577F, 0.0F, 0.0F));

        PartDefinition HairExtension = bipedHead.addOrReplaceChild("HairExtension", CubeListBuilder.create().texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition bipedBody = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition breast = bipedBody.addOrReplaceChild("breast", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
          .texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(-1.0F, 3.0F, 4.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bipedRightArm = partDefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bipedLeftArm = partDefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition bipedRightLeg = partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
          .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition bipedLeftLeg = partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
          .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityCitizen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.getChild("TopHat").visible = displayHat(entity);
    }
}
