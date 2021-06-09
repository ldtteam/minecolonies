package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityCitizenFemaleNoble extends CitizenModel<AbstractEntityCitizen>
{
    ModelRenderer breast;
    ModelRenderer hair;
    ModelRenderer dressPart1;
    ModelRenderer dressPart2;
    ModelRenderer dressPart3;
    ModelRenderer dressPart4;
    ModelRenderer dressPart5;
    ModelRenderer hat1;
    ModelRenderer hat2;
    ModelRenderer bag;
    ModelRenderer bagHand1;
    ModelRenderer bagHand2;

    public ModelEntityCitizenFemaleNoble()
    {
        texWidth = 128;
        texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setPos(0F, 0F, 1F);
        head.setTexSize(128, 64);
        setRotation(head, 0F, 0F, 0F);

        hat = new ModelRenderer(this, 32, 0);
        hat.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        hat.setPos(0F, 0F, 1F);
        hat.setTexSize(128, 64);
        setRotation(hat, 0F, 0F, 0F);

        body = new ModelRenderer(this, 12, 17);
        body.addBox(-4F, 0F, -4F, 8, 12, 3);
        body.setPos(0F, 0F, 3F);
        body.setTexSize(128, 64);
        setRotation(body, 0F, 0F, 0F);

        leftArm = new ModelRenderer(this, 34, 17);
        leftArm.mirror = true;
        leftArm.addBox(-1F, -2F, -1F, 3, 12, 3);
        leftArm.setPos(4F, 0F, 0F);
        leftArm.setTexSize(128, 64);
        setRotation(leftArm, 0F, 0F, -0.1396263F);

        rightArm = new ModelRenderer(this, 34, 17);
        rightArm.addBox(-2F, -2F, -1F, 3, 12, 3);
        rightArm.setPos(-5F, 0F, 0F);
        rightArm.setTexSize(128, 64);
        setRotation(rightArm, 0F, 0F, 0F);

        rightLeg = new ModelRenderer(this, 0, 17);
        rightLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        rightLeg.setPos(-1F, 12F, 1F);
        rightLeg.setTexSize(128, 64);
        setRotation(rightLeg, 0F, 0F, 0F);

        leftLeg = new ModelRenderer(this, 0, 17);
        leftLeg.mirror = true;
        leftLeg.addBox(-2F, 0F, -2F, 3, 12, 3);
        leftLeg.setPos(2F, 12F, 1F);
        leftLeg.setTexSize(128, 64);
        setRotation(leftLeg, 0F, 0F, 0F);

        breast = new ModelRenderer(this, 0, 33);
        breast.addBox(-3F, 2F, -4.5F, 8, 4, 3);
        breast.setPos(-1F, 3F, 1F);
        breast.setTexSize(128, 64);
        setRotation(breast, -0.5235988F, 0F, 0F);

        hair = new ModelRenderer(this, 46, 17);
        hair.addBox(-4F, 0F, 3F, 8, 7, 1, 0.5F);
        hair.setPos(0F, 0F, 1F);
        hair.setTexSize(128, 64);
        setRotation(hair, 0F, 0F, 0F);

        dressPart1 = new ModelRenderer(this, 65, 48);
        dressPart1.addBox(-8F, 9F, -9F, 16, 3, 13);
        dressPart1.setPos(0F, 11F, 0F);
        dressPart1.setTexSize(128, 64);
        setRotation(dressPart1, 0F, 0F, 0F);

        dressPart2 = new ModelRenderer(this, 65, 34);
        dressPart2.addBox(-7F, 6F, -8F, 14, 3, 11);
        dressPart2.setPos(0F, 11F, 0F);
        dressPart2.setTexSize(128, 64);
        setRotation(dressPart2, 0F, 0F, 0F);

        dressPart3 = new ModelRenderer(this, 65, 23);
        dressPart3.addBox(-6F, 4F, -7F, 12, 2, 9);
        dressPart3.setPos(0F, 11F, 0F);
        dressPart3.setTexSize(128, 64);
        setRotation(dressPart3, 0F, 0F, 0F);

        dressPart4 = new ModelRenderer(this, 65, 14);
        dressPart4.addBox(-5F, 2F, -6F, 10, 2, 7);
        dressPart4.setPos(0F, 11F, 0F);
        dressPart4.setTexSize(128, 64);
        setRotation(dressPart4, 0F, 0F, 0F);

        dressPart5 = new ModelRenderer(this, 65, 7);
        dressPart5.addBox(-4F, 0F, -5F, 8, 2, 5);
        dressPart5.setPos(0F, 11F, 0F);
        dressPart5.setTexSize(128, 64);
        setRotation(dressPart5, 0F, 0F, 0F);

        hat1 = new ModelRenderer(this, 0, 48);
        hat1.addBox(-5F, -8F, -6F, 10, 2, 10, 0.1F);
        hat1.setPos(0F, 0F, 1F);
        hat1.setTexSize(128, 64);
        setRotation(hat1, 0F, 0F, 0F);

        hat2 = new ModelRenderer(this, 0, 40);
        hat2.addBox(-3F, -10F, -4F, 6, 2, 6, 0.3F);
        hat2.setPos(0F, 0F, 1F);
        hat2.setTexSize(128, 64);
        setRotation(hat2, 0F, 0F, 0F);

        bag = new ModelRenderer(this, 24, 32);
        bag.addBox(0F, 6F, -6F, 1, 4, 7);
        bag.setPos(4F, 0F, 0F);
        bag.setTexSize(128, 64);
        setRotation(bag, 0F, 0F, 0F);

        bagHand1 = new ModelRenderer(this, 40, 32);
        bagHand1.addBox(0F, 1F, -3F, 1, 7, 0);
        bagHand1.setPos(4F, 0F, 0F);
        bagHand1.setTexSize(128, 64);
        setRotation(bagHand1, -0.4014257F, 0F, 0F);

        bagHand2 = new ModelRenderer(this, 40, 32);
        bagHand2.addBox(0F, 1F, -2F, 1, 7, 0);
        bagHand2.setPos(4F, 0F, 0F);
        bagHand2.setTexSize(128, 64);
        setRotation(bagHand2, 0.4014257F, 0F, 0F);

        hat.addChild(hat1);
        hat.addChild(hat2);

        body.addChild(breast);

        body.addChild(dressPart1);
        body.addChild(dressPart2);
        body.addChild(dressPart3);
        body.addChild(dressPart4);
        body.addChild(dressPart5);

        body.addChild(bagHand1);
        body.addChild(bagHand2);

        body.addChild(bag);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}