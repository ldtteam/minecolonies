package com.minecolonies.coremod.gameplay.loot;

import com.google.gson.JsonObject;
import com.minecolonies.api.util.constant.Constants;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.event.RegistryEvent;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class LootModifiers {
    public static final String MOD_ID = Constants.MOD_ID;
    public static final boolean ENABLE = true;

    public LootModifiers() {}

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class EventHandlers {
        @SubscribeEvent
        public static void registerModifierSerializers(@Nonnull final RegistryEvent.Register<GlobalLootModifierSerializer<?>> event)
        {
            if (ENABLE)
            {
                event.getRegistry().register(new WartLootModifier.Serializer().setRegistryName(new ResourceLocation(MOD_ID, "nether_wart_block")));
                //event.getRegistry().register(new WartLootModifier.Serializer().setRegistryName(new ResourceLocation(MOD_ID, "warped_wart_block")));
            }
        }
    }

    private static class WartLootModifier extends LootModifier
    {
        private final Item itemReplacement;
        private final Item itemCheck;
        public WartLootModifier(ILootCondition[] conditionsIn, Item check, Item replacement)
        {
            super(conditionsIn);
            itemCheck = check;
            itemReplacement = replacement;
        }

        @Nonnull
        @Override
        public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context)
        {
            for(ItemStack stack : generatedLoot)
            {
                if (stack.getItem() == itemCheck)
                {
                    generatedLoot.add(new ItemStack(itemReplacement, 1));
                    generatedLoot.remove(stack);
                }
            }
            return generatedLoot;
        }

        private static class Serializer extends GlobalLootModifierSerializer<WartLootModifier>
        {
            @Override
            public WartLootModifier read(ResourceLocation name, JsonObject object, ILootCondition[] conditionsIn)
            {
                Item wart = ForgeRegistries.ITEMS.getValue(new ResourceLocation((JSONUtils.getString(object, "checkItem"))));
                Item fungus = ForgeRegistries.ITEMS.getValue(new ResourceLocation(JSONUtils.getString(object, "replacement")));
                return new WartLootModifier(conditionsIn, wart, fungus);
            }

            public JsonObject write(WartLootModifier modifier)
            {
                return new JsonObject();
            }
        }
    }

}
