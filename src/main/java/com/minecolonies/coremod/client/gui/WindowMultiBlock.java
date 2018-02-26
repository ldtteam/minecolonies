package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.MultiBlockChangeMessage;
import com.minecolonies.coremod.tileentities.TileEntityMultiBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.coremod.tileentities.TileEntityMultiBlock.*;
import static net.minecraft.util.EnumFacing.*;

/**
 * BuildTool window.
 */
public class WindowMultiBlock extends AbstractWindowSkeleton
{
    /**
     * Position of the multiblock.
     */
    private final BlockPos pos;

    /**
     * The direction it is facing.
     */
    private EnumFacing facing = UP;

    /**
     * The player which opened the GUI.
     */
    private final EntityPlayer player = Minecraft.getMinecraft().player;

    /**
     * The input field with the range.
     */
    private final TextField input;

    /**
     * Creates a window build tool for a specific structure.
     * @param pos the position.
     * @param structureName the structure name.
     * @param rotation the rotation.
     * @param mode the mode.
     */
    public WindowMultiBlock(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + MULTI_BLOCK_RESOURCE_SUFFIX);
        this.pos = pos;
        input = findPaneOfTypeByID(INPUT_RANGE_NAME, TextField.class);
        this.init();
    }

    private void init()
    {
        //Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_UP, this::moveUpClicked);
        registerButton(BUTTON_DOWN, this::moveDownClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        final TileEntity block = Minecraft.getMinecraft().world.getTileEntity(pos);
        if (block instanceof TileEntityMultiBlock)
        {
            input.setText(Integer.toString(((TileEntityMultiBlock) block).getRange()));

            switch (((TileEntityMultiBlock) block).getDirection())
            {
                case UP:
                    moveUpClicked();
                    break;
                case DOWN:
                    moveDownClicked();
                    break;
                case NORTH:
                    moveForwardClicked();
                    break;
                case SOUTH:
                    moveBackClicked();
                    break;
                case EAST:
                    moveRightClicked();
                    break;
                case WEST:
                    moveLeftClicked();
                    break;
            }
            return;
        }
        close();
    }

    /*
     * ---------------- Button Handling -----------------
     */

    private void enableAll()
    {
        findPaneOfTypeByID(BUTTON_UP, Button.class).enable();
        findPaneOfTypeByID(BUTTON_DOWN, Button.class).enable();
        findPaneOfTypeByID(BUTTON_LEFT, Button.class).enable();
        findPaneOfTypeByID(BUTTON_FORWARD, Button.class).enable();
        findPaneOfTypeByID(BUTTON_BACKWARD, Button.class).enable();
        findPaneOfTypeByID(BUTTON_RIGHT, Button.class).enable();
    }

    /**
     * Move the schematic up.
     */
    private void moveUpClicked()
    {
        facing = UP;
        enableAll();
        findPaneOfTypeByID(BUTTON_UP, Button.class).disable();
    }

    /**
     * Move the structure down.
     */
    private void moveDownClicked()
    {
        facing = DOWN;
        enableAll();
        findPaneOfTypeByID(BUTTON_DOWN, Button.class).disable();
    }

    /**
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        facing = WEST;
        enableAll();
        findPaneOfTypeByID(BUTTON_LEFT, Button.class).disable();
    }
    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        facing = NORTH;
        enableAll();
        findPaneOfTypeByID(BUTTON_FORWARD, Button.class).disable();
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        facing = SOUTH;
        enableAll();
        findPaneOfTypeByID(BUTTON_BACKWARD, Button.class).disable();
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        facing = EAST;
        enableAll();
        findPaneOfTypeByID(BUTTON_RIGHT, Button.class).disable();
    }

    /**
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        final String inputText = input.getText();
        int range = DEFAULT_RANGE;
        try
        {
            range = Integer.valueOf(inputText);
        }
        catch(final NumberFormatException e)
        {
            Log.getLogger().warn("Unable to parse number for MultiBlock range, considering default range!", e);
        }
        MineColonies.getNetwork().sendToServer(new MultiBlockChangeMessage(pos, facing, range));
        close();
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        close();
    }
}
