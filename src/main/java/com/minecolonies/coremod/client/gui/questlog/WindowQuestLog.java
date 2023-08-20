package com.minecolonies.coremod.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.ButtonImage;
import com.ldtteam.blockui.views.ScrollingList;
import com.ldtteam.blockui.views.SwitchView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.QUEST_LOG_TRACK_CITIZEN_TEXT;
import static com.minecolonies.coremod.client.gui.questlog.Constants.BUTTON_QUEST_LOCATOR;
import static com.minecolonies.coremod.client.gui.questlog.Constants.LIST_QUESTS;

/**
 * Quest log window in progress tab.
 */
public class WindowQuestLog extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowquestlog.xml";

    /**
     * Map of the different renderer classes, each dedicated to its own view.
     */
    private final Map<String, QuestRendererContainer<?>> rendererMap = new HashMap<>();

    /**
     * Default constructor.
     *
     * @param colonyView the colony which this quest log is attached to.
     */
    public WindowQuestLog(final @NotNull IColonyView colonyView)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        registerButton(BUTTON_QUEST_LOCATOR, this::locateCitizenClickedInternal);

        this.rendererMap.put("pageInProgress", new QuestRendererContainer<>(new WindowQuestLogInProgressQuestQuestRenderer(), colonyView, switchView, "pageInProgress"));
        this.rendererMap.put("pageAvailable", new QuestRendererContainer<>(new WindowQuestLogAvailableQuestRenderer(), colonyView, switchView, "pageAvailable"));
        this.rendererMap.put("pageFinished", new QuestRendererContainer<>(new WindowQuestLogFinishedQuestRenderer(), colonyView, switchView, "pageFinished"));
    }

    /**
     * Internal method for handling the locate button.
     *
     * @param button clicked button.
     */
    private void locateCitizenClickedInternal(@NotNull final Button button)
    {
        if (switchView.getCurrentView() != null)
        {
            String pageId = switchView.getCurrentView().getID();
            final QuestRendererContainer<?> renderer = rendererMap.get(pageId);
            if (renderer != null)
            {
                renderer.trackQuest(button);
            }
        }
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        if (switchView.getCurrentView() != null)
        {
            String pageId = switchView.getCurrentView().getID();
            final QuestRendererContainer<?> renderer = rendererMap.get(pageId);
            if (renderer != null)
            {
                renderer.onUpdate();
            }
        }
    }

    /**
     * Internal class for keeping track of the different pages.
     */
    private static class QuestRendererContainer<T>
    {
        /**
         * The renderer class used to render the quest items.
         */
        private final WindowQuestLogQuestRenderer<T> renderer;

        /**
         * The current colony.
         */
        private final IColonyView colonyView;

        /**
         * The scrolling list containing all the quests.
         */
        private final ScrollingList questsList;

        /**
         * The current list of quest items for this page.
         */
        private List<T> questItems;

        /**
         * Default constructor.
         */
        public QuestRendererContainer(final WindowQuestLogQuestRenderer<T> renderer, final IColonyView colonyView, final SwitchView switchView, final String pageId)
        {
            this.renderer = renderer;
            this.colonyView = colonyView;
            Pane parent = switchView.getChildren().stream().filter(f -> f.getID().equals(pageId)).findFirst().orElseThrow();
            this.questsList = parent.findPaneOfTypeByID(LIST_QUESTS, ScrollingList.class);

            this.questItems = renderer.getQuestItems(colonyView);

            this.questsList.setDataProvider(new ScrollingList.DataProvider()
            {
                @Override
                public int getElementCount()
                {
                    return questItems.size();
                }

                @Override
                public void updateElement(final int index, final Pane rowPane)
                {
                    renderer.renderQuestItem(questItems.get(index), colonyView, rowPane);

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
         * Updates the underlying quest list.
         */
        void onUpdate()
        {
            this.questItems = renderer.getQuestItems(colonyView);
        }

        /**
         * Gives a signal that the given quest should be tracked.
         *
         * @param button the button which was clicked, helping to find which quest item should be tracked.
         */
        void trackQuest(final Button button)
        {
            final int row = questsList.getListElementIndexByPane(button);
            renderer.trackQuest(questItems.get(row));
        }
    }
}
