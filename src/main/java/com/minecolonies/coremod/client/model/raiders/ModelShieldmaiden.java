package com.minecolonies.coremod.client.model.raiders;// Made with Blockbench 3.5.2
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports

import com.minecolonies.api.client.render.modeltype.NorsemenModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ModelShieldmaiden extends NorsemenModel
{

    public ModelShieldmaiden(final ModelPart part)
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
            .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation( 0.5F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
            .texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition skirtDefinition = bodyDefinition.addOrReplaceChild("skirt",
          CubeListBuilder.create()
            .texOffs(67, 11).addBox(-3.5F, -0.25F, -3.5F, 9.0F, 6.0F, 6.0F)
            .texOffs(67, 0).addBox(-3.0F, -1.0F, -3.0F, 8.0F, 6.0F, 5.0F)
          , PartPose.offset(-0.95F, 12.25F, 0.5F));

        PartDefinition chestDefinition = bodyDefinition.addOrReplaceChild("chest",
          CubeListBuilder.create()
            .texOffs(67, 49).addBox(-4.0F, -23.8558F, -5.7275F, 8.0F, 5.0F, 4.0F, new CubeDeformation( 0.25F))
          , PartPose.offsetAndRotation(0.0F, 24.0F, -6.0F,  -0.4363F, 0.0F, 0.0F));

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

        PartDefinition shieldADefinition = bodyDefinition.addOrReplaceChild("shieldA",
          CubeListBuilder.create()
            .texOffs(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F)
            .texOffs(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F)
          , PartPose.offset(9.25F, 15.0F, -4.75F));

        PartDefinition shieldBDefinition = bodyDefinition.addOrReplaceChild("shieldB",
          CubeListBuilder.create()
            .texOffs(77, 27).addBox(0.0F, -6.0F, 4.0F, 1.0F, 2.0F, 2.0F)
            .texOffs(67, 26).addBox(-1.0F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F)
            .texOffs(94, 26).addBox(-0.5F, -11.0F, -1.0F, 1.0F, 12.0F, 12.0F)
          , PartPose.offsetAndRotation(5.0F, 12.0F, 4.0F,  0.0F, -1.5708F, 0.0F));

        return LayerDefinition.create(meshdefinition,  124,  64 );
    }
}
