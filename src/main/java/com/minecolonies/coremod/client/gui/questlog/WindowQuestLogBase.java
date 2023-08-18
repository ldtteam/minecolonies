package com.minecolonies.coremod.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_NEXTPAGE;
import static com.minecolonies.api.util.constant.WindowConstants.BUTTON_PREVPAGE;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.QUEST_LOG_TRACK_CITIZEN_TEXT;
import static com.minecolonies.coremod.client.gui.questlog.Constants.BUTTON_QUEST_LOCATOR;

/**
 * Quest log window base tab.
 */
public abstract class WindowQuestLogBase<T> extends AbstractWindowSkeleton
{
    /**
     * The colony this quest log is attached to.
     */
    @NotNull
    private final IColonyView colonyView;

    /**
     * ScrollList with all the quests.
     */
    private ScrollingList questsList;

    /**
     * The list of quest items
     */
    private List<T> questItems;

    /**
     * Default constructor.
     */
    protected WindowQuestLogBase(final String resource, final @NotNull IColonyView colonyView)
    {
        super(resource);
        this.colonyView = colonyView;
        registerButton(BUTTON_PREVPAGE, this::openTabPage);
        registerButton(BUTTON_NEXTPAGE, this::openTabPage);
        registerButton(BUTTON_QUEST_LOCATOR, this::locateCitizenClickedInternal);
    }

    /**
     * Open the previous or next tab.
     *
     * @param button the button which was clicked.
     */
    private void openTabPage(Button button)
    {
        Function<IColonyView, AbstractWindowSkeleton> nextWindow = button.getID().equals("prevPage") ? getNavigationPrev() : getNavigationNext();
        final AbstractWindowSkeleton window = nextWindow.apply(colonyView);
        window.open();
    }

    /**
     * Internal method for handling the locate button.
     *
     * @param button clicked button.
     */
    private void locateCitizenClickedInternal(@NotNull final Button button)
    {
        final int row = questsList.getListElementIndexByPane(button);
        final T quest = questItems.get(row);
        locateCitizenClicked(row, quest);
    }

    /**
     * Get the previous window to navigate to.
     *
     * @return an expression creating the previous navigation window.
     */
    protected abstract Function<IColonyView, AbstractWindowSkeleton> getNavigationPrev();

    /**
     * Get the next window to navigate to.
     *
     * @return an expression creating the next navigation window.
     */
    protected abstract Function<IColonyView, AbstractWindowSkeleton> getNavigationNext();

    /**
     * Fired when the locate button has been clicked in the quests list.
     *
     * @param row   the row in which was clicked on.
     * @param quest the quest item.
     */
    protected void locateCitizenClicked(final int row, final T quest)
    {
        // No-op
    }

    /**
     * Get the colony this quest log is attached to.
     *
     * @return the colony view instance.
     */
    protected @NotNull IColonyView getColonyView()
    {
        return colonyView;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        questItems = getQuestItems();

        questsList = findPaneOfTypeByID(Constants.LIST_QUESTS, ScrollingList.class);
        questsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return questItems.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                renderQuestItem(index, questItems.get(index), rowPane);

                final ButtonImage questLocator = rowPane.findPaneOfTypeByID(BUTTON_QUEST_LOCATOR, ButtonImage.class);
                if (questLocator != null)
                {
                    PaneBuilders.tooltipBuilder()
                      .append(Component.translatable(QUEST_LOG_TRACK_CITIZEN_TEXT))
                      .hoverPane(questLocator)
                      .build();
                }
            }
        });
    }

    /**
     * Query all quest items.
     *
     * @return the list of quests for this page to render.
     */
    protected abstract List<T> getQuestItems();

    /**
     * Render the quest log item.
     *
     * @param index the item index.
     * @param quest the current quest item.
     * @param row   the row pane.
     */
    protected abstract void renderQuestItem(final int index, final T quest, final Pane row);

    /**
     * Get the name of the citizen who gave the quest.
     *
     * @param quest the quest instance.
     * @return the component containing the name of the citizen.
     */
    protected Component getQuestGiverName(final IQuestInstance quest)
    {
        final ICitizenDataView citizen = colonyView.getCitizen(quest.getQuestGiverId());
        if (citizen != null)
        {
            return Component.literal(citizen.getName());
        }
        return Component.empty();
    }

    @Override
    public void onUpdate()
    {
        questItems = getQuestItems();
        super.onUpdate();
    }

    /**
     * Quick setter for assigning text to a field, as well as providing a tooltip if the text is too long.
     *
     * @param container the container element for the text element.
     * @param id        the id of the text element.
     * @param component the text component to write as text on the element.
     */
    protected void setText(Pane container, String id, Component component)
    {
        final Text label = container.findPaneOfTypeByID(id, Text.class);
        label.setText(component);

        if (label.getRenderedTextWidth() > label.getWidth())
        {
            PaneBuilders.tooltipBuilder()
              .append(component)
              .hoverPane(label)
              .build();
        }
    }
}
