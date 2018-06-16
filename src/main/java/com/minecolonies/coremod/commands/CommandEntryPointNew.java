package com.minecolonies.coremod.commands;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.configuration.Configurations;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.ColonyManager;
import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.context.PlayerContext;

/**
 * Command entry point to make minecraft inheritance happy.
 */
public class CommandEntryPointNew extends AbstractCommandParser
{
    public class MineColonyDataProvider
    {
        public List<Colony> getColonies()
        {
            return ColonyManager.getColonies();
        }

        public IColony getIColonyByOwner(final World entityWorld, final EntityPlayer sender)
        {
            return ColonyManager.getIColonyByOwner(entityWorld, sender);
        }

        public Colony getColony(final int colonyNumber)
        {
            return ColonyManager.getColony(colonyNumber);
        }
    }

    public class ForgeCommandsPermissionsChecker implements PermissionsChecker
    {
        public boolean hasPermission(final ForgePermissionNodes forgePermissionNode, final EntityPlayer player)
        {
            return PermissionAPI.hasPermission(player.getGameProfile(), forgePermissionNode.getNodeName(), new PlayerContext(player));
        }

        @Override
        public boolean canUseCommands(@NotNull final MinecraftServer server, @NotNull final ICommandSender sender)
        {
            if (sender instanceof EntityPlayer)
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
