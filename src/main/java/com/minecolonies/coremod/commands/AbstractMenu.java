package com.minecolonies.coremod.commands;

import org.jetbrains.annotations.NotNull;

public abstract class AbstractMenu implements Menu
{
    @NotNull protected MenuType menuType;
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
    public void setMenuType(final MenuType menuType)
    {
        this.menuType = menuType;
    }
}
