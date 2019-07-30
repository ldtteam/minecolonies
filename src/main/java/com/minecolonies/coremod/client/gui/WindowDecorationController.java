package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.util.PlacementSettings;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.IColonyView;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.colony.workorders.WorkOrderType;
import com.minecolonies.coremod.colony.workorders.WorkOrderView;
import com.minecolonies.coremod.network.messages.DecorationBuildRequestMessage;
import com.minecolonies.coremod.network.messages.DecorationControllUpdateMessage;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for a hut name entry.
 */
public class WindowDecorationController extends AbstractWindowSkeleton implements ButtonHandler
{
    /**
     * The max length of the name.
     */
    private static final int MAX_NAME_LENGTH = 100;

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
    private final World world = Minecraft.getMinecraft().world;

    /**
     * If the player opening the GUI is an isCreative.
     */
    private boolean isCreative = Minecraft.getMinecraft().player.isCreative();

    /**
     * Constructor for a hut rename entry window.
     *
     * @param b {@link AbstractBuilding}
     */
    public WindowDecorationController(final BlockPos b)
    {
        super(Constants.MOD_ID + HUT_NAME_RESOURCE_SUFFIX);
        this.controller = (TileEntityDecorationController) world.getTileEntity(b);
        registerButton(BUTTON_BUILD, this::confirmClicked);
        registerButton(BUTTON_REPAIR, this::repairClicked);
        registerButton(BUTTON_DONE, this::doneClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);

        final TextField textFieldName = findPaneOfTypeByID(INPUT_NAME, TextField.class);
        textFieldName.setText(controller.getSchematicName());

        final TextField textFieldLevel = findPaneOfTypeByID(INPUT_LEVEL, TextField.class);
        textFieldLevel.setText(String.valueOf(controller.getLevel()));

        final IColonyView view = IColonyManager.getInstance().getClosestColonyView(world, controller.getPos());

        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);

        if (view != null)
        {
            final Optional<WorkOrderView> wo = view.getWorkOrders().stream().filter(w -> w.getPos().equals(this.controller.getPos())).findFirst();
            if (wo.isPresent())
            {

                if (wo.get().getType() == WorkOrderType.BUILD)
                {
                    if (controller.getLevel() == 0)
                    {
                        buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelBuild"));
                    }
                    else
                    {
                        buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelUpgrade"));
                    }
                    findPaneByID(BUTTON_REPAIR).hide();
                }
                else if (wo.get().getType() == WorkOrderType.BUILD)
                {
                    buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.cancelRepair"));
                    findPaneByID(BUTTON_REPAIR).hide();
                }
            }
        }

        if (controller.getLevel() == 0)
        {
            findPaneByID(BUTTON_REPAIR).hide();
        }

        com.ldtteam.structures.helpers.Structure structure = null;
        try
        {
            structure = new com.ldtteam.structures.helpers.Structure(world, controller.getSchematicName() + (controller.getLevel() + 1), new PlacementSettings());
        }
        catch (final Exception e)
        {
            Log.getLogger().info("Unable to load structure: " + controller.getSchematicName() + " for decoration controller!");
        }

        if (structure == null || structure.isBluePrintMissing())
        {
            findPaneByID(BUTTON_BUILD).hide();
        }

        if (!isCreative)
        {
            textFieldName.disable();
            textFieldLevel.disable();
            findPaneByID(BUTTON_DONE).hide();
        }
        else
        {
            findPaneOfTypeByID("nameLabel", Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.deco.namescan"));
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
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.coremod.gui.name.tooLong", name);
            }

            final String levelString = findPaneOfTypeByID(INPUT_LEVEL, TextField.class).getText();
            try
            {
                final int level = Integer.parseInt(levelString);
                MineColonies.getNetwork().sendToServer(new DecorationControllUpdateMessage(controller.getPos(), name, level));
                controller.setSchematicName(name);
                controller.setLevel(level);
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
        MineColonies.getNetwork()
          .sendToServer(new DecorationBuildRequestMessage(controller.getPos(), controller.getSchematicName(), controller.getLevel() + 1, world.provider.getDimension()));
        close();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        MineColonies.getNetwork()
          .sendToServer(new DecorationBuildRequestMessage(controller.getPos(), controller.getSchematicName(), controller.getLevel(), world.provider.getDimension()));
        close();
    }
}
