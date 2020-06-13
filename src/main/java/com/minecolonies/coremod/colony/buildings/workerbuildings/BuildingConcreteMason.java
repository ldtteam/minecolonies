package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.jobs.IJob;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingCrafter;
import com.minecolonies.coremod.research.UnlockBuildingResearchEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;

/**
 * Class of the concrete mason building.
 */
public class BuildingConcreteMason extends AbstractBuildingCrafter
{
    /**
     * Description string of the building.
     */
    private static final String CONCRETE_MASON = "mechanic";

    /**
     * Instantiates a new concrete mason building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingConcreteMason(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return CONCRETE_MASON;
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
        return new JobConcreteMason(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return CONCRETE_MASON;
    }

    @NotNull
    @Override
    public Skill getPrimarySkill()
    {
        return Skill.Stamina;
    }

    @NotNull
    @Override
    public Skill getSecondarySkill()
    {
        return Skill.Dexterity;
    }

    @Override
    public boolean canRecipeBeAdded(final IToken<?> token)
    {
        //todo
        return true;
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.concreteMason;
    }

    @Override
    public void requestUpgrade(final PlayerEntity player, final BlockPos builder)
    {
        //todo add research
        final UnlockBuildingResearchEffect effect = colony.getResearchManager().getResearchEffects().getEffect("Concrete Mason", UnlockBuildingResearchEffect.class);
        if (effect == null)
        {
            player.sendMessage(new TranslationTextComponent("com.minecolonies.coremod.research.havetounlock"));
            return;
        }
        super.requestUpgrade(player, builder);
    }

    /**
     * Concrete Mason View.
     */
    public static class View extends AbstractBuildingCrafter.View
    {

        /**
         * Instantiate the Concrete Mason view.
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
            return new WindowHutWorkerPlaceholder<>(this, CONCRETE_MASON);
        }
    }
}
