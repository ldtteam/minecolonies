package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.workerbuildings.IBuildingPublicCrafter;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.crafting.IGenericRecipe;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.CraftingUtils;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.colony.jobs.JobStonemason;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the stonemason building.
 */
public class BuildingStonemason extends AbstractBuilding implements IBuildingPublicCrafter
{
    /**
     * Description string of the building.
     */
    private static final String STONEMASON = "stonemason";

    /**
     * Instantiates a new stonemason building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingStonemason(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return STONEMASON;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @NotNull
    @Override
    public IJob<?> createJob(final ICitizenData citizen)
    {
        return new JobStonemason(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return STONEMASON;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Creativity;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
    }

    /**
     * Crafter building View.
     */
    public static class View extends AbstractBuildingView
    {

        /**
         * Instantiate the stonemason view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, STONEMASON);
        }
    }

    public static class CraftingModule extends AbstractCraftingBuildingModule.Crafting
    {
        @Nullable
        @Override
        public IJob<?> getCraftingJob()
        {
            return getMainBuildingJob().orElseGet(() -> new JobStonemason(null));
        }

        @Override
        public boolean isRecipeCompatible(@NotNull final IGenericRecipe recipe)
        {
            if (!super.isRecipeCompatible(recipe)) return false;
            return CraftingUtils.isRecipeCompatibleBasedOnTags(recipe, STONEMASON).orElse(false);
        }
    }
}
