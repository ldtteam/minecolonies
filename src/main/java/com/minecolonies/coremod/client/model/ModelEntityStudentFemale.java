package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the male students (monks).
 */
public class ModelEntityStudentFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityStudentFemale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition frontDefinition = partDefinition.addOrReplaceChild("front",
          CubeListBuilder.create()
          , PartPose.offset(-4F, 12F, -3F));

        PartDefinition backDefinition = partDefinition.addOrReplaceChild("back",
          CubeListBuilder.create()
          , PartPose.offset(-4F, 12F, 3F));

        PartDefinition leftDefinition = partDefinition.addOrReplaceChild("left",
          CubeListBuilder.create()
          , PartPose.offset(2F, 12F, 0F));

        PartDefinition rightDefinition = partDefinition.addOrReplaceChild("right",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 12F, 0F));

        PartDefinition armCHorizontalDefinition = partDefinition.addOrReplaceChild("armCHorizontal",
          CubeListBuilder.create()
          , PartPose.offset(-8F, 3.8F, -3.5F));

        PartDefinition rightArmCDefinition = partDefinition.addOrReplaceChild("rightArmC",
          CubeListBuilder.create()
          , PartPose.offset(-8F, -0.5F, -1F));

        PartDefinition leftArmCDefinition = partDefinition.addOrReplaceChild("leftArmC",
          CubeListBuilder.create()
          , PartPose.offset(4F, -0.5F, -1F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
          , PartPose.offset(-5F, 2F, 0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
          , PartPose.offset(5F, 2F, 0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 12F, 0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
          , PartPose.offset(2F, 12F, 0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, -1F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition helmetDefinition = partDefinition.addOrReplaceChild("helmet",
          CubeListBuilder.create()
          , PartPose.offset(-4.5F, -8.5F, -4.5F));

        PartDefinition chestDefinition = partDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
          , PartPose.offset(-3.5F, 1.7F, -2.7F));

        PartDefinition ponytailBaseDefinition = partDefinition.addOrReplaceChild("ponytailBase",
          CubeListBuilder.create()
          , PartPose.offset(-1F, -4F, 2F));

        PartDefinition ponyTailTipDefinition = partDefinition.addOrReplaceChild("ponyTailTip",
          CubeListBuilder.create()
          , PartPose.offset(-0.5F, -1F, 4.8F));

        PartDefinition bookDefinition = partDefinition.addOrReplaceChild("book",
          CubeListBuilder.create()
          , PartPose.offset(-6.5F, 10F, -3F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
