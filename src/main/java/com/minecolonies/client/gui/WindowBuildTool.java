package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.Schematics;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildToolPlaceMessage;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Log;
import com.minecolonies.util.SchematicWrapper;
import com.schematica.Settings;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.util.RotationHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BuildTool Window
 *
 * @author Colton
 */
public class WindowBuildTool extends Window implements Button.Handler
{
    /*
    All buttons for the GUI
     */
    // Navigation buttons (selecting options)
    private static final    String          BUTTON_TYPE_ID              = "buildingType";
    private static final    String          BUTTON_HUT_ID               = "hut";
    private static final    String          BUTTON_STYLE_ID             = "style";
    private static final    String          BUTTON_DECORATION_ID        = "decoration";

    // Navigation buttons (confirm, cancel)
    private static final    String          BUTTON_CONFIRM              = "confirm";
    private static final    String          BUTTON_CANCEL               = "cancel";

    //Rotating buttons (left, right)
    private static final    String          BUTTON_ROTATE_LEFT          = "rotateLeft";
    private static final    String          BUTTON_ROTATE_RIGHT         = "rotateRight";

    //Directional buttons (x, y, z)
    private static final    String          BUTTON_UP                   = "up";
    private static final    String          BUTTON_DOWN                 = "down";
    private static final    String          BUTTON_FORWARD              = "forward";
    private static final    String          BUTTON_BACK                 = "back";
    private static final    String          BUTTON_LEFT                 = "left";
    private static final    String          BUTTON_RIGHT                = "right";

    /*
    Resource suffix and hut prefix
    */
    private static final    String          BUILD_TOOL_RESOURCE_SUFFIX  = ":gui/windowBuildTool.xml";
    private static final    String          HUT_PREFIX                  = ":blockHut";

    /*
    List of buildings possible to make
     */
    private                 List<String>    huts                        = new ArrayList<>();
    /*
    Index of the rendered hut
     */
    private                 int             hutDecIndex                 = 0;
    /*
    Index of                 the             current style
     */
    private                 int             styleIndex                  = 0;

    //Position and rotation for the tool
    private BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
    private                 int             rotation                    = 0;

