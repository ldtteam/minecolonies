package com.minecolonies.coremod.items;

import com.minecolonies.api.creativetab.ModCreativeTabs;
import com.minecolonies.coremod.util.TeleportHelper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Food;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import static com.minecolonies.api.util.constant.Constants.STACKSIZE;

/**
 * Class handling Chorus Bread.
 */
public class ItemChorusBread extends AbstractItemMinecolonies
{

    /**
     * Setup the food definition
     */
    private static Food chorusBread = (new Food.Builder())
                                        .hunger(5)
                                        .saturation(2.0F)
                                        .setAlwaysEdible()
                                        .build(); 

    /**
     * Sets the name, creative tab, and registers the Chorus Bread item.
     *
     * @param properties the properties.
     */
    public ItemChorusBread(final Properties properties)
    {
        super("chorus_bread", properties.maxStackSize(STACKSIZE).group(ModCreativeTabs.MINECOLONIES).food(chorusBread));
    }

   /**
    * Teleport to the surface. 
    */
    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        
        if (!worldIn.isRemote && entityLiving instanceof ServerPlayerEntity && worldIn.getDimension().getType() == DimensionType.OVERWORLD)
        {
            TeleportHelper.surfaceTeleport((ServerPlayerEntity)entityLiving);
        }

        if (entityLiving instanceof PlayerEntity && !((PlayerEntity)entityLiving).abilities.isCreativeMode) {
           stack.shrink(1);
        }
  
        return stack;
     }    
}
