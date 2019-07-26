package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.resolver.IRequestResolver;
import com.minecolonies.api.util.constant.TypeConstants;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.client.gui.WindowHutWorkerPlaceholder;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuildingWorker;
import com.minecolonies.coremod.colony.jobs.AbstractJob;
import com.minecolonies.coremod.colony.jobs.JobDeliveryman;
import com.minecolonies.coremod.colony.requestsystem.resolvers.DeliveryRequestResolver;
import com.minecolonies.coremod.entity.EntityCitizen;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.minecolonies.api.util.constant.BuildingConstants.CONST_DEFAULT_MAX_BUILDING_LEVEL;
import static com.minecolonies.api.util.constant.CitizenConstants.BASE_MOVEMENT_SPEED;

/**
 * Class of the warehouse building.
 */
public class BuildingDeliveryman extends AbstractBuildingWorker
{

    private static final String DELIVERYMAN = "Deliveryman";

    /**
     * Building the deliveryman will deliver somethingTo
     */
    private ILocation buildingToDeliver;

    /**
     * Instantiates a new warehouse building.
     *
     * @param c the colony.
     * @param l the location
     */
    public BuildingDeliveryman(final Colony c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Get the building the deliveryman should deliver to.
     *
     * @return the building.
     */
    public ILocation getBuildingToDeliver()
    {
        return this.buildingToDeliver;
    }

    /**
     * Set the building the deliveryman should deliver to.
     *
     * @param building building to deliver to.
     */
    public void setBuildingToDeliver(final ILocation building)
    {
        this.buildingToDeliver = building;
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return DELIVERYMAN;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return CONST_DEFAULT_MAX_BUILDING_LEVEL;
    }

    @Override
    public ImmutableCollection<IRequestResolver<?>> createResolvers()
    {
        final ImmutableCollection<IRequestResolver<?>> supers = super.createResolvers();
        final ImmutableList.Builder<IRequestResolver<?>> builder = ImmutableList.builder();

        builder.addAll(supers);
        builder.add(new DeliveryRequestResolver(getRequester().getRequesterLocation(),
                                                 getColony().getRequestManager().getFactoryController().getNewInstance(TypeConstants.ITOKEN)));

        return builder.build();
    }

    @NotNull
    @Override
    public AbstractJob createJob(final CitizenData citizen)
    {
        return new JobDeliveryman(citizen);
    }

    @Override
    public void readFromNBT(@NotNull final CompoundNBT compound)
    {
        super.readFromNBT(compound);
    }

    @Override
    public void write(@NotNull final CompoundNBT compound)
    {
        super.write(compound);
    }

    @NotNull
    @Override
    public String getJobName()
    {
        return DELIVERYMAN;
    }

    @Override
    public void serializeToView(@NotNull final ByteBuf buf)
    {
        super.serializeToView(buf);
    }

    @Override
    public void removeCitizen(final CitizenData citizen)
    {
        if (citizen != null)
        {
            final Optional<EntityCitizen> optCitizen = citizen.getCitizenEntity();
            optCitizen.ifPresent(entityCitizen -> entityCitizen.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                                                    .setBaseValue(BASE_MOVEMENT_SPEED));
        }
        super.removeCitizen(citizen);
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
            return new WindowHutWorkerPlaceholder<>(this, DELIVERYMAN);
        }

        @Override
        public void deserialize(@NotNull final ByteBuf buf)
        {
            super.deserialize(buf);
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
