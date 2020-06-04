package com.minecolonies.coremod.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IMeleeBarbarianEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

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
    public EntityBarbarian(final EntityType<? extends MobEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }
}
