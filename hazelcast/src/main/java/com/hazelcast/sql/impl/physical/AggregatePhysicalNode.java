/*
 * Copyright (c) 2008-2019, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql.impl.physical;

import com.hazelcast.internal.serialization.impl.SerializationUtil;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.sql.impl.expression.aggregate.AggregateExpression;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Collocated aggregation.
 */
public class AggregatePhysicalNode extends UniInputPhysicalNode {
    /** Group key. */
    private List<Integer> groupKey;

    /** Accumulators. */
    private List<AggregateExpression> expressions;

    /** Whether group key is already sorted, and hence blocking behavior is not needed. */
    private int sortedGroupKeySize;

    public AggregatePhysicalNode() {
        // No-op.
    }

    public AggregatePhysicalNode(
        PhysicalNode upstream,
        List<Integer> groupKey,
        List<AggregateExpression> expressions,
        int sortedGroupKeySize
    ) {
        super(upstream);

        this.groupKey = groupKey;
        this.expressions = expressions;
        this.sortedGroupKeySize = sortedGroupKeySize;
    }

    public PhysicalNode getUpstream() {
        return upstream;
    }

    public List<Integer> getGroupKey() {
        return groupKey;
    }

    public List<AggregateExpression> getExpressions() {
        return expressions;
    }

    public int getSortedGroupKeySize() {
        return sortedGroupKeySize;
    }

    @Override
    public void visit(PhysicalNodeVisitor visitor) {
        upstream.visit(visitor);

        visitor.onAggregateNode(this);
    }

    @Override
    public void writeData0(ObjectDataOutput out) throws IOException {
        SerializationUtil.writeList(groupKey, out);
        SerializationUtil.writeList(expressions, out);
        out.writeInt(sortedGroupKeySize);
    }

    @Override
    public void readData0(ObjectDataInput in) throws IOException {
        groupKey = SerializationUtil.readList(in);
        expressions = SerializationUtil.readList(in);
        sortedGroupKeySize = in.readInt();
    }

    @Override
    public int hashCode() {
        return Objects.hash(upstream, groupKey, expressions, sortedGroupKeySize);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AggregatePhysicalNode that = (AggregatePhysicalNode) o;

        return upstream.equals(that.upstream) && Objects.equals(groupKey, that.groupKey)
            && expressions.equals(that.expressions) && sortedGroupKeySize == that.sortedGroupKeySize ;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{groupKey=" + groupKey + ", expressions=" + expressions
            + ", sortedGroupKeySize=" + sortedGroupKeySize + ", upstream=" + upstream + '}';
    }
}
