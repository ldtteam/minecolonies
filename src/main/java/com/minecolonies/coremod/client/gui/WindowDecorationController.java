package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.buildings.AbstractBuilding;
import com.minecolonies.coremod.network.messages.DecorationBuildRequestMessage;
import com.minecolonies.coremod.tileentities.TileEntityDecorationController;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
     * The building associated to the GUI.
     */
    private final TileEntityDecorationController controller;

    /**
     * The world the player of the GUI is in.
     */
    private final World world = Minecraft.getMinecraft().world;

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
        findPaneOfTypeByID(LEVEL_LABEL, Label.class).setLabelText(LanguageHandler.format("com.minecolonies.coremod.gui.deco.level") + " " + controller.getLevel());
        final Button buttonBuild = findPaneOfTypeByID(BUTTON_BUILD, Button.class);
        if (controller.getLevel() == 0)
        {
            findPaneByID(BUTTON_REPAIR).hide();
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.build"));
        }
        else
        {
            buttonBuild.setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.upgrade"));
        }
    }

    /**
     * Done is clicked to save the new settings.
     */
    private void doneClicked()
    {
        String name = findPaneOfTypeByID(INPUT_NAME, TextField.class).getText();

        if (name.length() > MAX_NAME_LENGTH)
        {
            name = name.substring(0, MAX_NAME_LENGTH);
            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.coremod.gui.name.tooLong", name);
        }

        controller.setSchematicName(name);
        close();
    }

    /**
     * On confirm button.
     */
    private void confirmClicked()
    {
        MineColonies.getNetwork().sendToServer(new DecorationBuildRequestMessage(controller.getPos(), controller.getSchematicName(), controller.getLevel() + 1, world.provider.getDimension()));
        close();
    }

    /**
     * Action when repair button is clicked.
     */
    private void repairClicked()
    {
        MineColonies.getNetwork().sendToServer(new DecorationBuildRequestMessage(controller.getPos(), controller.getSchematicName(), controller.getLevel(), world.provider.getDimension()));
        close();
    }

    @Override
    public void onOpened()
    {
        findPaneOfTypeByID(INPUT_NAME, TextField.class).setText(controller.getSchematicName());
    }
}
