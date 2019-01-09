package com.minecolonies.blockout.controls;

import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Base button class.
 * Has a handler for when the button is clicked.
 */
public class Button extends Pane
{
    protected ButtonHandler handler;
    protected String        label;

    /**
     * Gui Timer for reenabling the Button
     */
    Timer timer = new Timer();

    /**
     * The delay for disabling the button in milliseconds.
     */
    private int buttonDisabledTime = 500;

    /**
     * Default constructor.
     */
    public Button()
    {
        super();
    }

    /**
     * Constructor used when loading from xml.
     *
     * @param params PaneParams from xml file.
     */
    public Button(@NotNull final PaneParams params)
    {
        super(params);
        label = params.getLocalizedStringAttribute("label", label);
    }

    /**
     * Button textContent getter.
     *
     * @return button textContent.
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * Button textContent setter.
     *
     * @param s new textContent.
     */
    public void setLabel(final String s)
    {
        label = s;
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final ButtonHandler h)
    {
        handler = h;
    }

    /**
     * Play click sound and find the proper handler.
     *
     * @param mx mouse X coordinate, relative to Pane's top-left
     * @param my mouse Y coordinate, relative to Pane's top-left
     */
    @Override
    public void handleClick(final int mx, final int my)
    {
        if (buttonDisabledTime > 0)
        {
            disable();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    enable();
                }
            }, buttonDisabledTime);
        }

        mc.getSoundHandler().playSound(PositionedSoundRecord.getMusicRecord(SoundEvents.UI_BUTTON_CLICK));

        ButtonHandler delegatedHandler = handler;

        if (delegatedHandler == null)
        {
            //  If we do not have a designated handler, find the closest ancestor that is a Handler
            for (Pane p = parent; p != null; p = p.getParent())
            {
                if (p instanceof ButtonHandler)
                {
                    delegatedHandler = (ButtonHandler) p;
                    break;
                }
            }
        }

        if (delegatedHandler != null)
        {
            delegatedHandler.onButtonClicked(this);
        }
    }

    /**
     * Set the time the Button is disabled after clicking
     *
     * @param buttonDisabledTime the time to set, use 0 to stop from disabling.
     */
    public void setButtonClickDisabledTime(final int buttonDisabledTime)
    {
        this.buttonDisabledTime = buttonDisabledTime;
    }

    /**
     * Get the time buttons are disabled after usage
     *
     * @return Disabled time
     */
    public int setButtonClickDisabledTime()
    {
        return buttonDisabledTime;
    }
}
