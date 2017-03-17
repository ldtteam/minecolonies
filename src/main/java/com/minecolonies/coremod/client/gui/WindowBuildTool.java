package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.Log;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.client.gui.WindowStructureNameEntry;
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

import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * BuildTool window.
 */
public class WindowBuildTool extends AbstractWindowSkeleton
{
    /**
     * This button is used to set the section either huts (Builder, Town Hall), decorations or custom mode.
     */
    private static final String BUTTON_TYPE_ID = "buildingType";

    /**
     * This button is used to choose which style should be used.
     */
    private static final String BUTTON_STYLE_ID = "style";

    /**
     * This button is used to choose which hut or decoration should be built.
     */
    private static final String BUTTON_SCHEMATIC_ID = "schematic";

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
     * Rename the custom structure.
     */
    private static final String BUTTON_RENAME = "rename";

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
     * Index of the section.
     */
    private int sectionIndex = 0;

    /**
     * Index of the current style.
     */
    private int styleIndex = 0;

    /**
     * Index of the rendered hutDec/decoration.
     */
    private int schematicIndex = 0;

    /**
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private BlockPos pos = new BlockPos(0, 0, 0);

    /**
     * Current rotation of the hut/decoration.
     */
    private int rotation = 0;

    final Button renameButton;

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
            Settings.instance.pos = pos;
            Settings.instance.setRotation(0);
        }

        //Register all necessary buttons with the window.
        registerButton(BUTTON_TYPE_ID, this::nextSection);
        registerButton(BUTTON_STYLE_ID, this::nextStyle);
        registerButton(BUTTON_SCHEMATIC_ID, this::nextSchematic);
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
        registerButton(BUTTON_RENAME, this::renameClicked);
        renameButton = findPaneOfTypeByID(BUTTON_RENAME, Button.class);
    }

    private void init()
    {
        Structures.loadCustomStyleMaps();

        sections.clear();
        final InventoryPlayer inventory = this.mc.player.inventory;
        final List<String> allSections = Structures.getSections();
        for(String section: allSections)
        {
            if (section.equals(Structures.SCHEMATICS_DECORATIONS) || section.equals(Structures.SCHEMATICS_CUSTOM) || inventoryHasHut(inventory, section))
            {
                sections.add(section);
            }
        }

        setStructureName(Settings.instance.getStructureName());
    }

    /**
     * Set the structure name.
     * @param String structureName name of the structure name
     * Ex: huts/wooden/Builder2
     */
    private void setStructureName(final String structureName)
    {
        if (structureName != null)
        {
            final Structures.StructureName sn = new Structures.StructureName(structureName);
            final int sectionIndex = sections.indexOf(sn.getSection());
            if (sectionIndex != -1)
            {
                setSection(sectionIndex);
                final int styleIndex = styles.indexOf(sn.getStyle());
                if (styleIndex != -1)
                {
                    setStyle(styleIndex);
                    final int schematicIndex = schematics.indexOf(sn.toString());
                    if (schematicIndex != -1)
                    {
                        setSchematic(schematicIndex);
                        return;
                    }
                }
            }
        }

        setSection(sectionIndex);
        setStyle(styleIndex);
        setSchematic(schematicIndex);
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
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        init();
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
            Settings.instance.setSchematicInfo(schematics.get(schematicIndex), rotation);
        }
    }

    /*
     * ---------------- Button Handling -----------------
     */

    /**
     * Changes the current structure.
     * Set to button position at that time
     */
    private void changeSchematic()
    {
        final String sname= getSchematicName();
        Log.getLogger().info("Loading structure sname:" + sname);
        final Structures.StructureName structureName = new Structures.StructureName(getSchematicName());
        Log.getLogger().info("Loading structure " + structureName.toString());
        Structure structure = new Structure(null,
                                   structureName.toString(),
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
            MineColonies.getNetwork().sendToServer(new SchematicRequestMessage(structureName.toString()));
        }


        Settings.instance.setActiveSchematic(structure);

        if (Settings.instance.pos == null)
        {
            Settings.instance.pos = this.pos;
        }
    }

    private void requestCustomSchematic(@NotNull final Structures.StructureName structureName)
    {
        if (!Structures.isPlayerSchematicsAllowed())
        {
            return;
        }

        if (Structures.hasMD5(structureName))
        {
            final String md5 = Structures.getMD5(structureName);
            final String serverSideName = "cache/"+md5;
            if (!Structures.hasMD5(new Structures.StructureName(serverSideName)))
            {
                final InputStream stream = Structure.getStream(structureName.toString());
                if (stream!= null)
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
                                                              Structures.SCHEMATICS_CACHE + '/' + md5,
                                                              structureName.toString(),
                                                              Settings.instance.pos,
                                                              Settings.instance.getRotation(),
                                                              false));
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
        Structures.StructureName structureName = new Structures.StructureName(schematics.get(schematicIndex));
        Log.getLogger().info("structureName="+structureName);
        if (structureName.getPrefix().equals(Structures.SCHEMATICS_CUSTOM) && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            //We need to check that the server hava it too using the md5
            requestCustomSchematic(structureName);
        }
        else
        {
            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(
                                                                              structureName.toString(),
                                                                              structureName.toString(),
                                                                              Settings.instance.pos,
                                                                              Settings.instance.getRotation(),
                                                                              structureName.isHut()));
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

    /**
     * Change to the next section, Builder, Citizen ... Decorations and Custom.
     */
    public void nextSection()
    {
        if (sections.size() == 0)
        {
            setSection(0);
        }
        else
        {
            setSection((sectionIndex + 1) % sections.size());
        }
    }

    /**
     * Action performed when rename button is clicked.
     */
    private void renameClicked()
    {
        final Structures.StructureName structureName = new Structures.StructureName(getSchematicName());
        @NotNull final WindowStructureNameEntry window = new WindowStructureNameEntry(structureName);
        window.open();
    }

    /**
     * Set the current section and update styles.
     */
    public void setSection(int index)
    {
        sectionIndex = index;
        String name = getSectionName();
        if (Structures.SCHEMATICS_CUSTOM.equals(name))
        {
            name = LanguageHandler.format("com.minecolonies.coremod.gui.buildtool.custom");
            renameButton.setEnabled(true);
        }
        else
        {
            renameButton.setEnabled(false);
            if (Structures.SCHEMATICS_DECORATIONS.equals(name))
            {
                name = LanguageHandler.format("com.minecolonies.coremod.gui.buildtool.decorations");
            }
            else
            {
                //should be a hut
                name = LanguageHandler.format("tile.minecolonies.blockHut" + name + ".name");
            }
        }

        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(name);
        updateStyles();
    }

    /**
     * get the name of the current section as displayed on the button.
     */
    public String getSectionName()
    {
        if (sectionIndex <0 || sectionIndex >= sections.size())
        {
            Log.getLogger().error("Could not get section name for index " + sectionIndex + "(size:" + sections.size() + ")");
            return "";
        }
        return sections.get(sectionIndex);
    }

    /**
     * Change to the next style.
     */
    public void nextStyle()
    {
        if (styles.size() == 0)
        {
            setStyle(0);
        }
        else
        {
            setStyle((styleIndex + 1) % styles.size());
        }

    }

    /**
     * set the current Style and update schematics accordingly.
     */
    public void setStyle(int index)
    {
        styleIndex = index;
        findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(getStyleName());
        updateSchematics();
    }

    /**
     * get the name of the current style as displayed on the button.
     */
    public String getStyleName()
    {
        if (styleIndex <0 || styleIndex >= styles.size())
        {
            Log.getLogger().error("Could not get style name for index " + styleIndex + "(size:" + styles.size() + ")");
            return "";
        }
        return styles.get(styleIndex);
    }

    /**
     * Update the styles list but try to keep the same one.
     */
    public void updateStyles()
    {
        final String currentStyle = getStyleName();
        styles = Structures.getStylesFor(getSectionName());
        int newIndex = styles.indexOf(currentStyle);
        if (newIndex == -1)
        {
            newIndex = 0;
        }

        findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setEnabled(styles.size() > 1);
        setStyle(newIndex);
    }

    /**
     * Go to the next schematic.
     */
    public void nextSchematic()
    {
        if (schematics.size() == 0)
        {
            setSchematic(0);
        }
        else
        {
            setSchematic((schematicIndex + 1) % schematics.size());
        }
    }

    /**
     * Set the current schematic.
     */
    public void setSchematic(int index)
    {
        schematicIndex = index;
        Structures.StructureName sn = new Structures.StructureName(getSchematicName());
        findPaneOfTypeByID(BUTTON_SCHEMATIC_ID, Button.class).setLabel(sn.getLocalizedName());
        changeSchematic();
    }

    /**
     * get the name of the schematic as displayed in the button.
     */
    public String getSchematicName()
    {
        if (schematicIndex <0 || schematicIndex >= schematics.size())
        {
            Log.getLogger().error("Could not get schematic name for index " + schematicIndex + "(size:" + schematics.size() + ")");
            return "";
        }
        return schematics.get(schematicIndex);
    }

    /**
     * Update the list a available schematics.
     */
    public void updateSchematics()
    {
        final String schematic = getSchematicName();
        final String currentSchematic = (schematic.isEmpty())?"":(new Structures.StructureName(getSchematicName())).getSchematic();
        String section = getSectionName();
        String style = getStyleName();
        schematics = Structures.getSchematicsFor(section, style);
        int newIndex = -1;
        for (int i = 0 ; i < schematics.size();i++)
        {
            Log.getLogger().info("updateSchematics: schematic = " + schematics.get(i));
            Structures.StructureName sn = new Structures.StructureName(schematics.get(i));
            if (sn.getSchematic().equals(currentSchematic))
            {
                newIndex = i;
                break;
            }
        }

        if (newIndex == -1)
        {
            Log.getLogger().info("Can no keep the schematic "+ currentSchematic);
            newIndex = 0;
        }
        else
        {
           Log.getLogger().info("Keep the schematic "+ currentSchematic);
        }

        findPaneOfTypeByID(BUTTON_SCHEMATIC_ID, Button.class).setEnabled(schematics.size() > 1);
        setSchematic(newIndex);
    }
}
