package com.minecolonies.api.enchants;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

/**
 * All our mods renchants
 */
public class ModEnchants
{
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, Constants.MOD_ID);

    private ModEnchants()
    {
        // Intentionally left empty
    }

    /**
     * Raider damage enchant, gives extra damage against raiders
     */
    public static RegistryObject<? extends Enchantment> raiderDamage;
}
