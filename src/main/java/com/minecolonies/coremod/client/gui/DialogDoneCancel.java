package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Loader;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.OverlayView;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;

import java.util.function.ObjIntConsumer;

/**
 * Manage windows and their events.
 */
public class DialogDoneCancel extends OverlayView implements ButtonHandler
{
    /**
     * buttonid when cancel is pressed.
     */
    public static final int CANCEL = 0;

    /**
     * buttonid when done is pressed.
     */
    public static final int DONE = 1;

    /**
     * Resource suffix.
     */
    private static final String DIALOG_OK_CANCEL_SUFFIX = ":gui/dialogdonecancel.xml";

    /**
     * Label for the title of the dialog.
     */
    protected final Label titleLabel;

    /**
     * Text for the text content of the dialog.
     */
    protected final Text contentText;

    /**
     * Done button.
     */
    protected final Button doneButton;

    /**
     * Cancel button.
     */
    protected final Button cancelButton;

    /**
     * Handler for the onCloseDialog event.
     */
    protected ObjIntConsumer<DialogDoneCancel> handler;

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
    }

    /**
     * Get the title of the dialog.
     *
     * @return title for the dialog
     */
    public String getTitle()
    {
        return titleLabel.getLabelText();
    }

    /**
     * Set the title of the dialog.
     *
     * @param title for the dialog
     */
    public void setTitle(final String title)
    {
        titleLabel.setLabelText(title);
    }

    /**
     * Get the textual content of the dialog.
     *
     * @return The textual content displayed in the dialog
     */
    public String getTextContent()
    {
        return contentText.getTextContent();
    }

    /**
     * Set the textual content of the dialog.
     *
     * @param content to display in the dialog
     */
    public void setTextContent(final String content)
    {
        contentText.setTextContent(content);
    }

    /**
     * When a button have been cicked on.
     *
     * @param button which have been clicked on.
     */
    public void onButtonClicked(final Button button)
    {
        this.setVisible(false);
        if (handler == null)
        {
            Log.getLogger().error("DialogDoneCancel does not have a handler.");
            return;
        }
        if (button == doneButton)
        {
            handler.accept(this, DONE);
        }
        else
        {
            handler.accept(this, CANCEL);
        }
    }

    @Override
    public void setVisible(final boolean visible)
    {
        if (visible)
        {
            setPosition(0, 0);
            setSize(window.getInteriorWidth(), window.getInteriorHeight());
            //Make sure we are on top
            putInside(window);
        }
        super.setVisible(visible);
    }

    /**
     * Open the dialog.
     */
    public void open()
    {
        setVisible(true);
    }

    /**
     * Close the dialog.
     */
    public void close()
    {
        setVisible(true);
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final ObjIntConsumer<DialogDoneCancel> h)
    {
        handler = h;
    }
}
