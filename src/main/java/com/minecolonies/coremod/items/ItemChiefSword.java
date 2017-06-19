package com.minecolonies.coremod.items;

import com.minecolonies.api.util.constant.Constants;
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
