package com.minecolonies.core.entity.mobs.amazons;

import com.minecolonies.api.entity.mobs.amazons.AbstractEntityAmazon;
import com.minecolonies.api.entity.mobs.amazons.IAmazonSpearman;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

/**
 * Class for the Amazon Spearman entity.
 */
public class EntityAmazonSpearman extends AbstractEntityAmazon implements IAmazonSpearman
{
    /**
     * Constructor of the entity.
     *
     * @param type  the entity type
     * @param world the world to construct it in
     */
    public EntityAmazonSpearman(final EntityType<? extends AbstractEntityAmazon> type, final Level world)
    {
        super(type, world);
    }
}
