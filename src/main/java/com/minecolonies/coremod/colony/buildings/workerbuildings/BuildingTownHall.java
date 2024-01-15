package com.minecolonies.coremod.colony.buildings.workerbuildings;

import com.google.common.collect.ImmutableList;
import com.ldtteam.blockui.views.BOWindow;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.modules.IBuildingModule;
import com.minecolonies.api.colony.buildings.modules.settings.ISettingKey;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHall;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.colonyEvents.descriptions.IColonyEventDescription;
import com.minecolonies.api.colony.colonyEvents.registry.ColonyEventDescriptionTypeRegistryEntry;
import com.minecolonies.api.colony.permissions.PermissionEvent;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.townhall.WindowMainPage;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.buildings.modules.BuildingModules;
import com.minecolonies.coremod.colony.buildings.modules.settings.BoolSetting;
import com.minecolonies.coremod.colony.buildings.modules.settings.SettingKey;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.minecolonies.api.util.constant.ColonyConstants.MAX_COLONY_EVENTS;
import static com.minecolonies.api.util.constant.Constants.MOD_ID;

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
     * Citizen spawning.
     */
    public static final ISettingKey<BoolSetting> MOVE_IN              = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "kidspawn"));
    /**
     * Enter leave messages.
     */
    public static final ISettingKey<BoolSetting> ENTER_LEAVE_MESSAGES = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "enterleave"));

    /**
     * Automatic hiring mode.
     */
    public static final ISettingKey<BoolSetting> AUTO_HIRING_MODE = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "autohiring"));

    /**
     * AUtomatic housing mode.
     */
    public static final ISettingKey<BoolSetting> AUTO_HOUSING_MODE = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "autohousing"));

    /**
     * Constructgion tape setting.
     */
    public static final ISettingKey<BoolSetting> CONSTRUCTION_TAPE = new SettingKey<>(BoolSetting.class, new ResourceLocation(MOD_ID, "tape"));

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
    public void addPermissionEvent(final PermissionEvent event)
    {
        if (!permissionEvents.contains(event))
        {
            if (permissionEvents.size() >= MAX_COLONY_EVENTS)
            {
                permissionEvents.removeFirst();
            }
            permissionEvents.add(event);
            markDirty();
        }
    }

    @Override
    public void registerModule(@NotNull final IBuildingModule module)
    {
        if (module.getProducer() == BuildingModules.TOWNHALL_SETTINGS)
        {
            super.registerModule(((Colony)colony).getSettings());
        }
        else
        {
            super.registerModule(module);
        }
    }

    @Override
    public void removePermissionEvents(@NotNull final UUID id)
    {
        if (permissionEvents.removeIf(e -> id.equals(e.getId())))
        {
            markDirty();
        }
    }

    @Override
    public void serializeToView(@NotNull final FriendlyByteBuf buf)
    {
        super.serializeToView(buf);

        buf.writeBoolean(MineColonies.getConfig().getServer().canPlayerUseAllyTHTeleport.get());
        buf.writeInt(permissionEvents.size());
        for (final PermissionEvent event : permissionEvents)
        {
            event.serialize(buf);
        }

        List<IColonyEventDescription> colonyEvents = colony.getEventDescriptionManager().getEventDescriptions();
        buf.writeInt(colonyEvents.size());
        for (final IColonyEventDescription event : colonyEvents)
        {
            buf.writeUtf(event.getEventTypeId().getPath());
            event.serialize(buf);
        }

        final List<ItemStack> maps = new ArrayList<>();
        for (final ItemStack stack : InventoryUtils.getBuildingInventory(this))
        {
            if (!stack.isEmpty() && stack.getItem() == Items.FILLED_MAP)
            {
                maps.add(stack);
            }
        }

        final Level level = colony.getWorld();

        int validMapCount = 0;
        for (final ItemStack stack : maps)
        {
            if(MapItem.getSavedData(stack, level) != null)
            {
                validMapCount++;
            }
        }

        buf.writeInt(validMapCount);
        for (final ItemStack stack : maps)
        {
            final MapItemSavedData mapData = MapItem.getSavedData(stack, level);
            if(mapData != null)
            {
                buf.writeNbt(mapData.save(new CompoundTag()));
            }
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
         * List of colony events.
         */
        private ImmutableList<IColonyEventDescription> colonyEvents = ImmutableList.of();

        /**
         * If the player is allowed to do townHall teleport.
         */
        private boolean canPlayerUseTP = false;

        /**
         * List of mapdata.
         */
        private List<MapItemSavedData> mapDataList = new ArrayList<>();

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
        public BOWindow getWindow()
        {
            return new WindowMainPage(this);
        }

        @Override
        public void deserialize(@NotNull final FriendlyByteBuf buf)
        {
            super.deserialize(buf);

            canPlayerUseTP = buf.readBoolean();
            final int permissionEventsSize = buf.readInt();
            permissionEvents.clear();
            for (int i = 0; i < permissionEventsSize; i++)
            {
                permissionEvents.add(new PermissionEvent(buf));
            }

            final List<IColonyEventDescription> tempEvents = new ArrayList<>();
            final int colonyEventsSize = buf.readInt();
            for (int i = 0; i < colonyEventsSize; i++)
            {
                final ResourceLocation eventTypeID = new ResourceLocation(MOD_ID, buf.readUtf());

                final ColonyEventDescriptionTypeRegistryEntry registryEntry = MinecoloniesAPIProxy.getInstance().getColonyEventDescriptionRegistry().getValue(eventTypeID);
                if (registryEntry == null)
                {
                    Log.getLogger().warn("Event is missing registryEntry!:" + eventTypeID.getPath());
                    continue;
                }

                tempEvents.add(registryEntry.deserializeEventDescriptionFromFriendlyByteBuf(buf));
            }
            Collections.reverse(tempEvents);
            colonyEvents = ImmutableList.copyOf(tempEvents);

            final int size = buf.readInt();
            mapDataList.clear();
            for (int i = 0; i < size; i++)
            {
                final MapItemSavedData mapData = MapItemSavedData.load(buf.readNbt());
                mapDataList.add(mapData);
            }
        }

        @Override
        public List<PermissionEvent> getPermissionEvents()
        {
            return new LinkedList<>(permissionEvents);
        }

        @Override
        public List<IColonyEventDescription> getColonyEvents()
        {
            return colonyEvents;
        }

        @Override
        public boolean canPlayerUseTP()
        {
            return canPlayerUseTP;
        }

        /**
         * Getter for the mapdata.
         * @return the original list.
         */
        public List<MapItemSavedData> getMapDataList()
        {
            return mapDataList;
        }
    }
}
