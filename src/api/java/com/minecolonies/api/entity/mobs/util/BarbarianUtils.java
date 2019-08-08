package com.minecolonies.api.entity.mobs.util;

import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.api.util.CompatibilityUtils;
import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A few utils used for barbarians.
 */
public final class BarbarianUtils
{

    private static final double Y_DISTANCE_TO_CHECK_WITHIN = 3.0D;

    /**
     * Private constructor to hide the implicit public one.
     */
    private BarbarianUtils()
    {
    }

    /**
     * Returns the closest barbarian to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarian (if any) that is nearest
     */
    public static AbstractEntityMinecoloniesMob getClosestRaiderToEntity(final Entity entity, final double distanceFromEntity)
    {
        final Optional<AbstractEntityMinecoloniesMob> barbarian = getBarbariansCloseToEntity(entity, distanceFromEntity).stream().findFirst();
        return barbarian.orElse(null);
    }

    /**
     * Returns the barbarians close to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static List<AbstractEntityMinecoloniesMob> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return new ArrayList<>(CompatibilityUtils.getWorldFromEntity(entity).getEntitiesWithinAABB(
          AbstractEntityMinecoloniesMob.class,
          entity.getEntityBoundingBox().expand(
            distanceFromEntity,
            Y_DISTANCE_TO_CHECK_WITHIN,
            distanceFromEntity),
          Entity::isAlive));
    }
}
