package com.minecolonies.coremod.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.IMeleePirateEntity;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.MovementHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;

/**
 * Class for the Pirate entity.
 */
public class EntityPirate extends AbstractEntityPirate implements IMeleePirateEntity
{

    /**
     * Constructor of the entity.
     * @param type the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityPirate(final EntityType type, final World worldIn)
    {
        super(type, worldIn);
        this.moveController = new MovementHandler(this);
    }
}
