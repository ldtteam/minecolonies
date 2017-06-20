package com.minecolonies.coremod.util;

import com.minecolonies.api.util.CompatibilityUtils;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.entity.ai.mobs.EntityArcherBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.EntityChiefBarbarian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Utils for the Barbarians
 */
public class BarbarianUtils
{

    /**
     * Takes a colony and spits out that colony's RaidLevel
     *
     * @param colony The colony to use
     * @return an int describing the raid level
     */
    public static int getColonyRaidLevel(final Colony colony)
    {
        int levels = 0;

        @NotNull final List<CitizenData> citizensList = new ArrayList<>();
        citizensList.addAll(colony.getCitizens().values());

        for (@NotNull final CitizenData citizen : citizensList)
        {
            if (citizen.getJob() != null && citizen.getWorkBuilding() != null)
            {
                final int buildingLevel = citizen.getWorkBuilding().getBuildingLevel();
                levels += buildingLevel;
            }
        }

        if (colony.getTownHall() != null)
        {
            return (levels + colony.getTownHall().getBuildingLevel());
        }
        else
        {
            return 0;
        }
    }

    /**
     * Simple method that returns whether or not an entity is a barbarian
     *
     * @param entity The entity to check
     * @return Boolean value of whether the entity is a barbarian
     */
    public static Boolean isBarbarian(final Entity entity)
    {
        return (entity instanceof EntityBarbarian || entity instanceof EntityArcherBarbarian || entity instanceof EntityChiefBarbarian);
    }

    /**
     * Returns the closest barbarian to an entity
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarian (if any) that is nearest
     */
    public static Entity getClosestBarbarianToEntity(final Entity entity, final double distanceFromEntity)
    {
        final List<Entity> entityList = CompatibilityUtils.getWorld(entity).getEntitiesInAABBexcluding(
          entity,
          entity.getEntityBoundingBox().expand(
            distanceFromEntity,
            3.0D,
            distanceFromEntity),
          Entity::isEntityAlive);

        final Optional<Entity> entityBarbarian = entityList.stream()
                                                   .filter(BarbarianUtils::isBarbarian)
                                                   .findFirst();

        return entityBarbarian.orElse(null);
    }

    /**
     * Returns the barbarians close to an entity
     *
     * @param entity             The entity to test against
     * @param distanceFromEntity The distance to check for
     * @return the barbarians (if any) that is nearest
     */
    public static Stream<EntityLivingBase> getBarbariansCloseToEntity(final Entity entity, final double distanceFromEntity)
    {
        final List<EntityLivingBase> entityList = CompatibilityUtils.getWorld(entity).getEntitiesWithinAABB(
          EntityLivingBase.class,
          entity.getEntityBoundingBox().expand(
            distanceFromEntity,
            3.0D,
            distanceFromEntity),
          Entity::isEntityAlive);

        return entityList.stream().filter(BarbarianUtils::isBarbarian);
    }
}
