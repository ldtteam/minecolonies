package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityLumberjackMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityLumberjackMale()
    {
        ModelRenderer Log1;
        ModelRenderer Log2;
        ModelRenderer Log3;
        ModelRenderer Basket1;
        ModelRenderer Basket2;
        ModelRenderer Basket3;
        ModelRenderer Basket4;
        ModelRenderer Basket5;
        ModelRenderer Basket6;
        ModelRenderer Basket7;
        ModelRenderer Basket8;
        ModelRenderer Basket9;
        ModelRenderer Basket10;
        ModelRenderer Basket11;
        ModelRenderer BasketE1;
        ModelRenderer BasketE2;

        texWidth = 64;
        texHeight = 64;

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setPos(0F, 0F, 0F);
        head.setTexSize(64, 64);
        setRotation(head, 0F, 0F, 0F);

        hat = new ModelRenderer(this, 32, 0);
        hat.addBox(-4F, -8F, -4F, 8, 8, 8, 0.5F);
        hat.setPos(0F, 0F, 0F);
        hat.setTexSize(64, 64);
        setRotation(hat, 0F, 0F, 0F);

        body = new ModelRenderer(this, 16, 16);
        body.addBox(-4F, 0F, -2F, 8, 12, 4);
        body.setPos(0F, 0F, 0F);
        body.setTexSize(64, 64);
        setRotation(body, 0F, 0F, 0F);

        rightArm = new ModelRenderer(this, 40, 16);
        rightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        rightArm.setPos(-5F, 2F, 0F);
        rightArm.setTexSize(64, 64);
        setRotation(rightArm, 0F, 0F, 0F);

        leftArm = new ModelRenderer(this, 40, 16);
        leftArm.mirror = true;
        leftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        leftArm.setPos(5F, 2F, 0F);
        leftArm.setTexSize(64, 64);
        setRotation(leftArm, 0F, 0F, 0F);

        rightLeg = new ModelRenderer(this, 0, 16);
        rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        rightLeg.setPos(-2F, 12F, 0F);
        rightLeg.setTexSize(64, 64);
        setRotation(rightLeg, 0F, 0F, 0F);

        leftLeg = new ModelRenderer(this, 0, 16);
        leftLeg.mirror = true;
        leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        leftLeg.setPos(2F, 12F, 0F);
        leftLeg.setTexSize(64, 64);
        setRotation(leftLeg, 0F, 0F, 0F);

        Log1 = new ModelRenderer(this, 25, 33);
        Log1.addBox(-5F, 8F, 3F, 10, 3, 3);
        Log1.setPos(0F, 0F, 0F);
        Log1.setTexSize(64, 64);
        setRotation(Log1, 0F, 0F, 0F);

        Log2 = new ModelRenderer(this, 28, 39);
        Log2.addBox(-5F, 6F, -1F, 5, 3, 3);
        Log2.setPos(0F, 0F, 0F);
        Log2.setTexSize(64, 64);
        setRotation(Log2, 0.5585054F, 0F, 0F);

        /*
         * Log3 = new ModelRenderer(this, 28, 33); Log3.addBox(-2F, 0F, 3F, 10,
         * 3, 3); Log3.setRotationPoint(0F, 0F, 0F); Log3.setTextureSize(64,
         * 64); setRotation(Log3, 0F, 0F, 1.099557F);
         */

        Log3 = new ModelRenderer(this, 52, 33);
        Log3.addBox(-3F, -2F, 3F, 3, 10, 3);
        Log3.setPos(0F, 0F, 0F);
        Log3.setTexSize(64, 64);
        setRotation(Log3, 0F, 0F, -0.5061455F);

        Basket1 = new ModelRenderer(this, 1, 33);
        Basket1.addBox(-3F, 0F, 2F, 1, 12, 1);
        Basket1.setPos(0F, 0F, 0F);
        Basket1.setTexSize(64, 64);
        setRotation(Basket1, 0F, 0F, 0F);

        Basket2 = new ModelRenderer(this, 1, 33);
        Basket2.addBox(2F, 0F, 2F, 1, 12, 1);
        Basket2.setPos(0F, 0F, 0F);
        Basket2.setTexSize(64, 64);
        setRotation(Basket2, 0F, 0F, 0F);

        Basket3 = new ModelRenderer(this, 12, 33);
        Basket3.addBox(-3F, 4F, 6F, 1, 8, 1);
        Basket3.setPos(0F, 0F, 0F);
        Basket3.setTexSize(64, 64);
        setRotation(Basket3, 0F, 0F, 0F);

        Basket4 = new ModelRenderer(this, 12, 33);
        Basket4.addBox(2F, 4F, 6F, 1, 8, 1);
        Basket4.setPos(0F, 0F, 0F);
        Basket4.setTexSize(64, 64);
        setRotation(Basket4, 0F, 0F, 0F);

        Basket5 = new ModelRenderer(this, 1, 33);
        Basket5.addBox(2F, 11F, 3F, 1, 1, 3);
        Basket5.setPos(0F, 0F, 0F);
        Basket5.setTexSize(64, 64);
        setRotation(Basket5, 0F, 0F, 0F);

        Basket6 = new ModelRenderer(this, 1, 33);
        Basket6.addBox(-3F, 11F, 3F, 1, 1, 3);
        Basket6.setPos(0F, 0F, 0F);
        Basket6.setTexSize(64, 64);
        setRotation(Basket6, 0F, 0F, 0F);

        Basket7 = new ModelRenderer(this, 17, 33);
        Basket7.addBox(2F, 2F, 1F, 1, 6, 1);
        Basket7.setPos(0F, 0F, 0F);
        Basket7.setTexSize(64, 64);
        setRotation(Basket7, 0.8080874F, 0F, 0F);

        Basket8 = new ModelRenderer(this, 17, 33);
        Basket8.addBox(-3F, 2F, 1F, 1, 6, 1);
        Basket8.setPos(0F, 0F, 0F);
        Basket8.setTexSize(64, 64);
        setRotation(Basket8, 0.8080874F, 0F, 0F);

        Basket9 = new ModelRenderer(this, 12, 43);
        Basket9.addBox(-2F, 4F, 6F, 4, 1, 1);
        Basket9.setPos(0F, 0F, 0F);
        Basket9.setTexSize(64, 64);
        setRotation(Basket9, 0F, 0F, 0F);

        Basket10 = new ModelRenderer(this, 1, 33);
        Basket10.addBox(-2F, 11F, 6F, 4, 1, 1);
        Basket10.setPos(0F, 0F, 0F);
        Basket10.setTexSize(64, 64);
        setRotation(Basket10, 0F, 0F, 0F);

        Basket11 = new ModelRenderer(this, 1, 33);
        Basket11.addBox(-2F, 11F, 2F, 4, 1, 1);
        Basket11.setPos(0F, 0F, 0F);
        Basket11.setTexSize(64, 64);
        setRotation(Basket11, 0F, 0F, 0F);

        BasketE1 = new ModelRenderer(this, 1, 47);
        BasketE1.addBox(2F, 1F, 1F, 1, 12, 1);
        BasketE1.setPos(0F, 0F, 0F);
        BasketE1.setTexSize(64, 64);
        setRotation(BasketE1, 0.413643F, 0F, 0F);

        BasketE2 = new ModelRenderer(this, 1, 47);
        BasketE2.addBox(-3F, 1F, 1F, 1, 12, 1);
        BasketE2.setPos(0F, 0F, 0F);
        BasketE2.setTexSize(64, 64);
        setRotation(BasketE2, 0.413643F, 0F, 0F);

        body.addChild(Log1);
        body.addChild(Log1);
        body.addChild(Log2);
        body.addChild(Log3);
        body.addChild(Basket1);
        body.addChild(Basket2);
        body.addChild(Basket3);
        body.addChild(Basket4);
        body.addChild(Basket5);
        body.addChild(Basket6);
        body.addChild(Basket7);
        body.addChild(Basket8);
        body.addChild(Basket9);
        body.addChild(Basket10);
        body.addChild(Basket11);
        body.addChild(BasketE1);
        body.addChild(BasketE2);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
