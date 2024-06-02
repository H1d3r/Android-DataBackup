package com.xayah.feature.main.settings.blacklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xayah.core.ui.component.IconButton
import com.xayah.core.ui.component.InnerBottomSpacer
import com.xayah.core.ui.component.LocalSlotScope
import com.xayah.core.ui.component.PackageItem
import com.xayah.core.ui.component.Title
import com.xayah.core.ui.component.confirm
import com.xayah.core.ui.component.paddingHorizontal
import com.xayah.core.ui.model.ImageVectorToken
import com.xayah.core.ui.model.StringResourceToken
import com.xayah.core.ui.token.SizeTokens
import com.xayah.core.ui.util.fromStringId
import com.xayah.core.ui.util.fromVector
import com.xayah.feature.main.settings.DotLottieView
import com.xayah.feature.main.settings.R
import com.xayah.feature.main.settings.SettingsScaffold

@ExperimentalFoundationApi
@ExperimentalLayoutApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@Composable
fun PageBlackList() {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val dialogState = LocalSlotScope.current!!.dialogSlot
    val viewModel = hiltViewModel<IndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val packagesState by viewModel.packagesState.collectAsStateWithLifecycle()

    SettingsScaffold(
        scrollBehavior = scrollBehavior,
        title = StringResourceToken.fromStringId(R.string.blacklist),
        actions = {
            AnimatedVisibility(visible = uiState.appIds.isNotEmpty()) {
                IconButton(icon = ImageVectorToken.fromVector(Icons.Outlined.Delete)) {
                    viewModel.launchOnIO {
                        if (dialogState.confirm(title = StringResourceToken.fromStringId(R.string.prompt), text = StringResourceToken.fromStringId(R.string.confirm_remove_from_blacklist))) {
                            viewModel.emitIntentOnIO(IndexUiIntent.RemoveSelected)
                        }
                    }
                }
            }
            if (packagesState.isNotEmpty())
                IconButton(icon = ImageVectorToken.fromVector(Icons.Outlined.Checklist)) {
                    viewModel.emitIntentOnIO(IndexUiIntent.SelectAll)
                }
        }
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                if (packagesState.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(modifier = Modifier.paddingHorizontal(SizeTokens.Level16), horizontalAlignment = Alignment.CenterHorizontally) {
                            DotLottieView()
                        }
                    }
                } else {
                    Title(title = StringResourceToken.fromStringId(R.string.apps)) {}
                }
            }

            items(items = packagesState, key = { "apps-${it.id}" }) { item ->
                Row(modifier = Modifier.animateItemPlacement()) {
                    PackageItem(
                        item = item,
                        checked = item.id in uiState.appIds,
                        onCheckedChange = {
                            viewModel.emitIntentOnIO(IndexUiIntent.Select(item.id))
                        },
                        onClick = {
                            viewModel.emitIntentOnIO(IndexUiIntent.Select(item.id))
                        },
                        filterMode = false
                    )
                }
            }

            item {
                InnerBottomSpacer(innerPadding = it)
            }
        }
    }
}
