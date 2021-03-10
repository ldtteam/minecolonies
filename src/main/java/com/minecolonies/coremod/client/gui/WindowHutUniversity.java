package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.blockout.views.View;
import com.minecolonies.api.research.IGlobalResearchBranch;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.research.AlternateBuildingResearchRequirement;
import com.minecolonies.coremod.research.BuildingResearchRequirement;
import com.minecolonies.coremod.research.GlobalResearchBranch;
import com.minecolonies.coremod.research.ResearchResearchRequirement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_UNIVERSITY;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the university.
 */
public class WindowHutUniversity extends AbstractWindowWorkerBuilding<BuildingUniversity.View>
{
    /**
     * The list of branches of the tree.
     */
    private final List<ResourceLocation> branches = new ArrayList<>();

    /**
     * Constructor for the window of the lumberjack.
     *
     * @param building {@link BuildingUniversity.View}.
     */
    public WindowHutUniversity(final BuildingUniversity.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final List<ResourceLocation> inputBranches = IGlobalResearchTree.getInstance().getBranches();
        final List<List<IFormattableTextComponent>> allReqs = new ArrayList<>();
        inputBranches.sort((b1, b2) -> Integer.compare(IGlobalResearchTree.getInstance().getBranchData(b1).getSortOrder(), IGlobalResearchTree.getInstance().getBranchData(b2).getSortOrder()));
        for (final ResourceLocation branch : inputBranches)
        {
            final List<IFormattableTextComponent> requirements = getHidingRequirementDesc(branch);
            if(requirements.isEmpty() || !IGlobalResearchTree.getInstance().getBranchData(branch).getHidden())
            {
                branches.add(branch);
                allReqs.add(requirements);
            }
        }
        ScrollingList researchList = findPaneOfTypeByID("researches", ScrollingList.class);
        researchList.setDataProvider(new ResearchListProvider(branches, allReqs));
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
                    requirements.add(new TranslationTextComponent("Or").setStyle((Style.EMPTY).setFormatting(TextFormatting.BLUE)));
                }
                for(IResearchRequirement req : IGlobalResearchTree.getInstance().getResearch(branch, primary).getResearchRequirement())
                {
                    // We'll include even completed partial components in the requirement list.
                    if (!req.isFulfilled(building.getColony()))
                    {
                        requirements.add(new StringTextComponent("-").append(req.getDesc().setStyle((Style.EMPTY).setFormatting(TextFormatting.RED))));
                    }
                    else
                    {
                        requirements.add(new StringTextComponent("-").append(req.getDesc().setStyle((Style.EMPTY).setFormatting(TextFormatting.AQUA))));
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

        if (button.getParent() != null && ResourceLocation.isResouceNameValid(button.getParent().getID()) && branches.contains(new ResourceLocation(button.getParent().getID())))
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
                AbstractTextBuilder.TooltipBuilder hoverText = PaneBuilders.tooltipBuilder().hoverPane(button);
                button.disable();
                for (IFormattableTextComponent req : requirements.get(index))
                {
                    hoverText.append(req).paragraphBreak();
                }
                hoverText.build();
            }
        }
    }
}
