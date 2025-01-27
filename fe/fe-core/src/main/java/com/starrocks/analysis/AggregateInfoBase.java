// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// This file is based on code available under the Apache license here:
//   https://github.com/apache/incubator-doris/blob/master/fe/fe-core/src/main/java/org/apache/doris/analysis/AggregateInfoBase.java

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.starrocks.analysis;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Base class for AggregateInfo and AnalyticInfo containing the intermediate and output
 * tuple descriptors as well as their smaps for evaluating aggregate functions.
 */
public abstract class AggregateInfoBase {
    private static final Logger LOG =
            LoggerFactory.getLogger(AggregateInfoBase.class);

    // For aggregations: All unique grouping expressions from a select block.
    // For analytics: Empty.
    protected ArrayList<Expr> groupingExprs_;

    // For aggregations: All unique aggregate expressions from a select block.
    // For analytics: The results of AnalyticExpr.getFnCall() for the unique
    // AnalyticExprs of a select block.
    protected ArrayList<FunctionCallExpr> aggregateExprs_;

    // The tuple into which the intermediate output of an aggregation is materialized.
    // Contains groupingExprs.size() + aggregateExprs.size() slots, the first of which
    // contain the values of the grouping exprs, followed by slots into which the
    // aggregateExprs' update()/merge() symbols materialize their output, i.e., slots
    // of the aggregate functions' intermediate types.
    // Identical to outputTupleDesc_ if no aggregateExpr has an output type that is
    // different from its intermediate type.
    protected TupleDescriptor intermediateTupleDesc_;

    // The tuple into which the final output of the aggregation is materialized.
    // Contains groupingExprs.size() + aggregateExprs.size() slots, the first of which
    // contain the values of the grouping exprs, followed by slots into which the
    // aggregateExprs' finalize() symbol write its result, i.e., slots of the aggregate
    // functions' output types.
    protected TupleDescriptor outputTupleDesc_;

    // For aggregation: indices into aggregate exprs for that need to be materialized
    // For analytics: indices into the analytic exprs and their corresponding aggregate
    // exprs that need to be materialized.
    // Populated in materializeRequiredSlots() which must be implemented by subclasses.
    protected ArrayList<Integer> materializedAggSlots = Lists.newArrayList();

    protected AggregateInfoBase(ArrayList<Expr> groupingExprs,
                                ArrayList<FunctionCallExpr> aggExprs) {
        Preconditions.checkState(groupingExprs != null || aggExprs != null);
        groupingExprs_ =
                groupingExprs != null ? Expr.cloneList(groupingExprs) : new ArrayList<Expr>();
        aggregateExprs_ =
                aggExprs != null ? Expr.cloneList(aggExprs) : new ArrayList<FunctionCallExpr>();
    }

    /**
     * C'tor for cloning.
     */
    protected AggregateInfoBase(AggregateInfoBase other) {
        groupingExprs_ =
                (other.groupingExprs_ != null) ? Expr.cloneList(other.groupingExprs_) : null;
        aggregateExprs_ =
                (other.aggregateExprs_ != null) ? Expr.cloneList(other.aggregateExprs_) : null;
        intermediateTupleDesc_ = other.intermediateTupleDesc_;
        outputTupleDesc_ = other.outputTupleDesc_;
        materializedAggSlots = Lists.newArrayList(other.materializedAggSlots);
    }

    public ArrayList<Expr> getGroupingExprs() {
        return groupingExprs_;
    }

    public ArrayList<FunctionCallExpr> getAggregateExprs() {
        return aggregateExprs_;
    }

    public TupleId getIntermediateTupleId() {
        return intermediateTupleDesc_.getId();
    }

    public TupleId getOutputTupleId() {
        return outputTupleDesc_.getId();
    }

    public void setOutputTupleDesc(TupleDescriptor tupleDesc) {
        this.outputTupleDesc_ = tupleDesc;
    }

    public boolean requiresIntermediateTuple() {
        Preconditions.checkNotNull(intermediateTupleDesc_);
        Preconditions.checkNotNull(outputTupleDesc_);
        return intermediateTupleDesc_ != outputTupleDesc_;
    }

    public String debugString() {
        StringBuilder out = new StringBuilder();
        out.append(MoreObjects.toStringHelper(this)
                .add("grouping_exprs", Expr.debugString(groupingExprs_))
                .add("aggregate_exprs", Expr.debugString(aggregateExprs_))
                .add("intermediate_tuple", (intermediateTupleDesc_ == null)
                        ? "null" : intermediateTupleDesc_.debugString())
                .add("output_tuple", (outputTupleDesc_ == null)
                        ? "null" : outputTupleDesc_.debugString())
                .toString());
        return out.toString();
    }

    protected abstract String tupleDebugName();
}
