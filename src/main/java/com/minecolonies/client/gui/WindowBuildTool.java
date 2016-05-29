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
    private static final    String          BUTTON_HUT_DEC_ID           = "hutDec";
    private static final    String          BUTTON_STYLE_ID             = "style";

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
    Resource suffix and hutDec prefix
    */
    private static final    String          BUILD_TOOL_RESOURCE_SUFFIX  = ":gui/windowBuildTool.xml";
    private static final    String          HUT_PREFIX                  = ":blockHut";

    private static final BlockPos DEFAULT_POS = new BlockPos(0, 0, 0);

    /**
     * List of huts or decorations possible to make.
     */
    private List<String> hutDec = new ArrayList<>();

    /**
     * Index of the rendered hutDec/decoration.
     */
    private int hutDecIndex = 0;

    /**
     * Index of the current style.
     */
    private int styleIndex = 0;

    //Position and rotation for the tool
    private BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
    private int rotation = 0;

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
        if (Settings.instance.inHutMode)
        {
            loadHutMode();
        }
        else
        {
            loadDecorationMode();
        }
    }

    private void loadDecorationMode()
    {
        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.decoration"));

        hutDec.addAll(Schematics.getDecorations());

        setupButtons();
    }

    private void loadHutMode()
    {
        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.hut"));

        InventoryPlayer inventory = this.mc.thePlayer.inventory;

        //Add possible hutDec (has item) to list, if it has a schematic, and player has the block
        hutDec.addAll(Schematics.getHuts().stream()
                                .filter(hut -> inventoryHasHut(inventory, hut) && Schematics.getStylesForHut(hut) != null)
                                .collect(Collectors.toList()));

        setupButtons();
    }

    private void setupButtons()
    {
        if(hutDec.isEmpty())
        {
            Button buttonHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class);
            buttonHutDec.setLabel(LanguageHandler.getString(Settings.instance.inHutMode ? "com.minecolonies.gui.buildtool.nohut" : "com.minecolonies.gui.buildtool.nodecoration"));
            buttonHutDec.setEnabled(false);

            Settings.instance.setActiveSchematic(null);
        }
        else
        {
            if (Settings.instance.getActiveSchematic() != null)
            {
                hutDecIndex = Math.max(0, hutDec.indexOf(Settings.instance.hutDec));
                styleIndex = Math.max(0, getStyles().indexOf(Settings.instance.style));
            }

            Button buttonHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class);
            buttonHutDec.setLabel(hutDec.get(hutDecIndex));
            buttonHutDec.setEnabled(true);

            Button buttonStyle = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class);
            buttonStyle.setVisible(true);
            buttonStyle.setLabel(getStyles().get(styleIndex));

            //Render stuff
            if (Settings.instance.getActiveSchematic() == null)
            {
                changeSchematic();
            }
        }
    }

    private boolean inventoryHasHut(InventoryPlayer inventory, String hut)
    {
        return inventory.hasItem(Block.getBlockFromName(Constants.MOD_ID + HUT_PREFIX + hut).getItem(null, DEFAULT_POS));
    }

    @Override
    public void onClosed()
    {
        if(Settings.instance.getActiveSchematic() != null)
        {
            Settings.instance.rotation = rotation;

            Settings.instance.hutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel();
            Settings.instance.style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

        }
    }

    private List<String> getStyles()
    {
        if(Settings.instance.inHutMode)
        {
            return Schematics.getStylesForHut(hutDec.get(hutDecIndex));
        }
        else
        {
            return Schematics.getStylesForDecoration(hutDec.get(hutDecIndex));
        }
    }

    @Override
    public void onButtonClicked(Button button)
    {
        switch (button.getID())
        {
        case BUTTON_TYPE_ID:
            Settings.instance.setActiveSchematic(null);
            hutDec.clear();
            hutDecIndex = 0;
            styleIndex = 0;

            if(Settings.instance.inHutMode)
            {
                Settings.instance.inHutMode = false;
                loadDecorationMode();
            }
            else
            {
                Settings.instance.inHutMode = true;
                loadHutMode();
            }
            break;
        case BUTTON_HUT_DEC_ID:
            if(hutDec.size() == 1)
            {
                break;
            }

            hutDecIndex = (hutDecIndex + 1) % hutDec.size();
            styleIndex = 0;

            button.setLabel(hutDec.get(hutDecIndex));
            findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(getStyles().get(styleIndex));

            changeSchematic();
            break;
        case BUTTON_STYLE_ID:
            List<String> styles = getStyles();

            if(styles.size() == 1)
            {
                break;
            }

            styleIndex = (styleIndex + 1) % styles.size();

            button.setLabel(styles.get(styleIndex));

            changeSchematic();
            break;
        case BUTTON_CONFIRM:
            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(hutDec.get(hutDecIndex),
                    getStyles().get(styleIndex), this.pos, rotation, Settings.instance.inHutMode));
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
            RotationHelper.rotate(Settings.instance.schematic, EnumFacing.DOWN, true);
            updatePosition();
            break;
        case BUTTON_ROTATE_RIGHT:
            rotation = (rotation + 1) % 4;
            RotationHelper.rotate(Settings.instance.schematic, EnumFacing.UP, true);
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
        String labelHutDec;
        String labelHutStyle;

        labelHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel();
        labelHutStyle = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

        rotation = 0;

        SchematicWrapper schematic = new SchematicWrapper(this.mc.theWorld, labelHutStyle + '/' + labelHutDec + (Settings.instance.inHutMode ? '1' : ""));

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
