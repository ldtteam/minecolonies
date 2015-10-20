package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.Window;
import com.github.lunatrius.schematica.Settings;
import com.github.lunatrius.schematica.world.SchematicWorld;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyManager;
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

/**
 * BuildTool Window
 *
 * @author Colton
 */
public class WindowBuildTool extends Window implements Button.Handler
{
    private static final String BUTTON_TYPE_ID = "buildingType";
    private static final String BUTTON_HUT_ID = "hut";
    private static final String BUTTON_STYLE_ID = "style";
    private static final String BUTTON_DECORATION_ID = "decoration";

    private static final String BUTTON_CONFIRM = "confirm";
    private static final String BUTTON_CANCEL = "cancel";

    private static final String BUTTON_ROTATE_LEFT = "rotateLeft";
    private static final String BUTTON_ROTATE_RIGHT = "rotateRight";

    private static final String BUTTON_UP = "up";
    private static final String BUTTON_DOWN = "down";

    private static final String BUTTON_FORWARD = "forward";
    private static final String BUTTON_BACK = "back";
    private static final String BUTTON_LEFT = "left";
    private static final String BUTTON_RIGHT = "right";

    private boolean inHutMode = true;

    private List<String> huts = new ArrayList<>();
    private int hutDecIndex = 0;
    private int styleIndex = 0;

    private int posX, posY, posZ;
    private int rotation = 0;

    public WindowBuildTool(int x, int y, int z)
    {
        super(Constants.MOD_ID + ":gui/windowBuildTool.xml");

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
        if(inHutMode)
        {
            findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.hut"));

            InventoryPlayer inventory = this.mc.thePlayer.inventory;

            for (String hut : Schematics.getHuts())
            {
                if (inventory.hasItem(Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut).getItem(null, 0, 0, 0)) && Schematics.getStylesForHut(hut) != null)
                {
                    huts.add(hut);
                }
            }

            if(huts.size() > 0)
            {
                if(MineColonies.proxy.getActiveSchematic() != null)
                {
                    hutDecIndex = Math.max(0, huts.indexOf(Settings.instance.hut));
                    styleIndex = Math.max(0, Schematics.getStylesForHut(huts.get(hutDecIndex)).indexOf(Settings.instance.style));
                }

                Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);
                //TODO Localize
                hut.setLabel(huts.get(hutDecIndex));
                hut.setEnabled(true);

                Button style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class);
                style.setVisible(true);
                style.setLabel(Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex));

                //Render stuff
                if(MineColonies.proxy.getActiveSchematic() == null)
                {
                    changeSchematic();
                }
            }
            else
            {
                Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);
                hut.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.nohut"));
                hut.setEnabled(false);

                MineColonies.proxy.setActiveSchematic(null);
            }
        }
        else
        {
            Button type = findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class);
            type.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.decoration"));
            type.setEnabled(false);//TODO disabled for now

            //TODO do stuff with decoration button
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
            findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).setLabel(huts.get(hutDecIndex));
            styleIndex = 0;
            findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex));

            changeSchematic();
            break;
        case BUTTON_STYLE_ID:
            List<String> styles = Schematics.getStylesForHut(huts.get(hutDecIndex));
            styleIndex = (styleIndex + 1) % styles.size();
            findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(styles.get(styleIndex));

            changeSchematic();
            break;
        case BUTTON_DECORATION_ID:
            //TODO
            break;

        case BUTTON_CONFIRM:
            MineColonies.network.sendToServer(new BuildToolPlaceMessage(huts.get(hutDecIndex), Schematics.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex), posX, posY, posZ, rotation));
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
            MineColonies.logger.warn("WindowBuildTool: Unhandled Button ID: " + button.getID());
        }
    }

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

    private void changeSchematic()
    {
        if(MineColonies.proxy.isClient() && FMLCommonHandler.instance().getEffectiveSide().isClient())
        {
            String hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class).getLabel();
            String style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).getLabel();

            SchematicWorld schematic = Schematic.loadSchematic(this.mc.theWorld, style + '/' + hut + '1').getWorldForRender();
            MineColonies.proxy.setActiveSchematic(schematic);

            Settings.instance.renderBlocks = new RenderBlocks(schematic);
            Settings.instance.createRendererSchematicChunk();

            updatePosition();

            schematic.setRendering(true);
        }
    }

    private void updatePosition()
    {
        Settings.instance.moveTo(posX, posY, posZ);
    }
}
