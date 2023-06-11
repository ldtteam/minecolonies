package com.minecolonies.coremod.client.gui.map;

import com.ldtteam.blockui.BOScreen;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.AbstractTextBuilder;
import com.ldtteam.blockui.controls.Image;
import com.ldtteam.blockui.controls.ItemIcon;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.ZoomDragView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.client.render.modeltype.ISimpleModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import com.minecolonies.coremod.network.messages.client.colony.ColonyListMessage;
import com.minecolonies.coremod.network.messages.server.colony.OpenInventoryMessage;
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

public class WindowColonyMap extends AbstractWindowSkeleton
{
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
    private final ZoomDragMap dragView;

    /**
     * Colony data beeing currently displayed
     */
    private Map<ICitizenDataView, Pane>             citizens       = new HashMap<>();
    private Map<IBuildingView, ItemIcon>            buildings      = new HashMap<>();
    private Map<ColonyListMessage.ColonyInfo, View> coloniesImages = new HashMap<>();
    private List<MinecraftMap>                      maps           = new ArrayList<>();

    /**
     * building reference of the view
     */
    private IBuildingView building;

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
    private boolean hasMaps = false;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param building The building the info window is for.
     */
    public WindowColonyMap(final IBuildingView building)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.building = building;
        playerPos = new BlockPos(Minecraft.getInstance().player.blockPosition().getX(), 0, Minecraft.getInstance().player.blockPosition().getZ());
        final ZoomDragView parent = findPaneOfTypeByID("dragView", ZoomDragView.class);
        dragView = new ZoomDragMap();
        dragView.setSize(parent.getWidth(), parent.getHeight());
        dragView.setPosition(parent.getX(), parent.getY());
        dragView.enable();
        dragView.setVisible(true);
        dragView.setFocus();
        dragView.setWindow(this);
        parent.addChild(dragView);
        if (addMaps())
        {
            addCitizens(building.getColony());
            addCenterPos();
        }

        registerButton(BUTTON_EXIT, () -> building.openGui(false));
        registerButton(BUTTON_INVENTORY, this::inventoryClicked);

        Network.getNetwork().sendToServer(new ColonyListMessage());
    }

    /**
     * Action when a button opening an inventory is clicked.
     */
    private void inventoryClicked()
    {
        Network.getNetwork().sendToServer(new OpenInventoryMessage(building));
    }

    /**
     * Add the map. Return false if has no maps.
     * @return true if so.
     */
    private boolean addMaps()
    {
        for (final MinecraftMap map : maps)
        {
            dragView.removeChild(map);
            map.close();
        }

        maps.clear();

        for (final MapItemSavedData mapData : ((BuildingTownHall.View) building).getMapDataList())
        {
            if (mapData.scale != 0)
            {
                continue;
            }

            hasMaps = true;

            final MinecraftMap mapImage = new MinecraftMap();
            mapImage.setPosition(worldPosToUIPos(new BlockPos(mapData.centerX - 64, 0, 0)).getX(), worldPosToUIPos(new BlockPos(0, 0, mapData.centerZ - 64)).getZ());
            mapImage.setID("map" + mapData.centerX + "-" + mapData.centerZ);
            mapImage.setMapData(mapData);
            mapImage.setSize((int) (512*currentScale), (int) (512*currentScale));
            dragView.addChildFirst(mapImage);
            maps.add(mapImage);
        }

        findPaneByID("warning").setVisible(!hasMaps);
        return hasMaps;
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
                    entry.getValue().setPosition(worldPosToUIPos(citizen.blockPosition()).getX(), worldPosToUIPos(citizen.blockPosition()).getZ());
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

        addMaps();
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
            colonyPane.setPosition(worldPosToUIPos(colonyInfo.getCenter()).getX() - colonyPane.getWidth() / 2,
              worldPosToUIPos(colonyInfo.getCenter()).getZ() - colonyPane.getHeight() / 2);
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
        citizenImage.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/red_wax_actions.png"), false);
        citizenImage.setSize(16, 16);
        citizenImage.setPosition(worldPosToUIPos(playerPos).getX(), worldPosToUIPos(playerPos).getZ());
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

            dragView.removeChild(this.buildings.get(buildingView));
            this.buildings.put(buildingView, uiBuilding);
        }

        uiBuilding.setPosition(worldPosToUIPos(buildingView.getID()).getX(), worldPosToUIPos(buildingView.getID()).getZ());
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
                citizenView.setPosition(worldPosToUIPos(citizen.blockPosition()).getX(), worldPosToUIPos(citizen.blockPosition()).getZ());

                final Image citizenImage = new Image();
                citizenImage.setImage(((ISimpleModelType) IModelTypeRegistry.getInstance().getModelType(citizen.getModelType())).getTextureIcon(citizen), false);
                citizenImage.setSize(4, 4);
                citizenView.addChild(citizenImage);

                dragView.addChild(citizenView);
                final AbstractTextBuilder.TooltipBuilder builder = PaneBuilders.tooltipBuilder().hoverPane(citizenView).paragraphBreak().append(citizen.getDisplayName());
                if (!data.getJob().isEmpty())
                {
                    citizenImage.setSize(8, 8);
                    builder.newLine().append(Component.translatable("com.minecolonies.coremod.gui.citizen.job.label", LanguageHandler.format(data.getJob())));
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

                dragView.removeChild(citizens.get(data));
                citizens.put(data, citizenView);
            }
        }
    }

    /**
     * Scales the world pos to the UI pos, current scales positions which are farther out to display closer so you get a decent overview of nearby colonies which can be quite far away
     *
     * @param worldPos
     * @return
     */
    private BlockPos worldPosToUIPos(final BlockPos worldPos)
    {
        return BlockPos.containing(
          dragView.getWidth() / 2.0 - ((playerPos.getX() - worldPos.getX()) * 4 / Math.max(1, Math.log(Math.abs(playerPos.getX() - worldPos.getX()) / 1000f))) * currentScale,
          0,
          dragView.getHeight() / 2.0 - ((playerPos.getZ() - worldPos.getZ()) * 4 / Math.max(1, Math.log(Math.abs(playerPos.getZ() - worldPos.getZ()) / 1000f))) * currentScale);
    }

    @Override
    public void onClosed()
    {
        super.onClosed();
        maps.forEach(MinecraftMap::close);
    }
}
