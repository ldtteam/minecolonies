package com.minecolonies.coremod.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.quests.FinishedQuest;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowSkeleton;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.*;
import static com.minecolonies.coremod.client.gui.questlog.Constants.LABEL_COMPLETED_COUNT;

/**
 * Quest log window finished tab.
 */
public class WindowQuestLogFinished extends WindowQuestLogBase<FinishedQuest>
{
    /**
     * Link to the xml file of the window.
     */
    private static final String WINDOW_RESOURCE = ":gui/windowquestlogfinished.xml";

    /**
     * Default constructor.
     *
     * @param colonyView the colony which this quest log is attached to.
     */
    public WindowQuestLogFinished(final @NotNull IColonyView colonyView)
    {
        super(Constants.MOD_ID + WINDOW_RESOURCE, colonyView);
    }

    @Override
    protected Function<IColonyView, AbstractWindowSkeleton> getNavigationPrev()
    {
        return WindowQuestLogAvailable::new;
    }

    @Override
    protected Function<IColonyView, AbstractWindowSkeleton> getNavigationNext()
    {
        return WindowQuestLogInProgress::new;
    }

    @Override
    protected List<FinishedQuest> getQuestItems()
    {
        return getColonyView().getQuestManager().getFinishedQuests();
    }

    @Override
    protected void renderQuestItem(final int index, final FinishedQuest quest, final Pane row)
    {
        setText(row, com.minecolonies.coremod.client.gui.questlog.Constants.LABEL_QUEST_NAME, Component.translatable(QUEST_LOG_NAME_PREFIX).append(quest.template().getName()));

        if (quest.template().getMaxOccurrence() > 1)
        {
            setText(row,
              LABEL_COMPLETED_COUNT,
              Component.translatable(QUEST_LOG_COMPLETED_MULTIPLE_TEXT, quest.finishedCount(), quest.template().getMaxOccurrence()).withStyle(ChatFormatting.GOLD));
        }
        else
        {
            setText(row, LABEL_COMPLETED_COUNT, Component.translatable(QUEST_LOG_COMPLETED_ONCE_TEXT).withStyle(ChatFormatting.GOLD));
        }
    }
}
