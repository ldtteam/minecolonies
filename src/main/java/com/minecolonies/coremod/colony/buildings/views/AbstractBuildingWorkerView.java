package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuildingWorkerView;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AbstractBuildingWorker View for clients.
 */
public abstract class AbstractBuildingWorkerView extends AbstractBuildingView implements IBuildingWorkerView
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
     * The name of the job.
     */
    private String jobName;

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
     * Creates the view representation of the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public AbstractBuildingWorkerView(final IColonyView c, @NotNull final BlockPos l)
    {
        super(c, l);
    }

    @Override
    public List<Integer> getWorkerId()
    {
        return new ArrayList<>(workerIDs);
    }

    @Override
    public void addWorkerId(final int workerId)
    {
        workerIDs.add(workerId);
    }

    @Override
    public void deserialize(@NotNull final PacketBuffer buf)
    {
        super.deserialize(buf);
        final int size = buf.readInt();
        workerIDs.clear();
        for (int i = 0; i < size; i++)
        {
            workerIDs.add(buf.readInt());
        }

        this.hiringMode = HiringMode.values()[buf.readInt()];
        this.jobName = buf.readUtf(32767);
        this.maxInhabitants = buf.readInt();
        this.primary = Skill.values()[buf.readInt()];
        this.secondary = Skill.values()[buf.readInt()];
        this.maxInhabitants = buf.readInt();
        this.jobDisplayName = buf.readUtf();
    }

    @Override
    @NotNull
    public Skill getPrimarySkill()
    {
        return primary;
    }

    @Override
    @NotNull
    public Skill getSecondarySkill()
    {
        return secondary;
    }

    @Override
    public void removeWorkerId(final int id)
    {
        workerIDs.remove(id);
    }

    @Override
    public boolean hasEnoughWorkers()
    {
        return !workerIDs.isEmpty();
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
        Network.getNetwork().sendToServer(new BuildingHiringModeMessage(this, hiringMode));
    }

    @Override
    public String getJobName()
    {
        return this.jobName;
    }

    /**
     * Check if it is possible to assign the citizen to this building.
     *
     * @param citizenDataView the citizen to test.
     * @return true if so.
     */
    public boolean canAssign(final ICitizenDataView citizenDataView)
    {
        return true;
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

    @Override
    public String getJobDisplayName()
    {
        return jobDisplayName;
    }
}