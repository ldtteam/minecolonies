package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import org.jetbrains.annotations.NotNull;

public class ModelEntityGlassblowerFemale extends CitizenModel<AbstractEntityCitizen>
{

    public ModelEntityGlassblowerFemale(final ModelPart part)
    {
        super(part);
        hat.visible = false;
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 0F));

        PartDefinition pocketDefinition = bodyDefinition.addOrReplaceChild("pocket",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 8F, -3F));

        PartDefinition thing1Definition = bodyDefinition.addOrReplaceChild("thing1",
          CubeListBuilder.create()
          , PartPose.offset(-1F, 6F, -3F));

        PartDefinition thing2Definition = bodyDefinition.addOrReplaceChild("thing2",
          CubeListBuilder.create()
          , PartPose.offset(0F, 6F, -3F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
          , PartPose.offset(-5F, 2F, 0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
          , PartPose.offset(5F, 2F, 0F));

        PartDefinition gauntlet1Definition = leftArmDefinition.addOrReplaceChild("gauntlet1",
          CubeListBuilder.create()
          , PartPose.offset(3.5F, 7F, -2.5F));

        PartDefinition gauntlet2Definition = rightArmDefinition.addOrReplaceChild("gauntlet2",
          CubeListBuilder.create()
          , PartPose.offset(-8.5F, 7F, -2.5F));

        PartDefinition Hair5Definition = headDefinition.addOrReplaceChild("Hair5",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, 1F));

        PartDefinition Hair1Definition = headDefinition.addOrReplaceChild("Hair1",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, 1F));

        PartDefinition Hair2Definition = headDefinition.addOrReplaceChild("Hair2",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -2F));

        PartDefinition Hair4Definition = headDefinition.addOrReplaceChild("Hair4",
          CubeListBuilder.create()
          , PartPose.offset(3F, -6F, -5F));

        PartDefinition Hair3Definition = headDefinition.addOrReplaceChild("Hair3",
          CubeListBuilder.create()
          , PartPose.offset(4F, -7F, 1F));

        PartDefinition Hair6Definition = headDefinition.addOrReplaceChild("Hair6",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -7F, -5F));

        PartDefinition Hair7Definition = headDefinition.addOrReplaceChild("Hair7",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -2F));

        PartDefinition Hair8Definition = headDefinition.addOrReplaceChild("Hair8",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -4F));

        PartDefinition Hair12Definition = headDefinition.addOrReplaceChild("Hair12",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -5F));

        PartDefinition Hair13Definition = headDefinition.addOrReplaceChild("Hair13",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -6F, -4F));

        PartDefinition Hair11Definition = headDefinition.addOrReplaceChild("Hair11",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -2F));

        PartDefinition Hair15Definition = headDefinition.addOrReplaceChild("Hair15",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, 0F));

        PartDefinition Hair17Definition = headDefinition.addOrReplaceChild("Hair17",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -8F, -5F));

        PartDefinition Hair18Definition = headDefinition.addOrReplaceChild("Hair18",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -7F, -5F));

        PartDefinition Hair19Definition = headDefinition.addOrReplaceChild("Hair19",
          CubeListBuilder.create()
          , PartPose.offset(1F, -7F, -5F));

        PartDefinition Hair192Definition = headDefinition.addOrReplaceChild("Hair192",
          CubeListBuilder.create()
          , PartPose.offset(1F, -7F, -5F));

        PartDefinition Hair14Definition = headDefinition.addOrReplaceChild("Hair14",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, -1F));

        PartDefinition Hair20Definition = headDefinition.addOrReplaceChild("Hair20",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -7F, 1F));

        PartDefinition Hair21Definition = headDefinition.addOrReplaceChild("Hair21",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, 1F));

        PartDefinition Hair16Definition = headDefinition.addOrReplaceChild("Hair16",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, 2F));

        PartDefinition Hair10Definition = headDefinition.addOrReplaceChild("Hair10",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, 1F));

        PartDefinition Hair9Definition = headDefinition.addOrReplaceChild("Hair9",
          CubeListBuilder.create()
          , PartPose.offset(4F, -6F, -3F));

        PartDefinition bipedChestDefinition = bodyDefinition.addOrReplaceChild("bipedChest",
          CubeListBuilder.create()
          , PartPose.offset(0.5F, 0F, 0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 12F, 0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
          , PartPose.offset(2F, 12F, 0F));

        PartDefinition PonytailBDefinition = headDefinition.addOrReplaceChild("PonytailB",
          CubeListBuilder.create()
          , PartPose.offset(0F, 0F, 1F));

        PartDefinition PonytailTDefinition = headDefinition.addOrReplaceChild("PonytailT",
          CubeListBuilder.create()
          , PartPose.offset(0F, 1F, 1F));

        PartDefinition Hair22Definition = headDefinition.addOrReplaceChild("Hair22",
          CubeListBuilder.create()
          , PartPose.offset(-2F, 2F, 4F));

        PartDefinition Hair23Definition = headDefinition.addOrReplaceChild("Hair23",
          CubeListBuilder.create()
          , PartPose.offset(4F, -4F, 2F));

        PartDefinition Hair24Definition = headDefinition.addOrReplaceChild("Hair24",
          CubeListBuilder.create()
          , PartPose.offset(-2F, -7F, -5F));

        PartDefinition Hair25Definition = headDefinition.addOrReplaceChild("Hair25",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -9F, -5F));

        PartDefinition Hair26Definition = headDefinition.addOrReplaceChild("Hair26",
          CubeListBuilder.create()
          , PartPose.offset(4F, -4F, -1F));

        PartDefinition Hair28Definition = headDefinition.addOrReplaceChild("Hair28",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -4F));

        PartDefinition Hair29Definition = headDefinition.addOrReplaceChild("Hair29",
          CubeListBuilder.create()
          , PartPose.offset(4F, -6F, -3F));

        PartDefinition Hair30Definition = headDefinition.addOrReplaceChild("Hair30",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, 1F));

        PartDefinition Hair31Definition = headDefinition.addOrReplaceChild("Hair31",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -2F));

        PartDefinition Hair32Definition = headDefinition.addOrReplaceChild("Hair32",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -5F));

        PartDefinition Hair33Definition = headDefinition.addOrReplaceChild("Hair33",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -6F, -4F));

        PartDefinition Hair35Definition = headDefinition.addOrReplaceChild("Hair35",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, 2F));

        PartDefinition Hair34Definition = headDefinition.addOrReplaceChild("Hair34",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, -1F));

        PartDefinition Hair37Definition = headDefinition.addOrReplaceChild("Hair37",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -7F, -5F));

        PartDefinition Hair38Definition = headDefinition.addOrReplaceChild("Hair38",
          CubeListBuilder.create()
          , PartPose.offset(2F, -7F, -5F));

        PartDefinition Hair39Definition = headDefinition.addOrReplaceChild("Hair39",
          CubeListBuilder.create()
          , PartPose.offset(4F, -7F, 1F));

        PartDefinition Hair40Definition = headDefinition.addOrReplaceChild("Hair40",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -7F, 1F));

        PartDefinition Hair42Definition = headDefinition.addOrReplaceChild("Hair42",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -9F, 4F));

        PartDefinition Hair43Definition = headDefinition.addOrReplaceChild("Hair43",
          CubeListBuilder.create()
          , PartPose.offset(-3F, 0F, 4F));

        PartDefinition Hair41Definition = headDefinition.addOrReplaceChild("Hair41",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, 1F));

        PartDefinition Hair27Definition = headDefinition.addOrReplaceChild("Hair27",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -2F));

        PartDefinition Hair44Definition = headDefinition.addOrReplaceChild("Hair44",
          CubeListBuilder.create()
          , PartPose.offset(4F, -4F, 2F));

        PartDefinition Hair45Definition = headDefinition.addOrReplaceChild("Hair45",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -9F, -5F));

        PartDefinition Hair46Definition = headDefinition.addOrReplaceChild("Hair46",
          CubeListBuilder.create()
          , PartPose.offset(4F, -4F, -1F));

        PartDefinition Hair47Definition = headDefinition.addOrReplaceChild("Hair47",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -2F));

        PartDefinition Hair48Definition = headDefinition.addOrReplaceChild("Hair48",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, -5F));

        PartDefinition Hair49Definition = headDefinition.addOrReplaceChild("Hair49",
          CubeListBuilder.create()
          , PartPose.offset(3F, -6F, -5F));

        PartDefinition Hair51Definition = headDefinition.addOrReplaceChild("Hair51",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -2F));

        PartDefinition Hair52Definition = headDefinition.addOrReplaceChild("Hair52",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, -5F));

        PartDefinition Hair55Definition = headDefinition.addOrReplaceChild("Hair55",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, 2F));

        PartDefinition Hair54Definition = headDefinition.addOrReplaceChild("Hair54",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -4F, -1F));

        PartDefinition Hair53Definition = headDefinition.addOrReplaceChild("Hair53",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -6F, -5F));

        PartDefinition Hair57Definition = headDefinition.addOrReplaceChild("Hair57",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -6F, -5F));

        PartDefinition Hair58Definition = headDefinition.addOrReplaceChild("Hair58",
          CubeListBuilder.create()
          , PartPose.offset(2F, -7F, -5F));

        PartDefinition Hair59Definition = headDefinition.addOrReplaceChild("Hair59",
          CubeListBuilder.create()
          , PartPose.offset(4F, -7F, 1F));

        PartDefinition Hair60Definition = headDefinition.addOrReplaceChild("Hair60",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -7F, 1F));

        PartDefinition Hair61Definition = headDefinition.addOrReplaceChild("Hair61",
          CubeListBuilder.create()
          , PartPose.offset(-5F, -8F, 1F));

        PartDefinition Hair62Definition = headDefinition.addOrReplaceChild("Hair62",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -9F, 4F));

        PartDefinition Hair63Definition = headDefinition.addOrReplaceChild("Hair63",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -7F, -5F));

        PartDefinition Hair56Definition = headDefinition.addOrReplaceChild("Hair56",
          CubeListBuilder.create()
          , PartPose.offset(-4F, -8F, -5F));

        PartDefinition Hair50Definition = headDefinition.addOrReplaceChild("Hair50",
          CubeListBuilder.create()
          , PartPose.offset(4F, -8F, 1F));

        return LayerDefinition.create(meshdefinition, 128, 64);
    }
}
