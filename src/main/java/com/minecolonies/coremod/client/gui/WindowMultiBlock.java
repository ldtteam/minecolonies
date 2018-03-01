package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.controls.ButtonImage;
import com.minecolonies.blockout.controls.TextField;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.network.messages.MultiBlockChangeMessage;
import com.minecolonies.coremod.tileentities.TileEntityMultiBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
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
     * Pre resource string.
     */
    private static final String RES_STRING = "textures/gui/buildtool/%s.png";

    /**
     * Green String for selected left click.
     */
    private static final String GREEN_POS = "_green";

    /**
     * Red String for selected right click.
     */
    private static final String RED_POS = "_red";

    /**
     * Position of the multiblock.
     */
    private final BlockPos pos;

    /**
     * The direction it is facing.
     */
    private EnumFacing facing = UP;

    /**
     * The output direction.
     */
    private EnumFacing output = DOWN;

    /**
     * The input field with the range.
     */
    private final TextField inputRange;

    /**
     * The input field with the range.
     */
    private final TextField inputSpeed;

    /**
     * The constructor called before opening this window.
     *
     * @param pos the position of the TileEntity which this window belogs to.
     */
    public WindowMultiBlock(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + MULTI_BLOCK_RESOURCE_SUFFIX);
        this.pos = pos;
        inputRange = findPaneOfTypeByID(INPUT_RANGE_NAME, TextField.class);
        inputSpeed = findPaneOfTypeByID(INPUT_SPEED, TextField.class);
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
            inputRange.setText(Integer.toString(((TileEntityMultiBlock) block).getRange()));
            inputSpeed.setText(Integer.toString(((TileEntityMultiBlock) block).getSpeed()));
            final EnumFacing dir = ((TileEntityMultiBlock) block).getDirection();
            final EnumFacing out = ((TileEntityMultiBlock) block).getOutput();
            enable(dir, dir, false);
            enable(out, out, true);
            return;
        }
        close();
    }

    private void enable(final EnumFacing oldFacing, final EnumFacing newFacing, final boolean rightClick)
    {
        switch (oldFacing)
        {
            case DOWN:
                findPaneOfTypeByID(BUTTON_DOWN, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_DOWN)));
                break;
            case NORTH:
                findPaneOfTypeByID(BUTTON_FORWARD, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_FORWARD)));
                break;
            case SOUTH:
                findPaneOfTypeByID(BUTTON_BACKWARD, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_BACKWARD)));
                break;
            case EAST:
                findPaneOfTypeByID(BUTTON_RIGHT, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_RIGHT)));
                break;
            case WEST:
                findPaneOfTypeByID(BUTTON_LEFT, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_LEFT)));
                break;
            default:
                findPaneOfTypeByID(BUTTON_UP, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_UP)));
                break;
        }

        final String color = rightClick ? RED_POS : GREEN_POS;
        switch (newFacing)
        {
            case DOWN:
                findPaneOfTypeByID(BUTTON_DOWN, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_DOWN + color)));
                break;
            case NORTH:
                findPaneOfTypeByID(BUTTON_FORWARD, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_FORWARD + color)));
                break;
            case SOUTH:
                findPaneOfTypeByID(BUTTON_BACKWARD, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_BACKWARD + color)));
                break;
            case EAST:
                findPaneOfTypeByID(BUTTON_RIGHT, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_RIGHT + color)));
                break;
            case WEST:
                findPaneOfTypeByID(BUTTON_LEFT, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_LEFT + color)));
                break;
            default:
                findPaneOfTypeByID(BUTTON_UP, ButtonImage.class).setImage(new ResourceLocation(Constants.MOD_ID, String.format(RES_STRING, BUTTON_UP + color)));
                break;
        }

        if (rightClick)
        {
            output = newFacing;
        }
        else
        {
            facing = newFacing;
        }
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Move the schematic up.
     */
    private void moveUpClicked()
    {
        enable(facing, UP, false);
    }

    /**
     * Move the structure down.
     */
    private void moveDownClicked()
    {
        enable(facing, DOWN, false);
    }

    /**
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        enable(facing, WEST, false);
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        enable(facing, NORTH, false);
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        enable(facing, SOUTH, false);
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        enable(facing, EAST, false);
    }

    /**
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        int range = DEFAULT_RANGE;
        int speed = DEFAULT_SPEED;
        try
        {
            range = Integer.valueOf(inputRange.getText());
            speed = Integer.valueOf(inputSpeed.getText());
        }
        catch (final NumberFormatException e)
        {
            Log.getLogger().warn("Unable to parse number for MultiBlock range or speed, considering default range/speed!", e);
        }
        MineColonies.getNetwork().sendToServer(new MultiBlockChangeMessage(pos, facing, output, range, speed));
        close();
    }

    @Override
    public void rightClick(final int mx, final int my)
    {
        Pane pane = this.findPaneForClick(mx, my);
        if (pane instanceof View)
        {
            pane = ((View) pane).findPaneForClick(mx - pane.getX(), my - pane.getY());
        }
        if (pane instanceof ButtonImage && pane.isEnabled())
        {
            final EnumFacing newFacing;
            switch (pane.getID())
            {
                case BUTTON_UP:
                    newFacing = UP;
                    break;
                case BUTTON_DOWN:
                    newFacing = DOWN;
                    break;
                case BUTTON_FORWARD:
                    newFacing = NORTH;
                    break;
                case BUTTON_BACKWARD:
                    newFacing = SOUTH;
                    break;
                case BUTTON_RIGHT:
                    newFacing = EAST;
                    break;
                case BUTTON_LEFT:
                    newFacing = WEST;
                    break;
                default:
                    newFacing = UP;
                    break;
            }
            enable(output, newFacing, true);
        }
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        close();
    }
}
