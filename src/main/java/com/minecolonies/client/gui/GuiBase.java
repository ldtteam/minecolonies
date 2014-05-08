package com.minecolonies.client.gui;

import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiBase extends GuiScreen
{
    protected int xSize;
    protected int ySize;
    protected final ResourceLocation background = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiInformatorBackground.png");

    protected void drawGuiForeground(){}

    protected void addButtons(){}

    public GuiBase()
    {
        xSize = 171;
        ySize = 247;
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
    public void drawScreen(int par1, int par2, float par3)
    {
        drawGuiBackground();
        drawGuiForeground();

        for(int k = 0; k < this.buttonList.size(); ++k)
        {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            guibutton.drawButton(this.mc, par1, par2);
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
        addButtons();
        super.initGui();
    }
}
