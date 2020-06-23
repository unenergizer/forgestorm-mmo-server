package com.forgestorm.server.database.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
class VariantData<T> {
    private List<T> searchObjects;
    private String searchString;
}
