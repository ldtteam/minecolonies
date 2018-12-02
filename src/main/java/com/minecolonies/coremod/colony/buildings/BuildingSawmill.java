package com.minecolonies.coremod.colony.buildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.constant.TypeConstants;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.jobs.JobSawmill;
import com.minecolonies.coremod.colony.requestsystem.resolvers.PublicWorkerCraftingRequestResolver;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

/**
 * Class of the sawmill building.
 */
public class BuildingSawmill extends AbstractBuildingWorker
{

    private static final String SAWMILL = "Sawmill";

    /**
     * Instantiates a new sawmill building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingSawmill(final Colony c, final BlockPos l)
    {
        super(c, l);
    }


    @NotNull
    @Override
    public String getSchematicName()
    {
        return SAWMILL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> getResolvers()
    {
        final ImmutableCollection<IRequestResolver<?>> supers = super.getResolvers();
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new PublicWorkerCraftingRequestResolver(getRequester().getRequesterLocation(),
                                                 getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobSawmill(citizen);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return SAWMILL;
    }

    @Override
    public boolean canCraftComplexRecipes()
    {
        return true;
    }

    /**
     * BuildingDeliveryman View.
     */
    public static class View extends AbstractBuildingWorker.View
    {

        /**
         * Instantiate the deliveryman view.
         *
         * @param c the colonyview to put it in
         * @param l the positon
         */
        public View(final ColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowHutWorkerPlaceholder<>(this, SAWMILL);
        }

        @NotNull
        @Override
        public Skill getPrimarySkill()
        {
            return Skill.INTELLIGENCE;
        }

        @NotNull
        @Override
        public Skill getSecondarySkill()
        {
            return Skill.ENDURANCE;
        }
    }
}
