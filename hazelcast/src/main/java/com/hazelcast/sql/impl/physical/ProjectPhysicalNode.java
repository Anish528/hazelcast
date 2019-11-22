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
import com.hazelcast.sql.impl.expression.Expression;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Projection.
 */
public class ProjectPhysicalNode extends UniInputPhysicalNode {
    /** Projections. */
    private List<Expression> projects;

    public ProjectPhysicalNode() {
        // No-op.
    }

    public ProjectPhysicalNode(PhysicalNode upstream, List<Expression> projects) {
        super(upstream);

        this.projects = projects;
    }

    public List<Expression> getProjects() {
        return projects;
    }

    @Override
    public void visit(PhysicalNodeVisitor visitor) {
        upstream.visit(visitor);

        visitor.onProjectNode(this);
    }

    @Override
    public void writeData0(ObjectDataOutput out) throws IOException {
        SerializationUtil.writeList(projects, out);
    }

    @Override
    public void readData0(ObjectDataInput in) throws IOException {
        projects = SerializationUtil.readList(in);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upstream, projects);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectPhysicalNode that = (ProjectPhysicalNode) o;

        return upstream.equals(that.upstream) && projects.equals(that.projects);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{projects=" + projects + ", upstream=" + upstream + '}';
    }
}
