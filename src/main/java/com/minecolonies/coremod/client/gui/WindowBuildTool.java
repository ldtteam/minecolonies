package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.coremod.network.messages.BuildToolPlaceMessage;
import com.minecolonies.coremod.network.messages.SchematicRequestMessage;
import com.minecolonies.coremod.network.messages.SchematicSaveMessage;
import com.minecolonies.coremod.util.BlockUtils;
import com.minecolonies.coremod.util.LanguageHandler;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import java.io.InputStream;

/**
 * BuildTool window.
 */
public class WindowBuildTool extends AbstractWindowSkeleton
{
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
     * This button will remove the currently rendered structure.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * This button will rotate the structure counterclockwise.
     */
    private static final String BUTTON_ROTATE_LEFT = "rotateLeft";

    /**
     * This button will rotated the structure clockwise.
     */
    private static final String BUTTON_ROTATE_RIGHT = "rotateRight";

    /**
     * Move the structure preview up.
     */
    private static final String BUTTON_UP = "up";

    /**
     * Move the structure preview down.
     */
    private static final String BUTTON_DOWN = "down";

    /**
     * Move the structure preview forward.
     */
    private static final String BUTTON_FORWARD = "forward";

    /**
     * Move the structure preview back.
     */
    private static final String BUTTON_BACK = "back";

    /**
     * Move the structure preview left.
     */
    private static final String BUTTON_LEFT = "left";

    /**
     * Move the structure preview right.
     */
    private static final String BUTTON_RIGHT = "right";

    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowbuilldtool.xml";

    /**
     * Hut prefix.
     */
    private static final String HUT_PREFIX = ":blockHut";

    /**
     * All possible rotations.
     */
    private static final int POSSIBLE_ROTATIONS = 4;

    /**
     * Rotation to rotate right.
     */
    private static final int ROTATE_RIGHT = 1;

    /**
     * Rotation to rotate 180 degree.
     */
    private static final int ROTATE_180 = 2;

    /**
     * Rotation to rotate left.
     */
    private static final int ROTATE_LEFT = 3;

    /**
     * Language key for missing hut message.
     */
    private static final String NO_HUT_IN_INVENTORY = "com.minecolonies.coremod.gui.buildtool.nohutininventory";

