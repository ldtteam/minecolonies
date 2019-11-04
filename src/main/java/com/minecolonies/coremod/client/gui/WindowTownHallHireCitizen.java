package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonHandler;
import com.minecolonies.blockout.controls.ButtonImage;
import com.minecolonies.blockout.controls.ItemIcon;
import com.minecolonies.blockout.views.Window;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.BuyCitizenMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public class WindowTownHallHireCitizen extends Window implements ButtonHandler
{
    /**
     * Button ID's used in the xml
     */
    private static final String BUTTON_DONE          = "done";
    private static final String BUTTON_DIAMOND       = "hirebottomright";
    private static final String BUTTON_EMERALD       = "hirebottomleft";
    private static final String BUTTON_BOOKS         = "hiretopright";
    private static final String BUTTON_HAY_BALE      = "hiretopleft";
    private static final String BUTTON_DIAMOND_ICON  = "hirebottomrighticon";
    private static final String BUTTON_EMERALD_ICON  = "hirebottomlefticon";
    private static final String BUTTON_BOOKS_ICON    = "hiretoprighticon";
    private static final String BUTTON_HAY_BALE_ICON = "hiretoplefticon";

    /**
     * The xml file for this gui
     */
    private static final String TOWNHALL_NAME_RESOURCE_SUFFIX = ":gui/townhall/windowtownhallhirecitizen.xml";

    /**
     * The client side colony data
     */
    private final IColonyView colony;

    /**
     * Constructor for a town hall rename entry window.
     *
     * @param c {@link ColonyView}
     */
    public WindowTownHallHireCitizen(final IColonyView c)
    {
        super(Constants.MOD_ID + TOWNHALL_NAME_RESOURCE_SUFFIX);
        this.colony = c;
    }

    @Override
    public void onOpened()
    {
        // Player inventory
        final IItemHandler playerInv = new InvWrapper(Minecraft.getMinecraft().player.inventory);

        // Cost of new packages, basecost 1
        final int buyCitizenCost = colony.getBoughtCitizenCost() + 1;

        // Hay Bale
        findPaneOfTypeByID(BUTTON_HAY_BALE_ICON, ItemIcon.class).setItem(Item.getItemFromBlock(Blocks.HAY_BLOCK).getDefaultInstance());
        findPaneOfTypeByID(BUTTON_HAY_BALE, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Item.getItemFromBlock(Blocks.HAY_BLOCK), 0) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_HAY_BALE, ButtonImage.class).disable();
        }

        // Books
        findPaneOfTypeByID(BUTTON_BOOKS_ICON, ItemIcon.class).setItem(Items.BOOK.getDefaultInstance());
        findPaneOfTypeByID(BUTTON_BOOKS, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Items.BOOK, 0) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_BOOKS, ButtonImage.class).disable();
        }

        // Emerald
        findPaneOfTypeByID(BUTTON_EMERALD_ICON, ItemIcon.class).setItem(Items.EMERALD.getDefaultInstance());
        findPaneOfTypeByID(BUTTON_EMERALD, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Items.EMERALD, 0) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_EMERALD, ButtonImage.class).disable();
        }

        // Diamond
        findPaneOfTypeByID(BUTTON_DIAMOND_ICON, ItemIcon.class).setItem(Items.DIAMOND.getDefaultInstance());
        findPaneOfTypeByID(BUTTON_DIAMOND, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Items.DIAMOND, 0) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_DIAMOND, ButtonImage.class).disable();
        }

        // Disable all if colony at max size
        if (colony.getCitizens().size() >= colony.getCitizenCountLimit())
        {
            findPaneOfTypeByID(BUTTON_HAY_BALE, ButtonImage.class).disable();
            findPaneOfTypeByID(BUTTON_BOOKS, ButtonImage.class).disable();
            findPaneOfTypeByID(BUTTON_EMERALD, ButtonImage.class).disable();
            findPaneOfTypeByID(BUTTON_DIAMOND, ButtonImage.class).disable();
        }
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        findPaneOfTypeByID(BUTTON_HAY_BALE, ButtonImage.class).disable();
        findPaneOfTypeByID(BUTTON_BOOKS, ButtonImage.class).disable();
        findPaneOfTypeByID(BUTTON_EMERALD, ButtonImage.class).disable();
        findPaneOfTypeByID(BUTTON_DIAMOND, ButtonImage.class).disable();

        switch (button.getID())
        {
            case BUTTON_DONE:
            {
                if (colony.getTownHall() != null)
                {
                    colony.getTownHall().openGui(false);
                }
                break;
            }
            case BUTTON_HAY_BALE:
            {
                MineColonies.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.HAY_BALE, colony.getID(), colony.getDimension()));
                break;
            }
            case BUTTON_BOOKS:
            {
                MineColonies.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.BOOK, colony.getID(), colony.getDimension()));
                break;
            }
            case BUTTON_EMERALD:
            {
                MineColonies.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.EMERALD, colony.getID(), colony.getDimension()));
                break;
            }
            case BUTTON_DIAMOND:
            {
                MineColonies.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.DIAMOND, colony.getID(), colony.getDimension()));
                break;
            }
        }
    }
}
