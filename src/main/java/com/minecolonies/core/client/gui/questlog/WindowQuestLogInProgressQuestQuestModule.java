package com.minecolonies.core.client.gui.questlog;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.PaneBuilders;
import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.quests.*;
import com.minecolonies.core.client.render.worldevent.HighlightManager;
import com.minecolonies.core.client.render.worldevent.highlightmanager.CitizenRenderData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.QUEST_LOG_GIVER_PREFIX;
import static com.minecolonies.api.util.constant.translation.GuiTranslationConstants.QUEST_LOG_NAME_PREFIX;
import static com.minecolonies.core.client.gui.questlog.Constants.*;

/**
 * Window quest log renderer for in progress quests.
 */
public class WindowQuestLogInProgressQuestQuestModule implements WindowQuestLogQuestModule<IQuestInstance>
{
    @Override
    public List<IQuestInstance> getQuestItems(final IColonyView colonyView)
    {
        return colonyView.getQuestManager().getInProgressQuests();
    }

    @Override
    public void renderQuestItem(final IQuestInstance quest, final IColonyView colonyView, final Pane row)
    {
        IQuestTemplate questTemplate = IQuestManager.GLOBAL_SERVER_QUESTS.get(quest.getId());

        setText(row, LABEL_QUEST_NAME, Component.translatable(QUEST_LOG_NAME_PREFIX).append(questTemplate.getName()));
        setText(row, LABEL_QUEST_GIVER, Component.translatable(QUEST_LOG_GIVER_PREFIX).append(getQuestGiverName(colonyView, quest)));

        final IQuestObjectiveTemplate objectiveTemplate = questTemplate.getObjective(quest.getObjectiveIndex());
        final Text questObjectiveText = row.findPaneOfTypeByID(LABEL_QUEST_OBJECTIVE, Text.class);

        final Component progressText = objectiveTemplate.getProgressText(quest, Style.EMPTY.withColor(ChatFormatting.GOLD));
        final Component mainComponent = Component.literal(" - ")
                                          .append(progressText)
                                          .withStyle(ChatFormatting.GOLD);

        questObjectiveText.setText(mainComponent);

        PaneBuilders.tooltipBuilder()
          .append(objectiveTemplate.getProgressText(quest, Style.EMPTY.withColor(ChatFormatting.WHITE)))
          .hoverPane(questObjectiveText)
          .build();
    }

    @Override
    public void trackQuest(final IQuestInstance quest)
    {
        HighlightManager.addHighlight(HIGHLIGHT_QUEST_LOG_TRACKER_KEY, new CitizenRenderData(quest.getQuestTarget(), HIGHLIGHT_QUEST_LOG_TRACKER_DURATION));
    }

    /**
     * Quick setter for assigning text to a field, as well as providing a tooltip if the text is too long.
     *
     * @param container the container element for the text element.
     * @param id        the id of the text element.
     * @param component the text component to write as text on the element.
     */
    private void setText(final Pane container, final String id, final Component component)
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

    /**
     * Get the name of the citizen who gave the quest.
     *
     * @param colonyView the colony view instance.
     * @param quest      the quest instance.
     * @return the component containing the name of the citizen.
     */
    private Component getQuestGiverName(final IColonyView colonyView, final IQuestInstance quest)
    {
        final ICitizenDataView citizen = colonyView.getCitizen(quest.getQuestGiverId());
        if (citizen != null)
        {
            return Component.literal(citizen.getName());
        }
        return Component.empty();
    }
}
