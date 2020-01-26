package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.views.View;
import com.minecolonies.api.research.GlobalResearchTree;
import com.minecolonies.api.research.ResearchTree;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.SLIGHTLY_BLUE;

/**
 * Window for the lumberjack hut.
 */
public class WindowHutUniversity extends AbstractWindowWorkerBuilding<BuildingUniversity.View>
{
    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutuniversity.xml";
    private static final String BRANCH_VIEW_ID = "pageBranches";
    private static final int INITITAL_X_OFFSET = 20;
    private static final int INITITAL_Y_OFFSET = 30;
    private static final int BUTTON_PADDING    = 10;

    /**
     * The list of branches of the tree.
     */
    private final List<String> branches = new ArrayList<>();

    /**
     * The local tree.
     */
    private final ResearchTree localTree;

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
        for (final String branch: GlobalResearchTree.researchTree.getBranches())
        {
            final ButtonImage button = new ButtonImage();
            button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
            button.setLabel(branch);
            button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
            button.setTextColor(SLIGHTLY_BLUE);
            button.setPosition(x + INITITAL_X_OFFSET + 10, y + offset + INITITAL_Y_OFFSET + 30);
            view.addChild(button);
            branches.add(branch);

            offset += button.getHeight() + BUTTON_PADDING;
        }

        localTree = building.getColony().getResearchTree();
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);
        final String label = button.getLabel();

        if (branches.contains(label))
        {
            new WindowResearchTree(localTree, label).open();
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

