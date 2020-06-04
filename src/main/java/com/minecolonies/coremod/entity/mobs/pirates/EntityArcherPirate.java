package com.minecolonies.coremod.entity.mobs.pirates;

import com.minecolonies.api.entity.mobs.pirates.AbstractEntityPirate;
import com.minecolonies.api.entity.mobs.pirates.IArcherPirateEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

/**
 * Class for the Archer Pirate entity.
 */
public class EntityArcherPirate extends AbstractEntityPirate implements IArcherPirateEntity
{
    /**
     * Constructor of the entity.
     * 
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherPirate(final EntityType<? extends MobEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }
}
