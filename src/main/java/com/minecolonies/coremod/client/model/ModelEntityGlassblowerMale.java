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

public class ModelEntityGlassblowerMale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityGlassblowerMale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

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
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition gauntlet1Definition = leftArmDefinition.addOrReplaceChild("gauntlet1",
          CubeListBuilder.create()
          , PartPose.offset(3.5F, 7F, -2.5F));

        PartDefinition gauntlet2Definition = rightArmDefinition.addOrReplaceChild("gauntlet2",
          CubeListBuilder.create()
          , PartPose.offset(-8.5F, 7F, -2.5F));

        PartDefinition hair1Definition = headDefinition.addOrReplaceChild("hair1",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.4F, -4.5F));

        PartDefinition hair2Definition = headDefinition.addOrReplaceChild("hair2",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, 4F));

        PartDefinition hair17Definition = headDefinition.addOrReplaceChild("hair17",
          CubeListBuilder.create()
          , PartPose.offset(4F, -1.4F, -4.5F));

        PartDefinition hair16Definition = headDefinition.addOrReplaceChild("hair16",
          CubeListBuilder.create()
          , PartPose.offset(4F, -3F, -1F));

        PartDefinition hair15Definition = headDefinition.addOrReplaceChild("hair15",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -3F, -1F));

        PartDefinition hair14Definition = headDefinition.addOrReplaceChild("hair14",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, -3F));

        PartDefinition hair13Definition = headDefinition.addOrReplaceChild("hair13",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, 3F));

        PartDefinition hair12Definition = headDefinition.addOrReplaceChild("hair12",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, -2F));

        PartDefinition hair11Definition = headDefinition.addOrReplaceChild("hair11",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -4F));

        PartDefinition hair10Definition = headDefinition.addOrReplaceChild("hair10",
          CubeListBuilder.create()
          , PartPose.offset(2F, -9F, -5F));

        PartDefinition hair9Definition = headDefinition.addOrReplaceChild("hair9",
          CubeListBuilder.create()
          , PartPose.offset(-2F, -9F, -5F));

        PartDefinition hair8Definition = headDefinition.addOrReplaceChild("hair8",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -9F, -4F));

        PartDefinition hair7Definition = headDefinition.addOrReplaceChild("hair7",
          CubeListBuilder.create()
          , PartPose.offset(4F, -5F, -2F));

        PartDefinition hair6Definition = headDefinition.addOrReplaceChild("hair6",
          CubeListBuilder.create()
          , PartPose.offset(-3.5F, 0F, -4.5F));

        PartDefinition hair5Definition = headDefinition.addOrReplaceChild("hair5",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, -3F));

        PartDefinition hair4Definition = headDefinition.addOrReplaceChild("hair4",
          CubeListBuilder.create()
          , PartPose.offset(-2F, -9F, -5F));

        PartDefinition hair18Definition = headDefinition.addOrReplaceChild("hair18",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.9F, -4.5F));

        PartDefinition hair19Definition = headDefinition.addOrReplaceChild("hair19",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.4F, -4.5F));

        PartDefinition hair20Definition = headDefinition.addOrReplaceChild("hair20",
          CubeListBuilder.create()
          , PartPose.offset(-4.5F, -0.9F, -4.5F));

        PartDefinition hair3Definition = headDefinition.addOrReplaceChild("hair3",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -8F, 4F));

        PartDefinition toolHandle1Definition = bodyDefinition.addOrReplaceChild("toolHandle1",
          CubeListBuilder.create()
          , PartPose.offset(-1F, 6F, -3F));

        PartDefinition toolHandle2Definition = bodyDefinition.addOrReplaceChild("toolHandle2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 6F, -3F));

        PartDefinition pocketDefinition = bodyDefinition.addOrReplaceChild("pocket",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 8F, -3F));

        PartDefinition hair_37Definition = headDefinition.addOrReplaceChild("hair_37",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -3F, -1F));

        PartDefinition hair_38Definition = headDefinition.addOrReplaceChild("hair_38",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, 3F));

        PartDefinition hair_36Definition = headDefinition.addOrReplaceChild("hair_36",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, -2F));

        PartDefinition hair_35Definition = headDefinition.addOrReplaceChild("hair_35",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -6F, -1F));

        PartDefinition hair21Definition = headDefinition.addOrReplaceChild("hair21",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, 4F));

        PartDefinition hair_23Definition = headDefinition.addOrReplaceChild("hair_23",
          CubeListBuilder.create()
          , PartPose.offset(4F, -6F, -1F));

        PartDefinition hair_24Definition = headDefinition.addOrReplaceChild("hair_24",
          CubeListBuilder.create()
          , PartPose.offset(4F, -5F, -2F));

        PartDefinition hair_22Definition = headDefinition.addOrReplaceChild("hair_22",
          CubeListBuilder.create()
          , PartPose.offset(1F, -1.9F, -4.5F));

        PartDefinition hair_25Definition = headDefinition.addOrReplaceChild("hair_25",
          CubeListBuilder.create()
          , PartPose.offset(4F, -5F, 3F));

        PartDefinition hair_26Definition = headDefinition.addOrReplaceChild("hair_26",
          CubeListBuilder.create()
          , PartPose.offset(4F, -3F, -1F));

        PartDefinition hair_27Definition = headDefinition.addOrReplaceChild("hair_27",
          CubeListBuilder.create()
          , PartPose.offset(4F, -1.5F, -4.5F));

        PartDefinition hair_28Definition = headDefinition.addOrReplaceChild("hair_28",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.9F, -4.5F));

        PartDefinition hair_29Definition = headDefinition.addOrReplaceChild("hair_29",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.5F, -4.5F));

        PartDefinition hair_30Definition = headDefinition.addOrReplaceChild("hair_30",
          CubeListBuilder.create()
          , PartPose.offset(-2.5F, 3F, -4.5F));

        PartDefinition hair_31Definition = headDefinition.addOrReplaceChild("hair_31",
          CubeListBuilder.create()
          , PartPose.offset(-4.5F, -0.9F, -4.5F));

        PartDefinition hair_32Definition = headDefinition.addOrReplaceChild("hair_32",
          CubeListBuilder.create()
          , PartPose.offset(-4F, 0F, -4.5F));

        PartDefinition hair_33Definition = headDefinition.addOrReplaceChild("hair_33",
          CubeListBuilder.create()
          , PartPose.offset(-3.5F, 1F, -4.5F));

        PartDefinition hair_34Definition = headDefinition.addOrReplaceChild("hair_34",
          CubeListBuilder.create()
          , PartPose.offset(-3F, 2F, -4.5F));

        PartDefinition hair39Definition = headDefinition.addOrReplaceChild("hair39",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -8F, 4F));

        PartDefinition hair40Definition = headDefinition.addOrReplaceChild("hair40",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -9F, -5F));

        PartDefinition hair41Definition = headDefinition.addOrReplaceChild("hair41",
          CubeListBuilder.create()
          , PartPose.offset(4F, -5F, -3F));

        PartDefinition hair42Definition = headDefinition.addOrReplaceChild("hair42",
          CubeListBuilder.create()
          , PartPose.offset(4F, -5F, 3F));

        PartDefinition hair43Definition = headDefinition.addOrReplaceChild("hair43",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -4F));

        PartDefinition hair44Definition = headDefinition.addOrReplaceChild("hair44",
          CubeListBuilder.create()
          , PartPose.offset(1F, -1.9F, -4.5F));

        PartDefinition hair45Definition = headDefinition.addOrReplaceChild("hair45",
          CubeListBuilder.create()
          , PartPose.offset(4F, -5F, -2F));

        PartDefinition hair48Definition = headDefinition.addOrReplaceChild("hair48",
          CubeListBuilder.create()
          , PartPose.offset(2F, -9F, -5F));

        PartDefinition hair50Definition = headDefinition.addOrReplaceChild("hair50",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, -2F));

        PartDefinition hair53Definition = headDefinition.addOrReplaceChild("hair53",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -3F, -1F));

        PartDefinition hair56Definition = headDefinition.addOrReplaceChild("hair56",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.9F, -4.5F));

        PartDefinition hair51Definition = headDefinition.addOrReplaceChild("hair51",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, 3F));

        PartDefinition hair55Definition = headDefinition.addOrReplaceChild("hair55",
          CubeListBuilder.create()
          , PartPose.offset(4F, -1.5F, -4.5F));

        PartDefinition hair58Definition = headDefinition.addOrReplaceChild("hair58",
          CubeListBuilder.create()
          , PartPose.offset(-1.5F, 2F, -4.5F));

        PartDefinition hair54Definition = headDefinition.addOrReplaceChild("hair54",
          CubeListBuilder.create()
          , PartPose.offset(4F, -3F, -1F));

        PartDefinition hair57Definition = headDefinition.addOrReplaceChild("hair57",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -1.5F, -4.5F));

        PartDefinition hair59Definition = headDefinition.addOrReplaceChild("hair59",
          CubeListBuilder.create()
          , PartPose.offset(-4.5F, -0.9F, -4.5F));

        PartDefinition hair49Definition = headDefinition.addOrReplaceChild("hair49",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -4F));

        PartDefinition hair61Definition = headDefinition.addOrReplaceChild("hair61",
          CubeListBuilder.create()
          , PartPose.offset(-2.5F, 1F, -4.5F));

        PartDefinition hair52Definition = headDefinition.addOrReplaceChild("hair52",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -5F, -3F));

        PartDefinition hair60Definition = headDefinition.addOrReplaceChild("hair60",
          CubeListBuilder.create()
          , PartPose.offset(-3.5F, 0F, -4.5F));

        PartDefinition hair46Definition = headDefinition.addOrReplaceChild("hair46",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -9F, -4F));

        PartDefinition hair47Definition = headDefinition.addOrReplaceChild("hair47",
          CubeListBuilder.create()
          , PartPose.offset(-2F, -9F, -5F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
