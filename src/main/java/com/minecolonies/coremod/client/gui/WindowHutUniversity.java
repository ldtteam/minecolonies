package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.Color;
import com.ldtteam.blockout.Alignment;
import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.View;
import com.minecolonies.api.research.IGlobalResearchTree;
import com.minecolonies.api.research.IResearchRequirement;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import com.minecolonies.coremod.research.AlternateBuildingResearchRequirement;
import com.minecolonies.coremod.research.BuildingResearchRequirement;
import com.minecolonies.coremod.research.ResearchResearchRequirement;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

        final View view = this.findPaneOfTypeByID(BRANCH_VIEW_ID, View.class);
        int offset = 0;
        // For now, sort research branches by name, as they may be loaded in any order.
        branches.addAll(IGlobalResearchTree.getInstance().getBranches());
        branches.sort(Comparator.comparing(ResourceLocation::getPath, String.CASE_INSENSITIVE_ORDER));
        for (final ResourceLocation branch : branches)
        {
            List<IFormattableTextComponent> requirements = getHidingRequirementDesc(branch);
            if(requirements.isEmpty())
            {

                final ButtonImage button = new ButtonImage();
                button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                button.setID(branch.toString());
                button.setText(IGlobalResearchTree.getInstance().getBranchName(branch));
                button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                button.setTextRenderBox(BUTTON_LENGTH, BUTTON_HEIGHT);
                button.setTextAlignment(Alignment.MIDDLE);
                button.setColors(SLIGHTLY_BLUE);
                button.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
                view.addChild(button);
            }
            else
            {
                final Gradient gradient = new Gradient();
                gradient.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                gradient.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
                gradient.setGradientStart(239, 230, 215, 255);
                gradient.setGradientEnd(239, 230, 215, 255);
                view.addChild(gradient);
                final ButtonImage button = new ButtonImage();
                button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                button.setText(new TranslationTextComponent("-----------"));
                button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                button.setTextColor(SLIGHTLY_BLUE);
                button.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
                view.addChild(button);
                final AbstractTextBuilder.TooltipBuilder reqBuilder = PaneBuilders.tooltipBuilder().hoverPane(button);
                for(IFormattableTextComponent req : requirements)
                {
                    reqBuilder.append(req).paragraphBreak();
                }
            }
            offset += BUTTON_HEIGHT + BUTTON_PADDING;
        }
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
                requirements.clear();
                break;
            }
            else
            {
                if(requirements.isEmpty())
                {
                    requirements.add(new TranslationTextComponent("com.minecolonies.coremod.research.locked"));
                }
                else
                {
                    requirements.add(new TranslationTextComponent("Or").setStyle((Style.EMPTY).setFormatting(TextFormatting.AQUA)));
                }
                for(IResearchRequirement req : IGlobalResearchTree.getInstance().getResearch(branch, primary).getResearchRequirement())
                {
                    if(req instanceof ResearchResearchRequirement)
                    {
                        if(!building.getColony().getResearchManager().getResearchTree().hasCompletedResearch(((ResearchResearchRequirement) req).getResearchId()))
                        {
                            requirements.add(req.getDesc().setStyle((Style.EMPTY).setFormatting(TextFormatting.RED)));
                        }
                    }
                    else if(req instanceof AlternateBuildingResearchRequirement)
                    {
                        if(req.isFulfilled(building.getColony()))
                        {
                            req.getDesc();
                        }
                    }
                    // We'll include even completed buildings in the requirement list, since buildings can get undone/removed.
                    else if (req instanceof BuildingResearchRequirement)
                    {

                        if(!req.isFulfilled(building.getColony()))
                        {
                            requirements.add(req.getDesc().setStyle((Style.EMPTY).setFormatting(TextFormatting.RED)));
                        }
                        else
                        {
                            requirements.add(req.getDesc().setStyle((Style.EMPTY).setFormatting(TextFormatting.AQUA)));
                        }
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

        if (ResourceLocation.isResouceNameValid(button.getID()) && branches.contains(new ResourceLocation(button.getID())))
        {
            new WindowResearchTree(new ResourceLocation(button.getID()), building, this).open();
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
}
