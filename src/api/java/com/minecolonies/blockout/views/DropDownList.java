package com.ldtteam.blockout.views;

import com.ldtteam.blockout.Pane;
import com.ldtteam.blockout.PaneParams;
import com.ldtteam.blockout.controls.*;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * A DropDownList is a Button which when click display a ScrollingList below it.
 */
public class DropDownList extends View implements ButtonHandler
{
    /**
     * View in which the list will be displayed.
     */
    protected OverlayView overlay;

    /**
     * button to access to the list.
     */
    protected Button button;

    /**
     * List to choose from.
     */
    protected ScrollingList list;
    /**
     * date required to fill the list.
     */
    protected DataProvider  dataProvider;

    /**
     * handler for the accept method.
     */
    protected Consumer<DropDownList> handler;

    /**
     * width of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownWidth;

    /**
     * maximum height of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownHeight;

    /**
     * index of the selected item.
     */
    protected int selectedIndex = -1;

    /**
     * Temporary fix until new release
     */
    protected int dropDownFixX = 0;

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
        dropDownWidth = dropDownSize == null ? width : dropDownSize.getX();
        //When unknown, we use the same height as it is wide.
        dropDownHeight = dropDownSize == null ? width : dropDownSize.getY();
        dropDownFixX = params.getIntAttribute("dropfixx", dropDownFixX);

        if(params.getStringAttribute("source", "").isEmpty())
        {
            button = new ButtonVanilla(params);
        }
        else
        {
            button = new ButtonImage(params);
        }
        button.putInside(this);

        overlay = new OverlayView();
        overlay.setVisible(false);
        overlay.setPosition(0, 0);

        list = new ScrollingList(params);
        list.setSize(dropDownWidth, dropDownHeight);
        list.setPosition((x + width / 2) - dropDownWidth / 2 + dropDownFixX, y + height);
        list.putInside(overlay);
        list.parseChildren(params);

        button.setHandler(this);
    }

    /**
     * handle when the button is clicked on.
     * <p>
     * The list is shown or hidden depending of the previous state.
     *
     * @param button which have been clicked on.
     */
    public void onButtonClicked(@NotNull final Button button)
    {
        if (button == this.button)
        {
            if (overlay.isVisible())
            {
                close();
            }
            else
            {
                overlay.setSize(this.getWindow().getInteriorWidth(), this.getWindow().getInteriorHeight());
                overlay.putInside(button.getWindow());
                open();
            }
        }
        else
        {
            onButtonClickedFromList(button);
        }
    }

    /**
     * close the dropdown list.
     */
    public void close()
    {
        overlay.setVisible(false);
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
     * handle when a button in the list have been clicked on.
     *
     * @param button which have been clicked on.
     */
    private void onButtonClickedFromList(@NotNull final Button button)
    {
        final Label idLabel = button.getParent().findPaneOfTypeByID("id", Label.class);
        if (idLabel != null)
        {
            final int index = Integer.parseInt(idLabel.getLabelText());
            setSelectedIndex(index);
            close();
        }
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
     * get the index of the selected item in the list.
     *
     * @return the index of the selected ietem.
     */
    public int getSelectedIndex()
    {
        return selectedIndex;
    }

    /**
     * set the index of the selected item in the list.
     *
     * @param index of the selected item
     */
    public void setSelectedIndex(final int index)
    {
        if (index < 0 || index >= dataProvider.getElementCount())
        {
            return;
        }
        selectedIndex = index;

        button.setLabel(dataProvider.getLabel(selectedIndex));
        if (handler != null)
        {
            handler.accept(this);
        }
    }

    /**
     * Select the previous Item in the list.
     */
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

    /**
     * Select the next item in the list.
     */
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

    /**
     * Set the data provider to fill the list.
     *
     * @param p is the data provider for the list.
     */
    public void setDataProvider(final DataProvider p)
    {
        dataProvider = p;
        list.setDataProvider(new ScrollingList.DataProvider()
        {
            @Override
            public int getElementCount()
            {
                return dataProvider.getElementCount();
            }

            @Override
            public void updateElement(final int index, @NotNull final Pane rowPane)
            {
                updateDropDownItem(rowPane, index, dataProvider.getLabel(index));
            }
        });

        refreshElementPanes();
    }

    /**
     * Update an pane item in the list.
     *
     * @param rowPane which need the update
     * @param index   of the item
     * @param label   use for this item
     */
    private void updateDropDownItem(@NotNull final Pane rowPane, final int index, final String label)
    {
        final Button choiceButton = rowPane.findPaneOfTypeByID("button", Button.class);
        if (choiceButton != null)
        {
            // is idLabel necessary ?
            final Label idLabel = rowPane.findPaneOfTypeByID("id", Label.class);
            if (idLabel != null)
            {
                idLabel.setLabelText(Integer.toString(index));
            }
            choiceButton.setLabel(label);
            choiceButton.setHandler(this);
        }
    }

    @Override
    public void setVisible(final boolean v)
    {
        button.setVisible(v);
    }

    @Override
    public void setEnabled(final boolean e)
    {
        button.setEnabled(e);
    }

    @Override
    public void drawSelf(final int mx, final int my)
    {
        button.drawSelf(mx, my);
    }

    @Override
    public void click(final int mx, final int my)
    {
        button.click(mx, my);
    }

    /**
     * Set the button handler for this button.
     *
     * @param h The new handler.
     */
    public void setHandler(final Consumer<DropDownList> h)
    {
        handler = h;
    }

    /**
     * Interface for a data provider that updates pane scrolling list pane info.
     */
    public interface DataProvider
    {
        int getElementCount();

        String getLabel(final int index);
    }
}
