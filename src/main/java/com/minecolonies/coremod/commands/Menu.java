package com.minecolonies.coremod.commands;

public interface Menu
{
    public String getMenuItemName();
    public MenuType getMenuType();
    public void setMenuType(MenuType menuType);
}
