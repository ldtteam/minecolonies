package com.blockout;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class Button extends Pane
{
    public interface Handler {
        void onButtonClicked(Button button);
    }

    protected Handler handler;

    public Button() {}
    public Button(Button other)
    {
        super(other);
        this.handler = other.handler;
    }

    public Button(Pane.PaneInfo info)
    {
        super(info);
    }
    public Button(Pane.PaneInfo info, View view)
    {
        super(info, view);
    }

    public String getLabel() { return ""; }
    public void setLabel(String s) {}

    public void setHandler(Handler h)
    {
        handler = h;
    }

    @Override
    public void onMouseClicked(int mx, int my)
    {
        if (handler != null)
        {
            handler.onButtonClicked(this);
        }
    }
}
