package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Gradient;
import com.ldtteam.blockout.controls.Label;
import com.ldtteam.blockout.views.DragView;
import com.minecolonies.api.research.GlobalResearchTree;
import com.minecolonies.api.research.IGlobalResearch;
import com.minecolonies.api.research.LocalResearchTree;
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
    public WindowResearchTree(final LocalResearchTree currentTree, final String branch)
    {
        super(Constants.MOD_ID + R_TREE_RESOURCE_SUFFIX);
        this.branch = branch;

        final List<String> researchList = GlobalResearchTree.researchTree.getPrimaryResearch(branch);
        final DragView view = findPaneOfTypeByID(DRAG_VIEW_ID, DragView.class);

        drawTree(0, 0, view, researchList);

        //todo then also display cost

        //todo add button for action (make button send message to server, make button check if items are in inventory)
        //todo render stage with different color
        //todo render progress

        //todo add x button

        //todo add building tick to tick the research.
        //todo add AI which wanders around


        //GlobalResearchTree.researchTree
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
    }

    /**
     * Draw the tree of research.
     * @param height the start y offset.
     * @param depth the current depth.
     * @param view the view to append it to.
     * @param researchList the list of research to go through.
     * @return the next y offset.
     */
    public int drawTree(final int height, final int depth, final DragView view, final List<String> researchList)
    {
        int nextHeight = height;
        for (int i = 0; i < researchList.size(); i++)
        {
            final String researchLabel = researchList.get(i);
            int offsetX = (depth * (175 + 20));

            final IGlobalResearch research = GlobalResearchTree.researchTree.getResearch(branch, researchLabel);
            final Gradient gradient = new Gradient();
            gradient.setSize(175, 50);
            gradient.setPosition(x + offsetX + 50, (nextHeight + i) * (50 + 20) + 20);
            view.addChild(gradient);

            final Label nameLabel = new Label();
            nameLabel.setLabelText(research.getDesc());
            nameLabel.setPosition(x + offsetX + 10 + 90, (nextHeight + i) * (50 + 20) + 5 + 20 + 5);
            view.addChild(nameLabel);

            if (research.getResearchRequirement() != null)
            {
                final Label requirementLabel = new Label();
                requirementLabel.setLabelText(research.getResearchRequirement().getDesc().getFormattedText());
                requirementLabel.setPosition(offsetX + 10 + 45, nameLabel.getY() + nameLabel.getHeight() + 10);
                view.addChild(requirementLabel);
            }

            final Label effectLabel = new Label();
            effectLabel.setLabelText(research.getEffect().getDesc().getFormattedText());
            effectLabel.setPosition(offsetX + 10 + 45, nameLabel.getY() + nameLabel.getHeight() * 2 + 10 + 10);
            view.addChild(effectLabel);

            if (!research.getChilds().isEmpty())
            {
                nextHeight = drawTree(nextHeight + i, depth + 1, view, research.getChilds());
            }
        }

        return nextHeight;
    }

}
