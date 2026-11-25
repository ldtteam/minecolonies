package com.minecolonies.api.util;

import com.minecolonies.api.util.constant.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * AI Generated Tests.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID)
public final class InventoryTests
{
    private InventoryTests() {}

    private record Test(String name, Runnable body) {}

    @SubscribeEvent
    public static void onRegisterCommands(final RegisterCommandsEvent event)
    {
        if (!FMLEnvironment.production)
        {
            event.getDispatcher().register(Commands.literal("mcinvtest")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> runAll(ctx.getSource())));
        }
    }

    private static List<Test> tests()
    {
        final List<Test> t = new ArrayList<>();

        // ---------------- transfer

        t.add(new Test("transfer: simple move into empty destination", () -> {
            final ItemStackHandler origin = inv(3, s(Items.DIRT, 10));
            final ItemStackHandler dest = inv(3);
            eq("returned", 10, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 10));
            eq("origin", 0, count(origin, Items.DIRT));
            eq("dest", 10, count(dest, Items.DIRT));
        }));

        // KNOWN BUG: extract asks each slot for the full qty, so this moves 26 instead of 16.
        t.add(new Test("transfer: qty spanning two origin slots moves exactly qty", () -> {
            final ItemStackHandler origin = inv(3, s(Items.DIRT, 10), s(Items.DIRT, 64));
            final ItemStackHandler dest = inv(5);
            eq("returned", 16, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 16));
            eq("origin", 58, count(origin, Items.DIRT));
            eq("dest", 16, count(dest, Items.DIRT));
        }));

        t.add(new Test("transfer: destination only has partial room", () -> {
            final ItemStackHandler origin = inv(2, s(Items.DIRT, 40), s(Items.DIRT, 40));
            final ItemStackHandler dest = inv(1, s(Items.DIRT, 30));
            eq("returned", 34, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 50));
            eq("origin", 46, count(origin, Items.DIRT));
            eq("dest", 64, count(dest, Items.DIRT));
        }));

        t.add(new Test("transfer: destination full of other items", () -> {
            final ItemStackHandler origin = inv(2, s(Items.DIRT, 20));
            final ItemStackHandler dest = inv(2, s(Items.STONE, 64), s(Items.STONE, 64));
            eq("returned", 0, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 20));
            eq("origin dirt", 20, count(origin, Items.DIRT));
            eq("dest stone", 128, count(dest, Items.STONE));
            eq("dest dirt", 0, count(dest, Items.DIRT));
        }));

        // KNOWN BUG: leftovers go back via insert(origin, ...) with the result ignored, so 6 dirt vanish.
        t.add(new Test("transfer: leftovers survive when origin refuses re-insert", () -> {
            final ItemStackHandler origin = extractOnly(1, s(Items.DIRT, 10));
            final ItemStackHandler dest = inv(1, s(Items.DIRT, 60));
            eq("returned", 4, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 10));
            eq("dest", 64, count(dest, Items.DIRT));
            eq("total dirt (no loss)", 70, count(origin, Items.DIRT) + count(dest, Items.DIRT));
        }));

        t.add(new Test("transfer: more requested than available", () -> {
            final ItemStackHandler origin = inv(2, s(Items.DIRT, 5));
            final ItemStackHandler dest = inv(2);
            eq("returned", 5, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 20));
            eq("origin", 0, count(origin, Items.DIRT));
            eq("dest", 5, count(dest, Items.DIRT));
        }));

        t.add(new Test("transfer: qty 0 moves nothing", () -> {
            final ItemStackHandler origin = inv(2, s(Items.DIRT, 10));
            final ItemStackHandler dest = inv(2);
            eq("returned", 0, InventoryUtils.transfer(origin, dest, s(Items.DIRT, 1), 0));
            eq("origin", 10, count(origin, Items.DIRT));
        }));

        t.add(new Test("transfer: non-stackables, destination has 2 of 3 slots", () -> {
            final ItemStackHandler origin = inv(3, s(Items.DIAMOND_SWORD, 1), s(Items.DIAMOND_SWORD, 1), s(Items.DIAMOND_SWORD, 1));
            final ItemStackHandler dest = inv(2);
            eq("returned", 2, InventoryUtils.transfer(origin, dest, (Predicate<ItemStack>) st -> st.is(Items.DIAMOND_SWORD), 3));
            eq("origin", 1, count(origin, Items.DIAMOND_SWORD));
            eq("dest", 2, count(dest, Items.DIAMOND_SWORD));
        }));

        // ---------------- transferAllItemHandler

        t.add(new Test("transferAll: everything fits", () -> {
            final ItemStackHandler origin = inv(3, s(Items.DIRT, 64), s(Items.STONE, 64), s(Items.DIRT, 10));
            final ItemStackHandler dest = inv(5);
            isTrue("returned true", InventoryUtils.transferAllItemHandler(origin, dest));
            eq("origin dirt", 0, count(origin, Items.DIRT));
            eq("origin stone", 0, count(origin, Items.STONE));
            eq("dest dirt", 74, count(dest, Items.DIRT));
            eq("dest stone", 64, count(dest, Items.STONE));
        }));

        t.add(new Test("transferAll: destination too small, nothing lost", () -> {
            final ItemStackHandler origin = inv(3, s(Items.DIRT, 64), s(Items.STONE, 64), s(Items.DIRT, 10));
            final ItemStackHandler dest = inv(2);
            isTrue("returned false", !InventoryUtils.transferAllItemHandler(origin, dest));
            eq("total dirt", 74, count(origin, Items.DIRT) + count(dest, Items.DIRT));
            eq("total stone", 64, count(origin, Items.STONE) + count(dest, Items.STONE));
        }));

        t.add(new Test("transferAll: destination partially full, nothing lost", () -> {
            final ItemStackHandler origin = inv(2, s(Items.DIRT, 64), s(Items.DIRT, 64));
            final ItemStackHandler dest = inv(2, s(Items.DIRT, 50), s(Items.STONE, 64));
            isTrue("returned false", !InventoryUtils.transferAllItemHandler(origin, dest));
            eq("dest dirt", 64, count(dest, Items.DIRT));
            eq("total dirt", 178, count(origin, Items.DIRT) + count(dest, Items.DIRT));
        }));

        return t;
    }

    // ------------------------------------------------------------------ runner

    private static int runAll(final CommandSourceStack source)
    {
        int passed = 0;
        int failed = 0;
        for (final Test test : tests())
        {
            try
            {
                test.body().run();
                passed++;
                report(source, ChatFormatting.GREEN, "PASS " + test.name());
            }
            catch (final Throwable e)
            {
                failed++;
                report(source, ChatFormatting.RED, "FAIL " + test.name() + " -> " + (e instanceof AssertionError ? e.getMessage() : e.toString()));
                if (!(e instanceof AssertionError))
                {
                    Log.getLogger().error("mcinvtest crashed: " + test.name(), e);
                }
            }
        }
        report(source, failed == 0 ? ChatFormatting.GREEN : ChatFormatting.GOLD, passed + " passed, " + failed + " failed");
        return failed == 0 ? 1 : 0;
    }

    private static void report(final CommandSourceStack source, final ChatFormatting color, final String line)
    {
        Log.getLogger().info("[mcinvtest] " + line);
        source.sendSuccess(() -> Component.literal(line).withStyle(color), false);
    }

    // ------------------------------------------------------------------ helpers

    private static ItemStack s(final Item item, final int count) { return new ItemStack(item, count); }

    private static ItemStackHandler inv(final int slots, final ItemStack... stacks)
    {
        return fill(new ItemStackHandler(slots), stacks);
    }

    /** A handler you can take from but not put into (like an output-only slot). */
    private static ItemStackHandler extractOnly(final int slots, final ItemStack... stacks)
    {
        return fill(new ItemStackHandler(slots)
        {
            @Override
            public boolean isItemValid(final int slot, @NotNull final ItemStack stack)
            {
                return false;
            }
        }, stacks);
    }

    private static ItemStackHandler fill(final ItemStackHandler handler, final ItemStack... stacks)
    {
        for (int i = 0; i < stacks.length; i++)
        {
            handler.setStackInSlot(i, stacks[i].copy());
        }
        return handler;
    }

    private static int count(final IItemHandler handler, final Item item) { return InventoryUtils.getItemCount(handler, item); }

    private static void eq(final String what, final int expected, final int actual)
    {
        if (expected != actual)
        {
            throw new AssertionError(what + ": expected " + expected + ", got " + actual);
        }
    }

    private static void isTrue(final String what, final boolean condition)
    {
        if (!condition)
        {
            throw new AssertionError(what);
        }
    }
}