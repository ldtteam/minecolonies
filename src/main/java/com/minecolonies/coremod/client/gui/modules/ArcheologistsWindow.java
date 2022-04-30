package com.minecolonies.coremod.client.gui.modules;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.client.gui.AbstractModuleWindow;
import com.minecolonies.coremod.colony.buildings.moduleviews.ArcheologistsModuleView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TranslatableComponent;

/**
 * BOWindow for the Archeologist building.
 */
public class ArcheologistsWindow extends AbstractModuleWindow
{
    /**
     * Resource suffix of the GUI.
     */
    private static final String HUT_ARCHEOLOGIST_RESOURCE_SUFFIX = ":gui/layouthuts/layoutarcheologist.xml";

    /**
     * Id of the target location inside the GUI.
     */
    private static final String TARGET = "target";

    /**
     * Id of the previous targets list inside the GUI.
     */
    private static final String PREVIOUS_TARGETS = "previousTargets";

    /**
     * Id of the previous target location inside the previous targets list.
     */
    private static final String PREVIOUS_TARGET = "previousTarget";

    /**
     * The world.
     */
    private final ClientLevel world = Minecraft.getInstance().level;

    /**
     * The module view.
     */
    private final ArcheologistsModuleView moduleView;

    /**
     * Constructor for the window of the archeologist.
     *
     * @param moduleView {@link ArcheologistsModuleView}.
     */
    public ArcheologistsWindow(final IBuildingView building, ArcheologistsModuleView moduleView)
    {
        super(building, Constants.MOD_ID + HUT_ARCHEOLOGIST_RESOURCE_SUFFIX);
        this.moduleView = moduleView;
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        updateTargetLocation();
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();
        updateTargetLocation();
        updatePreviousTargets();
    }

    private void updateTargetLocation()
    {
        final Text targetLocationTextField = findPaneOfTypeByID(ArcheologistsWindow.TARGET, Text.class);
        if (moduleView.hasTarget())
        {
            targetLocationTextField.setText(
              ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates",
                moduleView.getTarget().structureCenter().getX(),
                moduleView.getTarget().structureCenter().getY(),
                moduleView.getTarget().structureCenter().getZ()))
            );
        }
        else
        {
            targetLocationTextField.setText(new TranslatableComponent(TranslationConstants.NO_TARGET));
        }
    }

    private void updatePreviousTargets()
    {
        final ScrollingList previousTargetsList = findPaneOfTypeByID(ArcheologistsWindow.PREVIOUS_TARGETS, ScrollingList.class);
        previousTargetsList.setDataProvider(
          this.moduleView.getPreviouslyVisitedStructures()::size,
          (index, rowPane) -> {
              final Text text = rowPane.findPaneOfTypeByID(ArcheologistsWindow.PREVIOUS_TARGET, Text.class);
              final BlockPos location = moduleView.getPreviouslyVisitedStructures().get(index);
              text.setText(
                ComponentUtils.wrapInSquareBrackets(new TranslatableComponent("chat.coordinates",
                  location.getX(),
                  location.getY(),
                  location.getZ()))
              );
          }
        );
    }

}
