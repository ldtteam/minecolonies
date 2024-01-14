package com.minecolonies.core.client.model.raiders;

import com.minecolonies.api.client.render.modeltype.EgyptianModel;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

/**
 * Create a mummy model. Created using Tabula 7.0.0
 */
public class ModelMummy extends EgyptianModel<AbstractEntityEgyptian>
{
    private ModelPart stripRightA;
    private ModelPart stripRightB;
    private ModelPart stripLeftA;

    public ModelMummy(final ModelPart part)
    {
        super(part);
        hat.visible = false;
        stripLeftA = part.getChild("left_arm").getChild("stripLeftA");
        stripRightB = part.getChild("right_arm").getChild("stripRightB");
        stripRightA = part.getChild("right_arm").getChild("stripRightA");
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();

        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create().texOffs(40, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4)
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create().texOffs(24, 16).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4)
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition stripRightBDefinition = rightArmDefinition.addOrReplaceChild("stripRightB",
          CubeListBuilder.create().texOffs(36, -2).addBox(0.0F, 0.0F, -1.0F, 0, 5, 2)
          , PartPose.offset(1.3F, 5.0F, 1.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition armRightLayerDefinition = rightArmDefinition.addOrReplaceChild("armRightLayer",
          CubeListBuilder.create().texOffs(24, 32).addBox(-3.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.3F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create().texOffs(24, 16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4)
          , PartPose.offset(-1.9F, 12.0F, 0.1F));

        PartDefinition stripLeftADefinition = leftArmDefinition.addOrReplaceChild("stripLeftA",
          CubeListBuilder.create().texOffs(40, -2).addBox(0.0F, 0.0F, -1.0F, 0, 7, 2)
          , PartPose.offset(3.3F, 5.0F, 2.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8)
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create().texOffs(40,16).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4)
          , PartPose.offset(1.9F, 12.0F, 0.1F));

        PartDefinition bodyLayerDefinition = bodyDefinition.addOrReplaceChild("bodyLayer",
          CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4, new CubeDeformation(0.5F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition legLeftLayerDefinition = leftLegDefinition.addOrReplaceChild("legLeftLayer",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition armLeftLayerDefinition = leftArmDefinition.addOrReplaceChild("armLeftLayer",
          CubeListBuilder.create().texOffs(40, 32).addBox(-1.0F, -2.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.3F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition stripRightADefinition = rightArmDefinition.addOrReplaceChild("stripRightA",
          CubeListBuilder.create().texOffs(32, -2).addBox(0.0F, 0.0F, -1.0F, 0, 10, 2)
          , PartPose.offset(-3.3F, 7.0F, 1.0F));

        PartDefinition legRightLayerDefinition = rightLegDefinition.addOrReplaceChild("legRightLayer",
          CubeListBuilder.create().texOffs(40, 32).addBox(-2.0F, 0.0F, -2.0F, 4, 12, 4, new CubeDeformation(0.3F))
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition,  64,  64 );
    }

    /**
     * this is a helper function from tabula to set the rotation of model parts
     *
     * @param modelRenderer the model renderer.
     * @param z             the z coord.
     * @param y             the y coord.
     * @param x             the x coord.
     */
    public void setRotateAngle(ModelPart modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }

    @Override
    public void setupAnim(AbstractEntityEgyptian entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float f = 0.05F * Mth.sin((float) Math.PI * ageInTicks / 30.0F) % 2.0F;
        setRotateAngle(this.stripLeftA,
          -1.1F * this.leftArm.xRot + f,
          -this.leftArm.yRot,
          -this.leftArm.zRot + f);
        setRotateAngle(this.stripRightA,
          -1.1F * this.rightArm.xRot + f,
          -this.rightArm.yRot,
          -this.rightArm.zRot + f);
        setRotateAngle(this.stripRightB,
          -1.1F * this.rightArm.xRot + f,
          -this.rightArm.yRot,
          -this.rightArm.zRot + f);
    }
}
