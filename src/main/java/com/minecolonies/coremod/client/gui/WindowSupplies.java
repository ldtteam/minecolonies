package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.structurize.Network;
import com.ldtteam.structurize.blueprints.v1.Blueprint;
import com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.storage.*;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.render.worldevent.HighlightManager;
import com.minecolonies.coremod.items.ItemSupplyCampDeployer;
import com.minecolonies.coremod.items.ItemSupplyChestDeployer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Tuple;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDSTYLE_LEGACY_CAMP;
import static com.ldtteam.structurize.api.util.constant.Constants.GROUNDSTYLE_LEGACY_SHIP;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_WARNING_SUPPLY_BUILDING_ERROR;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_SUPPLY_BUILDING_BAD_BLOCKS;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_TOGGLE;
import static com.minecolonies.api.util.constant.WindowConstants.SUPPLIES_RESOURCE_SUFFIX;

/**
 * Adjust the supply tool window.
 */
public class WindowSupplies extends AbstractBlueprintManipulationWindow
{
    /**
     * Drop down list for section.
     */
    private final ScrollingList packList;

    /**
     * The list of packs that contain a supplycamp.
     */
    private static final List<Tuple<String, Blueprint>> matchingPacks = new ArrayList<>();

    /**
     *  The displayed boxes category
     */
    private static final String RENDER_BOX_CATEGORY = "placement";

    /**
     * The type that is currently being placed.
     */
    private static String type;

    /**
     * Create a new supply tool window.
     * @param pos the pos its initiated at.
     */
    public WindowSupplies(@Nullable final BlockPos pos, final String type)
    {
        super(Constants.MOD_ID + SUPPLIES_RESOURCE_SUFFIX, pos, (type.equals("supplycamp") ? GROUNDSTYLE_LEGACY_CAMP : GROUNDSTYLE_LEGACY_SHIP), "supplies");
        packList = findPaneOfTypeByID("packs", ScrollingList.class);
        registerButton(BUTTON_TOGGLE, this::switchPack);

        if (!type.equals(WindowSupplies.type))
        {
            HighlightManager.clearCategory(RENDER_BOX_CATEGORY);
            RenderingCache.removeBlueprint("supplies");
            matchingPacks.clear();
        }
        else if (!matchingPacks.isEmpty())
        {
            if (RenderingCache.getOrCreateBlueprintPreviewData("supplies").getBlueprint() == null)
            {
                RenderingCache.getOrCreateBlueprintPreviewData("supplies").setBlueprint(matchingPacks.get(0).getB());
            }
        }
        WindowSupplies.type = type;


        if (pos != null)
        {
            RenderingCache.getOrCreateBlueprintPreviewData("supplies").setPos(pos);
        }
        updatePlacementOptions(type);
    }

    /**
     * Add the matching pack to the list.
     * @param name the name of the pack.
     * @param blueprint the fitting blueprint.
     */
    private void addOption(final String name, final Blueprint blueprint)
    {
        if (matchingPacks.isEmpty())
        {
            if (RenderingCache.getOrCreateBlueprintPreviewData("supplies").getBlueprint() == null)
            {
                RenderingCache.getOrCreateBlueprintPreviewData("supplies").setBlueprint(blueprint);
            }
            matchingPacks.add(new Tuple<>(name, blueprint));
        }
        else
        {
            matchingPacks.add(new Tuple<>(name, blueprint));
            packList.refreshElementPanes();
        }
    }

