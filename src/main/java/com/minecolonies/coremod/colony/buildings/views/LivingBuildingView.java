package com.minecolonies.coremod.colony.buildings.views;

import com.minecolonies.api.colony.IColonyView;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
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
        residents.remove(index);
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
    }
}
