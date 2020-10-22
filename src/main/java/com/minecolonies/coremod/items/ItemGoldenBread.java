package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Golden Bread.
 */
public class ItemGoldenBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food goldenBread = (new Food.Builder())
                                        .hunger(5)
                                        .saturation(0.6F)
                                        //.effect(() -> new EffectInstance(Effects.REGENERATION, 100, 1), 1.0F)
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Golden Bread item.
     *
     * @param properties the properties.
     */
    public ItemGoldenBread(final Properties properties)
    {
        super("golden_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(goldenBread));
    }

   /**
    * Heal 2 hearts
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isRemote)
        {
            entityLiving.heal(4);
        }

        if (entityLiving instanceof PlayerEntity && !((PlayerEntity)entityLiving).abilities.isCreativeMode) {
           stack.shrink(1);
        }
  
        return stack;
     }    
}
