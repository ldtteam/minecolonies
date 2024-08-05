package com.minecolonies.core.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Client side version of the living building module.
 */
public class LivingBuildingModuleView extends AbstractBuildingModuleView
{
    /**
     * List of the residents assigned.
     */
    private final Set<Integer> residents = new HashSet<>();

    /**
     * The hiring mode of the building.
     */
    private HiringMode hiringMode;

    /**
     * The max amount of inhabitants
     */
    private int maxInhabitants = 1;

    public LivingBuildingModuleView()
    {
        super();
    }

    /**
     * Get the list of residents
     *
     * @return
     */
    public List<Integer> getAssignedCitizens()
    {
        return Collections.unmodifiableList(new ArrayList<>(residents));
    }

    /**
     * Remove a resident by citizen data id
     *
     * @param id
     */
    public void remove(final int id)
    {
        residents.remove(id);
    }

    /**
     * Add a resident by citizen data id
     *
     * @param id
     */
    public void add(final int id)
    {
        residents.add(id);
    }

    @Override
    public String getDesc()
    {
        return null;
    }

    @Override
    public void deserialize(@NotNull final RegistryFriendlyByteBuf buf)
    {
        residents.clear();
        final int numResidents = buf.readInt();
        for (int i = 0; i < numResidents; ++i)
        {
            residents.add(buf.readInt());
        }
        hiringMode = HiringMode.values()[buf.readInt()];
        maxInhabitants = buf.readInt();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BOWindow getWindow()
    {
        return null;
    }

    @Override
    public String getIcon()
    {
        return null;
    }

    public boolean isPageVisible() {return false;}

    public int getMax()
    {
        return maxInhabitants;
    }

    public HiringMode getHiringMode()
    {
        return hiringMode;
    }

    public void setHiringMode(final HiringMode value)
    {
        this.hiringMode = value;
    }
}
