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

        texWidth = 256;
        texHeight = 128;

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-3.99F, -6F, -3.9F, 8, 8, 8);
        head.setPos(0F, 2F, -4F);
        head.setTexSize(256, 128);
        head.mirror = true;
        setRotation(head, 0.3490659F, 0F, 0F);

        Chest = new ModelRenderer(this, 0, 32);
        Chest.addBox(-4.01F, 4F, -1.4F, 8, 3, 3);
        Chest.setPos(0F, 0F, 0F);
        Chest.setTexSize(256, 128);
        Chest.mirror = true;
        setRotation(Chest, -0.780751F, 0F, 0F);

        leftLeg = new ModelRenderer(this, 0, 16);
        leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        leftLeg.setPos(2F, 12F, 0F);
        leftLeg.setTexSize(256, 128);
        leftLeg.mirror = true;
        setRotation(leftLeg, 0F, 0F, 0F);

        rightLeg = new ModelRenderer(this, 0, 16);
        rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        rightLeg.setPos(-2F, 12F, 0F);
        rightLeg.setTexSize(256, 128);
        rightLeg.mirror = true;
        setRotation(rightLeg, 0F, 0F, 0F);

        leftArm = new ModelRenderer(this, 40, 16);
        leftArm.addBox(-1F, 0F, -4.9F, 4, 12, 4, 0F);
        leftArm.setPos(4F, 2F, -4F);
        leftArm.setTexSize(256, 128);
        leftArm.mirror = true;
        setRotation(leftArm, 0F, 0F, 0F);

        rightArm = new ModelRenderer(this, 40, 16);
        rightArm.addBox(-3F, 0F, -4.9F, 4, 12, 4, 0F);
        rightArm.setPos(-4F, 2F, -4F);
        rightArm.setTexSize(256, 128);
        rightArm.mirror = true;
        setRotation(rightArm, 0F, 0F, 0F);

        body = new ModelRenderer(this, 16, 16);
        body.addBox(-4F, 0F, -4F, 8, 12, 4, 0F);
        body.setPos(0F, 1F, -2F);

        RightB = new ModelRenderer(this, 7, 91);
        RightB.addBox(-5.1F, 3.3F, 3.2F, 1, 4, 1);
        RightB.setPos(0F, 0F, 0F);
        RightB.setTexSize(256, 128);
        RightB.mirror = true;
        setRotation(RightB, -0.111536F, 0F, 0.0743572F);

        Top = new ModelRenderer(this, 0, 39);
        Top.addBox(-4.5F, -8.5F, -4.5F, 9, 1, 9);
        Top.setPos(0F, 0F, 0F);
        Top.setTexSize(256, 128);
        Top.mirror = true;
        setRotation(Top, 0F, 0F, 0F);

        LeftB = new ModelRenderer(this, 0, 91);
        LeftB.addBox(3.8F, 3F, 3.5F, 1, 4, 1);
        LeftB.setPos(0F, 0F, 0F);
        LeftB.setTexSize(256, 128);
        LeftB.mirror = true;
        setRotation(LeftB, -0.185893F, 0F, -0.0734691F);

        RightM = new ModelRenderer(this, 7, 86);
        RightM.addBox(-4.6F, 1.3F, 2.2F, 1, 3, 2);
        RightM.setPos(0F, 0F, 0F);
        RightM.setTexSize(256, 128);
        RightM.mirror = true;
        setRotation(RightM, 0F, 0F, 0.2230717F);

        FrontLB = new ModelRenderer(this, 0, 64);
        FrontLB.addBox(3.5F, -3.5F, -6F, 1, 2, 1);
        FrontLB.setPos(0F, 0F, 0F);
        FrontLB.setTexSize(256, 128);
        FrontLB.mirror = true;
        setRotation(FrontLB, -0.3490659F, 0F, 0F);

        LeftM = new ModelRenderer(this, 0, 86);
        LeftM.addBox(3.5F, 1.2F, 2.2F, 1, 3, 2);
        LeftM.setPos(0F, 0F, 0F);
        LeftM.setTexSize(256, 128);
        LeftM.mirror = true;
        setRotation(LeftM, 0F, 0F, -0.1862235F);

        Back = new ModelRenderer(this, 0, 76);
        Back.addBox(-4.5F, -7.5F, 2.5F, 9, 8, 2);
        Back.setPos(0F, 0F, 0F);
        Back.setTexSize(256, 128);
        Back.mirror = true;
        setRotation(Back, 0F, 0F, 0F);

        BackM = new ModelRenderer(this, 0, 70);
        BackM.addBox(-4.5F, -7.5F, 0.5F, 9, 4, 2);
        BackM.setPos(0F, 0F, 0F);
        BackM.setTexSize(256, 128);
        BackM.mirror = true;
        setRotation(BackM, 0F, 0F, 0F);

        FrontM = new ModelRenderer(this, 0, 49);
        FrontM.addBox(-4.5F, -7.5F, -3.5F, 9, 2, 4);
        FrontM.setPos(0F, 0F, 0F);
        FrontM.setTexSize(256, 128);
        FrontM.mirror = true;
        setRotation(FrontM, 0F, 0F, 0F);

        Front1 = new ModelRenderer(this, 0, 55);
        Front1.addBox(-4.48F, -6.5F, -7F, 9, 2, 1);
        Front1.setPos(0F, 0F, 0F);
        Front1.setTexSize(256, 128);
        Front1.mirror = true;
        setRotation(Front1, -0.3490659F, 0F, 0F);

        FrontLM = new ModelRenderer(this, 0, 60);
        FrontLM.addBox(3.49F, -5.5F, -6.5F, 1, 2, 2);
        FrontLM.setPos(0F, 0F, 0F);
        FrontLM.setTexSize(256, 128);
        FrontLM.mirror = true;
        setRotation(FrontLM, -0.3490659F, 0F, 0F);

        FrontRM = new ModelRenderer(this, 7, 60);
        FrontRM.addBox(-4.49F, -5.5F, -6.5F, 1, 3, 2);
        FrontRM.setPos(0F, 0F, 0F);
        FrontRM.setTexSize(256, 128);
        FrontRM.mirror = true;
        setRotation(FrontRM, -0.3490659F, 0F, 0F);

        FrontRB = new ModelRenderer(this, 7, 65);
        FrontRB.addBox(-4.5F, -2.5F, -6F, 1, 3, 1);
        FrontRB.setPos(0F, 0F, 0F);
        FrontRB.setTexSize(256, 128);
        FrontRB.mirror = true;
        setRotation(FrontRB, -0.3490659F, 0F, 0F);

        Front2 = new ModelRenderer(this, 0, 58);
        Front2.addBox(-4.51F, -7.5F, -4.5F, 9, 1, 1);
        Front2.setPos(0F, 0F, 0F);
        Front2.setTexSize(256, 128);
        Front2.mirror = true;
        setRotation(Front2, 0F, 0F, 0F);

        Base = new ModelRenderer(this, 32, 50);
        Base.addBox(-4F, 4F, 0.1F, 8, 6, 6);
        Base.setPos(0F, 0F, 0F);
        Base.setTexSize(256, 128);
        Base.mirror = true;
        setRotation(Base, 0F, 0F, 0F);

        RimL = new ModelRenderer(this, 42, 65);
        RimL.addBox(3F, 3F, 1.1F, 1, 1, 4);
        RimL.setPos(0F, 0F, 0F);
        RimL.setTexSize(256, 128);
        RimL.mirror = true;
        setRotation(RimL, 0F, 0F, 0F);

        Lid1 = new ModelRenderer(this, 50, 70);
        Lid1.addBox(-4F, -4F, 2F, 8, 6, 1);
        Lid1.setPos(0F, 0F, 0F);
        Lid1.setTexSize(256, 128);
        Lid1.mirror = true;
        setRotation(Lid1, -0.785398F, 0F, 0F);

        Lock1 = new ModelRenderer(this, 50, 77);
        Lock1.addBox(-0.5F, -4.01F, 2.5F, 1, 0, 2);
        Lock1.setPos(0F, 0F, 0F);
        Lock1.setTexSize(256, 128);
        Lock1.mirror = true;
        setRotation(Lock1, -0.785398F, 0F, 0F);

        Lock2 = new ModelRenderer(this, 32, 77);
        Lock2.addBox(-0.5F, -6.1F, 2.5F, 1, 0, 2);
        Lock2.setPos(0F, 0F, 0F);
        Lock2.setTexSize(256, 128);
        Lock2.mirror = true;
        setRotation(Lock2, -1.570796F, 0F, 0F);

        RimB = new ModelRenderer(this, 32, 63);
        RimB.addBox(-4F, 3F, 5.1F, 8, 1, 1);
        RimB.setPos(0F, 0F, 0F);
        RimB.setTexSize(256, 128);
        RimB.mirror = true;
        setRotation(RimB, 0F, 0F, 0F);

        RimR = new ModelRenderer(this, 32, 65);
        RimR.addBox(-4F, 3F, 1.1F, 1, 1, 4);
        RimR.setPos(0F, 0F, 0F);
        RimR.setTexSize(256, 128);
        RimR.mirror = true;
        setRotation(RimR, 0F, 0F, 0F);

        RimF = new ModelRenderer(this, 50, 63);
        RimF.addBox(-4F, 3F, 0.1F, 8, 1, 1);
        RimF.setPos(0F, 0F, 0F);
        RimF.setTexSize(256, 128);
        RimF.mirror = true;
        setRotation(RimF, 0F, 0F, 0F);

        Lid2 = new ModelRenderer(this, 32, 70);
        Lid2.addBox(-4F, 0.12F, -2.99F, 8, 6, 1);
        Lid2.setPos(0F, 0F, 0F);
        Lid2.setTexSize(256, 128);
        Lid2.mirror = true;
        setRotation(Lid2, 1.570796F, 0F, 0F);

        Handle = new ModelRenderer(this, 32, 80);
        Handle.addBox(-1F, 5F, -0.1F, 1, 2, 1);
        Handle.setPos(0F, 0F, 0F);
        Handle.setTexSize(256, 128);
        Handle.mirror = true;
        //setRotation(Handle, 1.129817F, 0.6320364F, 0.5205006F);
        setRotation(Handle, 1.129978F, 0.538153F, 0.617719F);

        Block1 = new ModelRenderer(this, 32, 84);
        Block1.addBox(-2F, 3.5F, -0.9F, 2, 2, 2);
        Block1.setPos(0F, 0F, 0F);
        Block1.setTexSize(256, 128);
        Block1.mirror = true;
        //setRotation(Block1, 0.6464947F, 0.7063936F, 0.4461433F);
        setRotation(Block1, 0.650207F, 0.625571F, 0.561303F);

        Block2 = new ModelRenderer(this, 40, 84);
        Block2.addBox(-2F, 4F, -0.9F, 2, 2, 2);
        Block2.setPos(0F, 0F, 0F);
        Block2.setTexSize(256, 128);
        Block2.mirror = true;
        //setRotation(Block2, 0.6464947F, 1.264073F, 0.5205006F);
        setRotation(Block2, 1.301144F, 0.973891F, 1.085993F);

        Block3 = new ModelRenderer(this, 32, 88);
        Block3.addBox(-3F, 4F, -2.9F, 2, 2, 2);
        Block3.setPos(0F, 0F, 0F);
        Block3.setTexSize(256, 128);
        Block3.mirror = true;
        //setRotation(Block3, 1.129817F, 0.8922867F, 0.5205006F);
        setRotation(Block3, 1.333478F, 0.741522F, 0.740123F);

        Block4 = new ModelRenderer(this, 40, 88);
        Block4.addBox(-3F, 4F, -2.9F, 2, 2, 2);
        Block4.setPos(0F, 0F, 0F);
        Block4.setTexSize(256, 128);
        Block4.mirror = true;
        //setRotation(Block4, 1.129817F, 0.4089647F, 0.5205006F);
        setRotation(Block4, 0.993040F, 0.352236F, 0.558418F);

        Torch = new ModelRenderer(this, 36, 80);
        Torch.addBox(-1F, 3F, -2.9F, 1, 2, 1);
        Torch.setPos(0F, 0F, 0F);
        Torch.setTexSize(256, 128);
        Torch.mirror = true;
        //setRotation(Torch, 1.129817F, 0.1115358F, 0.5205006F);
        setRotation(Torch, 0.836393F, 0.096715F, 0.523194F);

        body.addChild(Chest);

        head.addChild(RightB);
        head.addChild(Top);
        head.addChild(LeftB);
        head.addChild(RightM);
        head.addChild(FrontLB);
        head.addChild(LeftM);
        head.addChild(Back);
        head.addChild(BackM);
        head.addChild(FrontM);
        head.addChild(Front1);
        head.addChild(FrontLM);
        head.addChild(FrontRM);
        head.addChild(FrontRB);
        head.addChild(Front2);

        body.addChild(Base);
        body.addChild(RimL);
        body.addChild(Lid1);
        body.addChild(Lock1);
        body.addChild(Lock2);
        body.addChild(RimB);
        body.addChild(RimR);
        body.addChild(RimF);
        body.addChild(Lid2);
        body.addChild(Handle);

        body.addChild(Block1);
        body.addChild(Block2);
        body.addChild(Block3);
        body.addChild(Block4);
        body.addChild(Torch);

        hat.visible = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }

    @Override
    public float getActualRotation()
    {
        return 0.34907F;
    }
}
