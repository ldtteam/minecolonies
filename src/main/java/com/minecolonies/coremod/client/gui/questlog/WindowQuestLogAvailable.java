package com.minecolonies.coremod.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.quests.IQuestInstance;
import com.minecolonies.api.quests.IQuestManager;
import com.minecolonies.api.quests.IQuestTemplate;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import com.minecolonies.coremod.client.render.worldevent.HighlightManager;
import com.minecolonies.coremod.client.render.worldevent.highlightmanager.CitizenRenderData;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.QUEST_LOG_GIVER_PREFIX;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.QUEST_LOG_NAME_PREFIX;
import static com.minecolonies.coremod.client.gui.questlog.Constants.HIGHLIGHT_QUEST_LOG_TRACKER_DURATION;
import static com.minecolonies.coremod.client.gui.questlog.Constants.HIGHLIGHT_QUEST_LOG_TRACKER_KEY;

/**
 * Quest log window available tab.
 */
public class WindowQuestLogAvailable extends WindowQuestLogBase<IQuestInstance>
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowquestlogavailable.xml";

    /**
     * Default constructor.
     *
     * @param colonyView the colony which this quest log is attached to.
     */
    public WindowQuestLogAvailable(final @NotNull IColonyView colonyView)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE, colonyView);
    }

    @Override
    protected Function<IColonyView, AbstractWindowSkeleton> getNavigationPrev()
    {
        return WindowQuestLogInProgress::new;
    }

    @Override
    protected Function<IColonyView, AbstractWindowSkeleton> getNavigationNext()
    {
        return WindowQuestLogFinished::new;
    }

    @Override
    protected void locateCitizenClicked(final int row, final IQuestInstance quest)
    {
        HighlightManager.addHighlight(HIGHLIGHT_QUEST_LOG_TRACKER_KEY, new CitizenRenderData(quest.getQuestGiverId(), HIGHLIGHT_QUEST_LOG_TRACKER_DURATION));
    }

    @Override
    protected List<IQuestInstance> getQuestItems()
    {
        return getColonyView().getQuestManager().getAvailableQuests();
    }

    @Override
    protected void renderQuestItem(final int index, final IQuestInstance quest, final Pane row)
    {
        IQuestTemplate questTemplate = IQuestManager.GLOBAL_SERVER_QUESTS.get(quest.getId());

        setText(row, com.minecolonies.coremod.client.gui.questlog.Constants.LABEL_QUEST_NAME, Component.translatable(QUEST_LOG_NAME_PREFIX).append(questTemplate.getName()));
        setText(row, com.minecolonies.coremod.client.gui.questlog.Constants.LABEL_QUEST_GIVER, Component.translatable(QUEST_LOG_GIVER_PREFIX).append(getQuestGiverName(quest)));
    }
}
