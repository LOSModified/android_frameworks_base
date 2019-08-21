/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.service.storage;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SdkConstant;
import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelableException;
import android.os.RemoteCallback;
import android.os.RemoteException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A service to handle filesystem I/O from other apps.
 *
 * <p>To extend this class, you must declare the service in your manifest file with the
 * {@link android.Manifest.permission#BIND_EXTERNAL_STORAGE_SERVICE} permission,
 * and include an intent filter with the {@link #SERVICE_INTERFACE} action.
 * For example:</p>
 * <pre>
 *     &lt;service android:name=".ExternalStorageServiceImpl"
 *             android:exported="true"
 *             android:priority="100"
 *             android:permission="android.permission.BIND_EXTERNAL_STORAGE_SERVICE"&gt;
 *         &lt;intent-filter&gt;
 *             &lt;action android:name="android.service.storage.ExternalStorageService" /&gt;
 *         &lt;/intent-filter&gt;
 *     &lt;/service&gt;
 * </pre>
 * @hide
 */
@SystemApi
public abstract class ExternalStorageService extends Service {
    /**
     * The Intent action that a service must respond to. Add it as an intent filter in the
     * manifest declaration of the implementing service.
     */
    @SdkConstant(SdkConstant.SdkConstantType.SERVICE_ACTION)
    public static final String SERVICE_INTERFACE = "android.service.storage.ExternalStorageService";
    /**
     * Whether the session associated with the device file descriptor when calling
     * {@link #onStartSession} is a FUSE session.
     */
    public static final int FLAG_SESSION_TYPE_FUSE = 1 << 0;

    /**
     * Whether the upper file system path specified when calling {@link #onStartSession}
     * should be indexed.
     */
    public static final int FLAG_SESSION_ATTRIBUTE_INDEXABLE = 1 << 1;

    /**
     * {@link Bundle} key for a {@link String} value.
     *
     * {@hide}
     */
    public static final String EXTRA_SESSION_ID =
            "android.service.storage.extra.session_id";
    /**
     * {@link Bundle} key for a {@link ParcelableException} value.
     *
     * {@hide}
     */
    public static final String EXTRA_ERROR =
            "android.service.storage.extra.error";

    /** @hide */
    @IntDef(flag = true, prefix = {"FLAG_SESSION_"},
        value = {FLAG_SESSION_TYPE_FUSE, FLAG_SESSION_ATTRIBUTE_INDEXABLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SessionFlag {}

    private final ExternalStorageServiceWrapper mWrapper = new ExternalStorageServiceWrapper();
    private final Handler mHandler = new Handler(Looper.getMainLooper(), null, true);

    /**
     * Called when the system starts a session associated with {@code deviceFd}
     * identified by {@code sessionId} to handle filesystem I/O for other apps. The type of
     * session and other attributes are passed in {@code flag}.
     *
     * <p> I/O is received as requests originating from {@code upperFileSystemPath} on
     * {@code deviceFd}. Implementors should handle the I/O by responding to these requests
     * using the data on the {@code lowerFileSystemPath}.
     *
     * <p> Additional calls to start a session for the same {@code sessionId} while the session
     * is still starting or already started should have no effect.
     */
    public abstract void onStartSession(@NonNull String sessionId, @SessionFlag int flag,
            @NonNull ParcelFileDescriptor deviceFd, @NonNull String upperFileSystemPath,
            @NonNull String lowerFileSystemPath) throws IOException;

    /**
     * Called when the system ends the session identified by {@code sessionId}. Implementors should
     * stop handling filesystem I/O and clean up resources from the ended session.
     *
     * <p> Additional calls to end a session for the same {@code sessionId} while the session
     * is still ending or has not started should have no effect.
     */
    public abstract void onEndSession(@NonNull String sessionId) throws IOException;

    @Override
    @NonNull
    public final IBinder onBind(@NonNull Intent intent) {
        return mWrapper;
    }

    private class ExternalStorageServiceWrapper extends IExternalStorageService.Stub {
        @Override
        public void startSession(String sessionId, @SessionFlag int flag,
                ParcelFileDescriptor deviceFd, String upperPath, String lowerPath,
                RemoteCallback callback) throws RemoteException {
            mHandler.post(() -> {
                try {
                    onStartSession(sessionId, flag, deviceFd, upperPath, lowerPath);
                    sendResult(sessionId, null /* throwable */, callback);
                } catch (Throwable t) {
                    sendResult(sessionId, t, callback);
                }
            });
        }

        @Override
        public void endSession(String sessionId, RemoteCallback callback) throws RemoteException {
            mHandler.post(() -> {
                try {
                    onEndSession(sessionId);
                    sendResult(sessionId, null /* throwable */, callback);
                } catch (Throwable t) {
                    sendResult(sessionId, t, callback);
                }
            });
        }

        private void sendResult(String sessionId, Throwable throwable, RemoteCallback callback) {
            Bundle bundle = new Bundle();
            bundle.putString(EXTRA_SESSION_ID, sessionId);
            if (throwable != null) {
                bundle.putParcelable(EXTRA_ERROR, new ParcelableException(throwable));
            }
            callback.sendResult(bundle);
        }
    }
}
