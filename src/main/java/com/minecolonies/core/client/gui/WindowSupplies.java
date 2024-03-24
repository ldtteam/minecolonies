package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.client.gui.AbstractBlueprintManipulationWindow;
import com.ldtteam.structurize.client.gui.WindowSwitchPack;
import com.ldtteam.structurize.network.messages.BuildToolPlacementMessage;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.StructurePackMeta;
import com.ldtteam.structurize.storage.StructurePacks;
import com.ldtteam.structurize.storage.rendering.RenderingCache;
import com.ldtteam.structurize.storage.rendering.types.BlueprintPreviewData;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.client.render.worldevent.HighlightManager;
import com.minecolonies.core.client.render.worldevent.highlightmanager.TimedBoxRenderData;
import com.minecolonies.core.items.ItemSupplyCampDeployer;
import com.minecolonies.core.items.ItemSupplyChestDeployer;
import com.minecolonies.core.placementhandlers.main.SuppliesHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.ldtteam.structurize.api.constants.Constants.GROUNDSTYLE_LEGACY_CAMP;
import static com.ldtteam.structurize.api.constants.Constants.GROUNDSTYLE_LEGACY_SHIP;
import static com.ldtteam.structurize.api.constants.GUIConstants.BUTTON_SWITCH_STYLE;
import static com.minecolonies.api.util.constant.TranslationConstants.PARTIAL_WARNING_SUPPLY_BUILDING_ERROR;
import static com.minecolonies.api.util.constant.TranslationConstants.WARNING_SUPPLY_BUILDING_BAD_BLOCKS;
import static com.minecolonies.api.util.constant.WindowConstants.SUPPLIES_RESOURCE_SUFFIX;

/**
 * Adjust the supply tool window.
 */
public class WindowSupplies extends AbstractBlueprintManipulationWindow
{
    /**
     *  The displayed boxes category
     */
    private static final String RENDER_BOX_CATEGORY = "placement";

    /**
     * The type that is currently being placed.
     */
    private static String type;

    /**
     * Current selected structure pack.
     */
    private static StructurePackMeta structurePack = null;

    /**
     * Create a new supply tool window.
     *
     * @param pos        the pos its initiated at.
     * @param itemInHand
     */
    public WindowSupplies(@Nullable final BlockPos pos, final String type)
    {
        super(Constants.MOD_ID + SUPPLIES_RESOURCE_SUFFIX, pos, (type.equals("supplycamp") ? GROUNDSTYLE_LEGACY_CAMP : GROUNDSTYLE_LEGACY_SHIP), "supplies");
        registerButton(BUTTON_SWITCH_STYLE, this::switchPackClicked);

        if (!type.equals(WindowSupplies.type))
        {
            HighlightManager.clearHighlightsForKey(RENDER_BOX_CATEGORY);
            RenderingCache.removeBlueprint("supplies");
        }
        WindowSupplies.type = type;

        if (pos != null)
        {
            RenderingCache.getOrCreateBlueprintPreviewData("supplies").setPos(pos);
        }

        if (RenderingCache.getOrCreateBlueprintPreviewData("supplies").getBlueprint() == null)
        {
            loadBlueprint();
        }
    }

    /**
     * Opens the switch style window.
     */
    private void switchPackClicked()
    {
        new WindowSwitchPack(() ->
        {
            final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("supplies");
            previewData.setBlueprint(null);
            return new WindowSupplies(previewData.getPos(), type);
        }, pack -> Files.exists(pack.getPath().resolve("decorations/supplies/" + type + ".blueprint"))).open();
    }

    /**
     * Try to load the appropriate supply blueprint for the selected pack, if there is one
     */
    private void loadBlueprint()
    {
        RenderingCache.getOrCreateBlueprintPreviewData("supplies").setBlueprint(null);

        structurePack = StructurePacks.selectedPack;

        ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(
                StructurePacks.getBlueprintFuture(structurePack.getName(), "decorations/supplies/" + type + ".blueprint"),
                blueprint -> {
                    if (blueprint == null)
                    {
                        return;
                    }
                    RenderingCache.getOrCreateBlueprintPreviewData("supplies").setBlueprint(blueprint);
                    adjustToGroundOffset();
                    findPaneOfTypeByID("tip", Text.class).setVisible(false);
                }));
    }

    @Override
    protected void cancelClicked()
    {
        HighlightManager.clearHighlightsForKey(RENDER_BOX_CATEGORY);
        RenderingCache.removeBlueprint("supplies");
        close();
    }

    @Override
    protected void confirmClicked()
    {
        handlePlacement(BuildToolPlacementMessage.HandlerType.Survival, SuppliesHandler.ID);
    }

    @Override
    protected void handlePlacement(final BuildToolPlacementMessage.HandlerType handlerType, final String handlerId)
    {
        final BlueprintPreviewData previewData = RenderingCache.getOrCreateBlueprintPreviewData("supplies");
        if (structurePack == null || previewData.getBlueprint() == null)
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
                new BuildToolPlacementMessage(handlerType, handlerId,
                          structurePack.getName(),
                          structurePack.getSubPath(previewData.getBlueprint().getFilePath().resolve(previewData.getBlueprint().getFileName() + ".blueprint")),
                    previewData.getPos(),
                    previewData.getRotationMirror()).sendToServer();
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
                new BuildToolPlacementMessage(handlerType, handlerId,
                          structurePack.getName(),
                          structurePack.getSubPath(previewData.getBlueprint().getFilePath().resolve(previewData.getBlueprint().getFileName() + ".blueprint")),
                    previewData.getPos(),
                    previewData.getRotationMirror()).sendToServer();
                cancelClicked();
                return;
            }
        }

        HighlightManager.clearHighlightsForKey(RENDER_BOX_CATEGORY);
        if (!placementErrorList.isEmpty())
        {
            MessageUtils.format(WARNING_SUPPLY_BUILDING_BAD_BLOCKS).sendTo(Minecraft.getInstance().player);

            int i = 0;
            for (final PlacementError error : placementErrorList)
            {
                HighlightManager.addHighlight(RENDER_BOX_CATEGORY + i++, new TimedBoxRenderData(error.getPos())
                  .addText(Component.translatableEscape(PARTIAL_WARNING_SUPPLY_BUILDING_ERROR + error.getType().toString().toLowerCase()).getString())
                  .setColor(0x80FF0000)
                  .setDuration(Duration.ofSeconds(60)));
            }
        }
    }
}
