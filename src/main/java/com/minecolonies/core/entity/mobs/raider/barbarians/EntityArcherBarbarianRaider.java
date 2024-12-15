package com.minecolonies.core.entity.mobs.raider.barbarians;

import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarianRaider;
import com.minecolonies.api.entity.mobs.barbarians.IArcherBarbarianEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer Barbarian entity.
 */
public class EntityArcherBarbarianRaider extends AbstractEntityBarbarianRaider implements IArcherBarbarianEntity
{

    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherBarbarianRaider(final EntityType<? extends EntityArcherBarbarianRaider> type, final Level worldIn)
    {
        super(type, worldIn);
    }
}
