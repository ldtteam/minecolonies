package com.minecolonies.coremod.commands;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class NavigationMenu extends AbstractMenu
{
    @NotNull private final List<MenuType> childrenMenuList;

    public NavigationMenu(@NotNull final String menuItemName, @NotNull final MenuType ... childMenuTypes)
    {
        super(menuItemName);
        this.childrenMenuList = Arrays.asList(childMenuTypes);
    }

    @Override
    public NavigationMenuType getMenuType()
    {
        return (NavigationMenuType) menuType;
    }


    public List<MenuType> getChildrenMenuList()
    {
        return childrenMenuList;
    }
}
