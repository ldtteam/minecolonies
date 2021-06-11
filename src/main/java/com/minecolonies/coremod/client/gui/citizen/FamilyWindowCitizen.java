package com.minecolonies.coremod.client.gui.citizen;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneBuilders;
import com.ldtteam.blockout.controls.AbstractTextBuilder;
import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.Text;
import com.ldtteam.blockout.views.ScrollingList;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.buildings.HiringMode;
import com.minecolonies.api.colony.buildings.IBuildingCanBeHiredFrom;
import com.minecolonies.api.entity.citizen.Skill;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.CitizenDataView;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;

import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRE_PAUSE;
import static com.minecolonies.api.util.constant.TranslationConstants.COM_MINECOLONIES_COREMOD_GUI_HIRE_UNPAUSE;
import static com.minecolonies.api.util.constant.WindowConstants.*;
import static com.minecolonies.api.util.constant.WindowConstants.ATTRIBUTES_LABEL;

/**
 * Window for the citizen.
 */
public class FamilyWindowCitizen extends AbstractWindowCitizen
{
    /**
     * Assigned citizen.
     */
    private final ICitizenDataView citizen;

    /**
     * Holder of a list element
     */
    protected final ScrollingList siblingList;
    protected final ScrollingList childrenList;

    /**
     * Constructor to initiate the citizen windows.
     *
     * @param citizen citizen to bind the window to.
     */
    public FamilyWindowCitizen(final ICitizenDataView citizen)
    {
        super(citizen, Constants.MOD_ID + CITIZEN_FAM_RESOURCE_SUFFIX);
        this.citizen = citizen;
        siblingList = findPaneOfTypeByID("siblings", ScrollingList.class);
        childrenList = findPaneOfTypeByID("children", ScrollingList.class);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        final String firstParent = citizen.getParents().getA();
        final String secondParent = citizen.getParents().getB();

        findPaneOfTypeByID("parentA", Text.class).setText(firstParent.isEmpty() ? new TranslationTextComponent("com.minecolonies.coremod.gui.citizen.family.unknown") : new StringTextComponent(firstParent));
        findPaneOfTypeByID("parentB", Text.class).setText(secondParent.isEmpty() ? new TranslationTextComponent("com.minecolonies.coremod.gui.citizen.family.unknown") : new StringTextComponent(secondParent));

        final int partner = citizen.getPartner();
        final ICitizenDataView partnerView = colony.getCitizen(partner);
        final Text partnerText = findPaneOfTypeByID("partner", Text.class);

        if (partnerView == null)
        {
            partnerText.setText(new StringTextComponent("-"));
        }
        else
        {
            partnerText.setText(new StringTextComponent(partnerView.getName()));
        }

        childrenList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return citizen.getChildren().size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID("name", Text.class).setText(new StringTextComponent(colony.getCitizen(citizen.getChildren().get(index)).getName()));
            }
        });

        siblingList.setDataProvider(new ScrollingList.DataProvider()
        {
            /**
             * The number of rows of the list.
             * @return the number.
             */
            @Override
            public int getElementCount()
            {
                return citizen.getSiblings().size();
            }

            /**
             * Inserts the elements into each row.
             * @param index the index of the row/list element.
             * @param rowPane the parent Pane for the row, containing the elements to update.
             */
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                rowPane.findPaneOfTypeByID("name", Text.class).setText(new StringTextComponent(colony.getCitizen(citizen.getSiblings().get(index)).getName()));
            }
        });
    }
}
