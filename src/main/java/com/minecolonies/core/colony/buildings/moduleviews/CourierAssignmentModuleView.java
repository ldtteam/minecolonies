package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IAssignmentModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.SpecialAssignmentModuleWindow;
import com.minecolonies.core.network.messages.server.colony.building.CourierHiringModeMessage;
import com.minecolonies.core.network.messages.server.colony.building.HireFireMessage;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Module view for courier assignment.
 */
public class CourierAssignmentModuleView extends AbstractBuildingModuleView implements IAssignmentModuleView
{
    /**
     * List of the worker ids.
     */
    private final Set<Integer> workerIDs = new HashSet<>();

    /**
     * The hiring mode of the building.
     */
    private HiringMode hiringMode;

    /**
     * Max number of miners.
     */
    private int maxSize;

    @Override
    public List<Integer> getAssignedCitizens()
    {
        return new ArrayList<>(workerIDs);
    }

    @Override
    public void addCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.add(citizen.getId());
        Network.getNetwork().sendToServer(new HireFireMessage(buildingView, true, citizen.getId(), getProducer().getRuntimeID()));
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        final int size = buf.readInt();
        workerIDs.clear();
        for (int i = 0; i < size; i++)
        {
            workerIDs.add(buf.readInt());
        }

        this.hiringMode = HiringMode.values()[buf.readInt()];
        this.maxSize = buf.readInt();
    }

    @Override
    public String getIcon()
    {
        return "entity";
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.warehouse.couriers";
    }

    @Override
    public boolean isPageVisible()
    {
        return true;
    }

    @Override
    public void removeCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.remove(citizen.getId());
        Network.getNetwork().sendToServer(new HireFireMessage(buildingView, false, citizen.getId(), getProducer().getRuntimeID()));
    }

    @Override
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    @Override
    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        Network.getNetwork().sendToServer(new CourierHiringModeMessage(buildingView, hiringMode, getProducer().getRuntimeID()));
    }

    @Override
    public boolean canAssign(ICitizenDataView data)
    {
        for (final IBuildingView bView : buildingView.getColony().getBuildings())
        {
            final CourierAssignmentModuleView view = bView.getModuleViewMatching(CourierAssignmentModuleView.class, m-> !m.buildingView.getId().equals(buildingView.getId()));
            if (view != null && view.getAssignedCitizens().contains(data.getId()))
            {
                return false;
            }
        }
        
        return !data.isChild() && data.getJobView() != null && data.getJobView().getEntry() == ModJobs.delivery.get();
    }

    @Override
    public int getMaxInhabitants()
    {
        return this.buildingView.getBuildingLevel() * 2;
    }

    @NotNull
    @Override
    public BOWindow getWindow()
    {
        return new SpecialAssignmentModuleWindow(buildingView, Constants.MOD_ID + ":gui/layouthuts/layoutcourierassignment.xml");
    }

    @Override
    public boolean isFull()
    {
        return getAssignedCitizens().size() >= getMaxInhabitants();
    }

    @Override
    public JobEntry getJobEntry()
    {
        return ModJobs.delivery.get();
    }
}
