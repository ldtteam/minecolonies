package com.minecolonies.core.client.gui.citizen;

import com.ldtteam.blockui.Pane;
import com.ldtteam.blockui.controls.Text;
import com.ldtteam.blockui.views.ScrollingList;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * BOWindow for the citizen.
 */
public class FamilyWindowCitizen extends AbstractWindowCitizen
{
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
        super(citizen, new ResourceLocation(Constants.MOD_ID, "gui/citizen/family.xml"));
        siblingList = findPaneOfTypeByID("siblings", ScrollingList.class);
        childrenList = findPaneOfTypeByID("children", ScrollingList.class);
    }

    @Override
    public void onOpened()
    {
        super.onOpened();

        final String firstParent = citizen.getParents().getA();
        final String secondParent = citizen.getParents().getB();

        findPaneOfTypeByID("parentA", Text.class).setText(firstParent.isEmpty() ? Component.translatable("com.minecolonies.coremod.gui.citizen.family.unknown") : Component.literal(firstParent));
        findPaneOfTypeByID("parentB", Text.class).setText(secondParent.isEmpty() ? Component.translatable("com.minecolonies.coremod.gui.citizen.family.unknown") : Component.literal(secondParent));

        final int partner = citizen.getPartner();
        final ICitizenDataView partnerView = colony.getCitizen(partner);
        final Text partnerText = findPaneOfTypeByID("partner", Text.class);

        if (partnerView == null)
        {
            partnerText.setText(Component.literal("-"));
        }
        else
        {
            partnerText.setText(Component.literal(partnerView.getName()));
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
                rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal(colony.getCitizen(citizen.getChildren().get(index)).getName()));
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
                rowPane.findPaneOfTypeByID("name", Text.class).setText(Component.literal(colony.getCitizen(citizen.getSiblings().get(index)).getName()));
            }
        });
    }
}
