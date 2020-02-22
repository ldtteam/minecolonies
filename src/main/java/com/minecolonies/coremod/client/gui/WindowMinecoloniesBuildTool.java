package com.minecolonies.coremod.client.gui;

import com.ldtteam.structures.lib.BlueprintUtils;
import com.ldtteam.structurize.util.BlockUtils;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.items.ItemSupplyCampDeployer;
import com.minecolonies.coremod.items.ItemSupplyChestDeployer;
import com.minecolonies.coremod.network.messages.BuildToolPasteMessage;
import com.minecolonies.coremod.network.messages.BuildToolPlaceMessage;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.placementhandlers.PlacementError;
import com.ldtteam.structures.helpers.Settings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * BuildTool window.
 */
public class WindowMinecoloniesBuildTool extends WindowBuildTool
{
    /**
     * Creates a window build tool for a specific structure.
     *
     * @param pos           the position.
     * @param structureName the structure name.
     * @param rotation      the rotation.
     * @param mode          the mode.
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

    @Override
    public void place(final StructureName structureName)
    {
        final BlockPos offset = BlueprintUtils.getPrimaryBlockOffset(Settings.instance.getActiveStructure().getBluePrint()).getA();;
        final BlockState state  = Settings.instance.getActiveStructure().getBlockState(offset).getBlockState();
        Network.getNetwork().sendToServer(new BuildToolPlaceMessage(
          structureName.toString(),
          structureName.toString(),
          Settings.instance.getPosition(),
          Settings.instance.getRotation(),
          structureName.isHut(),
          Settings.instance.getMirror(),
          state));
    }

    @Override
    public boolean hasPermission()
    {
        return true;
    }

    @Override
    public boolean pasteDirectly()
    {
        return false;
    }

    @Override
    public void paste(final StructureName name, final boolean complete)
    {
        final BlockPos offset = BlueprintUtils.getPrimaryBlockOffset(Settings.instance.getActiveStructure().getBluePrint()).getA();;
        final BlockState state  = Settings.instance.getActiveStructure().getBlockState(offset).getBlockState();
        if ( name.isHut() || Settings.instance.getFreeMode() != null )
        {
            Network.getNetwork().sendToServer(new BuildToolPasteMessage(
              name.toString(),
              name.toString(),
              Settings.instance.getPosition(),
              Settings.instance.getRotation(),
              name.isHut(),
              Settings.instance.getMirror(),
              complete, Settings.instance.getFreeMode(),
              state));
        }
        else
        {
            super.paste(name, complete);
        }
    }

    @Override
    public void checkAndPlace()
    {
        if (WindowBuildTool.FreeMode.SUPPLYSHIP == Settings.instance.getFreeMode())
        {
            if (ItemSupplyChestDeployer.canShipBePlaced(Minecraft.getInstance().world, Settings.instance.getPosition(),
              Settings.instance.getActiveStructure().getSize(BlockUtils.getRotation(Settings.instance.getRotation()), Settings.instance.getMirror())))
            {
                super.pasteNice();
            }
            else
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "item.supplyChestDeployer.invalid");
            }
        }
        else if (WindowBuildTool.FreeMode.SUPPLYCAMP == Settings.instance.getFreeMode())
        {
            final List<PlacementError> placementErrorList = new ArrayList<>();
            if (ItemSupplyCampDeployer.canCampBePlaced(Minecraft.getInstance().world, Settings.instance.getPosition(),
              Settings.instance.getActiveStructure().getSize(BlockUtils.getRotation(Settings.instance.getRotation()), Settings.instance.getMirror()), placementErrorList))
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
                    switch (placementErrorType)
                    {
                        case NOT_SOLID:
                            errorMessage = String.format(TranslationConstants.SUPPLY_CAMP_INVALID_NOT_SOLID_MESSAGE_KEY, outputList);
                            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, errorMessage, outputList);
                            break;
                        case NEEDS_AIR_ABOVE:
                            errorMessage = String.format(TranslationConstants.SUPPLY_CAMP_INVALID_NEEDS_AIR_ABOVE_MESSAGE_KEY, outputList);
                            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, errorMessage, outputList);
                            break;
                        case INSIDE_COLONY:
                            errorMessage = TranslationConstants.SUPPLY_CAMP_INVALID_INSIDE_COLONY_MESSAGE_KEY;
                            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, errorMessage);
                            break;
                        default:
                            errorMessage = TranslationConstants.SUPPLY_CAMP_INVALID;
                            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, errorMessage);
                            break;
                    }
                }
            }
        }

        if (!Screen.hasShiftDown())
        {
            super.cancelClicked();
        }
    }
}
