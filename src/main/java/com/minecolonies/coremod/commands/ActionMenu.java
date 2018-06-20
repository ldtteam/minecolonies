package com.minecolonies.coremod.commands;

import com.minecolonies.coremod.colony.permissions.ForgePermissionNodes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((actionArgumentList == null) ? 0 : actionArgumentList.hashCode());
        result = prime * result + ((actionCommandClass == null) ? 0 : actionCommandClass.hashCode());
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((forgePermissionNode == null) ? 0 : forgePermissionNode.hashCode());
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
        final ActionMenu other = (ActionMenu) obj;
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
        if (actionCommandClass != other.actionCommandClass)
        {
            return false;
        }
        if (description == null)
        {
            if (other.description != null)
            {
                return false;
            }
        }
        else if (!description.equals(other.description))
        {
            return false;
        }
        if (forgePermissionNode != other.forgePermissionNode)
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ActionMenu [description=" + description + ", forgePermissionNode=" + forgePermissionNode + ", actionArgumentList=" + actionArgumentList + ", actionCommandClass="
                + actionCommandClass + "]";
    }
}
