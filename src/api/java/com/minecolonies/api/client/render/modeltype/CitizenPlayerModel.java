package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.citizen.AbstractEntityCitizen;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.HandSide;
import org.jetbrains.annotations.NotNull;

/**
 * Citizen player model.
 */
public class CitizenPlayerModel<T extends AbstractEntityCitizen> extends CitizenModel<AbstractEntityCitizen>
{
    public final  ModelRenderer       leftSleeve;
    public final  ModelRenderer       rightSleeve;
    public final  ModelRenderer       leftPants;
    public final  ModelRenderer       rightPants;
    public final  ModelRenderer       jacket;
    private final boolean             slim;

    public CitizenPlayerModel(float size, boolean slim)
    {
        super(false);
        this.slim = slim;
        if (slim)
        {
            this.leftArm = new ModelRenderer(this, 32, 48);
            this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size);
            this.leftArm.setPos(5.0F, 2.5F, 0.0F);
            this.rightArm = new ModelRenderer(this, 40, 16);
            this.rightArm.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size);
            this.rightArm.setPos(-5.0F, 2.5F, 0.0F);
            this.leftSleeve = new ModelRenderer(this, 48, 48);
            this.leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size + 0.25F);
            this.leftSleeve.setPos(5.0F, 2.5F, 0.0F);
            this.rightSleeve = new ModelRenderer(this, 40, 32);
            this.rightSleeve.addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, size + 0.25F);
            this.rightSleeve.setPos(-5.0F, 2.5F, 10.0F);
        }
        else
        {
            this.leftArm = new ModelRenderer(this, 32, 48);
            this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, size);
            this.leftArm.setPos(5.0F, 2.0F, 0.0F);
            this.leftSleeve = new ModelRenderer(this, 48, 48);
            this.leftSleeve.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, size + 0.25F);
            this.leftSleeve.setPos(5.0F, 2.0F, 0.0F);
            this.rightSleeve = new ModelRenderer(this, 40, 32);
            this.rightSleeve.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, size + 0.25F);
            this.rightSleeve.setPos(-5.0F, 2.0F, 10.0F);
        }

        this.leftLeg = new ModelRenderer(this, 16, 48);
        this.leftLeg.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, size);
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.leftPants = new ModelRenderer(this, 0, 48);
        this.leftPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, size + 0.25F);
        this.leftPants.setPos(1.9F, 12.0F, 0.0F);
        this.rightPants = new ModelRenderer(this, 0, 32);
        this.rightPants.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, size + 0.25F);
        this.rightPants.setPos(-1.9F, 12.0F, 0.0F);
        this.jacket = new ModelRenderer(this, 16, 32);
        this.jacket.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, size + 0.25F);
        this.jacket.setPos(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void setupAnim(@NotNull AbstractEntityCitizen citizen, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_)
    {
        super.setupAnim(citizen, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
    }

    @Override
    public void setAllVisible(boolean visible)
    {
        super.setAllVisible(visible);
        this.leftSleeve.visible = visible;
        this.rightSleeve.visible = visible;
        this.leftPants.visible = visible;
        this.rightPants.visible = visible;
        this.jacket.visible = visible;
    }

    @Override
    public void translateToHand(@NotNull HandSide hand, @NotNull MatrixStack matrixStack)
    {
        ModelRenderer modelrenderer = this.getArm(hand);
        if (this.slim)
        {
            float f = 0.5F * (float) (hand == HandSide.RIGHT ? 1 : -1);
            modelrenderer.x += f;
            modelrenderer.translateAndRotate(matrixStack);
            modelrenderer.x -= f;
        }
        else
        {
            modelrenderer.translateAndRotate(matrixStack);
        }
    }
}
