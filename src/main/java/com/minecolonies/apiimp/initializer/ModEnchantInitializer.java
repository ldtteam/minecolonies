package com.minecolonies.apiimp.initializer;

import com.minecolonies.api.enchants.ModEnchants;
import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.enchants.RaiderDamageEnchant;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Enchants initializer
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEnchantInitializer
{
    @SubscribeEvent
    public static void registerEnchants(final RegistryEvent.Register<Enchantment> event)
    {
        ModEnchants.raiderDamage = new RaiderDamageEnchant(Enchantment.Rarity.VERY_RARE, new EquipmentSlotType[] {EquipmentSlotType.MAINHAND});
        event.getRegistry().register(ModEnchants.raiderDamage);
    }
}
