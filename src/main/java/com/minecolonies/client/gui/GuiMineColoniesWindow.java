package com.minecolonies.client.gui;

import com.blockout.Window;
import com.minecolonies.lib.Constants;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public abstract class GuiMineColoniesWindow extends Window
{
    protected final ResourceLocation background = new ResourceLocation(Constants.MODID + ":" + "textures/gui/guiHutBackground.png");

    public GuiMineColoniesWindow()
    {
        super(171, 247);
    }

    @Override
    public boolean doesWindowPauseGame()
    {
        return false;
    }

//    @Override
//    protected void unhandledKeyTyped(char c, int code)
//    {
//        if(code == Keyboard.KEY_ESCAPE || code == this.mc.gameSettings.keyBindInventory.getKeyCode())
//        {
//            close();
//        }
//    }

    @Override
    protected void drawBackground()
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(background);
        drawTexturedModalRect(0, -5,
                0, 0,
                /*root.*/getWidth(), /*root.*/getHeight());
    }
}
