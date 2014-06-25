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

import com.google.api.client.util.DateTime;
import com.google.cloud.backend.mobilebackend.model.EntityDto;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A class that represents a cloud entity on App Engine Datastore.
 */
public class CloudEntity {

    /**
     * Name of the auto-generated property that has a time stamp of creation.
     */
    public static final String PROP_CREATED_AT = "_createdAt";

    /**
     * Name of the auto-generated property that has a time stamp of update.
     */
    public static final String PROP_UPDATED_AT = "_updatedAt";

    /**
     * Name of the auto-generated property that has creator account name.
     */
    public static final String PROP_CREATED_BY = "_createdBy";

    /**
     * Name of the auto-generated property that has updater account name.
     */
    public static final String PROP_UPDATED_BY = "_updatedBy";

    /**
     * Name of the auto-generated property that has userId of the entity owner.
     */
    public static final String PROP_OWNER = "_owner";

    private String id;

    private Date createdAt;

    private Date updatedAt;

    private String createdBy;

    private String updatedBy;

    private String kindName;

    private Map<String, Object> properties = new HashMap<String, Object>();

    private String owner;

    @SuppressWarnings("unchecked")
    protected static CloudEntity createCloudEntityFromEntityDto(EntityDto cd) {
        CloudEntity co = new CloudEntity(cd.getKindName());
        co.id = cd.getId();
        co.createdAt = new Date(cd.getCreatedAt().getValue());
        co.updatedAt = new Date(cd.getUpdatedAt().getValue());
        co.createdBy = cd.getCreatedBy();
        co.updatedBy = cd.getUpdatedBy();
        co.kindName = cd.getKindName();
        co.properties.putAll((Map<String, Object>) cd.getProperties());
        co.owner = cd.getOwner();
        return co;
    }

    public CloudEntity(String kindName) {
        if (kindName == null || !kindName.matches("\\w+")) {
            throw new IllegalArgumentException("Illegal kind name: " + kindName);
        }
        this.kindName = kindName;
    }

    protected EntityDto getEntityDto() {
        EntityDto co = new EntityDto();
        co.setId(id);
        if (createdAt != null) {
            co.setCreatedAt(new DateTime(createdAt));
        }
        co.setCreatedBy(createdBy);
        co.setKindName(kindName);
        if (updatedAt != null) {
            co.setUpdatedAt(new DateTime(updatedAt));
        }
        co.setUpdatedBy(updatedBy);
        co.setProperties(properties);
        co.setOwner(owner);
        return co;
    }

    public void put(String key, Object value) {
        properties.put(key, value);
    }

    public Object get(String key) {
        return properties.get(key);
    }

    public Object remove(String key) {
        return properties.remove(key);
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getKindName() {
        return kindName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String aclOwner) {
        this.owner = aclOwner;
    }

    @Override
    public String toString() {
        return "CloudEntity(" + this.getKindName() + "/" + this.getId() + "): " + properties;
    }

    @Override
    public int hashCode() {
        String s = "" + this.id + this.kindName + this.createdAt + this.createdBy + this.updatedAt
                + this.updatedBy + this.owner + this.properties.hashCode();
        return s.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj.hashCode() == this.hashCode();
    }

}
