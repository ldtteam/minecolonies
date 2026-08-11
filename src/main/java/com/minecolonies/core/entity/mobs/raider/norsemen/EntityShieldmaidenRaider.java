package com.minecolonies.core.entity.mobs.raider.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemenRaider;
import com.minecolonies.api.entity.mobs.vikings.IMeleeNorsemenEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import static com.minecolonies.core.entity.mobs.raider.norsemen.EntityNorsemenChiefRaider.BASE_NORSE_ARMOR;
import static com.minecolonies.core.entity.mobs.raider.norsemen.EntityNorsemenChiefRaider.NORSE_HEALTH_REDUCTION_FACTOR;

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

    @Override
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        super.initStatsFor(baseHealth / NORSE_HEALTH_REDUCTION_FACTOR, difficulty, baseDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(BASE_NORSE_ARMOR);
    }
}
