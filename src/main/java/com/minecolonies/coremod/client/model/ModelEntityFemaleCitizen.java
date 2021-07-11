// Made with Blockbench 3.5.1
// Exported for Minecraft version 1.15
// Paste this class into your mod and generate all required imports
package com.minecolonies.coremod.client.model;

import com.minecolonies.api.client.render.modeltype.CitizenModel;
import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ModelEntityFemaleCitizen extends CitizenModel<AbstractEntityCitizen>
{
    public ModelEntityFemaleCitizen()
    {
        ModelRenderer hair;
        ModelRenderer hatPiece;
        ModelRenderer breast;
        ModelRenderer dressPart1;
        ModelRenderer dressPart2;
        ModelRenderer dressPart3;

        texWidth = 64;
        texHeight = 64;

        head = new ModelRenderer(this);
        head.setPos(0.0F, 0.0F, 1.0F);
        head.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.0F, false);

        hair = new ModelRenderer(this);
        hair.setPos(0.0F, 0.0F, 1.0F);
        head.addChild(hair);
        hair.texOffs(46, 17).addBox(-4.0F, 0.4F, 2.1F, 8.0F, 7.0F, 1.0F, 0.5F, false);

        hatPiece = new ModelRenderer(this);
        hatPiece.setPos(0.0F, 0.0F, 0.0F);
        head.addChild(hatPiece);
        hatPiece.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, 0.5F, false);

        body = new ModelRenderer(this);
        body.setPos(0.0F, 0.0F, 3.0F);
        body.texOffs(12, 17).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 3.0F, 0.0F, false);

        breast = new ModelRenderer(this);
        breast.setPos(-1.0F, 3.0F, 1.0F);
        body.addChild(breast);
        setRotationAngle(breast, -0.5236F, 0.0F, 0.0F);
        breast.texOffs(0, 33).addBox(-3.0F, 2.0F, -4.5F, 8.0F, 4.0F, 3.0F, 0.0F, false);

        dressPart1 = new ModelRenderer(this);
        dressPart1.setPos(0.0F, 11.0F, 0.0F);
        body.addChild(dressPart1);
        dressPart1.texOffs(26, 46).addBox(-5.0F, 2.0F, -7.0F, 10.0F, 9.0F, 9.0F, 0.0F, false);

        dressPart2 = new ModelRenderer(this);
        dressPart2.setPos(0.0F, 11.0F, 0.0F);
        body.addChild(dressPart2);
        dressPart2.texOffs(28, 38).addBox(-5.0F, 1.0F, -6.0F, 10.0F, 1.0F, 7.0F, 0.0F, false);

        dressPart3 = new ModelRenderer(this);
        dressPart3.setPos(0.0F, 11.0F, 0.0F);
        body.addChild(dressPart3);
        dressPart3.texOffs(32, 32).addBox(-4.0F, 0.0F, -5.0F, 8.0F, 1.0F, 5.0F, 0.0F, false);

        leftArm = new ModelRenderer(this);
        leftArm.setPos(5.0F, 0.0F, 0.0F);
        leftArm.texOffs(34, 17).addBox(-1.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);

        rightArm = new ModelRenderer(this);
        rightArm.setPos(-5.0F, 0.0F, 0.0F);
        rightArm.texOffs(34, 17).addBox(-2.0F, -2.0F, -1.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

        rightLeg = new ModelRenderer(this);
        rightLeg.setPos(-1.0F, 12.0F, 1.0F);
        rightLeg.texOffs(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, 0.0F, false);

        leftLeg = new ModelRenderer(this);
        leftLeg.setPos(2.0F, 12.0F, 1.0F);
        leftLeg.texOffs(0, 17).addBox(-2.0F, 0.0F, -2.0F, 3.0F, 12.0F, 3.0F, 0.0F, true);
        hat.visible = false;
    }

    public void setRotationAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.xRot = x;
        modelRenderer.yRot = y;
        modelRenderer.zRot = z;
    }
}
