package com.minecolonies.coremod.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelScarecrowBottom extends EntityModel
{
    //fields
    private final ModelRenderer Head;
    private final ModelRenderer LeftArmPeg;
    private final ModelRenderer RightArmPeg;
    private final ModelRenderer Torso;
    private final ModelRenderer LeftArm;
    private final ModelRenderer RightArm;

    public ModelScarecrowBottom()
    {
        textureWidth = 128;
        textureHeight = 64;

        Head = new ModelRenderer(this, 0, 0);
        Head.addCuboid(-8.2F, -35.6F, -4.2F, 8, 8, 8);
        Head.setRotationPoint(7F, 40F, -1F);
        Head.setTextureSize(128, 64);
        Head.mirror = true;
        setRotation(Head, 0F, 0.1858931F, -0.1092638F);

        LeftArmPeg = new ModelRenderer(this, 9, 33);
        LeftArmPeg.addCuboid(23.5F, 1F, -1F, 2, 2, 2);
        LeftArmPeg.setRotationPoint(7F, 40F, 0F);
        LeftArmPeg.setTextureSize(128, 64);
        LeftArmPeg.mirror = true;
        setRotation(LeftArmPeg, 0F, 0F, -1.351339F);

        RightArmPeg = new ModelRenderer(this, 9, 33);
        RightArmPeg.addCuboid(-28F, 15.8F, -1F, 2, 2, 2);
        RightArmPeg.setRotationPoint(7F, 40F, 0F);
        RightArmPeg.setTextureSize(128, 64);
        RightArmPeg.mirror = true;
        setRotation(RightArmPeg, 0F, 0F, 1.351339F);

        Torso = new ModelRenderer(this, 16, 16);
        Torso.addCuboid(-10.3F, -27.6F, -2F, 8, 12, 4);
        Torso.setRotationPoint(7F, 40F, 0F);
        Torso.setTextureSize(128, 64);
        Torso.mirror = true;
        setRotation(Torso, 0F, 0F, -0.0349066F);

        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.addCuboid(22.5F, -10F, -1.99F, 4, 12, 4);
        LeftArm.setRotationPoint(7F, 40F, 0F);
        LeftArm.setTextureSize(128, 64);
        LeftArm.mirror = true;
        setRotation(LeftArm, 0F, 0F, -1.351339F);

        RightArm = new ModelRenderer(this, 40, 16);
        RightArm.addCuboid(-29F, 4.8F, -1.99F, 4, 12, 4);
        RightArm.setRotationPoint(7F, 40F, 0F);
        RightArm.setTextureSize(128, 64);
        RightArm.mirror = true;
        setRotation(RightArm, 0F, 0F, 1.351339F);
    }

    @Override
    public void setAngles(final Entity entity, final float v, final float v1, final float v2, final float v3, final float v4)
    {

    }

    private void setRotation(@NotNull final ModelRenderer model, final float x, final float y, final float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    @Override
    public void render(final MatrixStack matrixStack, final IVertexBuilder iVertexBuilder, final int i, final int i1, final float v, final float v1, final float v2, final float v3)
    {
        Head.render(matrixStack, iVertexBuilder, i, i1, v, v1, v2, v3);
        LeftArmPeg.render(matrixStack, iVertexBuilder, i, i1, v, v1, v2, v3);
        RightArmPeg.render(matrixStack, iVertexBuilder, i, i1, v, v1, v2, v3);
        Torso.render(matrixStack, iVertexBuilder, i, i1, v, v1, v2, v3);
        LeftArm.render(matrixStack, iVertexBuilder, i, i1, v, v1, v2, v3);
        RightArm.render(matrixStack, iVertexBuilder, i, i1, v, v1, v2, v3);
    }
}
