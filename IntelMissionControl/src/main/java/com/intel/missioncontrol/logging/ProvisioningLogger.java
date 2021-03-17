/**
 * Copyright (c) 2020 Intel Corporation
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.intel.missioncontrol.logging;

import com.google.common.collect.Lists;
import com.google.inject.ProvisionException;
import com.google.inject.spi.Message;
import com.google.inject.spi.ProvisionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvisioningLogger implements ProvisionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisioningLogger.class);

    private enum TriState {
        UNSET,
        FALSE,
        TRUE
    }

    private static class ProvisionNode {
        final Class<?> type;
        final String name;
        final ProvisionNode parent;
        final List<ProvisionNode> children = new ArrayList<>();
        TriState circularDependency = TriState.UNSET;
        TriState constructionState = TriState.UNSET;
        TriState exceptionRoot = TriState.UNSET;

        ProvisionNode(Class<?> type, ProvisionNode parent) {
            this.type = type;
            this.parent = parent;

            Class<?> enclosingClass = type.getEnclosingClass();
            if (enclosingClass != null) {
                name = enclosingClass.getSimpleName() + "." + type.getSimpleName();
            } else {
                name = type.getSimpleName();
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            format(builder, 0);
            return builder.toString();
        }

        private void format(StringBuilder stringBuilder, int level) {
            if (level > 0) {
                stringBuilder.append("\n");
            }

            if (parent != null) {
                stringBuilder.append("        ");
            }

            for (ProvisionNode dependency : getDependencies(this)) {
                stringBuilder.append(dependency.hasSibling() ? "│  " : "   ");
            }

            if (parent != null) {
                if (hasSibling()) {
                    stringBuilder.append("├──");
                } else {
                    encloseWithColor(stringBuilder, constructionState == TriState.FALSE ? "0;37" : null, "└──");
                }
            }

            if (circularDependency == TriState.TRUE) {
                stringBuilder.append(name);
                encloseWithColor(stringBuilder, "1;31", " <-- circular dependency");
            } else if (constructionState == TriState.UNSET) {
                if (exceptionRoot == TriState.TRUE) {
                    encloseWithColor(stringBuilder, "1;31", name);
                } else {
                    encloseWithColor(stringBuilder, "1;33", name);
                }
            } else if (constructionState == TriState.TRUE) {
                stringBuilder.append(name);
            } else {
                encloseWithColor(stringBuilder, "0;37", name);
            }

            for (ProvisionNode child : children) {
                child.format(stringBuilder, level + 1);
            }
        }

        private void encloseWithColor(StringBuilder stringBuilder, String color, String value) {
            if (color != null) {
                stringBuilder.append("\u001B[").append(color).append("m").append(value).append("\033[0;36m");
            } else {
                stringBuilder.append(value);
            }
        }

        private List<ProvisionNode> getDependencies(ProvisionNode node) {
            List<ProvisionNode> list = new ArrayList<>();
            node = node.parent;
            while (node != null) {
                list.add(node);
                node = node.parent;
            }

            if (!list.isEmpty()) {
                list.remove(list.size() - 1);
            }

            return Lists.reverse(list);
        }

        private boolean hasSibling() {
            if (parent == null) {
                return false;
            }

            List<ProvisionNode> siblings = parent.children;
            for (int i = 0; i < siblings.size(); ++i) {
                if (siblings.get(i) == this) {
                    return i < siblings.size() - 1;
                }
            }

            return false;
        }
    }

    private ThreadLocal<ProvisionNode> currentNode = ThreadLocal.withInitial(() -> null);
    private ThreadLocal<Set<Object>> instances = ThreadLocal.withInitial(HashSet::new);

    @Override
    public <T> void onProvision(ProvisionInvocation<T> invocation) {
        if (!Platform.isFxApplicationThread()) {
            invocation.provision();
            return;
        }

        Class<?> type = invocation.getBinding().getKey().getTypeLiteral().getRawType();

        ProvisionNode previousNode = currentNode.get();
        ProvisionNode cn = new ProvisionNode(type, previousNode);
        currentNode.set(cn);
        if (previousNode != null) {
            previousNode.children.add(cn);
        }

        try {
            T instance = invocation.provision();
            if (instances.get().add(instance)) {
                cn.constructionState = TriState.TRUE;
            } else {
                cn.constructionState = TriState.FALSE;
            }
        } catch (ProvisionException e) {
            if (cn.exceptionRoot == TriState.UNSET) {
                cn.exceptionRoot = TriState.TRUE;
            }

            if (cn.circularDependency == TriState.UNSET) {
                Message message = e.getErrorMessages().iterator().next();
                Throwable cause = message.getCause();
                if (cause != null) {
                    String msg = cause.getMessage();
                    if (msg != null && msg.contains("circular")) {
                        cn.circularDependency = TriState.TRUE;

                        ProvisionNode parent = cn.parent;
                        while (parent != null) {
                            parent.circularDependency = TriState.FALSE;
                            parent = parent.parent;
                        }
                    }
                }
            }

            throw e;
        } finally {
            if (previousNode == null) {
                instances.get().clear();
                LOGGER.debug(cn.toString());
            }

            if (cn.exceptionRoot != TriState.UNSET) {
                previousNode.exceptionRoot = TriState.FALSE;
            }

            currentNode.set(previousNode);
        }
    }

}
