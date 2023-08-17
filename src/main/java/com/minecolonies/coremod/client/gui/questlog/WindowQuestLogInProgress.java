package com.minecolonies.coremod.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.quests.IObjectiveInstance;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.quests.IQuestTemplate;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Quest log window.
 */
public class WindowQuestLogInProgress extends AbstractWindowSkeleton
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowquestloginprogress.xml";

    /**
     * ID of the quests list inside the GUI.
     */
    private static final String LIST_QUESTS = "quests";

    /**
     * The colony this quest log was attached to.
     */
    @NotNull
    private final IColonyView colonyView;

    /**
     * The list of in progress quests in the colony.
     */
    private List<IQuestInstance> inProgressQuests;

    /**
     * ScrollList with all the quests.
     */
    private ScrollingList questsList;

    /**
     * Default constructor.
     *
     * @param colonyView the colony which this quest log is attached to.
     */
    public WindowQuestLogInProgress(final @NotNull IColonyView colonyView)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE);
        this.colonyView = colonyView;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        inProgressQuests = this.colonyView.getQuestManager().getInProgressQuests();

        questsList = findPaneOfTypeByID(LIST_QUESTS, ScrollingList.class);
        questsList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return inProgressQuests.size();
            }

            @Override
            public void updateElement(final int index, final Pane rowPane)
            {
                IQuestInstance quest = inProgressQuests.get(index);
                IQuestTemplate questTemplate = IQuestManager.GLOBAL_SERVER_QUESTS.get(quest.getId());

                rowPane.findPaneOfTypeByID("questName", Text.class).setText(questTemplate.getName());

                final IObjectiveInstance currentObjectiveInstance = quest.getCurrentObjectiveInstance();
                if (currentObjectiveInstance != null)
                {
                    final Text questObjectiveText = rowPane.findPaneOfTypeByID("questGiver", Text.class);

                    final MutableComponent progressText = currentObjectiveInstance.getProgressText(quest)
                                                            .withStyle(ChatFormatting.GRAY);
                    final MutableComponent mainComponent = Component.literal(" - ")
                                                             .append(progressText)
                                                             .withStyle(ChatFormatting.GRAY);

                    questObjectiveText.setText(mainComponent);
                    PaneBuilders.tooltipBuilder()
                      .append(progressText)
                      .hoverPane(questObjectiveText)
                      .build();
                }
            }
        });
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        inProgressQuests = this.colonyView.getQuestManager().getInProgressQuests();
    }
}
