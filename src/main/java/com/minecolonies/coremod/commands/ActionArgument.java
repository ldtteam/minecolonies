package com.minecolonies.coremod.commands;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.commands.ActionArgumentType.Is;

public class ActionArgument
{
    @NotNull private final String name;
    @NotNull private final ActionArgumentType type;
    private final boolean required;
    private boolean valueIsSet = false;
    @Nullable private Object value;
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
        return actionArgumentList;
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

    public boolean isValueSet()
    {
        return valueIsSet;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(final Object value)
    {
        this.valueIsSet = true;
        this.value = value;
    }
}
