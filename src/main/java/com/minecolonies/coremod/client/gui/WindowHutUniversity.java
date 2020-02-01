package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.views.View;
import com.minecolonies.api.research.interfaces.IGlobalResearchTree;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingUniversity;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window for the lumberjack hut.
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
        for (final String branch: IGlobalResearchTree.getInstance().getBranches())
        {
            final ButtonImage button = new ButtonImage();
            button.setImage(new ResourceLocation(Constants.MOD_ID, MEDIUM_SIZED_BUTTON_RES));
            button.setLabel(branch);
            button.setSize(BUTTON_LENGTH, BUTTON_HEIGHT);
            button.setTextColor(SLIGHTLY_BLUE);
            button.setPosition(x + INITITAL_X_OFFSET, y + offset + INITITAL_Y_OFFSET);
            view.addChild(button);
            branches.add(branch);

            offset += button.getHeight() + BUTTON_PADDING;
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        super.onButtonClicked(button);
        final String label = button.getLabel();

        if (branches.contains(label))
        {
            new WindowResearchTree( label, building, this).open();
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
