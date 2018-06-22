package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.commands.ActionArgumentType.Is;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ActionArgument
{
    @NotNull private final String name;
    @NotNull private final ActionArgumentType type;
    private final boolean required;
    @Nullable private final List<ActionArgument> actionArgumentList;

    public ActionArgument(@NotNull final String name, @NotNull final ActionArgumentType type, final Is optionalOrRequired, @Nullable final ActionArgument ... actionArguments
)
    {
        super();
        this.name = name;
        this.type = type;
        this.required = optionalOrRequired == Is.REQUIRED;
        this.actionArgumentList = Arrays.asList(actionArguments);
    }

    public String getName()
    {
        return name;
    }

    public ActionArgumentType getType()
    {
        return type;
    }

    public boolean isRequired()
    {
        return required;
    }

    public List<ActionArgument> getActionArgumentList()
    {
        return Collections.unmodifiableList(actionArgumentList);
    }

    public String getUsage()
    {
        final StringBuilder sb = new StringBuilder();
        if (isRequired())
        {
            sb.append('<');
        }
        else
        {
            sb.append('[');
        }
        sb.append(name).append(": ").append(type.getUsageValue());
        if (isRequired())
        {
            sb.append('>');
        }
        else
        {
            sb.append(']');
        }
        return sb.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionArgumentList == null) ? 0 : actionArgumentList.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (required ? 1231 : 1237);
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        final ActionArgument other = (ActionArgument) obj;
        if (actionArgumentList == null)
        {
            if (other.actionArgumentList != null)
            {
                return false;
            }
        }
        else if (!actionArgumentList.equals(other.actionArgumentList))
        {
            return false;
        }
        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }
        if (required != other.required)
        {
            return false;
        }
        if (type != other.type)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ActionArgument [name=" + name + ", type=" + type + ", required=" + required + ", actionArgumentList=" + actionArgumentList + "]";
    }
}
