package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.api.util.LanguageHandler;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.blockout.controls.Button;
import com.minecolonies.blockout.views.View;
import com.minecolonies.coremod.colony.buildings.workerbuildings.BuildingFlorist;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.minecolonies.coremod.client.gui.ViewFilterableList.*;

/**
 * Florist window class. Specifies the extras the florist has for its list.
 */
public class WindowHutFlorist extends AbstractHutFilterableLists
{
    /**
     * View containing the list.
     */
    private static final String PAGE_ITEMS_VIEW = "flowers";

    /**
     * The resource string.
     */
    private static final String RESOURCE_STRING = ":gui/windowhutflorist.xml";

    /**
     * The building of the florist (Client side representation).
     */
    private final BuildingFlorist.View ownBuilding;

    /**
     * Constructor for the window of the worker building.
     *
     * @param building class extending
     */
    public WindowHutFlorist(final BuildingFlorist.View building)
    {
        super(building, Constants.MOD_ID + RESOURCE_STRING);

        final ViewFilterableList window = new ViewFilterableList(findPaneOfTypeByID(PAGE_ITEMS_VIEW, View.class),
          this,
          building,
          LanguageHandler.format("com.minecolonies.gui.workerHuts.florist.flowers"),
          PAGE_ITEMS_VIEW,
          true);
        views.put(PAGE_ITEMS_VIEW, window);
        this.ownBuilding = building;
    }

    @Override
    public void onButtonClicked(@NotNull final Button button)
    {
        if (Objects.equals(button.getID(), BUTTON_SWITCH))
        {
            if (ownBuilding.getBuildingLevel() <= 3)
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.gui.workerhuts.florist.toolow");
                return;
            }

            if (ownBuilding.getBuildingLevel() < 5 && button.getLabel().equals(ON) && building.getSize("flowers") >= 5)
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "com.minecolonies.gui.workerhuts.florist.toomany");
                return;
            }
        }

        super.onButtonClicked(button);
    }

    @Override
    public List<? extends ItemStorage> getBlockList(final Predicate<ItemStack> filterPredicate, final String id)
    {
        switch (ownBuilding.getBuildingLevel())
        {
            case 0:
            case 1:
                return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables().stream().filter(storage -> storage.getDamageValue() == 0).filter(itemStorage -> itemStorage.getItem().getRegistryName().getPath().contains("flower")).collect(Collectors.toList());
            case 2:
                return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables().stream().filter(itemStorage -> itemStorage.getItem().getRegistryName().getPath().contains("flower")).collect(Collectors.toList());
            case 3:
            case 4:
            case 5:
            default:
                return IColonyManager.getInstance().getCompatibilityManager().getCopyOfPlantables();
        }
    }

    @Override
    public String getBuildingName()
    {
        return "com.minecolonies.coremod.gui.workerHuts.florist";
    }
}
