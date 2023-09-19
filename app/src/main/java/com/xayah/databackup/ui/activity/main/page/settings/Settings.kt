package com.xayah.databackup.ui.activity.main.page.settings

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import com.xayah.databackup.BuildConfig
import com.xayah.databackup.DataBackupApplication
import com.xayah.databackup.R
import com.xayah.databackup.ui.component.SettingsGridItem
import com.xayah.databackup.ui.component.SettingsGridItemConfig
import com.xayah.databackup.ui.component.SettingsModalDropdownMenu
import com.xayah.databackup.ui.component.SettingsSwitch
import com.xayah.databackup.ui.component.SettingsTitle
import com.xayah.databackup.ui.component.VerticalGrid
import com.xayah.databackup.ui.component.paddingHorizontal
import com.xayah.databackup.ui.component.paddingVertical
import com.xayah.databackup.ui.token.CommonTokens
import com.xayah.databackup.util.CompressionType
import com.xayah.databackup.util.readBackupUserId
import com.xayah.databackup.util.readCompressionType
import com.xayah.databackup.util.readKeepScreenOn
import com.xayah.databackup.util.readMonetEnabled
import com.xayah.databackup.util.readRestoreUserId
import com.xayah.databackup.util.saveBackupUserId
import com.xayah.databackup.util.saveCompressionType
import com.xayah.databackup.util.saveKeepScreenOn
import com.xayah.databackup.util.saveMonetEnabled
import com.xayah.databackup.util.saveRestoreUserId
import com.xayah.librootservice.service.RemoteRootService
import com.xayah.librootservice.util.ExceptionUtil.tryService

@Composable
private fun InfoCard() {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .paddingHorizontal(CommonTokens.PaddingMedium)
    ) {
        val context = LocalContext.current
        val items = remember {
            listOf(
                SettingsGridItemConfig(
                    icon = ImageVector.vectorResource(context.theme, context.resources, R.drawable.ic_rounded_apps),
                    title = context.getString(R.string.version),
                    content = BuildConfig.VERSION_NAME
                ),
                SettingsGridItemConfig(
                    icon = ImageVector.vectorResource(context.theme, context.resources, R.drawable.ic_rounded_star),
                    title = context.getString(R.string.feature),
                    content = BuildConfig.FLAVOR_feature
                ),
                SettingsGridItemConfig(
                    icon = ImageVector.vectorResource(context.theme, context.resources, R.drawable.ic_rounded_app_registration),
                    title = context.getString(R.string.abi),
                    content = BuildConfig.FLAVOR_abi
                ),
                SettingsGridItemConfig(
                    icon = ImageVector.vectorResource(context.theme, context.resources, R.drawable.ic_rounded_phone_android),
                    title = context.getString(R.string.architecture),
                    content = Build.SUPPORTED_ABIS.firstOrNull() ?: context.getString(R.string.loading_failed)
                ),
            )
        }
        VerticalGrid(columns = 2, count = items.size) { index ->
            SettingsGridItem(modifier = Modifier.padding(CommonTokens.PaddingMedium), config = items[index])
        }
    }
}

@Composable
private fun ApplicationSettings() {
    val context = LocalContext.current
    SettingsSwitch(
        icon = ImageVector.vectorResource(R.drawable.ic_round_auto_awesome),
        title = stringResource(R.string.monet),
        defaultValue = remember { context.readMonetEnabled() },
        content = stringResource(R.string.monet_desc)
    ) {
        context.saveMonetEnabled(it)
        DataBackupApplication.monetEnabled.value = it
    }
    SettingsSwitch(
        icon = ImageVector.vectorResource(R.drawable.ic_rounded_brightness_high),
        title = stringResource(R.string.keep_screen_on),
        defaultValue = remember { context.readKeepScreenOn() },
        content = stringResource(R.string.keep_screen_on_desc)
    ) {
        context.saveKeepScreenOn(it)
    }

    val ctList = remember { listOf(CompressionType.TAR, CompressionType.ZSTD, CompressionType.LZ4).map { it.type } }
    val ctIndex = remember { ctList.indexOf(context.readCompressionType().type) }
    SettingsModalDropdownMenu(
        icon = ImageVector.vectorResource(R.drawable.ic_rounded_bolt),
        title = stringResource(R.string.compression_type),
        content = stringResource(R.string.compression_type_desc),
        defaultValue = ctIndex,
        list = ctList
    ) { _, selected ->
        context.saveCompressionType(CompressionType.of(selected))
    }
}

