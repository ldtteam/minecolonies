package com.minecolonies.core.entity.mobs.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.egyptians.IArcherMummyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer mummy entity.
 */
public class EntityArcherMummy extends AbstractEntityEgyptian implements IArcherMummyEntity
{
    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherMummy(final EntityType<? extends EntityArcherMummy> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
