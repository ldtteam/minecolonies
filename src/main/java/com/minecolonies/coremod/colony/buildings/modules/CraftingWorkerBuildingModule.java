package com.minecolonies.coremod.colony.buildings.modules;

import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.IBuildingWorkerModule;
import com.minecolonies.api.colony.buildings.modules.IBuildingEventsModule;
import com.minecolonies.api.colony.buildings.modules.ICreatesResolversModule;
import com.minecolonies.api.colony.buildings.modules.IPersistentModule;
import com.minecolonies.api.colony.buildings.modules.ITickingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;

import java.util.function.Function;

/**
 * Assignment module for crafting workers.
 */
public class CraftingWorkerBuildingModule extends WorkerBuildingModule implements IBuildingEventsModule, ITickingModule, IPersistentModule, IBuildingWorkerModule, ICreatesResolversModule
{
    /**
     * Skill influencing crafting behaviour.
     */
    private final Skill craftingSpeedSkill;
    private final Skill recipeImprovementSkill;

    public CraftingWorkerBuildingModule(final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit,
      final Skill craftingSpeedSkill,
      final Skill recipeImprovementSkill)
    {
        super(entry, primary, secondary, canWorkingDuringRain, sizeLimit);
        this.craftingSpeedSkill = craftingSpeedSkill;
        this.recipeImprovementSkill = recipeImprovementSkill;
    }

    public CraftingWorkerBuildingModule(final JobEntry entry,
      final Skill primary,
      final Skill secondary,
      final boolean canWorkingDuringRain,
      final Function<IBuilding, Integer> sizeLimit)
    {
        super(entry, primary, secondary, canWorkingDuringRain, sizeLimit);
        this.craftingSpeedSkill = primary;
        this.recipeImprovementSkill = secondary;
    }

    /**
     * Get the skill that improves the crafting speed.
     * @return the speed.
     */
    public Skill getCraftSpeedSkill()
    {
        return craftingSpeedSkill;
    }

    /**
     * Skill responsible for recipe improvement.
     * @return the skill.
     */
    public Skill getRecipeImprovementSkill()
    {
        return recipeImprovementSkill;
    }
}
