package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.api.entity.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.api.entity.mobs.util.BarbarianUtils;
import com.minecolonies.api.items.IChiefSwordItem;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTier;
import net.minecraft.item.SwordItem;
import net.minecraft.potion.EffectInstance;
import net.minecraft.world.World;
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
     */
    public ItemChiefSword(final Properties properties)
    {
        super(ItemTier.DIAMOND, 3, -2.4f, properties.group(ModCreativeTabs.MINECOLONIES));
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + CHIEFSWORD_NAME);
    }

    @Override
    public void inventoryTick(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        if (entityIn instanceof PlayerEntity && isSelected)
        {
            BarbarianUtils.getBarbariansCloseToEntity(entityIn, GLOW_EFFECT_DISTANCE)
              .forEach(entity -> entity.addPotionEffect(new EffectInstance(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER)));
        }
    }

    @Override
    public boolean hitEntity(final ItemStack stack, final LivingEntity target, @NotNull final LivingEntity attacker)
    {
        if (attacker instanceof PlayerEntity && target instanceof AbstractEntityBarbarian)
        {
            target.addPotionEffect(new EffectInstance(LEVITATION_EFFECT, LEVITATION_EFFECT_DURATION, LEVITATION_EFFECT_MULTIPLIER));
        }

        return super.hitEntity(stack, target, attacker);
    }
}