@Composable
private fun BackupUserSettings() {
    val context = LocalContext.current
    var userIdList: List<Int> by remember { mutableStateOf(listOf(context.readBackupUserId())) }
    var userList: List<String> by remember { mutableStateOf(listOf()) }
    var userIdIndex: Int by remember { mutableIntStateOf(0) }
    SettingsModalDropdownMenu(
        icon = ImageVector.vectorResource(R.drawable.ic_rounded_person),
        title = stringResource(R.string.backup_user),
        content = stringResource(R.string.backup_user_desc),
        selectedIndex = userIdIndex,
        displayValue = userIdList[userIdIndex].toString(),
        onLoading = {
            val remoteRootService = RemoteRootService(context)
            tryService(onFailed = { msg ->
                Toast.makeText(context, "$msg\n${context.getString(R.string.remote_service_err_info)}", Toast.LENGTH_LONG).show()
            }) {
                val oldUserId = userIdList[userIdIndex]
                val users = remoteRootService.getUsers()
                userList = users.map { "${it.id}: ${it.name}" }
                userIdList = users.map { it.id }
                userIdIndex = userIdList.indexOf(oldUserId)
                if (userIdIndex == -1) {
                    // UserId doesn't exist any more.
                    userIdIndex = 0
                    context.saveBackupUserId(userIdList[userIdIndex])
                }
                remoteRootService.destroyService()
            }
            userList
        }
    ) { index, _ ->
        userIdIndex = index
        context.saveBackupUserId(userIdList[userIdIndex])
    }
}

@Composable
private fun RestoreUserSettings() {
    val context = LocalContext.current
    var userIdList: List<Int> by remember { mutableStateOf(listOf(context.readRestoreUserId())) }
    var userList: List<String> by remember { mutableStateOf(listOf()) }
    var userIdIndex: Int by remember { mutableIntStateOf(0) }
    SettingsModalDropdownMenu(
        icon = ImageVector.vectorResource(R.drawable.ic_rounded_phone_android),
        title = stringResource(R.string.restore_user),
        content = stringResource(R.string.restore_user_desc),
        selectedIndex = userIdIndex,
        displayValue = userIdList[userIdIndex].toString(),
        onLoading = {
            val remoteRootService = RemoteRootService(context)
            tryService(onFailed = { msg ->
                Toast.makeText(context, "$msg\n${context.getString(R.string.remote_service_err_info)}", Toast.LENGTH_LONG).show()
            }) {
                val oldUserId = userIdList[userIdIndex]
                val users = remoteRootService.getUsers()
                userList = users.map { "${it.id}: ${it.name}" }
                userIdList = users.map { it.id }
                userIdIndex = userIdList.indexOf(oldUserId)
                if (userIdIndex == -1) {
                    // UserId doesn't exist any more.
                    userIdIndex = 0
                    context.saveRestoreUserId(userIdList[userIdIndex])
                }
                remoteRootService.destroyService()
            }
            userList
        }
    ) { index, _ ->
        userIdIndex = index
        context.saveRestoreUserId(userIdList[userIdIndex])
    }
}

@Composable
private fun UserSettings() {
    BackupUserSettings()
    RestoreUserSettings()
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Composable
fun PageSettings() {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Spacer(modifier = Modifier.paddingVertical(CommonTokens.PaddingMedium))
        InfoCard()
        Divider(modifier = Modifier.padding(CommonTokens.PaddingMedium))
        ApplicationSettings()
        SettingsTitle(modifier = Modifier.paddingVertical(CommonTokens.PaddingMedium), title = stringResource(R.string.user))
        UserSettings()
        Spacer(modifier = Modifier.paddingVertical(CommonTokens.PaddingMedium))
    }
}