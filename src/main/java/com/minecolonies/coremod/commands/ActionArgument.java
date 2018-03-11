package com.minecolonies.coremod.commands;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.commands.ActionArgumentType.Is;

public class ActionArgument
{
    private String name;
    private ActionArgumentType type;
    private boolean required;
    private boolean isValueSet = false;
    private Object value;
    @NotNull private final List<ActionArgument> actionArgumentList;

    public ActionArgument(final String name, final ActionArgumentType type, final Is optionalOrRequired, final ActionArgument ... actionArguments
)
    {
        super();
        this.name = name;
        this.type = type;
        this.required = optionalOrRequired == Is.Required;
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
        sb.append(name);
        sb.append(':');
        sb.append(' ');
        sb.append(type.getUsageValue());
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
        return isValueSet;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(final Object value)
    {
        this.isValueSet = true;
        this.value = value;
    }
}
