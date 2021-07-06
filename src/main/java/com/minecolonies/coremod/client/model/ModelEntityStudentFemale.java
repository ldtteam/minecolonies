package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the male students (monks).
 */
public class ModelEntityStudentFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityStudentFemale()
    {
        ModelRenderer front;
        ModelRenderer back;
        ModelRenderer left;
        ModelRenderer right;
        ModelRenderer armCHorizontal;
        ModelRenderer rightArmC;
        ModelRenderer leftArmC;
        ModelRenderer helmet;
        ModelRenderer chest;
        ModelRenderer ponytailBase;
        ModelRenderer ponyTailTip;
        ModelRenderer book;

        texWidth = 128;
        texHeight = 64;

        front = new ModelRenderer(this, 16, 48);
        front.addBox(0F, 0F, 1F, 8, 8, 0);
        front.setPos(-4F, 12F, -3F);
        front.setTexSize(128, 64);
        front.mirror = true;
        setRotation(front, 0F, 0F, 0F);

        back = new ModelRenderer(this, 16, 40);
        back.addBox(0F, 0F, 1F, 8, 8, 0);
        back.setPos(-4F, 12F, 3F);
        back.setTexSize(128, 64);
        back.mirror = true;
        setRotation(back, 0F, 0F, 0F);

        left = new ModelRenderer(this, 16, 34);
        left.addBox(0F, 0F, 1F, 0, 8, 6);
        left.setPos(4F, 12F, -3F);
        left.setTexSize(128, 64);
        left.mirror = true;
        setRotation(left, 0F, 0F, 0F);

        right = new ModelRenderer(this, 16, 34);
        right.addBox(0F, 0F, 1F, 0, 8, 6);
        right.setPos(-4F, 12F, -3F);
        right.setTexSize(128, 64);
        right.mirror = true;
        setRotation(right, 0F, 0F, 0F);


        armCHorizontal = new ModelRenderer(this, 0, 56);
        armCHorizontal.addBox(0F, 0F, 0F, 16, 4, 4);
        armCHorizontal.setPos(-8F, 3.8F, -3.5F);
        armCHorizontal.setTexSize(128, 64);
        armCHorizontal.mirror = true;
        setRotation(armCHorizontal, -0.4886922F, 0F, 0F);

        rightArmC = new ModelRenderer(this, 0, 44);
        rightArmC.addBox(0F, 0F, 0F, 4, 8, 4);
        rightArmC.setPos(-8F, -0.5F, -1F);
        rightArmC.setTexSize(128, 64);
        rightArmC.mirror = true;
        setRotation(rightArmC, -0.5061455F, 0F, 0F);

        leftArmC = new ModelRenderer(this, 0, 44);
        leftArmC.addBox(0F, 0F, 0F, 4, 8, 4);
        leftArmC.setPos(4F, -0.5F, -1F);
        leftArmC.setTexSize(128, 64);
        leftArmC.mirror = true;
        setRotation(leftArmC, -0.5061455F, 0F, 0F);

        rightArm = new ModelRenderer(this, 44, 16);
        rightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        rightArm.setPos(-5F, 2F, 0F);
        rightArm.setTexSize(128, 64);
        rightArm.mirror = true;
        setRotation(rightArm, 0F, 0F, 0F);

        leftArm = new ModelRenderer(this, 44, 16);
        leftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        leftArm.setPos(5F, 2F, 0F);
        leftArm.setTexSize(128, 64);
        leftArm.mirror = true;
        setRotation(leftArm, 0F, 0F, 0F);

        rightLeg = new ModelRenderer(this, 0, 16);
        rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        rightLeg.setPos(-2F, 12F, 0F);
        rightLeg.setTexSize(128, 64);
        rightLeg.mirror = true;
        setRotation(rightLeg, 0F, 0F, 0F);

        leftLeg = new ModelRenderer(this, 0, 16);
        leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        leftLeg.setPos(2F, 12F, 0F);
        leftLeg.setTexSize(128, 64);
        leftLeg.mirror = true;
        setRotation(leftLeg, 0F, 0F, 0F);

        body = new ModelRenderer(this, 16, 16);
        body.addBox(-4F, 0F, -2F, 8, 12, 6);
        body.setPos(0F, 0F, -1F);
        body.setTexSize(128, 64);
        body.mirror = true;
        setRotation(body, 0F, 0F, 0F);

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setPos(0F, 0F, 0F);
        head.setTexSize(128, 64);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0F);

        helmet = new ModelRenderer(this, 40, 46);
        helmet.addBox(0F, 0F, 0F, 9, 9, 9);
        helmet.setPos(-4.5F, -8.5F, -4.5F);
        helmet.setTexSize(128, 64);
        helmet.mirror = true;
        setRotation(helmet, 0F, 0F, 0F);

        chest = new ModelRenderer(this, 44, 32);
        chest.addBox(0F, 0F, 0F, 7, 3, 4);
        chest.setPos(-3.5F, 1.7F, -2.7F);
        chest.setTexSize(128, 64);
        chest.mirror = false;
        setRotation(chest, -0.4537856F, 0F, 0F);

        ponytailBase = new ModelRenderer(this, 32, 49);
        ponytailBase.addBox(0F, 0F, 0F, 2, 5, 2);
        ponytailBase.setPos(-1F, -4F, 2F);
        ponytailBase.setTexSize(128, 64);
        ponytailBase.mirror = true;
        setRotation(ponytailBase, 0.5576792F, 0F, 0F);

        ponyTailTip = new ModelRenderer(this, 34, 49);
        ponyTailTip.addBox(0F, 0F, 0F, 1, 5, 1);
        ponyTailTip.setPos(-0.5F, -1F, 4.8F);
        ponyTailTip.setTexSize(128, 64);
        ponyTailTip.mirror = true;
        setRotation(ponyTailTip, 0.2230717F, 0F, 0F);

        book = new ModelRenderer(this, 32, 0);
        book.addBox(4F, -2.5F, 0F, 2, 4, 6);
        book.setPos(-6.5F, 10F, -3F);
        book.setTexSize(128, 64);
        book.mirror = true;
        setRotation(book, 0F, 0F, 0F);

        this.body.addChild(front);
        this.body.addChild(back);
        this.body.addChild(right);
        this.body.addChild(left);
        this.body.addChild(armCHorizontal);
        this.body.addChild(chest);

        this.head.addChild(helmet);
        this.head.addChild(ponytailBase);
        this.head.addChild(ponyTailTip);


        this.rightArm.addChild(book);

        this.body.addChild(leftArmC);
        this.body.addChild(rightArmC);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
