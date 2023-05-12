package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelArcherNorsemen extends NorsemenModel
{

    public ModelArcherNorsemen(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
            .texOffs(33, 1).addBox(-4.0F, -8.0F, -3.75F, 8.0F, 8.0F, 7.0F, new CubeDeformation( 0.5F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
            .texOffs(75, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
            .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hoodDefinition = headDefinition.addOrReplaceChild("hood",
          CubeListBuilder.create()
            .texOffs(59, 25).addBox(-4.0F, -33.0F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation( 0.5F))
            .texOffs(64, 0).addBox(-8.0F, -24.75F, -2.25F, 17.0F, 20.0F, 5.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(-0.5F, 24.5F, 0.0F));

        PartDefinition quiverDefinition = bodyDefinition.addOrReplaceChild("quiver",
          CubeListBuilder.create()
            .texOffs(99, 46).addBox(-0.979F, -4.9528F, -1.25F, 3.0F, 14.0F, 0.0F)
            .texOffs(90, 45).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F)
            .texOffs(79, 46).addBox(-1.0F, -1.0F, -2.0F, 3.0F, 14.0F, 1.0F, new CubeDeformation( 0.25F))
          , PartPose.offsetAndRotation(-4.9F, 2.0F, 6.0F,  0.0F, 0.0F, -0.6109F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition,  124,  64 );
    }
}
