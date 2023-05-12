package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelChiefNorsemen extends NorsemenModel
{

    public ModelChiefNorsemen(final ModelPart part)
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
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F).mirror()
            .texOffs(89, 44).addBox(-4.5F, -8.5F, -5.0F, 9.0F, 0.5F, 9.0F)
            .texOffs(89, 42).addBox(-4.5F, -8.5F, 4.0F, 9.0F, 8.5F, 0.5F)
            .texOffs(89, 42).addBox(4.0F, -8.0F, -2.0F, 0.5F, 6.0F, 6.0F)
            .texOffs(89, 42).addBox(-4.5F, -8.0F, -2.0F, 0.5F, 6.0F, 6.0F)
            .texOffs(89, 42).addBox(4.0F, -8.0F, -5.0F, 0.5F, 5.25F, 3.0F)
            .texOffs(89, 42).addBox(-4.5F, -8.0F, -5.0F, 0.5F, 5.25F, 3.0F)
            .texOffs(89, 42).addBox(4.0F, -2.0F, -0.5F, 0.5F, 2.0F, 4.5F)
            .texOffs(89, 42).addBox(-4.5F, -2.0F, -0.5F, 0.5F, 2.0F, 4.5F)
            .texOffs(89, 45).addBox(-4.0F, -8.0F, -5.0F, 8.0F, 2.75F, 0.75F)
            .texOffs(89, 42).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 1.0F, 0.75F)
            .texOffs(89, 42).addBox(-4.0F, -5.25F, -5.0F, 1.0F, 1.25F, 0.75F)
            .texOffs(89, 42).addBox(3.0F, -5.25F, -5.0F, 1.0F, 1.25F, 0.75F)
            .texOffs(89, 42).addBox(-1.0F, -5.25F, -5.0F, 2.0F, 1.25F, 0.75F)
            .texOffs(89, 42).addBox(-1.0F, -3.0F, -5.0F, 2.0F, 1.25F, 0.75F)
            .texOffs(45, 36).addBox(4.0F, -7.0F, -1.75F, 1.0F, 4.25F, 4.5F)
            .texOffs(45, 36).addBox(-5.0F, -7.0F, -1.75F, 1.0F, 4.25F, 4.5F)
            .texOffs(64, 23).addBox(-4.25F, -1.25F, -4.5F, 8.5F, 2.25F, 4.75F)
            .texOffs(64, 23).addBox(-4.25F, -4.0F, -4.5F, 8.5F, 2.0F, 4.75F)
            .texOffs(64, 23).addBox(-4.25F, -2.0F, -4.5F, 3.25F, 0.75F, 4.75F)
            .texOffs(64, 23).addBox(1.0F, -2.0F, -4.5F, 3.25F, 0.75F, 4.75F)
            .texOffs(64, 23).addBox(0.5F, 1.0F, -4.5F, 3.75F, 0.75F, 4.75F)
            .texOffs(64, 23).addBox(-4.25F, 1.0F, -4.5F, 3.75F, 0.75F, 4.75F)
            .texOffs(64, 23).addBox(-3.75F, 1.75F, -4.5F, 2.5F, 0.75F, 1.75F)
            .texOffs(64, 23).addBox(-3.5F, 2.5F, -4.5F, 1.25F, 1.5F, 1.5F)
            .texOffs(64, 23).addBox(1.5F, 1.75F, -4.5F, 2.25F, 1.5F, 2.0F)
            .texOffs(64, 23).addBox(2.25F, 3.25F, -4.5F, 1.5F, 1.5F, 1.75F)
            .texOffs(64, 23).addBox(2.75F, 4.75F, -4.5F, 1.0F, 1.25F, 1.0F)
            .texOffs(64, 23).addBox(-3.25F, 4.0F, -4.5F, 0.75F, 1.25F, 1.0F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition hornrDefinition = headDefinition.addOrReplaceChild("hornr",
          CubeListBuilder.create()
            .texOffs(2, 49).addBox(4.5F, 2.0F, -4.25F, 1.75F, 3.25F, 3.75F)
            .texOffs(2, 49).addBox(6.0F, 2.25F, -4.0F, 2.25F, 2.75F, 3.25F)
          , PartPose.offset(0.5F, -8.5F, 3.0F));

        PartDefinition bonerDefinition = hornrDefinition.addOrReplaceChild("boner",
          CubeListBuilder.create()
            .texOffs(2, 50).addBox(3.1411F, 0.3478F, -1.9003F, 2.0F, 2.75F, 2.25F)
            .texOffs(2, 49).addBox(6.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F)
            .texOffs(2, 49).addBox(8.5136F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F)
            .texOffs(2, 49).addBox(10.7569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F)
            .texOffs(2, 49).addBox(11.4919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F)
          , PartPose.offsetAndRotation(-2.9362F, -0.7956F, 1.0631F,  0.0F, 0.1745F, 0.0F));

        PartDefinition boner2Definition = bonerDefinition.addOrReplaceChild("boner2",
          CubeListBuilder.create()
            .texOffs(2, 49).addBox(6.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F)
            .texOffs(2, 49).addBox(8.5136F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F)
            .texOffs(2, 49).addBox(10.7569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F)
            .texOffs(2, 49).addBox(11.4919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F)
          , PartPose.offsetAndRotation(-2.9362F, -0.7956F, 1.0631F,  0.0F, 0.1745F, 0.0F));

        PartDefinition hornl2Definition = headDefinition.addOrReplaceChild("hornl2",
          CubeListBuilder.create()
            .texOffs(2, 49).addBox(-6.25F, 2.0F, -4.25F, 1.75F, 3.25F, 3.75F).mirror()
            .texOffs(2, 49).addBox(-8.25F, 2.25F, -4.0F, 2.25F, 2.75F, 3.25F).mirror()
          , PartPose.offset(-0.5F, -8.5F, 3.0F));

        PartDefinition bonel3Definition = hornl2Definition.addOrReplaceChild("bonel3",
          CubeListBuilder.create()
            .texOffs(2, 50).addBox(-5.1411F, 0.3478F, -1.9003F, 2.0F, 2.75F, 2.25F).mirror()
          , PartPose.offsetAndRotation(-6.25F, 0.5F, 0.0F,  0.6109F, -0.7854F, -0.6109F));

        PartDefinition bonel4Definition = bonel3Definition.addOrReplaceChild("bonel4",
          CubeListBuilder.create()
            .texOffs(2, 49).addBox(-9.4555F, 1.453F, -1.0788F, 3.0F, 2.0F, 1.5F).mirror()
            .texOffs(2, 49).addBox(-11.2636F, 1.7796F, -0.7375F, 2.75F, 1.25F, 1.0F).mirror()
            .texOffs(2, 49).addBox(-12.2569F, 1.8793F, -0.6498F, 1.5F, 1.0F, 0.75F).mirror()
            .texOffs(2, 49).addBox(-12.9919F, 1.9793F, -0.5364F, 1.5F, 0.8F, 0.5F).mirror()
          , PartPose.offsetAndRotation(2.9362F, -0.7956F, 1.0631F,  0.0F, -0.1745F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
          , PartPose.offset(-2.0F, 12.0F, 0.0F));

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
            .texOffs(0, 36).addBox(-1.25F, 10.0F, -2.25F, 4.5F, 0.5F, 3.25F)
            .texOffs(0, 36).addBox(-1.25F, 10.0F, 1.0F, 4.5F, 0.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, 8.5F, 1.0F, 4.5F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, 8.5F, -2.25F, 4.5F, 1.5F, 3.25F)
            .texOffs(0, 36).addBox(2.0F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(2.0F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(2.0F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(2.0F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, -2.0F, -2.25F, 4.5F, 2.5F, 3.25F)
            .texOffs(0, 36).addBox(-1.25F, -2.0F, 1.0F, 4.5F, 2.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, -2.5F, 1.0F, 4.5F, 0.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.25F, -2.5F, -2.25F, 4.5F, 0.5F, 3.25F)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition robe2Definition = leftArmDefinition.addOrReplaceChild("robe2",
          CubeListBuilder.create()
            .texOffs(58, 47).addBox(2.25F, -3.0F, -5.0F, 4.5F, 0.5F, 6.0F).mirror()
            .texOffs(58, 46).addBox(6.0F, -2.5F, -5.0F, 0.75F, 2.25F, 6.0F).mirror()
            .texOffs(58, 47).addBox(6.0F, -0.25F, -2.0F, 0.75F, 1.0F, 3.0F).mirror()
            .texOffs(58, 47).addBox(6.0F, -0.25F, -5.0F, 0.75F, 0.75F, 3.0F).mirror()
            .texOffs(58, 47).addBox(2.25F, -2.5F, -5.0F, 3.75F, 2.0F, 1.0F).mirror()
            .texOffs(58, 47).addBox(4.25F, -0.5F, -5.0F, 1.75F, 1.0F, 1.0F).mirror()
            .texOffs(58, 47).addBox(2.25F, -0.5F, -5.0F, 2.0F, 0.5F, 1.0F).mirror()
          , PartPose.offset(-3.0F, 0.0F, 2.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-4.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(0, 36).addBox(-4.25F, -2.5F, -2.25F, 4.5F, 0.5F, 3.25F)
            .texOffs(0, 36).addBox(-4.25F, -2.5F, 1.0F, 4.5F, 0.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, -2.0F, 1.0F, 4.5F, 2.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, -2.0F, -2.25F, 4.5F, 2.5F, 3.25F)
            .texOffs(0, 36).addBox(-1.0F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 0.5F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.0F, 0.5F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.0F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 7.0F, 1.0F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-1.0F, 7.0F, -2.25F, 1.25F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 8.5F, -2.25F, 4.5F, 1.5F, 3.25F)
            .texOffs(0, 36).addBox(-4.25F, 8.5F, 1.0F, 4.5F, 1.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 10.0F, 1.0F, 4.5F, 0.5F, 1.25F)
            .texOffs(0, 36).addBox(-4.25F, 10.0F, -2.25F, 4.5F, 0.5F, 3.25F)
          , PartPose.offset(-4.0F, 2.0F, 0.0F));

        PartDefinition robe1Definition = rightArmDefinition.addOrReplaceChild("robe1",
          CubeListBuilder.create()
            .texOffs(58, 47).addBox(-6.75F, -3.0F, -5.0F, 4.5F, 0.5F, 6.0F)
            .texOffs(58, 46).addBox(-6.75F, -2.5F, -5.0F, 0.75F, 2.25F, 6.0F)
            .texOffs(58, 47).addBox(-6.75F, -0.25F, -2.0F, 0.75F, 1.0F, 3.0F)
            .texOffs(58, 47).addBox(-6.75F, -0.25F, -5.0F, 0.75F, 0.75F, 3.0F)
            .texOffs(58, 47).addBox(-6.0F, -2.5F, -5.0F, 3.75F, 2.0F, 1.0F)
            .texOffs(58, 47).addBox(-6.0F, -0.5F, -5.0F, 1.75F, 1.0F, 1.0F)
            .texOffs(58, 47).addBox(-4.25F, -0.5F, -5.0F, 2.0F, 0.5F, 1.0F)
          , PartPose.offset(2.0F, 0.0F, 2.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F).mirror()
            .texOffs(70, 5).addBox(-4.5F, 10.0F, -2.5F, 9.0F, 3.0F, 0.5F)
            .texOffs(97, 1).addBox(-2.5F, 13.0F, -2.5F, 5.0F, 8.0F, 0.5F)
            .texOffs(70, 5).addBox(-4.5F, 10.0F, 2.0F, 9.0F, 3.0F, 0.5F)
            .texOffs(75, 2).addBox(-4.5F, 10.0F, -2.0F, 0.5F, 3.0F, 4.0F)
            .texOffs(76, 2).addBox(4.0F, 10.0F, -2.0F, 0.5F, 3.0F, 4.0F)
            .texOffs(32, 36).addBox(3.25F, 8.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(28, 36).addBox(2.5F, 8.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(31, 36).addBox(-3.0F, 0.0F, -2.5F, 1.0F, 2.0F, 0.5F)
            .texOffs(28, 36).addBox(-4.0F, 0.0F, -2.5F, 1.0F, 2.0F, 0.5F)
            .texOffs(28, 36).addBox(1.0F, 6.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(32, 36).addBox(1.75F, 6.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(28, 36).addBox(-0.5F, 4.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(32, 36).addBox(0.25F, 4.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(28, 36).addBox(-2.0F, 2.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(32, 36).addBox(-1.25F, 2.0F, -2.5F, 0.75F, 2.0F, 0.5F)
            .texOffs(23, 48).addBox(2.25F, 8.75F, 2.0F, 1.75F, 1.25F, 0.5F)
            .texOffs(27, 48).addBox(-3.75F, 8.75F, 2.0F, 1.75F, 1.25F, 0.5F)
            .texOffs(27, 48).addBox(-2.0F, 6.75F, 2.0F, 1.75F, 2.0F, 0.5F)
            .texOffs(23, 48).addBox(0.5F, 6.75F, 2.0F, 1.75F, 2.0F, 0.5F)
            .texOffs(23, 49).addBox(0.5F, 2.0F, 2.0F, 1.75F, 2.0F, 0.5F)
            .texOffs(23, 49).addBox(2.25F, 0.0F, 2.0F, 1.75F, 2.0F, 0.5F)
            .texOffs(26, 49).addBox(-0.75F, 4.0F, 2.0F, 1.75F, 2.75F, 0.5F)
            .texOffs(58, 49).addBox(-1.53F, 5.35F, 5.36F, 10.25F, 7.75F, 0.5F)
            .texOffs(66, 49).addBox(-8.53F, 5.35F, 5.36F, 7.0F, 7.75F, 0.5F)
            .texOffs(66, 49).addBox(-8.53F, 5.35F, 2.11F, 3.25F, 7.75F, 3.25F)
            .texOffs(66, 49).addBox(5.47F, 5.35F, 2.11F, 3.25F, 7.75F, 3.25F)
            .texOffs(59, 49).addBox(-5.78F, 10.6F, 2.11F, 11.25F, 2.5F, 3.25F)
            .texOffs(66, 49).addBox(-8.509F, 2.35F, 2.11F, 3.25F, 3.0F, 2.25F)
            .texOffs(66, 49).addBox(5.45F, 2.35F, 2.11F, 3.25F, 3.0F, 2.25F)
            .texOffs(27, 49).addBox(-2.0F, 2.0F, 2.0F, 1.75F, 2.0F, 0.5F)
            .texOffs(27, 49).addBox(-3.75F, 0.0F, 2.0F, 1.75F, 2.0F, 0.5F)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition robe3Definition = bodyDefinition.addOrReplaceChild("robe3",
          CubeListBuilder.create()
            .texOffs(58, 49).addBox(-1.52F, -21.4F, 12.7123F, 10.25F, 3.5F, 0.5F)
            .texOffs(66, 49).addBox(-8.52F, -21.4F, 12.7123F, 7.0F, 3.5F, 0.5F)
            .texOffs(66, 49).addBox(-8.51F, -21.6113F, 11.7591F, 0.5F, 7.0F, 1.0F)
            .texOffs(66, 49).addBox(8.24F, -21.6113F, 11.7591F, 0.5F, 7.0F, 1.0F)
            .texOffs(66, 49).addBox(-8.52F, -17.9F, 12.6982F, 7.0F, 3.5F, 0.5F)
            .texOffs(58, 49).addBox(-1.52F, -17.9F, 12.6982F, 10.25F, 3.5F, 0.5F)
          , PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F,  0.4363F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition,  128,  64 );
    }
}
