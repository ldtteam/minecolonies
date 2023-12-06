package com.minecolonies.coremod.colony.buildings.moduleviews;

import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModuleView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.modules.ToolModuleWindow;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Client side version of the abstract class for all buildings which allows to select tools.
 */
public class LivingBuildingModuleView extends AbstractBuildingModuleView
{
    /**
     * List of the worker ids.
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

    public List<Integer> getAssignedCitizens()
    {
        return Collections.unmodifiableList(new ArrayList<>(residents));
    }

    public void remove(final int id)
    {
        residents.remove(id);
    }

    public void add(final int id)
    {
        residents.add(id);
    }

    @Override
    public String getDesc()
    {
        return "com.minecolonies.coremod.gui.workerhuts.tools";
    }

    @Override
    public void deserialize(@NotNull final FriendlyByteBuf buf)
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
        return new ToolModuleWindow(Constants.MOD_ID + ":gui/layouthuts/layouttool.xml", buildingView, null);
    }

    @Override
    public String getIcon()
    {
        return "scepter";
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
