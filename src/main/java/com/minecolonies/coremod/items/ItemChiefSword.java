package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.util.BarbarianUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

/**
 * Class handling the Chief Sword item.
 */
public class ItemChiefSword extends ItemSword
{
    private static final String ITEM_NAME         = "chiefSword";
    private static final Potion LEVITATION_EFFECT = Potion.getPotionById(25);
    private static final Potion GLOW_EFFECT       = Potion.getPotionById(24);

    private static final int GLOW_EFFECT_DURATION = 30;
    private static final int GLOW_EFFECT_MULTIPLIER = 20;
    private static final int GLOW_EFFECT_DISTANCE = 30;

    private static final int LEVITATION_EFFECT_DURATION = 20;
    private static final int LEVITATION_EFFECT_MULTIPLIER = 3;

    /**
     * Constructor method for the Chief Sword Item
     */
    public ItemChiefSword()
    {
        super(ToolMaterial.GOLD);
        super.setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + ITEM_NAME);
        setRegistryName(ITEM_NAME);
        GameRegistry.register(this);
    }

    @Override
    public void onUpdate(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {
        if (entityIn instanceof EntityPlayer && isSelected)
        {
            final Stream<EntityLivingBase> barbarians = BarbarianUtils.getBarbariansCloseToEntity(entityIn, GLOW_EFFECT_DISTANCE);
            barbarians.forEach(entity -> entity.addPotionEffect(new PotionEffect(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER)));
        }
    }

    @Override
    public boolean hitEntity(final ItemStack stack, final EntityLivingBase target, @NotNull final EntityLivingBase attacker)
    {
        if (attacker instanceof EntityPlayer && BarbarianUtils.isBarbarian(target))
        {
            target.addPotionEffect(new PotionEffect(LEVITATION_EFFECT, LEVITATION_EFFECT_DURATION, LEVITATION_EFFECT_MULTIPLIER));
        }

        return super.hitEntity(stack, target, attacker);
    }

    /**
     * returns the items name
     *
     * @return Returns the items name in the form of a string
     */
    public final String getName()
    {
        return ITEM_NAME;
    }
}
