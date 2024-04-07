package com.minecolonies.core.entity.mobs.drownedpirates;

import com.minecolonies.api.entity.mobs.drownedpirate.AbstractDrownedEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.IArcherPirateEntity;
import net.minecraft.world.entity.EntityType;
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
}
