package com.minecolonies.coremod.client.model.raiders;

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
        stripLeftA = part.getChild("stripLeftA");
        stripRightB = part.getChild("stripRightB");
        stripRightA = part.getChild("stripRightA");
    }

    public static LayerDefinition createMesh()
    {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partDefinition = meshdefinition.getRoot();
        
        PartDefinition leftArmDefinition = partDefinition.addOrReplaceChild("left_arm",
          CubeListBuilder.create()
          , PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition rightArmDefinition = partDefinition.addOrReplaceChild("right_arm",
          CubeListBuilder.create()
          , PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition stripRightBDefinition = rightArmDefinition.addOrReplaceChild("stripRightB",
          CubeListBuilder.create()
          , PartPose.offset(1.3F, 5.0F, 1.0F));

        PartDefinition bodyDefinition = partDefinition.addOrReplaceChild("body",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition armRightLayerDefinition = rightArmDefinition.addOrReplaceChild("armRightLayer",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition rightLegDefinition = partDefinition.addOrReplaceChild("right_leg",
          CubeListBuilder.create()
          , PartPose.offset(-1.9F, 12.0F, 0.1F));

        PartDefinition stripLeftADefinition = leftArmDefinition.addOrReplaceChild("stripLeftA",
          CubeListBuilder.create()
          , PartPose.offset(3.3F, 5.0F, 2.0F));

        PartDefinition headDefinition = partDefinition.addOrReplaceChild("head",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition leftLegDefinition = partDefinition.addOrReplaceChild("left_leg",
          CubeListBuilder.create()
          , PartPose.offset(1.9F, 12.0F, 0.1F));

        PartDefinition bodyLayerDefinition = bodyDefinition.addOrReplaceChild("bodyLayer",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition legLeftLayerDefinition = leftLegDefinition.addOrReplaceChild("legLeftLayer",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition armLeftLayerDefinition = leftArmDefinition.addOrReplaceChild("armLeftLayer",
          CubeListBuilder.create()
          , PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition stripRightADefinition = rightArmDefinition.addOrReplaceChild("stripRightA",
          CubeListBuilder.create()
          , PartPose.offset(-3.3F, 7.0F, 1.0F));

        PartDefinition legRightLayerDefinition = rightLegDefinition.addOrReplaceChild("legRightLayer",
          CubeListBuilder.create()
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
