package com.minecolonies.api.enchants;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * All our mods renchants
 */
public class ModEnchants
{
    public static final ResourceKey<Enchantment> raiderDamage = ResourceKey.create(Registries.ENCHANTMENT, new ResourceLocation(Constants.MOD_ID, "raider_damage_enchant"));

    private ModEnchants()
    {
        // Intentionally left empty
    }
}
