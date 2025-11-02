package com.minecolonies.api.creativetab;

import com.minecolonies.api.blocks.ModBlocks;
import com.minecolonies.api.entity.ModEntities;
import com.minecolonies.api.items.ModFoodItems;
import com.minecolonies.api.items.ModItems;
import com.minecolonies.api.util.constant.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Class used to handle the creativeTab of minecolonies.
 */
public final class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> TAB_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> HUTS = TAB_REG.register("mchuts",
        () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1).icon(() -> new ItemStack(ModBlocks.blockHutTownHall))
            .title(Component.translatable("com.minecolonies.creativetab.huts"))
            .displayItems((config, output) -> ModBlocks.HUTS.forEach(output::accept))
            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> GENERAL = TAB_REG.register("mcgeneral",
        () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1).icon(() -> new ItemStack(ModBlocks.blockRack))
            .title(Component.translatable("com.minecolonies.creativetab.general"))
            .displayItems((config, output) -> {
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
                output.accept(ModBlocks.blockColonySign);

                output.accept(ModItems.scepterLumberjack);
                output.accept(ModItems.permTool);
                output.accept(ModItems.scepterGuard);
                output.accept(ModItems.assistantHammerGold);
                output.accept(ModItems.assistantHammerIron);
                output.accept(ModItems.assistantHammerDiamond);
                output.accept(ModItems.scepterBeekeeper);

                output.accept(ModItems.bannerRallyGuards);

                output.accept(ModItems.supplyChest);
                output.accept(ModItems.supplyCamp);

                output.accept(ModItems.clipboard);
                output.accept(ModItems.resourceScroll);
                output.accept(ModItems.compost);
                output.accept(ModItems.mistletoe);
                output.accept(ModItems.magicPotion);
                output.accept(ModItems.buildGoggles);
                output.accept(ModItems.scanAnalyzer);
                output.accept(ModItems.questLog);
                output.accept(ModItems.colonyMap);

                output.accept(ModItems.scrollColonyTP);
                output.accept(ModItems.scrollColonyAreaTP);
                output.accept(ModItems.scrollBuff);
                output.accept(ModItems.scrollGuardHelp);
                output.accept(ModItems.scrollHighLight);

                output.accept(ModItems.santaHat);

                output.accept(ModItems.blockItemIronGate);
                output.accept(ModItems.blockItemWoodenGate);

                output.accept(ModItems.blockItemFlagBanner);

                output.accept(ModItems.ancientTome);
                output.accept(ModItems.chiefSword);
                output.accept(ModItems.scimitar);
                output.accept(ModItems.pharaoScepter);
                output.accept(ModItems.fireArrow);
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

                output.accept(ModFoodItems.bread_dough);
                output.accept(ModFoodItems.cookie_dough);
                output.accept(ModFoodItems.cake_batter);
                output.accept(ModFoodItems.raw_pumpkin_pie);

                output.accept(ModFoodItems.milkyBread);
                output.accept(ModFoodItems.sugaryBread);
                output.accept(ModFoodItems.goldenBread);
                output.accept(ModFoodItems.chorusBread);

                if (SpawnEggItem.byId(ModEntities.CAMP_BARBARIAN) != null)
                {
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_BARBARIAN));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_ARCHERBARBARIAN));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_CHIEFBARBARIAN));

                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_PIRATE));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_ARCHERPIRATE));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_CHIEFPIRATE));

                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_MUMMY));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_ARCHERMUMMY));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_PHARAO));

                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_SHIELDMAIDEN));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_NORSEMEN_ARCHER));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_NORSEMEN_CHIEF));

                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_AMAZON));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_AMAZONSPEARMAN));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_AMAZONCHIEF));

                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_DROWNED_PIRATE));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_DROWNED_ARCHERPIRATE));
                    output.accept(SpawnEggItem.byId(ModEntities.CAMP_DROWNED_CHIEFPIRATE));
                }
            })
            .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FOOD = TAB_REG.register("mcfood",
        () -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 1).icon(() -> new ItemStack(ModBlocks.blockTomato))
            .title(Component.translatable("com.minecolonies.creativetab.food"))
            .displayItems((config, output) -> {
                output.accept(ModBlocks.blockFarmland);
                output.accept(ModBlocks.blockFloodedFarmland);

                ModBlocks.CROPS.forEach(output::accept);

                // bottles
                output.accept(ModItems.largeEmptyBottle);
                output.accept(ModItems.largeWaterBottle);
                output.accept(ModItems.largeMilkBottle);
                output.accept(ModItems.largeSoyMilkBottle);

                ModFoodItems.INGREDIENTS.forEach(output::accept);
                ModFoodItems.FOODS.forEach(output::accept);
            })
            .build());

    /**
     * Private constructor to hide the implicit one.
     */
    private ModCreativeTabs()
    {
    }
}
