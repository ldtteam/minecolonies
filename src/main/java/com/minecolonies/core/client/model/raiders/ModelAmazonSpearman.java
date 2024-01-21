package com.minecolonies.core.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.AmazonModel;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * General amazon model.
 */
public class ModelAmazonSpearman extends AmazonModel<AbstractEntityAmazon>
{
    /**
     * Create an instance of it.
     */
    public ModelAmazonSpearman(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, false),
          PartPose.offset(0.0F, -3.0F, 0.0F));

        PartDefinition hairBack1 = head.addOrReplaceChild("hairBack1",
          CubeListBuilder.create()
            .texOffs(24, 4).addBox(-4.5F, -5.5F, -4.5F, 1.0F, 2.0F, 1.0F, false)
            .texOffs(24, 0).addBox(3.5F, -5.5F, -4.5F, 1.0F, 3.0F, 1.0F, false)
            .texOffs(74, 14).addBox(-5.0F, -7.2F, -4.8F, 10.0F, 2.0F, 6.0F, false)
            .texOffs(74, 14).addBox(-2.0F, -7.2F, -5.3F, 4.0F, 2.0F, 6.0F, false)
            .texOffs(28, 18).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 2.0F, 4.0F, false)
            .texOffs(48, 13).addBox(-4.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, false)
            .texOffs(45, 24).addBox(2.5F, -6.5F, -4.5F, 2.0F, 1.0F, 4.0F, false)
            .texOffs(24, 26).addBox(-4.5F, -8.5F, 1.5F, 9.0F, 9.0F, 3.0F, false)
            .texOffs(48, 0).addBox(-4.5F, -8.5F, -0.5F, 9.0F, 5.0F, 2.0F, false),
          PartPose.offset(0.0F, 0.0F, 0.0F));

        hairBack1.addOrReplaceChild("feather",
          CubeListBuilder.create()
            .texOffs(104, 0).addBox(4.4F, -0.4293F, -2.1464F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(106, 0).addBox(4.6F, -0.9343F, -0.9343F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(106, 0).addBox(4.6F, -0.1464F, -0.5808F, 0.0F, 1.0F, 3.0F, false)
            .texOffs(106, 0).addBox(4.6F, -0.7121F, -1.7222F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(104, 0).addBox(4.7F, -0.4293F, -2.1464F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(-9.3F, -8.0F, -0.1F, 0.7854F, 0.0F, 0.0F));

        PartDefinition hairback = hairBack1.addOrReplaceChild("hairback",
          CubeListBuilder.create()
            .texOffs(3, 5).addBox(-0.5F, -3.0314F, -0.5411F, 1.0F, 2.0F, 1.0F, false)
            .texOffs(0, 26).addBox(-0.5F, -5.8046F, 0.3981F, 1.0F, 1.0F, 1.0F, false)
            .texOffs(0, 0).addBox(-0.5F, -1.2233F, 0.6553F, 1.0F, 5.0F, 1.0F, false)
            .texOffs(20, 26).addBox(-0.5F, -3.869F, 0.9776F, 1.0F, 1.0F, 1.0F, false)
            .texOffs(23, 27).addBox(-0.5F, -3.5984F, -0.2911F, 1.0F, 1.0F, 1.0F, false)
            .texOffs(14, 43).addBox(-0.5F, -5.3135F, 0.4115F, 1.0F, 9.0F, 1.0F, false),
          PartPose.offsetAndRotation(0.0F, -7.0F, 3.0F, -0.5236F, 0.0F, 0.0F));

        hairback.addOrReplaceChild("bone",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-0.5F, -1.3154F, -0.6545F, 1.0F, 4.0F, 1.0F, false)
            .texOffs(3, 20).addBox(-0.5F, 1.9346F, -0.2545F, 1.0F, 3.0F, 1.0F, false)
            .texOffs(4, 16).addBox(-0.5F, 3.9346F, -0.9545F, 1.0F, 3.0F, 1.0F, false)
            .texOffs(4, 0).addBox(-0.5F, 5.9346F, -0.6545F, 1.0F, 4.0F, 1.0F, false),
          PartPose.offsetAndRotation(0.0F, -4.5F, 1.5981F, 0.5236F, 0.0F, 0.0F));

        PartDefinition body = partdefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(74, 0).addBox(-3.5F, 6.4F, -2.6F, 7.0F, 1.0F, 5.0F, false)
            .texOffs(0, 63).addBox(-4.5F, 7.4F, -2.6F, 9.0F, 7.0F, 5.0F, false)
            .texOffs(0, 26).addBox(-4.0F, -3.0F, -2.0F, 8.0F, 13.0F, 4.0F, false),
          PartPose.offset(0.0F, 0.0F, 0.0F));

        body.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(48, 7).addBox(-3.5F, 0.2727F, -2.3632F, 7.0F, 3.0F, 3.0F, false),
          PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.632F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, false)
            .texOffs(32, 0).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, false),
          PartPose.offset(2.0F, 9.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(43, 33).addBox(-2.5F, 12.0F, -3.0F, 5.0F, 1.0F, 6.0F, false)
            .texOffs(24, 38).addBox(-2.0F, 1.0F, -2.0F, 4.0F, 14.0F, 4.0F, false),
          PartPose.offset(-2.0F, 9.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 40).addBox(-1.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, false),
          PartPose.offset(5.0F, 0.0F, 0.0F));

        partdefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(43, 33).addBox(-2.5F, 6.0F, -2.5F, 4.0F, 1.0F, 5.0F, false)
            .texOffs(0, 43).addBox(-2.0F, -3.0F, -2.0F, 3.0F, 13.0F, 4.0F, false),
          PartPose.offset(-5.0F, 0.0F, 0.0F));


        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
