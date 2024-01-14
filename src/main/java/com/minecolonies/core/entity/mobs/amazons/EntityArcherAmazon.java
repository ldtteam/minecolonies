package com.minecolonies.core.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.amazons.IArcherAmazon;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Archer amazon entity.
 */
public class EntityArcherAmazon extends AbstractEntityAmazon implements IArcherAmazon
{
    /**
     * Constructor of the entity.
     *
     * @param worldIn world to construct it in.
     * @param type    the entity type.
     */
    public EntityArcherAmazon(final EntityType<? extends EntityArcherAmazon> type, final Level worldIn)
    {
        super(type, worldIn);
    }

    @Override
    public double getAttackDelayModifier()
    {
        return 2;
    }
}