    /**
     * List of huts or decorations possible to make.
     */
    @NotNull
    private final List<String> hutDec = new ArrayList<>();

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
    private BlockPos pos = new BlockPos(0, 0, 0);

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
     * Creates a window build tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowBuildTool(@Nullable final BlockPos pos)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);

        @Nullable final Structure structure = Settings.instance.getActiveStructure();

        if (structure != null)
        {
            rotation = Settings.instance.getRotation();
            level = Settings.instance.getLevel();
        }
        else if (pos != null)
        {
            this.pos = pos;
            Settings.instance.pos = pos;
            Settings.instance.setRotation(0);
        }

        //Register all necessary buttons with the window.
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
        registerButton(BUTTON_UP, WindowBuildTool::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowBuildTool::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
    }

    /**
     * Check if the player inventory has a certain hut.
     *
     * @param inventory the player inventory.
     * @param hut       the hut.
     * @return true if so.
     */
    private static boolean inventoryHasHut(@NotNull final InventoryPlayer inventory, final String hut)
    {
        return inventory.hasItemStack(new ItemStack(Block.getBlockFromName(Constants.MOD_ID + HUT_PREFIX + hut)));
    }

    /**
     * Move the schmatic up.
     */
    private static void moveUpClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 1, 0));
    }

    /**
     * Move the structure down.
     */
    private static void moveDownClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, -1, 0));
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        Structures.loadCustomStyleMaps();
        onChangeMode();
    }

    public void onChangeMode()
    {
        switch (Settings.instance.getBuildingMode())
        {
            case HUT:
                loadHutMode();
                break;
            case DECORATION:
                loadDecorationMode();
                break;
            case CUSTOM:
                loadCustomMode();
                break;
        }

    }

    /**
     * Called when the window is closed.
     * If there is a current structure, its information is stored in {@link Settings}.
     */
    @Override
    public void onClosed()
    {
        if (Settings.instance.getActiveStructure() != null)
        {
            Settings.instance.setSchematicInfo(
              findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel(),
              findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel(),
              level,
              rotation);
        }
    }

    /**
     * Loads the decoration mode of the build tool.
     */
    private void loadDecorationMode()
    {
        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.buildtool.decoration"));

        hutDec.addAll(Structures.getDecorations());

        setupButtons();
    }

    private void loadCustomMode()
    {
        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel("Custom");

        Log.getLogger().info("hutDec.size() before "+hutDec.size());
        for (String customStyle : Structures.getCustoms()) 
        {
            if (!hutDec.contains(customStyle))
            {
                Log.getLogger().info("Adding custom style "+customStyle);
                hutDec.add(customStyle);
            }
            else
            {
                Log.getLogger().info("custom style already added: "+customStyle);
            }
        }
        //hutDec.addAll(Structures.getCustoms());
        Log.getLogger().info("hutDec.size() after "+hutDec.size());

        setupButtons();
    }


    /**
     * Loads the hut mode of the build tool.
     */
    private void loadHutMode()
    {
        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.format("com.minecolonies.coremod.gui.buildtool.hut"));

        final InventoryPlayer inventory = this.mc.player.inventory;

        //Add possible hutDec (has item) to list, if it has a structure, and player has the block
        hutDec.addAll(Structures.getHuts().stream()
                        .filter(hut -> inventoryHasHut(inventory, hut) && Structures.getStylesForHut(hut) != null)
                        .collect(Collectors.toList()));

        setupButtons();
    }

    /**
     * Setup all buttons, enable, disable them if required.
     */
    private void setupButtons()
    {
        if (hutDec.isEmpty())
        {
            final Button buttonHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class);
/*            buttonHutDec.setLabel(LanguageHandler.format(
              Settings.instance.isInHutMode() ? "com.minecolonies.coremod.gui.buildtool.nohut" : "com.minecolonies.coremod.gui.buildtool.nodecoration"));*/
            buttonHutDec.setEnabled(false);
            final Button buttonStyle = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class);
            buttonStyle.setVisible(false);
            Settings.instance.setActiveSchematic(null);
        }
        else
        {
            if (Settings.instance.getActiveStructure() != null)
            {
                hutDecIndex = Math.max(0, hutDec.indexOf(Settings.instance.getHutDec()));
                styleIndex = Math.max(0, getStyles().indexOf(Settings.instance.getStyle()));
            }

            final Button buttonHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class);
            buttonHutDec.setLabel(hutDec.get(hutDecIndex));
            buttonHutDec.setEnabled(true);

            final Button buttonStyle = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class);
            buttonStyle.setVisible(true);
            buttonStyle.setLabel(getStyles().get(styleIndex));
            if (Settings.instance.getActiveStructure() == null)
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
     */
    private void placementModeClicked()
    {
        Settings.instance.setActiveSchematic(null);
        hutDec.clear();
        hutDecIndex = 0;
        styleIndex = 0;


        Settings.instance.nextBuildingMode();
        onChangeMode();
    }

    /**
     * Change to the next hut/decoration.
     */
    private void hutDecClicked(@NotNull final Button button)
    {
        if (hutDec.size() == 1)
        {
            return;
        }

        hutDecIndex = (hutDecIndex + 1) % hutDec.size();
        styleIndex = 0;

        button.setLabel(hutDec.get(hutDecIndex));
        findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(getStyles().get(styleIndex));

        changeSchematic();
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Get all styles from the folders.
     *
     * @return list of style strings.
     */
    private List<String> getStyles()
    {
        if (hutDec.isEmpty())
        {
            Log.getLogger().info("hutDec.isEmpty())");
            return Collections.emptyList();
        }

        switch (Settings.instance.getBuildingMode())
        {
            case HUT:
                return Structures.getStylesForHut(hutDec.get(hutDecIndex));
            case DECORATION:
                return Structures.getStylesForDecoration(hutDec.get(hutDecIndex));
            case CUSTOM:
                return Structures.getStylesForCustom(hutDec.get(hutDecIndex));
            default:
                //TODO
                return Structures.getStylesForDecoration(hutDec.get(hutDecIndex));
        }
    }

    /**
     * Changes the current structure.
     * Set to button position at that time
     */
    private void changeSchematic()
    {
        final String labelHutDec = findPaneOfTypeByID(BUTTON_HUT_DEC_ID, Button.class).getLabel();
        final String labelHutStyle = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();
        final Structure structure;
        final String structureName;

        switch (Settings.instance.getBuildingMode())
        {
            case HUT:
                structureName = Structures.SCHEMATICS_HUTS + '/' + labelHutStyle + '/' + labelHutDec + (level + 1);
                break;
            case DECORATION:
                structureName = Structures.SCHEMATICS_DECORATIONS + '/' + labelHutDec + '/' + labelHutStyle;
                break;
            case CUSTOM:
            default:
                structureName = Structures.SCHEMATICS_CUSTOM + '/' + labelHutDec + '/' + labelHutStyle;
                break;
        }

        structure = new Structure(null,
                                   structureName,
                                   new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())));

        final String md5 = Structures.getMD5(structureName);

        if (structure.isTemplateMissing() || !structure.isCorrectMD5(md5))
        {
            if (structure.isTemplateMissing())
            {
                Log.getLogger().info("Template structure " + structureName + " missing");
            }
            else
            {
                Log.getLogger().info("structure " + structureName + " md5 error");
            }

            Log.getLogger().info("Request To Server for structure " + structureName);
            MineColonies.getNetwork().sendToServer(new SchematicRequestMessage(structureName));
        }


        Settings.instance.setActiveSchematic(structure);

        if (Settings.instance.pos == null)
        {
            Settings.instance.pos = this.pos;
        }
    }

    /**
     * Change to the next style.
     */
    private void styleClicked(@NotNull final Button button)
    {
        final List<String> styles = getStyles();

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
     */
    private void levelClicked()
    {
        final int maxLevel = Structures.getMaxLevelForHut(hutDec.get(hutDecIndex));
        if (maxLevel > 1)
        {
            level = (level + 1) % maxLevel;
            updateLevelButton();

            changeSchematic();
        }
    }

    /**
     * Switch to another level of the structure.
     */
    private void updateLevelButton()
    {
        final Button buttonLevel = findPaneOfTypeByID(BUTTON_LEVEL_ID, Button.class);
        if (Settings.instance.getBuildingMode() == Settings.BuildingMode.HUT)
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
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        if (hutDecIndex < hutDec.size())
        {
            switch (Settings.instance.getBuildingMode())
            {
                case HUT:
                    {
                        final String structureName = Structures.SCHEMATICS_HUTS + '/' + getStyles().get(styleIndex) + '/' + hutDec.get(hutDecIndex);
                        Log.getLogger().info("structureName="+structureName);
                        MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(
                                                                              structureName,
                                                                              //hutDec.get(hutDecIndex),
                                                                              structureName,
                                                                              //getStyles().get(styleIndex),
                                                                              Settings.instance.pos,
                                                                              Settings.instance.getRotation(),
                                                                              true));
                    }
                    break;
                case DECORATION:
                    {
                        final String structureName = Structures.SCHEMATICS_DECORATIONS + '/' + hutDec.get(hutDecIndex) + '/' + getStyles().get(styleIndex);
                        MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(
                                                                              structureName,
                                                                              structureName,
                                                                              Settings.instance.pos,
                                                                              Settings.instance.getRotation(),
                                                                              false));
                    }
                    break;
                case CUSTOM:
                    {
                        final String structureName = Structures.SCHEMATICS_CUSTOM + '/' + hutDec.get(hutDecIndex) + '/' + getStyles().get(styleIndex);
                        if (Structures.hasStructureName(structureName))
                        {
                            Log.getLogger().warn("BuilderTool: client has structure " + structureName);
                            final String md5 = Structures.getMD5(structureName);
                            Log.getLogger().warn("BuilderTool: " + structureName + " => " + md5);
                            final String serverSideName = "cache/"+md5;
                            Log.getLogger().warn("BuilderTool: serverSideName = " + serverSideName);
                            if (!Structures.hasStructureName(serverSideName))
                            {
                                Log.getLogger().warn("BuilderTool: server does not have " + serverSideName);
                                final InputStream stream = Structure.getStream(structureName);
                                if (stream!= null)
                                {
                                    Log.getLogger().warn("BuilderTool: Sending structure " + structureName +" (" + md5 + ")");
                                    MineColonies.getNetwork().sendToServer(new SchematicSaveMessage(Structure.getStreamAsByteArray(stream), structureName));
                                }
                                else
                                {
                                    Log.getLogger().warn("BuilderTool: Can not load " + structureName);
                                }
                            }
                            else
                            {
                                Log.getLogger().warn("BuilderTool: server does not have " + serverSideName);
                            }

                            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(
                                                                                  //Structures.SCHEMATICS_CUSTOM + '/' + hutDec.get(hutDecIndex),
                                                                                  "cache/" + md5,
                                                                                  structureName,
                                                                                  //getStyles().get(styleIndex),
                                                                                  Settings.instance.pos,
                                                                                  Settings.instance.getRotation(),
                                                                                  false));
                        }
                        else
                        {
                            Log.getLogger().warn("BuilderTool: Can not send schematic without md5: " + structureName);
                        }
                    }


                    break;
                    
            }
            /*else
            {
                MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(
                                                                              hutDec.get(hutDecIndex),
                                                                              getStyles().get(styleIndex),
                                                                              Settings.instance.pos,
                                                                              Settings.instance.getRotation(),
                                                                              Settings.instance.getBuildingMode() == Settings.BuildingMode.HUT));
            }*/
        }
        else
        {
            LanguageHandler.sendPlayerMessage(this.mc.player, WindowBuildTool.NO_HUT_IN_INVENTORY);
        }

        Settings.instance.reset();
        close();
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        Settings.instance.reset();
        close();
    }

    /**
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing().rotateYCCW()));
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing().rotateY()));
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing()));
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.player.getHorizontalFacing().getOpposite()));
    }

    /**
     * Rotate the structure clockwise.
     */
    private void rotateRightClicked()
    {
        rotation = (rotation + ROTATE_RIGHT) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
    }

    /**
     * Updates the rotation of the structure depending on the input.
     *
     * @param rotation the rotation to be set.
     */
    private static void updateRotation(final int rotation)
    {
        final PlacementSettings settings = new PlacementSettings();
        switch (rotation)
        {
            case ROTATE_RIGHT:
                settings.setRotation(Rotation.CLOCKWISE_90);
                break;
            case ROTATE_180:
                settings.setRotation(Rotation.CLOCKWISE_180);
                break;
            case ROTATE_LEFT:
                settings.setRotation(Rotation.COUNTERCLOCKWISE_90);
                break;
            default:
                settings.setRotation(Rotation.NONE);
        }
        Settings.instance.setRotation(rotation);

        if (Settings.instance.getActiveStructure() != null)
        {
            Settings.instance.getActiveStructure().setPlacementSettings(settings);
        }
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        rotation = (rotation + ROTATE_LEFT) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
    }

    public void onUpdate()
    {
        super.onUpdate();

        if (ColonyManager.isSchematicDownloaded())
        {
            ColonyManager.setSchematicDownloaded(false);
            changeSchematic();
        }
    }
}
