package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Egyptian model.
 */
public class EgyptianModel<T extends AbstractEntityMinecoloniesMonster> extends HumanoidModel<AbstractEntityMinecoloniesMonster>
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
