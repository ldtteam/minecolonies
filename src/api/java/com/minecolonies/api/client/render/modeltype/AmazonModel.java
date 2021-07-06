package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import net.minecraft.client.renderer.entity.model.BipedModel;
import org.jetbrains.annotations.NotNull;

/**
 * Amazon model.
 */
public class AmazonModel<T extends AbstractEntityAmazon> extends BipedModel<AbstractEntityAmazon>
{
    /**
     * Create a model of a specific size.
     *
     * @param size the size.
     */
    public AmazonModel(final float size)
    {
        super(size);
    }

    /**
     * Create a model of the default size.
     */
    public AmazonModel()
    {
        this(1.0F);
    }

    @Override
    public void setupAnim(@NotNull final AbstractEntityAmazon entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
    {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        head.y -= 3;
        rightLeg.y -= 3.5;
        leftLeg.y -= 3.5;
        rightArm.y -= 2;
        leftArm.y -= 2;
    }
}
