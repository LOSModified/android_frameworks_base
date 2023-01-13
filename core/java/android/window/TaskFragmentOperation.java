/*
 * Copyright (C) 2023 The Android Open Source Project
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

package android.window;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Data object of params for TaskFragment related {@link WindowContainerTransaction} operation.
 *
 * @see WindowContainerTransaction#addTaskFragmentOperation(IBinder, TaskFragmentOperation).
 * @hide
 */
public final class TaskFragmentOperation implements Parcelable {

    /**
     * Type for tracking other {@link WindowContainerTransaction} to TaskFragment that is not set
     * through {@link TaskFragmentOperation}, such as {@link WindowContainerTransaction#setBounds}.
     */
    public static final int OP_TYPE_UNKNOWN = -1;

    /** Creates a new TaskFragment. */
    public static final int OP_TYPE_CREATE_TASK_FRAGMENT = 0;

    /** Deletes the given TaskFragment. */
    public static final int OP_TYPE_DELETE_TASK_FRAGMENT = 1;

    /** Starts an Activity in the given TaskFragment. */
    public static final int OP_TYPE_START_ACTIVITY_IN_TASK_FRAGMENT = 2;

    /** Reparents the given Activity to the given TaskFragment. */
    public static final int OP_TYPE_REPARENT_ACTIVITY_TO_TASK_FRAGMENT = 3;

    /** Sets two TaskFragments adjacent to each other. */
    public static final int OP_TYPE_SET_ADJACENT_TASK_FRAGMENTS = 4;

    /** Clears the adjacent TaskFragments relationship. */
    public static final int OP_TYPE_CLEAR_ADJACENT_TASK_FRAGMENTS = 5;

    /** Requests focus on the top running Activity in the given TaskFragment. */
    public static final int OP_TYPE_REQUEST_FOCUS_ON_TASK_FRAGMENT = 6;

    /** Sets a given TaskFragment to have a companion TaskFragment. */
    public static final int OP_TYPE_SET_COMPANION_TASK_FRAGMENT = 7;

    /** Sets the {@link TaskFragmentAnimationParams} for the given TaskFragment. */
    public static final int OP_TYPE_SET_ANIMATION_PARAMS = 8;

    @IntDef(prefix = { "OP_TYPE_" }, value = {
            OP_TYPE_UNKNOWN,
            OP_TYPE_CREATE_TASK_FRAGMENT,
            OP_TYPE_DELETE_TASK_FRAGMENT,
            OP_TYPE_START_ACTIVITY_IN_TASK_FRAGMENT,
            OP_TYPE_REPARENT_ACTIVITY_TO_TASK_FRAGMENT,
            OP_TYPE_SET_ADJACENT_TASK_FRAGMENTS,
            OP_TYPE_CLEAR_ADJACENT_TASK_FRAGMENTS,
            OP_TYPE_REQUEST_FOCUS_ON_TASK_FRAGMENT,
            OP_TYPE_SET_COMPANION_TASK_FRAGMENT,
            OP_TYPE_SET_ANIMATION_PARAMS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OperationType {}

    @OperationType
    private final int mOpType;

    @Nullable
    private final TaskFragmentCreationParams mTaskFragmentCreationParams;

    @Nullable
    private final IBinder mActivityToken;

    @Nullable
    private final Intent mActivityIntent;

    @Nullable
    private final Bundle mBundle;

    @Nullable
    private final IBinder mSecondaryFragmentToken;

    @Nullable
    private final TaskFragmentAnimationParams mAnimationParams;

    private TaskFragmentOperation(@OperationType int opType,
            @Nullable TaskFragmentCreationParams taskFragmentCreationParams,
            @Nullable IBinder activityToken, @Nullable Intent activityIntent,
            @Nullable Bundle bundle, @Nullable IBinder secondaryFragmentToken,
            @Nullable TaskFragmentAnimationParams animationParams) {
        mOpType = opType;
        mTaskFragmentCreationParams = taskFragmentCreationParams;
        mActivityToken = activityToken;
        mActivityIntent = activityIntent;
        mBundle = bundle;
        mSecondaryFragmentToken = secondaryFragmentToken;
        mAnimationParams = animationParams;
    }

    private TaskFragmentOperation(Parcel in) {
        mOpType = in.readInt();
        mTaskFragmentCreationParams = in.readTypedObject(TaskFragmentCreationParams.CREATOR);
        mActivityToken = in.readStrongBinder();
        mActivityIntent = in.readTypedObject(Intent.CREATOR);
        mBundle = in.readBundle(getClass().getClassLoader());
        mSecondaryFragmentToken = in.readStrongBinder();
        mAnimationParams = in.readTypedObject(TaskFragmentAnimationParams.CREATOR);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mOpType);
        dest.writeTypedObject(mTaskFragmentCreationParams, flags);
        dest.writeStrongBinder(mActivityToken);
        dest.writeTypedObject(mActivityIntent, flags);
        dest.writeBundle(mBundle);
        dest.writeStrongBinder(mSecondaryFragmentToken);
        dest.writeTypedObject(mAnimationParams, flags);
    }

    @NonNull
    public static final Creator<TaskFragmentOperation> CREATOR =
            new Creator<TaskFragmentOperation>() {
                @Override
                public TaskFragmentOperation createFromParcel(Parcel in) {
                    return new TaskFragmentOperation(in);
                }

                @Override
                public TaskFragmentOperation[] newArray(int size) {
                    return new TaskFragmentOperation[size];
                }
            };

    /**
     * Gets the {@link OperationType} of this {@link TaskFragmentOperation}.
     */
    @OperationType
    public int getOpType() {
        return mOpType;
    }

    /**
     * Gets the options to create a new TaskFragment.
     */
    @Nullable
    public TaskFragmentCreationParams getTaskFragmentCreationParams() {
        return mTaskFragmentCreationParams;
    }

    /**
     * Gets the Activity token set in this operation.
     */
    @Nullable
    public IBinder getActivityToken() {
        return mActivityToken;
    }

    /**
     * Gets the Intent to start a new Activity.
     */
    @Nullable
    public Intent getActivityIntent() {
        return mActivityIntent;
    }

    /**
     * Gets the Bundle set in this operation.
     */
    @Nullable
    public Bundle getBundle() {
        return mBundle;
    }

    /**
     * Gets the fragment token of the secondary TaskFragment set in this operation.
     */
    @Nullable
    public IBinder getSecondaryFragmentToken() {
        return mSecondaryFragmentToken;
    }

    /**
     * Gets the animation related override of TaskFragment.
     */
    @Nullable
    public TaskFragmentAnimationParams getAnimationParams() {
        return mAnimationParams;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TaskFragmentOperation{ opType=").append(mOpType);
        if (mTaskFragmentCreationParams != null) {
            sb.append(", taskFragmentCreationParams=").append(mTaskFragmentCreationParams);
        }
        if (mActivityToken != null) {
            sb.append(", activityToken=").append(mActivityToken);
        }
        if (mActivityIntent != null) {
            sb.append(", activityIntent=").append(mActivityIntent);
        }
        if (mBundle != null) {
            sb.append(", bundle=").append(mBundle);
        }
        if (mSecondaryFragmentToken != null) {
            sb.append(", secondaryFragmentToken=").append(mSecondaryFragmentToken);
        }
        if (mAnimationParams != null) {
            sb.append(", animationParams=").append(mAnimationParams);
        }

        sb.append('}');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mOpType, mTaskFragmentCreationParams, mActivityToken, mActivityIntent,
                mBundle, mSecondaryFragmentToken, mAnimationParams);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof TaskFragmentOperation)) {
            return false;
        }
        final TaskFragmentOperation other = (TaskFragmentOperation) obj;
        return mOpType == other.mOpType
                && Objects.equals(mTaskFragmentCreationParams, other.mTaskFragmentCreationParams)
                && Objects.equals(mActivityToken, other.mActivityToken)
                && Objects.equals(mActivityIntent, other.mActivityIntent)
                && Objects.equals(mBundle, other.mBundle)
                && Objects.equals(mSecondaryFragmentToken, other.mSecondaryFragmentToken)
                && Objects.equals(mAnimationParams, other.mAnimationParams);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /** Builder to construct the {@link TaskFragmentOperation}. */
    public static final class Builder {

