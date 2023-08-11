package com.minecolonies.coremod.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Class to introduce custom stream collectors.
 */
public class CollectorUtils
{
    private static final Collector<?, ?, ?> SHUFFLER = Collectors.collectingAndThen(
      Collectors.toCollection(ArrayList::new),
      list -> {
          Collections.shuffle(list);
          return list;
      }
    );

    private CollectorUtils()
    {
    }

    @SuppressWarnings("unchecked")
    public static <T> Collector<T, ?, List<T>> toShuffledList()
    {
        return (Collector<T, ?, List<T>>) SHUFFLER;
    }
}