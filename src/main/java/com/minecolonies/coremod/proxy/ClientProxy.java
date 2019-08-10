package com.minecolonies.coremod.proxy;

import com.ldtteam.structures.helpers.Settings;
import com.ldtteam.structurize.client.gui.WindowBuildTool;
import com.ldtteam.structurize.management.Structures;
import com.minecolonies.api.colony.ICitizenDataView;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.Log;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.client.gui.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipeBook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Client side proxy.
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientProxy extends CommonProxy
{
    /**
     * Inventory description string.
     */
    private static final String INVENTORY = "inventory";

    @Override
    public boolean isClient()
    {
        return true;
    }


    @Override
    public void showCitizenWindow(final ICitizenDataView citizen)
    {
        @NotNull final WindowCitizen window = new WindowCitizen(citizen);
        window.open();
    }

    @Override
    public void openBuildToolWindow(@Nullable final BlockPos pos)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowMinecoloniesBuildTool window = new WindowMinecoloniesBuildTool(pos);
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
    public World getWorld(final int dimension)
    {
        return Minecraft.getInstance().world;
    }

    @Override
    public void openSuggestionWindow(@NotNull final BlockPos pos, @NotNull final BlockState state, @NotNull final ItemStack stack)
    {
        new WindowSuggestBuildTool(pos, state, stack).open();
    }

    @Override
    public void openBuildToolWindow(final BlockPos pos, final String structureName, final int rotation, final WindowBuildTool.FreeMode mode)
    {
        if (pos == null && Settings.instance.getActiveStructure() == null)
        {
            return;
        }

        @Nullable final WindowMinecoloniesBuildTool window = new WindowMinecoloniesBuildTool(pos, structureName, rotation, mode);
        window.open();
    }

    @Override
    public void openClipBoardWindow(final int colonyId)
    {
        @Nullable final WindowClipBoard window = new WindowClipBoard(colonyId);
        window.open();
    }

    @Override
    public void openResourceScrollWindow(final int colonyId, final BlockPos buildingPos)
    {
        @Nullable final WindowResourceList window = new WindowResourceList(colonyId, buildingPos);
        window.open();
    }

    @Override
    public File getSchematicsFolder()
    {
        if (ServerLifecycleHooks.getCurrentServer() == null)
        {
            if (IColonyManager.getInstance().getServerUUID() != null)
            {
                return new File(Minecraft.getInstance().gameDir, Constants.MOD_ID + "/" + IColonyManager.getInstance().getServerUUID());
            }
            else
            {
                Log.getLogger().error("ColonyManager.getServerUUID() => null this should not happen");
                return null;
            }
        }

        // if the world schematics folder exists we use it
        // otherwise we use the minecraft folder  /minecolonies/schematics if on the physical client on the logical server
        final File worldSchematicFolder = new File(ServerLifecycleHooks.getCurrentServer().getDataDirectory()
                                                     + "/" + Constants.MOD_ID + '/' + Structures.SCHEMATICS_PREFIX);

        if (!worldSchematicFolder.exists())
        {
            return new File(Minecraft.getInstance().gameDir, Constants.MOD_ID);
        }

        return worldSchematicFolder.getParentFile();
    }

    @NotNull
    @Override
    public RecipeBook getRecipeBookFromPlayer(@NotNull final PlayerEntity player)
    {
        if (player instanceof ClientPlayerEntity)
        {
            return ((ClientPlayerEntity) player).getRecipeBook();
        }

        return super.getRecipeBookFromPlayer(player);
    }
}
