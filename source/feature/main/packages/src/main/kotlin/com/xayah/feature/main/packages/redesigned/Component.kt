package com.xayah.feature.main.packages.redesigned

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import com.dotlottie.dlplayer.Mode
import com.lottiefiles.dotlottie.core.compose.ui.DotLottieAnimation
import com.lottiefiles.dotlottie.core.util.DotLottieSource
import com.xayah.core.model.database.PackageEntity
import com.xayah.core.ui.component.AnimatedLinearProgressIndicator
import com.xayah.core.ui.component.AssistChip
import com.xayah.core.ui.component.BodyLargeText
import com.xayah.core.ui.component.BodyMediumText
import com.xayah.core.ui.component.BodySmallText
import com.xayah.core.ui.component.CheckIconButton
import com.xayah.core.ui.component.Divider
import com.xayah.core.ui.component.InnerBottomSpacer
import com.xayah.core.ui.component.InnerTopSpacer
import com.xayah.core.ui.component.LinearProgressIndicator
import com.xayah.core.ui.component.PackageIconImage
import com.xayah.core.ui.component.SecondaryTopBar
import com.xayah.core.ui.component.TitleLargeText
import com.xayah.core.ui.component.paddingBottom
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.component.paddingStart
import com.xayah.core.ui.component.paddingTop
import com.xayah.core.ui.component.paddingVertical
import com.xayah.core.ui.material3.Surface
import com.xayah.core.ui.material3.toColor
import com.xayah.core.ui.material3.tokens.ColorSchemeKeyTokens
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.RefreshState
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.AnimationTokens
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.fromString
import com.xayah.core.ui.util.fromStringArgs
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.ui.util.fromVector
import com.xayah.core.ui.util.value
import com.xayah.feature.main.packages.R

@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun ListScaffold(
    scrollBehavior: TopAppBarScrollBehavior,
    title: StringResourceToken,
    actions: @Composable RowScope.() -> Unit = {},
    progress: Float? = null,
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    innerBottomSpacer: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    content: @Composable (BoxScope.(innerPadding: PaddingValues) -> Unit)
) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                SecondaryTopBar(
                    scrollBehavior = scrollBehavior,
                    title = title,
                    actions = actions,
                    onBackClick = onBackClick,
                )
                var targetProgress by remember { mutableFloatStateOf(0f) }
                val animatedProgress = animateFloatAsState(
                    targetValue = targetProgress,
                    animationSpec = tween(),
                    label = AnimationTokens.AnimatedProgressLabel
                )
                if (progress != null) {
                    targetProgress = progress
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = animatedProgress.value)
                }
            }
        },
        floatingActionButtonPosition = floatingActionButtonPosition,
        floatingActionButton = floatingActionButton,
    ) { innerPadding ->
        Column {
            InnerTopSpacer(innerPadding = innerPadding)

            Box(modifier = Modifier.weight(1f), content = {
                content(this, innerPadding)
            })

            if (innerBottomSpacer) InnerBottomSpacer(innerPadding = innerPadding)
        }
    }
}

@Composable
fun DotLottieView(isRefreshing: Boolean, refreshState: RefreshState) {
    DotLottieAnimation(
        source = if (isRefreshing) DotLottieSource.Asset("loading.lottie") else DotLottieSource.Asset("squirrel.lottie"),
        autoplay = true,
        loop = true,
        playMode = Mode.FORWARD,
        modifier = Modifier.background(Color.Transparent)
    )
    if (isRefreshing) {
        BodyLargeText(text = refreshState.user, color = ColorSchemeKeyTokens.OnSurface.toColor())
        AnimatedLinearProgressIndicator(
            modifier = Modifier.paddingVertical(SizeTokens.Level8),
            progress = refreshState.progress,
            strokeCap = StrokeCap.Round
        )
        BodySmallText(text = refreshState.pkg, textAlign = TextAlign.Center, color = ColorSchemeKeyTokens.OnSurfaceVariant.toColor())
    } else {
        BodyLargeText(text = StringResourceToken.fromStringId(R.string.pull_down_to_refresh).value, textAlign = TextAlign.Center, color = ColorSchemeKeyTokens.OnSurfaceVariant.toColor())
    }
}

