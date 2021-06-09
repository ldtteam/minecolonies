package com.minecolonies.coremod.entity.ai.util;

import com.ldtteam.structures.blueprints.v1.Blueprint;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingStructureBuilder;
import com.minecolonies.coremod.colony.jobs.AbstractJobStructure;
import com.minecolonies.coremod.entity.ai.basic.AbstractEntityAIStructure;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Load only structure handler just to get dimensions etc from structures, not for placement specific for worker usage.
 */
public final class WorkerLoadOnlyStructureHandler<J extends AbstractJobStructure<?, J>, B extends AbstractBuildingStructureBuilder> extends LoadOnlyStructureHandler
{
    /**
     * The structure AI handling this task.
     */
    private final AbstractEntityAIStructure<J, B> structureAI;

    /**
     * The minecolonies specific worker load only structure placer.
     *
     * @param world          the world.
     * @param pos            the pos it is placed at.
     * @param blueprint      the blueprint.
     * @param settings       the placement settings.
     * @param fancyPlacement if fancy or complete.
     */
    public WorkerLoadOnlyStructureHandler(final World world, final BlockPos pos, final Blueprint blueprint, final PlacementSettings settings, final boolean fancyPlacement,
      final AbstractEntityAIStructure<J, B> entityAIStructure)
    {
        super(world, pos, blueprint, settings, fancyPlacement);
        this.structureAI = entityAIStructure;
    }

    @Override
    public BlockState getSolidBlockForPos(final BlockPos worldPos)
    {
        return structureAI.getSolidSubstitution(worldPos);
    }
}
