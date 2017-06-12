package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.views.DropDownList;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.network.messages.BuildToolPlaceMessage;
import com.minecolonies.coremod.network.messages.SchematicRequestMessage;
import com.minecolonies.coremod.network.messages.SchematicSaveMessage;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * BuildTool window.
 */
public class WindowBuildTool extends AbstractWindowSkeleton
{
    /**
     * This button is used to set the previous available building type.
     */
    private static final String BUTTON_PREVIOUS_TYPE_ID = "previousBuildingType";

    /**
     * This drop down list is used to set the section either huts (Builder, Town Hall), decorations or scan mode.
     */
    private static final String DROPDOWN_TYPE_ID = "buildingType";

    /**
     * This button is used to set the next available building type.
     */
    private static final String BUTTON_NEXT_TYPE_ID = "nextBuildingType";

    /**
     * This button is used to set the previous available style.
     */
    private static final String BUTTON_PREVIOUS_STYLE_ID = "previousStyle";

    /**
     * This drop down list is used to choose which style should be used.
     */
    private static final String DROPDOWN_STYLE_ID = "style";

    /**
     * This button is used to set the next available style.
     */
    private static final String BUTTON_NEXT_STYLE_ID = "nextStyle";

    /**
     * This button is used to set the previous available schematic.
     */
    private static final String BUTTON_PREVIOUS_SCHEMATIC_ID = "previousSchematic";

    /**
     * This drop down list is used to set the schematic.
     */
    private static final String DROPDOWN_SCHEMATIC_ID = "schematic";

    /**
     * This button is used to set the next available schematic.
     */
    private static final String BUTTON_NEXT_SCHEMATIC_ID = "nextSchematic";

    /**
     * This button will send a packet to the server telling it to place this hut/decoration.
     */
    private static final String BUTTON_CONFIRM = "confirm";

    /**
     * This button will remove the currently rendered structure.
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * This button will rotateWithMirror the structure counterclockwise.
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
    private static final String BUTTON_BACKWARD = "backward";

    /**
     * Move the structure preview left.
     */
    private static final String BUTTON_LEFT = "left";

    /**
     * Move the structure preview right.
     */
    private static final String BUTTON_RIGHT = "right";

    /**
     * Rename the scanned structure.
     */
    private static final String BUTTON_RENAME = "rename";

    /**
     * Delete the scanned structure.
     */
    private static final String BUTTON_DELETE = "delete";

    /**
     * Mirror the structure.
     */
    private static final String BUTTON_MIRROR = "mirror";

    /**
     * Resource suffix.
     */
    private static final String BUILD_TOOL_RESOURCE_SUFFIX = ":gui/windowBuildTool.xml";

    /**
     * Hut prefix.
     */
    private static final String HUT_PREFIX = ":blockHut";

    /**
     * All possible rotations.
     */
    private static final int POSSIBLE_ROTATIONS = 4;

    /**
     * Rotation to rotateWithMirror right.
     */
    private static final int ROTATE_RIGHT = 1;

    /**
     * Rotation to rotateWithMirror 180 degree.
     */
    private static final int ROTATE_180 = 2;

    /**
     * Rotation to rotateWithMirror left.
     */
    private static final int ROTATE_LEFT = 3;

    /**
     * List of section.
     */
    @NotNull
    private List<String> sections = new ArrayList<>();

    /**
     * List of style for the section.
     */
    @NotNull
    private List<String> styles = new ArrayList<>();

    /**
     * List of decorations or level possible to make with the style.
     */
    @NotNull
    private List<String> schematics = new ArrayList<>();

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
     * Drop down list for section.
     */
    private       DropDownList     sectionsDropDownList;

    /**
     * Drop down list for style.
     */
    private       DropDownList     stylesDropDownList;

    /**
     * Drop down list for schematic.
     */
    private       DropDownList     schematicsDropDownList;

