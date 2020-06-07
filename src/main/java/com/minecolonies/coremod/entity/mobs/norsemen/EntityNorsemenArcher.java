package com.minecolonies.coremod.entity.mobs.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.api.entity.mobs.vikings.IArcherNorsemenEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Archer norsemen entity.
 */
public class EntityNorsemenArcher extends AbstractEntityNorsemen implements IArcherNorsemenEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     * @param type the entity type.
     */
    public EntityNorsemenArcher(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
