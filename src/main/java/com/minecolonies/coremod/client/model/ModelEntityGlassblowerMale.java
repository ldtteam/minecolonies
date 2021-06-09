package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityGlassblowerMale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityGlassblowerMale()
    {
        ModelRenderer gauntlet1;
        ModelRenderer gauntlet2;
        ModelRenderer hair1;
        ModelRenderer hair2;
        ModelRenderer hair17;
        ModelRenderer hair16;
        ModelRenderer hair15;
        ModelRenderer hair14;
        ModelRenderer hair13;
        ModelRenderer hair12;
        ModelRenderer hair11;
        ModelRenderer hair10;
        ModelRenderer hair9;
        ModelRenderer hair8;
        ModelRenderer hair7;
        ModelRenderer hair6;
        ModelRenderer hair5;
        ModelRenderer hair4;
        ModelRenderer hair18;
        ModelRenderer hair19;
        ModelRenderer hair20;
        ModelRenderer hair3;
        ModelRenderer toolHandle1;
        ModelRenderer toolHandle2;
        ModelRenderer pocket;
        ModelRenderer hair_37;
        ModelRenderer hair_38;
        ModelRenderer hair_36;
        ModelRenderer hair_35;
        ModelRenderer hair21;
        ModelRenderer hair_23;
        ModelRenderer hair_24;
        ModelRenderer hair_22;
        ModelRenderer hair_25;
        ModelRenderer hair_26;
        ModelRenderer hair_27;
        ModelRenderer hair_28;
        ModelRenderer hair_29;
        ModelRenderer hair_30;
        ModelRenderer hair_31;
        ModelRenderer hair_32;
        ModelRenderer hair_33;
        ModelRenderer hair_34;
        ModelRenderer hair39;
        ModelRenderer hair40;
        ModelRenderer hair41;
        ModelRenderer hair42;
        ModelRenderer hair43;
        ModelRenderer hair44;
        ModelRenderer hair45;
        ModelRenderer hair48;
        ModelRenderer hair50;
        ModelRenderer hair53;
        ModelRenderer hair56;
        ModelRenderer hair51;
        ModelRenderer hair55;
        ModelRenderer hair58;
        ModelRenderer hair54;
        ModelRenderer hair57;
        ModelRenderer hair59;
        ModelRenderer hair49;
        ModelRenderer hair61;
        ModelRenderer hair52;
        ModelRenderer hair60;
        ModelRenderer hair46;
        ModelRenderer hair47;

        texWidth = 128;
        texHeight = 64;

        gauntlet1 = new ModelRenderer(this, 98, 0);
        gauntlet1.addBox(-5F, -1.5F, 0F, 5, 1, 5);
        gauntlet1.setPos(3.5F, 7F, -2.5F);
        gauntlet1.setTexSize(128, 64);
        setRotation(gauntlet1, 0F, 0F, 0F);

        gauntlet2 = new ModelRenderer(this, 98, 0);
        gauntlet2.addBox(5F, -1.5F, 0F, 5, 1, 5);
        gauntlet2.setPos(-8.5F, 7F, -2.5F);
        gauntlet2.setTexSize(128, 64);
        setRotation(gauntlet2, 0F, 0F, 0F);

        hair1 = new ModelRenderer(this, 58, 1);
        hair1.addBox(0F, 0F, 0F, 8, 8, 1);
        hair1.setPos(-4F, -8F, 4F);
        hair1.setTexSize(128, 64);
        setRotation(hair1, 0F, 0F, 0F);

        hair2 = new ModelRenderer(this, 58, 0);
        hair2.addBox(0F, 0F, 0F, 3, 3, 1);
        hair2.setPos(-5F, -9F, -5F);
        hair2.setTexSize(128, 64);
        setRotation(hair2, 0F, 0F, 0F);

        hair17 = new ModelRenderer(this, 58, 5);
        hair17.addBox(0F, 0F, 0F, 1, 1, 3);
        hair17.setPos(4F, -1.4F, -4.5F);
        hair17.setTexSize(128, 64);
        setRotation(hair17, 0F, 0F, 0F);

        hair16 = new ModelRenderer(this, 58, 0);
        hair16.addBox(0F, 0F, 0F, 1, 2, 4);
        hair16.setPos(4F, -3F, -1F);
        hair16.setTexSize(128, 64);
        setRotation(hair16, 0F, 0F, 0F);

        hair15 = new ModelRenderer(this, 58, 6);
        hair15.addBox(0F, 0F, 0F, 1, 2, 4);
        hair15.setPos(-5F, -3F, -1F);
        hair15.setTexSize(128, 64);
        setRotation(hair15, 0F, 0F, 0F);

        hair14 = new ModelRenderer(this, 58, 6);
        hair14.addBox(0F, 0F, 0F, 1, 1, 1);
        hair14.setPos(-5F, -5F, -3F);
        hair14.setTexSize(128, 64);
        setRotation(hair14, 0F, 0F, 0F);

        hair13 = new ModelRenderer(this, 58, 6);
        hair13.addBox(0F, 0F, 0F, 1, 5, 2);
        hair13.setPos(-5F, -5F, 3F);
        hair13.setTexSize(128, 64);
        setRotation(hair13, 0F, 0F, 0F);

        hair12 = new ModelRenderer(this, 58, 6);
        hair12.addBox(0F, 0F, 0F, 1, 2, 5);
        hair12.setPos(-5F, -5F, -2F);
        hair12.setTexSize(128, 64);
        setRotation(hair12, 0F, 0F, 0F);

        hair11 = new ModelRenderer(this, 58, 6);
        hair11.addBox(0F, 0F, 0F, 1, 3, 9);
        hair11.setPos(-5F, -8F, -4F);
        hair11.setTexSize(128, 64);
        hair11.mirror = true;
        setRotation(hair11, 0F, 0F, 0F);

        hair10 = new ModelRenderer(this, 58, 0);
        hair10.addBox(0F, 0F, 0F, 3, 3, 1);
        hair10.setPos(2F, -9F, -5F);
        hair10.setTexSize(128, 64);
        setRotation(hair10, 0F, 0F, 0F);

        hair9 = new ModelRenderer(this, 58, 0);
        hair9.addBox(0F, 0F, 0F, 4, 2, 1);
        hair9.setPos(-2F, -9F, -5F);
        hair9.setTexSize(128, 64);
        setRotation(hair9, 0F, 0F, 0F);

        hair8 = new ModelRenderer(this, 58, 0);
        hair8.addBox(0F, 0F, 0F, 10, 1, 9);
        hair8.setPos(-5F, -9F, -4F);
        hair8.setTexSize(128, 64);
        setRotation(hair8, 0F, 0F, 0F);

        hair7 = new ModelRenderer(this, 58, 0);
        hair7.addBox(0F, 0F, 0F, 1, 2, 5);
        hair7.setPos(4F, -5F, -2F);
        hair7.setTexSize(128, 64);
        setRotation(hair7, 0F, 0F, 0F);

        hair6 = new ModelRenderer(this, 58, 0);
        hair6.addBox(0F, 0F, 0F, 4, 1, 4);
        hair6.setPos(1F, -1.9F, -4.5F);
        hair6.setTexSize(128, 64);
        setRotation(hair6, 0F, 0F, 0F);

        hair5 = new ModelRenderer(this, 58, 0);
        hair5.addBox(0F, 0F, 0F, 1, 3, 9);
        hair5.setPos(4F, -8F, -4F);
        hair5.setTexSize(128, 64);
        setRotation(hair5, 0F, 0F, 0F);

        hair4 = new ModelRenderer(this, 58, 0);
        hair4.addBox(0F, 0F, 0F, 1, 5, 2);
        hair4.setPos(4F, -5F, 3F);
        hair4.setTexSize(128, 64);
        setRotation(hair4, 0F, 0F, 0F);

        hair18 = new ModelRenderer(this, 58, 0);
        hair18.addBox(0F, 0F, 0F, 4, 1, 4);
        hair18.setPos(-5F, -1.9F, -4.5F);
        hair18.setTexSize(128, 64);
        setRotation(hair18, 0F, 0F, 0F);

        hair19 = new ModelRenderer(this, 58, 5);
        hair19.addBox(0F, 0F, 0F, 1, 1, 3);
        hair19.setPos(-5F, -1.4F, -4.5F);
        hair19.setTexSize(128, 64);
        setRotation(hair19, 0F, 0F, 0F);

        hair20 = new ModelRenderer(this, 61, 4);
        hair20.addBox(0F, 0F, 0F, 9, 1, 3);
        hair20.setPos(-4.5F, -0.9F, -4.5F);
        hair20.setTexSize(128, 64);
        setRotation(hair20, 0F, 0F, 0F);

        hair3 = new ModelRenderer(this, 58, 0);
        hair3.addBox(0F, 0F, 0F, 1, 1, 1);
        hair3.setPos(4F, -5F, -3F);
        hair3.setTexSize(128, 64);
        setRotation(hair3, 0F, 0F, 0F);

        toolHandle1 = new ModelRenderer(this, 10, 32);
        toolHandle1.addBox(0F, 0F, 0F, 1, 2, 1);
        toolHandle1.setPos(-1F, 6F, -3F);
        toolHandle1.setTexSize(128, 64);
        setRotation(toolHandle1, 0F, 0F, 0F);

        toolHandle2 = new ModelRenderer(this, 10, 32);
        toolHandle2.addBox(0F, 0F, 0F, 1, 2, 1);
        toolHandle2.setPos(0F, 6F, -3F);
        toolHandle2.setTexSize(128, 64);
        setRotation(toolHandle2, 0F, 0F, 0F);

        pocket = new ModelRenderer(this, 19, 28);
        pocket.addBox(0F, 0F, 0F, 4, 3, 1);
        pocket.setPos(-2F, 8F, -3F);
        pocket.setTexSize(128, 64);
        setRotation(pocket, 0F, 0F, 0F);

        rightArm = new ModelRenderer(this, 40, 16);
        rightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        rightArm.setPos(-5F, 2F, 0F);
        rightArm.setTexSize(128, 64);
        setRotation(rightArm, 0F, 0F, 0F);

        leftArm = new ModelRenderer(this, 40, 16);
        leftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        leftArm.setPos(5F, 2F, 0F);
        leftArm.setTexSize(128, 64);
        setRotation(leftArm, 0F, 0F, 0F);

        rightLeg = new ModelRenderer(this, 0, 16);
        rightLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        rightLeg.setPos(-2F, 12F, 0F);
        rightLeg.setTexSize(128, 64);
        rightLeg.mirror = true;
        setRotation(rightLeg, 0F, 0F, 0F);

        leftLeg.mirror = true;
        leftLeg = new ModelRenderer(this, 0, 16);
        leftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
        leftLeg.setPos(2F, 12F, 0F);
        leftLeg.setTexSize(128, 64);
        leftLeg.mirror = true;
        setRotation(leftLeg, 0F, 0F, 0F);

        body = new ModelRenderer(this, 16, 16);
        body.addBox(-4F, 0F, -2F, 8, 12, 4);
        body.setPos(0F, 0F, 0F);
        body.setTexSize(128, 64);
        setRotation(body, 0F, 0F, 0F);

        head = new ModelRenderer(this, 0, 0);
        head.addBox(-4F, -8F, -4F, 8, 8, 8);
        head.setPos(0F, 0F, 0F);
        head.setTexSize(128, 64);
        setRotation(head, 0F, 0F, 0F);

        hair_37 = new ModelRenderer(this, 58, 47);
        hair_37.addBox(0F, 0F, 0F, 1, 2, 4);
        hair_37.setPos(-5F, -3F, -1F);
        hair_37.setTexSize(128, 64);
        hair_37.mirror = true;
        setRotation(hair_37, 0F, 0F, 0F);

        hair_38 = new ModelRenderer(this, 58, 47);
        hair_38.addBox(0F, 0F, 0F, 1, 4, 1);
        hair_38.setPos(-5F, -5F, 3F);
        hair_38.setTexSize(128, 64);
        hair_38.mirror = true;
        setRotation(hair_38, 0F, 0F, 0F);

        hair_36 = new ModelRenderer(this, 58, 47);
        hair_36.addBox(0F, 0F, 0F, 1, 2, 5);
        hair_36.setPos(-5F, -5F, -2F);
        hair_36.setTexSize(128, 64);
        hair_36.mirror = true;
        setRotation(hair_36, 0F, 0F, 0F);

        hair_35 = new ModelRenderer(this, 58, 47);
        hair_35.addBox(0F, 0F, 0F, 1, 1, 5);
        hair_35.setPos(-5F, -6F, -1F);
        hair_35.setTexSize(128, 64);
        hair_35.mirror = true;
        setRotation(hair_35, 0F, 0F, 0F);

        hair21 = new ModelRenderer(this, 58, 42);
        hair21.addBox(0F, 0F, 0F, 10, 3, 1);
        hair21.setPos(-5F, -5F, 4F);
        hair21.setTexSize(128, 64);
        hair21.mirror = true;
        setRotation(hair21, 0F, 0F, 0F);

        hair_23 = new ModelRenderer(this, 58, 47);
        hair_23.addBox(0F, 0F, 0F, 1, 1, 5);
        hair_23.setPos(4F, -6F, -1F);
        hair_23.setTexSize(128, 64);
        hair_23.mirror = true;
        setRotation(hair_23, 0F, 0F, 0F);

        hair_24 = new ModelRenderer(this, 58, 47);
        hair_24.addBox(0F, 0F, 0F, 1, 2, 5);
        hair_24.setPos(4F, -5F, -2F);
        hair_24.setTexSize(128, 64);
        hair_24.mirror = true;
        setRotation(hair_24, 0F, 0F, 0F);

        hair_22 = new ModelRenderer(this, 58, 41);
        hair_22.addBox(0F, 0F, 0F, 4, 1, 4);
        hair_22.setPos(1F, -1.9F, -4.5F);
        hair_22.setTexSize(128, 64);
        hair_22.mirror = true;
        setRotation(hair_22, 0F, 0F, 0F);

        hair_25 = new ModelRenderer(this, 58, 47);
        hair_25.addBox(0F, 0F, 0F, 1, 4, 1);
        hair_25.setPos(4F, -5F, 3F);
        hair_25.setTexSize(128, 64);
        hair_25.mirror = true;
        setRotation(hair_25, 0F, 0F, 0F);

        hair_26 = new ModelRenderer(this, 58, 47);
        hair_26.addBox(0F, 0F, 0F, 1, 2, 4);
        hair_26.setPos(4F, -3F, -1F);
        hair_26.setTexSize(128, 64);
        hair_26.mirror = true;
        setRotation(hair_26, 0F, 0F, 0F);

        hair_27 = new ModelRenderer(this, 58, 46);
        hair_27.addBox(0F, 0F, 0F, 1, 1, 3);
        hair_27.setPos(4F, -1.5F, -4.5F);
        hair_27.setTexSize(128, 64);
        hair_27.mirror = true;
        setRotation(hair_27, 0F, 0F, 0F);

        hair_28 = new ModelRenderer(this, 58, 41);
        hair_28.addBox(0F, 0F, 0F, 4, 1, 4);
        hair_28.setPos(-5F, -1.9F, -4.5F);
        hair_28.setTexSize(128, 64);
        hair_28.mirror = true;
        setRotation(hair_28, 0F, 0F, 0F);

        hair_29 = new ModelRenderer(this, 58, 46);
        hair_29.addBox(0F, 0F, 0F, 1, 1, 3);
        hair_29.setPos(-5F, -1.5F, -4.5F);
        hair_29.setTexSize(128, 64);
        hair_29.mirror = true;
        setRotation(hair_29, 0F, 0F, 0F);

        hair_30 = new ModelRenderer(this, 61, 45);
        hair_30.addBox(0F, 0F, 0F, 5, 1, 2);
        hair_30.setPos(-2.5F, 3F, -4.5F);
        hair_30.setTexSize(128, 64);
        hair_30.mirror = true;
        setRotation(hair_30, 0F, 0F, 0F);

        hair_31 = new ModelRenderer(this, 61, 45);
        hair_31.addBox(0F, 0F, 0F, 9, 1, 3);
        hair_31.setPos(-4.5F, -0.9F, -4.5F);
        hair_31.setTexSize(128, 64);
        hair_31.mirror = true;
        setRotation(hair_31, 0F, 0F, 0F);

        hair_32 = new ModelRenderer(this, 61, 45);
        hair_32.addBox(0F, 0F, 0F, 8, 1, 2);
        hair_32.setPos(-4F, 0F, -4.5F);
        hair_32.setTexSize(128, 64);
        hair_32.mirror = true;
        setRotation(hair_32, 0F, 0F, 0F);

        hair_33 = new ModelRenderer(this, 61, 45);
        hair_33.addBox(0F, 0F, 0F, 7, 1, 2);
        hair_33.setPos(-3.5F, 1F, -4.5F);
        hair_33.setTexSize(128, 64);
        hair_33.mirror = true;
        setRotation(hair_33, 0F, 0F, 0F);

        hair_34 = new ModelRenderer(this, 61, 45);
        hair_34.addBox(0F, 0F, 0F, 6, 1, 2);
        hair_34.setPos(-3F, 2F, -4.5F);
        hair_34.setTexSize(128, 64);
        hair_34.mirror = true;
        setRotation(hair_34, 0F, 0F, 0F);

        hair39 = new ModelRenderer(this, 58, 22);
        hair39.addBox(0F, 0F, 0F, 8, 8, 1);
        hair39.setPos(-4F, -8F, 4F);
        hair39.setTexSize(128, 64);
        hair39.mirror = true;
        setRotation(hair39, 0F, 0F, 0F);

        hair40 = new ModelRenderer(this, 58, 21);
        hair40.addBox(0F, 0F, 0F, 3, 3, 1);
        hair40.setPos(-5F, -9F, -5F);
        hair40.setTexSize(128, 64);
        hair40.mirror = true;
        setRotation(hair40, 0F, 0F, 0F);

        hair41 = new ModelRenderer(this, 58, 21);
        hair41.addBox(0F, 0F, 0F, 1, 1, 1);
        hair41.setPos(4F, -5F, -3F);
        hair41.setTexSize(128, 64);
        hair41.mirror = true;
        setRotation(hair41, 0F, 0F, 0F);

        hair42 = new ModelRenderer(this, 58, 21);
        hair42.addBox(0F, 0F, 0F, 1, 5, 2);
        hair42.setPos(4F, -5F, 3F);
        hair42.setTexSize(128, 64);
        hair42.mirror = true;
        setRotation(hair42, 0F, 0F, 0F);

        hair43 = new ModelRenderer(this, 58, 21);
        hair43.addBox(0F, 0F, 0F, 1, 3, 9);
        hair43.setPos(4F, -8F, -4F);
        hair43.setTexSize(128, 64);
        hair43.mirror = true;
        setRotation(hair43, 0F, 0F, 0F);

        hair44 = new ModelRenderer(this, 58, 21);
        hair44.addBox(0F, 0F, 0F, 4, 1, 4);
        hair44.setPos(1F, -1.9F, -4.5F);
        hair44.setTexSize(128, 64);
        hair44.mirror = true;
        setRotation(hair44, 0F, 0F, 0F);

        hair45 = new ModelRenderer(this, 58, 21);
        hair45.addBox(0F, 0F, 0F, 1, 2, 5);
        hair45.setPos(4F, -5F, -2F);
        hair45.setTexSize(128, 64);
        hair45.mirror = true;
        setRotation(hair45, 0F, 0F, 0F);

        hair48 = new ModelRenderer(this, 58, 21);
        hair48.addBox(0F, 0F, 0F, 3, 3, 1);
        hair48.setPos(2F, -9F, -5F);
        hair48.setTexSize(128, 64);
        hair48.mirror = true;
        setRotation(hair48, 0F, 0F, 0F);

        hair50 = new ModelRenderer(this, 58, 27);
        hair50.addBox(0F, 0F, 0F, 1, 2, 5);
        hair50.setPos(-5F, -5F, -2F);
        hair50.setTexSize(128, 64);
        hair50.mirror = true;
        setRotation(hair50, 0F, 0F, 0F);

        hair53 = new ModelRenderer(this, 58, 27);
        hair53.addBox(0F, 0F, 0F, 1, 2, 4);
        hair53.setPos(-5F, -3F, -1F);
        hair53.setTexSize(128, 64);
        hair53.mirror = true;
        setRotation(hair53, 0F, 0F, 0F);

        hair56 = new ModelRenderer(this, 58, 21);
        hair56.addBox(0F, 0F, 0F, 4, 1, 4);
        hair56.setPos(-5F, -1.9F, -4.5F);
        hair56.setTexSize(128, 64);
        hair56.mirror = true;
        setRotation(hair56, 0F, 0F, 0F);

        hair51 = new ModelRenderer(this, 58, 27);
        hair51.addBox(0F, 0F, 0F, 1, 5, 2);
        hair51.setPos(-5F, -5F, 3F);
        hair51.setTexSize(128, 64);
        hair51.mirror = true;
        setRotation(hair51, 0F, 0F, 0F);

        hair55 = new ModelRenderer(this, 58, 26);
        hair55.addBox(0F, 0F, 0F, 1, 1, 3);
        hair55.setPos(4F, -1.5F, -4.5F);
        hair55.setTexSize(128, 64);
        hair55.mirror = true;
        setRotation(hair55, 0F, 0F, 0F);

        hair58 = new ModelRenderer(this, 61, 25);
        hair58.addBox(0F, 0F, 0F, 3, 1, 2);
        hair58.setPos(-1.5F, 2F, -4.5F);
        hair58.setTexSize(128, 64);
        hair58.mirror = true;
        setRotation(hair58, 0F, 0F, 0F);

        hair54 = new ModelRenderer(this, 58, 21);
        hair54.addBox(0F, 0F, 0F, 1, 2, 4);
        hair54.setPos(4F, -3F, -1F);
        hair54.setTexSize(128, 64);
        hair54.mirror = true;
        setRotation(hair54, 0F, 0F, 0F);

        hair57 = new ModelRenderer(this, 58, 26);
        hair57.addBox(0F, 0F, 0F, 1, 1, 3);
        hair57.setPos(-5F, -1.5F, -4.5F);
        hair57.setTexSize(128, 64);
        hair57.mirror = true;
        setRotation(hair57, 0F, 0F, 0F);

        hair59 = new ModelRenderer(this, 61, 25);
        hair59.addBox(0F, 0F, 0F, 9, 1, 3);
        hair59.setPos(-4.5F, -0.9F, -4.5F);
        hair59.setTexSize(128, 64);
        hair59.mirror = true;
        setRotation(hair59, 0F, 0F, 0F);

        hair49 = new ModelRenderer(this, 58, 27);
        hair49.addBox(0F, 0F, 0F, 1, 3, 9);
        hair49.setPos(-5F, -8F, -4F);
        hair49.setTexSize(128, 64);
        hair49.mirror = true;
        setRotation(hair49, 0F, 0F, 0F);

        hair61 = new ModelRenderer(this, 61, 25);
        hair61.addBox(0F, 0F, 0F, 5, 1, 2);
        hair61.setPos(-2.5F, 1F, -4.5F);
        hair61.setTexSize(128, 64);
        hair61.mirror = true;
        setRotation(hair61, 0F, 0F, 0F);

        hair52 = new ModelRenderer(this, 58, 27);
        hair52.addBox(0F, 0F, 0F, 1, 1, 1);
        hair52.setPos(-5F, -5F, -3F);
        hair52.setTexSize(128, 64);
        hair52.mirror = true;
        setRotation(hair52, 0F, 0F, 0F);

        hair60 = new ModelRenderer(this, 61, 25);
        hair60.addBox(0F, 0F, 0F, 7, 1, 2);
        hair60.setPos(-3.5F, 0F, -4.5F);
        hair60.setTexSize(128, 64);
        hair60.mirror = true;
        setRotation(hair60, 0F, 0F, 0F);

        hair46 = new ModelRenderer(this, 58, 21);
        hair46.addBox(0F, 0F, 0F, 10, 1, 9);
        hair46.setPos(-5F, -9F, -4F);
        hair46.setTexSize(128, 64);
        hair46.mirror = true;
        setRotation(hair46, 0F, 0F, 0F);

        hair47 = new ModelRenderer(this, 58, 21);
        hair47.addBox(0F, 0F, 0F, 4, 2, 1);
        hair47.setPos(-2F, -9F, -5F);
        hair47.setTexSize(128, 64);
        hair47.mirror = true;
        setRotation(hair47, 0F, 0F, 0F);

        hat.visible = false;

        leftArm.addChild(gauntlet1);
        rightArm.addChild(gauntlet2);
        body.addChild(toolHandle1);
        body.addChild(toolHandle2);
        body.addChild(pocket);

        head.addChild(hair1);
        head.addChild(hair2);
        head.addChild(hair17);
        head.addChild(hair16);
        head.addChild(hair15);
        head.addChild(hair14);
        head.addChild(hair13);
        head.addChild(hair12);
        head.addChild(hair11);
        head.addChild(hair10);
        head.addChild(hair9);
        head.addChild(hair8);
        head.addChild(hair7);
        head.addChild(hair6);
        head.addChild(hair5);
        head.addChild(hair4);
        head.addChild(hair18);
        head.addChild(hair19);
        head.addChild(hair20);
        head.addChild(hair3);
        head.addChild(hair_37);
        head.addChild(hair_38);
        head.addChild(hair_36);
        head.addChild(hair_35);
        head.addChild(hair21);
        head.addChild(hair_23);
        head.addChild(hair_24);
        head.addChild(hair_22);
        head.addChild(hair_25);
        head.addChild(hair_26);
        head.addChild(hair_27);
        head.addChild(hair_28);
        head.addChild(hair_29);
        head.addChild(hair_30);
        head.addChild(hair_31);
        head.addChild(hair_32);
        head.addChild(hair_33);
        head.addChild(hair_34);
        head.addChild(hair39);
        head.addChild(hair40);
        head.addChild(hair41);
        head.addChild(hair42);
        head.addChild(hair43);
        head.addChild(hair44);
        head.addChild(hair45);
        head.addChild(hair48);
        head.addChild(hair50);
        head.addChild(hair53);
        head.addChild(hair56);
        head.addChild(hair51);
        head.addChild(hair55);
        head.addChild(hair58);
        head.addChild(hair54);
        head.addChild(hair57);
        head.addChild(hair59);
        head.addChild(hair49);
        head.addChild(hair61);
        head.addChild(hair52);
        head.addChild(hair60);
        head.addChild(hair46);
        head.addChild(hair47);
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
