package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleNoble extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityCitizenFemaleNoble(final ModelPart part)
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
          , PartPose.offset(0F, 0F, 1F));

        PartDefinition hatDefinition = partDefinition.addOrReplaceChild("hat",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 1F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 3F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
          , PartPose.offset(4F, 0F, 0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
          , PartPose.offset(-5F, 0F, 0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
          , PartPose.offset(-1F, 12F, 1F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
          , PartPose.offset(2F, 12F, 1F));

        PartDefinition breastDefinition = bodyDefinition.addOrReplaceChild("breast",
          CubeListBuilder.create()
          , PartPose.offset(-1F, 3F, 1F));

        PartDefinition hairDefinition = partDefinition.addOrReplaceChild("hair",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 1F));

        PartDefinition dressPart1Definition = bodyDefinition.addOrReplaceChild("dressPart1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 11F, 0F));

        PartDefinition dressPart2Definition = bodyDefinition.addOrReplaceChild("dressPart2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 11F, 0F));

        PartDefinition dressPart3Definition = bodyDefinition.addOrReplaceChild("dressPart3",
          CubeListBuilder.create()
          , PartPose.offset(0F, 11F, 0F));

        PartDefinition dressPart4Definition = bodyDefinition.addOrReplaceChild("dressPart4",
          CubeListBuilder.create()
          , PartPose.offset(0F, 11F, 0F));

        PartDefinition dressPart5Definition = bodyDefinition.addOrReplaceChild("dressPart5",
          CubeListBuilder.create()
          , PartPose.offset(0F, 11F, 0F));

        PartDefinition hat1Definition = hatDefinition.addOrReplaceChild("hat1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 1F));

        PartDefinition hat2Definition = hatDefinition.addOrReplaceChild("hat2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 1F));

        PartDefinition bagDefinition = bodyDefinition.addOrReplaceChild("bag",
          CubeListBuilder.create()
          , PartPose.offset(4F, 0F, 0F));

        PartDefinition bagHand1Definition = bodyDefinition.addOrReplaceChild("bagHand1",
          CubeListBuilder.create()
          , PartPose.offset(4F, 0F, 0F));

        PartDefinition bagHand2Definition = bodyDefinition.addOrReplaceChild("bagHand2",
          CubeListBuilder.create()
          , PartPose.offset(4F, 0F, 0F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
