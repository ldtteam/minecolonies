package com.minecolonies.coremod.inventory;

import com.ldtteam.structurize.api.util.constant.Constants;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MinecoloniesContainers
{
    @ObjectHolder("CraftingFurnace")
    public static ContainerType<?> craftingFurnace;

    @ObjectHolder("CraftingGrid")
    public static Container craftingGrid;

    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event)
    {
        craftingFurnace = IForgeContainerType.create(ContainerGUICraftingFurnace::new).setRegistryName("crafting_furnace");
    }
}
