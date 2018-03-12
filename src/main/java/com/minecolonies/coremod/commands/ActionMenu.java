package com.minecolonies.coremod.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;
import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;

import net.minecraft.entity.player.EntityPlayerMP;

public class ActionMenu extends AbstractMenu
{
    @NotNull private final String description;
    @NotNull private final ForgePermissionNodes forgePermissionNode;
    @NotNull private final List<ActionArgument> actionArgumentList;
    @NotNull private final Class<? extends IActionCommand> actionCommandClass;

    public ActionMenu(
            @NotNull final String description,
            @NotNull final String menuItemName,
            @NotNull final ForgePermissionNodes forgePermissionNode,
            @NotNull final Class<? extends IActionCommand> actionCommandClass,
            @Nullable final ActionArgument ... actionArguments
            )
    {
        super(menuItemName);
        this.description = description;
        this.forgePermissionNode = forgePermissionNode;
        this.actionCommandClass = actionCommandClass;
        this.actionArgumentList = Arrays.asList(actionArguments);
    }

    public List<ActionArgument> getActionArgumentList()
    {
        return new ArrayList<>(actionArgumentList);
    }

    @Override
    public ActionMenuType getMenuType()
    {
        return (ActionMenuType) menuType;
    }

    public String getDescription()
    {
        return description;
    }

    public ForgePermissionNodes getForgePermissionNode()
    {
        return forgePermissionNode;
    }

    public Class<? extends IActionCommand> getActionCommandClass()
    {
        return actionCommandClass;
    }

    public Colony getColonyForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.COLONY == actionArgument.getType())
                {
                    return (Colony) actionArgument.getValue();
                }
            }
        }
        return null;
    }

    public EntityPlayerMP getPlayerForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.PLAYER == actionArgument.getType())
                {
                    return (EntityPlayerMP) actionArgument.getValue();
                }
            }
        }
        return null;
    }

    public CitizenData getCitizenForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.CITIZEN == actionArgument.getType())
                {
                    return (CitizenData) actionArgument.getValue();
                }
            }
        }
        return null;
    }

    public Boolean getBooleanForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.BOOLEAN == actionArgument.getType())
                {
                    return (Boolean) actionArgument.getValue();
                }
            }
        }
        return null;
    }

    public boolean getBooleanValueForArgument(@NotNull final String argumentName, final boolean defaultValue)
    {
        final Boolean booleanObject = getBooleanForArgument(argumentName);
        if (null != booleanObject)
        {
            return booleanObject.booleanValue();
        }
        else
        {
            return defaultValue;
        }
    }

    public Integer getIntegerForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : actionArgumentList)
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                switch (actionArgument.getType())
                {
                    case INTEGER:
                    case COORDINATE_X:
                    case COORDINATE_Y:
                    case COORDINATE_Z:
                        return (Integer) actionArgument.getValue();
                    default:
                        break;
                }
            }
        }
        return null;
    }

    public int getIntValueForArgument(@NotNull final String argumentName, final int defaultValue)
    {
        final Integer integerObject = getIntegerForArgument(argumentName);
        if (null != integerObject)
        {
            return integerObject.intValue();
        }
        else
        {
            return defaultValue;
        }
    }
}
