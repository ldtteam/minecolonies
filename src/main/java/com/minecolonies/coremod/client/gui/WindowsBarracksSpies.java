package com.minecolonies.coremod.client.gui;

import com.ldtteam.blockout.controls.*;
import com.ldtteam.blockout.views.Window;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.colony.buildings.views.IBuildingView;
import com.minecolonies.api.tileentities.TileEntityRack;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.network.messages.server.colony.HireSpiesMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

/**
 * UI for hiring spies on the barracks
 */
public class WindowsBarracksSpies extends Window implements ButtonHandler
{
    /**
     * The xml file for this gui
     */
    private static final String SPIES_GUI_XML = ":gui/windowbarracksspies.xml";

    /**
     * The cancel button id
     */
    private static final String BUTTON_CANCEL = "cancel";

    /**
     * The hire spies button id
     */
    private static final String BUTTON_HIRE = "hireSpies";

    /**
     * The spies button icon id
     */
    private static final String SPIES_BUTTON_ICON = "hireSpiesIcon";

    /**
     * The gold amount label id
     */
    private static final String GOLD_COST_LABEL = "amount";

    /**
     * Text element id
     */
    private static final String TEXT_ID = "text";

    private static final int GOLD_COST = 5;

    /**
     * The client side colony data
     */
    private final IBuildingView buildingView;

    public WindowsBarracksSpies(final IBuildingView buildingView, final BlockPos buildingPos)
    {
        super(Constants.MOD_ID + SPIES_GUI_XML);
        this.buildingView = buildingView;

        findPaneOfTypeByID(SPIES_BUTTON_ICON, ItemIcon.class).setItem(Items.GOLD_INGOT.getDefaultInstance());
        findPaneOfTypeByID(GOLD_COST_LABEL, Label.class).setLabelText("x5");

        final IItemHandler rackInv = ((TileEntityRack) buildingView.getColony().getWorld().getTileEntity(buildingPos)).getInventory();
        final IItemHandler playerInv = new InvWrapper(Minecraft.getInstance().player.inventory);
        int goldCount = InventoryUtils.getItemCountInItemHandler(playerInv, Items.GOLD_INGOT);
        goldCount += InventoryUtils.getItemCountInItemHandler(rackInv, Items.GOLD_INGOT);

        if (!buildingView.getColony().isRaiding() || goldCount < GOLD_COST || buildingView.getColony().areSpiesEnabled())
        {
            findPaneOfTypeByID(BUTTON_HIRE, ButtonImage.class).disable();
        }
        findPaneOfTypeByID(TEXT_ID, Text.class).setTextContent(LanguageHandler.format("com.minecolonies.coremod.gui.barracks.spies.desc"));
    }

    @Override
    public void onButtonClicked(final Button button)
    {
        switch (button.getID())
        {
            case BUTTON_CANCEL:
            {
                this.close();
                break;
            }
            case BUTTON_HIRE:
            {
                findPaneOfTypeByID(BUTTON_HIRE, ButtonImage.class).disable();
                Network.getNetwork().sendToServer(new HireSpiesMessage(buildingView.getColony()));
                this.close();
                break;
            }
        }
    }
}
