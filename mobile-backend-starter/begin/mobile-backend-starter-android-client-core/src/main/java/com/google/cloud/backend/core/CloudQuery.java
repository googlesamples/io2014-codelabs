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

import android.util.Base64;

import com.google.cloud.backend.mobilebackend.model.QueryDto;
import com.google.gson.Gson;

/**
 * A query object to execute it with {@link CloudBackend}. See
 * {@link CloudBackendTest#testList()} for detailed usage.
 */
public class CloudQuery {

    private static final Gson gson = new Gson();

    /**
     * An enum for specifying sort order of the query.
     */
    public enum Order {
        ASC, DESC
    };

    /**
     * An enum to specify scope of query.
     */
    public enum Scope {
        /**
         * PAST: query will be executed only for the entities that have been
         * stored in past, and will not be executed when the entities will be
         * updated in future.
         */
        PAST,

        /**
         * FUTURE: query will be executed only when entities are updated in
         * future, and will not be executed against the current entities.
         */
        FUTURE,

        /**
         * FUTURE_AND_PAST: query will be executed for the current entities and
         * future updates.
         */
        FUTURE_AND_PAST
    }

    private Filter filter;

    private final QueryDto queryDto;

    /**
     * Creates an instance of {@link CloudQuery}.
     *
     * @param kindName Name of the kind that this query will execute with.
     */
    public CloudQuery(String kindName) {
        this.queryDto = new QueryDto();
        this.queryDto.setKindName(kindName);
        this.queryDto.setScope(Scope.PAST.name());
    }

    /**
     * Creates an instance of {@link CloudQuery} from the values of the
     * specified CloudQuery. queryDto field will be deep copied, and filter
     * field will be shallow copied.
     *
     * @param cq {@link CloudQuery} that will be used for instantiation.
     */
    public CloudQuery(CloudQuery cq) {
        this.queryDto = copyQueryDto(cq.queryDto);
        this.filter = cq.filter;
    }

    private QueryDto copyQueryDto(QueryDto cq) {
        QueryDto ncq = new QueryDto();
        ncq.setFilterDto(cq.getFilterDto());
        ncq.setKindName(cq.getKindName());
        ncq.setLimit(cq.getLimit());
        ncq.setQueryId(cq.getQueryId());
        ncq.setRegId(cq.getRegId());
        ncq.setScope(cq.getScope());
        ncq.setSortAscending(cq.getSortAscending());
        ncq.setSortedPropertyName(cq.getSortedPropertyName());
        ncq.setSubscriptionDurationSec(cq.getSubscriptionDurationSec());
        return ncq;
    }

    /**
     * Sets registration id.
     *
     * @param regId
     */
    protected void setRegId(String regId) {
        this.queryDto.setRegId(regId);
    }

    /**
     * Returns a kind name of this query.
     *
     * @return kind name
     */
    public String getKindName() {
        return this.queryDto.getKindName();
    }

    /**
     * Set Sort property name and sort order to this query.
     *
     * @param propertyName Name of the target name.
     * @param sortOrder {@link Order} of this sort.
     * @return {@link CloudQuery}
     */
    public CloudQuery setSort(String propertyName, Order sortOrder) {
        this.queryDto.setSortedPropertyName(propertyName);
        this.queryDto.setSortAscending(sortOrder == Order.ASC);
        return this;
    }

    /**
     * Set Limit number on the result set to this query.
     *
     * @param limit Max number of lines for the query result set.
     * @return {@link CloudQuery}
     */
    public CloudQuery setLimit(int limit) {
        this.queryDto.setLimit(limit);
        return this;
    }

    /**
     * Creates a {@link QueryDto} for this CloudQuery.
     *
     * @return {@link QueryDto} for this CloudQuery
     */
    protected QueryDto convertToQueryDto() {

        // generate FilterDto
        if (filter != null) {
            this.queryDto.setFilterDto(filter.getFilterDto());
        }
        return this.queryDto;
    }

    /**
     * Returns {@link com.google.cloud.backend.core.Filter} (filter) for this query.
     *
     * @return {@link com.google.cloud.backend.core.Filter}
     */
    public Filter getFilter() {
        return filter;
    }

    /**
     * Sets a filter {@link com.google.cloud.backend.core.Filter} for this query.
     *
     * @param filter
     * @return {@link CloudQuery}
     */
    public CloudQuery setFilter(Filter filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Sets a {@link Scope} of this query.
     *
     * @param scope {@link Scope} of this query.
     * @return {@link CloudQuery}
     */
    public CloudQuery setScope(Scope scope) {
        this.queryDto.setScope(scope.name());
        return this;
    }

    /**
     * Returns {@link Scope} of this query.
     *
     * @return
     */
    public Scope getScope() {
        return Scope.valueOf(this.queryDto.getScope());
    }

    /**
     * Returns true if this query is a continuous query.
     *
     * @return
     */
    public boolean isContinuous() {
        return (getScope() == Scope.FUTURE || getScope() == Scope.FUTURE_AND_PAST);
    }

    /**
     * Sets subscription duration (in sec) for this query.
     *
     * @param duration
     */
    public void setSubscriptionDurationSec(int duration) {
        this.queryDto.setSubscriptionDurationSec(duration);
    }

    @Override
    public String toString() {
        return "CloudQuery (" + this.queryDto.getKindName() + "/" + getScope() + "): filter: "
                + filter;
    }

    /**
     * Sets the specified queryId to this query. This method will be used when
     * developer want to control identity of each query explicitly, such as
     * grouping multiple {@link CloudQuery} as one query and map it to one
     * handler.
     *
     * @param queryId {@link String} that identifies each query
     */
    public void setQueryId(String queryId) {
        this.queryDto.setQueryId(queryId);
    }

    /**
     * Returns queryId of this query. Unless developer sets it by
     * {@link #setQueryId(String)} explicitly, this method will return an ID
     * based on the query condition. It means the same ID will be returned for
     * multiple {@link CloudQuery}s if they have the same query condition.
     * 
     * @return
     */
    public String getQueryId() {
        setDefaultQueryIdIfNeeded();
        return this.queryDto.getQueryId();
    }

    private void setDefaultQueryIdIfNeeded() {
        if (this.queryDto.getQueryId() == null) {
            int queryHash = (gson.toJson(queryDto) + gson.toJson(filter)).hashCode();
            String queryId = Base64.encodeToString(String.valueOf(queryHash).getBytes(),
                    Base64.NO_PADDING | Base64.NO_WRAP);
            this.queryDto.setQueryId(queryId);
        }
    }
}
