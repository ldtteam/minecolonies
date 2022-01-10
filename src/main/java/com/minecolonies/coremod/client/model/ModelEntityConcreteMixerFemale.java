// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityConcreteMixerFemale extends CitizenModel<AbstractEntityCitizen>
{
  public ModelEntityConcreteMixerFemale(final ModelPart part)
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
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation( 0.5F))
        , PartPose.offset(0.0F, 0.0F, 1.0F));

      PartDefinition hairDefinition = headDefinition.addOrReplaceChild("hair",
        CubeListBuilder.create()
          .texOffs(70, 49).addBox(-4.35F, -26.0F, 3.02F, 0.35F, 6.0F, 1.0F).mirror()
          .texOffs(74, 46).addBox(-4.25F, -25.0F, 1.02F, 0.25F, 3.25F, 2.0F)
          .texOffs(28, 56).addBox(4.0F, -25.0F, 1.02F, 0.25F, 3.25F, 2.0F)
          .texOffs(16, 56).addBox(-4.25F, -26.0F, 1.02F, 0.25F, 1.0F, 1.0F)
          .texOffs(12, 56).addBox(4.0F, -26.0F, 1.02F, 0.25F, 1.0F, 1.0F)
          .texOffs(29, 45).addBox(4.0F, -26.0F, 3.02F, 0.35F, 6.0F, 1.0F)
          .texOffs(51, 46).addBox(4.0F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F)
          .texOffs(12, 60).addBox(2.75F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F)
          .texOffs(18, 58).addBox(4.0F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F)
          .texOffs(23, 57).addBox(4.0F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F)
          .texOffs(26, 46).addBox(4.0F, -26.0F, 2.02F, 0.35F, 1.0F, 1.0F)
          .texOffs(24, 49).addBox(-4.35F, -26.0F, 2.02F, 0.35F, 1.0F, 1.0F).mirror()
          .texOffs(0, 46).addBox(-4.35F, -32.25F, -1.73F, 0.35F, 4.25F, 1.75F).mirror()
          .texOffs(0, 55).addBox(-4.35F, -32.25F, -3.48F, 0.35F, 3.5F, 1.75F).mirror()
          .texOffs(6, 59).addBox(-4.35F, -32.25F, -4.48F, 1.6F, 2.75F, 1.0F).mirror()
          .texOffs(51, 58).addBox(-2.75F, -32.25F, -4.48F, 5.5F, 2.25F, 1.0F).mirror()
          .texOffs(0, 46).addBox(-4.0F, -32.25F, -3.48F, 8.0F, 2.25F, 7.5F).mirror()
          .texOffs(33, 46).addBox(-4.25F, -32.25F, 4.02F, 8.5F, 6.75F, 0.25F).mirror()
          .texOffs(74, 52).addBox(3.75F, -25.5F, 4.02F, 0.5F, 5.5F, 0.25F).mirror()
          .texOffs(76, 52).addBox(-4.25F, -25.5F, 4.02F, 0.5F, 5.5F, 0.25F).mirror()
          .texOffs(33, 54).addBox(-3.75F, -25.5F, 3.02F, 7.5F, 7.75F, 1.25F).mirror()
          .texOffs(61, 46).addBox(-4.35F, -32.25F, 0.02F, 0.35F, 6.25F, 4.0F).mirror()
        , PartPose.offset(0.0F, 24.0F, 0.0F));

      PartDefinition maskDefinition = headDefinition.addOrReplaceChild("mask",
        CubeListBuilder.create()
          .texOffs(72, 0).addBox(-10.28F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F)
          .texOffs(87, 0).addBox(-10.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F)
          .texOffs(87, 4).addBox(-7.5F, -3.0F, 0.76F, 3.0F, 4.0F, 0.75F)
          .texOffs(87, 10).addBox(-5.0F, -2.75F, 0.75F, 3.0F, 3.5F, 0.25F)
          .texOffs(94, 0).addBox(-9.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F)
          .texOffs(95, 3).addBox(-5.5F, -1.0F, 0.77F, 3.0F, 2.0F, 0.75F)
          .texOffs(95, 6).addBox(-2.0F, -2.0F, 0.77F, 0.28F, 1.5F, 6.75F)
        , PartPose.offset(6.0F, -0.5F, -5.0F));

      PartDefinition headDetailDefinition = headDefinition.addOrReplaceChild("headDetail",
        CubeListBuilder.create()
          .texOffs(32, 0).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation( 0.5F))
        , PartPose.offset(0.0F, 0.0F, 1.0F));

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
        , PartPose.offset(0.0F, 0.0F, 0.0F));

      PartDefinition breastDefinition = bodyDefinition.addOrReplaceChild("breast",
        CubeListBuilder.create()
          .texOffs(41, 32).addBox(-6.0F, -1.0F, -2.0F, 6.0F, 3.0F, 3.0F)
        , PartPose.offsetAndRotation(3.0F, 2.5F, -2.0F,  0.7854F, 0.0F, 0.0F));

      return LayerDefinition.create(meshdefinition,  128,  64 );
  }
}