    /**
     * Creates a window build tool
     * This requires X, Y and Z coordinates
     * If a schematic is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used
     *
     * @param pos     coordinate
     */
    public WindowBuildTool(BlockPos pos)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);

        if(Settings.instance.getActiveSchematic() != null)
        {
            BlockPosUtil.set(this.pos, Settings.instance.offset.add(Settings.instance.getActiveSchematic().getOffset()));
            rotation = Settings.instance.rotation;
        }
        else
        {
            BlockPosUtil.set(this.pos, pos);
        }
    }

	@Override
    public void onOpened()
    {
        boolean inHutMode = true;
        if (!inHutMode)
        {
            Button type = findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class);
            type.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.decoration"));
            type.setEnabled(false);

            //TODO do stuff with decoration button
            return;
        }

        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.hut"));


        InventoryPlayer inventory = this.mc.thePlayer.inventory;

            /*
            Add possible huts (has item) to list, if it has a schematic, and player has the block
             */
        huts.addAll(Schematics.getHuts().stream().filter(hut -> inventory.hasItem(
                Block.getBlockFromName(Constants.MOD_ID + HUT_PREFIX + hut).getItem(null, new BlockPos(0, 0, 0)))
                                                                && Schematics.getStylesForHut(hut) != null).collect(
                Collectors.toList()));

        if (!huts.isEmpty())
        {
            if (Settings.instance.getActiveSchematic() != null)
            {
                hutDecIndex = Math.max(0, huts.indexOf(Settings.instance.hut));
                styleIndex = Math.max(0, Schematics.getStylesForHut(huts.get(hutDecIndex)).indexOf(Settings.instance.style));
            }

            Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);


            hut.setLabel(huts.get(hutDecIndex));
            hut.setEnabled(true);


            Button style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class);


            style.setVisible(true);
            style.setLabel(Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex));


            //Render stuff
            if (Settings.instance.getActiveSchematic() == null)
            {
                changeSchematic();
            }
        }
        else
        {
            Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);

            hut.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.nullHut"));
            hut.setEnabled(false);

            Settings.instance.setActiveSchematic(null);
        }
    }

    @Override
    public void onClosed()
    {
        if(Settings.instance.getActiveSchematic() != null)
        {
            Settings.instance.rotation = rotation;

            Settings.instance.hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).getLabel();
            Settings.instance.style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

        }
    }

    @Override
    public void onButtonClicked(Button button)
    {
        switch (button.getID())
        {
        case BUTTON_TYPE_ID:
            //TODO
            break;
        case BUTTON_HUT_ID:
            hutDecIndex = (hutDecIndex + 1) % huts.size();
            styleIndex = 0;

            findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).setLabel(huts.get(hutDecIndex));
            findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex));


            changeSchematic();
            break;
        case BUTTON_STYLE_ID:
            List<String> styles = Schematics.getStylesForHut(huts.get(hutDecIndex));
            styleIndex = (styleIndex + 1) % styles.size();
            try
            {
                findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(styles.get(styleIndex));
            }
            catch (NullPointerException e)
            {
                Log.logger.error("findPane error, report to mod authors", e);
            }
            changeSchematic();
            break;
        case BUTTON_DECORATION_ID:
            //TODO
            break;

        case BUTTON_CONFIRM:
            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(huts.get(hutDecIndex),
                    Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex), this.pos, rotation));
            Settings.instance.setActiveSchematic(null);
            close();
            break;
        case BUTTON_CANCEL:
            Settings.instance.setActiveSchematic(null);
            close();
            break;
        case BUTTON_LEFT:
        case BUTTON_RIGHT:
        case BUTTON_FORWARD:
        case BUTTON_BACK:
            moveArrow(button.getID());
            break;
        case BUTTON_UP:
            BlockPosUtil.set(pos, pos.up());
            updatePosition();
            break;
        case BUTTON_DOWN:
            BlockPosUtil.set(pos, pos.down());
            updatePosition();
            break;

        case BUTTON_ROTATE_LEFT:
            rotation = (rotation + 3) % 4;
            RotationHelper.INSTANCE.rotate(Settings.instance.schematic, EnumFacing.DOWN, true);
            updatePosition();
            break;
        case BUTTON_ROTATE_RIGHT:
            rotation = (rotation + 1) % 4;
            RotationHelper.INSTANCE.rotate(Settings.instance.schematic, EnumFacing.UP, true);
            updatePosition();
            break;

        default:
            Log.logger.warn("WindowBuildTool: Unhandled Button ID:" + button.getID());
        }
    }

    /**
     * Moves the pointer to a new position
     *
     * @param id    Button ID
     */
    private void moveArrow(String id)
    {
        int facing = MathHelper.floor_double((double) (this.mc.thePlayer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

        switch(id)
        {
        case BUTTON_LEFT:
            switch (facing)
            {
            case 0:
                BlockPosUtil.set(pos, pos.east());
                break;
            case 1:
                BlockPosUtil.set(pos, pos.south());
                break;
            case 2:
                BlockPosUtil.set(pos, pos.west());
                break;
            case 3:
                BlockPosUtil.set(pos, pos.north());
                break;
            }
            break;
        case BUTTON_RIGHT:
            switch (facing)
            {
            case 0:
                BlockPosUtil.set(pos, pos.west());
                break;
            case 1:
                BlockPosUtil.set(pos, pos.north());
                break;
            case 2:
                BlockPosUtil.set(pos, pos.east());
                break;
            case 3:
                BlockPosUtil.set(pos, pos.south());
                break;
            }
            break;
        case BUTTON_FORWARD:
            switch (facing)
            {
            case 0:
                BlockPosUtil.set(pos, pos.south());
                break;
            case 1:
                BlockPosUtil.set(pos, pos.west());
                break;
            case 2:
                BlockPosUtil.set(pos, pos.north());
                break;
            case 3:
                BlockPosUtil.set(pos, pos.east());
                break;
                default:
                    break;
            }
            break;
        case BUTTON_BACK:
            switch (facing)
            {
            case 0:
                BlockPosUtil.set(pos, pos.north());
                break;
            case 1:
                BlockPosUtil.set(pos, pos.east());
                break;
            case 2:
                BlockPosUtil.set(pos, pos.south());
                break;
            case 3:
                BlockPosUtil.set(pos, pos.west());
                break;
            }
            break;
        }
        updatePosition();
    }

    /**
     * Changes the current schematic.
     * Set to button position at that time
     */
    private void changeSchematic()
    {
        String hut;
        String style;

        hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).getLabel();
        style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

        rotation = 0;

        SchematicWrapper schematic = new SchematicWrapper(this.mc.theWorld, style + '/' + hut + '1');

        Settings.instance.setActiveSchematic(schematic.getSchematic());

        Settings.instance.moveTo(pos);
    }

    /**
     * Update position of the schematic
     */
    private void updatePosition()
    {
        Settings.instance.moveTo(pos);
        RenderSchematic.INSTANCE.refresh();
    }
}
