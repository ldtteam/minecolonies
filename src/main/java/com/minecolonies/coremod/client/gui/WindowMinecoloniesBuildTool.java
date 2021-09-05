package com.minecolonies.coremod.client.gui;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.WorldUtil;
import com.minecolonies.api.util.constant.TranslationConstants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.items.ItemSupplyCampDeployer;
import com.minecolonies.coremod.items.ItemSupplyChestDeployer;
import com.minecolonies.coremod.network.messages.server.BuildToolPasteMessage;
import com.minecolonies.coremod.network.messages.server.BuildToolPlaceMessage;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
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
     */
    public WindowMinecoloniesBuildTool(@Nullable final BlockPos pos, final String structureName, final int rotation)
    {
        super(pos, structureName, rotation);
    }

    /**
     * Creates a window build tool. This requires X, Y and Z coordinates. If a structure is active, recalculates the X Y Z with offset. Otherwise the given parameters are used.
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
        final BlockPos offset = Settings.instance.getActiveStructure().getPrimaryBlockOffset();
        final BlockState state = Settings.instance.getActiveStructure().getBlockState(offset).getBlockState();

        BuildToolPlaceMessage msg = new BuildToolPlaceMessage(
          structureName.toString(),
          structureName.getLocalizedName(),
          Settings.instance.getPosition(),
          Settings.instance.getRotation(),
          structureName.isHut(),
          Settings.instance.getMirror(),
          state);

        if (structureName.isHut())
        {
            Network.getNetwork().sendToServer(msg);
        }
        else
        {
            Minecraft.getInstance().tell(new WindowBuildDecoration(msg, Settings.instance.getPosition(), structureName)::open);
        }
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
        final BlockPos offset = Settings.instance.getActiveStructure().getPrimaryBlockOffset();
        ;
        final BlockState state = Settings.instance.getActiveStructure().getBlockState(offset).getBlockState();

        Network.getNetwork().sendToServer(new BuildToolPasteMessage(
          name.toString(),
          name.toString(),
          Settings.instance.getPosition(),
          Settings.instance.getRotation(),
          name.isHut(),
          Settings.instance.getMirror(),
          complete,
          state));
    }

    @Override
    public void checkAndPlace()
    {
        final List<PlacementError> placementErrorList = new ArrayList<>();
        final String schemName = Settings.instance.getStaticSchematicName();

        if (schemName.contains("supplyship") || schemName.contains("nethership"))
        {
            if (ItemSupplyChestDeployer.canShipBePlaced(Minecraft.getInstance().level, Settings.instance.getPosition(),
              Settings.instance.getActiveStructure(),
              placementErrorList,
              Minecraft.getInstance().player))
            {
                super.pasteNice();
            }
            else
            {
                LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "item.supplyChestDeployer.invalid");
            }
        }
        else if (schemName.contains("supplycamp"))
        {
            if (ItemSupplyCampDeployer.canCampBePlaced(Minecraft.getInstance().level, Settings.instance.getPosition(),
              new BlockPos(Settings.instance.getActiveStructure().getSizeX(), Settings.instance.getActiveStructure().getSizeY(), Settings.instance.getActiveStructure().getSizeZ()),
              placementErrorList,
              Minecraft.getInstance().player))
            {
                super.pasteNice();
            }
        }

        if (!placementErrorList.isEmpty())
        {
            final Map<PlacementError.PlacementErrorType, List<BlockPos>> blockPosListByErrorTypeMap = PlacementError.partitionPlacementErrorsByErrorType(
              placementErrorList);
            for (final Map.Entry<PlacementError.PlacementErrorType, List<BlockPos>> entry : blockPosListByErrorTypeMap.entrySet())
            {
                final PlacementError.PlacementErrorType placementErrorType = entry.getKey();
                final List<BlockPos> blockPosList = entry.getValue();

                final int numberOfBlocksToReport = blockPosList.size() > 5 ? 5 : blockPosList.size();
                final List<BlockPos> blocksToReportList = blockPosList.subList(0, numberOfBlocksToReport);
                String outputList = PlacementError.blockListToCommaSeparatedString(blocksToReportList);
                if (blockPosList.size() > numberOfBlocksToReport)
                {
                    outputList += "...";
                }
                final String errorMessage;
                switch (placementErrorType)
                {
                    case NOT_WATER:
                        final String dim = WorldUtil.isNetherType(Minecraft.getInstance().level)
                                             ? TranslationConstants.SUPPLY_CAMP_INVALID_NOT_LAVA_MESSAGE_KEY
                                             : TranslationConstants.SUPPLY_CAMP_INVALID_NOT_WATER_MESSAGE_KEY;
                        errorMessage = String.format(dim, outputList);
                        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, errorMessage, outputList);
                        break;
                    case NOT_SOLID:
                        errorMessage = LanguageHandler.format(TranslationConstants.SUPPLY_CAMP_INVALID_NOT_SOLID_MESSAGE_KEY, outputList);
                        LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, errorMessage, outputList);
                        break;
                    case NEEDS_AIR_ABOVE:
                        errorMessage = LanguageHandler.format(TranslationConstants.SUPPLY_CAMP_INVALID_NEEDS_AIR_ABOVE_MESSAGE_KEY, outputList);
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

        if (!Screen.hasShiftDown())
        {
            super.cancelClicked();
        }
    }

    @Override
    public boolean hasMatchingBlock(@NotNull final PlayerInventory inventory, final String hut)
    {
        return InventoryUtils.hasItemInProvider(inventory.player,
          item -> item.getItem() instanceof BlockItem && ((BlockItem) item.getItem()).getBlock() instanceof AbstractBlockHut && ((BlockItem) item.getItem()).getBlock()
                                                                                                                                  .getRegistryName()
                                                                                                                                  .getPath()
                                                                                                                                  .equalsIgnoreCase("blockhut" + hut));
    }
}
