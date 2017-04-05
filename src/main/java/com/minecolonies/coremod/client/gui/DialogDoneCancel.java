package com.minecolonies.coremod.client.gui;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.controls.Text;
import com.minecolonies.blockout.Loader;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.lib.Constants;
import com.minecolonies.blockout.OverlayView;
import com.minecolonies.coremod.util.Log;

/**
 * Manage windows and their events.
 */
public class DialogDoneCancel extends OverlayView implements Button.Handler
{
    public static final int CANCEL = 0;
    public static final int DONE   = 1;

    /**
     * Resource suffix.
     */
    private static final String DIALOG_OK_CANCEL_SUFFIX = ":gui/dialogdonecancel.xml";

    protected final Label   titleLabel;
    protected final Text    contentText;
    protected final Button  doneButton;
    protected final Button  cancelButton;
    protected       Handler handler;

    /**
     * Constructor for the DialogDoneCancel class.
     *
     * @param window in which the dialog will be displayed
     */
    public DialogDoneCancel(final Window window)
    {
        super();
        Loader.createFromXMLFile(Constants.MOD_ID + DIALOG_OK_CANCEL_SUFFIX, this);
        titleLabel = findPaneOfTypeByID("title", Label.class);
        contentText = findPaneOfTypeByID("textcontent", Text.class);
        doneButton = findPaneOfTypeByID("done", Button.class);
        cancelButton = findPaneOfTypeByID("cancel", Button.class);
        doneButton.setHandler(this);
        cancelButton.setHandler(this);
        this.window = window;
        setPosition(0, 0);
        setVisible(false);
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
        if (handler == null)
        {
            Log.getLogger().error("DialogDoneCancel does not have a handler.");
            return;
        }
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
        if (visible)
        {
            setSize(window.getInteriorWidth(), window.getInteriorHeight());
            //Make sure we are on top
            putInside(window);
        }
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
         * Called then dialog is close by a button.
         *
         * @param dialog the dialog that was closed.
         * @param buttonId   whether it is {DONE} or {CANCEL}.
         */
        void onDialogClosed(final DialogDoneCancel dialog, final int buttonId);
    }
}
