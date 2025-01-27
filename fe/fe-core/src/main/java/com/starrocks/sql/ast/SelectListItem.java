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


package com.starrocks.sql.ast;

import com.google.common.base.Preconditions;
import com.starrocks.analysis.Expr;
import com.starrocks.analysis.ParseNode;
import com.starrocks.analysis.TableName;

public class SelectListItem implements ParseNode {
    private Expr expr;
    // for "[name.]*"
    private final TableName tblName;
    private final boolean isStar;
    private String alias;

    public SelectListItem(Expr expr, String alias) {
        super();
        Preconditions.checkNotNull(expr);
        this.expr = expr;
        this.alias = alias;
        this.tblName = null;
        this.isStar = false;
    }

    public SelectListItem(TableName tblName) {
        super();
        this.expr = null;
        this.tblName = tblName;
        this.isStar = true;
    }

    protected SelectListItem(SelectListItem other) {
        if (other.expr == null) {
            expr = null;
        } else {
            expr = other.expr.clone().reset();
        }
        tblName = other.tblName;
        isStar = other.isStar;
        alias = other.alias;
    }

    @Override
    public SelectListItem clone() {
        return new SelectListItem(this);
    }

    public boolean isStar() {
        return isStar;
    }

    public TableName getTblName() {
        return tblName;
    }

    public Expr getExpr() {
        return expr;
    }

    public void setExpr(Expr expr) {
        this.expr = expr;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
