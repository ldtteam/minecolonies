package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.SwitchView;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.ClickGuiButtonTriggerMessage;
import com.minecolonies.coremod.network.messages.server.OpenGuiWindowTriggerMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Manage windows and their events.
 */
public abstract class AbstractWindowSkeleton extends BOWindow implements ButtonHandler
{
    @NotNull
    private final HashMap<String, Consumer<Button>> buttons;

    /**
     * Panes used by the generic page handler
     */
    protected final Text       pageNum;
    protected final Button     buttonPrevPage;
    protected final Button     buttonNextPage;
    protected       SwitchView switchView;

    /**
     * This window's resource location
     */
    private String resource;

    /**
     * This window's parent
     */
    @Nullable
    private BOWindow parent;

    /**
     * Constructor with no parent window
     *
     * @param resource Resource location string.
     */
    public AbstractWindowSkeleton(final String resource)
    {
        this(resource, null);
    }

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param resource Resource location string.
     */
    public AbstractWindowSkeleton(final String resource, @Nullable final BOWindow parent)
    {
        super(new ResourceLocation(resource));
        this.resource = resource;
        this.parent = parent;

        buttons = new HashMap<>();

        switchView = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class);
        if (switchView != null)
        {
            buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
            buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
            pageNum = findPaneOfTypeByID(LABEL_PAGE_NUMBER, Text.class);
            registerButton(BUTTON_NEXTPAGE, () -> setPage(true, 1));
            registerButton(BUTTON_PREVPAGE, () -> setPage(true, -1));
        }
        else
        {
            buttonNextPage = null;
            buttonPrevPage = null;
            pageNum = null;
        }

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
        registerButton(id, button -> action.run());
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
     * Handle a button clicked event. Find the registered event and execute that.
     * <p>
     * todo: make final once migration is complete
     *
     * @param button the button that was clicked.
     */
    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
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
     * @param relative whether page param is relative or absolute
     * @param page if relative turn x pages forward/backward, if absolute turn to x-th page
     */
    public void setPage(final boolean relative, final int page)
    {
        if (switchView == null)
        {
            return;
        }

        final int switchPagesSize = switchView.getChildrenSize();
    
        if (switchPagesSize <= 1)
        {
            buttonPrevPage.off();
            buttonNextPage.off();
            pageNum.off();
            return;
        }

        final int curPage = switchView.setView(relative, page) + 1;

        buttonNextPage.on();
        buttonPrevPage.on();
        if (curPage == 1/* && !switchView.isEndlessScrollingEnabled()*/)
        {
            buttonPrevPage.off();
        }
        if (curPage == switchPagesSize/* && !switchView.isEndlessScrollingEnabled()*/)
        {
            buttonNextPage.off();
        }
        if (pageNum != null)
        {
            pageNum.setText(Component.literal(curPage + "/" + switchPagesSize));
        }
    }

    @Override
    public void close()
    {
        super.close();
        if (parent != null)
        {
            parent.open();
        }
    }
}
