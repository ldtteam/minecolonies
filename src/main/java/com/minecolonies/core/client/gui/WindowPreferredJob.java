package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Button;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.modules.IAssignmentModuleView;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.MessageUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.util.BuildingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Preferred job selector window, when multiple are available.
 */
public class WindowPreferredJob extends AbstractWindowSkeleton
{
    /**
     * The view of the current building.
     */
    protected final IBuildingView building;

    /**
     * Holder of a list element
     */
    protected final ScrollingList jobList;

    /**
     * The different job module views.
     */
    protected final List<IAssignmentModuleView> moduleViews = new ArrayList<>();

    /**
     * Constructor for the window when the player can select the preferred job for a new building.
     *
     * @param colony the colony.
     * @param buildingId the building position.
     */
    public WindowPreferredJob(final IColonyView colony, final BlockPos buildingId)
    {
        super(Constants.MOD_ID + ":gui/windowpreferredjob.xml");

        building = colony.getBuilding(buildingId);

        jobList = findPaneOfTypeByID(JOB_LIST, ScrollingList.class);

        registerButton(BUTTON_JOB, this::onJobClicked);

        if (building != null)
        {
            final Predicate<JobEntry> allowedJobs = BuildingUtils.getAllowedJobs(colony.getWorld(), buildingId);
            final Predicate<IAssignmentModuleView> allowedModules = m -> allowedJobs.test(m.getJobEntry());

            moduleViews.addAll(building.getModuleViews(IAssignmentModuleView.class).stream()
                    .filter(allowedModules).toList());
        }
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        if (moduleViews.isEmpty())
        {
            MessageUtils.format(COM_MINECOLONIES_COREMOD_GUI_WORKERHUTS_LEVEL_0).sendTo(Minecraft.getInstance().player);
            close();
            return;
        }

        jobList.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return  moduleViews.size() + (moduleViews.size() < 2 ? 1 : 2);
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                if (index == 0)
                {
                    rowPane.findPaneOfTypeByID(BUTTON_JOB, Button.class).setText(Component.translatable("com.minecolonies.coremod.gui.jobpref.none"));
                    rowPane.findPaneOfTypeByID(JOB_TITLE_LABEL, Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.jobpref.none.desc"));
                }
                else if (index > moduleViews.size())
                {
                    rowPane.findPaneOfTypeByID(BUTTON_JOB, Button.class).setText(Component.translatable("com.minecolonies.coremod.gui.jobpref.any"));
                    rowPane.findPaneOfTypeByID(JOB_TITLE_LABEL, Text.class).setText(Component.translatable("com.minecolonies.coremod.gui.jobpref.any.desc"));
                }
                else
                {
                    final IAssignmentModuleView moduleView = moduleViews.get(index - 1);
                    rowPane.findPaneOfTypeByID(BUTTON_JOB, Button.class).setText(Component.translatable(moduleView.getJobEntry().getTranslationKey()));
                    rowPane.findPaneOfTypeByID(JOB_TITLE_LABEL, Text.class).setText(Component.translatable(moduleView.getJobEntry().getKey().toString() + ".job.desc"));
                }
            }
        });
    }

    private void onJobClicked(@NotNull final Button button)
    {
        final int index = jobList.getListElementIndexByPane(button);

        final IAssignmentModuleView selectedModule = index > 0 && index <= moduleViews.size() ? moduleViews.get(index - 1) : null;
        for (final IAssignmentModuleView moduleView : moduleViews)
        {
            moduleView.setHiringMode(moduleView == selectedModule ? HiringMode.DEFAULT : HiringMode.MANUAL);
        }

        close();
    }
}