@Composable
fun DotLottieView() {
    DotLottieAnimation(
        source = DotLottieSource.Asset("bear.lottie"),
        autoplay = true,
        loop = true,
        playMode = Mode.FORWARD,
        modifier = Modifier.background(Color.Transparent)
    )
    BodyLargeText(
        text = StringResourceToken.fromStringId(R.string.no_backups_found_warning).value,
        color = ColorSchemeKeyTokens.OnSurfaceVariant.toColor(),
        textAlign = TextAlign.Center
    )
}

@ExperimentalLayoutApi
@ExperimentalFoundationApi
@Composable
fun PackageItem(item: PackageEntity, onCheckedChange: ((Boolean) -> Unit)?, filterMode: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick) {
        Column {
            Row(
                modifier = Modifier
                    .height(IntrinsicSize.Min)
                    .paddingTop(SizeTokens.Level16)
                    .paddingHorizontal(SizeTokens.Level16)
                    .then(if (filterMode.not()) Modifier.paddingBottom(SizeTokens.Level16) else Modifier),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level16)
            ) {
                PackageIconImage(packageName = item.packageName, label = "${item.packageInfo.label.firstOrNull() ?: ""}", size = SizeTokens.Level32)
                Column(modifier = Modifier.weight(1f)) {
                    TitleLargeText(text = item.packageInfo.label, color = ColorSchemeKeyTokens.OnSurface.toColor())
                    BodyMediumText(
                        text = StringResourceToken.fromStringArgs(
                            StringResourceToken.fromString("${item.packageName} ("),
                            StringResourceToken.fromStringId(R.string.user),
                            StringResourceToken.fromString(" ${item.userId})"),
                        ).value,
                        color = ColorSchemeKeyTokens.Outline.toColor()
                    )
                }

                Divider(
                    modifier = Modifier
                        .height(SizeTokens.Level36)
                        .width(SizeTokens.Level1)
                        .fillMaxHeight()
                )
                CheckIconButton(checked = item.extraInfo.activated, onCheckedChange = onCheckedChange)
            }

            AnimatedVisibility(visible = filterMode, enter = fadeIn() + slideInVertically(), exit = slideOutVertically() + fadeOut()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .paddingStart(SizeTokens.Level64)
                        .paddingBottom(SizeTokens.Level16),
                    horizontalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    verticalArrangement = Arrangement.spacedBy(SizeTokens.Level8),
                    content = {
                        val ssaid = item.extraInfo.ssaid
                        val hasKeystore = item.extraInfo.hasKeystore
                        val storageStatsFormat = item.storageStatsFormat

                        AssistChip(
                            enabled = true,
                            label = StringResourceToken.fromString(storageStatsFormat),
                            leadingIcon = ImageVectorToken.fromVector(Icons.Outlined.Folder),
                            trailingIcon = null,
                            color = ColorSchemeKeyTokens.Primary,
                            containerColor = ColorSchemeKeyTokens.PrimaryContainer,
                            border = null,
                        )
                        if (ssaid.isNotEmpty()) AssistChip(
                            enabled = true,
                            label = StringResourceToken.fromStringId(R.string.ssaid),
                            leadingIcon = ImageVectorToken.fromVector(Icons.Outlined.Pin),
                            trailingIcon = null,
                            color = ColorSchemeKeyTokens.Primary,
                            containerColor = ColorSchemeKeyTokens.PrimaryContainer,
                            border = null,
                        )
                        if (hasKeystore) AssistChip(
                            enabled = true,
                            label = StringResourceToken.fromStringId(R.string.keystore),
                            leadingIcon = ImageVectorToken.fromVector(Icons.Outlined.Key),
                            trailingIcon = null,
                            color = ColorSchemeKeyTokens.Primary,
                            containerColor = ColorSchemeKeyTokens.PrimaryContainer,
                            border = null,
                        )
                    }
                )
            }
        }
    }
}
