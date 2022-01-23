package com.minecolonies.coremod.client.gui;

import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.helpers.Settings;
import com.ldtteam.structurize.management.StructureName;
import com.ldtteam.structurize.placement.handlers.placement.PlacementError;
import com.ldtteam.structurize.util.LanguageHandler;
import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.colony.buildings.registry.IBuildingRegistry;
import com.minecolonies.api.util.InventoryUtils;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.Network;
import com.minecolonies.coremod.client.render.worldevent.HighlightManager;
import com.minecolonies.coremod.items.ItemSupplyCampDeployer;
import com.minecolonies.coremod.items.ItemSupplyChestDeployer;
import com.minecolonies.coremod.network.messages.server.BuildToolPasteMessage;
import com.minecolonies.coremod.network.messages.server.BuildToolPlaceMessage;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * BuildTool window.
 */
public class WindowMinecoloniesBuildTool extends WindowBuildTool
{
    /**
     * The displayed boxes category
     */
    private static final String RENDER_BOX_CATEGORY = "placement";

    /**
     * Creates a window build tool for a specific structure.
     *
     * @param pos           the position.
     * @param structureName the structure name.
     * @param rotation      the rotation.
     * @param groundstyle   one of the GROUNDSTYLE_ values.
     */
    public WindowMinecoloniesBuildTool(@Nullable final BlockPos pos, final String structureName, final int rotation, final int groundstyle)
    {
        super(pos, structureName, rotation, groundstyle);
    }

    /**
     * Creates a window build tool. This requires X, Y and Z coordinates. If a structure is active, recalculates the X Y Z with offset. Otherwise the given parameters are used.
     *
     * @param pos coordinate.
     * @param groundstyle one of the GROUNDSTYLE_ values.
     */
    public WindowMinecoloniesBuildTool(@Nullable final BlockPos pos, final int groundstyle)
    {
        super(pos, groundstyle);
    }

    @Override
    public void place(final StructureName structureName)
    {
        final BlockPos offset = Settings.instance.getActiveStructure().getPrimaryBlockOffset();
        final BlockState state = Settings.instance.getActiveStructure().getBlockState(offset);

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
        final BlockState state = Settings.instance.getActiveStructure().getBlockState(offset);

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
              placementErrorList,
              Minecraft.getInstance().player))
            {
                super.pasteNice();
            }
        }

        HighlightManager.clearCategory(RENDER_BOX_CATEGORY);
        if (!placementErrorList.isEmpty())
        {
            LanguageHandler.sendPlayerMessage(Minecraft.getInstance().player, "item.supply.badblocks");

            for (final PlacementError error : placementErrorList)
            {
                HighlightManager.addRenderBox(RENDER_BOX_CATEGORY, new HighlightManager.TimedBoxRenderData()
                                                                     .setPos(error.getPos())
                                                                     .setRemovalTimePoint(Minecraft.getInstance().level.getGameTime() + 120 * 20 * 60)
                                                                     .addText(LanguageHandler.translateKey("item.supply.error." + error.getType().toString().toLowerCase()))
                                                                     .setColor(0xffFF0000));
            }
        }

        if (!Screen.hasShiftDown())
        {
            super.cancelClicked();
        }
    }

    @Override
    public void cancelClicked()
    {
        super.cancelClicked();
        HighlightManager.clearCategory(RENDER_BOX_CATEGORY);
    }

    @Override
    public boolean hasMatchingBlock(@NotNull final Inventory inventory, final String hut)
    {
        //todo 1.19 remove this
        final String name = hut.equals("citizen") ? "home" : hut;
        return InventoryUtils.hasItemInProvider(inventory.player,
          item -> item.getItem() instanceof BlockItem
                    && StructureName.HUTS.contains(hut)
                    && ((BlockItem) item.getItem()).getBlock() == IBuildingRegistry.getInstance().getValue(new ResourceLocation(Constants.MOD_ID, name)).getBuildingBlock());
    }
}
