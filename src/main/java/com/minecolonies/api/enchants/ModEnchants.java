package com.minecolonies.api.enchants;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.core.enchants.RaiderDamageEnchant;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * All our mods renchants
 */
public class ModEnchants
{
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, Constants.MOD_ID);

    private ModEnchants()
    {
        // Intentionally left empty
    }

    /**
     * Raider damage enchant, gives extra damage against raiders
     */
    public static DeferredHolder<Enchantment, RaiderDamageEnchant> raiderDamage;
}
