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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BuildTool window.
 *
 * @author Colton
 */
public class WindowBuildTool extends AbstractWindowSkeleton
{
    /*
    All buttons for the GUI
     */

    /**
     * This button is used to set whether the window is in hut mode or decoration mode.
     */
    private static final String BUTTON_TYPE_ID = "buildingType";

    /**
     * This button is used to choose which hut or decoration should be built.
     */
    private static final String BUTTON_HUT_DEC_ID = "hutDec";

    /**
     * This button is used to choose which style should be used.
     */
    private static final String BUTTON_STYLE_ID = "style";

    /**
     * This button is used to cycle through different hut levels.
     */
    private static final String BUTTON_LEVEL_ID = "level";

    /**
     * This button will send a packet to the server telling it to place this hut/decoration.
     */
    private static final String BUTTON_CONFIRM = "confirm";

    /**
     * This button will remove the currently rendered schematic.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * This button will rotate the schematic counterclockwise.
     */
    private static final String BUTTON_ROTATE_LEFT = "rotateLeft";

    /**
     * This button will rotated the schematic clockwise.
     */
    private static final String BUTTON_ROTATE_RIGHT = "rotateRight";

    /**
     * Move the schematic preview up.
     */
    private static final String BUTTON_UP = "up";

    /**
     * Move the schematic preview down.
     */
    private static final String BUTTON_DOWN = "down";

    /**
     * Move the schematic preview forward.
     */
    private static final String BUTTON_FORWARD = "forward";

    /**
     * Move the schematic preview back.
     */
    private static final String BUTTON_BACK = "back";

    /**
     * Move the schematic preview left.
     */
    private static final String BUTTON_LEFT = "left";

    /**
     * Move the schematic preview right.
     */
    private static final String BUTTON_RIGHT = "right";

    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowBuildTool.xml";

    /**
     * Hut prefix.
     */
    private static final String HUT_PREFIX = ":blockHut";

    private static final int POSSIBLE_ROTATIONS = 4;
    private static final int ROTATE_RIGHT       = 1;
    private static final int ROTATE_LEFT        = 3;

    private static final String NO_HUT_IN_INVENTORY = "com.minecolonies.gui.buildtool.nohutininventory";
    /**
     * List of huts or decorations possible to make.
     */
    @NotNull
    private              List<String> hutDec              = new ArrayList<>();

    /**
     * Index of the rendered hutDec/decoration.
     */
    private int hutDecIndex = 0;

    /**
     * Index of the current style.
     */
    private int styleIndex = 0;

    /**
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);

    /**
     * Current rotation of the hut/decoration.
     */
    private int rotation = 0;

    /**
     * Current hut level that is being rendered.
     * This stores the level minus 1, because its easier and cooperates with modulus better.
     */
    private int level = 0;

