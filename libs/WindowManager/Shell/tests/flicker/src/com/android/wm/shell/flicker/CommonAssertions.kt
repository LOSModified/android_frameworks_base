/*
 * Copyright (C) 2020 The Android Open Source Project
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

@file:JvmName("CommonAssertions")

package com.android.wm.shell.flicker

import android.view.Surface
import com.android.server.wm.flicker.FlickerTestParameter
import com.android.server.wm.flicker.helpers.WindowUtils
import com.android.server.wm.flicker.traces.layers.LayerTraceEntrySubject
import com.android.server.wm.flicker.traces.layers.LayersTraceSubject
import com.android.server.wm.traces.common.IComponentMatcher
import com.android.server.wm.traces.common.region.Region

fun FlickerTestParameter.appPairsDividerIsVisibleAtEnd() {
    assertLayersEnd { this.isVisible(APP_PAIR_SPLIT_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.appPairsDividerIsInvisibleAtEnd() {
    assertLayersEnd { this.notContains(APP_PAIR_SPLIT_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.appPairsDividerBecomesVisible() {
    assertLayers {
        this.isInvisible(DOCKED_STACK_DIVIDER_COMPONENT)
            .then()
            .isVisible(DOCKED_STACK_DIVIDER_COMPONENT)
    }
}

fun FlickerTestParameter.splitScreenEntered(
    component1: IComponentMatcher,
    component2: IComponentMatcher,
    fromOtherApp: Boolean
) {
    if (fromOtherApp) {
        appWindowIsInvisibleAtStart(component1)
    } else {
        appWindowIsVisibleAtStart(component1)
    }
    appWindowIsInvisibleAtStart(component2)
    splitScreenDividerIsInvisibleAtStart()

    appWindowIsVisibleAtEnd(component1)
    appWindowIsVisibleAtEnd(component2)
    splitScreenDividerIsVisibleAtEnd()
}

fun FlickerTestParameter.splitScreenDismissed(
    component1: IComponentMatcher,
    component2: IComponentMatcher,
    toHome: Boolean
) {
    appWindowIsVisibleAtStart(component1)
    appWindowIsVisibleAtStart(component2)
    splitScreenDividerIsVisibleAtStart()

    appWindowIsInvisibleAtEnd(component1)
    if (toHome) {
        appWindowIsInvisibleAtEnd(component2)
    } else {
        appWindowIsVisibleAtEnd(component2)
    }
    splitScreenDividerIsInvisibleAtEnd()
}

fun FlickerTestParameter.splitScreenDividerIsVisibleAtStart() {
    assertLayersStart { this.isVisible(SPLIT_SCREEN_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.splitScreenDividerIsVisibleAtEnd() {
    assertLayersEnd { this.isVisible(SPLIT_SCREEN_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.splitScreenDividerIsInvisibleAtStart() {
    assertLayersStart { this.isInvisible(SPLIT_SCREEN_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.splitScreenDividerIsInvisibleAtEnd() {
    assertLayersEnd { this.isInvisible(SPLIT_SCREEN_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.splitScreenDividerBecomesVisible() {
    layerBecomesVisible(SPLIT_SCREEN_DIVIDER_COMPONENT)
}

fun FlickerTestParameter.splitScreenDividerBecomesInvisible() {
    assertLayers {
        this.isVisible(SPLIT_SCREEN_DIVIDER_COMPONENT)
            .then()
            .isInvisible(SPLIT_SCREEN_DIVIDER_COMPONENT)
    }
}

fun FlickerTestParameter.layerBecomesVisible(component: IComponentMatcher) {
    assertLayers { this.isInvisible(component).then().isVisible(component) }
}

fun FlickerTestParameter.layerBecomesInvisible(component: IComponentMatcher) {
    assertLayers { this.isVisible(component).then().isInvisible(component) }
}

fun FlickerTestParameter.layerIsVisibleAtEnd(component: IComponentMatcher) {
    assertLayersEnd { this.isVisible(component) }
}

fun FlickerTestParameter.layerKeepVisible(component: IComponentMatcher) {
    assertLayers { this.isVisible(component) }
}

fun FlickerTestParameter.splitAppLayerBoundsBecomesVisible(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean
) {
    assertLayers {
        this.notContains(SPLIT_SCREEN_DIVIDER_COMPONENT.or(component))
            .then()
            .isInvisible(SPLIT_SCREEN_DIVIDER_COMPONENT.or(component))
            .then()
            .splitAppLayerBoundsSnapToDivider(
                component,
                landscapePosLeft,
                portraitPosTop,
                endRotation
            )
    }
}

fun FlickerTestParameter.splitAppLayerBoundsBecomesVisibleByDrag(component: IComponentMatcher) {
    assertLayers {
        this.notContains(SPLIT_SCREEN_DIVIDER_COMPONENT.or(component), isOptional = true)
            .then()
            .isInvisible(SPLIT_SCREEN_DIVIDER_COMPONENT.or(component))
            .then()
            // TODO(b/245472831): Verify the component should snap to divider.
            .isVisible(component)
    }
}

fun FlickerTestParameter.splitAppLayerBoundsBecomesInvisible(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean
) {
    assertLayers {
        this.splitAppLayerBoundsSnapToDivider(
                component,
                landscapePosLeft,
                portraitPosTop,
                endRotation
            )
            .then()
            .isVisible(component, true)
            .then()
            .isInvisible(component)
    }
}

fun FlickerTestParameter.splitAppLayerBoundsIsVisibleAtEnd(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean
) {
    assertLayersEnd {
        splitAppLayerBoundsSnapToDivider(component, landscapePosLeft, portraitPosTop, endRotation)
    }
}

fun FlickerTestParameter.splitAppLayerBoundsKeepVisible(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean
) {
    assertLayers {
        splitAppLayerBoundsSnapToDivider(component, landscapePosLeft, portraitPosTop, endRotation)
    }
}

fun FlickerTestParameter.splitAppLayerBoundsChanges(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean
) {
    assertLayers {
        if (landscapePosLeft) {
            this.splitAppLayerBoundsSnapToDivider(
                component,
                landscapePosLeft,
                portraitPosTop,
                endRotation
            )
        } else {
            this.splitAppLayerBoundsSnapToDivider(
                    component,
                    landscapePosLeft,
                    portraitPosTop,
                    endRotation
                )
                .then()
                .isInvisible(component)
                .then()
                .splitAppLayerBoundsSnapToDivider(
                    component,
                    landscapePosLeft,
                    portraitPosTop,
                    endRotation
                )
        }
    }
}

fun LayersTraceSubject.splitAppLayerBoundsSnapToDivider(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean,
    rotation: Int
): LayersTraceSubject {
    return invoke("splitAppLayerBoundsSnapToDivider") {
        it.splitAppLayerBoundsSnapToDivider(component, landscapePosLeft, portraitPosTop, rotation)
    }
}

fun LayerTraceEntrySubject.splitAppLayerBoundsSnapToDivider(
    component: IComponentMatcher,
    landscapePosLeft: Boolean,
    portraitPosTop: Boolean,
    rotation: Int
): LayerTraceEntrySubject {
    val displayBounds = WindowUtils.getDisplayBounds(rotation)
    return invoke {
        val dividerRegion = layer(SPLIT_SCREEN_DIVIDER_COMPONENT).visibleRegion.region
        visibleRegion(component)
            .coversAtMost(
                if (displayBounds.width > displayBounds.height) {
                    if (landscapePosLeft) {
                        Region.from(
                            0,
                            0,
                            (dividerRegion.bounds.left + dividerRegion.bounds.right) / 2,
                            displayBounds.bounds.bottom
                        )
                    } else {
                        Region.from(
                            (dividerRegion.bounds.left + dividerRegion.bounds.right) / 2,
                            0,
                            displayBounds.bounds.right,
                            displayBounds.bounds.bottom
                        )
                    }
                } else {
                    if (portraitPosTop) {
                        Region.from(
                            0,
                            0,
                            displayBounds.bounds.right,
                            (dividerRegion.bounds.top + dividerRegion.bounds.bottom) / 2
                        )
                    } else {
                        Region.from(
                            0,
                            (dividerRegion.bounds.top + dividerRegion.bounds.bottom) / 2,
                            displayBounds.bounds.right,
                            displayBounds.bounds.bottom
                        )
                    }
                }
            )
    }
}

fun FlickerTestParameter.appWindowBecomesVisible(component: IComponentMatcher) {
    assertWm {
        this.isAppWindowInvisible(component)
            .then()
            .notContains(component, isOptional = true)
            .then()
            .isAppWindowInvisible(component, isOptional = true)
            .then()
            .isAppWindowVisible(component)
    }
}

fun FlickerTestParameter.appWindowBecomesInvisible(component: IComponentMatcher) {
    assertWm { this.isAppWindowVisible(component).then().isAppWindowInvisible(component) }
}

fun FlickerTestParameter.appWindowIsVisibleAtStart(component: IComponentMatcher) {
    assertWmStart { this.isAppWindowVisible(component) }
}

fun FlickerTestParameter.appWindowIsVisibleAtEnd(component: IComponentMatcher) {
    assertWmEnd { this.isAppWindowVisible(component) }
}

fun FlickerTestParameter.appWindowIsInvisibleAtStart(component: IComponentMatcher) {
    assertWmStart { this.isAppWindowInvisible(component) }
}

fun FlickerTestParameter.appWindowIsInvisibleAtEnd(component: IComponentMatcher) {
    assertWmEnd { this.isAppWindowInvisible(component) }
}

fun FlickerTestParameter.appWindowKeepVisible(component: IComponentMatcher) {
    assertWm { this.isAppWindowVisible(component) }
}

fun FlickerTestParameter.dockedStackDividerIsVisibleAtEnd() {
    assertLayersEnd { this.isVisible(DOCKED_STACK_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.dockedStackDividerBecomesVisible() {
    assertLayers {
        this.isInvisible(DOCKED_STACK_DIVIDER_COMPONENT)
            .then()
            .isVisible(DOCKED_STACK_DIVIDER_COMPONENT)
    }
}

fun FlickerTestParameter.dockedStackDividerBecomesInvisible() {
    assertLayers {
        this.isVisible(DOCKED_STACK_DIVIDER_COMPONENT)
            .then()
            .isInvisible(DOCKED_STACK_DIVIDER_COMPONENT)
    }
}

fun FlickerTestParameter.dockedStackDividerNotExistsAtEnd() {
    assertLayersEnd { this.notContains(DOCKED_STACK_DIVIDER_COMPONENT) }
}

fun FlickerTestParameter.appPairsPrimaryBoundsIsVisibleAtEnd(
    rotation: Int,
    primaryComponent: IComponentMatcher
) {
    assertLayersEnd {
        val dividerRegion = layer(APP_PAIR_SPLIT_DIVIDER_COMPONENT).visibleRegion.region
        visibleRegion(primaryComponent).overlaps(getPrimaryRegion(dividerRegion, rotation))
    }
}

fun FlickerTestParameter.dockedStackPrimaryBoundsIsVisibleAtEnd(
    rotation: Int,
    primaryComponent: IComponentMatcher
) {
    assertLayersEnd {
        val dividerRegion = layer(DOCKED_STACK_DIVIDER_COMPONENT).visibleRegion.region
        visibleRegion(primaryComponent).overlaps(getPrimaryRegion(dividerRegion, rotation))
    }
}

fun FlickerTestParameter.appPairsSecondaryBoundsIsVisibleAtEnd(
    rotation: Int,
    secondaryComponent: IComponentMatcher
) {
    assertLayersEnd {
        val dividerRegion = layer(APP_PAIR_SPLIT_DIVIDER_COMPONENT).visibleRegion.region
        visibleRegion(secondaryComponent).overlaps(getSecondaryRegion(dividerRegion, rotation))
    }
}

fun FlickerTestParameter.dockedStackSecondaryBoundsIsVisibleAtEnd(
    rotation: Int,
    secondaryComponent: IComponentMatcher
) {
    assertLayersEnd {
        val dividerRegion = layer(DOCKED_STACK_DIVIDER_COMPONENT).visibleRegion.region
        visibleRegion(secondaryComponent).overlaps(getSecondaryRegion(dividerRegion, rotation))
    }
}

fun getPrimaryRegion(dividerRegion: Region, rotation: Int): Region {
    val displayBounds = WindowUtils.getDisplayBounds(rotation)
    return if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
        Region.from(
            0,
            0,
            displayBounds.bounds.right,
            dividerRegion.bounds.top + WindowUtils.dockedStackDividerInset
        )
    } else {
        Region.from(
            0,
            0,
            dividerRegion.bounds.left + WindowUtils.dockedStackDividerInset,
            displayBounds.bounds.bottom
        )
    }
}

fun getSecondaryRegion(dividerRegion: Region, rotation: Int): Region {
    val displayBounds = WindowUtils.getDisplayBounds(rotation)
    return if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
        Region.from(
            0,
            dividerRegion.bounds.bottom - WindowUtils.dockedStackDividerInset,
            displayBounds.bounds.right,
            displayBounds.bounds.bottom
        )
    } else {
        Region.from(
            dividerRegion.bounds.right - WindowUtils.dockedStackDividerInset,
            0,
            displayBounds.bounds.right,
            displayBounds.bounds.bottom
        )
    }
}