    /**
     * Button to rename a scanned schematic.
     */
    private final Button           renameButton;

    /**
     * Button to delete a scanned schematic.
     */
    private final Button           deleteButton;

    /**
     * Confirmation dialog when deleting a scanned schematic.
     */
    private       DialogDoneCancel confirmDeleteDialog;

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
        }
        else if (pos != null)
        {
            this.pos = pos;
            Settings.instance.setPosition(pos);
            Settings.instance.setRotation(0);
        }

        initBuildingTypeNavigation();
        initStyleNavigation();
        initSchematicNavigation();

        //Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, WindowBuildTool::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, WindowBuildTool::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowBuildTool::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);

        registerButton(BUTTON_RENAME, this::renameClicked);
        registerButton(BUTTON_DELETE, this::deleteClicked);
        renameButton = findPaneOfTypeByID(BUTTON_RENAME, Button.class);
        deleteButton = findPaneOfTypeByID(BUTTON_DELETE, Button.class);
    }

    /**
     * Drop down class for sections.
     */
    private class SectionDropDownList implements DropDownList.DataProvider
    {
        @Override
        public int getElementCount()
        {
            return sections.size();
        }

        @Override
        public String getLabel(final int index)
        {
            final String name = sections.get(index);
            if (Structures.SCHEMATICS_SCAN.equals(name))
            {
                return LanguageHandler.format("com.minecolonies.coremod.gui.buildtool.scans");
            }
            else if (Structures.SCHEMATICS_PREFIX.equals(name))
            {
                return LanguageHandler.format("com.minecolonies.coremod.gui.buildtool.decorations");
            }
            //should be a hut
            return LanguageHandler.format("tile.minecolonies.blockHut" + name + ".name");
        }
    }

    /**
     * Initialise the previous/next and drop down list for section.
     */
    private void initBuildingTypeNavigation()
    {
        registerButton(BUTTON_PREVIOUS_TYPE_ID, this::previousSection);
        registerButton(BUTTON_NEXT_TYPE_ID, this::nextSection);
        sectionsDropDownList = findPaneOfTypeByID(DROPDOWN_TYPE_ID, DropDownList.class);
        sectionsDropDownList.setHandler(this::onDropDownListChanged);
        sectionsDropDownList.setDataProvider(new SectionDropDownList());
    }

    /**
     * Initialise the previous/next and drop down list for style.
     */
    private void initStyleNavigation()
    {
        registerButton(BUTTON_PREVIOUS_STYLE_ID, this::previousStyle);
        registerButton(BUTTON_NEXT_STYLE_ID, this::nextStyle);
        stylesDropDownList = findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class);
        stylesDropDownList.setHandler(this::onDropDownListChanged);
        stylesDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return styles.size();
            }

            @Override
            public String getLabel(final int index)
            {
                if (index >= 0 && index < styles.size())
                {
                    return styles.get(index);
                }
                return "";
            }
        });
    }

    /**
     * Initialise the previous/next and drop down list for schematic.
     */
    private void initSchematicNavigation()
    {
        registerButton(BUTTON_PREVIOUS_SCHEMATIC_ID, this::previousSchematic);
        registerButton(BUTTON_NEXT_SCHEMATIC_ID, this::nextSchematic);
        schematicsDropDownList = findPaneOfTypeByID(DROPDOWN_SCHEMATIC_ID, DropDownList.class);
        schematicsDropDownList.setHandler(this::onDropDownListChanged);
        schematicsDropDownList.setDataProvider(new DropDownList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return schematics.size();
            }

            @Override
            public String getLabel(final int index)
            {
                final Structures.StructureName sn = new Structures.StructureName(schematics.get(index));
                return sn.getLocalizedName();
            }
        });
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        Structures.loadScannedStyleMaps();

        sections.clear();
        final InventoryPlayer inventory = this.mc.thePlayer.inventory;
        final List<String> allSections = Structures.getSections();
        for (String section : allSections)
        {
            if (section.equals(Structures.SCHEMATICS_PREFIX) || section.equals(Structures.SCHEMATICS_SCAN) || inventoryHasHut(inventory, section))
            {
                sections.add(section);
            }
        }

        setStructureName(Settings.instance.getStructureName());
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (ColonyManager.isSchematicDownloaded())
        {
            ColonyManager.setSchematicDownloaded(false);
            changeSchematic();
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
            Settings.instance.setSchematicInfo(schematics.get(schematicsDropDownList.getSelectedIndex()), rotation);
        }
    }


    /**
     * ---------------- Schematic Navigation Handling -----------------
     */

    /**
     * Change to the next section, Builder, Citizen ... Decorations and Scan.
     */
    private void nextSection()
    {
        sectionsDropDownList.selectNext();
    }

    /**
     * Change to the previous section, Builder, Citizen ... Decorations and Scan.
     */
    private void previousSection()
    {
        sectionsDropDownList.selectPrevious();
    }

    /**
     * Change to the next style.
     */
    private void nextStyle()
    {
        stylesDropDownList.selectNext();
    }

    /**
     * Change to the previous style.
     */
    private void previousStyle()
    {
        stylesDropDownList.selectPrevious();
    }

    /**
     * Update the styles list but try to keep the same one.
     */
    private void updateStyles()
    {
        String currentStyle = "";
        if (stylesDropDownList.getSelectedIndex() > -1 && stylesDropDownList.getSelectedIndex() < styles.size())
        {
            currentStyle = styles.get(stylesDropDownList.getSelectedIndex());
        }
        styles = Structures.getStylesFor(sections.get(sectionsDropDownList.getSelectedIndex()));
        int newIndex = styles.indexOf(currentStyle);
        if (newIndex == -1)
        {
            newIndex = 0;
        }

        final boolean enabled = styles.size() > 1;
        findPaneOfTypeByID(BUTTON_PREVIOUS_STYLE_ID, Button.class).setEnabled(enabled);
        findPaneOfTypeByID(DROPDOWN_STYLE_ID, DropDownList.class).setEnabled(enabled);
        findPaneOfTypeByID(BUTTON_NEXT_STYLE_ID, Button.class).setEnabled(enabled);
        stylesDropDownList.setSelectedIndex(newIndex);
    }

    /**
     * Go to the next schematic.
     */
    private void nextSchematic()
    {
        schematicsDropDownList.selectNext();
    }

    /**
     * Go to the previous schematic.
     */
    private void previousSchematic()
    {
        schematicsDropDownList.selectPrevious();
    }

    /**
     * Update the list a available schematics.
     */
    private void updateSchematics()
    {
        String schematic = "";
        if (schematicsDropDownList.getSelectedIndex() > -1 && schematicsDropDownList.getSelectedIndex() < schematics.size())
        {
            schematic = schematics.get(schematicsDropDownList.getSelectedIndex());
        }
        final String currentSchematic = schematic.isEmpty() ? "" : (new Structures.StructureName(schematic)).getSchematic();
        String section = sections.get(sectionsDropDownList.getSelectedIndex());
        String style = styles.get(stylesDropDownList.getSelectedIndex());
        schematics = Structures.getSchematicsFor(section, style);
        int newIndex = -1;
        for (int i = 0; i < schematics.size(); i++)
        {
            Structures.StructureName sn = new Structures.StructureName(schematics.get(i));
            if (sn.getSchematic().equals(currentSchematic))
            {
                newIndex = i;
                break;
            }
        }

        if (newIndex == -1)
        {
            newIndex = 0;
        }

        final boolean enabled = schematics.size() > 1;
        findPaneOfTypeByID(BUTTON_PREVIOUS_SCHEMATIC_ID, Button.class).setEnabled(enabled);
        findPaneOfTypeByID(DROPDOWN_SCHEMATIC_ID, DropDownList.class).setEnabled(enabled);
        findPaneOfTypeByID(BUTTON_NEXT_SCHEMATIC_ID, Button.class).setEnabled(enabled);
        schematicsDropDownList.setSelectedIndex(newIndex);
    }

    /**
     * called every time one of the dropdownlist changed.
     *
     * @param list the dropdown list which change
     * @param index is the index selected in the list
     */
    private void onDropDownListChanged(final DropDownList list)
    {
        if (list == sectionsDropDownList)
        {
            final String name = sections.get(sectionsDropDownList.getSelectedIndex());
            if (Structures.SCHEMATICS_SCAN.equals(name))
            {
                renameButton.setVisible(true);
                deleteButton.setVisible(true);
            }
            else
            {
                renameButton.setVisible(false);
                deleteButton.setVisible(false);
            }
            updateStyles();
        }
        else if (list == stylesDropDownList)
        {
            updateSchematics();
        }
        else if (list == schematicsDropDownList)
        {
            changeSchematic();
        }
    }

    /**
     * Set the structure name.
     *
     * @param structureName name of the structure name
     *               Ex: schematics/wooden/Builder2
     */
    private void setStructureName(final String structureName)
    {
        if (structureName != null)
        {
            final Structures.StructureName sn = new Structures.StructureName(structureName);
            final int sectionIndex = sections.indexOf(sn.getSection());
            if (sectionIndex != -1)
            {
                sectionsDropDownList.setSelectedIndex(sectionIndex);
                final int styleIndex = styles.indexOf(sn.getStyle());
                if (styleIndex != -1)
                {
                    stylesDropDownList.setSelectedIndex(styleIndex);
                    final int schematicIndex = schematics.indexOf(sn.toString());
                    if (schematicIndex != -1)
                    {
                        schematicsDropDownList.setSelectedIndex(schematicIndex);
                        return;
                    }
                }
            }
        }

        //We did not find the structure, select the first of each
        sectionsDropDownList.setSelectedIndex(0);
        stylesDropDownList.setSelectedIndex(0);
        schematicsDropDownList.setSelectedIndex(0);
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

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Move the schematic up.
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
     * Move the structure left.
     */
    private void moveLeftClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.thePlayer.getHorizontalFacing().rotateYCCW()));
    }

    /**
     * Move the structure right.
     */
    private void moveRightClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.thePlayer.getHorizontalFacing().rotateY()));
    }

    /**
     * Move the structure forward.
     */
    private void moveForwardClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.thePlayer.getHorizontalFacing()));
    }

    /**
     * Move the structure back.
     */
    private void moveBackClicked()
    {
        Settings.instance.moveTo(new BlockPos(0, 0, 0).offset(this.mc.thePlayer.getHorizontalFacing().getOpposite()));
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
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        rotation = (rotation + ROTATE_LEFT) % POSSIBLE_ROTATIONS;
        updateRotation(rotation);
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private static void mirror()
    {
        Settings.instance.mirror();
    }


    /*
     * ---------------- Miscellaneous ----------------
     */

    /**
     * Changes the current structure.
     * Set to button position at that time
     */
    private void changeSchematic()
    {
        final String sname = schematics.get(schematicsDropDownList.getSelectedIndex());
        final Structures.StructureName structureName = new Structures.StructureName(sname);
        Structure structure = new Structure(null,
                                             structureName.toString(),
                                             new PlacementSettings().setRotation(BlockUtils.getRotation(Settings.instance.getRotation())).setMirror(Settings.instance.getMirror()));

        final String md5 = Structures.getMD5(structureName.toString());
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
            if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
            {
                MineColonies.getNetwork().sendToServer(new SchematicRequestMessage(structureName.toString()));
                return;
            }
            else
            {
                Log.getLogger().error("WindowBuildTool: Need to download schematic on a standalone client/server. This should never happen");
            }
        }


        Settings.instance.setStructureName(structureName.toString());
        Settings.instance.setActiveSchematic(structure);

        if (Settings.instance.getPosition() == null)
        {
            Settings.instance.setPosition(this.pos);
        }
    }

    /**
     * Request to build a player scan.
     *
     * @param structureName of the scan to be built.
     */
    private void requestScannedSchematic(@NotNull final Structures.StructureName structureName)
    {
        if (!Structures.isPlayerSchematicsAllowed())
        {
            return;
        }

        if (Structures.hasMD5(structureName))
        {
            final String md5 = Structures.getMD5(structureName.toString());
            final String serverSideName = Structures.SCHEMATICS_CACHE + '/' + md5;
            if (!Structures.hasMD5(new Structures.StructureName(serverSideName)))
            {
                final InputStream stream = Structure.getStream(structureName.toString());
                if (stream != null)
                {
                    Log.getLogger().info("BuilderTool: sending schematic " + structureName + "(md5:" + md5 + ") to the server");
                    MineColonies.getNetwork().sendToServer(new SchematicSaveMessage(Structure.getStreamAsByteArray(stream)));
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
                                                                              serverSideName,
                                                                              structureName.toString(),
                                                                              Settings.instance.getPosition(),
                                                                              Settings.instance.getRotation(),
                                                                              false,
                                                                              Settings.instance.getMirror()));
        }
        else
        {
            Log.getLogger().warn("BuilderTool: Can not send schematic without md5: " + structureName);
        }
    }

    /**
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        final Structures.StructureName structureName = new Structures.StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
        if (structureName.getPrefix().equals(Structures.SCHEMATICS_SCAN) && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            //We need to check that the server have it too using the md5
            requestScannedSchematic(structureName);
        }
        else
        {
            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(
                                                                              structureName.toString(),
                                                                              structureName.toString(),
                                                                              Settings.instance.getPosition(),
                                                                              Settings.instance.getRotation(),
                                                                              structureName.isHut(),
                                                                              Settings.instance.getMirror()));
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
            Settings.instance.getActiveStructure().setPlacementSettings(settings.setMirror(Settings.instance.getMirror()));
        }
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        final Structures.StructureName structureName = new Structures.StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
        @NotNull final WindowStructureNameEntry window = new WindowStructureNameEntry(structureName);
        window.open();
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void deleteClicked()
    {
        confirmDeleteDialog = new DialogDoneCancel(getWindow());
        confirmDeleteDialog.setHandler(this::onDialogClosed);
        final Structures.StructureName structureName = new Structures.StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
        confirmDeleteDialog.setTitle(LanguageHandler.format("com.minecolonies.coremod.gui.structure.delete.title"));
        confirmDeleteDialog.setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.structure.delete.body", structureName.toString()));
        confirmDeleteDialog.open();
    }

    /**
     * handle when a dialog is closed.
     *
     * @param dialog which is being closed.
     * @param buttonId is the id of the button used to close the dialog.
     */
    public void onDialogClosed(final DialogDoneCancel dialog, final int buttonId)
    {
        if (dialog == confirmDeleteDialog && buttonId == DialogDoneCancel.DONE)
        {
            final Structures.StructureName structureName = new Structures.StructureName(schematics.get(schematicsDropDownList.getSelectedIndex()));
            if (Structures.SCHEMATICS_SCAN.equals(structureName.getPrefix())
                && Structures.deleteScannedStructure(structureName))
            {
                Structures.loadScannedStyleMaps();
                if (schematics.size() > 1)
                {
                    schematicsDropDownList.selectNext();
                    stylesDropDownList.setSelectedIndex(stylesDropDownList.getSelectedIndex());
                }
                else if (styles.size() > 1)
                {
                    stylesDropDownList.selectNext();
                }
                else
                {
                    sectionsDropDownList.selectNext();
                }
            }
        }
    }
}
