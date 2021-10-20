// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityRabbitHerderFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityRabbitHerderFemale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
          , PartPose.offset(2.0F, 12.0F, 0.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
          CubeListBuilder.create()
            .texOffs(32, 0).addBox(-4.0F, -32.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
            .texOffs(96, 56).addBox(-4.25F, -25.0F, 1.02F, 0.25F, 3.25F, 2.0F)
            .texOffs(64, 45).addBox(4.0F, -25.0F, 1.02F, 0.25F, 3.25F, 2.0F)
            .texOffs(102, 62).addBox(-4.25F, -26.0F, 1.02F, 0.25F, 1.0F, 1.0F)
            .texOffs(69, 50).addBox(4.0F, -26.0F, 1.02F, 0.25F, 1.0F, 1.0F)
            .texOffs(53, 52).addBox(4.0F, -26.0F, 3.02F, 0.35F, 5.0F, 1.0F)
            .texOffs(61, 51).addBox(4.0F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F)
            .texOffs(48, 46).addBox(2.75F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F)
            .texOffs(54, 46).addBox(4.0F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F)
            .texOffs(59, 46).addBox(4.0F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F)
            .texOffs(69, 47).addBox(4.0F, -26.0F, 2.02F, 0.35F, 1.0F, 1.0F)
            .texOffs(102, 59).addBox(-4.35F, -26.0F, 2.02F, 0.35F, 1.0F, 1.0F).mirror()
            .texOffs(101, 46).addBox(-4.35F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F).mirror()
            .texOffs(102, 53).addBox(-4.35F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F).mirror()
            .texOffs(29, 46).addBox(-4.35F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F).mirror()
            .texOffs(35, 46).addBox(-2.75F, -32.25F, -4.48F, 5.5F, 2.25F, 1.0F).mirror()
            .texOffs(29, 52).addBox(-4.0F, -32.25F, -3.48F, 8.0F, 2.25F, 7.5F).mirror()
            .texOffs(71, 54).addBox(-4.25F, -32.25F, 4.02F, 8.5F, 6.75F, 0.25F).mirror()
            .texOffs(89, 46).addBox(3.75F, -25.5F, 4.02F, 0.5F, 5.5F, 0.25F).mirror()
            .texOffs(89, 52).addBox(-4.25F, -25.5F, 4.02F, 0.5F, 5.5F, 0.25F).mirror()
            .texOffs(73, 46).addBox(-3.75F, -25.5F, 4.02F, 7.5F, 6.75F, 0.25F).mirror()
            .texOffs(92, 45).addBox(-4.35F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F).mirror()
            .texOffs(92, 56).addBox(-4.35F, -26.0F, 3.02F, 0.35F, 5.0F, 1.0F).mirror()
          , PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F).mirror()
            .texOffs(21, 32).addBox(-1.5F, 5.0F, -2.25F, 4.0F, 0.75F, 4.5F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F)
            .texOffs(1, 32).addBox(-2.5F, 5.0F, -2.25F, 4.0F, 0.75F, 4.5F).mirror()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
            .texOffs(5, 53).addBox(3.0F, 0.0F, -3.25F, 1.0F, 10.0F, 1.25F)
            .texOffs(0, 53).addBox(-4.0F, 0.0F, -3.25F, 1.0F, 10.0F, 1.25F)
            .texOffs(5, 47).addBox(-4.0F, 10.0F, -6.0F, 8.0F, 1.0F, 4.0F)
            .texOffs(10, 53).addBox(-4.0F, 9.0F, -6.0F, 8.0F, 1.0F, 1.0F)
            .texOffs(10, 59).addBox(-4.0F, 9.0F, -5.0F, 1.0F, 1.0F, 1.75F)
            .texOffs(10, 56).addBox(3.0F, 9.0F, -5.0F, 1.0F, 1.0F, 1.75F)
            .texOffs(62, 34).addBox(-3.0F, 9.5F, -5.0F, 6.0F, 0.5F, 3.0F)
            .texOffs(12, 58).addBox(-3.0F, -0.25F, -2.25F, 6.0F, 0.75F, 4.5F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(41, 32).addBox(-3.0F, -1.0F, -2.0F, 6.0F, 3.0F, 3.0F)
          , PartPose.offsetAndRotation(0.0F, 2.0F, -2.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition carrot1Definition = bodyDefinition.addOrReplaceChild("carrot1",
          CubeListBuilder.create()
            .texOffs(63, 25).addBox(14.8647F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(62, 24).addBox(14.6948F, 5.1066F, -33.73F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(62, 25).addBox(14.8647F, 5.1066F, -33.73F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 24).addBox(14.6948F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(64, 24).addBox(14.6948F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 24).addBox(14.6948F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(62, 23).addBox(14.525F, 5.1066F, -33.73F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 23).addBox(14.525F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 23).addBox(14.525F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 23).addBox(14.525F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 23).addBox(14.525F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 22).addBox(14.3552F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 22).addBox(14.3552F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 22).addBox(14.3552F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 22).addBox(14.3552F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 21).addBox(14.1853F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(64, 21).addBox(14.1853F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 21).addBox(14.1853F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 21).addBox(14.1853F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 21).addBox(14.1853F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 22).addBox(14.3552F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 22).addBox(14.3552F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 21).addBox(14.1853F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 21).addBox(14.1853F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 20).addBox(14.0155F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 20).addBox(14.0155F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 20).addBox(14.0155F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 20).addBox(14.0155F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 20).addBox(14.0155F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 20).addBox(14.0155F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(64, 20).addBox(14.0155F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(63, 19).addBox(13.8457F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 19).addBox(13.8457F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 19).addBox(13.8457F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 19).addBox(13.8457F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 19).addBox(13.8457F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 19).addBox(13.8457F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 19).addBox(13.8457F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 19).addBox(13.8457F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 20).addBox(14.0155F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 20).addBox(14.0155F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 19).addBox(13.8457F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 19).addBox(13.8457F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 18).addBox(13.6759F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 18).addBox(13.6759F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(74, 18).addBox(13.6759F, 5.1066F, -31.692F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(75, 18).addBox(13.6759F, 5.1066F, -31.5222F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(74, 17).addBox(13.506F, 5.1066F, -31.692F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 17).addBox(13.506F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 17).addBox(13.506F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 17).addBox(13.506F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 15).addBox(13.3362F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 16).addBox(13.3362F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 16).addBox(13.3362F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 14).addBox(12.9966F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 14).addBox(12.9966F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 15).addBox(13.1664F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 15).addBox(13.1664F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 14).addBox(12.9966F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 14).addBox(12.9966F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 15).addBox(13.1664F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 16).addBox(13.3362F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 17).addBox(13.506F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 18).addBox(13.6759F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(71, 18).addBox(13.6759F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 18).addBox(13.6759F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 17).addBox(13.506F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 16).addBox(13.3362F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 15).addBox(13.1664F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 14).addBox(12.9966F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 18).addBox(13.6759F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 18).addBox(13.6759F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 18).addBox(13.6759F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 18).addBox(13.6759F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 17).addBox(13.506F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 17).addBox(13.506F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 17).addBox(13.506F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 17).addBox(13.506F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 16).addBox(13.3362F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 16).addBox(13.3362F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 16).addBox(13.3362F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 15).addBox(13.1664F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 15).addBox(13.1664F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 15).addBox(13.1664F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 14).addBox(12.9966F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 14).addBox(12.9966F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(66, 14).addBox(12.9966F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(65, 14).addBox(12.9966F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 13).addBox(12.8267F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 13).addBox(12.8267F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(70, 13).addBox(12.8267F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(72, 13).addBox(12.8267F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(73, 13).addBox(12.8267F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(69, 12).addBox(12.6569F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(68, 12).addBox(12.6569F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F)
            .texOffs(67, 12).addBox(12.6569F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F)
          , PartPose.offset(-15.5F, 4.0F, 29.0F));

        PartDefinition carrot2Definition = bodyDefinition.addOrReplaceChild("carrot2",
          CubeListBuilder.create()
            .texOffs(63, 25).addBox(-15.0345F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(62, 24).addBox(-14.8647F, 5.1066F, -33.73F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(62, 25).addBox(-15.0345F, 5.1066F, -33.73F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 24).addBox(-14.8647F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(64, 24).addBox(-14.8647F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 24).addBox(-14.8647F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(62, 23).addBox(-14.6948F, 5.1066F, -33.73F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 23).addBox(-14.6948F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 23).addBox(-14.6948F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 23).addBox(-14.6948F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 23).addBox(-14.6948F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 22).addBox(-14.525F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 22).addBox(-14.525F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 22).addBox(-14.525F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 22).addBox(-14.525F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 21).addBox(-14.3552F, 5.1066F, -33.5601F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(64, 21).addBox(-14.3552F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 21).addBox(-14.3552F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 21).addBox(-14.3552F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 21).addBox(-14.3552F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 22).addBox(-14.525F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 22).addBox(-14.525F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 21).addBox(-14.3552F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 21).addBox(-14.3552F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 20).addBox(-14.1853F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 20).addBox(-14.1853F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 20).addBox(-14.1853F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 20).addBox(-14.1853F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 20).addBox(-14.1853F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 20).addBox(-14.1853F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(64, 20).addBox(-14.1853F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(63, 19).addBox(-14.0155F, 5.1066F, -33.3903F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 19).addBox(-14.0155F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 19).addBox(-14.0155F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 19).addBox(-14.0155F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 19).addBox(-14.0155F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 19).addBox(-14.0155F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 19).addBox(-14.0155F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 19).addBox(-14.0155F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 20).addBox(-14.1853F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 20).addBox(-14.1853F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 19).addBox(-14.0155F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 19).addBox(-14.0155F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 18).addBox(-13.8457F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 18).addBox(-13.8457F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(74, 18).addBox(-13.8457F, 5.1066F, -31.692F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(75, 18).addBox(-13.8457F, 5.1066F, -31.5222F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(74, 17).addBox(-13.6759F, 5.1066F, -31.692F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 17).addBox(-13.6759F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 17).addBox(-13.6759F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 17).addBox(-13.6759F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 15).addBox(-13.506F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 16).addBox(-13.506F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 16).addBox(-13.506F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 14).addBox(-13.1664F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 14).addBox(-13.1664F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 15).addBox(-13.3362F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 15).addBox(-13.3362F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 14).addBox(-13.1664F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 14).addBox(-13.1664F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 15).addBox(-13.3362F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 16).addBox(-13.506F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 17).addBox(-13.6759F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 18).addBox(-13.8457F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(71, 18).addBox(-13.8457F, 5.1066F, -32.2015F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 18).addBox(-13.8457F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 17).addBox(-13.6759F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 16).addBox(-13.506F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 15).addBox(-13.3362F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 14).addBox(-13.1664F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 18).addBox(-13.8457F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 18).addBox(-13.8457F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 18).addBox(-13.8457F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 18).addBox(-13.8457F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 17).addBox(-13.6759F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 17).addBox(-13.6759F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 17).addBox(-13.6759F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 17).addBox(-13.6759F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 16).addBox(-13.506F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 16).addBox(-13.506F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 16).addBox(-13.506F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 15).addBox(-13.3362F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 15).addBox(-13.3362F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 15).addBox(-13.3362F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 14).addBox(-13.1664F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 14).addBox(-13.1664F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(66, 14).addBox(-13.1664F, 5.1066F, -33.0507F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(65, 14).addBox(-13.1664F, 5.1066F, -33.2205F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 13).addBox(-12.9966F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 13).addBox(-12.9966F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(70, 13).addBox(-12.9966F, 5.1066F, -32.3713F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(72, 13).addBox(-12.9966F, 5.1066F, -32.0317F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(73, 13).addBox(-12.9966F, 5.1066F, -31.8619F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(69, 12).addBox(-12.8267F, 5.1066F, -32.5412F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(68, 12).addBox(-12.8267F, 5.1066F, -32.711F, 0.1698F, 0.1698F, 0.1698F).mirror()
            .texOffs(67, 12).addBox(-12.8267F, 5.1066F, -32.8808F, 0.1698F, 0.1698F, 0.1698F).mirror()
          , PartPose.offset(15.5F, 4.0F, 29.0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
