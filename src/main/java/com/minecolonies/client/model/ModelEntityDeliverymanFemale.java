package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelEntityDeliverymanFemale extends ModelBiped
{
    //fields
    ModelRenderer Chest;
    ModelRenderer RightB;
    ModelRenderer Top;
    ModelRenderer LeftB;
    ModelRenderer RightM;
    ModelRenderer FrontLB;
    ModelRenderer LeftM;
    ModelRenderer Back;
    ModelRenderer BackM;
    ModelRenderer FrontM;
    ModelRenderer Front1;
    ModelRenderer FrontLM;
    ModelRenderer FrontRM;
    ModelRenderer FrontRB;
    ModelRenderer Front2;
    ModelRenderer Base;
    ModelRenderer RimL;
    ModelRenderer Lid1;
    ModelRenderer Lock1;
    ModelRenderer Lock2;
    ModelRenderer RimB;
    ModelRenderer RimR;
    ModelRenderer RimF;
    ModelRenderer Lid2;
    ModelRenderer Handle;
    ModelRenderer Block1;
    ModelRenderer Block2;
    ModelRenderer Block3;
    ModelRenderer Block4;
    ModelRenderer Torch;

    public ModelEntityDeliverymanFemale()
    {
        textureWidth = 256;
        textureHeight = 128;

        bipedHead = new ModelRenderer(this, 0, 0);
        bipedHead.addBox(-3.99F, -6F, -3.9F, 8, 8, 8);
        bipedHead.setRotationPoint(0F, 2F, -4F);
        bipedHead.setTextureSize(256, 128);
        bipedHead.mirror = true;
        setRotation(bipedHead, 0.3490659F, 0F, 0F);

        Chest = new ModelRenderer(this, 0, 32);
        Chest.addBox(-4.01F, 4F, -1.4F, 8, 3, 3);
        Chest.setRotationPoint(0F, 1F, -2F);
        Chest.setTextureSize(256, 128);
        Chest.mirror = true;
        setRotation(Chest, -0.431685F, 0F, 0F);

        bipedLeftLeg = new ModelRenderer(this, 0, 16);
        bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
        bipedLeftLeg.setTextureSize(256, 128);
        bipedLeftLeg.mirror = true;
        setRotation(bipedLeftLeg, 0F, 0F, 0F);

        bipedRightLeg = new ModelRenderer(this, 0, 16);
        bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        bipedRightLeg.setRotationPoint(-2F, 12F, 0F);
        bipedRightLeg.setTextureSize(256, 128);
        bipedRightLeg.mirror = true;
        setRotation(bipedRightLeg, 0F, 0F, 0F);

        bipedLeftArm = new ModelRenderer(this, 40, 16);
        bipedLeftArm.addBox(0F, 0F, -1.9F, 4, 12, 4);
        bipedLeftArm.setRotationPoint(4F, 2F, -4F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-4F, 0F, -1.9F, 4, 12, 4);
        bipedRightArm.setRotationPoint(-4F, 2F, -4F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4.01F, 0F, -3.9F, 8, 12, 4);
        bipedBody.setRotationPoint(0F, 1F, -2F);
        bipedBody.setTextureSize(256, 128);
        bipedBody.mirror = true;
        setRotation(bipedBody, 0.3490659F, 0F, 0F);

        RightB = new ModelRenderer(this, 7, 91);
        RightB.addBox(-5.1F, 3.3F, 3.2F, 1, 4, 1);
        RightB.setRotationPoint(0F, 2F, -4F);
        RightB.setTextureSize(256, 128);
        RightB.mirror = true;
        setRotation(RightB, 0.23753F, 0F, 0.0743572F);

        Top = new ModelRenderer(this, 0, 39);
        Top.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
        Top.setRotationPoint(0F, 2F, -4F);
        Top.setTextureSize(256, 128);
        Top.mirror = true;
        setRotation(Top, 0.3490659F, 0F, 0F);

        LeftB = new ModelRenderer(this, 0, 91);
        LeftB.addBox(3.8F, 3F, 3.5F, 1, 4, 1);
        LeftB.setRotationPoint(0F, 2F, -4F);
        LeftB.setTextureSize(256, 128);
        LeftB.mirror = true;
        setRotation(LeftB, 0.1631728F, 0F, -0.0734691F);

        RightM = new ModelRenderer(this, 7, 86);
        RightM.addBox(-4.6F, 1.3F, 2.2F, 1, 3, 2);
        RightM.setRotationPoint(0F, 2F, -4F);
        RightM.setTextureSize(256, 128);
        RightM.mirror = true;
        setRotation(RightM, 0.3490659F, 0F, 0.2230717F);

        FrontLB = new ModelRenderer(this, 0, 64);
        FrontLB.addBox(3.5F, -3.5F, -6F, 1, 2, 1);
        FrontLB.setRotationPoint(0F, 2F, -4F);
        FrontLB.setTextureSize(256, 128);
        FrontLB.mirror = true;
        setRotation(FrontLB, 0F, 0F, 0F);

        LeftM = new ModelRenderer(this, 0, 86);
        LeftM.addBox(3.5F, 1.2F, 2.2F, 1, 3, 2);
        LeftM.setRotationPoint(0F, 2F, -4F);
        LeftM.setTextureSize(256, 128);
        LeftM.mirror = true;
        setRotation(LeftM, 0.3490659F, 0F, -0.1862235F);

        Back = new ModelRenderer(this, 0, 76);
        Back.addBox(-4.5F, -7.5F, 2.5F, 9, 8, 2);
        Back.setRotationPoint(0F, 2F, -4F);
        Back.setTextureSize(256, 128);
        Back.mirror = true;
        setRotation(Back, 0.3490659F, 0F, 0F);

        BackM = new ModelRenderer(this, 0, 70);
        BackM.addBox(-4.5F, -7.5F, 0.5F, 9, 4, 2);
        BackM.setRotationPoint(0F, 2F, -4F);
        BackM.setTextureSize(256, 128);
        BackM.mirror = true;
        setRotation(BackM, 0.3490659F, 0F, 0F);

        FrontM = new ModelRenderer(this, 0, 49);
        FrontM.addBox(-4.5F, -7.5F, -3.5F, 9, 2, 4);
        FrontM.setRotationPoint(0F, 2F, -4F);
        FrontM.setTextureSize(256, 128);
        FrontM.mirror = true;
        setRotation(FrontM, 0.3490659F, 0F, 0F);

        Front1 = new ModelRenderer(this, 0, 55);
        Front1.addBox(-4.48F, -6.5F, -7F, 9, 2, 1);
        Front1.setRotationPoint(0F, 2F, -4F);
        Front1.setTextureSize(256, 128);
        Front1.mirror = true;
        setRotation(Front1, 0F, 0F, 0F);

        FrontLM = new ModelRenderer(this, 0, 60);
        FrontLM.addBox(3.49F, -5.5F, -6.5F, 1, 2, 2);
        FrontLM.setRotationPoint(0F, 2F, -4F);
        FrontLM.setTextureSize(256, 128);
        FrontLM.mirror = true;
        setRotation(FrontLM, 0F, 0F, 0F);

        FrontRM = new ModelRenderer(this, 7, 60);
        FrontRM.addBox(-4.49F, -5.5F, -6.5F, 1, 3, 2);
        FrontRM.setRotationPoint(0F, 2F, -4F);
        FrontRM.setTextureSize(256, 128);
        FrontRM.mirror = true;
        setRotation(FrontRM, 0F, 0F, 0F);

        FrontRB = new ModelRenderer(this, 7, 65);
        FrontRB.addBox(-4.5F, -2.5F, -6F, 1, 3, 1);
        FrontRB.setRotationPoint(0F, 2F, -4F);
        FrontRB.setTextureSize(256, 128);
        FrontRB.mirror = true;
        setRotation(FrontRB, 0F, 0F, 0F);

        Front2 = new ModelRenderer(this, 0, 58);
        Front2.addBox(-4.51F, -7.5F, -4.5F, 9, 1, 1);
        Front2.setRotationPoint(0F, 2F, -4F);
        Front2.setTextureSize(256, 128);
        Front2.mirror = true;
        setRotation(Front2, 0.3490659F, 0F, 0F);

        Base = new ModelRenderer(this, 32, 50);
        Base.addBox(-4F, 4F, 0.1F, 8, 6, 6);
        Base.setRotationPoint(0F, 1F, -2F);
        Base.setTextureSize(256, 128);
        Base.mirror = true;
        setRotation(Base, 0.3490659F, 0F, 0F);

        RimL = new ModelRenderer(this, 42, 65);
        RimL.addBox(3F, 3F, 1.1F, 1, 1, 4);
        RimL.setRotationPoint(0F, 1F, -2F);
        RimL.setTextureSize(256, 128);
        RimL.mirror = true;
        setRotation(RimL, 0.3490659F, 0F, 0F);

        Lid1 = new ModelRenderer(this, 50, 70);
        Lid1.addBox(-4F, -4F, 2F, 8, 6, 1);
        Lid1.setRotationPoint(0F, 1F, -2F);
        Lid1.setTextureSize(256, 128);
        Lid1.mirror = true;
        setRotation(Lid1, -0.4363323F, 0F, 0F);

        Lock1 = new ModelRenderer(this, 50, 77);
        Lock1.addBox(-0.5F, -4.01F, 2.5F, 1, 0, 2);
        Lock1.setRotationPoint(0F, 1F, -2F);
        Lock1.setTextureSize(256, 128);
        Lock1.mirror = true;
        setRotation(Lock1, -0.4363323F, 0F, 0F);

        Lock2 = new ModelRenderer(this, 32, 77);
        Lock2.addBox(-0.5F, -6.1F, 2.5F, 1, 0, 2);
        Lock2.setRotationPoint(0F, 1F, -2F);
        Lock2.setTextureSize(256, 128);
        Lock2.mirror = true;
        setRotation(Lock2, -1.22173F, 0F, 0F);

        RimB = new ModelRenderer(this, 32, 63);
        RimB.addBox(-4F, 3F, 5.1F, 8, 1, 1);
        RimB.setRotationPoint(0F, 1F, -2F);
        RimB.setTextureSize(256, 128);
        RimB.mirror = true;
        setRotation(RimB, 0.3490659F, 0F, 0F);

        RimR = new ModelRenderer(this, 32, 65);
        RimR.addBox(-4F, 3F, 1.1F, 1, 1, 4);
        RimR.setRotationPoint(0F, 1F, -2F);
        RimR.setTextureSize(256, 128);
        RimR.mirror = true;
        setRotation(RimR, 0.3490659F, 0F, 0F);

        RimF = new ModelRenderer(this, 50, 63);
        RimF.addBox(-4F, 3F, 0.1F, 8, 1, 1);
        RimF.setRotationPoint(0F, 1F, -2F);
        RimF.setTextureSize(256, 128);
        RimF.mirror = true;
        setRotation(RimF, 0.3490659F, 0F, 0F);

        Lid2 = new ModelRenderer(this, 32, 70);
        Lid2.addBox(-4F, 0.12F, -2.99F, 8, 6, 1);
        Lid2.setRotationPoint(0F, 1F, -2F);
        Lid2.setTextureSize(256, 128);
        Lid2.mirror = true;
        setRotation(Lid2, 1.919862F, 0F, 0F);

        Handle = new ModelRenderer(this, 32, 80);
        Handle.addBox(-1F, 5F, -0.1F, 1, 2, 1);
        Handle.setRotationPoint(0F, 1F, -2F);
        Handle.setTextureSize(256, 128);
        Handle.mirror = true;
        setRotation(Handle, 1.129817F, 0.6320364F, 0.5205006F);

        Block1 = new ModelRenderer(this, 32, 84);
        Block1.addBox(-2F, 3.5F, -0.9F, 2, 2, 2);
        Block1.setRotationPoint(0F, 1F, -2F);
        Block1.setTextureSize(256, 128);
        Block1.mirror = true;
        setRotation(Block1, 0.6464947F, 0.7063936F, 0.4461433F);

        Block2 = new ModelRenderer(this, 40, 84);
        Block2.addBox(-2F, 4F, -0.9F, 2, 2, 2);
        Block2.setRotationPoint(0F, 1F, -2F);
        Block2.setTextureSize(256, 128);
        Block2.mirror = true;
        setRotation(Block2, 0.6464947F, 1.264073F, 0.5205006F);

        Block3 = new ModelRenderer(this, 32, 88);
        Block3.addBox(-3F, 4F, -2.9F, 2, 2, 2);
        Block3.setRotationPoint(0F, 1F, -2F);
        Block3.setTextureSize(256, 128);
        Block3.mirror = true;
        setRotation(Block3, 1.129817F, 0.8922867F, 0.5205006F);

        Block4 = new ModelRenderer(this, 40, 88);
        Block4.addBox(-3F, 4F, -2.9F, 2, 2, 2);
        Block4.setRotationPoint(0F, 1F, -2F);
        Block4.setTextureSize(256, 128);
        Block4.mirror = true;
        setRotation(Block4, 1.129817F, 0.4089647F, 0.5205006F);

        Torch = new ModelRenderer(this, 36, 80);
        Torch.addBox(-1F, 3F, -2.9F, 1, 2, 1);
        Torch.setRotationPoint(0F, 1F, -2F);
        Torch.setTextureSize(256, 128);
        Torch.mirror = true;
        setRotation(Torch, 1.129817F, 0.1115358F, 0.5205006F);

        bipedHeadwear.isHidden = true;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void render(final Entity entity, final float f, final float f1, final float f2, final float f3, final float f4, final float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);

        Chest.render(f5);
        RightB.render(f5);//hair
        Top.render(f5);//top of hair?
        LeftB.render(f5);//hair
        RightM.render(f5);//hair
        FrontLB.render(f5);//hair
        LeftM.render(f5);//hair
        Back.render(f5);//hair
        BackM.render(f5);//hair
        FrontM.render(f5);//hair
        Front1.render(f5);//hair
        FrontLM.render(f5);//hair
        FrontRM.render(f5);//hair
        FrontRB.render(f5);//hair
        Front2.render(f5);//hair
        Base.render(f5);//bag
        RimL.render(f5);//bag
        Lid1.render(f5);//bag
        Lock1.render(f5);//bag
        Lock2.render(f5);//bag
        RimB.render(f5);//bag
        RimR.render(f5);//bag
        RimF.render(f5);//bag
        Lid2.render(f5);//bag
        Handle.render(f5);//bag
        Block1.render(f5);
        Block2.render(f5);
        Block3.render(f5);
        Block4.render(f5);
        Torch.render(f5);
    }

    @Override
    public void setRotationAngles(final float limbSwing, final float limbSwingAmount, final float ageInTicks, final float netHeadYaw, final float headPitch, final float scaleFactor, final Entity entityIn)
    {
        final float bodyX = bipedBody.rotateAngleX;
        final float headX = bipedHead.rotateAngleX;

        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

        bipedBody.rotateAngleX = bodyX;
        bipedHead.rotateAngleX = headX;
    }
}