    /**
     * Update the different supplycamp/ship options in the dropdown.
     * @param type the type (camp or ship).
     */
    public void updatePlacementOptions(final String type)
    {
        if (matchingPacks.isEmpty())
        {
            for (final StructurePackMeta packName : StructurePacks.getPackMetas())
            {
                ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(packName.getName(),
                  "decorations/supplies/" + type + ".blueprint"), (blueprint -> {
                    if (blueprint != null)
                    {
                        addOption(packName.getName(), blueprint);
                    }
                })));
            }
        }

        packList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return matchingPacks.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                final ButtonImage buttonImage = rowPane.findPaneOfTypeByID(BUTTON_TOGGLE, ButtonImage.class);
                buttonImage.setText(new TextComponent(matchingPacks.get(index).getA()));
                if (RenderingCache.getOrCreateBlueprintPreviewData("supplies").getBlueprint() != null && RenderingCache.getOrCreateBlueprintPreviewData("supplies").getBlueprint().equals(matchingPacks.get(index).getB()))
                {
                    buttonImage.disable();
                }
                else
                {
                    buttonImage.enable();
                }
            }
        });
    }

    /**
     * Select a pack.
     * @param button the clicked button.
     */
    private void switchPack(final Button button)
    {
        RenderingCache.getOrCreateBlueprintPreviewData("supplies").setBlueprint(matchingPacks.get(this.packList.getListElementIndexByPane(button)).getB());
        adjustToGroundOffset();
    }

    @Override
    protected void cancelClicked()
    {
        HighlightManager.clearCategory(RENDER_BOX_CATEGORY);
        RenderingCache.removeBlueprint("supplies");
        close();
    }

    @Override
    protected void confirmClicked()
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("supplies");
        if (previewData.getBlueprint() == null)
        {
            return;
        }

        String pack = "";
        for (final Tuple<String, Blueprint> element : matchingPacks)
        {
            if (element.getB().equals(previewData.getBlueprint()))
            {
                pack = element.getA();
            }
        }

        if (pack.isEmpty())
        {
            return;
        }

        final List<PlacementError> placementErrorList = new ArrayList<>();
        if (type.equals("supplycamp"))
        {
            if (ItemSupplyCampDeployer.canCampBePlaced(Minecraft.getInstance().level, RenderingCache.getOrCreateBlueprintPreviewData("supplies").getPos(),
              placementErrorList,
              Minecraft.getInstance().player))
            {
                Network.getNetwork()
                  .sendToServer(new BuildToolPlacementMessage(BuildToolPlacementMessage.HandlerType.Survival,
                    Constants.MOD_ID,
                    pack,
                      StructurePacks.getStructurePack(pack).getSubPath(previewData.getBlueprint().getFilePath().resolve(previewData.getBlueprint().getFileName() + ".blueprint")),
                    previewData.getPos(),
                    previewData.getRotation(),
                    previewData.getMirror()));
                cancelClicked();
                return;
            }
        }
        else
        {
            if (ItemSupplyChestDeployer.canShipBePlaced(Minecraft.getInstance().level, RenderingCache.getOrCreateBlueprintPreviewData("supplies").getPos(),
              previewData.getBlueprint(),
              placementErrorList,
              Minecraft.getInstance().player))
            {
                Network.getNetwork()
                  .sendToServer(new BuildToolPlacementMessage(BuildToolPlacementMessage.HandlerType.Survival,
                    Constants.MOD_ID,
                    pack,
                    StructurePacks.getStructurePack(pack).getSubPath(previewData.getBlueprint().getFilePath().resolve(previewData.getBlueprint().getFileName() + ".blueprint")),
                    previewData.getPos(),
                    previewData.getRotation(),
                    previewData.getMirror()));
                cancelClicked();
                return;
            }
        }

        HighlightManager.clearCategory(RENDER_BOX_CATEGORY);
        if (!placementErrorList.isEmpty())
        {
            MessageUtils.format(WARNING_SUPPLY_BUILDING_BAD_BLOCKS).sendTo(Minecraft.getInstance().player);

            for (final PlacementError error : placementErrorList)
            {
                HighlightManager.addRenderBox(RENDER_BOX_CATEGORY, new HighlightManager.TimedBoxRenderData()
                  .setPos(error.getPos())
                  .setRemovalTimePoint(Minecraft.getInstance().level.getGameTime() + 120 * 20 * 60)
                  .addText(new TranslatableComponent(PARTIAL_WARNING_SUPPLY_BUILDING_ERROR + error.getType().toString().toLowerCase()).getString())
                  .setColor(0xFF0000));
            }
        }
    }
}
