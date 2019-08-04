package com.minecolonies.coremod.commands;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Command entry point to make minecraft inheritance happy.
 */
public class CommandEntryPointNew extends AbstractCommandParser
{
    public class MineColonyDataProvider
    {
        public List<IColony> getColonies()
        {
            return IColonyManager.getInstance().getAllColonies();
        }

        public IColony getIColonyByOwner(final World entityWorld, final PlayerEntity sender)
        {
            return IColonyManager.getInstance().getIColonyByOwner(entityWorld, sender);
        }

        public IColony getColony(final int colonyNumber, final int senderDimension)
        {
            return IColonyManager.getInstance().getColonyByWorld(colonyNumber, FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(senderDimension));
        }
    }

    public class ForgeCommandsPermissionsChecker implements PermissionsChecker
    {
        public boolean hasPermission(final ForgePermissionNodes forgePermissionNode, final PlayerEntity player)
        {
            return PermissionAPI.hasPermission(player.getGameProfile(), forgePermissionNode.getNodeName(), new PlayerContext(player));
        }

        @Override
        public boolean canUseCommands(@NotNull final MinecraftServer server, @NotNull final CommandSource sender)
        {
            if (sender instanceof PlayerEntity)
            {
                return AbstractSingleCommand.isPlayerOpped(sender) || Configurations.gameplay.opLevelForServer <= 0;
            }
            return true;
        }
    }

    protected ForgeCommandsPermissionsChecker getPermissionsChecker()
    {
        return new ForgeCommandsPermissionsChecker();
    }

    protected NavigationMenuType getRootNavigationMenuType()
    {
        return NavigationMenuType.MINECOLONIES;
    }
    
    protected ModuleContext getModuleContext()
    {
        return new ModuleContext()
        {
            @Override
            public <T> T get(final Class<? extends T> type)
            {
                if (MineColonyDataProvider.class == type)
                {
                    return type.cast(new MineColonyDataProvider());
                }
                
                return null;
            }
        };
    }
}
