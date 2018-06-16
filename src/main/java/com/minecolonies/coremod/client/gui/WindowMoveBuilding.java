package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.BlockUtils;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.StructureName;
import com.minecolonies.coremod.colony.Structures;
import com.minecolonies.coremod.colony.buildings.views.AbstractBuildingView;
import com.minecolonies.coremod.network.messages.BuildingMoveMessage;
import com.minecolonies.coremod.network.messages.SchematicRequestMessage;
import com.minecolonies.structures.helpers.Settings;
import com.minecolonies.structures.helpers.Structure;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Move Building Window.
 */
public class WindowMoveBuilding extends AbstractWindowSkeleton
{
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
     * Current position the hut/decoration is rendered at.
     */
    @NotNull
    private final BlockPos pos;

    /**
     * Name of the static schematic if existent.
     */
    private final String schematicName;

    /**
     * Building related to this.
     */
    private final AbstractBuildingView building;

    /**
     * Creates a window move building for a specific structure.
     *
     * @param pos           the position of the building.
     * @param building      the building.
     * @param schematicName the schematic name.
     */
    public WindowMoveBuilding(@Nullable final BlockPos pos, final AbstractBuildingView building, final String schematicName)
    {
        super(Constants.MOD_ID + MOVE_BUILDING_SOURCE_SUFFIX);
        this.building = building;
        this.schematicName = schematicName;
        this.pos = pos;

        this.init();
    }

    /**
     * Inititate button handlers.
     */
    private void init()
    {
        //Register all necessary buttons with the window.
        registerButton(BUTTON_CONFIRM, this::confirmClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);
        registerButton(BUTTON_LEFT, this::moveLeftClicked);
        registerButton(BUTTON_MIRROR, WindowMoveBuilding::mirror);
        registerButton(BUTTON_RIGHT, this::moveRightClicked);
        registerButton(BUTTON_BACKWARD, this::moveBackClicked);
        registerButton(BUTTON_FORWARD, this::moveForwardClicked);
        registerButton(BUTTON_UP, WindowMoveBuilding::moveUpClicked);
        registerButton(BUTTON_DOWN, WindowMoveBuilding::moveDownClicked);
        registerButton(BUTTON_ROTATE_RIGHT, this::rotateRightClicked);
        registerButton(BUTTON_ROTATE_LEFT, this::rotateLeftClicked);
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private static void mirror()
    {
        Settings.instance.mirror();
    }

    /**
     * Called when the window is opened.
     * Sets up the buttons for either hut mode or decoration mode.
     */
    @Override
    public void onOpened()
    {
        if(Settings.instance.getActiveStructure() == null)
        {
            Settings.instance.setRotation(building.getRotation());
            if(building.isMirrored())
            {
                Settings.instance.mirror();
            }
            Settings.instance.setPosition(pos);
            final StructureName structureName = new StructureName(Structures.SCHEMATICS_PREFIX, schematicName ,
                    building.getSchematicName() + building.getBuildingLevel());
            final Structure structure = new Structure(null,
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
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (ColonyManager.isSchematicDownloaded())
        {
            ColonyManager.setSchematicDownloaded(false);
        }
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
        updateRotation((Settings.instance.getRotation() + ROTATE_RIGHT) % POSSIBLE_ROTATIONS);
    }

    /**
     * Rotate the structure counter clockwise.
     */
    private void rotateLeftClicked()
    {
        updateRotation((Settings.instance.getRotation() + ROTATE_LEFT) % POSSIBLE_ROTATIONS);
    }


    /*
     * ---------------- Miscellaneous ----------------
     */

    /**
     * Send a packet telling the server to place the current structure.
     */
    private void confirmClicked()
    {
        final StructureName structureName = new StructureName(Settings.instance.getStructureName());
        if (structureName.getPrefix().equals(Structures.SCHEMATICS_SCAN) && FMLCommonHandler.instance().getMinecraftServerInstance() == null)
        {
            //We need to check that the server have it too using the md5
            WindowBuildTool.requestScannedSchematic(structureName, false, false);
        }
        else
        {
            MineColonies.getNetwork().sendToServer(new BuildingMoveMessage(
                    structureName.toString(),
                    structureName.toString(),
                    Settings.instance.getPosition(),
                    Settings.instance.getRotation(),
                    Settings.instance.getMirror(), building));
        }

        if (!GuiScreen.isShiftKeyDown())
        {
            cancelClicked();
        }
    }

    /**
     * Cancel the current structure.
     */
    private void cancelClicked()
    {
        building.openGui(false);
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
}
