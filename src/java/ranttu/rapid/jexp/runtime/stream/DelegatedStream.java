/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package ranttu.rapid.jexp.runtime.stream;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * delegated jexp stream
 *
 * @author rapid
 * @version : DelegatedStream.java, v 0.1 2020-11-09 10:17 PM rapid Exp $
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DelegatedStream<T> implements Stream<T> {
    /**
     * inner wrapped stream
     */
    private Stream<T> cachedStream = null;

    private final Supplier<Stream<T>> streamSupplier;

    /**
     * create a new jexp stream
     */
    public <S> DelegatedStream<S> of(Stream<S> stream) {
        if (stream instanceof DelegatedStream) {
            return (DelegatedStream<S>) stream;
        } else {
            return new DelegatedStream<>(() -> stream);
        }
    }

    private Stream<T> cached() {
        if (cachedStream != null) {
            return cachedStream;
        } else {
            return (cachedStream = streamSupplier.get());
        }
    }

    //~~~ delegate to Stream<T>
    @Override
    public Stream<T> filter(Predicate<? super T> predicate) {
        return cached().filter(predicate);
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return cached().map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return cached().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return cached().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return cached().mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return cached().flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return cached().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return cached().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return cached().flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return cached().distinct();
    }

    @Override
    public Stream<T> sorted() {
        return cached().sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return cached().sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return cached().peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return cached().limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return cached().skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        cached().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        cached().forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return cached().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        //noinspection SuspiciousToArrayCall
        return cached().toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return cached().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return cached().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return cached().reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return cached().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return cached().collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return cached().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return cached().max(comparator);
    }

    @Override
    public long count() {
        return cached().count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return cached().anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return cached().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return cached().noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return cached().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return cached().findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return cached().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return cached().spliterator();
    }

    @Override
    public boolean isParallel() {
        return cached().isParallel();
    }

    @Override
    public Stream<T> sequential() {
        return cached().sequential();
    }

    @Override
    public Stream<T> parallel() {
        return cached().parallel();
    }

    @Override
    public Stream<T> unordered() {
        return cached().unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return cached().onClose(closeHandler);
    }

    @Override
    public void close() {
        cached().close();
    }
}