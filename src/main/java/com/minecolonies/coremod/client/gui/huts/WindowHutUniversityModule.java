package com.minecolonies.coremod.client.gui.huts;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.AbstractWindowWorkerModuleBuilding;
import com.minecolonies.coremod.client.gui.WindowResearchTree;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.COLOR_TEXT_UNFULFILLED;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_UNIVERSITY;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the university.
 */
public class WindowHutUniversityModule extends AbstractWindowWorkerModuleBuilding<BuildingUniversity.View>
{
    /**
     * Constructor for the window of the lumberjack.
     *
     * @param building {@link BuildingUniversity.View}.
     */
    public WindowHutUniversityModule(final BuildingUniversity.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final List<ResourceLocation> inputBranches = IGlobalResearchTree.getInstance().getBranches();
        inputBranches.sort(Comparator.comparingInt(branchId -> IGlobalResearchTree.getInstance().getBranchData(branchId).getSortOrder()));
        final List<ResourceLocation> visibleBranches = new ArrayList<>();
        final List<List<IFormattableTextComponent>> allReqs = new ArrayList<>();
        for (final ResourceLocation branch : inputBranches)
        {
            final List<IFormattableTextComponent> requirements = getHidingRequirementDesc(branch);
            if(requirements.isEmpty() || !IGlobalResearchTree.getInstance().getBranchData(branch).getHidden())
            {
                visibleBranches.add(branch);
                allReqs.add(requirements);
            }
        }
        ScrollingList researchList = findPaneOfTypeByID("researches", ScrollingList.class);
        researchList.setDataProvider(new ResearchListProvider(visibleBranches, allReqs));
        updateResearchCount(0);
    }

    /**
     * Gets a list describing what requirements must be met to make at least one primary research for a branch visible.
     *
     * @param branch  The identifier for a branch.
     * @return An empty list if at least one primary research is visible, or a list of IFormattableTextComponents describing the dependencies for each hidden primary research.
     */
    public List<IFormattableTextComponent> getHidingRequirementDesc(final ResourceLocation branch)
    {
        final List<IFormattableTextComponent> requirements = new ArrayList<>();
        for(final ResourceLocation primary : IGlobalResearchTree.getInstance().getPrimaryResearch(branch))
        {
            if(!IGlobalResearchTree.getInstance().getResearch(branch, primary).isHidden()
                 || IGlobalResearchTree.getInstance().isResearchRequirementsFulfilled(IGlobalResearchTree.getInstance().getResearch(branch, primary).getResearchRequirement(), building.getColony()))
            {
                return Collections.EMPTY_LIST;
            }
            else
            {
                if(requirements.isEmpty())
                {
                    requirements.add(new TranslationTextComponent("com.minecolonies.coremod.research.locked"));
                }
                else
                {
                    requirements.add(new TranslationTextComponent("Or").setStyle((Style.EMPTY).withColor(TextFormatting.BLUE)));
                }
                for(IResearchRequirement req : IGlobalResearchTree.getInstance().getResearch(branch, primary).getResearchRequirement())
                {
                    // We'll include even completed partial components in the requirement list.
                    if (!req.isFulfilled(building.getColony()))
                    {
                        requirements.add(new StringTextComponent("-").append(req.getDesc().setStyle((Style.EMPTY).withColor(TextFormatting.RED))));
                    }
                    else
                    {
                        requirements.add(new StringTextComponent("-").append(req.getDesc().setStyle((Style.EMPTY).withColor(TextFormatting.AQUA))));
                    }
                }
            }
        }
        return requirements;
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);

        if (button.getParent() != null && ResourceLocation.isValidResourceLocation(button.getParent().getID()) && IGlobalResearchTree.getInstance().getBranches().contains(new ResourceLocation(button.getParent().getID())))
        {
            new WindowResearchTree(new ResourceLocation(button.getParent().getID()), building, this).open();
        }
    }

    /**
     * Display the count of InProgress research, and the max number for this university, and change the color text to red if at max.
     * @param offset        An amount to offset the count of inProgress research, normally zero, or -1 when cancelling a research
     */
    public void updateResearchCount(final int offset)
    {
        this.findPaneOfTypeByID("maxresearchwarn", Text.class)
          .setText(new TranslationTextComponent("com.minecolonies.coremod.gui.research.countinprogress",
            building.getColony().getResearchManager().getResearchTree().getResearchInProgress().size() + offset, building.getBuildingLevel()));
        if(building.getBuildingLevel() <= building.getColony().getResearchManager().getResearchTree().getResearchInProgress().size() + offset)
        {
            this.findPaneOfTypeByID("maxresearchwarn", Text.class).setColors(Color.getByName("red", 0));
        }
        else
        {
            this.findPaneOfTypeByID("maxresearchwarn", Text.class).setColors(Color.getByName("black", 0));
        }
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return COM_MINECOLONIES_COREMOD_GUI_UNIVERSITY;
    }

    private static class ResearchListProvider implements ScrollingList.DataProvider
    {
        private final List<ResourceLocation> branches;
        private final List<List<IFormattableTextComponent>> requirements;

        ResearchListProvider(List<ResourceLocation> branches, List<List<IFormattableTextComponent>> requirements)
        {
            this.branches = branches;
            this.requirements = requirements;
        }

        @Override
        public int getElementCount()
        {
            return branches.size();
        }

        @Override
        public void updateElement(final int index, final Pane rowPane)
        {
            ButtonImage button = rowPane.findPaneOfTypeByID(GUI_LIST_ELEMENT_NAME, ButtonImage.class);
            button.getParent().setID(branches.get(index).toString());
            if(requirements.get(index).isEmpty())
            {
                button.setText(IGlobalResearchTree.getInstance().getBranchData(branches.get(index)).getName());
            }
            else
            {
                button.setText(new TranslationTextComponent("----------"));
                button.disable();
            }

            // This null check isn't strictly required, but prevents unnecessary creation of tooltip panes, since the contents here never updates.
            if(button.getHoverPane() == null && (!requirements.get(index).isEmpty() || !IGlobalResearchTree.getInstance().getBranchData(branches.get(index)).getSubtitle().getKey().isEmpty()))
            {
                AbstractTextBuilder.TooltipBuilder hoverText = PaneBuilders.tooltipBuilder().hoverPane(button);
                if (!IGlobalResearchTree.getInstance().getBranchData(branches.get(index)).getSubtitle().getKey().isEmpty())
                {
                    hoverText.append(IGlobalResearchTree.getInstance().getBranchData(branches.get(index)).getSubtitle()).colorName("GRAY").paragraphBreak();
                }
                if (!requirements.get(index).isEmpty())
                {
                    for (IFormattableTextComponent req : requirements.get(index))
                    {
                        hoverText.append(req).color(COLOR_TEXT_UNFULFILLED).paragraphBreak();
                    }
                }
                hoverText.build();
            }
        }
    }
}
