package com.minecolonies.core.entity.mobs.raider.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemenRaider;
import com.minecolonies.api.entity.mobs.vikings.IArcherNorsemenEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import static com.minecolonies.core.entity.mobs.raider.norsemen.EntityNorsemenChiefRaider.NORSE_HEALTH_REDUCTION_FACTOR;

/**
 * Class for the Archer norsemen entity.
 */
public class EntityNorsemenArcherRaider extends AbstractEntityNorsemenRaider implements IArcherNorsemenEntity
{

    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityNorsemenArcherRaider(final EntityType<? extends EntityNorsemenArcherRaider> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        super.initStatsFor(baseHealth / NORSE_HEALTH_REDUCTION_FACTOR, difficulty, baseDamage);
    }
}
