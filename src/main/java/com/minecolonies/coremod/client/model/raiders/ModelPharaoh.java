package com.minecolonies.coremod.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * ModelPharaohMummy. Created using Tabula 7.0.0
 */
public class ModelPharaoh extends EgyptianModel<AbstractEntityEgyptian>
{
    private ModelPart bodyGoldenStrip;
    private ModelPart jaw;

    public ModelPharaoh(final ModelPart part)
    {
        super(part);
        hat.visible = false;
        bodyGoldenStrip = part.getChild("body").getChild("bodyGoldenStrip");
        jaw = part.getChild("head").getChild("jaw");
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
            .texOffs(24, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
            .texOffs(24, 32).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.3F)).mirror()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F).mirror()
            .texOffs(56, 20).addBox(-2.4F, -0.5F, -2.5F, 5.0F, 6.0F, 5.0F).mirror()
            .texOffs(40, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.2F)).mirror()
          , PartPose.offset(1.9F, 12.0F, 0.1F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
            .texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F).mirror()
            .texOffs(44, 0).addBox(-5.5F, -0.2F, -2.5F, 11.0F, 5.0F, 5.0F, new CubeDeformation( 0.15F)).mirror()
            .texOffs(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation( 0.3F)).mirror()
            .texOffs(52, 11).addBox(-4.5F, 8.5F, -2.5F, 9.0F, 4.0F, 5.0F).mirror()
            .texOffs(0, 0).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation( -0.3F)).mirror()
            .texOffs(38, 12).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 8.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 10.0F, -2.6F));

        PartDefinition bodyJewelDefinition = bodyDefinition.addOrReplaceChild("bodyJewel",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation( -0.3F)).mirror()
          , PartPose.offsetAndRotation(0.0F, 10.0F, -2.0F,  0.0F, 0.0F, 0.7854F));

        PartDefinition bodyGoldenStripDefinition = bodyDefinition.addOrReplaceChild("bodyGoldenStrip",
          CubeListBuilder.create()
            .texOffs(38, 12).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 8.0F, 0.0F).mirror()
          , PartPose.offset(0.0F, 10.0F, -2.6F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
            .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(40, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.3F))
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
            .texOffs(24, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F)
            .texOffs(56, 20).addBox(-2.6F, -0.5F, -2.5F, 5.0F, 6.0F, 5.0F)
            .texOffs(24, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation( 0.2F))
          , PartPose.offset(-1.9F, 12.0F, 0.1F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
            .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 6.0F, 8.0F).mirror()
            .texOffs(80, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation( 0.1F)).mirror()
            .texOffs(38, 48).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 2.0F, 4.0F).mirror()
            .texOffs(14, 59).addBox(-4.5F, -2.85F, 1.0F, 9.0F, 3.0F, 0.0F).mirror()
            .texOffs(0, 14).addBox(-2.5F, -2.0F, -3.5F, 5.0F, 1.0F, 0.0F).mirror()
            .texOffs(10, 10).addBox(-2.5F, -2.0F, -3.5F, 0.0F, 1.0F, 4.0F).mirror()
            .texOffs(10, 10).addBox(2.5F, -2.0F, -3.5F, 0.0F, 1.0F, 4.0F).mirror()
            .texOffs(0, 51).addBox(-5.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, new CubeDeformation( 0.13F))
            .texOffs(14, 51).addBox(0.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, new CubeDeformation( 0.14F))
            .texOffs(0, 57).addBox(0.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F)
            .texOffs(0, 51).addBox(0.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, new CubeDeformation( 0.13F)).mirror()
            .texOffs(14, 51).addBox(-5.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, new CubeDeformation( 0.14F)).mirror()
            .texOffs(0, 57).addBox(-3.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F).mirror()
            .texOffs(28, 51).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation( -0.2F)).mirror()
            .texOffs(76, 18).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 4.0F, 8.0F, new CubeDeformation( 0.09F)).mirror()
            .texOffs(18, 49).addBox(-4.5F, 0.15F, 0.19F, 9.0F, 0.0F, 2.0F, new CubeDeformation( 0.13F)).mirror()
          , PartPose.offset(0.0F, -11.4F, 0.85F));

        PartDefinition snakeBodyDefinition = headDefinition.addOrReplaceChild("snakeBody",
          CubeListBuilder.create()
            .texOffs(76, 2).addBox(-0.5F, -3.0F, -1.0F, 1.0F, 3.0F, 1.0F).mirror()
          , PartPose.offsetAndRotation(0.0F, -7.5F, -4.6F,  -0.3491F, 0.0F, 0.0F));

        PartDefinition snakeHeadDefinition = headDefinition.addOrReplaceChild("snakeHead",
          CubeListBuilder.create()
            .texOffs(77, 3).addBox(-1.5F, -1.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation( -0.99F)).mirror()
          , PartPose.offsetAndRotation(0.0F, -10.65F, -4.5F,  0.2793F, 0.0F, 0.0F));

        PartDefinition headRightSideTopDefinition = headDefinition.addOrReplaceChild("headRightSideTop",
          CubeListBuilder.create()
            .texOffs(0, 51).addBox(-5.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, new CubeDeformation( 0.13F))
          , PartPose.offsetAndRotation(-4.6F, -11.4F, 0.86F,  0.0F, 0.0F, -0.8901F));

        PartDefinition headRightSideMiddleDefinition = headDefinition.addOrReplaceChild("headRightSideMiddle",
          CubeListBuilder.create()
            .texOffs(14, 51).addBox(0.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, new CubeDeformation( 0.14F))
          , PartPose.offsetAndRotation(-7.82F, -7.11F, 0.86F,  0.0F, 0.0F, 0.2688F));

        PartDefinition headRightSideBottomDefinition = headDefinition.addOrReplaceChild("headRightSideBottom",
          CubeListBuilder.create()
            .texOffs(0, 57).addBox(0.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F)
          , PartPose.offsetAndRotation(-9.58F, -1.23F, 0.91F,  0.0F, -0.9147F, 0.2688F));

        PartDefinition headLeftSideTopDefinition = headDefinition.addOrReplaceChild("headLeftSideTop",
          CubeListBuilder.create()
            .texOffs(0, 51).addBox(0.15F, 0.13F, 0.19F, 5.0F, 4.0F, 2.0F, new CubeDeformation( 0.13F)).mirror()
          , PartPose.offsetAndRotation(4.6F, -11.4F, 0.86F,  0.0F, 0.0F, 0.8901F));

        PartDefinition headLeftSideMiddleDefinition = headDefinition.addOrReplaceChild("headLeftSideMiddle",
          CubeListBuilder.create()
            .texOffs(14, 51).addBox(-5.0F, 0.0F, 0.19F, 5.0F, 6.0F, 2.0F, new CubeDeformation( 0.14F)).mirror()
          , PartPose.offsetAndRotation(7.82F, -7.11F, 0.86F,  0.0F, 0.0F, -0.2688F));

        PartDefinition headLeftSideBottomDefinition = headDefinition.addOrReplaceChild("headLeftSideBottom",
          CubeListBuilder.create()
            .texOffs(0, 57).addBox(-3.0F, -1.0F, -4.0F, 3.0F, 1.0F, 4.0F).mirror()
          , PartPose.offsetAndRotation(9.58F, -1.23F, 0.91F,  0.0F, 0.9147F, -0.2688F));

        PartDefinition headTailDefinition = headDefinition.addOrReplaceChild("headTail",
          CubeListBuilder.create()
            .texOffs(28, 51).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation( -0.2F)).mirror()
          , PartPose.offset(1.5F, 0.4F, 2.5F));

        PartDefinition headTopDefinition = headDefinition.addOrReplaceChild("headTop",
          CubeListBuilder.create()
            .texOffs(76, 18).addBox(-4.5F, 0.0F, 0.0F, 9.0F, 4.0F, 8.0F, new CubeDeformation( 0.09F)).mirror()
          , PartPose.offsetAndRotation(0.0F, -8.55F, -4.45F,  0.4714F, 0.0F, 0.0F));

        PartDefinition headCapDefinition = headDefinition.addOrReplaceChild("headCap",
          CubeListBuilder.create()
            .texOffs(18, 49).addBox(-4.5F, 0.15F, 0.19F, 9.0F, 0.0F, 2.0F, new CubeDeformation( 0.13F)).mirror()
          , PartPose.offset(0.0F, -11.4F, 0.85F));

        PartDefinition jawDefinition = headDefinition.addOrReplaceChild("jaw",
          CubeListBuilder.create()
            .texOffs(33, 54).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 1.0F, 5.0F).mirror()
            .texOffs(10, 11).addBox(2.5F, 0.0F, -4.0F, 0.0F, 1.0F, 4.0F).mirror()
            .texOffs(0, 15).addBox(-2.5F, 0.0F, -4.0F, 5.0F, 1.0F, 0.0F).mirror()
            .texOffs(10, 11).addBox(-2.5F, 0.0F, -4.0F, 0.0F, 1.0F, 4.0F).mirror()
          , PartPose.offset(0.0F, -2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition,  128,  64 );
    }

    private static float sinPi(float f)
    {
        return Mth.sin(f * (float) Math.PI);
    }

    @Override
    public void setupAnim(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        this.bodyGoldenStrip.xRot = -Math.max(this.rightLeg.xRot, this.leftLeg.xRot);
        this.jaw.xRot = 0.3F - 0.1F * sinPi(ageInTicks / 20.0F) % 2.0F;
        this.jaw.yRot = 0.05F * sinPi((ageInTicks + 10.0F) / 20.0F) % 2.0F;
    }
}
