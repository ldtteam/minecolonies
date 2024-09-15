package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.storage.ClientFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.colony.buildings.AbstractBuilding;
import com.minecolonies.core.network.messages.server.DecorationBuildRequestMessage;
import com.minecolonies.core.tileentities.TileEntityDecorationController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for a hut name entry.
 */
public class WindowDecorationController extends AbstractWindowSkeleton
{
    /**
     * Resource suffix of GUI xml file.
     */
    private static final String HUT_NAME_RESOURCE_SUFFIX = ":gui/windowdecorationcontroller.xml";

    /**
     * The building associated to the GUI.
     */
    private final TileEntityDecorationController controller;

    /**
     * The world the player of the GUI is in.
     */
    private final Level world = Minecraft.getInstance().level;

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b {@link AbstractBuilding}
     */
    public WindowDecorationController(final BlockPos b)
    {
        super(Constants.MOD_ID + HUT_NAME_RESOURCE_SUFFIX);
        this.controller = (TileEntityDecorationController) world.getBlockEntity(b);
        registerButton(BUTTON_BUILD, this::buildClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);

        findPaneOfTypeByID(LABEL_NAME, Text.class).setText(Component.literal(controller.getBlueprintPath()));

        final IColonyView view = IColonyManager.getInstance().getClosestColonyView(world, controller.getBlockPos());

        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        findPaneByID(BUTTON_REPAIR).hide();
        findPaneByID(BUTTON_BUILD).hide();

        if (view != null)
        {
            final Optional<IWorkOrderView> wo = view.getWorkOrders().stream().filter(w -> w.getLocation().equals(this.controller.getBlockPos())).findFirst();

            int level = Utils.getBlueprintLevel(controller.getBlueprintPath());
            if (wo.isPresent())
            {
                findPaneByID(BUTTON_BUILD).show();

                buttonBuild.setText(Component.translatableEscape(ACTION_CANCEL_BUILD));
                if (wo.get().getWorkOrderType() == WorkOrderType.REPAIR)
                {
                    buttonBuild.setText(Component.translatableEscape(ACTION_CANCEL_REPAIR));
                }
            }
            else
            {
                buttonBuild.setText(Component.translatableEscape(ACTION_UPGRADE));

                try
                {
                    final String cleanedPackName = this.controller.getPackName().replace(Minecraft.getInstance().player.getUUID().toString(), "");
                    ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(cleanedPackName,
                      StructurePacks.getStructurePack(cleanedPackName).getPath().resolve(this.controller.getBlueprintPath()), mc.level.registryAccess()), (blueprint -> {
                        if (blueprint != null)
                        {
                            final BlockState blockState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                            if (blockState.getBlock() == ModBlocks.blockDecorationPlaceholder.get())
                            {
                                findPaneByID(BUTTON_REPAIR).show();
                            }
                        }
                    })));

                    if (level != -1)
                    {
                        final String path = this.controller.getBlueprintPath().replace(level + ".blueprint", (level + 1) + ".blueprint");
                        ClientFutureProcessor.queueBlueprint(new ClientFutureProcessor.BlueprintProcessingData(StructurePacks.getBlueprintFuture(cleanedPackName,
                          StructurePacks.getStructurePack(cleanedPackName).getPath().resolve(path), mc.level.registryAccess()),
                          (blueprint -> {
                              if (blueprint != null)
                              {
                                  final BlockState blockState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                                  if (blockState.getBlock() == ModBlocks.blockDecorationPlaceholder.get())
                                  {
                                      findPaneByID(BUTTON_BUILD).show();
                                  }
                              }
                          })));
                    }
                }
                catch (final Exception ex)
                {
                    Log.getLogger().warn("Unable to retrieve blueprint");
                }
            }
        }
    }

    /**
     * When cancel is clicked.
     */
    private void cancelClicked()
    {
        close();
    }

    /**
     * On confirm button.
     */
    private void buildClicked()
    {
        final int level = Utils.getBlueprintLevel(this.controller.getBlueprintPath());

        final String path = controller.getBlueprintPath().replace(level + ".blueprint", (level + 1) + ".blueprint");

        close();
        new WindowBuildDecoration(controller.getBlockPos(),
          controller.getPackName(),
          path,
          controller.getRotationMirror(),
          builder -> new DecorationBuildRequestMessage(WorkOrderType.BUILD,
            controller.getBlockPos(),
            controller.getPackName(),
            path,
            Minecraft.getInstance().level.dimension(),
            controller.getRotationMirror(),
            builder)).open();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        close();
        new WindowBuildDecoration(controller.getBlockPos(),
          controller.getPackName(),
          controller.getBlueprintPath(),
          controller.getRotationMirror(),
          builder -> new DecorationBuildRequestMessage(WorkOrderType.REPAIR,
            controller.getBlockPos(),
            controller.getPackName(),
            controller.getBlueprintPath(),
            Minecraft.getInstance().level.dimension(),
            controller.getRotationMirror(),
            builder)).open();
    }
}
