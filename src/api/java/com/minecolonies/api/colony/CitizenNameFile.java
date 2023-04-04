package com.minecolonies.api.colony;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The citizen name file of a specific style of names.
 */
public class CitizenNameFile
{
    /**
     * Current types of name order.
     */
    public enum NameOrder
    {
        EASTERN,
        WESTERN
    }

    /**
     * Number of parts the name consists of (usually 3 with middle initial or 2 without).
     */
    public int parts;

    /**
     * The order of the name (initially eastern and western).
     */
    public NameOrder order;

    /**
     * List of male first names.
     */
    public List<String> maleFirstNames;

    /**
     * List of female first names.
     */
    public List<String> femalefirstNames;

    /**
     * List of surnames.
     */
    public List<String> surnames;

    /**
     * Create a new instance of a specific name file.
     * @param parts the number of parts.
     * @param order the name order.
     * @param maleFirstNames the male first names.
     * @param femaleFirstNames the female first names.
     * @param surnames the surnames.
     */
    public CitizenNameFile(
      final int parts,
      @NotNull final NameOrder order,
      @NotNull final List<String> maleFirstNames,
      @NotNull final List<String> femaleFirstNames,
      @NotNull final List<String> surnames)
    {
        this.parts = parts;
        this.order = order;
        this.maleFirstNames = maleFirstNames;
        this.femalefirstNames = femaleFirstNames;
        this.surnames = surnames;
    }
}
