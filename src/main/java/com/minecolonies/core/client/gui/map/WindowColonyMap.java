package com.minecolonies.core.client.gui.map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.*;
import com.ldtteam.blockui.util.resloc.OutOfJarResourceLocation;
import com.ldtteam.blockui.views.Box;
import com.ldtteam.blockui.views.View;
import com.ldtteam.blockui.views.ZoomDragView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.client.render.modeltype.ISimpleModelType;
import com.minecolonies.api.client.render.modeltype.registry.IModelTypeRegistry;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.requestsystem.location.ILocation;
import com.minecolonies.api.colony.requestsystem.manager.IRequestManager;
import com.minecolonies.api.colony.requestsystem.request.IRequest;
import com.minecolonies.api.colony.requestsystem.requestable.IStackBasedTask;
import com.minecolonies.api.colony.requestsystem.resolver.player.IPlayerRequestResolver;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.SoundUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.AbstractWindowSkeleton;
import com.minecolonies.core.client.render.worldevent.HighlightManager;
import com.minecolonies.core.client.render.worldevent.highlightmanager.CitizenRenderData;
import com.minecolonies.core.colony.buildings.views.LivingBuildingView;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingTownHall;
import com.minecolonies.core.colony.requestsystem.locations.StaticLocation;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import com.minecolonies.core.network.messages.client.colony.ColonyListMessage;
import com.minecolonies.core.network.messages.server.colony.OpenInventoryMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minecolonies.api.research.util.ResearchConstants.COLOR_TEXT_FULFILLED;
import static com.minecolonies.api.util.constant.CitizenConstants.LOW_SATURATION;
import static com.minecolonies.api.util.constant.TranslationConstants.COLONYMAP_PLAYER_RESOLVED_REQUESTS;
import static com.minecolonies.api.util.constant.TranslationConstants.COLONYMAP_PLAYER_RESOLVED_REQUESTS_COUNT;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.core.client.gui.questlog.Constants.HIGHLIGHT_QUEST_LOG_TRACKER_DURATION;
import static net.minecraft.client.gui.Gui.GUI_ICONS_LOCATION;

public class WindowColonyMap extends AbstractWindowSkeleton
{
    public static final int HIGHLIGHT_RANGE = 2;

    /**
     * Holds information about the map decorations that may be associated with each building.
     */
    public class MapDecoration
    {

        public ItemIcon icon;
        public Box rangeBox;
        public Box highlightBox;
        public Image statusImage;

        public MapDecoration(final ItemIcon icon, final Box rangeBox, final Box highlightBox, final Image statusImage)
        {
            this.icon = icon;
            this.rangeBox = rangeBox;
            this.highlightBox = highlightBox;
            this.statusImage = statusImage;
        }

    /**
     * Returns the red component of a color based on the percentage of building capacity used.
     *
     * @param pctFull the percentage of building capacity used
     * @return the red component of the color
     */
        public static int getRedFromRange(double pctFull)
        {
            int red;

            if (pctFull <= 0.5)
            {
                red = (int) (255 * (pctFull / 0.5));
            }
            else
            {
                red = 255;
            }

            return red;
        }


        /**
         * Returns the green component of a color based on the percentage of building capacity used.
         *
         * @param pctFull the percentage of building capacity used
         * @return the green component of the color
         */
        public static int getGreenFromRange(double pctFull)
        {
            int green;

            if (pctFull <= 0.5)
            {
                green = 255;
            }
            else
            {
                green = (int) (255 * (1 - (pctFull - 0.5) / 0.5));
            }

            return green;
        }
    }

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
     * If map is being accessed directly from town hall.
     */
    private final boolean atTownHall;

    /**
     * Colony data beeing currently displayed
     */
    private Map<ICitizenDataView, View>              citizens       = new HashMap<>();
    private Map<IBuildingView, MapDecoration>        buildings      = new HashMap<>();
    private Map<ColonyListMessage.ColonyInfo, View>  coloniesImages = new HashMap<>();
    private List<MinecraftMap>                       maps           = new ArrayList<>();

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
    public WindowColonyMap(final boolean atTownHall, final IBuildingView building)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.atTownHall = atTownHall;
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

        if (atTownHall)
        {
            registerButton(BUTTON_EXIT, () -> building.openGui(false));
        }
        else
        {
            findPaneByID(BUTTON_INVENTORY).hide();
            registerButton(BUTTON_EXIT, this::close);
        }
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
            for (Map.Entry<ICitizenDataView, View> entry : citizens.entrySet())
            {
                final EntityCitizen citizen = (EntityCitizen) building.getColony().getWorld().getEntity(entry.getKey().getEntityId());
                if (citizen != null)
                {
                    entry.getValue().setPosition(worldPosToUIPos(citizen.blockPosition()).getX(), worldPosToUIPos(citizen.blockPosition()).getZ());
                }
            }

