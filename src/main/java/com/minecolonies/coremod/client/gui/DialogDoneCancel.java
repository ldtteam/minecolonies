package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.Text;
import com.minecolonies.blockout.Loader;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.blockout.OverlayView;
import com.minecolonies.coremod.util.Log;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Manage windows and their events.
 */
public class DialogDoneCancel extends OverlayView
{
    public static final int CANCEL = 0;
    public static final int DONE   = 1;

    /**
     * Resource suffix.
     */
    private static final String DIALOG_OK_CANCEL_SUFFIX = ":gui/dialogdonecancel.xml";

    protected final Label titleLabel;
    protected final Text contentText;
    protected final Button doneButton;
    protected final Button cancelButton;
    protected Handler handler;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param resource Resource location string.
     */
    public DialogDoneCancel(final Window window)
    {
        super();
        Loader.createFromXMLFile(Constants.MOD_ID + DIALOG_OK_CANCEL_SUFFIX, this);
        titleLabel   = findPaneOfTypeByID("title", Label.class);
        contentText  = findPaneOfTypeByID("textcontent", Text.class);
        doneButton   = findPaneOfTypeByID("done", Button.class);
        cancelButton = findPaneOfTypeByID("cancel", Button.class);
        this.window  = window;
    }

    public void setTitle(final String title)
    {
        titleLabel.setLabelText(title);
    }

    public String getTitle()
    {
        return titleLabel.getLabelText();
    }

    public void setTextContent(final String content)
    {
        contentText.setTextContent(content);
    }

    public String getTextContent()
    {
        return contentText.getTextContent();
    }

    public void onButtonClicked(Button button)
    {
        this.setVisible(false);
        if (button == doneButton)
        {
            handler.onDialogClosed(this, DONE);
        }
        else
        {
            handler.onDialogClosed(this, CANCEL);
        }
    }

    @Override
    public void setVisible(final boolean visible)
    {
        this.putInside(window);
        super.setVisible(visible);
    }

    public void open()
    {
        setVisible(true);
    }

    public void close()
    {
        setVisible(true);
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final Handler h)
    {
        handler = h;
    }

    /**
     * Used for windows that have dialog message.
     */
    @FunctionalInterface
    public interface Handler
    {
        /**
         * Called when a button is clicked.
         *
         * @param dialog the dialog that was closed.
         * @param done whether it is done or cancel.
         */
        void onDialogClosed(final DialogDoneCancel dialog, final int buttonId);
    }
}
