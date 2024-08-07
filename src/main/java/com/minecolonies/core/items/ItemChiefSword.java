package com.minecolonies.core.items;

import com.minecolonies.api.entity.mobs.RaiderMobUtils;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.items.IChiefSwordItem;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Class handling the Chief Sword item.
 */
public class ItemChiefSword extends SwordItem implements IChiefSwordItem
{
    private static final int LEVITATION_EFFECT_DURATION   = 20 * 10;
    private static final int LEVITATION_EFFECT_MULTIPLIER = 3;

    /**
     * Constructor method for the Chief Sword Item
     *
     * @param properties the properties.
     */
    public ItemChiefSword(final Properties properties)
    {
        super(Tiers.DIAMOND, properties.attributes(SwordItem.createAttributes(Tiers.WOOD, 3, -2.4F)));
    }

    @Override
    public void inventoryTick(final ItemStack stack, final Level worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        if (entityIn instanceof Player && isSelected)
        {
            RaiderMobUtils.getBarbariansCloseToEntity(entityIn, GLOW_EFFECT_DISTANCE)
              .forEach(entity -> entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER)));
        }
    }

    @Override
    public boolean hurtEnemy(final ItemStack stack, final LivingEntity target, @NotNull final LivingEntity attacker)
    {
        if (attacker instanceof Player && target instanceof AbstractEntityBarbarian)
        {
            target.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_EFFECT_DURATION, LEVITATION_EFFECT_MULTIPLIER));
        }

        return super.hurtEnemy(stack, target, attacker);
    }
}
