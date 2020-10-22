package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Milk Bread.
 */
public class ItemMilkyBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food milkBread = (new Food.Builder())
                                        .hunger(5)
                                        .saturation(0.6F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Milk Bread item.
     *
     * @param properties the properties.
     */
    public ItemMilkyBread(final Properties properties)
    {
        super("milky_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(milkBread));
    }

   /**
    * Remove the potion effects like Milk
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isRemote)
        {
            entityLiving.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
        }

        if (entityLiving instanceof PlayerEntity && !((PlayerEntity)entityLiving).abilities.isCreativeMode) {
           stack.shrink(1);
        }
  
        return stack;
     }    
}
