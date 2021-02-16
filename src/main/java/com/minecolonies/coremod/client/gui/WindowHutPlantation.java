package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingPlantation;
import com.minecolonies.coremod.research.UnlockAbilityResearchEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.minecolonies.api.research.util.ResearchConstants.PLANT_2;

/**
 * Window for the plantation hut.
 */
public class WindowHutPlantation extends AbstractWindowWorkerBuilding<BuildingPlantation.View>
{
    /**
     * The mode button id.
     */
    private static final String BLOCK_BUTTON = "block";

    private static final String HINT_LABEL = "hint";

    /**
     * The id of the gui.
     */
    private static final String PLANTATION_RESOURCE_SUFFIX = ":gui/windowhutplantation.xml";

    /**
     * Constructor for the window of the planter hut.
     *
     * @param building {@link BuildingPlantation.View}.
     */
    public WindowHutPlantation(final BuildingPlantation.View building)
    {
        super(building, Constants.MOD_ID + PLANTATION_RESOURCE_SUFFIX);
        final Button plantSettingsButton = findPaneOfTypeByID(BLOCK_BUTTON, Button.class);
        final Text plantHintLabel = findPaneOfTypeByID(HINT_LABEL, Text.class);

        final UnlockAbilityResearchEffect effect = building.getColony().getResearchManager().getResearchEffects().getEffect(PLANT_2, UnlockAbilityResearchEffect.class);
        if (effect != null && effect.getEffect())
        {
            plantHintLabel.setText(new TranslationTextComponent(TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_WORKER_HUTS_PLANTATION_NOT_PLANT));
        }

        registerButton(BLOCK_BUTTON, this::switchPlantingMode);
        setupSettings(plantSettingsButton);
    }

    /**
     * Switch the mode after clicking the button.
     *
     * @param phaseSettingButton the clicked button.
     */
    private void switchPlantingMode(final Button phaseSettingButton)
    {
        final List<Item> phases = building.getPhases();
        int index = building.getPhases().indexOf(building.getCurrentPhase()) + 1;

        if (index >= phases.size())
        {
            index = 0;
        }

        building.setPhase(phases.get(index));
        setupSettings(phaseSettingButton);
    }

    /**
     * Setup the settings.
     *
     * @param plantSettingsButton the buttons to setup.
     */
    private void setupSettings(final Button plantSettingsButton)
    {
        if (building.getCurrentPhase() != null)
        {
            plantSettingsButton.setText(new ItemStack(building.getCurrentPhase()).getDisplayName());
        }
    }

    @NotNull
    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerhuts.plantation";
    }
}

