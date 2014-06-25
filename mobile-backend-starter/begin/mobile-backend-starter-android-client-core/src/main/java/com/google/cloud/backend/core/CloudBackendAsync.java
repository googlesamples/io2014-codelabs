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

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.cloud.backend.GCMIntentService;
import com.google.cloud.backend.core.CloudQuery.Order;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.mobilebackend.model.BlobAccess;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Cloud Backend API class that provides asynchronous APIs in addition to
 * {@link com.google.cloud.backend.core.CloudBackend}. The added methods work asynchronously with
 * {@link com.google.cloud.backend.core.CloudCallbackHandler}, so they can be called from UI thread of
 * {@link android.app.Activity}s or {@link android.app.Fragment}s. This class also implements Continuous
 * Query feature based on Google Cloud Messaging.
 */
public class CloudBackendAsync extends CloudBackend {

    /**
     * Map of ContinuousQueryHandlers (key = queryId)
     */
    protected final Map<String, ContinuousQueryHandler> continuousQueries =
            new HashMap<String, ContinuousQueryHandler>();

    /**
     * {@link android.app.Application} for this backend object, such as {@link android.app.Activity}.
     */
    protected final Application application;

    /**
     * Creates an instance of {@link com.google.cloud.backend.core.CloudBackendAsync}. Caller need to pass a
     * {@link android.content.Context} such as {@link android.app.Activity} that will be used to Google
     * Cloud Messaging and {@link android.content.SharedPreferences}.
     *
     * @param context {@link android.content.Context} for getting Application for GCM
     *                registration and SharedPreference. Null can be passed if you
     *                don't use those features.
     */
    public CloudBackendAsync(Context context) {

        // set Application
        this.application =
                (Application) (context != null ? context.getApplicationContext() : null);

        // registering GCM
        if (this.application != null) {
            GCMIntentService.getRegistrationId(this.application);
        }
    }

