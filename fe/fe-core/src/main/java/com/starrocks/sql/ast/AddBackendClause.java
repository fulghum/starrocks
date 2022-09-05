// This file is licensed under the Elastic License 2.0. Copyright 2021-present, StarRocks Inc.

package com.starrocks.sql.ast;

import java.util.List;

public class AddBackendClause extends BackendClause {
    public AddBackendClause(List<String> hostPorts) {
        super(hostPorts);
    }
}