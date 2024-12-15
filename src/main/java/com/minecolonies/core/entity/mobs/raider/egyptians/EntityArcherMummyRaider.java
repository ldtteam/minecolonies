package com.minecolonies.core.entity.mobs.raider.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptianRaider;
import com.minecolonies.api.entity.mobs.egyptians.IArcherMummyEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer mummy entity.
 */
public class EntityArcherMummyRaider extends AbstractEntityEgyptianRaider implements IArcherMummyEntity
{
    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherMummyRaider(final EntityType<? extends EntityArcherMummyRaider> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