    /**
     * Inserts a CloudEntity into the backend asynchronously.
     *
     * @param ce      {@link CloudEntity} for inserting a CloudEntity.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void insert(CloudEntity ce, CloudCallbackHandler<CloudEntity> handler) {
        (new BackendCaller<CloudEntity, CloudEntity>(ce, handler) {
            @Override
            protected CloudEntity callBackend(CloudEntity param) throws IOException {
                return CloudBackendAsync.super.insert(param);
            }
        }).start();
    }

    /**
     * Inserts multiple {@link CloudEntity}s on the backend asynchronously.
     * Works just the same as {@link #insert(CloudEntity, com.google.cloud.backend.core.CloudCallbackHandler)}
     * .
     *
     * @param ceList  {@link java.util.List} that holds {@link CloudEntity}s to save.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void insertAll(
            List<CloudEntity> ceList, CloudCallbackHandler<List<CloudEntity>> handler) {
        (new BackendCaller<List<com.google.cloud.backend.core.CloudEntity>, List<com.google.cloud.backend.core.CloudEntity>>(ceList, handler) {
            @Override
            protected List<com.google.cloud.backend.core.CloudEntity> callBackend(List<com.google.cloud.backend.core.CloudEntity> ceList) throws IOException {
                return CloudBackendAsync.super.insertAll(ceList);
            }
        }).start();
    }

    /**
     * Updates the specified {@link CloudEntity} on the backend asynchronously.
     * If it does not have any Id, it creates a new Entity. If it has, find the
     * existing entity and update it.
     *
     * @param ce      {@link CloudEntity} for updating a CloudEntity.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void update(com.google.cloud.backend.core.CloudEntity ce, CloudCallbackHandler<com.google.cloud.backend.core.CloudEntity> handler) {
        (new BackendCaller<com.google.cloud.backend.core.CloudEntity, com.google.cloud.backend.core.CloudEntity>(ce, handler) {
            @Override
            protected com.google.cloud.backend.core.CloudEntity callBackend(com.google.cloud.backend.core.CloudEntity param) throws IOException {
                return CloudBackendAsync.super.update(param);
            }
        }).start();
    }

    /**
     * Updates multiple {@link CloudEntity}s on the backend asynchronously.
     * Works just the same as
     * {@link #updateAll(CloudEntity, com.google.cloud.backend.core.CloudCallbackHandler)}.
     *
     * @param ceList  {@link java.util.List} that holds {@link CloudEntity}s to save.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void updateAll(
            List<com.google.cloud.backend.core.CloudEntity> ceList, CloudCallbackHandler<List<CloudEntity>> handler) {
        (new BackendCaller<List<CloudEntity>, List<CloudEntity>>(ceList, handler) {
            @Override
            protected List<CloudEntity> callBackend(List<CloudEntity> ceList) throws IOException {
                return CloudBackendAsync.super.updateAll(ceList);
            }
        }).start();
    }

    /**
     * Reads the specified {@link CloudEntity} asynchronously.
     *
     * @param ce      {@link CloudEntity} that has kindName and id to specify the
     *                CloudEntity on the backend. Other property values will be
     *                ignored.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void get(CloudEntity ce, CloudCallbackHandler<CloudEntity> handler) {
        (new BackendCaller<CloudEntity, CloudEntity>(ce, handler) {
            @Override
            protected CloudEntity callBackend(CloudEntity ce) throws IOException {
                return CloudBackendAsync.super.get(ce.getKindName(), ce.getId());
            }
        }).start();
    }

    /**
     * Reads the specified multiple {@link CloudEntity}s asynchronously.
     *
     * @param ceList  a List of {@link CloudEntity}s that has kindName and id to
     *                specify the CloudEntity on the backend. Other property values
     *                will be ignored.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void getAll(List<CloudEntity> ceList, CloudCallbackHandler<List<CloudEntity>> handler) {
        (new BackendCaller<List<CloudEntity>, List<CloudEntity>>(ceList, handler) {
            @Override
            protected List<CloudEntity> callBackend(List<CloudEntity> ceList) throws IOException {

                // if the id list is empty, return it as is.
                if (ceList.isEmpty()) {
                    return ceList;
                }

                // get a List of IDs
                List<String> idList = new LinkedList<String>();
                for (CloudEntity ce : ceList) {
                    idList.add(ce.getId());
                }
                return CloudBackendAsync.super.getAll(idList.get(0), idList);
            }
        }).start();
    }

    /**
     * Deletes the specified {@link CloudEntity} asynchronously.
     *
     * @param ce      {@link CloudEntity} that has kindName and id to specify the
     *                CloudEntity on the backend. Other property values will be
     *                ignored.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void delete(CloudEntity ce, CloudCallbackHandler<Void> handler) {
        (new BackendCaller<CloudEntity, Void>(ce, handler) {
            @Override
            protected Void callBackend(CloudEntity ce) throws IOException {
                CloudBackendAsync.super.delete(ce.getKindName(), ce.getId());
                return null;
            }
        }).start();
    }

    /**
     * Deletes the specified multiple {@link CloudEntity}s asynchronously.
     *
     * @param ceList  a List of {@link com.google.cloud.backend.core.CloudEntity}s that has kindName and id to
     *                specify the CloudEntity on the backend. Other property values
     *                will be ignored.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void deleteAll(
            List<CloudEntity> ceList, CloudCallbackHandler<List<CloudEntity>> handler) {
        (new BackendCaller<List<CloudEntity>, List<CloudEntity>>(ceList, handler) {
            @Override
            protected List<CloudEntity> callBackend(List<CloudEntity> ceList) throws IOException {

                // if the id list is empty, return it as is.
                if (ceList.isEmpty()) {
                    return ceList;
                }

                // get a List of IDs
                List<String> idList = new LinkedList<String>();
                for (CloudEntity ce : ceList) {
                    idList.add(ce.getId());
                }
                CloudBackendAsync.super.deleteAllById(idList.get(0), idList);
                return null;
            }
        }).start();
    }

    /**
     * Executes a query with specified {@link com.google.cloud.backend.core.CloudQuery}.
     *
     * @param query   {@link com.google.cloud.backend.core.CloudQuery} to execute.
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void list(CloudQuery query, CloudCallbackHandler<List<CloudEntity>> handler) {

        // register the query as continuous query
        if (query.isContinuous()) {
            CloudQuery ncq = new CloudQuery(query);
            ncq.setScope(Scope.PAST);
            ContinuousQueryHandler cqh = new ContinuousQueryHandler(handler, ncq, getCredential());
            continuousQueries.put(query.getQueryId(), cqh);
        }

        // execute the query
        _list(query, handler, new Handler());
    }

    private void _list(CloudQuery query, CloudCallbackHandler<List<CloudEntity>> handler,
                       Handler uiThreadHandler) {
        (new BackendCaller<CloudQuery, List<CloudEntity>>(query, handler, uiThreadHandler) {
            @Override
            protected List<CloudEntity> callBackend(CloudQuery query) throws IOException {

                // set regId (this may blocks until registration finishes)
                if (application != null) {
                    query.setRegId(GCMIntentService.getRegistrationId(application));
                }

                // execute query
                return CloudBackendAsync.super.list(query);
            }
        }).start();
    }

    /**
     * Handles notification from Google Cloud Messaging service and invokes a
     * query specified by the queryId.
     *
     * @param queryId
     */
    public void handleQueryMessage(String queryId) {

        // retrieve query and handler name for the notification
        ContinuousQueryHandler cqh = continuousQueries.get(queryId);
        if (cqh == null) {
            Log.i(Consts.TAG, "handleQueryMessage: Query not found for ID: " + queryId);
            return;
        }

        // execute the query
        CloudBackendAsync cba = new CloudBackendAsync(application);
        cba.setCredential(cqh.getCredential());
        cba._list(cqh.getQuery(), cqh.getHandler(), cqh.getUiThreadHandler());
    }

