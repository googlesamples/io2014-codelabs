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

import java.io.IOException;

/**
 * A handler class to handle an asynchronous callback from Cloud Backend. Used
 * with {@link com.google.cloud.backend.core.CloudBackendAsync} and {@link CloudBackendMessaging}.
 *
 */
public abstract class CloudCallbackHandler<T> {

    /**
     * Subclasses should override this to implement a handler method to process
     * the results. If not overridden, the result will be discarded.
     *
     * @param results The result value (usually, a {@link CloudEntity} or a
     *            {@link java.util.List} of CloudEntities)
     */
    public abstract void onComplete(T results);

    /**
     * Subclasses may override this to implement an exception handler. If not
     * overridden, the exception will be ignored.
     *
     * @param exception {@link java.io.IOException} that would be thrown on the request
     *            call.
     */
    public void onError(IOException exception) {
    }

}
