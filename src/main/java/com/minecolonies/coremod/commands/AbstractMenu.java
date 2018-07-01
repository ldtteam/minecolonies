package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractMenu implements IMenu
{
    @NotNull protected IMenuType menuType;
    @NotNull private final String menuItemName;

    protected AbstractMenu(@NotNull final String menuItemName)
    {
        this.menuItemName = menuItemName;
    }

    @Override
    public String getMenuItemName()
    {
        return menuItemName;
    }

    @Override
    public void setMenuType(final IMenuType menuType)
    {
        this.menuType = menuType;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((menuItemName == null) ? 0 : menuItemName.hashCode());
        result = prime * result + ((menuType == null) ? 0 : menuType.hashCode());
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
        final AbstractMenu other = (AbstractMenu) obj;
        if (menuItemName == null)
        {
            if (other.menuItemName != null)
            {
                return false;
            }
        }
        else if (!menuItemName.equals(other.menuItemName))
        {
            return false;
        }
        if (menuType == null)
        {
            if (other.menuType != null)
            {
                return false;
            }
        }
        else if (!menuType.equals(other.menuType))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "AbstractMenu [menuType=" + menuType + ", menuItemName=" + menuItemName + "]";
    }
}