    /**
     * Executes a {@link com.google.cloud.backend.core.CloudQuery} with specified single property condition.
     *
     * @param kindName      a name of Kind to query
     * @param propertyName  property name for filtering
     * @param operator      operator that will be applied to the filtering
     * @param propertyValue value that will be used in the filtering
     * @param order         {@link Order} of sorting on the specified property (ignored
     *                      when inequality filter is not used as the operator)
     * @param limit         number of maximum entities to be returned
     * @param scope         {@link Scope} of this query
     * @param handler       {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void listByProperty(String kindName, String propertyName, Filter.Op operator,
                               Object propertyValue, com.google.cloud.backend.core.CloudQuery.Order order, int limit, Scope scope,
                               CloudCallbackHandler<List<CloudEntity>> handler) {

        com.google.cloud.backend.core.CloudQuery cq = new com.google.cloud.backend.core.CloudQuery(kindName);
        cq.setFilter(com.google.cloud.backend.core.Filter.createFilter(operator.name(), propertyName, propertyValue));
        cq.setSort(propertyName, order);
        cq.setLimit(limit);
        cq.setScope(scope);
        this.list(cq, handler);
    }

    /**
     * Executes a {@link com.google.cloud.backend.core.CloudQuery} that retrieves all entities in the
     * specified kind.
     *
     * @param kindName         a name of Kind to query
     * @param sortPropertyName property name for sorting
     * @param order            {@link Order} of sorting on the specified property
     * @param limit            number of maximum entities to be returned
     * @param scope            {@link Scope} of this query
     * @param handler          {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void listByKind(String kindName, String sortPropertyName, com.google.cloud.backend.core.CloudQuery.Order order,
                           int limit, Scope scope, CloudCallbackHandler<List<CloudEntity>> handler) {

        CloudQuery cq = new CloudQuery(kindName);
        cq.setSort(sortPropertyName, order);
        cq.setLimit(limit);
        cq.setScope(scope);
        this.list(cq, handler);
    }

    /**
     * Removes a continuous query by specifying a queryId. QueryId can be
     * retrived from {@link com.google.cloud.backend.core.CloudQuery#getQueryId()}.
     *
     * @param handler {@link com.google.cloud.backend.core.CloudCallbackHandler} to remove
     */
    public void unsubscribeFromQuery(String queryId) {
        continuousQueries.remove(queryId);
    }

    /**
     * Clears all continuous queries.
     */
    public void clearAllSubscription() {
        continuousQueries.clear();
    }

    /**
     * Executes a {@link com.google.cloud.backend.core.CloudQuery} that retrieves the last one entity in the
     * specified kind.
     *
     * @param kindName a name of Kind to query
     * @param scope    {@link Scope} of this query
     * @param handler  {@link com.google.cloud.backend.core.CloudCallbackHandler} that handles the response.
     */
    public void getLastEntityOfKind(String kindName, Scope scope,
                                    CloudCallbackHandler<List<CloudEntity>> handler) {
        this.listByKind(kindName, com.google.cloud.backend.core.CloudEntity.PROP_CREATED_AT, Order.DESC, 1, scope, handler);
    }

