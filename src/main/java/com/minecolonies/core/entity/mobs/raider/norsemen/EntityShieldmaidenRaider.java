package com.minecolonies.core.entity.mobs.raider.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemenRaider;
import com.minecolonies.api.entity.mobs.vikings.IMeleeNorsemenEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Norsemen Shieldmaiden entity.
 */
public class EntityShieldmaidenRaider extends AbstractEntityNorsemenRaider implements IMeleeNorsemenEntity
{

    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityShieldmaidenRaider(final EntityType<? extends EntityShieldmaidenRaider> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
