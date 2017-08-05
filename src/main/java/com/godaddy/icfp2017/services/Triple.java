package com.godaddy.icfp2017.services;

import com.google.common.base.Objects;

final class Triple<T1, T2, T3> {
    final T1 left;
    final T2 middle;
    final T3 right;

    private Triple(final T1 left, final T2 middle, final T3 right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return Objects.equal(left, triple.left) &&
                Objects.equal(middle, triple.middle) &&
                Objects.equal(right, triple.right);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(left, right);
    }

    static <T1, T2, T3> Triple<T1, T2, T3> of(
        final T1 left,
        final T2 middle,
        final T3 right) {
        return new Triple<>(left, middle, right);
    }
}
