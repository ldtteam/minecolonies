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
}
