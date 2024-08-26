package com.minecolonies.core.client.gui.map;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.ZoomDragView;
import com.minecolonies.api.client.render.modeltype.ISimpleModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView;
import com.minecolonies.api.colony.buildings.workerbuildings.ITownHallView.MapEntry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.colony.ColonyListMessage;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.COLOR_TEXT_FULFILLED;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_EXIT;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_INVENTORY;
import static com.minecolonies.core.client.gui.map.MinecraftMap.MAP_CENTER;

public class WindowColonyMap extends AbstractWindowSkeleton
{
    /**
     * Static multiplier to make background map larger -> icons smaller
     */
    private static final int MAP_ZOOM = 2;
    private static final int MAP_SIZE = MinecraftMap.MAP_SIZE * MAP_ZOOM;

    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/map/windowcolonymap.xml";

    /**
     * Scale at which colonies stop showing details
     */
    private static final double COLONY_DETAIL_SCALE = 0.3;

    /**
     * List of known colonies
     */
    private static List<ColonyListMessage.ColonyInfo> colonies = new ArrayList<>();

    /**
     * The position of the looker
     */
    private final BlockPos playerPos;

    /**
     * The zoomable view
     */
    private final ZoomDragView dragView;

    /**
     * Colony data beeing currently displayed
     */
    private Map<ICitizenDataView, Pane>             citizens       = new HashMap<>();
    private Map<IBuildingView, ItemIcon>            buildings      = new HashMap<>();
    private Map<ColonyListMessage.ColonyInfo, View> coloniesImages = new HashMap<>();

    /**
     * building reference of the view
     */
    private ITownHallView building;

    /**
     * Scale formatting
     */
    private final DecimalFormat scaleformet = new DecimalFormat("##");

    /**
     * Scale of the map element
     */
    private double currentScale = 0;

    /**
     * Check if it has maps.
     */
    private final boolean hasMaps;

    /**
     * Top left corner for map positioning
     */
    private final BlockPos worldPosRoot;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param building The building the info window is for.
     */
    public WindowColonyMap(final ITownHallView building)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.building = building;
        playerPos = Minecraft.getInstance().player.blockPosition();
        dragView = findPaneOfTypeByID("dragView", ZoomDragView.class);

        hasMaps = !building.getMapDataList().isEmpty();
        findPaneByID("warning").setVisible(!hasMaps);

        if (hasMaps)
        {
            // compute top left corner using all provided maps
            // then shift even more to top left to provide spacing for one more "fake" map
            // another fake map is added to bottom right for same reason
            int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE, maxScale = 0;
            for (final MapEntry entry : building.getMapDataList())
            {
                final int scale = 1 << entry.mapData().scale;
                maxScale = Math.max(maxScale, scale);
                minX = Math.min(minX, entry.mapData().centerX - MAP_CENTER * scale);
                maxX = Math.max(maxX, entry.mapData().centerX + MAP_CENTER * scale);
                minZ = Math.min(minZ, entry.mapData().centerZ - MAP_CENTER * scale);
                maxZ = Math.max(maxZ, entry.mapData().centerZ + MAP_CENTER * scale);
            }
            worldPosRoot = new BlockPos(minX - MAP_SIZE * maxScale, 0, minZ - MAP_SIZE * maxScale);

            addMaps();
            final MinecraftMap bottomRightFake = new MinecraftMap();
            bottomRightFake.setSize(MAP_SIZE * maxScale, MAP_SIZE * maxScale);
            bottomRightFake.setID("mapBottomRight" + maxX + "-" + maxZ);
            putPaneTopLeftCornerAtWorldPos(bottomRightFake, new BlockPos(maxX, 0, maxZ));
            dragView.addChild(bottomRightFake);

            addCitizens(building.getColony());
            addCenterPos();
        }
        else
        {
            worldPosRoot = null;
        }