    /**
     * Invokes the getBlobDownloadUrl asynchronously.
     *
     * @param param
     * @param handler
     */
    public void getBlobDownloadUrl(BlobAccessParam param, CloudCallbackHandler<BlobAccess> handler) {
        (new BackendCaller<BlobAccessParam, BlobAccess>(param, handler) {
            @Override
            protected BlobAccess callBackend(BlobAccessParam param) throws IOException {
                return CloudBackendAsync.super.getBlobDownloadUrl(param);
            }
        }).start();
    }

    /**
     * Invokes the getBlobUploadUrl asynchronously.
     *
     * @param param
     * @param handler
     */
    public void getBlobUploadUrl(BlobAccessParam param, CloudCallbackHandler<BlobAccess> handler) {
        (new BackendCaller<BlobAccessParam, BlobAccess>(param, handler) {
            @Override
            protected BlobAccess callBackend(BlobAccessParam param) throws IOException {
                return CloudBackendAsync.super.getBlobUploadUrl(param);
            }
        }).start();
    }

    /**
     * Uploads the blob to Google Cloud Storage asynchronously.
     * @param param
     * @param handler
     */
    public void uploadBlob(BlobUploadParam param, CloudCallbackHandler<Boolean> handler) {
        (new BackendCaller<BlobUploadParam, Boolean>(param, handler) {
            @Override
            protected Boolean callBackend(BlobUploadParam param) throws IOException {
                return CloudBackendAsync.super.uploadBlob(param);
            }
        }).start();
    }

    /**
     * Changes the color in the server side asynchronously.
     * @param param
     * @param handler
     */
    public void transformImage(ImageTransformationParam param, CloudCallbackHandler<BlobAccess> handler) {
        (new BackendCaller<ImageTransformationParam, BlobAccess>(param, handler) {
            @Override
            protected BlobAccess callBackend(ImageTransformationParam param) throws IOException {
                return CloudBackendAsync.super.transformImage(param);
            }
        }).start();
    }

    // a Thread class that will call backend API asynchronously
    // and call back the handler on UI thread
    private abstract class BackendCaller<Param, Result> extends Thread {

        final Handler uiThreadHandler;

        final CloudCallbackHandler<Result> handler;

        final Param param;

        private BackendCaller(
                Param param, CloudCallbackHandler<Result> crh, Handler uiThreadHandler) {
            this.handler = crh;
            this.param = param;
            this.uiThreadHandler = uiThreadHandler;
        }

        private BackendCaller(Param param, CloudCallbackHandler<Result> crh) {
            this.handler = crh;
            this.param = param;
            this.uiThreadHandler = new Handler();
        }

        @Override
        public void run() {

            // execute call
            Result r = null;
            IOException ie = null;
            try {
                r = callBackend(param);
            } catch (IOException e) {
                Log.i(com.google.cloud.backend.core.Consts.TAG, "error: ", e);
                ie = e;
            }
            final Result results = r;
            final IOException exception = ie;

            // if no handler specified, no need to callback
            if (handler == null) {
                return;
            }

            // pass the result to the handler on UI thread
            uiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (exception == null) {
                       handler.onComplete(results);
                    } else {
                        handler.onError(exception);
                    }
                }
            });

        }

        abstract protected Result callBackend(Param param) throws IOException;
    }

    ;

    /**
     * A class that holds required objects for continuous query callback
     */
    protected class ContinuousQueryHandler {

        private final CloudCallbackHandler<List<com.google.cloud.backend.core.CloudEntity>> handler;

        private final Handler uiThreadHandler;

        private final com.google.cloud.backend.core.CloudQuery query;

        private final GoogleAccountCredential credential;

        public ContinuousQueryHandler(final CloudCallbackHandler<List<CloudEntity>> handler,
                                      final CloudQuery query, final GoogleAccountCredential credential) {
            this.handler = handler;
            this.query = query;
            this.credential = credential;
            uiThreadHandler = new Handler();
        }

        public CloudCallbackHandler<List<CloudEntity>> getHandler() {
            return handler;
        }

        public Handler getUiThreadHandler() {
            return uiThreadHandler;
        }

        public CloudQuery getQuery() {
            return query;
        }

        public GoogleAccountCredential getCredential() {
            return credential;
        }

    }
}
