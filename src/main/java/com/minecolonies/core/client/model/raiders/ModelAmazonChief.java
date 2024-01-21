package com.minecolonies.core.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.AmazonModel;
import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Amazon Chief model.
 */
public class ModelAmazonChief extends AmazonModel<AbstractEntityAmazon>
{
    /**
     * Create an instance of it.
     */
    public ModelAmazonChief(final ModelPart part)
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

        PartDefinition hatPiece = hairBack1.addOrReplaceChild("hatPiece",
          CubeListBuilder.create()
            .texOffs(80, 14).addBox(-4.6F, -2.961F, -1.1223F, 9.0F, 3.0F, 1.0F, false),
          PartPose.offsetAndRotation(0.1F, -6.8F, -4.3F, -0.5585F, 0.0F, 0.0F));

        hatPiece.addOrReplaceChild("nehat",
          CubeListBuilder.create()
            .texOffs(104, 15).addBox(3.0F, -3.1545F, -0.4684F, 1.0F, 4.0F, 1.0F, false)
            .texOffs(113, 13).addBox(1.0F, -6.192F, -0.3757F, 1.0F, 7.0F, 1.0F, false)
            .texOffs(122, 12).addBox(-1.0F, -9.1545F, -0.4684F, 1.0F, 10.0F, 1.0F, false)
            .texOffs(124, 23).addBox(-3.0F, -6.192F, -0.3757F, 1.0F, 7.0F, 1.0F, false)
            .texOffs(106, 32).addBox(-5.0F, -3.192F, -0.3757F, 1.0F, 4.0F, 1.0F, false),
          PartPose.offsetAndRotation(0.4F, -1.9F, -1.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition mask = hairBack1.addOrReplaceChild("mask",
          CubeListBuilder.create()
            .texOffs(83, 22).addBox(-3.0F, 0.5F, -0.1F, 6.0F, 1.0F, 1.0F, false)
            .texOffs(104, 34).addBox(-1.0F, 1.5F, -0.1F, 2.0F, 4.0F, 1.0F, false)
            .texOffs(100, 47).addBox(-2.0F, 2.5F, -0.1F, 4.0F, 2.0F, 1.0F, false)
            .texOffs(116, 33).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 6.0F, 1.0F, false)
            .texOffs(116, 33).addBox(-3.0F, 2.5F, -0.5F, 1.0F, 3.0F, 1.0F, false)
            .texOffs(116, 33).addBox(2.0F, 2.5F, -0.5F, 1.0F, 3.0F, 1.0F, false)
            .texOffs(114, 45).addBox(-3.0F, 3.5F, -0.7F, 6.0F, 1.0F, 1.0F, false)
            .texOffs(116, 33).addBox(-4.1F, -1.5F, 0.4F, 1.0F, 6.0F, 1.0F, false)
            .texOffs(116, 33).addBox(3.1F, -1.5F, 0.4F, 1.0F, 6.0F, 1.0F, false)
            .texOffs(116, 33).addBox(-2.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, false)
            .texOffs(116, 33).addBox(1.0F, 3.5F, -0.5F, 1.0F, 3.0F, 1.0F, false),
          PartPose.offset(0.0F, -5.7F, -4.7F));

        mask.addOrReplaceChild("bone2",
          CubeListBuilder.create(),
          PartPose.offset(-3.5F, 33.0F, 3.8F));

        hairBack1.addOrReplaceChild("feather",
          CubeListBuilder.create()
            .texOffs(104, 0).addBox(-0.1F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(106, 0).addBox(0.1F, -1.005F, 0.4092F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(106, 0).addBox(0.1F, -0.2172F, 0.7627F, 0.0F, 1.0F, 3.0F, false)
            .texOffs(106, 0).addBox(0.1F, -0.7828F, -0.3787F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(104, 0).addBox(0.2F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(-4.8F, -7.0F, -1.0F, 0.7854F, 0.0F, 0.0F));

        hairBack1.addOrReplaceChild("feather2",
          CubeListBuilder.create()
            .texOffs(104, 0).addBox(-0.1F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(106, 0).addBox(0.1F, -1.005F, 0.4092F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(106, 0).addBox(0.1F, -0.2172F, 0.7627F, 0.0F, 1.0F, 3.0F, false)
            .texOffs(106, 0).addBox(0.1F, -0.7828F, -0.3787F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(104, 0).addBox(0.2F, -0.5F, -0.8029F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(4.6F, -7.0F, -1.0F, 0.7854F, 0.0F, 0.0F));

        hairBack1.addOrReplaceChild("feather3",
          CubeListBuilder.create()
            .texOffs(0, 90).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(0, 90).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(0, 90).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 90).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 90).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(4.0F, -9.7F, -4.0F, 0.0F, 0.6981F, -1.5708F));

        hairBack1.addOrReplaceChild("feather5",
          CubeListBuilder.create()
            .texOffs(0, 96).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(0, 96).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(0, 96).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 96).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 96).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(2.0F, -11.7F, -2.0F, 0.0F, 0.6981F, -1.5708F));

        hairBack1.addOrReplaceChild("feather6",
          CubeListBuilder.create()
            .texOffs(0, 96).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(0, 96).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(0, 96).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 96).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 96).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(-2.0F, -11.7F, -2.0F, 0.0F, 0.6981F, -1.5708F));

        hairBack1.addOrReplaceChild("feather7",
          CubeListBuilder.create()
            .texOffs(0, 104).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(0, 104).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(0, 104).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 104).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 104).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(0.0F, -13.7F, 0.0F, 0.0F, 0.6981F, -1.5708F));

        hairBack1.addOrReplaceChild("feather4",
          CubeListBuilder.create()
            .texOffs(0, 90).addBox(-0.2736F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false)
            .texOffs(0, 90).addBox(-0.0736F, -1.005F, 1.394F, 0.0F, 2.0F, 3.0F, false)
            .texOffs(0, 90).addBox(-0.0736F, -0.2172F, 0.7475F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 90).addBox(-0.0736F, -0.7828F, 0.6061F, 0.0F, 1.0F, 4.0F, false)
            .texOffs(0, 90).addBox(0.0264F, -0.5F, 0.1819F, 0.0F, 1.0F, 5.0F, false),
          PartPose.offsetAndRotation(-4.0F, -9.7F, -3.6F, 0.0F, 0.6981F, -1.5708F));

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

    public void setRotationAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
