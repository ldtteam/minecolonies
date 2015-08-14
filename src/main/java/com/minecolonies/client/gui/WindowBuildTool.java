package com.minecolonies.client.gui;

import com.blockout.controls.Button;
import com.blockout.views.Window;
import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildToolPlaceMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;

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

        posX = x;
        posY = y;
        posZ = z;
    }

    @Override
    public void onOpened()
    {
        if(inHutMode)
        {
            findPaneOfTypeByID(BUTTON_TYPE_ID, Button.class).setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.hut"));

            InventoryPlayer inventory = Minecraft.getMinecraft().thePlayer.inventory;
            for (String hut : ColonyManager.getHuts())
            {
                if (inventory.hasItem(Block.getBlockFromName(Constants.MOD_ID + ":blockHut" + hut).getItem(null, 0, 0, 0)))//Hope someone doesn't override this because the block isnt in the world
                {
                    huts.add(hut);
                }
            }

            if(huts.size() > 0)
            {
                Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);
                //TODO Localize
                hut.setLabel(huts.get(hutDecIndex));
                hut.setEnabled(true);

                Button style = findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class);
                style.setVisible(true);
                style.setLabel(ColonyManager.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex));
            }
            else
            {
                Button hut = findPaneOfTypeByID(BUTTON_HUT_ID, Button.class);
                hut.setLabel(LanguageHandler.getString("com.minecolonies.gui.buildtool.nohut"));
                hut.setEnabled(false);
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
            findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(ColonyManager.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex));
            break;
        case BUTTON_STYLE_ID:
            List<String> styles = ColonyManager.getStylesForHut(huts.get(hutDecIndex));
            styleIndex = (styleIndex + 1) % styles.size();
            findPaneOfTypeByID(BUTTON_STYLE_ID, Button.class).setLabel(styles.get(styleIndex));
            break;
        case BUTTON_DECORATION_ID:
            //TODO
            break;

        case BUTTON_CONFIRM:
            MineColonies.network.sendToServer(new BuildToolPlaceMessage(huts.get(hutDecIndex), ColonyManager.getStylesForHut(huts.get(hutDecIndex)).get(styleIndex), posX, posY, posZ, rotation));
            close();
            break;
        case BUTTON_CANCEL:
            close();
            break;

        //TODO: account for player facing direction
        case BUTTON_LEFT:
            posX--;
            break;
        case BUTTON_RIGHT:
            posX++;
            break;
        case BUTTON_FORWARD:
            posZ--;
            break;
        case BUTTON_BACK:
            posZ++;
            break;
        case BUTTON_UP:
            posY++;
            break;
        case BUTTON_DOWN:
            posY--;
            break;

        case BUTTON_ROTATE_LEFT:
            rotation = (rotation+3) % 4;
            break;
        case BUTTON_ROTATE_RIGHT:
            rotation = (rotation+1) % 4;
            break;

        default:
            MineColonies.logger.warn("WindowBuildTool: Unhandled Button ID: " + button.getID());
        }
    }
}
