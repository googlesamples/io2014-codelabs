/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.backend.core;

import com.google.cloud.backend.mobilebackend.model.FilterDto;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A filter class for a {@link com.google.cloud.backend.core.CloudQuery}. See
 * {@link CloudBackendTest#testList()} of CloudBackendAndroidClientTest project
 * for detailed usage.
 */
public class Filter {

    /**
     * Enum class for Filter operations.
     * <p>
     * EQ equal
     * LT less than
     * LE less than or equal to
     * GT greater than
     * GE greater
     * than or equal to
     * NE not equal
     * IN in
     * AND and
     * OR or
     */
    public enum Op {
        EQ, LT, LE, GT, GE, NE, IN, AND, OR
    }

    private FilterDto filterDto = new FilterDto();

    /**
     * Creates a {@link Filter} for EQUAL operation.
     *
     * @param propertyName Name of the target property.
     * @param value Value for this operation.
     * @return {@link Filter} for this operation.
     */
    public static Filter eq(String propertyName, Object value) {
        return createFilter(Op.EQ.name(), propertyName, value);
    }

    /**
     * Creates a {@link Filter} for LESS_THAN operation.
     *
     * @param propertyName Name of the target property.
     * @param value Value for this operation.
     * @return {@link Filter} for this operation.
     */
    public static Filter lt(String propertyName, Object value) {
        return createFilter(Op.LT.name(), propertyName, value);
    }

    /**
     * Creates a {@link Filter} for LESS_THAN_EQUAL operation.
     *
     * @param propertyName Name of the target property.
     * @param value Value for this operation.
     * @return {@link Filter} for this operation.
     */
    public static Filter le(String propertyName, Object value) {
        return createFilter(Op.LE.name(), propertyName, value);
    }

    /**
     * Creates a {@link Filter} for GREATER_THAN operation.
     *
     * @param propertyName Name of the target property.
     * @param value Value for this operation.
     * @return {@link com.google.cloud.backend.core.Filter} for this operation.
     */
    public static Filter gt(String propertyName, Object value) {
        return createFilter(Op.GT.name(), propertyName, value);
    }

    /**
     * Creates a {@link com.google.cloud.backend.core.Filter} for GREATER_THAN_EQUAL operation.
     *
     * @param propertyName Name of the target property.
     * @param value Value for this operation.
     * @return {@link com.google.cloud.backend.core.Filter} for this operation.
     */
    public static Filter ge(String propertyName, Object value) {
        return createFilter(Op.GE.name(), propertyName, value);
    }

    /**
     * Creates a {@link com.google.cloud.backend.core.Filter} for NOT_EQUAL operation.
     *
     * @param propertyName Name of the target property.
     * @param value Value for this operation.
     * @return {@link com.google.cloud.backend.core.Filter} for this operation.
     */
    public static Filter ne(String propertyName, Object value) {
        return createFilter(Op.NE.name(), propertyName, value);
    }

    /**
     * Creates a {@link com.google.cloud.backend.core.Filter} for IN operation.
     *
     * @param propertyName Name of the target property.
     * @param values any number of {@link Object}s for the IN operation.
     * @return {@link com.google.cloud.backend.core.Filter} for this operation.
     */
    public static Filter in(String propertyName, List<Object> values) {
        LinkedList<Object> l = new LinkedList<Object>(values);
        l.addFirst(propertyName);
        Filter f = new Filter();
        f.filterDto.setOperator(Op.IN.name());
        f.filterDto.setValues(l);
        return f;
    }

    /**
     * Creates a {@link com.google.cloud.backend.core.Filter} for IN operation.
     *
     * @param propertyName Name of the target property.
     * @param values any number of {@link Object}s for the IN operation.
     * @return {@link com.google.cloud.backend.core.Filter} for this operation.
     */
    public static Filter in(String propertyName, Object... values) {
        LinkedList<Object> l = new LinkedList<Object>(Arrays.asList(values));
        l.addFirst(propertyName);
        Filter f = new Filter();
        f.filterDto.setOperator(Op.IN.name());
        f.filterDto.setValues(l);
        return f;
    }

    /**
     * Creates a {@link Filter} for AND operation.
     *
     * @param filters Any number of {@link Filter}s for this operation.
     * @return {@link Filter} for this operation.
     */
    public static Filter and(Filter... filters) {
        Filter f = createFilterForAndOr(Op.AND.name(), filters);
        return f;
    }

    /**
     * Creates a {@link Filter} for OR operation.
     *
     * @param filters Any number of {@link com.google.cloud.backend.core.Filter}s for this operation.
     * @return {@link com.google.cloud.backend.core.Filter} for this operation.
     */
    public static Filter or(Filter... filters) {
        Filter f = createFilterForAndOr(Op.OR.name(), filters);
        return f;
    }

    private static Filter createFilterForAndOr(String op, Filter... filters) {
        Filter f = new Filter();
        f.filterDto.setOperator(op);
        List<FilterDto> subfilters = new LinkedList<FilterDto>();
        for (Filter cf : filters) {
            subfilters.add(cf.getFilterDto());
        }
        f.filterDto.setSubfilters(subfilters);
        return f;
    }

    protected static Filter createFilter(String op, String propertyName, Object value) {
        Filter f = new Filter();
        f.filterDto.setOperator(op);
        List<Object> values = new LinkedList<Object>();
        values.add(propertyName);
        values.add(value);
        f.filterDto.setValues(values);
        return f;
    }

    public FilterDto getFilterDto() {
        return filterDto;
    }

    @Override
    public String toString() {
        return "Filter: op: " + this.filterDto.getOperator() +
                ", values: " + this.filterDto.getValues();
    }

}
