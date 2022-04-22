package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityAlchemistFemale extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityAlchemistFemale()
    {
        final ModelRenderer HairExtension;
        final ModelRenderer Ponytail;
        final ModelRenderer ponyTailTip_r1;
        final ModelRenderer ponytailBase_r1;
        final ModelRenderer PointHat;
        final ModelRenderer top4_r1;
        final ModelRenderer top3_r1;
        final ModelRenderer top2_r1;
        final ModelRenderer glasses;
        final ModelRenderer breast;
        final ModelRenderer capeBody;
        final ModelRenderer Potion1;
        final ModelRenderer IngredientPouch;
        final ModelRenderer top_r1;
        final ModelRenderer capeShoulderRight;
        final ModelRenderer capeShoulderLeft;

        texWidth = 128;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 0.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);
        head.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        HairExtension = new ModelRenderer(this);
        HairExtension.setPos(0.0F, 1.0F, 0.0F);
        head.addChild(HairExtension);
        HairExtension.texOffs(56, 0).addBox(-4.0F, 0.0F, 3.0F, 8.0F, 7.0F, 1.0F, 0.5F, false);

        Ponytail = new ModelRenderer(this);
        Ponytail.setPos(0.0F, 24.0F, 0.0F);
        head.addChild(Ponytail);
        
        ponyTailTip_r1 = new ModelRenderer(this);
        ponyTailTip_r1.setPos(-0.5F, -25.0F, 4.8F);
        Ponytail.addChild(ponyTailTip_r1);
        setRotationAngle(ponyTailTip_r1, 0.2231F, 0.0F, 0.0F);
        ponyTailTip_r1.texOffs(88, 55).addBox(0.0F, 0.0F, 0.0F, 1.0F, 5.0F, 1.0F, 0.1F, false);

        ponytailBase_r1 = new ModelRenderer(this);
        ponytailBase_r1.setPos(-1.0F, -28.0F, 2.0F);
        Ponytail.addChild(ponytailBase_r1);
        setRotationAngle(ponytailBase_r1, 0.5577F, 0.0F, 0.0F);
        ponytailBase_r1.texOffs(86, 48).addBox(0.0F, 0.0F, 0.0F, 2.0F, 5.0F, 2.0F, 0.0F, false);

        PointHat = new ModelRenderer(this);
        PointHat.setPos(0.0F, -6.2F, -0.1F);
        head.addChild(PointHat);
        setRotationAngle(PointHat, -1.0272F, 1.4692F, -1.0325F);
        PointHat.texOffs(96, 0).addBox(-4.0F, -2.19F, -4.0F, 8.0F, 2.0F, 8.0F, 0.81F, false);
        PointHat.texOffs(76, 10).addBox(-8.0F, -0.4F, -5.0F, 16.0F, 1.0F, 10.0F, 0.0F, false);
        PointHat.texOffs(98, 21).addBox(-5.0F, -0.4F, -8.0F, 10.0F, 1.0F, 3.0F, 0.0F, false);
        PointHat.texOffs(93, 21).addBox(-7.0F, -0.4F, -7.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
        PointHat.texOffs(85, 21).addBox(5.0F, -0.4F, -7.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
        PointHat.texOffs(93, 25).addBox(-7.0F, -0.4F, 5.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
        PointHat.texOffs(85, 25).addBox(5.0F, -0.4F, 5.0F, 2.0F, 1.0F, 2.0F, 0.0F, false);
        PointHat.texOffs(98, 25).addBox(-5.0F, -0.4F, 5.0F, 10.0F, 1.0F, 3.0F, 0.0F, false);

        top4_r1 = new ModelRenderer(this);
        top4_r1.setPos(0.0F, 0.1F, -0.4F);
        PointHat.addChild(top4_r1);
        setRotationAngle(top4_r1, -0.5638F, -0.2158F, -0.1694F);
        top4_r1.texOffs(84, 0).addBox(-0.3F, -9.5F, -2.4F, 2.0F, 3.0F, 2.0F, 0.0F, false);

        top3_r1 = new ModelRenderer(this);
        top3_r1.setPos(0.0F, 0.1F, -0.4F);
        PointHat.addChild(top3_r1);
        setRotationAngle(top3_r1, -0.3051F, -0.0885F, 0.0096F);
        top3_r1.texOffs(88, 1).addBox(-2.5F, -7.5F, -1.8F, 4.0F, 3.0F, 4.0F, 0.0F, false);

        top2_r1 = new ModelRenderer(this);
        top2_r1.setPos(0.0F, 0.1F, 0.0F);
        PointHat.addChild(top2_r1);
        setRotationAngle(top2_r1, -0.1752F, -0.0859F, 0.0152F);
        top2_r1.texOffs(58, 9).addBox(-3.5F, -4.9F, -3.8F, 7.0F, 3.0F, 7.0F, 0.0F, false);

        glasses = new ModelRenderer(this);
        glasses.setPos(0.0F, -3.9F, -2.1F);
        head.addChild(glasses);
        setRotationAngle(glasses, 0.0873F, 0.0F, 0.0F);
        glasses.texOffs(54, 19).addBox(-5.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, -2.0F, false);
        glasses.texOffs(54, 27).addBox(-1.5F, -3.6F, -1.3F, 7.0F, 7.0F, 1.0F, -2.0F, false);
        glasses.texOffs(70, 19).addBox(-1.0F, -0.6F, -2.55F, 2.0F, 1.0F, 1.0F, -0.3F, false);
        glasses.texOffs(70, 21).addBox(-4.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, -0.6F, false);
        glasses.texOffs(65, 30).addBox(2.8F, -0.9F, -2.89F, 2.0F, 2.0F, 5.0F, -0.6F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 0.0F);
        body.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.0F, false);
        body.texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, 0.25F, false);

        breast = new ModelRenderer(this);
        breast.setPos(-1.0F, 3.0F, 4.0F);
        body.addChild(breast);
        setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
        breast.texOffs(64, 49).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, 0.0F, false);
        breast.texOffs(64, 55).addBox(-3.0F, 1.8938F, -5.716F, 8.0F, 3.0F, 3.0F, 0.25F, false);

        capeBody = new ModelRenderer(this);
        capeBody.setPos(0.0F, 5.0F, 0.0F);
        body.addChild(capeBody);
        capeBody.texOffs(102, 36).addBox(-4.0F, -5.2F, -2.5F, 8.0F, 2.0F, 5.0F, 0.21F, false);
        capeBody.texOffs(108, 43).addBox(-5.0F, -3.0F, 2.7F, 10.0F, 18.0F, 0.0F, 0.0F, false);

        Potion1 = new ModelRenderer(this);
        Potion1.setPos(1.3F, 4.78F, -4.2F);
        body.addChild(Potion1);
        Potion1.texOffs(81, 37).addBox(-1.4152F, 3.4965F, -1.4F, 5.0F, 6.0F, 5.0F, -1.4F, false);
        Potion1.texOffs(91, 52).addBox(-0.9152F, 4.2965F, -0.9F, 4.0F, 5.0F, 4.0F, -1.3F, false);
        Potion1.texOffs(96, 48).addBox(0.0848F, 3.6465F, 0.1F, 2.0F, 2.0F, 2.0F, -0.5F, false);
        Potion1.texOffs(61, 39).addBox(-1.4152F, 4.5465F, -1.4F, 5.0F, 4.0F, 5.0F, -1.3F, false);

        IngredientPouch = new ModelRenderer(this);
        IngredientPouch.setPos(-2.0667F, 10.3417F, -2.65F);
        body.addChild(IngredientPouch);
        setRotationAngle(IngredientPouch, 0.0F, 0.0F, 0.0436F);
        IngredientPouch.texOffs(78, 35).addBox(-1.1333F, -0.9417F, -0.35F, 2.0F, 1.0F, 1.0F, -0.1F, false);
        IngredientPouch.texOffs(79, 33).addBox(-0.6333F, -1.4417F, -0.35F, 1.0F, 1.0F, 1.0F, -0.25F, false);
        IngredientPouch.texOffs(84, 33).addBox(-1.6333F, -0.2417F, -0.35F, 3.0F, 3.0F, 1.0F, 0.0F, false);
        IngredientPouch.texOffs(86, 28).addBox(-1.6333F, -0.1417F, -1.45F, 3.0F, 3.0F, 2.0F, -0.4F, false);
        IngredientPouch.texOffs(97, 36).addBox(-1.3333F, -1.9417F, -0.65F, 4.0F, 4.0F, 1.0F, -1.0F, false);

        top_r1 = new ModelRenderer(this);
        top_r1.setPos(-0.1333F, 1.3583F, 0.15F);
        IngredientPouch.addChild(top_r1);
        setRotationAngle(top_r1, 0.0F, -0.2618F, 0.0F);
        top_r1.texOffs(81, 31).addBox(-0.5F, -2.65F, -0.5F, 1.0F, 0.0F, 1.0F, -0.1F, false);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 2.0F, 0.0F);
        rightArm.texOffs(40, 16).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        rightArm.texOffs(40, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

        capeShoulderRight = new ModelRenderer(this);
        capeShoulderRight.setPos(-1.1F, -1.2F, 0.0F);
        rightArm.addChild(capeShoulderRight);
        capeShoulderRight.texOffs(112, 29).addBox(-1.0F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, 0.2F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 2.0F, 0.0F);
        leftArm.texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F, false);
        leftArm.texOffs(48, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.25F, false);

        capeShoulderLeft = new ModelRenderer(this);
        capeShoulderLeft.setPos(1.1F, -1.2F, 0.0F);
        leftArm.addChild(capeShoulderLeft);
        capeShoulderLeft.texOffs(96, 29).addBox(-2.0F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, 0.2F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.9F, 12.0F, 0.0F);
        rightLeg.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        rightLeg.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(1.9F, 12.0F, 0.0F);
        leftLeg.texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F, false);
        leftLeg.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.25F, false);

        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
