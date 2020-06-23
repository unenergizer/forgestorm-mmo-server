package com.forgestorm.server.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
class SqlSearchData {
    private final String tableName;
    private final String columnName;
    private final Object setData;
}