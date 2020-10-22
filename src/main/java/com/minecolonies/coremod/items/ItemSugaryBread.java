package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Milk Bread.
 */
public class ItemSugaryBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food sweetBread = (new Food.Builder())
                                        .hunger(6)
                                        .saturation(0.7F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Sweet Bread item.
     *
     * @param properties the properties.
     */
    public ItemSugaryBread(final Properties properties)
    {
        super("sugary_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(sweetBread));
    }

   /**
    * Remove the poison effect
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {

        if (!worldIn.isRemote)
        {
            entityLiving.removePotionEffect(Effects.POISON);
        }

        if (entityLiving instanceof PlayerEntity && !((PlayerEntity)entityLiving).abilities.isCreativeMode) {
           stack.shrink(1);
        }
  
        return stack;
     }    
}
