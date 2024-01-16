package com.minecolonies.core.entity.mobs.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.api.entity.mobs.vikings.IArcherNorsemenEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer norsemen entity.
 */
public class EntityNorsemenArcher extends AbstractEntityNorsemen implements IArcherNorsemenEntity
{

    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityNorsemenArcher(final EntityType<? extends EntityNorsemenArcher> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
