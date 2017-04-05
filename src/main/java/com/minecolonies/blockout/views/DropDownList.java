package com.minecolonies.blockout.views;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonVanilla;
import com.minecolonies.blockout.controls.Label;
import com.minecolonies.blockout.views.ScrollingList;
import com.minecolonies.blockout.OverlayView;
import com.minecolonies.blockout.Pane;
import com.minecolonies.blockout.PaneParams;
import org.jetbrains.annotations.NotNull;
import com.minecolonies.blockout.Log;

import java.util.List;

/**
 * A DropDownList is a Button which when click display a ScrollingList.
 */
//TODO do not extend ButtonVanilla but extend view
public class DropDownList extends ButtonVanilla
{
    protected OverlayView   overlay;
    protected ScrollingList list;
    protected DataProvider  dataProvider;
    protected Handler  handlerdd;
    /**
     * width of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownWidth;
    /**
     * maximum height of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownHeight;
    protected int selectedIndex = -1;


    /**
     * Default constructor required by Blockout.
     */
    public DropDownList()
    {
        super();
    }

    /**
     * Constructs a DropDownList from PaneParams.
     *
     * @param params Params for the ScrollingList
     */
    public DropDownList(final PaneParams params)
    {
        super(params);
        final PaneParams.SizePair dropDownSize = params.getSizePairAttribute("dropDownSize", null, null);
        if (dropDownSize == null)
        {
            dropDownWidth = width;
            dropDownHeight = width;
        }
        else
        {
            dropDownWidth = dropDownSize.getX();
            dropDownHeight = dropDownSize.getY();
        }

        overlay = new OverlayView();
        overlay.setVisible(false);
        overlay.setPosition(0, 0);

        list = new ScrollingList(params);

        list.setSize(dropDownWidth, dropDownHeight);
        list.setPosition((x+width/2) - dropDownWidth/2, y + height);
        list.putInside(overlay);
        list.parseChildren(params);


        setHandler(new Button.Handler(){
            public void onButtonClicked(Button button)
            {
                 if (overlay.isVisible())
                 {
                     close();
                 }
                 else
                 {
                     overlay.setSize(button.getWindow().getInteriorWidth(),button.getWindow().getInteriorHeight());
                     overlay.putInside(button.getWindow());
                     open();
                 }
            }
       });
    }

    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    public void selectNext()
    {
        if (dataProvider.getElementCount() == 0)
        {
            setSelectedIndex(0);
        }
        else
        {
            setSelectedIndex((selectedIndex + 1) % dataProvider.getElementCount());
        }
    }

    public void selectPrevious()
    {
        if (dataProvider.getElementCount() == 0)
        {
            setSelectedIndex(0);
        }
        else
        {
            setSelectedIndex((selectedIndex + dataProvider.getElementCount() - 1) % dataProvider.getElementCount());
        }
    }

    public void setSelectedIndex(final int index)
    {
        if (index <0 || index >= dataProvider.getElementCount()) return;
        selectedIndex = index;

        setLabel(dataProvider.getLabel(selectedIndex));
        if (handlerdd != null)
        {
            handlerdd.onSelectedItemChanged(this,index);
        }
    }

    public void setDataProvider(final DataProvider p)
    {
        dataProvider=p;
        final DropDownList ddList = this;
        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return dataProvider.getElementCount();
            }
            //TODO remove this
            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateDropDownItem(ddList, rowPane, index, dataProvider.getLabel(index));
            }
        });

        refreshElementPanes();
    }

    /**
     * Use the data provider to update all the element panes.
     */
    public void refreshElementPanes()
    {
        list.refreshElementPanes();
        if (list.getContentHeight() < dropDownHeight)
        {
            list.setSize(dropDownWidth, list.getContentHeight());
        }
        else
        {
            list.setSize(dropDownWidth, dropDownHeight);
        }
    }

    /**
     * open the dropdown list.
     */
    public void open()
    {
        refreshElementPanes();
        overlay.setVisible(true);
        overlay.setFocus();
    }

    /**
     * close the dropdown list.
     */
    public void close()
    {
        overlay.setVisible(false);
    }

    /**
     * Interface for a data provider that updates pane scrolling list pane info.
     */
    public interface DataProvider
    {
        public int getElementCount();
        public String getLabel(final int index);
    }

    /*public void onButtonClicked(@NotNull final Button button)
    {
        @NotNull final Label idLabel = button.getParent().findPaneOfTypeByID("id", Label.class);;
        final int index = Integer.parseInt(idLabel.getLabelText());
        list.setSelectedIndex(index);
        list.close();
    }
*/

    private void updateDropDownItem(@NotNull final DropDownList list, @NotNull final Pane rowPane, final int index, final String label)
    {
        final Button choiceButton = rowPane.findPaneOfTypeByID("button", Button.class);
        rowPane.findPaneOfTypeByID("id", Label.class).setLabelText(Integer.toString(index));
        choiceButton.setLabel(label);
        choiceButton.setHandler(new Button.Handler()
        {
            public void onButtonClicked(@NotNull final Button button)
            {
                @NotNull final Label idLabel = button.getParent().findPaneOfTypeByID("id", Label.class);;
                final int index = Integer.parseInt(idLabel.getLabelText());
                list.setSelectedIndex(index);
                list.close();
            }
        });
    }


    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    //TODO rename to setHandler once we do not extend ButtonVanilla
    public void setDDHandler(final Handler h)
    {
        handlerdd = h;
    }

    /**
     * Used for windows that have dialog message.
     */
    @FunctionalInterface
    public interface Handler
    {
        /**
         * Called when a button is clicked.
         *
         * @param dialog the dialog that was closed.
         * @param done whether it is done or cancel.
         */
        void onSelectedItemChanged(final DropDownList list, final int index);
    }
}
