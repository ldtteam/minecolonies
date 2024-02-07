package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.buildings.modules.IAssignmentModuleView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.core.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.core.network.messages.server.colony.building.HireFireMessage;
import com.minecolonies.core.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AbstractBuilding View for clients.
 */
public class WorkerBuildingModuleView extends AbstractBuildingModuleView implements IAssignmentModuleView
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
     * The max amount of inhabitants
     */
    private int maxInhabitants = 1;

    /**
     * The primary skill.
     */
    private Skill primary = Skill.Intelligence;

    /**
     * The secondary skill.
     */
    private Skill secondary = Skill.Intelligence;

    /**
     * Job entry of the module view.
     */
    private JobEntry jobEntry;

    @Override
    public List<Integer> getAssignedCitizens()
    {
        return new ArrayList<>(workerIDs);
    }

    @Override
    public void addCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.add(citizen.getId());
        new HireFireMessage(buildingView, true, citizen.getId(), getProducer().getRuntimeID()).sendToServer();
        citizen.setWorkBuilding(buildingView.getPosition());
        citizen.setJobView(getJobEntry().getJobViewProducer().get().apply(buildingView.getColony(), citizen));
        citizen.getJobView().setEntry(getJobEntry());
    }

    @Override
    public void removeCitizen(final @NotNull ICitizenDataView citizen)
    {
        workerIDs.remove(citizen.getId());
        new HireFireMessage(buildingView, false, citizen.getId(), getProducer().getRuntimeID()).sendToServer();
        citizen.setWorkBuilding(null);
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
        this.maxInhabitants = buf.readInt();

        this.jobEntry = buf.readById(MinecoloniesAPIProxy.getInstance().getJobRegistry());
        this.primary = Skill.values()[buf.readInt()];
        this.secondary = Skill.values()[buf.readInt()];
    }

    @Override
    public String getIcon()
    {
        return "";
    }

    @Override
    public String getDesc()
    {
        return "";
    }

    @Override
    public boolean isPageVisible()
    {
        return false;
    }

    @NotNull
    public Skill getPrimarySkill()
    {
        return primary;
    }

    @NotNull
    public Skill getSecondarySkill()
    {
        return secondary;
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
        new BuildingHiringModeMessage(buildingView, hiringMode, getProducer().getRuntimeID()).sendToServer();
    }

    @Override
    public boolean canAssign(final ICitizenDataView citizen)
    {
        return !citizen.isChild() &&
                 (citizen.getWorkBuilding() == null
                    || workerIDs.contains(citizen.getId())
                    || buildingView.getColony().getBuilding(citizen.getWorkBuilding()) != null && buildingView.getColony().getBuilding(citizen.getWorkBuilding()).getModuleViewMatching(WorkerBuildingModuleView.class, m -> m.canBeHiredAs(getJobEntry()))
                         != null);
    }

    @Override
    public int getMaxInhabitants()
    {
        return this.maxInhabitants;
    }

    /**
     * Get the display name of the job.
     *
     * @return the display name.
     */
    public String getJobDisplayName()
    {
        return Component.translatable(jobEntry.getTranslationKey()).getString();
    }

    @NotNull
    @Override
    public BOWindow getWindow()
    {
        return new WindowHutWorkerModulePlaceholder<>(buildingView);
    }

    /**
     * Getter for the job entry of the module view.
     *
     * @return the entry.
     */
    public JobEntry getJobEntry()
    {
        return jobEntry;
    }

    @Override
    public boolean isFull()
    {
        return !buildingView.allowsAssignment() || getAssignedCitizens().size() >= getMaxInhabitants();
    }
}
