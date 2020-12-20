package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.Gradient;
import com.ldtteam.blockout.views.View;
import com.minecolonies.api.research.IGlobalResearch;
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
    private final List<String> branches = new ArrayList<>();

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
        branches.sort(Comparator.comparing(String::toString, String.CASE_INSENSITIVE_ORDER));
        for (final String branch : branches)
        {
            List<IFormattableTextComponent> requirements = getHidingRequirementDesc(branch);
            if(requirements.isEmpty())
            {
                final ButtonImage button = new ButtonImage();
                button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                button.setLabel(branch);
                button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                button.setTextColor(SLIGHTLY_BLUE);
                button.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
                view.addChild(button);

                offset += button.getHeight() + BUTTON_PADDING;
            }
            else
            {
                final Gradient gradient = new Gradient();
                gradient.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                gradient.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
                gradient.setGradientStart(239, 230, 215, 255);
                gradient.setGradientEnd(239, 230, 215, 255);
                gradient.setHoverToolTip(requirements);
                view.addChild(gradient);
                final ButtonImage button = new ButtonImage();
                button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
                button.setLabel("-----------");
                button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
                button.setTextColor(SLIGHTLY_BLUE);
                button.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
                view.addChild(button);

                offset += button.getHeight() + BUTTON_PADDING;
            }
        }
    }

    /**
     * Gets a list describing what requirements must be met to make at least one primary research for a branch visible.
     *
     * @param branch  The identifier for a branch.
     * @return An empty list if at least one primary research is visible, or a list of IFormattableTextComponents describing the dependencies for each hidden primary research.
     */
    public List<IFormattableTextComponent> getHidingRequirementDesc(final String branch)
    {
        final List<IFormattableTextComponent> requirements = new ArrayList<>();
        for(final String primary : IGlobalResearchTree.getInstance().getPrimaryResearch(branch))
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
                        if(!building.getColony().getBuildings().contains(((BuildingResearchRequirement) req).getBuilding()))
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
        final String label = button.getLabel();

        if (branches.contains(label))
        {
            new WindowResearchTree(label, building, this).open();
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
