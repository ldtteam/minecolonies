package com.minecolonies.core.proxy;

import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyView;
import com.minecolonies.apiimp.ClientMinecoloniesAPIImpl;
import com.minecolonies.core.client.gui.*;
import com.minecolonies.core.colony.buildings.workerbuildings.BuildingBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    public ClientProxy()
    {
        apiImpl = new ClientMinecoloniesAPIImpl();
    }

    @Override
    public boolean isClient()
    {
        return true;
    }

    @Override
    public void showCitizenWindow(final ICitizenDataView citizen)
    {
        @NotNull final WindowInteraction window = new WindowInteraction(citizen);
        window.open();
    }

    @Override
    public void openDecorationControllerWindow(@Nullable final BlockPos pos)
    {
        if (pos == null)
        {
            return;
        }

        @Nullable final WindowDecorationController window = new WindowDecorationController(pos);
        window.open();
    }

    @Override
    public Level getWorld(final ResourceKey<Level> dimension)
    {
        return Minecraft.getInstance().level;
    }

    @Override
    public void openSuggestionWindow(@NotNull final BlockPos pos, @NotNull final BlockState state, @NotNull final ItemStack stack)
    {
        new WindowSuggestBuildTool(pos, state, stack).open();
    }

    @Override
    public void openBannerRallyGuardsWindow(final ItemStack banner)
    {
        @Nullable final WindowBannerRallyGuards window = new WindowBannerRallyGuards(banner);
        window.open();
    }

    @Override
    public void openClipboardWindow(final IColonyView colonyView)
    {
        @Nullable final WindowClipBoard window = new WindowClipBoard(colonyView);
        window.open();
    }

    @Override
    public void openResourceScrollWindow(@NotNull final BuildingBuilder.View buildingView, @NotNull final Map<String, Integer> warehouseSnapshot)
    {
        @Nullable final WindowResourceList window = new WindowResourceList(buildingView, warehouseSnapshot);
        window.open();
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final Player player)
    {
        if (player instanceof LocalPlayer localPlayer)
        {
            return localPlayer.getRecipeBook();
        }

        return super.getRecipeBookFromPlayer(player);
    }
}
