package com.minecolonies.coremod.client.gui.map;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.AbstractTextBuilder;
import com.ldtteam.blockout.controls.Image;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.View;
import com.ldtteam.blockout.views.ZoomDragView;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.client.render.modeltype.ISimpleModelType;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import com.minecolonies.coremod.entity.citizen.EntityCitizen;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.research.util.ResearchConstants.COLOR_TEXT_FULFILLED;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_EXIT;

public class WindowColonyMap extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/map/windowcolonymap.xml";

    private final BlockPos center;

    private final ZoomDragMap dragView;

    private Map<ICitizenDataView, Image> citizens  = new HashMap<>();
    private Map<IBuildingView, ItemIcon> buildings = new HashMap<>();

    private IBuildingView building;

    private double currentScale = 0;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param building The building the info window is for.
     */
    public WindowColonyMap(final IBuildingView building)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.building = building;
        center = new BlockPos(Minecraft.getInstance().player.blockPosition().getX(), 0, Minecraft.getInstance().player.blockPosition().getZ());
        final ZoomDragView parent = findPaneOfTypeByID("dragView", ZoomDragView.class);
        dragView = new ZoomDragMap();
        dragView.setSize(parent.getWidth(), parent.getHeight());
        dragView.setPosition(parent.getX(), parent.getY());
        dragView.enable();
        dragView.setVisible(true);
        dragView.setFocus();
        dragView.setWindow(this);
        parent.addChild(dragView);
        currentScale = dragView.getScale();

        registerButton(BUTTON_EXIT, () -> building.openGui(false));
        final List<IBuildingView> buildings = building.getColony().getBuildings();
        addIconsForBuildings(buildings);
        addCitizens(building.getColony());
        addCenterPos();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        for (Map.Entry<ICitizenDataView, Image> entry : citizens.entrySet())
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

    /**
     * Update elements to the new scaling factor
     */
    private void updateScale()
    {
        if (currentScale < 0.3)
        {
            // Hide small icons
            // show colony
            addColonyImage("small");
            addColonyImage("medium");
            addColonyImage("large");

            for (Map.Entry<IBuildingView, ItemIcon> buildingEntry : buildings.entrySet())
            {
                buildingEntry.getValue().off();
            }

            for (Map.Entry<ICitizenDataView, Image> citizenEntry : citizens.entrySet())
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

            for (Map.Entry<ICitizenDataView, Image> citizenEntry : citizens.entrySet())
            {
                citizenEntry.getValue().on();
            }
        }
    }

    int offset = 0;

    private void addColonyImage(final String size)
    {
        final View colonyPane = new View();
        Loader.createFromXMLFile("minecolonies:gui/map/colony" + size + ".xml", colonyPane);
        final Pane background = colonyPane.findPaneByID("background");
        colonyPane.setSize(background.getWidth(), background.getHeight());
        colonyPane.setPosition(worldPosToUIPos(center.offset(0, 0, offset)).getX() - colonyPane.getWidth() / 2,
          worldPosToUIPos(center.offset(0, 0, offset)).getZ() - colonyPane.getHeight() / 2);
        colonyPane.on();
        colonyPane.setID("" + building.getColony().getID());

        final Text colonyName = colonyPane.findPaneOfTypeByID("textcontent", Text.class);
        colonyName.setText(new StringTextComponent(building.getColony().getName()));
        offset += 200;
        dragView.addChild(colonyPane);
    }

    private void addCenterPos()
    {
        final Image citizenImage = new Image();
        citizenImage.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/red_wax_actions.png"));
        citizenImage.setSize(16, 16);
        citizenImage.setPosition(worldPosToUIPos(center).getX(), worldPosToUIPos(center).getZ());
        dragView.addChild(citizenImage);
    }

    /**
     * Generate a list of icons for the buildings
     *
     * @param buildings
     * @return
     */
    private void addIconsForBuildings(final List<IBuildingView> buildings)
    {
        final List<ItemIcon> buildingIcons = new ArrayList<>();
        for (final IBuildingView buildingView : buildings)
        {
            final ItemIcon icon = new ItemIcon();
            icon.setID(buildingView.getID().toShortString());
            icon.setSize(11 + buildingView.getBuildingLevel(), 11 + buildingView.getBuildingLevel());
            final ItemStack item = buildingView.getBuildingType().getBuildingBlock().asItem().getDefaultInstance();
            icon.setItem(item);
            icon.setPosition(worldPosToUIPos(buildingView.getID()).getX(), worldPosToUIPos(buildingView.getID()).getZ());
            icon.setVisible(true);
            dragView.addChild(icon);
            dragView.removeChild(this.buildings.get(buildingView));
            this.buildings.put(buildingView, icon);
        }
    }

    private void addCitizens(final IColonyView colony)
    {
        for (final ICitizenDataView data : colony.getCitizens().values())
        {
            final EntityCitizen citizen = (EntityCitizen) colony.getWorld().getEntity(data.getEntityId());
            if (citizen != null)
            {
                final Image citizenImage = new Image();
                citizenImage.setImage(((ISimpleModelType) citizen.getModelType()).getTextureIcon(citizen));
                citizenImage.setSize(4, 4);
                citizenImage.setPosition(worldPosToUIPos(citizen.blockPosition()).getX(), worldPosToUIPos(citizen.blockPosition()).getZ());
                dragView.addChild(citizenImage);
                final AbstractTextBuilder.TooltipBuilder builder = PaneBuilders.tooltipBuilder().hoverPane(citizenImage).paragraphBreak().append(citizen.getDisplayName());
                if (!data.getJob().equals(""))
                {
                    citizenImage.setSize(8, 8);
                    builder.newLine().append(new TranslationTextComponent("com.minecolonies.coremod.gui.citizen.job.label", LanguageHandler.format(data.getJob())));
                }
                builder.color(COLOR_TEXT_FULFILLED).build();

                dragView.removeChild(citizens.get(data));
                citizens.put(data, citizenImage);
            }
        }
    }

    private BlockPos worldPosToUIPos(final BlockPos worldPos)
    {
        return new BlockPos(
          dragView.getWidth() / 2 + (center.getX() - worldPos.getX()) * currentScale * currentScale,
          0,
          dragView.getHeight() / 2 + (center.getZ() - worldPos.getZ()) * currentScale * currentScale);
    }
}
