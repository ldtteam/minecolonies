package com.minecolonies.api.creativetab;

import com.minecolonies.api.blocks.AbstractBlockHut;
import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Class used to handle the creativeTab of minecolonies.
 */
@Mod.EventBusSubscriber
public final class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TAB_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final RegistryObject<CreativeModeTab> HUTS = TAB_REG.register("mchuts", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1)
                                                                                                      .icon(() -> new ItemStack(ModBlocks.blockHutTownHall))
                                                                                                      .title(Component.translatable("com.minecolonies.creativetab.huts")).displayItems((config, output) -> {
          for (final AbstractBlockHut<?> hut : ModBlocks.getHuts())
          {
              output.accept(hut);
          }
      }).build());

    public static final RegistryObject<CreativeModeTab> GENERAL = TAB_REG.register("mcgeneral", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1)
                                                                                                      .icon(() -> new ItemStack(ModBlocks.blockRack))
                                                                                                      .title(Component.translatable("com.minecolonies.creativetab.general")).displayItems((config, output) -> {
          output.accept(ModBlocks.blockScarecrow);
          output.accept(ModBlocks.blockPlantationField);
          output.accept(ModBlocks.blockRack);
          output.accept(ModBlocks.blockGrave);
          output.accept(ModBlocks.blockNamedGrave);
          output.accept(ModBlocks.blockWayPoint);
          output.accept(ModBlocks.blockBarrel);
          output.accept(ModBlocks.blockDecorationPlaceholder);
          output.accept(ModBlocks.blockCompostedDirt);
          output.accept(ModBlocks.blockConstructionTape);

          output.accept(ModItems.scepterLumberjack);
          output.accept(ModItems.permTool);
          output.accept(ModItems.scepterGuard);
          output.accept(ModItems.scepterBeekeeper);

          output.accept(ModItems.bannerRallyGuards);

          output.accept(ModItems.supplyChest);
          output.accept(ModItems.supplyCamp);

          output.accept(ModItems.clipboard);
          output.accept(ModItems.resourceScroll);
          output.accept(ModItems.compost);
          output.accept(ModItems.mistletoe);
          output.accept(ModItems.magicpotion);
          output.accept(ModItems.buildGoggles);
          output.accept(ModItems.scanAnalyzer);
          output.accept(ModItems.questLog);

          output.accept(ModItems.scrollColonyTP);
          output.accept(ModItems.scrollColonyAreaTP);
          output.accept(ModItems.scrollBuff);
          output.accept(ModItems.scrollGuardHelp);
          output.accept(ModItems.scrollHighLight);

          output.accept(ModItems.santaHat);

          output.accept(ModItems.irongate);
          output.accept(ModItems.woodgate);

          output.accept(ModItems.flagBanner);

          output.accept(ModItems.ancientTome);
          output.accept(ModItems.chiefSword);
          output.accept(ModItems.scimitar);
          output.accept(ModItems.pharaoscepter);
          output.accept(ModItems.firearrow);
          output.accept(ModItems.spear);
          output.accept(ModItems.pirateHelmet_1);
          output.accept(ModItems.pirateChest_1);
          output.accept(ModItems.pirateLegs_1);
          output.accept(ModItems.pirateBoots_1);

          output.accept(ModItems.pirateHelmet_2);
          output.accept(ModItems.pirateChest_2);
          output.accept(ModItems.pirateLegs_2);
          output.accept(ModItems.pirateBoots_2);

          output.accept(ModItems.plateArmorHelmet);
          output.accept(ModItems.plateArmorChest);
          output.accept(ModItems.plateArmorLegs);
          output.accept(ModItems.plateArmorBoots);

          output.accept(ModItems.sifterMeshString);
          output.accept(ModItems.sifterMeshFlint);
          output.accept(ModItems.sifterMeshIron);
          output.accept(ModItems.sifterMeshDiamond);

          output.accept(ModItems.breadDough);
          output.accept(ModItems.cookieDough);
          output.accept(ModItems.cakeBatter);
          output.accept(ModItems.rawPumpkinPie);

          output.accept(ModItems.milkyBread);
          output.accept(ModItems.sugaryBread);
          output.accept(ModItems.goldenBread);
          output.accept(ModItems.chorusBread);
      }).build());

    public static final RegistryObject<CreativeModeTab> FOOD = TAB_REG.register("mcfood", () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1)
                                                                                                      .icon(() -> new ItemStack(ModBlocks.blockTomato))
                                                                                                      .title(Component.translatable("com.minecolonies.creativetab.food")).displayItems((config, output) -> {
          output.accept(ModBlocks.farmland);
          output.accept(ModBlocks.floodedFarmland);

          for (final Block crop : ModBlocks.getCrops())
          {
              output.accept(crop);
          }

          // bottles
          output.accept(ModItems.large_empty_bottle);
          output.accept(ModItems.large_water_bottle);
          output.accept(ModItems.large_milk_bottle);
          output.accept(ModItems.large_soy_milk_bottle);

          for (final Item food : ModItems.getAllIngredients())
          {
              output.accept(food);
          }

          for (final Item food : ModItems.getAllFoods())
          {
              output.accept(food);
          }
      }).build());

    /**
     * Private constructor to hide the implicit one.
     */
    private ModCreativeTabs()
    {
        /*
         * Intentionally left empty.
         */
    }
}
