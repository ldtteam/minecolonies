package com.minecolonies.core.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLLoader;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Base argument class that can serve to handle multiple different option values.
 * <p>
 * You pass one more {@link ArgumentOption} instances into the constructor.
 * The parser will then run over these options, in the specified order, and determine which option matches the argument value first.
 * <p>
 * Make sure the order can actually be respected in a logical way, with the outermost fallback option at the last position.
 * For example:
 * <ol>
 *     <li>Match against a specific keyword</li>
 *     <li>Match against a known identifier (i.e. a number or UUID)</li>
 *     <li>Match against any other string value</li>
 * </ol>
 * If not a single option matches the argument value, the parser will automatically fail with the missing selector warning.
 *
 * @param <TValue> the generic type of the value that the parser should return (this must be shared across all options).
 */
public abstract class MultipleOptionsArgument<TValue> implements ArgumentType<MultipleOptionsArgument.OptionContainer<TValue>>
{
    /**
     * Container class carrying the chosen option + original input value to the resolver method.
     *
     * @param option   the chosen option in the parser.
     * @param value    the original input value originating from the parser.
     * @param <TValue> the generic type of the option parser.
     */
    public record OptionContainer<TValue>(
        ArgumentOption<TValue> option,
        String value)
    {}

    /**
     * The list of all argument options specified.
     */
    private final List<ArgumentOption<TValue>> allowedOptions;

    /**
     * Default constructor.
     *
     * @param allowedOptions the list of all argument options specified.
     */
    protected MultipleOptionsArgument(final List<ArgumentOption<TValue>> allowedOptions)
    {
        this.allowedOptions = allowedOptions;
    }

    /**
     * Get the underlying value by running the resolver method on the chosen option parser.
     *
     * @param context  the command context.
     * @param name     the name of the argument.
     * @param <TValue> the generic type of the option parser.
     * @return the resolved value from the option.
     * @throws CommandSyntaxException when any of the resolvers fail to resolve the finalized value.
     */
    @SuppressWarnings("unchecked")
    protected static <TValue> TValue getValue(final CommandContext<CommandSourceStack> context, final String name) throws CommandSyntaxException
    {
        final OptionContainer<TValue> container = context.getArgument(name, OptionContainer.class);
        return container.option.resolveValue(context.getSource(), container.value);
    }

    @Override
    public final OptionContainer<TValue> parse(final StringReader reader) throws CommandSyntaxException
    {
        if (reader.canRead())
        {
            final String argumentValue;
            if (reader.peek() == '@')
            {
                reader.skip();
                argumentValue = "@" + reader.readString();
            }
            else
            {
                argumentValue = reader.readString();
            }
            for (final ArgumentOption<TValue> allowedOption : allowedOptions)
            {
                if (allowedOption.matches(argumentValue))
                {
                    return new OptionContainer<>(allowedOption, argumentValue);
                }
            }
            throw EntitySelectorParser.ERROR_UNKNOWN_SELECTOR_TYPE.createWithContext(reader, argumentValue);
        }

        throw EntitySelectorParser.ERROR_MISSING_SELECTOR_TYPE.createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder)
    {
        if (context.getSource() instanceof final CommandSourceStack source)
        {
            for (final ArgumentOption<TValue> allowedOption : allowedOptions)
            {
                allowedOption.createSuggestions(source.getLevel(), source, builder);
            }
        }
        else if (FMLLoader.getDist().isClient())
        {
            ClientSuggester.loadClientSuggestions(context.getSource(), builder, allowedOptions);
        }

        return builder.buildFuture();
    }

    /**
     * A singular argument option, this is responsible for checking if the argument value matches the expected user input.
     * This class is also responsible for resolving the finalized value after the matching succeeded.
     *
     * @param <TValue> the generic type of the option parser.
     */
    public interface ArgumentOption<TValue>
    {
        /**
         * Check if the user defined input matches what this specific argument expects.
         * If this method returns false the process will transfer to the next option.
         *
         * @param value the original user input.
         * @return true if the option matches the user input, false if not.
         */
        boolean matches(final String value);

        /**
         * Resolves a value from the command source during post execution of the command.
         *
         * @param source the command source stack.
         * @param value  the original user input.
         * @return the finalized value that the specific implementation expects.
         * @throws CommandSyntaxException when the resolver fails to resolve the finalized value.
         */
        TValue resolveValue(final CommandSourceStack source, final String value) throws CommandSyntaxException;

        /**
         * Build any potential suggestions that this option can list for the argument preview when typing.
         *
         * @param world              the world the command is being typed/executed in.
         * @param suggestionProvider the suggestion provider, allowing access to excess suggestion data.
         * @param builder            the suggestion builder.
         */
        void createSuggestions(final Level world, final SharedSuggestionProvider suggestionProvider, final SuggestionsBuilder builder);
    }

    /**
     * Special client side class to prevent side aware classloading issues.
     */
    private static class ClientSuggester
    {
        /**
         * Load the suggestions for the client side suggestion provider.
         *
         * @param source             The command context source.
         * @param builder            the suggestion builder.
         * @param allowedOptions     the list of allowed options.
         * @param <TValue>           the generic argument of the option value.
         */
        private static <TValue> void loadClientSuggestions(
            final Object source,
            final SuggestionsBuilder builder,
            final List<MultipleOptionsArgument.ArgumentOption<TValue>> allowedOptions)
        {
            if (source instanceof ClientSuggestionProvider clientSuggestionProvider)
            {
                for (final ArgumentOption<TValue> allowedOption : allowedOptions)
                {
                    allowedOption.createSuggestions(Minecraft.getInstance().level, clientSuggestionProvider, builder);
                }
            }
        }
    }
}
