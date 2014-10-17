package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.lib.Constants;
import com.minecolonies.network.messages.BuildRequestMessage;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public abstract class GuiBase extends GuiScreen
{
    //IDs for default layout
    protected final ResourceLocation background  = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiHutBackground.png");
    protected final int              buttonWidth = 116, buttonHeight = 20, buttonSpan = 4, labelSpan = 11;
    protected int middleX, middleY, topY, xSize, ySize, buttonMiddleX, buttonMiddleY;

    private ArrayList<GuiModIcon> iconList;

    public GuiBase()
    {
        super();

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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
