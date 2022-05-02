package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.controls.TextField;
import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.workorders.IWorkOrderView;
import com.minecolonies.api.colony.workorders.WorkOrderType;
import com.minecolonies.api.util.LoadOnlyStructureHandler;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.network.messages.server.DecorationBuildRequestMessage;
import com.minecolonies.coremod.network.messages.server.DecorationControllerUpdateMessage;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.Optional;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * BOWindow for a hut name entry.
 */
public class WindowDecorationController extends AbstractWindowSkeleton
{
    /**
     * The max length of the name.
     */
    private static final int MAX_NAME_LENGTH = 200;

    /**
     * Resource suffix of GUI xml file.
     */
    private static final String HUT_NAME_RESOURCE_SUFFIX = ":gui/windowdecorationcontroller.xml";

    /**
     * The id of the level input.
     */
    private static final String INPUT_LEVEL = "level";

    /**
     * The building associated to the GUI.
     */
    private final TileEntityDecorationController controller;

    /**
     * The world the player of the GUI is in.
     */
    private final Level world = Minecraft.getInstance().level;

    /**
     * If the player opening the GUI is an isCreative.
     */
    private boolean isCreative = Minecraft.getInstance().player.isCreative();

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b {@link AbstractBuilding}
     */
    public WindowDecorationController(final BlockPos b)
    {
        super(Constants.MOD_ID + HUT_NAME_RESOURCE_SUFFIX);
        this.controller = (TileEntityDecorationController) world.getBlockEntity(b);
        registerButton(BUTTON_BUILD, this::confirmClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_DONE, this::doneClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);

        final TextField textFieldName = findPaneOfTypeByID(INPUT_NAME, TextField.class);
        textFieldName.setText(controller.getSchematicPath().replaceAll("\\d$", ""));

        final TextField textFieldLevel = findPaneOfTypeByID(INPUT_LEVEL, TextField.class);
        textFieldLevel.setText(String.valueOf(controller.getTier()));

        final IColonyView view = IColonyManager.getInstance().getClosestColonyView(world, controller.getBlockPos());

        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);

        if (view != null)
        {
            final Optional<IWorkOrderView> wo = view.getWorkOrders().stream().filter(w -> w.getLocation().equals(this.controller.getBlockPos())).findFirst();
            if (wo.isPresent())
            {
                if (wo.get().getWorkOrderType() == WorkOrderType.BUILD)
                {
                    if (controller.getTier() == 0)
                    {
                        buttonBuild.setText(new TranslatableComponent(ACTION_CANCEL_BUILD));
                    }
                    else
                    {
                        buttonBuild.setText(new TranslatableComponent(ACTION_CANCEL_UPGRADE));
                    }
                    findPaneByID(BUTTON_REPAIR).hide();
                }
                else if (wo.get().getWorkOrderType() == WorkOrderType.BUILD)
                {
                    buttonBuild.setText(new TranslatableComponent(ACTION_CANCEL_REPAIR));
                    findPaneByID(BUTTON_REPAIR).hide();
                }
            }
        }

        if (controller.getTier() == 0)
        {
            findPaneByID(BUTTON_REPAIR).hide();
        }

        LoadOnlyStructureHandler structure = null;
        try
        {
            final String structureName = controller.getSchematicPath().replace("/structurize/", "").replaceAll("\\d$", "");
            structure =
              new LoadOnlyStructureHandler(world, b, structureName + (controller.getTier() + 1), new PlacementSettings(), true);
        }
        catch (final Exception e)
        {
            Log.getLogger().info("Unable to load structure: " + controller.getSchematicPath() + " for decoration controller!");
        }

        findPaneByID(LABEL_NO_UPGRADE).hide();
        if (structure == null || !structure.hasBluePrint())
        {
            findPaneByID(BUTTON_BUILD).hide();
            findPaneByID(LABEL_NO_UPGRADE).show();
        }

        if (!isCreative)
        {
            textFieldName.disable();
            textFieldLevel.disable();
            findPaneByID(BUTTON_DONE).hide();
        }
        else
        {
            findPaneOfTypeByID("nameLabel", Text.class).setText(new TranslatableComponent(WARNING_DECORATION_NAME_SCAN));
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
     * Done is clicked to save the new settings.
     */
    private void doneClicked()
    {
        if (isCreative)
        {
            String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();

            if (name.length() > MAX_NAME_LENGTH)
            {
                name = name.substring(0, MAX_NAME_LENGTH);
                MessageUtils.format(WARNING_NAME_TOO_LONG, name).sendTo(Minecraft.getInstance().player);
            }

            final String levelString = findPaneOfTypeByID(INPUT_LEVEL, TextField.class).getText();
            try
            {
                final int level = Integer.parseInt(levelString);
                Network.getNetwork().sendToServer(new DecorationControllerUpdateMessage(controller.getBlockPos(), name, level));
                controller.setSchematicPath(name + level);
                controller.setTier(level);
                close();
            }
            catch (final NumberFormatException ex)
            {
                Log.getLogger().warn("Error parsing number: " + levelString, ex);
            }
        }
    }

    /**
     * On confirm button.
     */
    private void confirmClicked()
    {
        Network.getNetwork().sendToServer(new DecorationBuildRequestMessage(controller.getBlockPos(),
          controller.getSchematicName()
            .substring(controller.getSchematicName().lastIndexOf("/") + 1)
            .replaceAll("\\d$", ""),
          controller.getSchematicPath()
            .replaceAll("\\d$", ""),
          controller.getTier() + 1,
          world.dimension()));
        close();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        Network.getNetwork().sendToServer(new DecorationBuildRequestMessage(controller.getBlockPos(),
          controller.getSchematicName()
            .substring(controller.getSchematicName().lastIndexOf("/") + 1)
            .replaceAll("\\d$", ""),
          controller.getSchematicPath()
            .replaceAll("\\d$", ""),
          controller.getTier(),
          world.dimension()));
        close();
    }
}