    /**
     * Creates a window build tool
     * This requires X, Y and Z coordinates
     * If a schematic is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used
     *
     * @param pos coordinate
     */
    public WindowBuildTool(@Nullable BlockPos pos)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);

        @Nullable Schematic schematic = Settings.instance.getActiveSchematic();
        if (schematic != null)
        {
            BlockPosUtil.set(this.pos, Settings.instance.getOffset().add(schematic.getOffset()));
            rotation = Settings.instance.getRotation();
            level = Settings.instance.getLevel();
        }
        else if (pos != null)
        {
            BlockPosUtil.set(this.pos, pos);
        }


        registerButton(BUTTON_TYPE_ID, this::placementModeClicked);
        registerButton(BUTTON_HUT_DEC_ID, this::hutDecClicked);
        registerButton(BUTTON_STYLE_ID, this::styleClicked);
        registerButton(BUTTON_LEVEL_ID, this::levelClicked);
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

    private static boolean inventoryHasHut(@NotNull InventoryPlayer inventory, String hut)
    {
        return inventory.hasItemStack(new ItemStack(Block.getBlockFromName(Constants.MOD_ID + HUT_PREFIX + hut)));
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
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

    /**
     * Called when the window is closed.
     * If there is a current schematic, its information is stored in {@link Settings}.
     */
    @Override
    public void onClosed()
    {
        if (Settings.instance.getActiveSchematic() != null)
        {
            Settings.instance.setSchematicInfo(
              findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel(),
              findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel(),
              level,
              rotation);
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
        if (hutDec.isEmpty())
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

            if (Settings.instance.getActiveSchematic() == null)
            {
                rotation = 0;
                level = 0;
                changeSchematic();
            }

            updateLevelButton();
        }
    }

    /**
     * Change placement modes. Hut or Decoration.
     *
     * @param button required parameter.
     */
    private void placementModeClicked(Button button)
    {
        Settings.instance.setActiveSchematic(null);
        hutDec.clear();
        hutDecIndex = 0;
        styleIndex = 0;

        if (Settings.instance.isInHutMode())
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

    /**
     * Change to the next hut/decoration.
     *
     * @param button required parameter.
     */
    private void hutDecClicked(@NotNull Button button)
    {
        if (hutDec.size() == 1)
        {
            return;
        }

        hutDecIndex = (hutDecIndex + 1) % hutDec.size();
        styleIndex = 0;

        button.setLabel(hutDec.get(hutDecIndex));
        findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(getStyles().get(styleIndex));

        //TODO: Should we reset rotation here? Do we want rotation to be reset when switching hut types.
        rotation = 0;
        level = 0;
        changeSchematic();
    }

    private List<String> getStyles()
    {
        if (Settings.instance.isInHutMode())
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
        String labelHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel();
        String labelHutStyle = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

        @NotNull SchematicWrapper schematic = new SchematicWrapper(this.mc.theWorld, labelHutStyle + '/' + labelHutDec + (Settings.instance.isInHutMode() ? (level + 1) : ""));

        Settings.instance.setActiveSchematic(schematic.getSchematic());

        Settings.instance.moveTo(this.pos);

        //Catch up on rotations, makes it so going up a level or changing style doesn't reset rotation.
        if (this.rotation == ROTATE_LEFT)
        {
            RotationHelper.rotate(Settings.instance.getSchematicWorld(), EnumFacing.DOWN, true);
        }
        else
        {
            //Runs 0, 1, or 2 times.
            for (int times = 0; times < rotation; times++)
            {
                RotationHelper.rotate(Settings.instance.getSchematicWorld(), EnumFacing.UP, true);
            }
        }
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Change to the next style.
     *
     * @param button required parameter.
     */
    private void styleClicked(@NotNull Button button)
    {
        List<String> styles = getStyles();

        if (styles.size() == 1)
        {
            return;
        }

        styleIndex = (styleIndex + 1) % styles.size();

        button.setLabel(styles.get(styleIndex));

        changeSchematic();
    }

    /**
     * Change to the next level building.
     *
     * @param button required parameter.
     */
    private void levelClicked(Button button)
    {
        int maxLevel = Schematics.getMaxLevelForHut(hutDec.get(hutDecIndex));
        if (maxLevel > 1)
        {
            level = (level + 1) % maxLevel;
            updateLevelButton();

            changeSchematic();
        }
    }

    private void updateLevelButton()
    {
        Button buttonLevel = findPaneOfTypeByID(BUTTON_LEVEL_ID, Button.class);
        if (Settings.instance.isInHutMode())
        {
            buttonLevel.setVisible(true);
            buttonLevel.setLabel("Level: " + (level + 1));
        }
        else
        {
            buttonLevel.setVisible(false);
        }
    }

    /**
     * Send a packet telling the server to place the current schematic.
     *
     * @param button required parameter.
     */
    private void confirmClicked(Button button)
    {
        InventoryPlayer inventory = this.mc.thePlayer.inventory;
        if (null != Settings.instance.getActiveSchematic() &&
              ((Settings.instance.isInHutMode() && inventoryHasHut(inventory, hutDec.get(hutDecIndex)))
                 || !Settings.instance.isInHutMode())
          )
        {
            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(hutDec.get(hutDecIndex),
                                                                              getStyles().get(styleIndex), this.pos, rotation, Settings.instance.isInHutMode()));
        }
        else
        {
            LanguageHandler.sendPlayerLocalizedMessage(this.mc.thePlayer, WindowBuildTool.NO_HUT_IN_INVENTORY);
        }

        Settings.instance.setActiveSchematic(null);
        close();
    }

    /**
     * Cancel the current schematic.
     *
     * @param button required parameter.
     */
    private void cancelClicked(Button button)
    {
        Settings.instance.setActiveSchematic(null);
        close();
    }

    /**
     * Move the schematic left.
     *
     * @param button required parameter.
     */
    private void moveLeftClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing().rotateYCCW()));
        updatePosition();
    }

    /**
     * Update position of the schematic
     */
    private void updatePosition()
    {
        Settings.instance.moveTo(pos);
        RenderSchematic.INSTANCE.refresh();
    }

    /**
     * Move the schematic right.
     *
     * @param button required parameter.
     */
    private void moveRightClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing().rotateY()));
        updatePosition();
    }

    /**
     * Move the schematic forward.
     *
     * @param button required parameter.
     */
    private void moveForwardClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing()));
        updatePosition();
    }

    /**
     * Move the schematic back.
     *
     * @param button required parameter.
     */
    private void moveBackClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.offset(this.mc.thePlayer.getHorizontalFacing().getOpposite()));
        updatePosition();
    }

    /**
     * Move the schmatic up.
     *
     * @param button required parameter.
     */
    private void moveUpClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.up());
        updatePosition();
    }

    /**
     * Move the schematic down.
     *
     * @param button required parameter.
     */
    private void moveDownClicked(Button button)
    {
        BlockPosUtil.set(pos, pos.down());
        updatePosition();
    }

    /**
     * Rotate the schematic clockwise.
     *
     * @param button required parameter.
     */
    private void rotateRightClicked(Button button)
    {
        rotation = (rotation + ROTATE_RIGHT) % POSSIBLE_ROTATIONS;
        RotationHelper.rotate(Settings.instance.getSchematicWorld(), EnumFacing.UP, true);
        updatePosition();
    }

    /**
     * Rotate the schematic counter clockwise.
     *
     * @param button required parameter.
     */
    private void rotateLeftClicked(Button button)
    {
        rotation = (rotation + ROTATE_LEFT) % POSSIBLE_ROTATIONS;
        RotationHelper.rotate(Settings.instance.getSchematicWorld(), EnumFacing.DOWN, true);
        updatePosition();
    }
}
