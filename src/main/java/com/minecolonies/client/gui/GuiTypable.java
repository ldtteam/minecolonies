package com.minecolonies.client.gui;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.ColonyView;
import com.minecolonies.lib.EnumGUI;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

public class GuiTypable extends GuiScreen
{
    private final int BUTTON_DONE = 0, BUTTON_CANCEL = 1;
    private final ColonyView   colony;
    private       GuiTextField guiTextField = null;
    private final String       title        = LanguageHandler.format("com.minecolonies.gui.townhall.rename.title");
    private       String       newCityName;
    private final EntityPlayer player;
    private final World        world;
    private final int          x, y, z;

    public GuiTypable(ColonyView colony, EntityPlayer player, World world, int x, int y, int z)
    {
        this.colony = colony;
        this.player = player;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;

        newCityName = colony.getName();
    }

    @Override
    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        //Do Not Move down, hides crosshair
        guiTextField = new GuiTextField(this.fontRendererObj, this.width / 2 - 75, this.height / 2 - 10, 150, 18);
        this.buttonList.add(new GuiButton(BUTTON_DONE, this.width / 2 - 100, this.height / 4 + 110, LanguageHandler.format("gui.done")));
        this.buttonList.add(new GuiButton(BUTTON_CANCEL, this.width / 2 - 100, this.height / 4 + 134, LanguageHandler.format("gui.cancel")));

        this.guiTextField.setMaxStringLength(128);
        this.guiTextField.setText(newCityName);
    }

    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
        this.guiTextField.mouseClicked(par1, par2, par3);
        super.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void keyTyped(char par1, int par2)
    {
        this.guiTextField.textboxKeyTyped(par1, par2);
        this.newCityName = this.guiTextField.getText();
        super.keyTyped(par1, par2);
    }

    @Override
    public void updateScreen()
    {
        this.guiTextField.updateCursorCounter();
        super.updateScreen();
        newCityName = guiTextField.getText();
    }

    @Override
    protected void actionPerformed(GuiButton guiButton)
    {
        if(guiButton.enabled)
        {
            switch(guiButton.id)
            {
                case BUTTON_DONE:
                    if(!newCityName.isEmpty())
                    {
                        colony.setName(newCityName);
                    }
                    player.openGui(MineColonies.instance, EnumGUI.TOWNHALL.getID(), world, x, y, z);
                    break;
                case BUTTON_CANCEL:
                    player.openGui(MineColonies.instance, EnumGUI.TOWNHALL.getID(), world, x, y, z);
                    break;
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return true;
    }

    @Override
    public void drawScreen(int par1, int par2, float par3)
    {
        super.drawScreen(par1, par2, par3);
        fontRendererObj.drawString(title, this.width / 2 - fontRendererObj.getStringWidth(title) / 2, this.height / 2 - 20, 0xffffff);
        this.guiTextField.drawTextBox();
    }
}