        @OperationType
        private final int mOpType;

        @Nullable
        private TaskFragmentCreationParams mTaskFragmentCreationParams;

        @Nullable
        private IBinder mActivityToken;

        @Nullable
        private Intent mActivityIntent;

        @Nullable
        private Bundle mBundle;

        @Nullable
        private IBinder mSecondaryFragmentToken;

        @Nullable
        private TaskFragmentAnimationParams mAnimationParams;

        /**
         * @param opType the {@link OperationType} of this {@link TaskFragmentOperation}.
         */
        public Builder(@OperationType int opType) {
            mOpType = opType;
        }

        /**
         * Sets the {@link TaskFragmentCreationParams} for creating a new TaskFragment.
         */
        @NonNull
        public Builder setTaskFragmentCreationParams(
                @Nullable TaskFragmentCreationParams taskFragmentCreationParams) {
            mTaskFragmentCreationParams = taskFragmentCreationParams;
            return this;
        }

        /**
         * Sets an Activity token to this operation.
         */
        @NonNull
        public Builder setActivityToken(@Nullable IBinder activityToken) {
            mActivityToken = activityToken;
            return this;
        }

        /**
         * Sets the Intent to start a new Activity.
         */
        @NonNull
        public Builder setActivityIntent(@Nullable Intent activityIntent) {
            mActivityIntent = activityIntent;
            return this;
        }

        /**
         * Sets a Bundle to this operation.
         */
        @NonNull
        public Builder setBundle(@Nullable Bundle bundle) {
            mBundle = bundle;
            return this;
        }

        /**
         * Sets the secondary fragment token to this operation.
         */
        @NonNull
        public Builder setSecondaryFragmentToken(@Nullable IBinder secondaryFragmentToken) {
            mSecondaryFragmentToken = secondaryFragmentToken;
            return this;
        }

        /**
         * Sets the {@link TaskFragmentAnimationParams} for the given TaskFragment.
         */
        @NonNull
        public Builder setAnimationParams(@Nullable TaskFragmentAnimationParams animationParams) {
            mAnimationParams = animationParams;
            return this;
        }

        /**
         * Constructs the {@link TaskFragmentOperation}.
         */
        @NonNull
        public TaskFragmentOperation build() {
            return new TaskFragmentOperation(mOpType, mTaskFragmentCreationParams, mActivityToken,
                    mActivityIntent, mBundle, mSecondaryFragmentToken, mAnimationParams);
        }
    }
}
