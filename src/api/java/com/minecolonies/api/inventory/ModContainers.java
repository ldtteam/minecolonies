package com.minecolonies.api.inventory;

import com.minecolonies.api.inventory.container.*;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
public class ModContainers
{
    @ObjectHolder("crafting_furnace")
    public static MenuType<ContainerCraftingFurnace> craftingFurnace;

    @ObjectHolder("bulding_inv")
    public static MenuType<ContainerBuildingInventory> buildingInv;

    @ObjectHolder("citizen_inv")
    public static MenuType<ContainerCitizenInventory> citizenInv;

    @ObjectHolder("rack_inv")
    public static MenuType<ContainerRack> rackInv;

    @ObjectHolder("grave_inv")
    public static MenuType<ContainerGrave> graveInv;

    @ObjectHolder("crafting_building")
    public static MenuType<ContainerCrafting> craftingGrid;

    @ObjectHolder("field")
    public static MenuType<ContainerField> field;
}
