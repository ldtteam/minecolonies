package com.minecolonies.core.entity.mobs.drownedpirates;

import com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.IArcherPirateEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer drowned Pirate entity.
 */
public class EntityDrownedArcherPirate extends AbstractDrownedEntityPirate implements IArcherPirateEntity
{
    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityDrownedArcherPirate(final EntityType<? extends EntityDrownedArcherPirate> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public boolean penetrateFluids()
    {
        return true;
    }

    @Override
    public void initStatsFor(final double baseHealth, final double difficulty, final double baseDamage)
    {
        super.initStatsFor(baseHealth, difficulty, baseDamage);
        this.getAttribute(Attributes.ARMOR).setBaseValue(0.25);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(baseHealth * 1.5);
        this.setHealth(this.getMaxHealth());
    }
}
