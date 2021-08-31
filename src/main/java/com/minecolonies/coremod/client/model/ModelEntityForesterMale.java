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

public class ModelEntityForesterMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityForesterMale(final ModelPart part)
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
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition hatDefinition = partDefinition.addOrReplaceChild("hat",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

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

        PartDefinition Log1Definition = bodyDefinition.addOrReplaceChild("Log1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Log2Definition = bodyDefinition.addOrReplaceChild("Log2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Log3Definition = bodyDefinition.addOrReplaceChild("Log3",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket1Definition = bodyDefinition.addOrReplaceChild("Basket1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket2Definition = bodyDefinition.addOrReplaceChild("Basket2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket3Definition = bodyDefinition.addOrReplaceChild("Basket3",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket4Definition = bodyDefinition.addOrReplaceChild("Basket4",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket5Definition = bodyDefinition.addOrReplaceChild("Basket5",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket6Definition = bodyDefinition.addOrReplaceChild("Basket6",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket7Definition = bodyDefinition.addOrReplaceChild("Basket7",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket8Definition = bodyDefinition.addOrReplaceChild("Basket8",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket9Definition = bodyDefinition.addOrReplaceChild("Basket9",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket10Definition = bodyDefinition.addOrReplaceChild("Basket10",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition Basket11Definition = bodyDefinition.addOrReplaceChild("Basket11",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition BasketE1Definition = bodyDefinition.addOrReplaceChild("BasketE1",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition BasketE2Definition = bodyDefinition.addOrReplaceChild("BasketE2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
