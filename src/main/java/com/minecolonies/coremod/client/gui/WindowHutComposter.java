package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonImage;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.IColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingComposter;
import com.minecolonies.coremod.items.ModItems;
import com.minecolonies.coremod.network.messages.ComposterRetrievalMessage;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.coremod.client.gui.WindowTownHall.BLACK;

/**
 * Composter window class. Specifies the extras the composter has for its list.
 */
public class WindowHutComposter extends WindowFilterableList<BuildingComposter.View>
{
    /**
     * Id of the button to toggle replant of saplings
     */
    private static final String BUTTON_TOGGLE_RETRIEVE_DIRT = "retrieveDirt";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "pageItems";

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutComposter(final BuildingComposter.View building)
    {
        super(building, stack -> true, LanguageHandler.format("com.minecolonies.gui.workerHuts.composter.compostables"));
    }

    @Override
    public Collection<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        return IColonyManager.getInstance().getCompatibilityManager().getCopyOfCompostableItems().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final ButtonImage button = new ButtonImage();
        button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
        button.setPosition(50, 193);
        button.setSize(86, 17);
        button.setID(BUTTON_TOGGLE_RETRIEVE_DIRT);
        button.setTextColor(BLACK);
        setupRetrieveDirtButton(button);

        findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class).addChild(button);
        registerButton(BUTTON_TOGGLE_RETRIEVE_DIRT, this::switchReplant);
    }

    /**
     * Setup replant button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupRetrieveDirtButton(final Button button)
    {
        if (((BuildingComposter.View) building).retrieveDirtFromCompostBin)
        {
            button.setLabel(LanguageHandler.format(Blocks.DIRT.getLocalizedName()));
        }
        else
        {
            button.setLabel(LanguageHandler.format(ModItems.compost.getItemStackDisplayName(new ItemStack(ModItems.compost))));
        }
    }

    /**
     * Method to send the message to switch the toggle to the server, then updates button
     */
    private void switchReplant(final Button retrieve)
    {
        final BuildingComposter.View composter = (BuildingComposter.View) building;
        composter.retrieveDirtFromCompostBin = !composter.retrieveDirtFromCompostBin;
        setupRetrieveDirtButton(retrieve);
        MineColonies.getNetwork().sendToServer(new ComposterRetrievalMessage(composter, composter.retrieveDirtFromCompostBin));
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.composter";
    }
}
