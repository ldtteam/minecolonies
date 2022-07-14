package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.structurize.storage.ClientBlueprintFutureProcessor;
import com.ldtteam.structurize.storage.StructurePacks;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.Utils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.network.messages.server.DecorationBuildRequestMessage;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        findPaneOfTypeByID(LABEL_NAME, Text.class).setText(new TextComponent(controller.getSchematicName()));

        final IColonyView view = IColonyManager.getInstance().getClosestColonyView(world, controller.getBlockPos());

        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        findPaneByID(BUTTON_REPAIR).hide();
        findPaneByID(BUTTON_BUILD).hide();

        if (view != null)
        {
            final Optional<IWorkOrderView> wo = view.getWorkOrders().stream().filter(w -> w.getLocation().equals(this.controller.getBlockPos())).findFirst();

            int level = Utils.getBlueprintLevel(controller.getSchematicName()) -1;
            if (wo.isPresent())
            {
                findPaneByID(BUTTON_BUILD).show();

                buttonBuild.setText(new TranslatableComponent(ACTION_CANCEL_BUILD));
                if (wo.get().getWorkOrderType() == WorkOrderType.REPAIR)
                {
                    buttonBuild.setText(new TranslatableComponent(ACTION_CANCEL_REPAIR));
                }
            }
            else
            {
                buttonBuild.setText(new TranslatableComponent(ACTION_BUILD));

                ClientBlueprintFutureProcessor.consumerQueue.add(new ClientBlueprintFutureProcessor.ProcessingData(StructurePacks.getBlueprintFuture(this.controller.getPackName(), this.controller.getSchematicPath()), (blueprint -> {
                    if (blueprint != null )
                    {
                        final BlockState blockState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                        if (blockState.getBlock() == ModBlocks.blockDecorationPlaceholder)
                        {
                            findPaneByID(BUTTON_REPAIR).show();
                        }
                    }
                })));

                if (level != -1)
                {
                    final String path = this.controller.getSchematicPath().replace(level + ".blueprint", (level + 1) + ".blueprint");
                    ClientBlueprintFutureProcessor.consumerQueue.add(new ClientBlueprintFutureProcessor.ProcessingData(StructurePacks.getBlueprintFuture(this.controller.getPackName(), path),
                      (blueprint -> {
                        if (blueprint != null)
                        {
                            final BlockState blockState = blueprint.getBlockState(blueprint.getPrimaryBlockOffset());
                            if (blockState.getBlock() == ModBlocks.blockDecorationPlaceholder)
                            {
                                findPaneByID(BUTTON_BUILD).show();
                            }
                        }
                    })));
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
        final int level = Utils.getBlueprintLevel(this.controller.getSchematicName());
        Network.getNetwork().sendToServer(new DecorationBuildRequestMessage(WorkOrderType.BUILD,
          controller.getBlockPos(),
          controller.getPackName(),
          controller.getBlueprintPath().replace(level + ".blueprint", (level + 1) + ".blueprint"),
          world.dimension(),
          controller.getRotation(),
          controller.getMirror()));
        close();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        Network.getNetwork().sendToServer(new DecorationBuildRequestMessage(WorkOrderType.BUILD,
          controller.getBlockPos(),
          controller.getPackName(),
          controller.getSchematicPath(),
          world.dimension(),
          controller.getRotation(),
          controller.getMirror()));
        close();
    }
}
