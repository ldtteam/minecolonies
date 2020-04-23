package com.minecolonies.coremod.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.ICaptainPirateEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Chief Pirate entity.
 */
public class EntityCaptainPirate extends AbstractEntityPirate implements ICaptainPirateEntity
{

    /**
     * Constructor of the entity.
     * @param type the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityCaptainPirate(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
    }
}
