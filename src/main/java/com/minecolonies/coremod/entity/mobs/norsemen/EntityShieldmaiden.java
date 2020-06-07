package com.minecolonies.coremod.entity.mobs.norsemen;

import com.minecolonies.api.entity.mobs.vikings.AbstractEntityNorsemen;
import com.minecolonies.api.entity.mobs.vikings.IMeleeNorsemenEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Norsemen Shieldmaiden entity.
 */
public class EntityShieldmaiden extends AbstractEntityNorsemen implements IMeleeNorsemenEntity
{

    /**
     * Constructor of the entity.
     * @param worldIn world to construct it in.
     * @param type the entity type.
     */
    public EntityShieldmaiden(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
