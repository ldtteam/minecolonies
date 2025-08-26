package com.minecolonies.core.commands.arguments;

import com.minecolonies.api.colony.IColony;
import com.minecolonies.api.colony.IColonyManager;
import com.minecolonies.api.util.constant.translation.CommandTranslationConstants;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A command argument that can dynamically resolve to a colony id.
 */
public class ColonyIdArgument implements ArgumentType<ColonyIdArgument.Result>
{
    private static final String                     HERE                 = "@here";
    private static final String                     MINE                 = "@mine";
    private static final Collection<String>         EXAMPLES             = List.of("1", HERE, MINE, "Player", "dd12be42-52a9-4a91-a8a1-11c01849e498");
    private static final SimpleCommandExceptionType ERROR_UNKNOWN_PLAYER = new SimpleCommandExceptionType(Component.translatable("argument.player.unknown"));
    private static final SimpleCommandExceptionType ERROR_UNKNOWN_COLONY = new SimpleCommandExceptionType(Component.translatable("com.minecolonies.command.argument.colony.unknown"));

    private static final Map<String, Message>       TOOLTIPS             = Map.of(
        HERE, Component.translatable("com.minecolonies.command.argument.colony.here"),
        MINE, Component.translatable("com.minecolonies.command.argument.colony.mine")
    );

    /**
     * Create a placeholder for a {@link ColonyIdArgument} when defining a command.
     */
    public static ColonyIdArgument id()
    {
        return new ColonyIdArgument();
    }

    /**
     * Resolve the actual argument value into a colony id.
     * @param context the command context.
     * @param name    the argument name.
     * @return the colony id.
     * @throws CommandRuntimeException if a colony id cannot be parsed from the given argument (this is already reported back).
     */
    public static int getColonyId(@NotNull final CommandContext<CommandSourceStack> context, @NotNull final String name)
    {
        try
        {
            return context.getArgument(name, ColonyIdArgument.Result.class).resolve(context.getSource());
        }
        catch (CommandSyntaxException e)
        {
            final Component message = ComponentUtils.fromMessage(e.getRawMessage());
            context.getSource().sendFailure(message);
            throw new CommandRuntimeException(message);
        }
    }

    /**
     * Resolve the actual argument value into a colony.
     * @param context the command context.
     * @param name    the argument name.
     * @return the colony.
     * @throws CommandRuntimeException if a colony id cannot be parsed from the given argument (this is already reported back).
     */
    @NotNull
    public static IColony getColony(@NotNull final CommandContext<CommandSourceStack> context, @NotNull final String name)
    {
        final int colonyId = getColonyId(context, name);

        final IColony colony = IColonyManager.getInstance().getColonyByWorld(colonyId, context.getSource().getLevel());
        if (colony == null)
        {
            final Component message = Component.translatable(CommandTranslationConstants.COMMAND_COLONY_ID_NOT_FOUND, colonyId);
            context.getSource().sendFailure(message);
            throw new CommandRuntimeException(message);
        }

        return colony;
    }

    @Override
    public ColonyIdArgument.Result parse(final StringReader reader) throws CommandSyntaxException
    {
        int i = reader.getCursor();

        if (reader.canRead() && reader.peek() == '@')
        {
            reader.skip();
            final String selector = "@" + reader.readUnquotedString();

            if (selector.equals(HERE))
            {
                return ColonyIdArgument::resolveHere;
            }
            else if (selector.equals(MINE))
            {
                return ColonyIdArgument::resolveMine;
            }

            reader.setCursor(i + 1);
            throw EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(reader, selector);
        }
        else if (reader.canRead())
        {
            try
            {
                final int id = reader.readInt();
                if (id < 1)
                {
                    throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(reader, id, 1);
                }
                return source -> id;
            }
            catch (CommandSyntaxException e)
            {
                reader.setCursor(i);
            }

            final String name = reader.readString();
            try
            {
                final UUID id = UUID.fromString(name);
                return source -> resolveTheirs(source, id);
            }
            catch (IllegalArgumentException e)
            {
                if (name.isEmpty() || name.length() > 16)
                {
                    reader.setCursor(i);
                    throw EntitySelectorParser.ERROR_INVALID_NAME_OR_UUID.createWithContext(reader);
                }

                return source -> resolveTheirs(source, name);
            }
        }

        throw EntitySelectorParser.ERROR_INVALID_NAME_OR_UUID.createWithContext(reader);
    }

    @Override
    public Collection<String> getExamples()
    {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        if (context.getSource() instanceof final SharedSuggestionProvider provider)
        {
            final List<String> suggestions = new ArrayList<>();
            suggestions.add(HERE);
            suggestions.add(MINE);

            suggestColonyIds(context, suggestions);
            suggestions.addAll(provider.getOnlinePlayerNames());

            return SharedSuggestionProvider.suggest(suggestions, builder, Function.identity(), TOOLTIPS::get);
        }
        else
        {
            return Suggestions.empty();
        }
    }

    private static <S> void suggestColonyIds(final CommandContext<S> context, final List<String> suggestions)
    {
        if (context.getSource() instanceof final CommandSourceStack source)
        {
            for (final IColony colony : IColonyManager.getInstance().getIColonies(source.getLevel()))
            {
                suggestions.add(String.valueOf(colony.getID()));
            }
        }
        else
        {
            // this is safe but Forge still gets upset with safeRun
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientOnly.suggestColonyIds(context.getSource(), suggestions));
        }
    }

    private static class ClientOnly
    {
        public static <S> void suggestColonyIds(final S source, final List<String> suggestions)
        {
            if (source instanceof ClientSuggestionProvider)
            {
                for (final IColony colony : IColonyManager.getInstance().getIColonies(Minecraft.getInstance().level))
                {
                    suggestions.add(String.valueOf(colony.getID()));
                }
            }
        }
    }

    private static int resolveHere(@NotNull final CommandSourceStack source) throws CommandSyntaxException
    {
        final IColony colony = IColonyManager.getInstance().getIColony(source.getLevel(), BlockPos.containing(source.getPosition()));
        if (colony == null)
        {
            throw ERROR_UNKNOWN_COLONY.create();
        }
        return colony.getID();
    }

    private static int resolveMine(@NotNull final CommandSourceStack source) throws CommandSyntaxException
    {
        return resolveTheirs(source, source.getPlayerOrException().getGameProfile().getId());
    }

    private static int resolveTheirs(@NotNull final CommandSourceStack source, @NotNull final String name) throws CommandSyntaxException
    {
        final Optional<GameProfile> profile = source.getServer().getProfileCache().get(name);
        if (profile.isPresent())
        {
            return resolveTheirs(source, profile.get().getId());
        }
        throw ERROR_UNKNOWN_PLAYER.create();
    }

    private static int resolveTheirs(@NotNull final CommandSourceStack source, @NotNull final UUID id) throws CommandSyntaxException
    {
        final IColony colony = IColonyManager.getInstance().getIColonyByOwner(source.getLevel(), id);
        if (colony == null)
        {
            throw ERROR_UNKNOWN_COLONY.create();
        }
        return colony.getID();
    }

    @FunctionalInterface
    public interface Result
    {
        int resolve(CommandSourceStack source) throws CommandSyntaxException;
    }
}
