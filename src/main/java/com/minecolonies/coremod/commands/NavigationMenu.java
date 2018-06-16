package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class NavigationMenu extends AbstractMenu
{
    @NotNull private final List<IMenuType> childrenMenuList;

    public NavigationMenu(@NotNull final String menuItemName, @NotNull final IMenuType ... childMenuTypes)
    {
        super(menuItemName);
        this.childrenMenuList = Arrays.asList(childMenuTypes);
    }

    @Override
    public NavigationMenuType getMenuType()
    {
        return (NavigationMenuType) menuType;
    }


    public List<IMenuType> getChildrenMenuList()
    {
        return childrenMenuList;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((childrenMenuList == null) ? 0 : childrenMenuList.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final NavigationMenu other = (NavigationMenu) obj;
        if (childrenMenuList == null)
        {
            if (other.childrenMenuList != null)
            {
                return false;
            }
        }
        else if (!childrenMenuList.equals(other.childrenMenuList))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "NavigationMenu [childrenMenuList=" + childrenMenuList + "]";
    }
}
