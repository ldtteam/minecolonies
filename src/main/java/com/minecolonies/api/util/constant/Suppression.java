package com.minecolonies.api.util.constant;

import org.jetbrains.annotations.NonNls;

/**
 * Constants for suppression keys.
 */
public final class Suppression
{
    /**
     * Suppress warnings for unchecked type conversions.
     * <p>
     * We sometimes need this for complicated typings.
     */
    @NonNls
    public static final String UNCHECKED = "unchecked";

    /**
     * Suppress warnings for raw type annotation.
     * <p>
     * We sometimes need this for complicated typings.
     */
    @NonNls
    public static final String RAWTYPES = "rawtypes";

    /**
     * Suppress warnings for deprecations.
     * <p>
     * We sometimes need this for minecraft methods we have to keep support for.
     */
    @NonNls
    public static final String DEPRECATION = "deprecation";

    /**
     * We sometimes suppress this to ignore irrelevant error messages.
     * <p>
     * Use this sparely!
     */
    @NonNls
    public static final String EXCEPTION_HANDLERS_SHOULD_PRESERVE_THE_ORIGINAL_EXCEPTIONS = "squid:S1166";

    /**
     * We sometimes suppress this because we map static things.
     * <p>
     * This is not ideal but needed sometimes.
     */
    @NonNls
    public static final String CLASSES_SHOULD_NOT_ACCESS_STATIC_MEMBERS_OF_THEIR_OWN_SUBCLASSES_DURING_INITIALIZATION = "squid:S2390";

    /**
     * We sometimes suppress this because sonar does not detect this.
     * <p>
     * Overriding methods need the parameter.
     */
    @NonNls
    public static final String UNUSED_METHOD_PARAMETERS_SHOULD_BE_REMOVED = "squid:S1172";

    /**
     * We sometimes suppress this because we think this is more readable.
     * <p>
     * Use this sparely!
     */
    @NonNls
    public static final String INCREMENT_AND_DECREMENT_OPERATORS_SHOULD_NOT_BE_USED_IN_A_METHOD_CALL_OR_MIXED_WITH_OTHER_OPERATORS_IN_AN_EXPRESSION = "squid:S881";

    /**
     * We sometimes suppress this because we use number literals in switch statements.
     * <p>
     * Use this sparely!
     */
    @NonNls
    public static final String MAGIC_NUMBERS_SHOULD_NOT_BE_USED = "squid:S109";

    /**
     * We sometimes suppress this because it would be silly to split up the classes.
     * <p>
     * Use this sparely!
     */
    @NonNls
    public static final String BIG_CLASS = "squid:S2972";

    /**
     * Sometimes classes are used in many places.
     */
    @NonNls
    public static final String SPLIT_CLASS = "squid:S1200";

    /**
     * Sometimes it would decrease the readability of the code.
     * <p>
     * Use this sparely!
     */
    @NonNls
    public static final String LEFT_CURLY_BRACE = "squid:LeftCurlyBraceStartLineCheck";

    /**
     * Yeah generics are complicated. No reason to not use fully though.
     */
    @NonNls
    public static final String GENERIC_WILDCARD = "squid:S1452";

    /**
     * Sometimes we don't need to override the equals of subclasses. (Building IDS are unique enough for equals for example).
     */
    @NonNls
    public static final String OVERRIDE_EQUALS = "squid:S2160";

    /**
     * Sometimes it is just needed to have many returns so the code is easily readable.
     */
    @NonNls
    public static final String TOO_MANY_RETURNS = "squid:S1142";

    private Suppression()
    {
        //empty default
    }
}
