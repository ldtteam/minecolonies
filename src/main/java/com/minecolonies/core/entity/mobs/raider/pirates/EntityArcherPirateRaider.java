package com.minecolonies.core.entity.mobs.raider.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirateRaider;
import com.minecolonies.api.entity.mobs.pirates.IArcherPirateEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer Pirate entity.
 */
public class EntityArcherPirateRaider extends AbstractEntityPirateRaider implements IArcherPirateEntity
{
    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherPirateRaider(final EntityType<? extends EntityArcherPirateRaider> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
