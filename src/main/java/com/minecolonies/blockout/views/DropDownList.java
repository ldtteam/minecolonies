package com.minecolonies.blockout.views;

import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonVanilla;
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
public class DropDownList extends ButtonVanilla
{
    protected OverlayView   overlay;
    protected ScrollingList list;
    /**
     * width of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownWidth;
    /**
     * maximum height of the scrolling list, by default it is the same as the DropDownList width.
     */
    protected int dropDownHeight;


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

    public void setDataProvider(final ScrollingList.DataProvider p)
    {
        list.setDataProvider(p);
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
}
