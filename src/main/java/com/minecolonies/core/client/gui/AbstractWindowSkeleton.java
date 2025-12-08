package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Loader;
import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonHandler;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.BOWindow;
import com.ldtteam.blockui.views.SwitchView;
import com.minecolonies.api.colony.modules.IModuleContainer;
import com.minecolonies.core.Network;
import com.minecolonies.core.client.gui.modules.IWindowModule;
import com.minecolonies.core.client.gui.modules.IWindowWithLayoutModule;
import com.minecolonies.core.network.messages.server.ClickGuiButtonTriggerMessage;
import com.minecolonies.core.network.messages.server.OpenGuiWindowTriggerMessage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Manage windows and their events.
 */
public abstract class AbstractWindowSkeleton extends BOWindow implements ButtonHandler, IModuleContainer<IWindowModule>
{
    @NotNull
    private final HashMap<String, Consumer<Button>> buttons;

    private final List<IWindowModule> modules = new ArrayList<>();

    /**
     * Panes used by the generic page handler
     */
    protected final Text       pageNum;
    protected final Button     buttonPrevPage;
    protected final Button     buttonNextPage;
    protected       SwitchView switchView;

    /**
     * This window's parent
     */
    @Nullable
    private final BOWindow parent;

    /**
     * Constructor with no parent window
     *
     * @param resource window resource location.
     */
    public AbstractWindowSkeleton(final ResourceLocation resource)
    {
        this(null, resource);
    }

    /**
     * Constructor for the skeleton class of the windows.
     *
     * @param parent   the parent window.
     * @param resource window resource location.
     */
    public AbstractWindowSkeleton(@Nullable final BOWindow parent, final ResourceLocation resource)
    {
        super(resource);
        this.parent = parent;

        buttons = new HashMap<>();

        switchView = findPaneOfTypeByID(VIEW_PAGES, SwitchView.class);
        if (switchView != null)
        {
            buttonNextPage = findPaneOfTypeByID(BUTTON_NEXTPAGE, Button.class);
            buttonPrevPage = findPaneOfTypeByID(BUTTON_PREVPAGE, Button.class);
            PaneBuilders.singleLineTooltip(Component.translatable("com.minecolonies.core.gui.nextpage"), buttonNextPage);
            PaneBuilders.singleLineTooltip(Component.translatable("com.minecolonies.core.gui.prevpage"), buttonPrevPage);

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

        Network.getNetwork().sendToServer(new OpenGuiWindowTriggerMessage(this.xmlResourceLocation));
    }

    @Override
    @NotNull
    public List<IWindowModule> getModules()
    {
        return modules;
    }

    @Override
    @NotNull
    public Class<IWindowModule> getClassType()
    {
        return IWindowModule.class;
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
     * Add a module to this window. Extending the original logic of the window.
     *
     * @param moduleBuilder the new module.
     */
    public final <T extends IWindowModule, A> T registerModule(final BiFunction<AbstractWindowSkeleton, A, T> moduleBuilder, final A argument)
    {
        final T module = moduleBuilder.apply(this, argument);
        this.modules.add(module);
        return module;
    }

    /**
     * Add a module to this window. Extending the original logic of the window.
     *
     * @param moduleBuilder the new module.
     */
    public final <T extends IWindowWithLayoutModule, A> T registerLayoutModule(final BiFunction<AbstractWindowSkeleton, A, T> moduleBuilder, A argument, int xPos, int yPos)
    {
        final T module = moduleBuilder.apply(this, argument);
        final Pane rootPane = Loader.createFromXMLFile2(module.getLayout(), this);
        rootPane.setPosition(xPos, yPos);
        module.onLayoutMounted(rootPane);
        this.modules.add(module);
        return module;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        this.modules.forEach(IWindowModule::onOpened);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        this.modules.forEach(IWindowModule::onUpdate);
    }

    @Override
    public void onClosed()
    {
        super.onClosed();
        this.modules.forEach(IWindowModule::onClosed);
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
            Network.getNetwork().sendToServer(new ClickGuiButtonTriggerMessage(button.getID(), this.xmlResourceLocation));
        }

        modules.forEach(module -> module.onButtonClicked(button));
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
        if (curPage == 1 && !switchView.isEndlessScrollingEnabled())
        {
            buttonPrevPage.off();
        }
        if (curPage == switchPagesSize && !switchView.isEndlessScrollingEnabled())
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
