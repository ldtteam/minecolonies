package com.minecolonies.core.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Barbarian entity.
 */
public class EntityBarbarian extends AbstractEntityBarbarian implements IMeleeBarbarianEntity
{

    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityBarbarian(final EntityType<? extends EntityBarbarian> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
