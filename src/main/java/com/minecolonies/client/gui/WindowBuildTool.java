package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.Schematics;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildToolPlaceMessage;
import com.minecolonies.util.BlockPosUtil;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.SchematicWrapper;
import com.schematica.Settings;
import com.schematica.client.renderer.RenderSchematic;
import com.schematica.client.util.RotationHelper;
import com.schematica.world.storage.Schematic;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BuildTool Window
 *
 * @author Colton
 */
public class WindowBuildTool extends AbstractWindowSkeleton
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

    private static final int POSSIBLE_ROTATIONS = 4;
    private static final int ROTATE_RIGHT       = 1;
    private static final int ROTATE_LEFT        = 3;

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

        Schematic schematic = Settings.instance.getActiveSchematic();
        if(schematic != null)
        {
            BlockPosUtil.set(this.pos, Settings.instance.getOffset().add(schematic.getOffset()));
            rotation = Settings.instance.getRotation();
        }
        else
        {
            BlockPosUtil.set(this.pos, pos);
        }

        registerButton(BUTTON_TYPE_ID, this::placementModeClicked);
        registerButton(BUTTON_HUT_DEC_ID, this::hutDecClicked);
        registerButton(BUTTON_STYLE_ID, this::styleClicked);
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACK, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, this::moveUpClicked);
        registerButton(BUTTON_DOWN, this::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
    }

	@Override
    public void onOpened()
    {
        if (Settings.instance.isInHutMode())
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
            buttonHutDec.setLabel(LanguageHandler.getString(
                    Settings.instance.isInHutMode() ? "com.minecolonies.gui.buildtool.nohut" : "com.minecolonies.gui.buildtool.nodecoration"));
            buttonHutDec.setEnabled(false);

            Settings.instance.setActiveSchematic(null);
        }
        else
        {
            if (Settings.instance.getActiveSchematic() != null)
            {
                hutDecIndex = Math.max(0, hutDec.indexOf(Settings.instance.getHutDec()));
                styleIndex = Math.max(0, getStyles().indexOf(Settings.instance.getStyle()));
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

    private static boolean inventoryHasHut(InventoryPlayer inventory, String hut)
    {
        return inventory.hasItem(Block.getBlockFromName(Constants.MOD_ID + HUT_PREFIX + hut).getItem(null, DEFAULT_POS));
    }

    @Override
    public void onClosed()
    {
        if(Settings.instance.getActiveSchematic() != null)
        {
            Settings.instance.setSchematicInfo(
                    findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel(),
                    findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel(),
                    rotation);
        }
    }

    private List<String> getStyles()
    {
        if(Settings.instance.isInHutMode())
        {
            return Schematics.getStylesForHut(hutDec.get(hutDecIndex));
        }
        else
        {
            return Schematics.getStylesForDecoration(hutDec.get(hutDecIndex));
        }
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

        SchematicWrapper schematic = new SchematicWrapper(this.mc.theWorld, labelHutStyle + '/' + labelHutDec + (Settings.instance.isInHutMode() ? '1' : ""));

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

    /*
     * ---------------- Button Handling -----------------
     */

    private void placementModeClicked(Button button)
    {
        Settings.instance.setActiveSchematic(null);
        hutDec.clear();
        hutDecIndex = 0;
        styleIndex = 0;

        if(Settings.instance.isInHutMode())
        {
            Settings.instance.setInHutMode(false);
            loadDecorationMode();
        }
        else
        {
            Settings.instance.setInHutMode(true);
            loadHutMode();
        }
    }

    private void hutDecClicked(Button button)
    {
        if(hutDec.size() == 1)
        {
            return;
        }

        hutDecIndex = (hutDecIndex + 1) % hutDec.size();
        styleIndex = 0;

        button.setLabel(hutDec.get(hutDecIndex));
        findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(getStyles().get(styleIndex));

        changeSchematic();
    }

    private void styleClicked(Button button)
    {
        List<String> styles = getStyles();

        if(styles.size() == 1)
        {
            return;
        }

        styleIndex = (styleIndex + 1) % styles.size();

        button.setLabel(styles.get(styleIndex));

        changeSchematic();
    }

    private void confirmClicked(Button button)
    {
        MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(hutDec.get(hutDecIndex),
                getStyles().get(styleIndex), this.pos, rotation, Settings.instance.isInHutMode()));
        Settings.instance.setActiveSchematic(null);
        close();
    }

    private void cancelClicked(Button button)
    {
        Settings.instance.setActiveSchematic(null);
        close();
    }

    private void moveLeftClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing().rotateYCCW()));
        updatePosition();
    }

    private void moveRightClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing().rotateY()));
        updatePosition();
    }

    private void moveForwardClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing()));
        updatePosition();
    }

    private void moveBackClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing().getOpposite()));
        updatePosition();
    }

    private void moveUpClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.up());
        updatePosition();
    }

    private void moveDownClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.down());
        updatePosition();
    }

    private void rotateRightClicked(Button button)
    {
        rotation = (rotation + ROTATE_RIGHT) % POSSIBLE_ROTATIONS;
        RotationHelper.rotate(Settings.instance.getSchematicWorld(), EnumFacing.UP, true);
        updatePosition();
    }

    private void rotateLeftClicked(Button button)
    {
        rotation = (rotation + ROTATE_LEFT) % POSSIBLE_ROTATIONS;
        RotationHelper.rotate(Settings.instance.getSchematicWorld(), EnumFacing.DOWN, true);
        updatePosition();
    }
}