        registerButton(BUTTON_EXIT, () -> building.openGui(false));
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);

        new ColonyListMessage().sendToServer();
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        new OpenInventoryMessage(building).sendToServer();
    }

    /**
     * Add the map.
     */
    private void addMaps()
    {
        for (final MapEntry mapEntry : building.getMapDataList())
        {
            final MapItemSavedData mapData = mapEntry.mapData();
            final int scale = 1 << mapData.scale;

            final MinecraftMap mapImage = new MinecraftMap();
            putPaneTopLeftCornerAtWorldPos(mapImage, new BlockPos(mapData.centerX - MAP_CENTER * scale, 0, mapData.centerZ - MAP_CENTER * scale));
            mapImage.setID("map" + mapData.centerX + "-" + mapData.centerZ);
            mapImage.setMapData(mapEntry.mapId(), mapData);
            mapImage.setSize(MAP_SIZE * scale, MAP_SIZE * scale);
            dragView.addChild(mapImage);
        }
    }

    /**
     * Set the known colonies with minimal info
     *
     * @param colonyInfo
     */
    public static void setColonies(final List<ColonyListMessage.ColonyInfo> colonyInfo)
    {
        colonies = colonyInfo;
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (hasMaps)
        {
            for (Map.Entry<ICitizenDataView, Pane> entry : citizens.entrySet())
            {
                final EntityCitizen citizen = (EntityCitizen) building.getColony().getWorld().getEntity(entry.getKey().getEntityId());
                if (citizen != null)
                {
                    putPaneCenterAtWorldPos(entry.getValue(), citizen.blockPosition());
                }
            }

            if (currentScale != dragView.getScale())
            {
                currentScale = dragView.getScale();
                updateScale();
            }
        }
    }

    /**
     * Update elements to the new scaling factor
     */
    private void updateScale()
    {
        for (final ColonyListMessage.ColonyInfo info : colonies)
        {
            updateColonyInfoImage(info);
        }

        for (final IBuildingView buildingView : building.getColony().getBuildings())
        {
            updateBuildingView(buildingView);
        }

        if (currentScale < COLONY_DETAIL_SCALE)
        {
            // Hide small icons
            // show colony

            for (Map.Entry<IBuildingView, ItemIcon> buildingEntry : buildings.entrySet())
            {
                buildingEntry.getValue().off();
            }

            for (Map.Entry<ICitizenDataView, Pane> citizenEntry : citizens.entrySet())
            {
                citizenEntry.getValue().off();
            }
        }
        else
        {
            // Display small icons
            for (Map.Entry<IBuildingView, ItemIcon> buildingEntry : buildings.entrySet())
            {
                buildingEntry.getValue().on();
            }

            for (Map.Entry<ICitizenDataView, Pane> citizenEntry : citizens.entrySet())
            {
                citizenEntry.getValue().on();
            }

            for (final Map.Entry<ColonyListMessage.ColonyInfo, View> colonyEntry : coloniesImages.entrySet())
            {
                colonyEntry.getValue().off();
            }
        }

        findPaneOfTypeByID("scale", Text.class).setText(Component.literal(scaleformet.format(1 / currentScale) + "x"));
    }

    /**
     * Adds a colony image for the given colony
     *
     * @param colonyInfo
     */
    private void updateColonyInfoImage(final ColonyListMessage.ColonyInfo colonyInfo)
    {
        View colonyPane = coloniesImages.get(colonyInfo);
        if (colonyPane == null)
        {
            colonyPane = ColonySize.createViewForInfo(colonyInfo);
            colonyPane.setID(colonyInfo.getId() + colonyInfo.getOwner());
            dragView.addChild(colonyPane);
            coloniesImages.put(colonyInfo, colonyPane);
            PaneBuilders.tooltipBuilder().hoverPane(colonyPane)
              .append(Component.literal("Owner:" + colonyInfo.getOwner()))
              .appendNL(Component.literal("Coordinates: " + colonyInfo.getCenter().getX() + "X, " + colonyInfo.getCenter().getZ() + "Z"))
              .appendNL(Component.literal("Citizens: " + colonyInfo.getCitizencount()))
              .build();
        }

        if (currentScale < COLONY_DETAIL_SCALE)
        {
            putPaneCenterAtWorldPos(colonyPane, colonyInfo.getCenter());
            colonyPane.on();
        }
        else
        {
            colonyPane.off();
        }
    }

    /**
     * Adds the central blob identifying the current position
     */
    private void addCenterPos()
    {
        final Image citizenImage = new Image();
        citizenImage.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/red_wax_home.png"), false);
        citizenImage.setSize(16, 16);
        putPaneTopLeftCornerAtWorldPos(citizenImage, playerPos);

        dragView.setScaleRaw(4.0 / 5);
        dragView.setScrollX(dragView.calcInverseAbsoluteX(citizenImage.getX()) - dragView.getWidth() / 2);
        dragView.setScrollY(dragView.calcInverseAbsoluteY(citizenImage.getY()) - dragView.getHeight() / 2);

        putPaneCenterAtWorldPos(citizenImage, playerPos);
        dragView.addChild(citizenImage);
    }

    /**
     * Generate a list of icons for the buildings
     *
     * @param buildingView
     * @return
     */
    private void updateBuildingView(final IBuildingView buildingView)
    {
        ItemIcon uiBuilding = buildings.get(buildingView);
        if (uiBuilding == null)
        {
            uiBuilding = new ItemIcon();

            uiBuilding.setID(buildingView.getID().toShortString());
            uiBuilding.setSize(11 + buildingView.getBuildingLevel(), 11 + buildingView.getBuildingLevel());
            final ItemStack item = buildingView.getBuildingType().getBuildingBlock().asItem().getDefaultInstance();
            uiBuilding.setItem(item);
            dragView.addChild(uiBuilding);

            AbstractTextBuilder.TooltipBuilder tooltip = PaneBuilders.tooltipBuilder();
            tooltip.hoverPane(uiBuilding)
              .append(BOScreen.getTooltipFromItem(mc, item).get(0)).append(Component.literal(" : " + buildingView.getBuildingLevel()))
              .appendNL(Component.literal("Coordinates: " + buildingView.getID().getX() + "X, " + buildingView.getID().getZ() + "Z"))
              .appendNL(Component.literal("Citizens: " + buildingView.getAllAssignedCitizens().size()));

            for (int id : buildingView.getAllAssignedCitizens())
            {
                final ICitizenDataView dataView = building.getColony().getCitizen(id);
                if (dataView != null)
                {
                    tooltip.appendNL(Component.literal(dataView.getName()));
                }
            }
            tooltip.build();

            uiBuilding.setVisible(true);

            this.buildings.put(buildingView, uiBuilding);
        }

        putPaneCenterAtWorldPos(uiBuilding, buildingView.getID());
    }

    /**
     * Adds all citizen icons
     *
     * @param colony
     */
    private void addCitizens(final IColonyView colony)
    {
        for (final ICitizenDataView data : colony.getCitizens().values())
        {
            final EntityCitizen citizen = (EntityCitizen) colony.getWorld().getEntity(data.getEntityId());
            if (citizen != null)
            {
                final View citizenView = new View();
                putPaneCenterAtWorldPos(citizenView, citizen.blockPosition());

                final Image citizenImage = new Image();
                citizenImage.setImage(((ISimpleModelType) IModelTypeRegistry.getInstance().getModelType(citizen.getModelType())).getTextureIcon(citizen), false);
                citizenImage.setSize(4, 4);
                citizenView.addChild(citizenImage);

                dragView.addChild(citizenView);
                final AbstractTextBuilder.TooltipBuilder builder = PaneBuilders.tooltipBuilder().hoverPane(citizenView).paragraphBreak().append(citizen.getDisplayName());
                if (!data.getJob().isEmpty())
                {
                    citizenImage.setSize(8, 8);
                    builder.newLine().append(Component.translatableEscape("com.minecolonies.coremod.gui.citizen.job.label", Component.translatable(data.getJob())));
                }
                builder.color(COLOR_TEXT_FULFILLED).build();
                citizenView.setSize(citizenImage.getWidth(), citizenImage.getHeight());

                if (data.hasVisibleInteractions())
                {
                    final Image interactionImage = new Image();
                    interactionImage.setImage(data.getInteractionIcon(), false);
                    interactionImage.setSize(6, 6);
                    citizenImage.setPosition(5, 0);
                    citizenView.addChild(interactionImage);
                    citizenView.setSize(citizenView.getWidth() + 6, citizenView.getHeight() + 6);
                }

                if (citizens.containsKey(data))
                {
                    dragView.removeChild(citizens.get(data));
                }
                citizens.put(data, citizenView);
            }
        }
    }

    private void putPaneTopLeftCornerAtWorldPos(final Pane pane, final BlockPos worldPos)
    {
        applyWorldPosToPane(pane, worldPos, 0, 0);
    }

    private void putPaneCenterAtWorldPos(final Pane pane, final BlockPos worldPos)
    {
        applyWorldPosToPane(pane, worldPos, -pane.getWidth() / 2, -pane.getHeight() / 2);
    }

    private void applyWorldPosToPane(final Pane pane, final BlockPos worldPos, final int offsetUiX, final int offsetUiZ)
    {
        // vector from player pos
        int x = worldPos.getX() - worldPosRoot.getX();
        int z = worldPos.getZ() - worldPosRoot.getZ();

        x *= MAP_ZOOM;
        z *= MAP_ZOOM;

        x += offsetUiX;
        z += offsetUiZ;

        pane.setPosition(x, z);
    }
}
