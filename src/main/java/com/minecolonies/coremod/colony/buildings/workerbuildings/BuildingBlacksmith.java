package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.compatibility.Compatibility;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobBlacksmith;
import net.minecraft.item.*;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Creates a new building for the blacksmith.
 */
public class BuildingBlacksmith extends AbstractBuildingCrafter
{
    /**
     * Description of the job executed in the hut.
     */
    private static final String BLACKSMITH = "blacksmith";

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingBlacksmith(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return BLACKSMITH;
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
        return new JobBlacksmith(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return BLACKSMITH;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Strength;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Focus;
    }

    @Override
    @NotNull
    public Skill getCraftSpeedSkill()
    {
        return getPrimarySkill();
    }

    @Override
    @NotNull
    public Skill getRecipeImprovementSkill()
    {
        return getSecondarySkill();
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {

        Optional<Boolean> isRecipeAllowed;

        if (!super.canRecipeBeAdded(token))
        {
            return false;
        }

        // Additional recipe rules

        final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

        final ItemStack output = storage.getPrimaryOutput();

        boolean matchOverride;
        matchOverride= output.getItem() instanceof ToolItem ||
                    output.getItem() instanceof SwordItem ||
                    output.getItem() instanceof ArmorItem ||
                    output.getItem() instanceof HoeItem ||
                    output.getItem() instanceof ShieldItem ||
                    Compatibility.isTinkersWeapon(output);

        // End Additional recipe rules

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return matchOverride || isRecipeAllowed.get();
        }
        else
        {
            return matchOverride;
        }
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.blacksmith;
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingCrafter.View
    {
        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        public Window getWindow()
        {
            return new WindowHutWorkerModulePlaceholder<>(this, BLACKSMITH);
        }
    }
}
