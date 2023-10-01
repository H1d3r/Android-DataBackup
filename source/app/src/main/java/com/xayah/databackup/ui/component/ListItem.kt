package com.xayah.databackup.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.core.graphics.drawable.toDrawable
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.xayah.databackup.R
import com.xayah.databackup.data.DirectoryEntity
import com.xayah.databackup.data.OperationMask
import com.xayah.databackup.data.PackageBackupEntire
import com.xayah.databackup.data.PackageRestoreEntire
import com.xayah.databackup.data.StorageType
import com.xayah.databackup.ui.activity.directory.page.DirectoryViewModel
import com.xayah.databackup.ui.component.material3.Card
import com.xayah.databackup.ui.component.material3.outlinedCardBorder
import com.xayah.databackup.ui.theme.ColorScheme
import com.xayah.databackup.ui.token.CommonTokens
import com.xayah.databackup.ui.token.ListItemTokens
import com.xayah.databackup.util.PathUtil
import com.xayah.librootservice.util.ExceptionUtil.tryOn
import com.xayah.librootservice.util.withIOContext
import kotlinx.coroutines.launch
import java.io.File
import com.xayah.databackup.ui.activity.operation.page.packages.backup.ListViewModel as BackupListViewModel
import com.xayah.databackup.ui.activity.operation.page.packages.restore.ListViewModel as RestoreListViewModel

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun ListItemPackage(
    modifier: Modifier = Modifier,
    packageName: String,
    label: String,
    icon: Any,
    apkSelected: Boolean,
    dataSelected: Boolean,
    onApkSelected: () -> Unit,
    onDataSelected: () -> Unit,
    selected: Boolean,
    onCardClick: () -> Unit,
    onCardLongClick: () -> Unit,
    chipGroup: @Composable RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        onClick = onCardClick,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onCardLongClick()
        },
        border = if (selected) outlinedCardBorder(lineColor = ColorScheme.primary()) else null,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(ListItemTokens.PaddingMedium)
        ) {
            Row(
                modifier = Modifier
                    .paddingHorizontal(ListItemTokens.PaddingMedium)
                    .paddingTop(ListItemTokens.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ListItemTokens.PaddingSmall)
            ) {
                AsyncImage(
                    modifier = Modifier.size(ListItemTokens.IconSize),
                    model = ImageRequest.Builder(context)
                        .data(icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = null
                )
                Column(modifier = Modifier.weight(1f)) {
                    TitleMediumBoldText(text = label)
                    LabelSmallText(text = packageName)
                }
                if (selected) Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Top),
                    tint = ColorScheme.primary(),
                )
            }
            Row(
                modifier = Modifier.paddingHorizontal(ListItemTokens.PaddingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(ListItemTokens.PaddingSmall),
                    content = {
                        chipGroup()
                    }
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ColorScheme.inverseOnSurface())
                    .paddingHorizontal(ListItemTokens.PaddingMedium),
                horizontalArrangement = Arrangement.spacedBy(ListItemTokens.PaddingMedium, Alignment.End)
            ) {
                ApkChip(selected = apkSelected, onClick = onApkSelected)
                DataChip(selected = dataSelected, onClick = onDataSelected)
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun ListItemPackageBackup(
    modifier: Modifier = Modifier,
    packageInfo: PackageBackupEntire,
    selectionMode: Boolean,
    onSelectedChange: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<BackupListViewModel>()
    val scope = rememberCoroutineScope()
    var icon by remember { mutableStateOf<Any>(0) }
    val selected by packageInfo.selected

    ListItemPackage(
        modifier = modifier,
        packageName = packageInfo.packageName,
        label = packageInfo.label,
        icon = icon,
        apkSelected = OperationMask.isApkSelected(packageInfo.operationCode),
        dataSelected = OperationMask.isDataSelected(packageInfo.operationCode),
        onApkSelected = {
            scope.launch {
                withIOContext {
                    packageInfo.operationCode = packageInfo.operationCode xor OperationMask.Apk
                    viewModel.updatePackage(packageInfo)
                }
            }
        },
        onDataSelected = {
            scope.launch {
                withIOContext {
                    packageInfo.operationCode = packageInfo.operationCode xor OperationMask.Data
                    viewModel.updatePackage(packageInfo)
                }
            }
        },
        selected = selected,
        onCardClick = {
            if (selectionMode.not()) {
                scope.launch {
                    withIOContext {
                        packageInfo.operationCode =
                            if (packageInfo.operationCode == OperationMask.Both) OperationMask.None else OperationMask.Both
                        viewModel.updatePackage(packageInfo)
                    }
                }
            } else {
                onSelectedChange()
            }
        },
        onCardLongClick = onSelectedChange
    ) {
        if (packageInfo.versionName.isNotEmpty()) Serial(serial = packageInfo.versionName)
        Serial(serial = packageInfo.sizeDisplay)
    }

    LaunchedEffect(null) {
        // Read icon from cached internal dir.
        withIOContext {
            tryOn {
                val bytes = File(PathUtil.getIconPath(context, packageInfo.packageName)).readBytes()
                icon = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).toDrawable(context.resources)
            }
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun ListItemPackageRestore(
    modifier: Modifier = Modifier,
    packageInfo: PackageRestoreEntire,
    selectionMode: Boolean,
    onSelectedChange: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<RestoreListViewModel>()
    val scope = rememberCoroutineScope()
    var icon by remember { mutableStateOf<Any>(0) }
    val selected by packageInfo.selected

    ListItemPackage(
        modifier = modifier,
        packageName = packageInfo.packageName,
        label = packageInfo.label,
        icon = icon,
        apkSelected = OperationMask.isApkSelected(packageInfo.operationCode),
        dataSelected = OperationMask.isDataSelected(packageInfo.operationCode),
        onApkSelected = {
            scope.launch {
                withIOContext {
                    packageInfo.operationCode = packageInfo.operationCode xor OperationMask.Apk
                    viewModel.updatePackage(packageInfo)
                }
            }
        },
        onDataSelected = {
            scope.launch {
                withIOContext {
                    packageInfo.operationCode = packageInfo.operationCode xor OperationMask.Data
                    viewModel.updatePackage(packageInfo)
                }
            }
        },
        selected = selected,
        onCardClick = {
            if (selectionMode.not()) {
                scope.launch {
                    withIOContext {
                        packageInfo.operationCode =
                            if (packageInfo.operationCode == OperationMask.Both) OperationMask.None else OperationMask.Both
                        viewModel.updatePackage(packageInfo)
                    }
                }
            } else {
                onSelectedChange()
            }
        },
        onCardLongClick = onSelectedChange
    ) {
        if (packageInfo.versionName.isNotEmpty()) Serial(serial = packageInfo.versionName)
    }

    LaunchedEffect(null) {
        // Read icon from cached internal dir.
        withIOContext {
            tryOn {
                val bytes = File(PathUtil.getIconPath(context, packageInfo.packageName)).readBytes()
                icon = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).toDrawable(context.resources)
            }
        }
    }
}

@Composable
fun ListItemManifest(icon: ImageVector, title: String, content: String, onButtonClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(
            modifier = Modifier.size(ListItemTokens.ManifestIconButtonSize),
            onClick = onButtonClick
        ) {
            Icon(
                modifier = Modifier.size(ListItemTokens.ManifestIconSize),
                imageVector = icon,
                contentDescription = null
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .paddingHorizontal(ListItemTokens.PaddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TitleMediumBoldText(text = title)
            HeadlineLargeBoldText(text = content)
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterial3Api
@Composable
fun ListItemDirectory(
    modifier: Modifier = Modifier,
    entity: DirectoryEntity,
    onCardClick: () -> Unit,
    chipGroup: @Composable RowScope.() -> Unit,
) {
    val context = LocalContext.current
    val viewModel = hiltViewModel<DirectoryViewModel>()
    val scope = rememberCoroutineScope()
    val dialogSlot = LocalSlotScope.current!!.dialogSlot
    val haptic = LocalHapticFeedback.current
    val selected = entity.selected
    val enabled = entity.enabled
    val path = entity.path
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        enabled = enabled,
        onClick = onCardClick,
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            expanded = true
        },
        border = if (selected) outlinedCardBorder(lineColor = ColorScheme.primary()) else null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ListItemTokens.PaddingMedium)
        ) {
            Column {
                Row {
                    HeadlineMediumBoldText(text = entity.title)
                    Spacer(modifier = Modifier.weight(1f))
                    if (selected) Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Top),
                        tint = ColorScheme.primary(),
                    )
                }
                BodySmallBoldText(text = path)
                if (entity.error.isNotEmpty()) BodySmallBoldText(text = entity.error, color = ColorScheme.error(), enabled = enabled)
                Divider(modifier = Modifier.paddingVertical(ListItemTokens.PaddingSmall))
                Row(
                    modifier = Modifier.paddingBottom(ListItemTokens.PaddingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ListItemTokens.PaddingMedium)
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .clip(CircleShape)
                            .weight(1f),
                        color = if (enabled) ColorScheme.primary() else ColorScheme.primary().copy(alpha = CommonTokens.DisabledAlpha),
                        trackColor = if (enabled) ColorScheme.inverseOnSurface() else ColorScheme.inverseOnSurface().copy(alpha = CommonTokens.DisabledAlpha),
                        progress = entity.availableBytes.toFloat() / entity.totalBytes
                    )
                    Box(modifier = Modifier.wrapContentSize(Alignment.Center)) {
                        val actions = remember(entity) {
                            listOf(
                                ActionMenuItem(
                                    title = context.getString(R.string.delete),
                                    icon = Icons.Rounded.Delete,
                                    enabled = entity.storageType == StorageType.CUSTOM,
                                    onClick = {
                                        scope.launch {
                                            withIOContext {
                                                expanded = false
                                                dialogSlot.openConfirmDialog(context, context.getString(R.string.confirm_delete)).also { (confirmed, _) ->
                                                    if (confirmed) {
                                                        if (entity.selected) viewModel.reset(context = context)
                                                        viewModel.delete(entity)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                            )
                        }

                        LabelSmallText(text = "${entity.availableBytesDisplay} / ${entity.totalBytesDisplay}")

                        ModalActionDropdownMenu(expanded = expanded, actionList = actions, onDismissRequest = { expanded = false })
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(ListItemTokens.PaddingSmall),
                    content = {
                        chipGroup()
                    }
                )
            }
        }
    }
}
