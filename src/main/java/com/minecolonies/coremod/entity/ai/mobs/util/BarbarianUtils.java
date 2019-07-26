package com.minecolonies.coremod.entity.ai.mobs.util;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.entity.ai.mobs.AbstractEntityMinecoloniesMob;
import com.minecolonies.coremod.entity.ai.mobs.IBaseMinecoloniesMob;
import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public static IBaseMinecoloniesMob getClosestRaiderToEntity(final Entity entity, final double distanceFromEntity)
    {
        final Optional<IBaseMinecoloniesMob> barbarian = getBarbariansCloseToEntity(entity, distanceFromEntity).stream().findFirst();
        return barbarian.orElse(null);
    }

    /**
     * Returns the barbarians close to an entity.
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static List<IBaseMinecoloniesMob> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        return CompatibilityUtils.getWorldFromEntity(entity).getEntitiesWithinAABB(
          AbstractEntityMinecoloniesMob.class,
          entity.getEntityBoundingBox().expand(
            distanceFromEntity,
            Y_DISTANCE_TO_CHECK_WITHIN,
            distanceFromEntity),
          Entity::isEntityAlive)
                 .stream()
                 .map(abstractEntityMinecoloniesMob -> (IBaseMinecoloniesMob) abstractEntityMinecoloniesMob)
                 .collect(Collectors.toList());
    }
}
