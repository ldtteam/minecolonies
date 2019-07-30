package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.entity.ai.mobs.IBaseMinecoloniesMob;
import com.minecolonies.coremod.entity.ai.mobs.barbarians.AbstractEntityBarbarian;
import com.minecolonies.coremod.entity.ai.mobs.util.BarbarianUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static com.minecolonies.api.util.constant.Constants.*;

/**
 * Class handling the Chief Sword item.
 */
public class ItemChiefSword extends ItemSword
{
    private static final int LEVITATION_EFFECT_DURATION   = 20*10;
    private static final int LEVITATION_EFFECT_MULTIPLIER = 3;

    /**
     * Constructor method for the Chief Sword Item
     */
    public ItemChiefSword()
    {
        super(ToolMaterial.DIAMOND);
        super.setTranslationKey(Constants.MOD_ID.toLowerCase() + "." + CHIEFSWORD_NAME);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + CHIEFSWORD_NAME);
        super.setCreativeTab(ModCreativeTabs.MINECOLONIES);
    }

    @Override
    public void onUpdate(final ItemStack stack, final World worldIn, final Entity entityIn, final int itemSlot, final boolean isSelected)
    {

        if (entityIn instanceof EntityPlayer && isSelected)
        {
            final Stream<IBaseMinecoloniesMob> barbarians = BarbarianUtils.getBarbariansCloseToEntity(entityIn, GLOW_EFFECT_DISTANCE).stream();
            barbarians.forEach(entity -> entity.addPotionEffect(new PotionEffect(GLOW_EFFECT, GLOW_EFFECT_DURATION, GLOW_EFFECT_MULTIPLIER)));
        }
    }

    @Override
    public boolean hitEntity(final ItemStack stack, final EntityLivingBase target, @NotNull final EntityLivingBase attacker)
    {
        if (attacker instanceof EntityPlayer && target instanceof AbstractEntityBarbarian)
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
        return CHIEFSWORD_NAME;
    }
}
