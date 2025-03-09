/*
 * Copyright (C) 2024 Parisi Alessandro - alessandro.parisi406@gmail.com
 * This file is part of ArchitectFX (https://github.com/palexdev/ArchitectFX)
 *
 * ArchitectFX is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * ArchitectFX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArchitectFX. If not, see <http://www.gnu.org/licenses/>.
 */

package io.github.palexdev.architectfx.examples.utils;


import java.util.*;
import java.util.function.Predicate;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class RefineList<T> implements ObservableList<T> {
    //================================================================================
    // Properties
    //================================================================================
    private final ObservableList<T> src;
    private final FilteredList<T> filtered;
    private final SortedList<T> sorted;

    //================================================================================
    // Constructors
    //================================================================================
    public RefineList(ObservableList<T> src) {
        this.src = src;
        filtered = new FilteredList<>(src);
        sorted = new SortedList<>(filtered);
    }

    //================================================================================
    // Delegate Methods
    //================================================================================
    public void setPredicate(Predicate<T> predicate) {
        filtered.setPredicate(predicate);
    }

    public void setComparator(Comparator<T> comparator) {
        sorted.setComparator(comparator);
    }

    // Observability (on the "last view")
    @Override
    public void addListener(ListChangeListener<? super T> listener) {
        sorted.addListener(listener);
    }

    @Override
    public void removeListener(ListChangeListener<? super T> listener) {
        sorted.removeListener(listener);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        sorted.addListener(listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        sorted.removeListener(listener);
    }

    // Basic List Operations
    @Override
    public boolean add(T t) {
        return src.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return src.remove(o);
    }

    @Override
    public void clear() {
        src.clear();
    }

    @Override
    public int size() {
        return src.size();
    }

    @Override
    public boolean isEmpty() {
        return src.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return src.contains(o);
    }

    // Bulk Operations
    @Override
    public boolean addAll(T... elements) {
        return src.addAll(elements);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return src.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return src.addAll(index, c);
    }

    @Override
    public boolean setAll(T... elements) {
        return src.setAll(elements);
    }

    @Override
    public boolean setAll(Collection<? extends T> col) {
        return src.setAll(col);
    }

    @Override
    public boolean removeAll(T... elements) {
        return src.removeAll(elements);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return src.removeAll(c);
    }

    @Override
    public boolean retainAll(T... elements) {
        return src.retainAll(elements);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return src.retainAll(c);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return src.containsAll(c);
    }

    // Indexed Operations
    @Override
    public T get(int index) {
        return src.get(index);
    }

    @Override
    public T set(int index, T element) {
        return src.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        src.add(index, element);
    }

    @Override
    public T remove(int index) {
        return src.remove(index);
    }

    @Override
    public void remove(int from, int to) {
        src.remove(from, to);
    }

    // Positional Search
    @Override
    public int indexOf(Object o) {
        return src.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return src.lastIndexOf(o);
    }

    // Iterators
    @Override
    public Iterator<T> iterator() {
        return src.iterator();
    }

    @Override
    public ListIterator<T> listIterator() {
        return src.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return src.listIterator(index);
    }

    // Sublist Operations
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return src.subList(fromIndex, toIndex);
    }

    // Array Conversions
    @Override
    public Object[] toArray() {
        return src.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return src.toArray(a);
    }

    //================================================================================
    // Getters
    //================================================================================
    public ObservableList<T> getSrc() {
        return src;
    }

    public ObservableList<T> getView() {
        return sorted;
    }
}
