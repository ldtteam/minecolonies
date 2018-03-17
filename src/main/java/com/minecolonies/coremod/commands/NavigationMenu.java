package com.minecolonies.coremod.commands;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.NotNull;

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
}
