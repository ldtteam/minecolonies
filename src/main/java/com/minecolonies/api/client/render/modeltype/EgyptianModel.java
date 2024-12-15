package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptianRaider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Egyptian model.
 */
public class EgyptianModel<T extends AbstractEntityMinecoloniesMob> extends HumanoidModel<AbstractEntityMinecoloniesMob>
{
    /**
     * Create a model of a specific size.
     *
     */
    public EgyptianModel(final ModelPart part)
    {
        super(part);
    }
}
