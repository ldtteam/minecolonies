package com.minecolonies.core.client.gui;

import com.ldtteam.blockui.controls.Text;
import com.minecolonies.api.colony.buildings.ModBuildings;
import com.minecolonies.api.colony.buildings.registry.BuildingEntry;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.MineColonies;
import com.minecolonies.core.Network;
import com.minecolonies.core.network.messages.server.ReactivateBuildingMessage;
import com.minecolonies.core.tileentities.TileEntityColonyBuilding;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.WindowConstants.*;

/**
 * Window to reactivate a building.
 */
public class WindowReactivateBuilding extends AbstractWindowSkeleton
{
    /*
     * Building the worker is trying to place.
     */
    @NotNull
    private final BlockPos pos;

    /**
     * Creates a new instance of this window.
     * @param pos the position of the building.
     */
    public WindowReactivateBuilding(@NotNull final BlockPos pos)
    {
        super(Constants.MOD_ID + REACTIVATE_BUILDING_SOURCE_SUFFIX);
        this.pos = pos;
        registerButton(BUTTON_REACTIVATE, this::reactivateClicked);
        registerButton(BUTTON_CANCEL, this::cancelClicked);


        if (Minecraft.getInstance().level.getBlockEntity(pos) instanceof TileEntityColonyBuilding tileEntityColonyBuilding)
        {
            final BuildingEntry buildingEntry = IBuildingRegistry.getInstance().getValue(tileEntityColonyBuilding.registryName);
            if (buildingEntry == ModBuildings.home.get() || buildingEntry == ModBuildings.tavern.get())
            {
                findPaneOfTypeByID("text", Text.class).setText(Component.translatable("com.minecolonies.core.gui.reactivate.message.living", Component.translatable(buildingEntry.getTranslationKey())));
            }
            else if (buildingEntry != null)
            {
                findPaneOfTypeByID("text", Text.class).setText(Component.translatable("com.minecolonies.core.gui.reactivate.message.working", Component.translatable(buildingEntry.getTranslationKey())));
            }
        }
    }

    /**
     * Reactivate the building.
     */
    private void reactivateClicked()
    {
        Network.getNetwork().sendToServer(new ReactivateBuildingMessage(pos));
        close();
    }


    /**
     * Cancel reactivation.
     */
    private void cancelClicked()
    {
        close();
    }
}
