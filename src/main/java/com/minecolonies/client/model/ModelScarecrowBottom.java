package com.minecolonies.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelScarecrowBottom extends ModelBase
{
    //fields
    private ModelRenderer Head;
    private ModelRenderer LeftArmPeg;
    private ModelRenderer RightArmPeg;
    private ModelRenderer Torso;
    private ModelRenderer LeftArm;
    private ModelRenderer RightArm;

    public ModelScarecrowBottom()
    {
        textureWidth = 128;
        textureHeight = 64;

        Head = new ModelRenderer(this, 0, 0);
        Head.addBox(-8.2F, -35.6F, -4.2F, 8, 8, 8);
        Head.setRotationPoint(7F, 40F, -1F);
        Head.setTextureSize(128, 64);
        Head.mirror = true;
        setRotation(Head, 0F, 0.1858931F, -0.1092638F);

        LeftArmPeg = new ModelRenderer(this, 9, 33);
        LeftArmPeg.addBox(23.5F, 1F, -1F, 2, 2, 2);
        LeftArmPeg.setRotationPoint(7F, 40F, 0F);
        LeftArmPeg.setTextureSize(128, 64);
        LeftArmPeg.mirror = true;
        setRotation(LeftArmPeg, 0F, 0F, -1.351339F);

        RightArmPeg = new ModelRenderer(this, 9, 33);
        RightArmPeg.addBox(-28F, 15.8F, -1F, 2, 2, 2);
        RightArmPeg.setRotationPoint(7F, 40F, 0F);
        RightArmPeg.setTextureSize(128, 64);
        RightArmPeg.mirror = true;
        setRotation(RightArmPeg, 0F, 0F, 1.351339F);

        Torso = new ModelRenderer(this, 16, 16);
        Torso.addBox(-10.3F, -27.6F, -2F, 8, 12, 4);
        Torso.setRotationPoint(7F, 40F, 0F);
        Torso.setTextureSize(128, 64);
        Torso.mirror = true;
        setRotation(Torso, 0F, 0F, -0.0349066F);

        LeftArm = new ModelRenderer(this, 40, 16);
        LeftArm.addBox(22.5F, -10F, -1.99F, 4, 12, 4);
        LeftArm.setRotationPoint(7F, 40F, 0F);
        LeftArm.setTextureSize(128, 64);
        LeftArm.mirror = true;
        setRotation(LeftArm, 0F, 0F, -1.351339F);

        RightArm = new ModelRenderer(this, 40, 16);
        RightArm.addBox(-29F, 4.8F, -1.99F, 4, 12, 4);
        RightArm.setRotationPoint(7F, 40F, 0F);
        RightArm.setTextureSize(128, 64);
        RightArm.mirror = true;
        setRotation(RightArm, 0F, 0F, 1.351339F);
    }

    private void setRotation(@NotNull ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        Head.render(f5);
        LeftArmPeg.render(f5);
        RightArmPeg.render(f5);
        Torso.render(f5);
        LeftArm.render(f5);
        RightArm.render(f5);
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }
}