            for (Map.Entry<IBuildingView, MapDecoration> building : buildings.entrySet())
            {
                if (building.getValue().rangeBox != null)
                {
                    if (building.getValue().icon.wasCursorInPane())
                    {
                        building.getValue().rangeBox.show();
                    }
                    else
                    {
                        building.getValue().rangeBox.hide();
                    }
                }
            }

            for (final Player player : Minecraft.getInstance().level.players())
            {
                if (building.getColony().isCoordInColony(Minecraft.getInstance().level, player.blockPosition()))
                {
                    Image playerImage = findPaneOfTypeByID(player.getStringUUID(), Image.class);
                    if (playerImage == null)
                    {
                        final ResourceLocation resourceLocation = OutOfJarResourceLocation.ofMinecraftSkin(Minecraft.getInstance(), Minecraft.getInstance().getUser().getGameProfile(), null);
                        if (resourceLocation == null)
                        {
                            continue;
                        }
                        playerImage = new Image();
                        playerImage.setID(player.getStringUUID());
                        playerImage.setImage(resourceLocation, 8,8,8,8);
                        playerImage.setSize(16,16);
                        dragView.addChild(playerImage);
                        PaneBuilders.tooltipBuilder().hoverPane(playerImage)
                            .append(Component.literal(player.getDisplayName().getString()))
                            .build();
                    }
                    playerImage.setPosition(worldPosToUIPos(player.blockPosition()).getX(), worldPosToUIPos(player.blockPosition()).getZ());
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

            for (Map.Entry<IBuildingView, MapDecoration> buildingEntry : buildings.entrySet())
            {
                buildingEntry.getValue().icon.off();
            }

            for (Map.Entry<ICitizenDataView, View> citizenEntry : citizens.entrySet())
            {
                citizenEntry.getValue().off();
            }
        }
        else
        {
            // Display small icons
            for (Map.Entry<IBuildingView, MapDecoration> buildingEntry : buildings.entrySet())
            {
                buildingEntry.getValue().icon.on();
            }

            for (Map.Entry<ICitizenDataView, View> citizenEntry : citizens.entrySet())
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
        if (this.atTownHall)
        {
            citizenImage.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/red_wax_home.png"), false);
            citizenImage.setSize(16, 16);
            citizenImage.setPosition(worldPosToUIPos(playerPos).getX(), worldPosToUIPos(playerPos).getZ());
            dragView.addChild(citizenImage);
        }
    }

    /**
     * Generate a list of icons for the buildings
     *
     * @param buildingView
     * @return
     */
    private void updateBuildingView(final IBuildingView buildingView)
    {
        MapDecoration buildingDecorations = buildings.get(buildingView);
        if (buildingDecorations == null)
        {
            ItemIcon uiBuilding = new ItemIcon();
            Box box = null;
            Box houseWithCapacity = null;
            Image statusImage = null;

            uiBuilding.setID(buildingView.getID().toShortString());
            uiBuilding.setSize(11 + buildingView.getBuildingLevel(), 11 + buildingView.getBuildingLevel());
            final ItemStack item = buildingView.getBuildingType().getBuildingBlock().asItem().getDefaultInstance();
            uiBuilding.setItem(item);
            dragView.addChild(uiBuilding);

            AbstractTextBuilder.TooltipBuilder tooltip = PaneBuilders.tooltipBuilder();
            tooltip.hoverPane(uiBuilding)
              .append(Component.translatable(buildingView.getBuildingDisplayName())).append(Component.literal(" : " + buildingView.getBuildingLevel()))
              .appendNL(Component.literal("Coordinates: " + buildingView.getID().getX() + "X, " + buildingView.getID().getZ() + "Z"))
              .appendNL(Component.literal("Citizens: " + (buildingView instanceof LivingBuildingView home ? home.getResidents().size() : buildingView.getAllAssignedCitizens().size())));

            for (int id : buildingView.getAllAssignedCitizens())
            {
                final ICitizenDataView dataView = building.getColony().getCitizen(id);
                if (dataView != null)
                {
                    tooltip.appendNL(Component.literal(dataView.getName()));
                }
            }

            ImmutableList<IRequest<?>> playerResolvedRequests = getPlayerResolvedRequestsForBuilding(buildingView);

            // If there are player resolved requests for this building, add the appropriate status icon, and a tooltip with the detail
            if (playerResolvedRequests.size() > 0)
            {
                statusImage = new Image();
                statusImage.setImage(new ResourceLocation("minecolonies:textures/icons/information.png"), false);
                statusImage.setSize(6, 6);
                final BlockPos uiPos = worldPosToUIPos(buildingView.getPosition());
                statusImage.setPosition(uiPos.getX() - 4, uiPos.getY() - 4);
                statusImage.setVisible(true);

                AbstractTextBuilder.TooltipBuilder statustip = PaneBuilders.tooltipBuilder();
                statustip.hoverPane(statusImage);

                statustip.append(Component.translatable(COLONYMAP_PLAYER_RESOLVED_REQUESTS));

                for (IRequest<?> request : playerResolvedRequests)
                {
                    int count = -1;
                    Component tipText = null;

                    if (request instanceof IStackBasedTask stackRequest)
                    {
                        count = stackRequest.getDisplayCount();
                        tipText = stackRequest.getDisplayPrefix();
                    }
                    else
                    {   
                        tipText = Component.literal(request.getLongDisplayString().getString().replace("§f", ""));
                    }
                    statustip.appendNL(tipText);
                    
                    if (count > 0)
                    {
                        statustip.append(Component.literal(": " + count));
                    }
                }

                dragView.addChild(statusImage);
                statustip.build();

                tooltip.appendNL(Component.translatable(COLONYMAP_PLAYER_RESOLVED_REQUESTS_COUNT, playerResolvedRequests.size()));
            }

            tooltip.build();
            uiBuilding.setVisible(true);

            // Highlight houses that have capacity for additional citizens.
            if (buildingView instanceof LivingBuildingView home && home.getResidents().size() < buildingView.getBuildingLevel() && buildingView.getRange() == 0)
            {
                double pctFull = Math.max(0, Math.min(1, (double) home.getResidents().size() / (double) buildingView.getBuildingLevel()));
                houseWithCapacity = new Box();
                final BlockPos uiPos = worldPosToUIPos(buildingView.getPosition());
                houseWithCapacity.setLineWidth(2);
                houseWithCapacity.setColor(MapDecoration.getRedFromRange(pctFull), MapDecoration.getGreenFromRange(pctFull), 33);
                houseWithCapacity.setPosition(uiPos.getX(), uiPos.getZ());
                houseWithCapacity.setSize(uiBuilding.getWidth(), uiBuilding.getHeight());
                dragView.addChild(houseWithCapacity);
            }


            if (buildingView.getRange() != 0)
            {
                final int range = buildingView.getRange();
                final BlockPos UIPos1 = worldPosToUIPos(buildingView.getPosition().offset(-range, 0, -range));
                final BlockPos UIPos2 = worldPosToUIPos(buildingView.getPosition().offset(range, 0, range));

                box = new Box();
                box.setLineWidth(2);
                box.setColor(255, 0, 0);
                box.setPosition(UIPos1.getX(), UIPos1.getZ());
                box.setSize(UIPos2.getX() - UIPos1.getX(), UIPos2.getZ() - UIPos1.getZ());
                box.hide();
                dragView.addChild(box);
            }
            dragView.addChild(uiBuilding);

            buildingDecorations = new MapDecoration(uiBuilding, box, houseWithCapacity, statusImage);
            this.buildings.put(buildingView, buildingDecorations);
        }

        final BlockPos newPos = worldPosToUIPos(buildingView.getID());
        buildingDecorations.icon.setPosition(newPos.getX(), newPos.getZ());
        if (buildingDecorations.rangeBox != null)
        {
            final int range = buildingView.getRange();
            final BlockPos UIPos1 = worldPosToUIPos(buildingView.getPosition().offset(-range, 0, -range));
            final BlockPos UIPos2 = worldPosToUIPos(buildingView.getPosition().offset(range, 0, range));
            buildingDecorations.rangeBox.setPosition(UIPos1.getX(), UIPos1.getZ());
            buildingDecorations.rangeBox.setSize(UIPos2.getX() - UIPos1.getX(), UIPos2.getZ() - UIPos1.getZ());
        }

        if (buildingDecorations.highlightBox != null)
        {
            final BlockPos UIPos1 = worldPosToUIPos(buildingView.getPosition());
            buildingDecorations.highlightBox.setPosition(UIPos1.getX(), UIPos1.getZ());
            buildingDecorations.highlightBox.setSize(buildingDecorations.icon.getWidth(), buildingDecorations.icon.getHeight());
        }

        if (buildingDecorations.statusImage != null)
        {
            final BlockPos UIPos1 = worldPosToUIPos(buildingView.getPosition());
            buildingDecorations.statusImage.setPosition(UIPos1.getX() - 4, UIPos1.getZ() - 4);
        }
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

                final ButtonImage citizenImage = new ButtonImage();
                citizenImage.setImage(((ISimpleModelType) IModelTypeRegistry.getInstance().getModelType(citizen.getModelType())).getTextureIcon(citizen), false);
                citizenImage.setSize(4, 4);
                citizenImage.setID("citizen: " + data.getId());
                registerButton(citizenImage.getID(), button -> {
                    HighlightManager.addHighlight("mapsearchtracking", "", new CitizenRenderData(citizen.getCivilianID(), HIGHLIGHT_QUEST_LOG_TRACKER_DURATION));
                    SoundUtils.playSuccessSound(mc.player, mc.player.blockPosition());
                });

                final AbstractTextBuilder.TooltipBuilder builder = PaneBuilders.tooltipBuilder().hoverPane(citizenView).paragraphBreak().append(citizen.getDisplayName());
                if (!data.getJob().isEmpty())
                {
                    citizenImage.setSize(8, 8);
                    builder.newLine().append(Component.translatable("com.minecolonies.coremod.gui.citizen.job.label", LanguageHandler.format(data.getJob())));
                }
                citizenView.setSize(citizenImage.getWidth(), citizenImage.getHeight());
                citizenView.addChild(citizenImage);
                dragView.addChild(citizenView);

                if (data.hasVisibleStatus())
                {
                    final Image interactionImage = new Image();
                    interactionImage.setImage(data.getStatusIcon(), false);
                    interactionImage.setSize(6, 6);
                    interactionImage.setPosition(-3, -3);
                    citizenView.addChild(interactionImage);
                    citizenView.setSize(citizenView.getWidth() + 6, citizenView.getHeight() + 6);
                    if (data.hasBlockingInteractions())
                    {
                        final MutableComponent inquiry = (MutableComponent) data.getOrderedInteractions().get(0).getInquiry();
                        builder.newLine().append(inquiry.withStyle(ChatFormatting.DARK_RED));
                    }
                }

                if (data.isSick())
                {
                    final Image sickIcon = new Image();
                    sickIcon.setImage(new ResourceLocation("minecolonies:textures/icons/small_sick_icon.png"), false);
                    sickIcon.setSize(4, 8);
                    sickIcon.setPosition(3,-3);
                    citizenView.addChild(sickIcon);
                    citizenView.setSize(citizenView.getWidth() + 6, citizenView.getHeight() + 6);
                }
                else if (data.getSaturation() < LOW_SATURATION)
                {
                    final Image saturationIcon = new Image();
                    saturationIcon.setImage(GUI_ICONS_LOCATION,
                            EMPTY_SATURATION_ITEM_ROW_POS,
                            SATURATION_ICON_COLUMN, HEART_ICON_HEIGHT_WIDTH, HEART_ICON_HEIGHT_WIDTH);
                    saturationIcon.setMapDimensions(256, 256);
                    saturationIcon.setSize(SATURATION_ICON_HEIGHT_WIDTH, SATURATION_ICON_HEIGHT_WIDTH);
                    saturationIcon.setPosition(3, -3);
                    citizenView.addChild(saturationIcon);
                    citizenView.setSize(citizenView.getWidth() + 6, citizenView.getHeight() + 6);
                }
                builder.color(COLOR_TEXT_FULFILLED).build();

                if (citizens.containsKey(data))
                {
                    dragView.removeChild(citizens.get(data));
                }
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

    /**
     * Retrieves the player resolved requests that match with a specified building view.
     * 
     * @param buildingView the building view to match requests against
     * @return an immutable list of the player resolved requests for the building, or an empty list if the building view or colony is null
     * or if there is no request manager or player resolver
     */
    public ImmutableList<IRequest<?>> getPlayerResolvedRequestsForBuilding(final IBuildingView buildingView)
    {
        if (buildingView == null || buildingView.getColony() == null) 
        {
            return ImmutableList.of();
        }

        // buildingView.getLocation() is null... is this a bug? Using the colony dimension to allow multi-dimensional matching against player requests.
        ILocation buildingLocation = new StaticLocation(buildingView.getPosition(), buildingView.getColony().getDimension());  

        final ArrayList<IRequest<?>> playerResolvedBuildingRequests = Lists.newArrayList();

        if (buildingView == null || buildingView.getColony() == null)
        {
            return ImmutableList.of();
        }

        final IRequestManager requestManager = buildingView.getColony().getRequestManager();
        if (requestManager == null)
        {
            return ImmutableList.of();
        }

        try
        {
            final IPlayerRequestResolver resolver = requestManager.getPlayerResolver();
            final Set<IToken<?>> assigned = new HashSet<>(resolver.getAllAssignedRequests());

            for (final IToken<?> assignedToken : assigned)
            {
                final IRequest<?> r = requestManager.getRequestForToken(assignedToken);
                if (r != null)
                {
                    if (r.getRequester().getLocation().equals(buildingLocation))
                    {
                        playerResolvedBuildingRequests.add(r);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Log.getLogger().warn("Exception trying to retrieve requests:", e);
            requestManager.reset();
            return ImmutableList.of();
        }

        return ImmutableList.copyOf(playerResolvedBuildingRequests);
    }

}
