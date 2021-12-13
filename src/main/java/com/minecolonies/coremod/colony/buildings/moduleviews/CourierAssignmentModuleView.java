package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IAssignmentModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.ModJobs;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.modules.CourierAssignmentModuleWindow;
import com.minecolonies.coremod.network.messages.server.colony.building.CourierHiringModeMessage;
import com.minecolonies.coremod.network.messages.server.colony.building.HireFireMessage;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AbstractBuilding View for clients.
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

    @Override
    public List<Integer> getAssignedCitizens()
    {
        return new ArrayList<>(workerIDs);
    }

    @Override
    public void addCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.add(citizen.getId());
        Network.getNetwork().sendToServer(new HireFireMessage(buildingView, true, citizen.getId(), getJobEntry()));
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        final int size = buf.readInt();
        workerIDs.clear();
        for (int i = 0; i < size; i++)
        {
            workerIDs.add(buf.readInt());
        }

        this.hiringMode = HiringMode.values()[buf.readInt()];
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
        Network.getNetwork().sendToServer(new HireFireMessage(buildingView, false, citizen.getId(), getJobEntry()));
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
        Network.getNetwork().sendToServer(new CourierHiringModeMessage(buildingView, hiringMode));
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
        
        return !data.isChild() && data.getJobView() != null && data.getJobView().getEntry() == ModJobs.delivery;
    }

    @Override
    public int getMaxInhabitants()
    {
        return this.buildingView.getBuildingLevel() * 2;
    }

    @NotNull
    @Override
    public Window getWindow()
    {
        return new CourierAssignmentModuleWindow(buildingView, Constants.MOD_ID + ":gui/layouthuts/layoutcourierassignment.xml");
    }

    @Override
    public boolean isFull()
    {
        return getAssignedCitizens().size() >= getMaxInhabitants();
    }

    @Override
    public JobEntry getJobEntry()
    {
        return ModJobs.delivery;
    }
}
