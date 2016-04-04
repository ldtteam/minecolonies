package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.Window;
import com.schematica.Settings;
import com.schematica.world.SchematicWorld;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.Schematics;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildToolPlaceMessage;
import com.minecolonies.util.LanguageHandler;
import com.minecolonies.util.Schematic;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.InventoryPlayer;
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
    private static final    String          BUTTON_TYPE_ID              = "buildingType";
    private static final    String          BUTTON_HUT_ID               = "hut";
    private static final    String          BUTTON_STYLE_ID             = "style";
    private static final    String          BUTTON_DECORATION_ID        = "decoration";

    private static final    String          BUTTON_CONFIRM              = "confirm";
    private static final    String          BUTTON_CANCEL               = "cancel";

    private static final    String          BUTTON_ROTATE_LEFT          = "rotateLeft";
    private static final    String          BUTTON_ROTATE_RIGHT         = "rotateRight";

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

    private                 int             posX;
    private                 int             posY;
    private                 int             posZ;
    private                 int             rotation                    = 0;

    /**
     * Creates a window build tool
     * This requires X, Y and Z coordinates
     * If a schematic is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used
     *
     * @param x     x-coordinate
     * @param y     y-coordinate
     * @param z     z-coordinate
     */
    public WindowBuildTool(int x, int y, int z)
    {
        super(Constants.MOD_ID + BUILD_TOOL_RESOURCE_SUFFIX);

        if(MineColonies.proxy.getActiveSchematic() != null)
        {
            posX = (int) Settings.instance.offset.x + MineColonies.proxy.getActiveSchematic().getOffsetX();
            posY = (int) Settings.instance.offset.y + MineColonies.proxy.getActiveSchematic().getOffsetY();
            posZ = (int) Settings.instance.offset.z + MineColonies.proxy.getActiveSchematic().getOffsetZ();
            rotation = Settings.instance.rotation;
        }
        else
        {
            posX = x;
            posY = y;
            posZ = z;
        }
    }

    @Override
    public void onOpened()
    {
        boolean inHutMode = true;
        //TODO is this not redundant? see no usage
        if (!inHutMode)
        {
            Button type = findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class);
            type.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.decoration"));
            type.setEnabled(false);//TODO disabled for now

            //TODO do stuff with decoration button
            return;
        }

        findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.hut"));


        InventoryPlayer inventory = this.mc.thePlayer.inventory;

            /*
            Add possible huts (has item) to list, if it has a schematic, and player has the block
             */
        huts.addAll(Schematics.getHuts().stream().filter(hut -> inventory.hasItem(
                Block.getBlockFromName(Constants.MOD_ID + HUT_PREFIX + hut).getItem(null, 0, 0, 0))
                                                                && Schematics.getStylesForHut(hut) != null).collect(
                Collectors.toList()));

        if (huts.size() > 0)
        {
            if (MineColonies.proxy.getActiveSchematic() != null)
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
            if (MineColonies.proxy.getActiveSchematic() == null)
            {
                changeSchematic();
            }
        } else
        {
            Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);

            hut.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.nullHut"));
            hut.setEnabled(false);

            MineColonies.proxy.setActiveSchematic(null);
        }
    }

    @Override
    public void onClosed()
    {
        if(MineColonies.proxy.getActiveSchematic() != null)
        {
            Settings.instance.rotation = rotation;

            Settings.instance.hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).getLabel();
            Settings.instance.style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
    }

    @Override
    public boolean onKeyTyped(char ch, int key)
    {
        return super.onKeyTyped(ch, key);
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
            } catch (NullPointerException e)
            {
                MineColonies.logger.error("findPane error, report to mod authors",e);
            }
            changeSchematic();
            break;
        case BUTTON_DECORATION_ID:
            //TODO
            break;

        case BUTTON_CONFIRM:
            MineColonies.getNetwork().sendToServer(new BuildToolPlaceMessage(huts.get(hutDecIndex),
                    Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex), posX, posY, posZ, rotation));
            MineColonies.proxy.setActiveSchematic(null);
            close();
            break;
        case BUTTON_CANCEL:
            MineColonies.proxy.setActiveSchematic(null);
            close();
            break;
        case BUTTON_LEFT:
        case BUTTON_RIGHT:
        case BUTTON_FORWARD:
        case BUTTON_BACK:
            moveArrow(button.getID());
            break;
        case BUTTON_UP:
            posY++;
            updatePosition();
            break;
        case BUTTON_DOWN:
            posY--;
            updatePosition();
            break;

        case BUTTON_ROTATE_LEFT:
            rotation = (rotation+3) % 4;
            MineColonies.proxy.getActiveSchematic().rotate();
            MineColonies.proxy.getActiveSchematic().rotate();
            MineColonies.proxy.getActiveSchematic().rotate();
            updatePosition();
            break;
        case BUTTON_ROTATE_RIGHT:
            rotation = (rotation+1) % 4;
            MineColonies.proxy.getActiveSchematic().rotate();
            updatePosition();
            break;

        default:
            MineColonies.logger.warn("WindowBuildTool: Unhandled Button ID:" + button.getID());
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
                posX++;
                break;
            case 1:
                posZ++;
                break;
            case 2:
                posX--;
                break;
            case 3:
                posZ--;
                break;
            }
            break;
        case BUTTON_RIGHT:
            switch (facing)
            {
            case 0:
                posX--;
                break;
            case 1:
                posZ--;
                break;
            case 2:
                posX++;
                break;
            case 3:
                posZ++;
                break;
            }
            break;
        case BUTTON_FORWARD:
            switch (facing)
            {
            case 0:
                posZ++;
                break;
            case 1:
                posX--;
                break;
            case 2:
                posZ--;
                break;
            case 3:
                posX++;
                break;
            }
            break;
        case BUTTON_BACK:
            switch (facing)
            {
            case 0:
                posZ--;
                break;
            case 1:
                posX++;
                break;
            case 2:
                posZ++;
                break;
            case 3:
                posX--;
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
        if(MineColonies.isClient())
        {
            String hut = "";
            String style = "";

            hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).getLabel();
            style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

            SchematicWorld schematic = Schematic.loadSchematic(this.mc.theWorld, style + '/' + hut + '1').getWorldForRender();
            MineColonies.proxy.setActiveSchematic(schematic);

            Settings.instance.renderBlocks = new RenderBlocks(schematic);
            Settings.instance.createRendererSchematicChunk();

            updatePosition();

            schematic.setRendering(true);
        }
    }

    /**
     * Update position of the schematic
     */
    private void updatePosition()
    {
        Settings.instance.moveTo(posX, posY, posZ);
    }
}
