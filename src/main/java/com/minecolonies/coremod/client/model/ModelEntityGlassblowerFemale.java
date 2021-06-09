package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;
import org.jetbrains.annotations.NotNull;

public class ModelEntityGlassblowerFemale extends CitizenModel<AbstractEntityCitizen>
{
    //fields
    public ModelEntityGlassblowerFemale()
    {
        ModelRenderer pocket;
        ModelRenderer thing1;
        ModelRenderer thing2;
        ModelRenderer gauntlet2;
        ModelRenderer gauntlet1;
        ModelRenderer Hair5;
        ModelRenderer Hair1;
        ModelRenderer Hair2;
        ModelRenderer Hair4;
        ModelRenderer Hair3;
        ModelRenderer Hair6;
        ModelRenderer Hair7;
        ModelRenderer Hair8;
        ModelRenderer Hair12;
        ModelRenderer Hair13;
        ModelRenderer Hair11;
        ModelRenderer Hair15;
        ModelRenderer Hair17;
        ModelRenderer Hair18;
        ModelRenderer Hair19;
        ModelRenderer Hair192;
        ModelRenderer Hair14;
        ModelRenderer Hair20;
        ModelRenderer Hair21;
        ModelRenderer Hair16;
        ModelRenderer Hair10;
        ModelRenderer Hair9;
        ModelRenderer bipedChest;
        ModelRenderer PonytailB;
        ModelRenderer PonytailT;
        ModelRenderer Hair22;
        ModelRenderer Hair23;
        ModelRenderer Hair24;
        ModelRenderer Hair25;
        ModelRenderer Hair26;
        ModelRenderer Hair28;
        ModelRenderer Hair29;
        ModelRenderer Hair30;
        ModelRenderer Hair31;
        ModelRenderer Hair32;
        ModelRenderer Hair33;
        ModelRenderer Hair35;
        ModelRenderer Hair34;
        ModelRenderer Hair37;
        ModelRenderer Hair38;
        ModelRenderer Hair39;
        ModelRenderer Hair40;
        ModelRenderer Hair42;
        ModelRenderer Hair43;
        ModelRenderer Hair41;
        ModelRenderer Hair27;
        ModelRenderer Hair44;
        ModelRenderer Hair45;
        ModelRenderer Hair46;
        ModelRenderer Hair47;
        ModelRenderer Hair48;
        ModelRenderer Hair49;
        ModelRenderer Hair51;
        ModelRenderer Hair52;
        ModelRenderer Hair55;
        ModelRenderer Hair54;
        ModelRenderer Hair53;
        ModelRenderer Hair57;
        ModelRenderer Hair58;
        ModelRenderer Hair59;
        ModelRenderer Hair60;
        ModelRenderer Hair61;
        ModelRenderer Hair62;
        ModelRenderer Hair63;
        ModelRenderer Hair56;
        ModelRenderer Hair50;

        texWidth = 128;
        texHeight = 64;

        pocket = new ModelRenderer(this, 19, 28);
        pocket.addBox(0F, 0F, 0F, 4, 3, 1);
        pocket.setPos(-2F, 8F, -3F);
        pocket.setTexSize(128, 64);
        setRotation(pocket, 0F, 0F, 0F);

        thing1 = new ModelRenderer(this, 10, 32);
        thing1.addBox(0F, 0F, 0F, 1, 2, 1);
        thing1.setPos(-1F, 6F, -3F);
        thing1.setTexSize(128, 64);
        setRotation(thing1, 0F, 0F, 0F);

        thing2 = new ModelRenderer(this, 10, 32);
        thing2.addBox(0F, 0F, 0F, 1, 2, 1);
        thing2.setPos(0F, 6F, -3F);
        thing2.setTexSize(128, 64);
        setRotation(thing2, 0F, 0F, 0F);

        gauntlet1 = new ModelRenderer(this, 64, 0);
        gauntlet1.addBox(-5F, -1.5F, 0F, 5, 1, 5);
        gauntlet1.setPos(3.5F, 7F, -2.5F);
        gauntlet1.setTexSize(128, 64);
        gauntlet1.mirror = true;
        setRotation(gauntlet1, 0F, 0F, 0F);

        gauntlet2 = new ModelRenderer(this, 64, 0);
        gauntlet2.addBox(5F, -1.5F, 0F, 5, 1, 5);
        gauntlet2.setPos(-8.5F, 7F, -2.5F);
        gauntlet2.setTexSize(128, 64);
        gauntlet2.mirror = true;
        setRotation(gauntlet2, 0F, 0F, 0F);

        Hair5 = new ModelRenderer(this, 59, 0);
        Hair5.addBox(0F, 0F, 0F, 1, 3, 2);
        Hair5.setPos(4F, -4F, 2F);
        Hair5.setTexSize(128, 64);
        Hair5.mirror = true;
        setRotation(Hair5, 0F, 0F, 0F);

        Hair1 = new ModelRenderer(this, 59, 0);
        Hair1.addBox(0F, 0F, 0F, 2, 1, 1);
        Hair1.setPos(-4F, -6F, -5F);
        Hair1.setTexSize(128, 64);
        Hair1.mirror = true;
        setRotation(Hair1, 0F, 0F, 0F);

        Hair2 = new ModelRenderer(this, 59, 0);
        Hair2.addBox(0F, 0F, 0F, 8, 2, 9);
        Hair2.setPos(-4F, -9F, -5F);
        Hair2.setTexSize(128, 64);
        Hair2.mirror = true;
        setRotation(Hair2, 0F, 0F, 0F);

        Hair4 = new ModelRenderer(this, 59, 0);
        Hair4.addBox(0F, 0F, 0F, 8, 9, 1);
        Hair4.setPos(-4F, -9F, 4F);
        Hair4.setTexSize(128, 64);
        Hair4.mirror = true;
        setRotation(Hair4, 0F, 0F, 0F);

        Hair3 = new ModelRenderer(this, 59, 0);
        Hair3.addBox(0F, 0F, 0F, 1, 2, 2);
        Hair3.setPos(4F, -4F, 0F);
        Hair3.setTexSize(128, 64);
        Hair3.mirror = true;
        setRotation(Hair3, 0F, 0F, 0F);

        Hair6 = new ModelRenderer(this, 59, 0);
        Hair6.addBox(0F, 0F, 0F, 1, 1, 1);
        Hair6.setPos(4F, -4F, -1F);
        Hair6.setTexSize(128, 64);
        Hair6.mirror = true;
        setRotation(Hair6, 0F, 0F, 0F);

        Hair7 = new ModelRenderer(this, 59, 0);
        Hair7.addBox(0F, 0F, 0F, 1, 4, 3);
        Hair7.setPos(4F, -8F, -2F);
        Hair7.setTexSize(128, 64);
        Hair7.mirror = true;
        setRotation(Hair7, 0F, 0F, 0F);

        Hair8 = new ModelRenderer(this, 59, 0);
        Hair8.addBox(0F, 0F, 0F, 1, 2, 2);
        Hair8.setPos(4F, -8F, -4F);
        Hair8.setTexSize(128, 64);
        Hair8.mirror = true;
        setRotation(Hair8, 0F, 0F, 0F);

        Hair12 = new ModelRenderer(this, 59, 0);
        Hair12.addBox(0F, 0F, 0F, 1, 2, 3);
        Hair12.setPos(-5F, -8F, -5F);
        Hair12.setTexSize(128, 64);
        Hair12.mirror = true;
        setRotation(Hair12, 0F, 0F, 0F);

        Hair13 = new ModelRenderer(this, 59, 0);
        Hair13.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair13.setPos(-5F, -6F, -4F);
        Hair13.setTexSize(128, 64);
        Hair13.mirror = true;
        setRotation(Hair13, 0F, 0F, 0F);

        Hair11 = new ModelRenderer(this, 59, 0);
        Hair11.addBox(0F, 0F, 0F, 1, 4, 3);
        Hair11.setPos(-5F, -8F, -2F);
        Hair11.setTexSize(128, 64);
        Hair11.mirror = true;
        setRotation(Hair11, 0F, 0F, 0F);

        Hair15 = new ModelRenderer(this, 59, 0);
        Hair15.addBox(0F, 0F, 0F, 1, 2, 2);
        Hair15.setPos(-5F, -4F, 0F);
        Hair15.setTexSize(128, 64);
        Hair15.mirror = true;
        setRotation(Hair15, 0F, 0F, 0F);

        Hair17 = new ModelRenderer(this, 59, 0);
        Hair17.addBox(0F, 0F, 0F, 8, 1, 1);
        Hair17.setPos(-4F, -8F, -5F);
        Hair17.setTexSize(128, 64);
        Hair17.mirror = true;
        setRotation(Hair17, 0F, 0F, 0F);

        Hair18 = new ModelRenderer(this, 59, 0);
        Hair18.addBox(0F, 0F, 0F, 3, 1, 1);
        Hair18.setPos(-4F, -7F, -5F);
        Hair18.setTexSize(128, 64);
        Hair18.mirror = true;
        setRotation(Hair18, 0F, 0F, 0F);

        Hair19 = new ModelRenderer(this, 59, 0);
        Hair19.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair19.setPos(4F, -7F, 1F);
        Hair19.setTexSize(128, 64);
        Hair19.mirror = true;
        setRotation(Hair19, 0F, 0F, 0F);

        Hair192 = new ModelRenderer(this, 59, 0);
        Hair192.addBox(0F, 0F, 0F, 3, 1, 1);
        Hair192.setPos(1F, -7F, -5F);
        Hair192.setTexSize(128, 64);
        Hair192.mirror = true;
        setRotation(Hair192, 0F, 0F, 0F);

        Hair14 = new ModelRenderer(this, 59, 0);
        Hair14.addBox(0F, 0F, 0F, 1, 1, 1);
        Hair14.setPos(-5F, -4F, -1F);
        Hair14.setTexSize(128, 64);
        Hair14.mirror = true;
        setRotation(Hair14, 0F, 0F, 0F);

        Hair20 = new ModelRenderer(this, 59, 0);
        Hair20.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair20.setPos(-5F, -7F, 1F);
        Hair20.setTexSize(128, 64);
        Hair20.mirror = true;
        setRotation(Hair20, 0F, 0F, 0F);

        Hair21 = new ModelRenderer(this, 59, 0);
        Hair21.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair21.setPos(-5F, -8F, 1F);
        Hair21.setTexSize(128, 64);
        Hair21.mirror = true;
        setRotation(Hair21, 0F, 0F, 0F);

        Hair16 = new ModelRenderer(this, 59, 0);
        Hair16.addBox(0F, 0F, 0F, 1, 3, 2);
        Hair16.setPos(-5F, -4F, 2F);
        Hair16.setTexSize(128, 64);
        Hair16.mirror = true;
        setRotation(Hair16, 0F, 0F, 0F);

        Hair10 = new ModelRenderer(this, 59, 0);
        Hair10.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair10.setPos(4F, -8F, 1F);
        Hair10.setTexSize(128, 64);
        Hair10.mirror = true;
        setRotation(Hair10, 0F, 0F, 0F);

        Hair9 = new ModelRenderer(this, 59, 0);
        Hair9.addBox(0F, 0F, 0F, 1, 1, 1);
        Hair9.setPos(4F, -6F, -3F);
        Hair9.setTexSize(128, 64);
        Hair9.mirror = true;
        setRotation(Hair9, 0F, 0F, 0F);

        rightArm = new ModelRenderer(this, 40, 16);
        rightArm.addBox(-3F, -2F, -2F, 4, 12, 4);
        rightArm.setPos(-5F, 2F, 0F);
        rightArm.setTexSize(128, 64);
        rightArm.mirror = true;
        setRotation(rightArm, 0F, 0F, 0F);

        leftArm.mirror = true;
        leftArm = new ModelRenderer(this, 40, 16);
        leftArm.addBox(-1F, -2F, -2F, 4, 12, 4);
        leftArm.setPos(5F, 2F, 0F);
        leftArm.setTexSize(128, 64);
        leftArm.mirror = true;
        setRotation(leftArm, 0F, 0F, 0F);

        bipedChest = new ModelRenderer(this, 37, 32);
        bipedChest.addBox(-3.5F, 2.7F, -0.6F, 6, 3, 4);
        bipedChest.setPos(0.5F, 0F, 0F);
        bipedChest.setTexSize(128, 64);
        setRotation(bipedChest, -0.5934119F, 0F, 0F);

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
        body.addBox(-4F, 0F, -2F, 8, 12, 4);
        body.setPos(0F, 0F, 0F);
        body.setTexSize(128, 64);
        body.mirror = true;
        setRotation(body, 0F, 0F, 0F);

        head = new ModelRenderer(this, 0, 1);
        head.addBox(-4F, -7F, -4F, 8, 7, 8);
        head.setPos(0F, 0F, 0F);
        head.setTexSize(128, 64);
        head.mirror = true;
        setRotation(head, 0F, 0F, 0F);

        PonytailB = new ModelRenderer(this, 76, 9);
        PonytailB.addBox(-0.5F, 2.4F, 3.7F, 1, 5, 1);
        PonytailB.setPos(0F, 0F, 1F);
        PonytailB.setTexSize(128, 64);
        PonytailB.mirror = true;
        setRotation(PonytailB, 0.296706F, 0F, 0F);

        PonytailT = new ModelRenderer(this, 75, 2);
        PonytailT.addBox(-1F, -2F, 3.4F, 2, 5, 2);
        PonytailT.setPos(0F, 1F, 1F);
        PonytailT.setTexSize(128, 64);
        PonytailT.mirror = true;
        setRotation(PonytailT, 0.418879F, 0F, 0F);

        Hair22 = new ModelRenderer(this, 59, 23);
        Hair22.addBox(0F, 0F, 0F, 4, 3, 1);
        Hair22.setPos(-2F, 2F, 4F);
        Hair22.setTexSize(128, 64);
        Hair22.mirror = true;
        setRotation(Hair22, 0F, 0F, 0F);

        Hair23 = new ModelRenderer(this, 59, 23);
        Hair23.addBox(0F, 0F, 0F, 1, 3, 2);
        Hair23.setPos(4F, -4F, 2F);
        Hair23.setTexSize(128, 64);
        Hair23.mirror = true;
        setRotation(Hair23, 0F, 0F, 0F);

        Hair24 = new ModelRenderer(this, 59, 23);
        Hair24.addBox(0F, 0F, 0F, 4, 2, 1);
        Hair24.setPos(-2F, -7F, -5F);
        Hair24.setTexSize(128, 64);
        Hair24.mirror = true;
        setRotation(Hair24, 0F, 0F, 0F);

        Hair25 = new ModelRenderer(this, 59, 23);
        Hair25.addBox(0F, 0F, 0F, 8, 2, 9);
        Hair25.setPos(-4F, -9F, -5F);
        Hair25.setTexSize(128, 64);
        Hair25.mirror = true;
        setRotation(Hair25, 0F, 0F, 0F);

        Hair26 = new ModelRenderer(this, 59, 23);
        Hair26.addBox(0F, 0F, 0F, 1, 1, 3);
        Hair26.setPos(4F, -4F, -1F);
        Hair26.setTexSize(128, 64);
        Hair26.mirror = true;
        setRotation(Hair26, 0F, 0F, 0F);

        Hair28 = new ModelRenderer(this, 59, 23);
        Hair28.addBox(0F, 0F, 0F, 1, 2, 2);
        Hair28.setPos(4F, -8F, -4F);
        Hair28.setTexSize(128, 64);
        Hair28.mirror = true;
        setRotation(Hair28, 0F, 0F, 0F);

        Hair29 = new ModelRenderer(this, 59, 15);
        Hair29.addBox(0F, 0F, 0F, 1, 1, 1);
        Hair29.setPos(4F, -6F, -3F);
        Hair29.setTexSize(128, 64);
        Hair29.mirror = true;
        setRotation(Hair29, 0F, 0F, 0F);

        Hair30 = new ModelRenderer(this, 59, 23);
        Hair30.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair30.setPos(4F, -8F, 1F);
        Hair30.setTexSize(128, 64);
        Hair30.mirror = true;
        setRotation(Hair30, 0F, 0F, 0F);

        Hair31 = new ModelRenderer(this, 59, 23);
        Hair31.addBox(0F, 0F, 0F, 1, 4, 3);
        Hair31.setPos(-5F, -8F, -2F);
        Hair31.setTexSize(128, 64);
        Hair31.mirror = true;
        setRotation(Hair31, 0F, 0F, 0F);

        Hair32 = new ModelRenderer(this, 59, 23);
        Hair32.addBox(0F, 0F, 0F, 1, 2, 3);
        Hair32.setPos(-5F, -8F, -5F);
        Hair32.setTexSize(128, 64);
        Hair32.mirror = true;
        setRotation(Hair32, 0F, 0F, 0F);

        Hair33 = new ModelRenderer(this, 59, 23);
        Hair33.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair33.setPos(-5F, -6F, -4F);
        Hair33.setTexSize(128, 64);
        Hair33.mirror = true;
        setRotation(Hair33, 0F, 0F, 0F);

        Hair35 = new ModelRenderer(this, 59, 23);
        Hair35.addBox(0F, 0F, 0F, 1, 3, 2);
        Hair35.setPos(-5F, -4F, 2F);
        Hair35.setTexSize(128, 64);
        Hair35.mirror = true;
        setRotation(Hair35, 0F, 0F, 0F);

        Hair34 = new ModelRenderer(this, 59, 23);
        Hair34.addBox(0F, 0F, 0F, 1, 1, 3);
        Hair34.setPos(-5F, -4F, -1F);
        Hair34.setTexSize(128, 64);
        Hair34.mirror = true;
        setRotation(Hair34, 0F, 0F, 0F);

        Hair37 = new ModelRenderer(this, 59, 23);
        Hair37.addBox(0F, 0F, 0F, 2, 1, 1);
        Hair37.setPos(-4F, -7F, -5F);
        Hair37.setTexSize(128, 64);
        Hair37.mirror = true;
        setRotation(Hair37, 0F, 0F, 0F);

        Hair38 = new ModelRenderer(this, 59, 23);
        Hair38.addBox(0F, 0F, 0F, 2, 1, 1);
        Hair38.setPos(2F, -7F, -5F);
        Hair38.setTexSize(128, 64);
        Hair38.mirror = true;
        setRotation(Hair38, 0F, 0F, 0F);

        Hair39 = new ModelRenderer(this, 59, 23);
        Hair39.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair39.setPos(4F, -7F, 1F);
        Hair39.setTexSize(128, 64);
        Hair39.mirror = true;
        setRotation(Hair39, 0F, 0F, 0F);

        Hair40 = new ModelRenderer(this, 59, 23);
        Hair40.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair40.setPos(-5F, -7F, 1F);
        Hair40.setTexSize(128, 64);
        Hair40.mirror = true;
        setRotation(Hair40, 0F, 0F, 0F);

        Hair42 = new ModelRenderer(this, 59, 23);
        Hair42.addBox(0F, 0F, 0F, 8, 9, 1);
        Hair42.setPos(-4F, -9F, 4F);
        Hair42.setTexSize(128, 64);
        Hair42.mirror = true;
        setRotation(Hair42, 0F, 0F, 0F);

        Hair43 = new ModelRenderer(this, 59, 23);
        Hair43.addBox(0F, 0F, 0F, 6, 2, 1);
        Hair43.setPos(-3F, 0F, 4F);
        Hair43.setTexSize(128, 64);
        Hair43.mirror = true;
        setRotation(Hair43, 0F, 0F, 0F);

        Hair41 = new ModelRenderer(this, 59, 23);
        Hair41.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair41.setPos(-5F, -8F, 1F);
        Hair41.setTexSize(128, 64);
        Hair41.mirror = true;
        setRotation(Hair41, 0F, 0F, 0F);

        Hair27 = new ModelRenderer(this, 59, 23);
        Hair27.addBox(0F, 0F, 0F, 1, 4, 3);
        Hair27.setPos(4F, -8F, -2F);
        Hair27.setTexSize(128, 64);
        Hair27.mirror = true;
        setRotation(Hair27, 0F, 0F, 0F);

        Hair44 = new ModelRenderer(this, 59, 43);
        Hair44.addBox(0F, 0F, 0F, 1, 3, 2);
        Hair44.setPos(4F, -4F, 2F);
        Hair44.setTexSize(128, 64);
        Hair44.mirror = true;
        setRotation(Hair44, 0F, 0F, 0F);

        Hair45 = new ModelRenderer(this, 59, 43);
        Hair45.addBox(0F, 0F, 0F, 8, 2, 9);
        Hair45.setPos(-4F, -9F, -5F);
        Hair45.setTexSize(128, 64);
        Hair45.mirror = true;
        setRotation(Hair45, 0F, 0F, 0F);

        Hair46 = new ModelRenderer(this, 59, 43);
        Hair46.addBox(0F, 0F, 0F, 1, 1, 3);
        Hair46.setPos(4F, -4F, -1F);
        Hair46.setTexSize(128, 64);
        Hair46.mirror = true;
        setRotation(Hair46, 0F, 0F, 0F);

        Hair47 = new ModelRenderer(this, 59, 43);
        Hair47.addBox(0F, 0F, 0F, 1, 4, 3);
        Hair47.setPos(4F, -8F, -2F);
        Hair47.setTexSize(128, 64);
        Hair47.mirror = true;
        setRotation(Hair47, 0F, 0F, 0F);

        Hair48 = new ModelRenderer(this, 59, 43);
        Hair48.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair48.setPos(4F, -8F, -5F);
        Hair48.setTexSize(128, 64);
        Hair48.mirror = true;
        setRotation(Hair48, 0F, 0F, 0F);

        Hair49 = new ModelRenderer(this, 59, 43);
        Hair49.addBox(0F, 0F, 0F, 1, 2, 1);
        Hair49.setPos(3F, -6F, -5F);
        Hair49.setTexSize(128, 64);
        Hair49.mirror = true;
        setRotation(Hair49, 0F, 0F, 0F);

        Hair51 = new ModelRenderer(this, 59, 43);
        Hair51.addBox(0F, 0F, 0F, 1, 4, 3);
        Hair51.setPos(-5F, -8F, -2F);
        Hair51.setTexSize(128, 64);
        Hair51.mirror = true;
        setRotation(Hair51, 0F, 0F, 0F);

        Hair52 = new ModelRenderer(this, 59, 43);
        Hair52.addBox(0F, 0F, 0F, 1, 2, 3);
        Hair52.setPos(-5F, -8F, -5F);
        Hair52.setTexSize(128, 64);
        Hair52.mirror = true;
        setRotation(Hair52, 0F, 0F, 0F);

        Hair55 = new ModelRenderer(this, 59, 43);
        Hair55.addBox(0F, 0F, 0F, 1, 3, 2);
        Hair55.setPos(-5F, -4F, 2F);
        Hair55.setTexSize(128, 64);
        Hair55.mirror = true;
        setRotation(Hair55, 0F, 0F, 0F);

        Hair54 = new ModelRenderer(this, 59, 43);
        Hair54.addBox(0F, 0F, 0F, 1, 1, 3);
        Hair54.setPos(-5F, -4F, -1F);
        Hair54.setTexSize(128, 64);
        Hair54.mirror = true;
        setRotation(Hair54, 0F, 0F, 0F);

        Hair53 = new ModelRenderer(this, 59, 43);
        Hair53.addBox(0F, 0F, 0F, 1, 3, 1);
        Hair53.setPos(-5F, -6F, -5F);
        Hair53.setTexSize(128, 64);
        Hair53.mirror = true;
        setRotation(Hair53, 0F, 0F, 0F);

        Hair57 = new ModelRenderer(this, 59, 43);
        Hair57.addBox(0F, 0F, 0F, 1, 2, 1);
        Hair57.setPos(-4F, -6F, -5F);
        Hair57.setTexSize(128, 64);
        Hair57.mirror = true;
        setRotation(Hair57, 0F, 0F, 0F);

        Hair58 = new ModelRenderer(this, 59, 43);
        Hair58.addBox(0F, 0F, 0F, 2, 1, 1);
        Hair58.setPos(2F, -7F, -5F);
        Hair58.setTexSize(128, 64);
        Hair58.mirror = true;
        setRotation(Hair58, 0F, 0F, 0F);

        Hair59 = new ModelRenderer(this, 59, 43);
        Hair59.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair59.setPos(4F, -7F, 1F);
        Hair59.setTexSize(128, 64);
        Hair59.mirror = true;
        setRotation(Hair59, 0F, 0F, 0F);

        Hair60 = new ModelRenderer(this, 59, 43);
        Hair60.addBox(0F, 0F, 0F, 1, 3, 3);
        Hair60.setPos(-5F, -7F, 1F);
        Hair60.setTexSize(128, 64);
        Hair60.mirror = true;
        setRotation(Hair60, 0F, 0F, 0F);

        Hair61 = new ModelRenderer(this, 59, 43);
        Hair61.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair61.setPos(-5F, -8F, 1F);
        Hair61.setTexSize(128, 64);
        Hair61.mirror = true;
        setRotation(Hair61, 0F, 0F, 0F);

        Hair62 = new ModelRenderer(this, 59, 43);
        Hair62.addBox(0F, 0F, 0F, 8, 9, 1);
        Hair62.setPos(-4F, -9F, 4F);
        Hair62.setTexSize(128, 64);
        Hair62.mirror = true;
        setRotation(Hair62, 0F, 0F, 0F);

        Hair63 = new ModelRenderer(this, 59, 43);
        Hair63.addBox(0F, 0F, 0F, 2, 1, 1);
        Hair63.setPos(-4F, -7F, -5F);
        Hair63.setTexSize(128, 64);
        Hair63.mirror = true;
        setRotation(Hair63, 0F, 0F, 0F);

        Hair56 = new ModelRenderer(this, 59, 43);
        Hair56.addBox(0F, 0F, 0F, 8, 1, 1);
        Hair56.setPos(-4F, -8F, -5F);
        Hair56.setTexSize(128, 64);
        Hair56.mirror = true;
        setRotation(Hair56, 0F, 0F, 0F);

        Hair50 = new ModelRenderer(this, 59, 43);
        Hair50.addBox(0F, 0F, 0F, 1, 1, 2);
        Hair50.setPos(4F, -8F, 1F);
        Hair50.setTexSize(128, 64);
        Hair50.mirror = true;
        setRotation(Hair50, 0F, 0F, 0F);

        body.addChild(pocket);
        body.addChild(thing1);
        body.addChild(thing2);
        rightArm.addChild(gauntlet2);
        leftArm.addChild(gauntlet1);
        body.addChild(bipedChest);

        head.addChild(PonytailB);
        head.addChild(PonytailT);
        head.addChild(Hair5);
        head.addChild(Hair1);
        head.addChild(Hair2);
        head.addChild(Hair4);
        head.addChild(Hair3);
        head.addChild(Hair6);
        head.addChild(Hair7);
        head.addChild(Hair8);
        head.addChild(Hair12);
        head.addChild(Hair13);
        head.addChild(Hair11);
        head.addChild(Hair15);
        head.addChild(Hair17);
        head.addChild(Hair18);
        head.addChild(Hair19);
        head.addChild(Hair192);
        head.addChild(Hair14);
        head.addChild(Hair20);
        head.addChild(Hair21);
        head.addChild(Hair16);
        head.addChild(Hair10);
        head.addChild(Hair9);
        head.addChild(Hair22);
        head.addChild(Hair23);
        head.addChild(Hair24);
        head.addChild(Hair25);
        head.addChild(Hair26);
        head.addChild(Hair28);
        head.addChild(Hair29);
        head.addChild(Hair30);
        head.addChild(Hair31);
        head.addChild(Hair32);
        head.addChild(Hair33);
        head.addChild(Hair35);
        head.addChild(Hair34);
        head.addChild(Hair37);
        head.addChild(Hair38);
        head.addChild(Hair39);
        head.addChild(Hair40);
        head.addChild(Hair42);
        head.addChild(Hair43);
        head.addChild(Hair41);
        head.addChild(Hair27);
        head.addChild(Hair44);
        head.addChild(Hair45);
        head.addChild(Hair46);
        head.addChild(Hair47);
        head.addChild(Hair48);
        head.addChild(Hair49);
        head.addChild(Hair51);
        head.addChild(Hair52);
        head.addChild(Hair55);
        head.addChild(Hair54);
        head.addChild(Hair53);
        head.addChild(Hair57);
        head.addChild(Hair58);
        head.addChild(Hair59);
        head.addChild(Hair60);
        head.addChild(Hair61);
        head.addChild(Hair62);
        head.addChild(Hair63);
        head.addChild(Hair56);
        head.addChild(Hair50);

        hat.visible = false;
    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.xRot = x;
        model.yRot = y;
        model.zRot = z;
    }
}
