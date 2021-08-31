package com.minecolonies.coremod.client.model;

import com.minecolonies.api.IMinecoloniesAPI;
import com.minecolonies.api.client.render.modeltype.BipedModelType;
import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;

public class ModelEntityCourierFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityCourierFemale(final ModelPart part)
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
          , PartPose.offset(0F, 2F, -4F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
          , PartPose.offset(0F, 1F, -2F));

        PartDefinition ChestDefinition = bodyDefinition.addOrReplaceChild("Chest",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
          , PartPose.offset(2F, 12F, 0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 12F, 0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
          , PartPose.offset(4F, 2F, -4F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
          , PartPose.offset(-4F, 2F, -4F));

        PartDefinition RightBDefinition = headDefinition.addOrReplaceChild("RightB",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition TopDefinition = headDefinition.addOrReplaceChild("Top",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition LeftBDefinition = headDefinition.addOrReplaceChild("LeftB",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition RightMDefinition = headDefinition.addOrReplaceChild("RightM",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition FrontLBDefinition = headDefinition.addOrReplaceChild("FrontLB",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition LeftMDefinition = headDefinition.addOrReplaceChild("LeftM",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition BackDefinition = headDefinition.addOrReplaceChild("Back",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition BackMDefinition = headDefinition.addOrReplaceChild("BackM",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition FrontMDefinition = headDefinition.addOrReplaceChild("FrontM",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Front1Definition = headDefinition.addOrReplaceChild("Front1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition FrontLMDefinition = headDefinition.addOrReplaceChild("FrontLM",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition FrontRMDefinition = headDefinition.addOrReplaceChild("FrontRM",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition FrontRBDefinition = headDefinition.addOrReplaceChild("FrontRB",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Front2Definition = headDefinition.addOrReplaceChild("Front2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition BaseDefinition = bodyDefinition.addOrReplaceChild("Base",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition RimLDefinition = bodyDefinition.addOrReplaceChild("RimL",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Lid1Definition = bodyDefinition.addOrReplaceChild("Lid1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Lock1Definition = bodyDefinition.addOrReplaceChild("Lock1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Lock2Definition = bodyDefinition.addOrReplaceChild("Lock2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition RimBDefinition = bodyDefinition.addOrReplaceChild("RimB",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition RimRDefinition = bodyDefinition.addOrReplaceChild("RimR",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition RimFDefinition = bodyDefinition.addOrReplaceChild("RimF",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Lid2Definition = bodyDefinition.addOrReplaceChild("Lid2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition HandleDefinition = bodyDefinition.addOrReplaceChild("Handle",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Block1Definition = bodyDefinition.addOrReplaceChild("Block1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Block2Definition = bodyDefinition.addOrReplaceChild("Block2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Block3Definition = bodyDefinition.addOrReplaceChild("Block3",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Block4Definition = bodyDefinition.addOrReplaceChild("Block4",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition TorchDefinition = bodyDefinition.addOrReplaceChild("Torch",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        return LayerDefinition.create(meshdefinition, 256, 128);
    }
}
