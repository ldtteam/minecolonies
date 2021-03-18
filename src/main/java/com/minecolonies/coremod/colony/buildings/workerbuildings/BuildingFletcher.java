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
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.WindowHutCrafterModule;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.colony.jobs.JobFletcher;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArrowItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the fletcher building.
 */
public class BuildingFletcher extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String FLETCHER = "fletcher";

    /**
     * Instantiates a new fletcher building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingFletcher(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return FLETCHER;
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
        return new JobFletcher(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return FLETCHER;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Dexterity;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Creativity;
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

        isRecipeAllowed = super.canRecipeBeAddedBasedOnTags(token);
        if (isRecipeAllowed.isPresent())
        {
            return isRecipeAllowed.get();
        }
        else
        {
            // Additional recipe rules
            final IRecipeStorage storage = IColonyManager.getInstance().getRecipeManager().getRecipes().get(token);

            return storage.getPrimaryOutput().getItem() instanceof ArrowItem
                     || (storage.getPrimaryOutput().getItem() instanceof DyeableArmorItem
                           && ((DyeableArmorItem) storage.getPrimaryOutput().getItem()).getArmorMaterial() == ArmorMaterial.LEATHER);

            // End Additional recipe rules
        }
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.fletcher;
    }

    /**
     * Fletcher View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the fletcher view.
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
            return new WindowHutCrafterModule(this, FLETCHER);
        }
    }
}
