package com.minecolonies.coremod.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;

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
}
