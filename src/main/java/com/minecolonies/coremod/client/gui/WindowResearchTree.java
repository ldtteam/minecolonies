package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Gradient;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.DragView;
import com.minecolonies.api.research.GlobalResearchTree;
import com.minecolonies.api.research.IResearch;
import com.minecolonies.api.research.ResearchTree;
import com.minecolonies.api.util.constant.Constants;

import java.util.List;

import static com.minecolonies.api.util.constant.WindowConstants.*;
import static org.jline.utils.AttributedStyle.WHITE;

/**
 * Window to display a particular branch of the tree.
 */
public class WindowResearchTree extends AbstractWindowSkeleton
{
    private static final int LABEL_OFFSET_X = 10;
    private static final int LABEL_OFFSET_Y = 5;
    private static final int Y_PADDING    = 20;
    private static final String DRAG_VIEW_ID = "dragView";

    private final String branch;

    /**
     * Create this window.
     * @param currentTree the current tree belonging to the colony.
     * @param branch the current branch we operate on.
     */
    public WindowResearchTree(final ResearchTree currentTree, final String branch)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX);
        this.branch = branch;

        final List<String> researchList = GlobalResearchTree.researchTree.getPrimaryResearch(branch);
        final DragView view = findPaneOfTypeByID(DRAG_VIEW_ID, DragView.class);

        drawTree(0, 0, view, researchList);

        //todo then also display effect, and requirement
        //todo fix loading of cost from configuration

        //todo add button for action
        //todo render stage with different color
        //todo render progress

        //GlobalResearchTree.researchTree
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
    }

    /**
     * Draw the tree of research.
     * @param startY the start y offset.
     * @param depth the current depth.
     * @param view the view to append it to.
     * @param researchList the list of research to go through.
     * @return the next y offset.
     */
    public int drawTree(final int startY, final int depth, final DragView view, final List<String> researchList)
    {
        int otherStart = startY;
        for (int i = 0; i < researchList.size(); i++)
        {
            final String researchLabel = researchList.get(i);
            int offsetX = (depth * (150 + 20));

            final IResearch research = GlobalResearchTree.researchTree.getResearch(branch, researchLabel);
            final Gradient gradient = new Gradient();
            gradient.setSize(150, 50);
            gradient.setPosition(x + offsetX + 50, y + startY + i * (50 + 20) + 20);
            view.addChild(gradient);

            final Label nameLabel = new Label();
            nameLabel.setLabelText(researchLabel);
            nameLabel.setPosition(x + offsetX + 10 + 70, y + startY + i * (50 + 20) + 5 + 20 + 5);
            nameLabel.setColor(WHITE, WHITE);
            view.addChild(nameLabel);

            final Label requirementLabel = new Label();
            requirementLabel.setLabelText(research.getResearchRequirement().getDesc().getFormattedText());
            requirementLabel.setPosition(x + offsetX + 10 + 70, nameLabel.getY() + nameLabel.getHeight() + 5);
            requirementLabel.setColor(WHITE, WHITE);
            view.addChild(requirementLabel);

            final Label effectLabel = new Label();
            effectLabel.setLabelText(research.getEffect().getDesc().getFormattedText());
            effectLabel.setPosition(x + offsetX + 10 + 70, requirementLabel.getY() + requirementLabel.getHeight() + 5);
            effectLabel.setColor(WHITE, WHITE);
            view.addChild(effectLabel);

            otherStart = drawTree(otherStart, depth + 1, view, research.getChilds());
        }

        return otherStart + researchList.size() * (50 + 20);
    }

}
