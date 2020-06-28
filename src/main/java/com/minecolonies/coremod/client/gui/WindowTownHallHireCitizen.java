package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.Button;
import com.ldtteam.blockout.controls.ButtonHandler;
import com.ldtteam.blockout.controls.ButtonImage;
import com.ldtteam.blockout.controls.ItemIcon;
import com.ldtteam.blockout.views.Window;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.colony.ColonyView;
import com.minecolonies.coremod.network.messages.server.colony.citizen.BuyCitizenMessage;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
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
        final IItemHandler playerInv = new InvWrapper(Minecraft.getInstance().player.inventory);

        // Cost of new packages, basecost 1
        final int buyCitizenCost = colony.getBoughtCitizenCost() + 1;

        // Hay Bale
        findPaneOfTypeByID(BUTTON_HAY_BALE_ICON, ItemIcon.class).setItem(Item.getItemFromBlock(Blocks.HAY_BLOCK).getDefaultInstance());
        findPaneOfTypeByID(BUTTON_HAY_BALE, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Item.getItemFromBlock(Blocks.HAY_BLOCK)) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_HAY_BALE, ButtonImage.class).disable();
        }

        // Books
        findPaneOfTypeByID(BUTTON_BOOKS_ICON, ItemIcon.class).setItem(Items.BOOK.getDefaultInstance());
        findPaneOfTypeByID(BUTTON_BOOKS, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Items.BOOK) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_BOOKS, ButtonImage.class).disable();
        }

        // Emerald
        findPaneOfTypeByID(BUTTON_EMERALD_ICON, ItemIcon.class).setItem(Items.EMERALD.getDefaultInstance());
        findPaneOfTypeByID(BUTTON_EMERALD, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Items.EMERALD) < buyCitizenCost)
        {
            findPaneOfTypeByID(BUTTON_EMERALD, ButtonImage.class).disable();
        }

        // Diamond
        findPaneOfTypeByID(BUTTON_DIAMOND_ICON, ItemIcon.class).setItem(Items.DIAMOND.getDefaultInstance());
        findPaneOfTypeByID(BUTTON_DIAMOND, ButtonImage.class).setLabel(buyCitizenCost + "x");
        if (InventoryUtils.getItemCountInItemHandler(playerInv, Items.DIAMOND) < buyCitizenCost)
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
                Network.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.HAY_BALE, colony.getID(), colony.getDimension()));
                break;
            }
            case BUTTON_BOOKS:
            {
                Network.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.BOOK, colony.getID(), colony.getDimension()));
                break;
            }
            case BUTTON_EMERALD:
            {
                Network.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.EMERALD, colony.getID(), colony.getDimension()));
                break;
            }
            case BUTTON_DIAMOND:
            {
                Network.getNetwork().sendToServer(new BuyCitizenMessage(BuyCitizenMessage.BuyCitizenType.DIAMOND, colony.getID(), colony.getDimension()));
                break;
            }
        }
    }
}
