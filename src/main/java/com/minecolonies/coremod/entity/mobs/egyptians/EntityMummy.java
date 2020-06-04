package com.minecolonies.coremod.entity.mobs.egyptians;

import com.minecolonies.api.entity.mobs.egyptians.AbstractEntityEgyptian;
import com.minecolonies.api.entity.mobs.egyptians.IMeleeMummyEntity;
import com.minecolonies.coremod.entity.citizen.citizenhandlers.MovementHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.world.World;

/**
 * Class for the Mummy entity.
 */
public class EntityMummy extends AbstractEntityEgyptian implements IMeleeMummyEntity
{
    /**
     * Constructor of the entity.
     * 
     * @param type    the entity type.
     * @param worldIn world to construct it in.
     */
    public EntityMummy(final EntityType<? extends MobEntity> type, final World worldIn)
    {
        super(type, worldIn);
        this.moveController = new MovementHandler(this);
    }
}
