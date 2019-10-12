package com.minecolonies.coremod.commands;

import com.minecolonies.api.colony.ICitizenData;
import com.minecolonies.api.colony.IColony;
import net.minecraft.entity.player.EntityPlayerMP;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.minecolonies.coremod.commands.ActionArgumentType.STRING;

public class ActionMenuState
{
    @NotNull private final ActionMenu actionMenu;
    @NotNull private final Map<String, ArgumentState> argumentStateByActionArgumentNameMap = new HashMap<>();

    private static class ArgumentState
    {
        private boolean valueIsSet = false;
        @Nullable private Object value = null;
    }

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

    public IColony getColonyForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.COLONY == actionArgument.getType())
                {
                    return (IColony) getValue(actionArgument);
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
                if (ActionArgumentType.ONLINE_PLAYER == actionArgument.getType() || ActionArgumentType.PLAYER == actionArgument.getType())
                {
                    return (EntityPlayerMP) getValue(actionArgument);
                }
            }
        }
        return null;
    }

    public EntityPlayerMP getOnlinePlayerForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.ONLINE_PLAYER == actionArgument.getType())
                {
                    return (EntityPlayerMP) getValue(actionArgument);
                }
            }
        }
        return null;
    }

    public ICitizenData getCitizenForArgument(@NotNull final String argumentName)
    {
        for (final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                if (ActionArgumentType.CITIZEN == actionArgument.getType())
                {
                    return (ICitizenData) getValue(actionArgument);
                }
            }
        }
        return null;
    }

    /*
     * Suppressing Sonar Rule squid:S2447
     * This rule complains about returning null for a Boolean method.
     * But in this case the rule does not apply because
     * We are returning null to indicate that no boolean argument value was set.
     */
    @SuppressWarnings({"squid:S2447"})
    @Nullable
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

    public Double getDoubleForArgument(@NotNull final String argumentName)
    {
        for(final ActionArgument actionArgument : getAllArgumentsList())
        {
            if (argumentName.equals(actionArgument.getName()))
            {
                switch (actionArgument.getType())
                {
                    case DOUBLE:
                        return (Double) getValue(actionArgument);
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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionMenu == null) ? 0 : actionMenu.hashCode());
        result = prime * result + argumentStateByActionArgumentNameMap.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final ActionMenuState other = (ActionMenuState) obj;
        if (actionMenu == null)
        {
            if (other.actionMenu != null)
            {
                return false;
            }
        }
        else if (!actionMenu.equals(other.actionMenu))
        {
            return false;
        }
        return argumentStateByActionArgumentNameMap.equals(other.argumentStateByActionArgumentNameMap);
    }

    @Override
    public String toString()
    {
        return "ActionMenuState [actionMenu=" + actionMenu + ", argumentStateByActionArgumentNameMap=" + argumentStateByActionArgumentNameMap + "]";
    }
}
