package com.kamilpm.zero_waste.common.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OwnMapper {
  // public static <S, T> T map(Supplier<S> source, Function<S, T> mapper) {
  // return mapper.apply(source.get());
  // }

  public static <I, O> Optional<O> mapOptional(
      Supplier<Optional<I>> supplier,
      Function<I, O> mapper) {
    return supplier.get().map(mapper);
  }

  public static <I, O> Optional<O> mapOptional(Optional<I> value, Function<I, O> mapper) {
    return value.map(mapper);
  }

  public static <I, O> O map(I input, Function<I, O> mapper) {
    return mapper.apply(input);
  }

  public static <I, O> List<O> mapList(
      Collection<I> values,
      Function<I, O> mapper) {
    return values.stream()
        .map(mapper)
        .toList();
  }

  // public static <I, O> Map<UUID, O> mapToMap(Collection<I> items, Function<I,
  // UUID> keyMapper,
  // Function<I, O> valueMapper) {
  // return items.stream().collect(Collectors.toMap(keyMapper, valueMapper));
  // }
  public static <K, I, O> Map<K, O> mapValues(Map<K, I> map, Function<I, O> mapper) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> mapper.apply(entry.getValue())));
  }

}
