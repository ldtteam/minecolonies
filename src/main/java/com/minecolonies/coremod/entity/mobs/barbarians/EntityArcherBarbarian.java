package com.minecolonies.coremod.entity.mobs.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.barbarians.IArcherBarbarianEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

/**
 * Class for the Archer Barbarian entity.
 */
public class EntityArcherBarbarian extends AbstractEntityBarbarian implements IArcherBarbarianEntity
{
    /**
     * Constructor of the entity.
     * 
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherBarbarian(final EntityType<? extends MobEntity> type, final World worldIn)
    {
        super(type, worldIn);
    }
}
