package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.SwitchView;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.ClickGuiButtonTriggerMessage;
import com.minecolonies.coremod.network.messages.server.OpenGuiWindowTriggerMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Manage windows and their events.
 */
public abstract class AbstractWindowSkeleton extends Window implements ButtonHandler
{
    @NotNull
    private final HashMap<String, Consumer<Button>> buttons;
    
    /**
     * Panes used by the generic page handler
     */
    protected final Label      pageNum;
    protected final Button     buttonPrevPage;
    protected final Button     buttonNextPage;
    protected       SwitchView switchView;

    /**
     * This window's resource location
     */
    private String resource;

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param resource Resource location string.
     */
    public AbstractWindowSkeleton(final String resource)
    {
        super(resource);
        this.resource = resource;

        buttons = new HashMap<>();

        buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
        buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
        pageNum = findPaneOfTypeByID(LABEL_PAGE_NUMBER, Label.class);
        switchView = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class);

        Network.getNetwork().sendToServer(new OpenGuiWindowTriggerMessage(this.resource));
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Runnable action)
    {
        registerButton(id, (button) -> action.run());
    }

    /**
     * Register a button on the window.
     *
     * @param id     Button ID.
     * @param action Consumer with the action to be performed.
     */
    public final void registerButton(final String id, final Consumer<Button> action)
    {
        buttons.put(id, action);
    }

    /**
     * Handle a button clicked event.
     * Find the registered event and execute that.
     * <p>
     * todo: make final once migration is complete
     *
     * @param button the button that was clicked.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_PREVPAGE:
                setPage(BUTTON_PREVPAGE);
                return;
            case BUTTON_NEXTPAGE:
                setPage(BUTTON_NEXTPAGE);
                return;
            default:
                break;
        }

        if (buttons.containsKey(button.getID()))
        {
            buttons.get(button.getID()).accept(button);
            Network.getNetwork().sendToServer(new ClickGuiButtonTriggerMessage(button.getID(), this.resource));
        }
    }

    /**
     * Button clicked without an action. Method does nothing.
     *
     * @param ignored Parameter is ignored. Since some actions require a button, we must accept a button parameter.
     */
    public final void doNothing(final Button ignored)
    {
        //do nothing with that event
    }

    /**
     * Generic page handler, uses common ids
     * 
     * @param button Button pressed in a GUI
     */
    public void setPage(@NotNull final String button)
    {
        final int switchPagesSize = switchView.getChildrenSize();
        int curPage = pageNum.getLabelText().isEmpty() ? 1 : Integer.parseInt(pageNum.getLabelText().substring(0, pageNum.getLabelText().indexOf("/")));

        switch (button)
        {
            case BUTTON_PREVPAGE:
                switchView.previousView();
                curPage--;
                break;
            case BUTTON_NEXTPAGE:
                switchView.nextView();
                curPage++;
                break;
            default:
                if (switchPagesSize == 1)
                {
                    buttonPrevPage.off();
                    buttonNextPage.off();
                    pageNum.off();
                    return;
                }
                break;
        }

        buttonNextPage.on();
        buttonPrevPage.on();
        if (curPage == 1)
        {
            buttonPrevPage.off();
        }
        if (curPage == switchPagesSize)
        {
            buttonNextPage.off();
        }
        pageNum.setLabelText(curPage + "/" + switchPagesSize);
    }
}
