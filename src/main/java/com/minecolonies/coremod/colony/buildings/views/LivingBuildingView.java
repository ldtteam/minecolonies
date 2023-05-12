package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.building.worker.BuildingHiringModeMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Living building view.
 */
public abstract class LivingBuildingView extends AbstractBuildingView
{
    @NotNull
    private final List<Integer> residents = new ArrayList<>();

    /**
     * Max number of citizens that can live here.
     */
    private int max;

    /**
     * The set hiring mode for the view.
     */
    private HiringMode hiringMode;

    /**
     * Creates an instance of the citizen hut window.
     *
     * @param c the colonyView.
     * @param l the position the hut is at.
     */
    public LivingBuildingView(final IColonyView c, final BlockPos l)
    {
        super(c, l);
    }

    /**
     * Getter for the list of residents.
     *
     * @return an unmodifiable list.
     */
    @NotNull
    public List<Integer> getResidents()
    {
        return Collections.unmodifiableList(residents);
    }

    /**
     * Removes a resident from the building.
     *
     * @param index the index to remove it from.
     */
    public void removeResident(final int index)
    {
        residents.removeIf(v -> v == index);
    }

    /**
     * Add a resident from the building.
     *
     * @param id the id of the citizen.
     */
    public void addResident(final int id)
    {
        residents.add(id);
    }

    /**
     * Set the max citizens.
     * @param max the max.
     */
    public void setMax(final int max)
    {
        this.max = max;
    }

    /**
     * Get the max citizens.
     * @return the max.
     */
    public int getMax()
    {
        return this.max;
    }

    /**
     * Get the  hiring mode.
     * @return the mode.
     */
    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    /**
     * Adjust the hiring mode.
     * @param value the value to set it to.
     */
    public void setHiringMode(final HiringMode value)
    {
        this.hiringMode = value;
        Network.getNetwork().sendToServer(new BuildingHiringModeMessage(this, hiringMode, null));
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
    {
        super.deserialize(buf);

        residents.clear();
        final int numResidents = buf.readInt();
        for (int i = 0; i < numResidents; ++i)
        {
            residents.add(buf.readInt());
        }
        hiringMode = HiringMode.values()[buf.readInt()];
        max = buf.readInt();
    }
}
