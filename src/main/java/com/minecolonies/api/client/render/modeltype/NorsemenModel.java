package com.minecolonies.api.client.render.modeltype;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemenRaider;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

/**
 * Norsemen model.
 */
public class NorsemenModel extends HumanoidModel<AbstractEntityMinecoloniesMob>
{
    /**
     * Create a model of a specific size.
     *
     */
    public NorsemenModel(final ModelPart part)
    {
        super(part);
    }
}
