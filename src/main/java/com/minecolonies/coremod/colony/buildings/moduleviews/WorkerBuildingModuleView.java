package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.huts.WindowHutWorkerModulePlaceholder;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AbstractBuilding View for clients.
 */
public class WorkerBuildingModuleView extends AbstractBuildingModuleView
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
     * The job display name
     */
    private String jobDisplayName;

    /**
     * Job entry of the module view.
     */
    private JobEntry jobEntry;

    public List<Integer> getWorkerId()
    {
        return new ArrayList<>(workerIDs);
    }

    public void addWorkerId(final int workerId)
    {
        workerIDs.add(workerId);
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

        this.jobEntry = buf.readRegistryIdSafe(JobEntry.class);;
        this.hiringMode = HiringMode.values()[buf.readInt()];
        this.maxInhabitants = buf.readInt();
        this.primary = Skill.values()[buf.readInt()];
        this.secondary = Skill.values()[buf.readInt()];
        this.jobDisplayName = buf.readUtf();
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

    public void removeWorkerId(final int id)
    {
        workerIDs.remove(id);
    }

    /**
     * Check if it has enough workers.
     *
     * @return true if so.
     */
    public boolean hasEnoughWorkers()
    {
        return getWorkerId().size() >= maxInhabitants;
    }

    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    public void setHiringMode(final HiringMode hiringMode)
    {
        this.hiringMode = hiringMode;
        Network.getNetwork().sendToServer(new BuildingHiringModeMessage(buildingView, hiringMode, jobEntry));
    }

    /**
     * Check if citizens can be assigned.
     * @param data the data to check.
     * @return true if so.
     */
    public boolean canAssign(ICitizenDataView data)
    {
        return !data.isChild();
    }

    /**
     * Get the max number of inhabitants
     *
     * @return max inhabitants
     */
    public int getMaxInhabitants()
    {
        return this.maxInhabitants;
    }

    public String getJobDisplayName()
    {
        return jobDisplayName;
    }

    @NotNull
    @Override
    public Window getWindow()
    {
        return new WindowHutWorkerModulePlaceholder<>(buildingView, "");
    }

    /**
     * Getter for the job entry of the module view.
     * @return the entry.
     */
    public JobEntry getJobEntry()
    {
        return jobEntry;
    }
}
