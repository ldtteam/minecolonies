package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.IBuilding;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowTownHall;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

import static com.minecolonies.api.util.constant.ColonyConstants.MAX_PERMISSION_EVENTS;

/**
 * Class used to manage the townHall building block.
 */
public class BuildingTownHall extends AbstractBuilding implements ITownHall
{
    /**
     * Description of the block used to set this block.
     */
    private static final String TOWN_HALL = "townhall";

    /**
     * Max building level of the hut.
     */
    private static final int MAX_BUILDING_LEVEL = 5;

    /**
     * List of permission events of the colony.
     */
    private final LinkedList<PermissionEvent> permissionEvents = new LinkedList<>();

    /**
     * Instantiates the building.
     *
     * @param c the colony.
     * @param l the location.
     */
    public BuildingTownHall(final IColony c, final BlockPos l)
    {
        super(c, l);
    }

    @NotNull
    @Override
    public String getSchematicName()
    {
        return TOWN_HALL;
    }

    @Override
    public int getMaxBuildingLevel()
    {
        return MAX_BUILDING_LEVEL;
    }

    @Override
    public void onUpgradeComplete(final int newLevel)
    {
        super.onUpgradeComplete(newLevel);
    }

    @Override
    public BuildingEntry getBuildingRegistryEntry()
    {
        return ModBuildings.townHall;
    }

    /**
     * Add a colony permission event to the colony.
     * Reduce the list by one if bigger than a treshhold.
     * @param event the event to add.
     */
    @Override
    public void addPermissionEvent(final PermissionEvent event)
    {
        if(getBuildingLevel() >= 1 && !permissionEvents.contains(event))
        {
            if (permissionEvents.size() >= MAX_PERMISSION_EVENTS)
            {
                permissionEvents.removeFirst();
            }
            permissionEvents.add(event);
            markDirty();
        }
    }

    @Override
    public void serializeToView(@NotNull final PacketBuffer buf)
    {
        super.serializeToView(buf);

        buf.writeBoolean(MineColonies.getConfig().getCommon().canPlayerUseAllyTHTeleport.get());
        buf.writeInt(permissionEvents.size());
        for(final PermissionEvent event: permissionEvents)
        {
            event.serialize(buf);
        }
    }

    @Override
    public int getClaimRadius(final int newLevel)
    {
        switch (newLevel)
        {
            case 0:
                return 0;
            case 1:
            case 2:
                return 1;
            case 3:
                return 2;
            case 4:
                return 3;
            case 5:
                return 5;
            default:
                return 0;
        }
    }

    @Override
    public boolean canBeGathered()
    {
        return false;
    }

    @Override
    public void onBuildingMove(final IBuilding oldBuilding)
    {
        super.onBuildingMove(oldBuilding);
        colony.getBuildingManager().setTownHall(this);
    }

    /**
     * Sets the style of the building.
     *
     * @param style String value of the style.
     */
    @Override
    public void setStyle(final String style)
    {
        super.setStyle(style);
        colony.setStyle(style);
    }

    /**
     * ClientSide representation of the building.
     */
    public static class View extends AbstractBuildingView implements ITownHallView
    {
        /**
         * List of permission events of the colony.
         */
        private final List<PermissionEvent> permissionEvents = new LinkedList<>();

        /**
         * If the player is allowed to do townHall teleport.
         */
        private boolean canPlayerUseTP = false;

        /**
         * Instantiates the view of the building.
         *
         * @param c the colonyView.
         * @param l the location of the block.
         */
        public View(final IColonyView c, final BlockPos l)
        {
            super(c, l);
        }

        @NotNull
        @Override
        public Window getWindow()
        {
            return new WindowTownHall(this);
        }

        @Override
        public void deserialize(@NotNull final PacketBuffer buf)
        {
            super.deserialize(buf);

            canPlayerUseTP = buf.readBoolean();
            final int size = buf.readInt();
            for(int i = 0; i < size; i++)
            {
                permissionEvents.add(new PermissionEvent(buf));
            }
        }

        /**
         * Get a list of permission events.
         * @return a copy of the list of events.
         */
        @Override
        public List<PermissionEvent> getPermissionEvents()
        {
            return new LinkedList<>(permissionEvents);
        }

        /**
         * Check if the player can use the teleport command.
         * @return true if so.
         */
        @Override
        public boolean canPlayerUseTP()
        {
            return canPlayerUseTP;
        }
    }
}
