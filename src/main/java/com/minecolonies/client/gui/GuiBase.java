package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public abstract class GuiBase extends GuiScreen
{
    //IDs for default layout
    protected final int BUTTON_HIRE_FIRE = 0, BUTTON_RECALL = 1, BUTTON_BUILD = 2, BUTTON_REPAIR = 3;
    protected final ResourceLocation background  = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiHutBackground.png");
    protected final int              buttonWidth = 116, buttonHeight = 20, buttonSpan = 4, labelSpan = 11;
    protected int middleX, middleY, topY, xSize, ySize, buttonMiddleX, buttonMiddleY;

    protected final EntityPlayer player;
    protected final World        world;
    protected final int          x, y, z;

    private ArrayList<GuiModIcon> iconList;

    public GuiBase(EntityPlayer player, World world, int x, int y, int z)
    {
        super();

        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        xSize = 171;
        ySize = 247;
        iconList = new ArrayList<GuiModIcon>();
    }

    protected void addElements()
    {
        middleX = width / 2;
        middleY = height / 2;
        topY = (height - ySize) / 2;
        buttonMiddleX = middleX - buttonWidth / 2;
        buttonMiddleY = middleY - buttonHeight / 2;

        buttonList.clear();
        labelList.clear();
        iconList.clear();
    }

    protected GuiButton addBottomButton(int id, String text, int x, int w, int h)
    {
        return addButton(id, text, x, topY + ySize - labelSpan * 3, w, h);
    }

    protected GuiButton addButton(int id, String text, int x, int y, int w, int h)
    {
        GuiButton button = new GuiButton(id, x, y, w, h, text);
        buttonList.add(button);
        return button;
    }

    protected void addLabel(String text, int x, int y)
    {
        addLabel(text, x, y, 0x000000);
    }

    protected void addLabel(String text, int x, int y, int color)
    {
        labelList.add(new GuiModLabel(text, x, y, color));
    }

    protected void addCenteredLabel(String text, int y)
    {
        addCenteredLabel(text, y, 0x000000);
    }

    protected void addCenteredLabel(String text, int y, int color)
    {
        addLabel(text, middleX - fontRendererObj.getStringWidth(text) / 2, y, color);
    }

    protected void addIcon(ItemStack is, int x, int y)
    {
        iconList.add(new GuiModIcon(is, x, y));
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type)
    {
        addDefaultWorkerLayout(hutName, workerName, level, type, 0);
    }

    protected void addDefaultWorkerLayout(String hutName, String workerName, String level, String type, int yPadding)
    {
        String workerAssigned = LanguageHandler.format("com.minecolonies.gui.workerHuts.workerAssigned");
        String workerLevel = LanguageHandler.format("com.minecolonies.gui.workerHuts.workerLevel", level);
        String buildType = LanguageHandler.format("com.minecolonies.gui.workerHuts.buildType");

        addCenteredLabel(hutName, topY + yPadding, 0xff0000);
        addCenteredLabel(workerAssigned, topY + yPadding + 18);
        addCenteredLabel(workerName, topY + yPadding + 28);
        addCenteredLabel(workerLevel, topY + yPadding + 44);
        addButton(BUTTON_HIRE_FIRE, LanguageHandler.format("com.minecolonies.gui.workerHuts.hire"), buttonMiddleX, topY + yPadding + 64, buttonWidth, buttonHeight);
        addButton(BUTTON_RECALL, LanguageHandler.format("com.minecolonies.gui.workerHuts.recall"), buttonMiddleX, topY + yPadding + 88, buttonWidth, buttonHeight);
        addButton(BUTTON_BUILD, LanguageHandler.format("com.minecolonies.gui.workerHuts.build"), buttonMiddleX, topY + yPadding + 120, buttonWidth, buttonHeight);
        addButton(BUTTON_REPAIR, LanguageHandler.format("com.minecolonies.gui.workerHuts.repair"), buttonMiddleX, topY + yPadding + 144, buttonWidth, buttonHeight);
        addCenteredLabel(buildType, topY + yPadding + 172);
        addCenteredLabel(type, topY + yPadding + 182);
    }

    protected int getSameCenterX(String... strings)
    {
        int x = middleX;
        int maxStringWidth = 0;
        for(String string : strings)
        {
            int stringWidth = fontRendererObj.getStringWidth(string);
            if(stringWidth > maxStringWidth)
            {
                maxStringWidth = stringWidth;
                x = middleX - stringWidth / 2;
            }
        }
        return x;
    }

    protected void drawGuiBackground()
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        int xCoord = (width - xSize) / 2;
        int yCoord = (height - ySize - 10) / 2;
        drawTexturedModalRect(xCoord, yCoord, 0, 0, xSize, ySize);
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        switch(guiButton.id)
        {
            case BUTTON_HIRE_FIRE:
                if(guiButton.displayString.equals(LanguageHandler.format("com.minecolonies.gui.workerHuts.hire")))
                {
                    //TODO: hire worker
                    guiButton.displayString = LanguageHandler.format("com.minecolonies.gui.workerHuts.fire");
                }
                else
                {
                    //TODO: fire worker
                    guiButton.displayString = LanguageHandler.format("com.minecolonies.gui.workerHuts.hire");
                }
                break;
            case BUTTON_RECALL:
                //TODO recall
                break;
            case BUTTON_BUILD:
                MineColonies.network.sendToServer(new BuildRequestMessage(x, y, z, BuildRequestMessage.BUILD));
                break;
            case BUTTON_REPAIR:
                MineColonies.network.sendToServer(new BuildRequestMessage(x, y, z, BuildRequestMessage.REPAIR));
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float par3)
    {
        drawGuiBackground();

        int k;
        for(k = 0; k < this.buttonList.size(); k++)
        {
            ((GuiButton) this.buttonList.get(k)).drawButton(this.mc, mouseX, mouseY);
        }
        for(k = 0; k < this.labelList.size(); k++)
        {
            ((GuiModLabel) this.labelList.get(k)).drawLabel(this.mc);
        }
        for(k = 0; k < this.iconList.size(); k++)
        {
            (this.iconList.get(k)).drawIcon(this.mc, itemRender);
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void initGui()
    {
        addElements();
        super.initGui();
    }

    @Override
    protected void keyTyped(char character, int keyCode)
    {
        if(keyCode == Keyboard.KEY_ESCAPE || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.thePlayer.closeScreen();
        }
    }
}
