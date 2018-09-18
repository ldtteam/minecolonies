package com.minecolonies.coremod.client.gui;

import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.items.ItemSupplyCampDeployer;
import com.minecolonies.coremod.items.ItemSupplyChestDeployer;
import com.structurize.api.util.BlockUtils;
import com.structurize.api.util.LanguageHandler;
import com.structurize.coremod.client.gui.WindowBuildTool;
import com.structurize.coremod.placementhandlers.PlacementError;
import com.structurize.structures.helpers.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BuildTool window.
 */
public class WindowMinecoloniesBuildTool extends com.structurize.coremod.client.gui.WindowBuildTool
{
    /**
     * Creates a window build tool for a specific structure.
     * @param pos the position.
     * @param structureName the structure name.
     * @param rotation the rotation.
     * @param mode the mode.
     */
    public WindowMinecoloniesBuildTool(@Nullable final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode)
    {
        super(pos, structureName, rotation, mode);
    }

    /**
     * Creates a window build tool.
     * This requires X, Y and Z coordinates.
     * If a structure is active, recalculates the X Y Z with offset.
     * Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     */
    public WindowMinecoloniesBuildTool(@Nullable final BlockPos pos)
    {
        super(pos);
    }

    private void checkAndPlace()
    {
        if (com.structurize.coremod.client.gui.WindowBuildTool.FreeMode.SUPPLYSHIP == Settings.instance.getFreeMode())
        {
            if (ItemSupplyChestDeployer.canShipBePlaced(Minecraft.getMinecraft().world, Settings.instance.getPosition(),
              Settings.instance.getActiveStructure().getSize(BlockUtils.getRotation(Settings.instance.getRotation()))))
            {
                super.pasteNice();
            }
            else
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, "item.supplyChestDeployer.invalid");
            }
        }
        else if (com.structurize.coremod.client.gui.WindowBuildTool.FreeMode.SUPPLYCAMP == Settings.instance.getFreeMode())
        {
            final List<PlacementError> placementErrorList = new ArrayList<>();
            if (ItemSupplyCampDeployer.canCampBePlaced(Minecraft.getMinecraft().world, Settings.instance.getPosition(),
              Settings.instance.getActiveStructure().getSize(BlockUtils.getRotation(Settings.instance.getRotation())), placementErrorList))
            {
                super.pasteNice();
            }
            else
            {
                final Map<PlacementError.PlacementErrorType, List<BlockPos>> blockPosListByErrorTypeMap = PlacementError.partitionPlacementErrorsByErrorType(
                  placementErrorList);
                for (final Map.Entry<PlacementError.PlacementErrorType, List<BlockPos>> entry : blockPosListByErrorTypeMap.entrySet())
                {
                    final PlacementError.PlacementErrorType placementErrorType = entry.getKey();
                    final List<BlockPos> blockPosList = entry.getValue();

                    final int numberOfBlocksTOReport = blockPosList.size() > 5 ? 5 : blockPosList.size();
                    final List<BlockPos> blocksToReportList = blockPosList.subList(0, numberOfBlocksTOReport);
                    String outputList = PlacementError.blockListToCommaSeparatedString(blocksToReportList);
                    if (blockPosList.size() > numberOfBlocksTOReport)
                    {
                        outputList += "...";
                    }
                    final String errorMessage;
                    switch(placementErrorType)
                    {
                        case NOT_SOLID:
                            errorMessage = String.format(TranslationConstants.SUPPLY_CAMP_INVALID_NOT_SOLID_MESSAGE_KEY, outputList);
                            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, errorMessage, outputList);
                            break;
                        case NEEDS_AIR_ABOVE:
                            errorMessage = String.format(TranslationConstants.SUPPLY_CAMP_INVALID_NEEDS_AIR_ABOVE_MESSAGE_KEY, outputList);
                            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, errorMessage, outputList);
                            break;
                        case INSIDE_COLONY:
                            errorMessage = TranslationConstants.SUPPLY_CAMP_INVALID_INSIDE_COLONY_MESSAGE_KEY;
                            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, errorMessage);
                            break;
                        default:
                            errorMessage = TranslationConstants.SUPPLY_CAMP_INVALID;
                            LanguageHandler.sendPlayerMessage(Minecraft.getMinecraft().player, errorMessage);
                            break;
                    }
                }
            }
        }

        if(!GuiScreen.isShiftKeyDown())
        {
            super.cancelClicked();
        }
    }

}
