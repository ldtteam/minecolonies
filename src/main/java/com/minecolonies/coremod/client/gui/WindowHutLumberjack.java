package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.controls.ButtonImage;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.MineColonies;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingLumberjack;
import com.minecolonies.coremod.network.messages.LumberjackScepterMessage;
import com.minecolonies.coremod.network.messages.LumberjackReplantSaplingToggleMessage;
import com.minecolonies.coremod.network.messages.LumberjackRestrictionToggleMessage;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.api.util.constant.TranslationConstants.*;
import static com.minecolonies.coremod.client.gui.WindowTownHall.BLACK;

/**
 * Window for the lumberjack hut.
 */
public class WindowHutLumberjack extends WindowFilterableList<BuildingLumberjack.View>
{
    /**
     * Id of the button to toggle replant of saplings
     */
    private static final String BUTTON_TOGGLE_REPLANT = "saplingReplant";

    /**
     * Id of the button to give tool
     */
    private static final String BUTTON_GIVE_TOOL = "giveTool";

    /**
     * Id of the button to toggle restrict
     */
    private static final String BUTTON_TOGGLE_RESTRICTION = "toggleRestriction";

    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "pageItems";

    /**
     * The building of the lumberjack (Client side representation).
     */
    private final BuildingLumberjack.View ownBuilding;

    /**
     * Constructor for the window of the lumberjack.
     *
     * @param building {@link BuildingLumberjack.View}.
     */
    public WindowHutLumberjack(final BuildingLumberjack.View building)
    {
        super(building, stack -> true, LanguageHandler.format("com.minecolonies.coremod.gui.workerHuts.saplingList"));
        this.ownBuilding = building;
    }

    private void giveTool()
    {
        givePlayerScepter();
    }

    /**
     * Send message to player to add scepter to his inventory.
     *
     */
    private void givePlayerScepter()
    {
        MineColonies.getNetwork().sendToServer(new LumberjackScepterMessage(building.getID(), building.getColony().getID()));
    }

    @Override
    public Collection<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate)
    {
        return ColonyManager.getCompatibilityManager().getCopyOfSaplings().stream().filter(storage -> filterPredicate.test(storage.getItemStack())).collect(Collectors.toList());
    }

    @Override
    public void onOpened()
    {
        super.onOpened();
        final ButtonImage button = new ButtonImage();
        button.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
        button.setPosition(50, 193);
        button.setSize(86, 17);
        button.setID(BUTTON_TOGGLE_REPLANT);
        button.setTextColor(BLACK);
        setupReplantButton(button);

        findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class).addChild(button);
        registerButton(BUTTON_TOGGLE_REPLANT, this::switchReplant);

        final ButtonImage toggleRestrictionButton = new ButtonImage();
        toggleRestrictionButton.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
        toggleRestrictionButton.setPosition(20, 210);
        toggleRestrictionButton.setSize(46, 17);
        toggleRestrictionButton.setID(BUTTON_TOGGLE_RESTRICTION);
        toggleRestrictionButton.setTextColor(BLACK);
        setupRestrictionButton(toggleRestrictionButton);

        findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class).addChild(toggleRestrictionButton);
        registerButton(BUTTON_TOGGLE_RESTRICTION, this::toggleRestriction);

        final ButtonImage giveToolButton = new ButtonImage();
        giveToolButton.setImage(new ResourceLocation(Constants.MOD_ID, "textures/gui/builderhut/builder_button_medium.png"));
        giveToolButton.setPosition(100, 210);
        giveToolButton.setSize(46, 17);
        giveToolButton.setID(BUTTON_GIVE_TOOL);
        giveToolButton.setTextColor(BLACK);
        setupGiveToolButton(giveToolButton);

        findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class).addChild(giveToolButton);
        registerButton(BUTTON_GIVE_TOOL, this::giveTool);

        this.isInverted = true;
    }

    /**
     * Setup replant button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupReplantButton(final Button button)
    {
        if (ownBuilding.shouldReplant)
        {
            button.setLabel(LanguageHandler.format(TOGGLE_REPLANT_SAPLINGS_ON));
        }
        else
        {
            button.setLabel(LanguageHandler.format(TOGGLE_REPLANT_SAPLINGS_OFF));
        }
    }

    /**
     * Setup giveTool button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupGiveToolButton(final Button button)
    {
        // TODO: Use localisation when this has proper UI
        button.setLabel(LanguageHandler.format("Give tool"));
    }

    /**
     * Setup toggleRestriction button with correct string.
     *
     * @param button the button to setup.
     */
    private void setupRestrictionButton(final Button button)
    {
        // TODO: Use localisation when this has proper UI
        button.setLabel(LanguageHandler.format( ownBuilding.shouldRestrict ? "Unrestrict" : "Restrict" ));
    }

    /**
     * Method to send the message to switch the toggle to the server, then updates button
     */
    private void switchReplant(final Button replant)
    {
        ownBuilding.shouldReplant = !ownBuilding.shouldReplant;
        setupReplantButton(replant);
        MineColonies.getNetwork().sendToServer(new LumberjackReplantSaplingToggleMessage(ownBuilding, ownBuilding.shouldReplant));
    }

    /**
     * Method to send the message to switch the toggle to the server, then updates button
     */
    private void toggleRestriction(final Button restriction)
    {
        ownBuilding.shouldRestrict = !ownBuilding.shouldRestrict;
        setupRestrictionButton(restriction);
        MineColonies.getNetwork().sendToServer(new LumberjackRestrictionToggleMessage(ownBuilding, ownBuilding.shouldRestrict));
    }

    /**
     * Returns the name of a building.
     *
     * @return Name of a building.
     */
    @NotNull
    @Override
    public String getBuildingName()
    {
        return COM_MINECOLONIES_COREMOD_GUI_LUMBERJACK;
    }
}

