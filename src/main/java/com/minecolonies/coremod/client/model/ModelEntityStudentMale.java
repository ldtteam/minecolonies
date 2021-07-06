package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

/**
 * Model for the male students (monks).
 */
public class ModelEntityStudentMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityStudentMale()
    {
        ModelRenderer front;
        ModelRenderer back;
        ModelRenderer left;
        ModelRenderer right;
        ModelRenderer armCHorizontal;
        ModelRenderer rightArmC;
        ModelRenderer leftArmC;
        ModelRenderer belly;
        ModelRenderer bellyU;
        ModelRenderer helmet;

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
        armCHorizontal.addBox(0F, -1F, 1F, 16, 4, 4);
        armCHorizontal.setPos(-8F, 3.8F, -3.5F);
        armCHorizontal.setTexSize(128, 64);
        armCHorizontal.mirror = true;
        setRotation(armCHorizontal, -0.4886922F, 0F, 0F);

        rightArmC = new ModelRenderer(this, 0, 44);
        rightArmC.addBox(0F, 0F, 1F, 4, 8, 4);
        rightArmC.setPos(-8F, -0.5F, -1F);
        rightArmC.setTexSize(128, 64);
        rightArmC.mirror = true;
        setRotation(rightArmC, -0.5061455F, 0F, 0F);

        leftArmC = new ModelRenderer(this, 0, 44);
        leftArmC.addBox(0F, 0F, 1F, 4, 8, 4);
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

        belly = new ModelRenderer(this, 29, 34);
        belly.addBox(0F, 0F, 1F, 6, 4, 1);
        belly.setPos(-3F, 6F, -4F);
        belly.setTexSize(128, 64);
        belly.mirror = true;
        setRotation(belly, 0F, 0F, 0F);

        bellyU = new ModelRenderer(this, 19, 29);
        bellyU.addBox(0F, 0F, 1F, 4, 1, 1);
        bellyU.setPos(-2F, 5F, -4F);
        bellyU.setTexSize(128, 64);
        bellyU.mirror = true;
        setRotation(bellyU, 0F, 0F, 0F);

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

        this.body.addChild(belly);
        this.body.addChild(bellyU);
        this.body.addChild(front);
        this.body.addChild(back);
        this.body.addChild(right);
        this.body.addChild(left);
        this.body.addChild(armCHorizontal);

        this.head.addChild(helmet);


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
