package com.minecolonies.coremod.commands;

import static com.minecolonies.coremod.commands.ActionArgumentType.STRING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.CitizenData;
import com.minecolonies.coremod.colony.Colony;

import net.minecraft.entity.player.EntityPlayerMP;

public class ActionMenuState
{
    private static class ArgumentState
    {
        private boolean valueIsSet = false;
        @Nullable private Object value;
    }

    @NotNull private final ActionMenu actionMenu;
    @NotNull private final Map<String, ArgumentState> argumentStateByActionArgumentNameMap = new HashMap<>();

    public ActionMenuState(@NotNull final ActionMenu actionMenu)
    {
        super();
        this.actionMenu = actionMenu;
    }

    public ActionMenu getActionMenu()
    {
        return actionMenu;
    }

    public boolean isValueSet(@NotNull final ActionArgument actionArgument)
    {
        final ArgumentState argumentState = argumentStateByActionArgumentNameMap.get(actionArgument.getName());
        if (null == argumentState)
        {
            return false;
        }
        return argumentState.valueIsSet;
    }

    public Object getValue(@NotNull final ActionArgument actionArgument)
    {
        final ArgumentState argumentState = argumentStateByActionArgumentNameMap.get(actionArgument.getName());
        if (null == argumentState)
        {
            return null;
        }
        return argumentState.value;
    }

    public void setValue(@NotNull final ActionArgument actionArgument, @NotNull final Object newValue)
    {
        ArgumentState argumentState = argumentStateByActionArgumentNameMap.get(actionArgument.getName());
        if (null == argumentState)
        {
            argumentState = new ArgumentState();
            argumentStateByActionArgumentNameMap.put(actionArgument.getName(), argumentState);
        }
        argumentState.value = newValue;
        argumentState.valueIsSet = true;
    }

    private List<ActionArgument> getAllArgumentsList()
    {
        return getAllArgumentsList(actionMenu.getActionArgumentList());
    }

    private List<ActionArgument> getAllArgumentsList(@NotNull final List<ActionArgument> currentActionArgumentList)
    {
        final List<ActionArgument> allActionArgumentsList = new ArrayList<>();
        for (final ActionArgument actionArgument : currentActionArgumentList)
        {
            allActionArgumentsList.add(actionArgument);
            if (isValueSet(actionArgument))
            {
                allActionArgumentsList.addAll(getAllArgumentsList(actionArgument.getActionArgumentList()));
            }
        }
        return allActionArgumentsList;
    }

    public Colony getColonyForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.COLONY == actionArgument.getType())
                {
                    return (Colony) getValue(actionArgument);
                }
            }
        }
        return null;
    }

    public EntityPlayerMP getPlayerForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.PLAYER == actionArgument.getType())
                {
                    return (EntityPlayerMP) getValue(actionArgument);
                }
            }
        }
        return null;
    }

    public CitizenData getCitizenForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.CITIZEN == actionArgument.getType())
                {
                    return (CitizenData) getValue(actionArgument);
                }
            }
        }
        return null;
    }

    public Boolean getBooleanForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.BOOLEAN == actionArgument.getType())
                {
                    return (Boolean) getValue(actionArgument);
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
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                switch (actionArgument.getType())
                {
                    case INTEGER:
                    case COORDINATE_X:
                    case COORDINATE_Y:
                    case COORDINATE_Z:
                        return (Integer) getValue(actionArgument);
                    default:
                        break;
                }
            }
        }
        return null;
    }

    public String getStringForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()) && actionArgument.getType() == STRING)
            {
                return (String) getValue(actionArgument);
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
