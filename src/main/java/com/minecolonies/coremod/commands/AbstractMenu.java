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
}
