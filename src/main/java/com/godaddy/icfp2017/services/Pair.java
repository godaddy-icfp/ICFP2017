package com.godaddy.icfp2017.services;

import com.google.common.base.Objects;

final class Pair<T1, T2> {
  final T1 left;
  final T2 right;

  Pair(final T1 left, final T2 right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equal(left, pair.left) &&
        Objects.equal(right, pair.right);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(left, right);
  }

  public static <T1, T2> Pair<T1, T2> of(
      final T1 left,
      final T2 right) {
    return new Pair<>(left, right);
  }
}
