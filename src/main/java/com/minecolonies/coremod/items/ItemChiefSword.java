package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
import com.minecolonies.coremod.entity.EntityCitizen;
import com.minecolonies.coremod.entity.ai.mobs.EntityChiefBarbarian;
import com.minecolonies.coremod.util.BarbarianUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAmbientCreature;
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
            Stream<EntityLivingBase> barbarians = BarbarianUtils.getBarbariansCloseToEntity(entityIn, 30);
            barbarians.forEach(entity -> entity.addPotionEffect(new PotionEffect(GLOW_EFFECT, 30, 20)));
        }
    }

    @Override
    public boolean hitEntity(final ItemStack stack, final EntityLivingBase target, @NotNull final EntityLivingBase attacker)
    {
        if (attacker instanceof EntityPlayer && BarbarianUtils.isBarbarian(target))
        {
            target.addPotionEffect(new PotionEffect(LEVITATION_EFFECT, 20, 3));
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
