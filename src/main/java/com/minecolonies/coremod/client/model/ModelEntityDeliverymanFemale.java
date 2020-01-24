package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityDeliverymanFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityDeliverymanFemale()
    {
        final ModelRenderer Chest;
        final ModelRenderer RightB;
        final ModelRenderer Top;
        final ModelRenderer LeftB;
        final ModelRenderer RightM;
        final ModelRenderer FrontLB;
        final ModelRenderer LeftM;
        final ModelRenderer Back;
        final ModelRenderer BackM;
        final ModelRenderer FrontM;
        final ModelRenderer Front1;
        final ModelRenderer FrontLM;
        final ModelRenderer FrontRM;
        final ModelRenderer FrontRB;
        final ModelRenderer Front2;
        final ModelRenderer Base;
        final ModelRenderer RimL;
        final ModelRenderer Lid1;
        final ModelRenderer Lock1;
        final ModelRenderer Lock2;
        final ModelRenderer RimB;
        final ModelRenderer RimR;
        final ModelRenderer RimF;
        final ModelRenderer Lid2;
        final ModelRenderer Handle;
        final ModelRenderer Block1;
        final ModelRenderer Block2;
        final ModelRenderer Block3;
        final ModelRenderer Block4;
        final ModelRenderer Torch;

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
        Chest.setRotationPoint(0F, 0F, 0F);
        Chest.setTextureSize(256, 128);
        Chest.mirror = true;
        setRotation(Chest, -0.780751F, 0F, 0F);

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
        bipedLeftArm.addBox(-1F, 0F, -4.9F, 4, 12, 4, 0F);
        bipedLeftArm.setRotationPoint(4F, 2F, -4F);
        bipedLeftArm.setTextureSize(256, 128);
        bipedLeftArm.mirror = true;
        setRotation(bipedLeftArm, 0F, 0F, 0F);

        bipedRightArm = new ModelRenderer(this, 40, 16);
        bipedRightArm.addBox(-3F, 0F, -4.9F, 4, 12, 4, 0F);
        bipedRightArm.setRotationPoint(-4F, 2F, -4F);
        bipedRightArm.setTextureSize(256, 128);
        bipedRightArm.mirror = true;
        setRotation(bipedRightArm, 0F, 0F, 0F);

        bipedBody = new ModelRenderer(this, 16, 16);
        bipedBody.addBox(-4F, 0F, -4F, 8, 12, 4, 0F);
        bipedBody.setRotationPoint(0F, 1F, -2F);

        RightB = new ModelRenderer(this, 7, 91);
        RightB.addBox(-5.1F, 3.3F, 3.2F, 1, 4, 1);
        RightB.setRotationPoint(0F, 0F, 0F);
        RightB.setTextureSize(256, 128);
        RightB.mirror = true;
        setRotation(RightB, -0.111536F, 0F, 0.0743572F);

        Top = new ModelRenderer(this, 0, 39);
        Top.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
        Top.setRotationPoint(0F, 0F, 0F);
        Top.setTextureSize(256, 128);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);

        LeftB = new ModelRenderer(this, 0, 91);
        LeftB.addBox(3.8F, 3F, 3.5F, 1, 4, 1);
        LeftB.setRotationPoint(0F, 0F, 0F);
        LeftB.setTextureSize(256, 128);
        LeftB.mirror = true;
        setRotation(LeftB, -0.185893F, 0F, -0.0734691F);

        RightM = new ModelRenderer(this, 7, 86);
        RightM.addBox(-4.6F, 1.3F, 2.2F, 1, 3, 2);
        RightM.setRotationPoint(0F, 0F, 0F);
        RightM.setTextureSize(256, 128);
        RightM.mirror = true;
        setRotation(RightM, 0F, 0F, 0.2230717F);

        FrontLB = new ModelRenderer(this, 0, 64);
        FrontLB.addBox(3.5F, -3.5F, -6F, 1, 2, 1);
        FrontLB.setRotationPoint(0F, 0F, 0F);
        FrontLB.setTextureSize(256, 128);
        FrontLB.mirror = true;
        setRotation(FrontLB, -0.3490659F, 0F, 0F);

        LeftM = new ModelRenderer(this, 0, 86);
        LeftM.addBox(3.5F, 1.2F, 2.2F, 1, 3, 2);
        LeftM.setRotationPoint(0F, 0F, 0F);
        LeftM.setTextureSize(256, 128);
        LeftM.mirror = true;
        setRotation(LeftM, 0F, 0F, -0.1862235F);

        Back = new ModelRenderer(this, 0, 76);
        Back.addBox(-4.5F, -7.5F, 2.5F, 9, 8, 2);
        Back.setRotationPoint(0F, 0F, 0F);
        Back.setTextureSize(256, 128);
        Back.mirror = true;
        setRotation(Back, 0F, 0F, 0F);

        BackM = new ModelRenderer(this, 0, 70);
        BackM.addBox(-4.5F, -7.5F, 0.5F, 9, 4, 2);
        BackM.setRotationPoint(0F, 0F, 0F);
        BackM.setTextureSize(256, 128);
        BackM.mirror = true;
        setRotation(BackM, 0F, 0F, 0F);

        FrontM = new ModelRenderer(this, 0, 49);
        FrontM.addBox(-4.5F, -7.5F, -3.5F, 9, 2, 4);
        FrontM.setRotationPoint(0F, 0F, 0F);
        FrontM.setTextureSize(256, 128);
        FrontM.mirror = true;
        setRotation(FrontM, 0F, 0F, 0F);

        Front1 = new ModelRenderer(this, 0, 55);
        Front1.addBox(-4.48F, -6.5F, -7F, 9, 2, 1);
        Front1.setRotationPoint(0F, 0F, 0F);
        Front1.setTextureSize(256, 128);
        Front1.mirror = true;
        setRotation(Front1, -0.3490659F, 0F, 0F);

        FrontLM = new ModelRenderer(this, 0, 60);
        FrontLM.addBox(3.49F, -5.5F, -6.5F, 1, 2, 2);
        FrontLM.setRotationPoint(0F, 0F, 0F);
        FrontLM.setTextureSize(256, 128);
        FrontLM.mirror = true;
        setRotation(FrontLM, -0.3490659F, 0F, 0F);

        FrontRM = new ModelRenderer(this, 7, 60);
        FrontRM.addBox(-4.49F, -5.5F, -6.5F, 1, 3, 2);
        FrontRM.setRotationPoint(0F, 0F, 0F);
        FrontRM.setTextureSize(256, 128);
        FrontRM.mirror = true;
        setRotation(FrontRM, -0.3490659F, 0F, 0F);

        FrontRB = new ModelRenderer(this, 7, 65);
        FrontRB.addBox(-4.5F, -2.5F, -6F, 1, 3, 1);
        FrontRB.setRotationPoint(0F, 0F, 0F);
        FrontRB.setTextureSize(256, 128);
        FrontRB.mirror = true;
        setRotation(FrontRB, -0.3490659F, 0F, 0F);

        Front2 = new ModelRenderer(this, 0, 58);
        Front2.addBox(-4.51F, -7.5F, -4.5F, 9, 1, 1);
        Front2.setRotationPoint(0F, 0F, 0F);
        Front2.setTextureSize(256, 128);
        Front2.mirror = true;
        setRotation(Front2, 0F, 0F, 0F);

        Base = new ModelRenderer(this, 32, 50);
        Base.addBox(-4F, 4F, 0.1F, 8, 6, 6);
        Base.setRotationPoint(0F, 0F, 0F);
        Base.setTextureSize(256, 128);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);

        RimL = new ModelRenderer(this, 42, 65);
        RimL.addBox(3F, 3F, 1.1F, 1, 1, 4);
        RimL.setRotationPoint(0F, 0F, 0F);
        RimL.setTextureSize(256, 128);
        RimL.mirror = true;
        setRotation(RimL, 0F, 0F, 0F);

        Lid1 = new ModelRenderer(this, 50, 70);
        Lid1.addBox(-4F, -4F, 2F, 8, 6, 1);
        Lid1.setRotationPoint(0F, 0F, 0F);
        Lid1.setTextureSize(256, 128);
        Lid1.mirror = true;
        setRotation(Lid1, -0.785398F, 0F, 0F);

        Lock1 = new ModelRenderer(this, 50, 77);
        Lock1.addBox(-0.5F, -4.01F, 2.5F, 1, 0, 2);
        Lock1.setRotationPoint(0F, 0F, 0F);
        Lock1.setTextureSize(256, 128);
        Lock1.mirror = true;
        setRotation(Lock1, -0.785398F, 0F, 0F);

        Lock2 = new ModelRenderer(this, 32, 77);
        Lock2.addBox(-0.5F, -6.1F, 2.5F, 1, 0, 2);
        Lock2.setRotationPoint(0F, 0F, 0F);
        Lock2.setTextureSize(256, 128);
        Lock2.mirror = true;
        setRotation(Lock2, -1.570796F, 0F, 0F);

        RimB = new ModelRenderer(this, 32, 63);
        RimB.addBox(-4F, 3F, 5.1F, 8, 1, 1);
        RimB.setRotationPoint(0F, 0F, 0F);
        RimB.setTextureSize(256, 128);
        RimB.mirror = true;
        setRotation(RimB, 0F, 0F, 0F);

        RimR = new ModelRenderer(this, 32, 65);
        RimR.addBox(-4F, 3F, 1.1F, 1, 1, 4);
        RimR.setRotationPoint(0F, 0F, 0F);
        RimR.setTextureSize(256, 128);
        RimR.mirror = true;
        setRotation(RimR, 0F, 0F, 0F);

        RimF = new ModelRenderer(this, 50, 63);
        RimF.addBox(-4F, 3F, 0.1F, 8, 1, 1);
        RimF.setRotationPoint(0F, 0F, 0F);
        RimF.setTextureSize(256, 128);
        RimF.mirror = true;
        setRotation(RimF, 0F, 0F, 0F);

        Lid2 = new ModelRenderer(this, 32, 70);
        Lid2.addBox(-4F, 0.12F, -2.99F, 8, 6, 1);
        Lid2.setRotationPoint(0F, 0F, 0F);
        Lid2.setTextureSize(256, 128);
        Lid2.mirror = true;
        setRotation(Lid2, 1.570796F, 0F, 0F);

        Handle = new ModelRenderer(this, 32, 80);
        Handle.addBox(-1F, 5F, -0.1F, 1, 2, 1);
        Handle.setRotationPoint(0F, 0F, 0F);
        Handle.setTextureSize(256, 128);
        Handle.mirror = true;
        //setRotation(Handle, 1.129817F, 0.6320364F, 0.5205006F);
        setRotation(Handle, 1.129978F, 0.538153F, 0.617719F);

        Block1 = new ModelRenderer(this, 32, 84);
        Block1.addBox(-2F, 3.5F, -0.9F, 2, 2, 2);
        Block1.setRotationPoint(0F, 0F, 0F);
        Block1.setTextureSize(256, 128);
        Block1.mirror = true;
        //setRotation(Block1, 0.6464947F, 0.7063936F, 0.4461433F);
        setRotation(Block1, 0.650207F, 0.625571F, 0.561303F);

        Block2 = new ModelRenderer(this, 40, 84);
        Block2.addBox(-2F, 4F, -0.9F, 2, 2, 2);
        Block2.setRotationPoint(0F, 0F, 0F);
        Block2.setTextureSize(256, 128);
        Block2.mirror = true;
        //setRotation(Block2, 0.6464947F, 1.264073F, 0.5205006F);
        setRotation(Block2, 1.301144F, 0.973891F, 1.085993F);

        Block3 = new ModelRenderer(this, 32, 88);
        Block3.addBox(-3F, 4F, -2.9F, 2, 2, 2);
        Block3.setRotationPoint(0F, 0F, 0F);
        Block3.setTextureSize(256, 128);
        Block3.mirror = true;
        //setRotation(Block3, 1.129817F, 0.8922867F, 0.5205006F);
        setRotation(Block3, 1.333478F, 0.741522F, 0.740123F);

        Block4 = new ModelRenderer(this, 40, 88);
        Block4.addBox(-3F, 4F, -2.9F, 2, 2, 2);
        Block4.setRotationPoint(0F, 0F, 0F);
        Block4.setTextureSize(256, 128);
        Block4.mirror = true;
        //setRotation(Block4, 1.129817F, 0.4089647F, 0.5205006F);
        setRotation(Block4, 0.993040F, 0.352236F, 0.558418F);

        Torch = new ModelRenderer(this, 36, 80);
        Torch.addBox(-1F, 3F, -2.9F, 1, 2, 1);
        Torch.setRotationPoint(0F, 0F, 0F);
        Torch.setTextureSize(256, 128);
        Torch.mirror = true;
        //setRotation(Torch, 1.129817F, 0.1115358F, 0.5205006F);
        setRotation(Torch, 0.836393F, 0.096715F, 0.523194F);

        bipedBody.addChild(Chest);

        bipedHead.addChild(RightB);
        bipedHead.addChild(Top);
        bipedHead.addChild(LeftB);
        bipedHead.addChild(RightM);
        bipedHead.addChild(FrontLB);
        bipedHead.addChild(LeftM);
        bipedHead.addChild(Back);
        bipedHead.addChild(BackM);
        bipedHead.addChild(FrontM);
        bipedHead.addChild(Front1);
        bipedHead.addChild(FrontLM);
        bipedHead.addChild(FrontRM);
        bipedHead.addChild(FrontRB);
        bipedHead.addChild(Front2);

        bipedBody.addChild(Base);
        bipedBody.addChild(RimL);
        bipedBody.addChild(Lid1);
        bipedBody.addChild(Lock1);
        bipedBody.addChild(Lock2);
        bipedBody.addChild(RimB);
        bipedBody.addChild(RimR);
        bipedBody.addChild(RimF);
        bipedBody.addChild(Lid2);
        bipedBody.addChild(Handle);

        bipedBody.addChild(Block1);
        bipedBody.addChild(Block2);
        bipedBody.addChild(Block3);
        bipedBody.addChild(Block4);
        bipedBody.addChild(Torch);

        bipedHeadwear.showModel = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public float getActualRotation()
    {
        return 0.34907F;
    }
}
