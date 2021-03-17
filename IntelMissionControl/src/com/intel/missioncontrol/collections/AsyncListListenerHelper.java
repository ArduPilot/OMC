/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.collections;

import com.intel.missioncontrol.PublishSource;
import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

@PublishSource(
    module = "openjfx",
    licenses = {"openjfx", "intel-gpl-classpath-exception"}
)
abstract class AsyncListListenerHelper<E> extends ExpressionHelperBase {

    public static <E> AsyncListListenerHelper<E> addListener(
            AsyncListListenerHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleInvalidation<>(listener) : helper.addListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> removeListener(
            AsyncListListenerHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> addListener(
            AsyncListListenerHelper<E> helper, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? new SingleChange<>(listener) : helper.addListener(listener);
    }

    public static <E> AsyncListListenerHelper<E> removeListener(
            AsyncListListenerHelper<E> helper, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }

        return (helper == null) ? null : helper.removeListener(listener);
    }

    public static <E> void fireValueChangedEvent(
            AsyncListListenerHelper<E> helper, ListChangeListener.Change<? extends E> change) {
        if (helper != null) {
            change.reset();
            helper.fireValueChangedEvent(change);
        }
    }

    static <E> boolean hasListeners(AsyncListListenerHelper<E> helper) {
        return helper != null;
    }

    protected abstract AsyncListListenerHelper<E> addListener(InvalidationListener listener);

    protected abstract AsyncListListenerHelper<E> removeListener(InvalidationListener listener);

    protected abstract AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener);

    protected abstract AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener);

    protected abstract void fireValueChangedEvent(ListChangeListener.Change<? extends E> change);

    private static class SingleInvalidation<E> extends AsyncListListenerHelper<E> {

        private final InvalidationListener listener;

        private SingleInvalidation(InvalidationListener listener) {
            this.listener = listener;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            try {
                if (change instanceof AsyncListChangeBuilder.SingleChange) {
                    listener.invalidated(((AsyncListChangeBuilder.SingleChange<? extends E>)change).getListInternal());
                } else if (change instanceof AsyncListChangeBuilder.IterableChange) {
                    listener.invalidated(
                        ((AsyncListChangeBuilder.IterableChange<? extends E>)change).getListInternal());
                } else if (change instanceof AsyncSourceAdapterChange) {
                    listener.invalidated(((AsyncSourceAdapterChange<? extends E>)change).getListInternal());
                } else {
                    throw new IllegalArgumentException("Unsupported change type.");
                }
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleChange<E> extends AsyncListListenerHelper<E> {

        private final ListChangeListener<? super E> listener;

        private SingleChange(ListChangeListener<? super E> listener) {
            this.listener = listener;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(listener, this.listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(this.listener, listener);
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (this.listener == null) {
                return this;
            }

            return this.listener.equals(listener) ? null : this;
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            try {
                listener.onChanged(change);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class Generic<E> extends AsyncListListenerHelper<E> {

        private InvalidationListener[] invalidationListeners;
        private ListChangeListener<? super E>[] changeListeners;
        private int invalidationSize;
        private int changeSize;
        private boolean locked;

        private Generic(InvalidationListener listener0, InvalidationListener listener1) {
            this.invalidationListeners = new InvalidationListener[] {listener0, listener1};
            this.invalidationSize = 2;
        }

        private Generic(ListChangeListener<? super E> listener0, ListChangeListener<? super E> listener1) {
            this.changeListeners = new ListChangeListener[] {listener0, listener1};
            this.changeSize = 2;
        }

        private Generic(InvalidationListener invalidationListener, ListChangeListener<? super E> changeListener) {
            this.invalidationListeners = new InvalidationListener[] {invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ListChangeListener[] {changeListener};
            this.changeSize = 1;
        }

        @Override
        protected Generic<E> addListener(InvalidationListener listener) {
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[] {listener};
                invalidationSize = 1;
            } else {
                final int oldCapacity = invalidationListeners.length;
                if (locked) {
                    final int newCapacity = (invalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                } else if (invalidationSize == oldCapacity) {
                    invalidationSize = trim(invalidationSize, invalidationListeners);
                    if (invalidationSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                    }
                }

                invalidationListeners[invalidationSize++] = listener;
            }

            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (invalidationListeners[index].equals(listener)) {
                        if (invalidationSize == 1) {
                            if (changeSize == 1) {
                                return new SingleChange<>(changeListeners[0]);
                            }

                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0)) {
                            return new SingleInvalidation<>(invalidationListeners[1 - index]);
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            final InvalidationListener[] oldListeners = invalidationListeners;
                            if (locked) {
                                invalidationListeners = new InvalidationListener[invalidationListeners.length];
                                System.arraycopy(oldListeners, 0, invalidationListeners, 0, index);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, invalidationListeners, index, numMoved);
                            }

                            invalidationSize--;
                            if (!locked) {
                                invalidationListeners[invalidationSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            if (changeListeners == null) {
                changeListeners = new ListChangeListener[] {listener};
                changeSize = 1;
            } else {
                final int oldCapacity = changeListeners.length;
                if (locked) {
                    final int newCapacity = (changeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                } else if (changeSize == oldCapacity) {
                    changeSize = trim(changeSize, changeListeners);
                    if (changeSize == oldCapacity) {
                        final int newCapacity = (oldCapacity * 3) / 2 + 1;
                        changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                    }
                }

                changeListeners[changeSize++] = listener;
            }

            return this;
        }

        @Override
        protected AsyncListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (changeListeners[index].equals(listener)) {
                        if (changeSize == 1) {
                            if (invalidationSize == 1) {
                                return new SingleInvalidation<>(invalidationListeners[0]);
                            }

                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0)) {
                            return new SingleChange<>(changeListeners[1 - index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            final ListChangeListener<? super E>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ListChangeListener[changeListeners.length];
                                System.arraycopy(oldListeners, 0, changeListeners, 0, index);
                            }

                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, changeListeners, index, numMoved);
                            }

                            changeSize--;
                            if (!locked) {
                                changeListeners[changeSize] = null; // Let gc do its work
                            }
                        }

                        break;
                    }
                }
            }

            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ListChangeListener<? super E>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;
                ObservableList<? extends E> list;
                if (change instanceof AsyncListChangeBuilder.SingleChange) {
                    list = ((AsyncListChangeBuilder.SingleChange<? extends E>)change).getListInternal();
                } else if (change instanceof AsyncListChangeBuilder.IterableChange) {
                    list = ((AsyncListChangeBuilder.IterableChange<? extends E>)change).getListInternal();
                } else {
                    throw new IllegalArgumentException("Unsupported change type.");
                }

                for (int i = 0; i < curInvalidationSize; i++) {
                    try {
                        curInvalidationList[i].invalidated(list);
                    } catch (Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }

                for (int i = 0; i < curChangeSize; i++) {
                    change.reset();
                    try {
                        curChangeList[i].onChanged(change);
                    } catch (Exception e) {
                        Thread.currentThread()
                            .getUncaughtExceptionHandler()
                            .uncaughtException(Thread.currentThread(), e);
                    }
                }
            } finally {
                locked = false;
            }
        }
    }

}
