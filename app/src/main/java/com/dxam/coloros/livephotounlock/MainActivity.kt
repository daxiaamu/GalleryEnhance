package com.dxam.coloros.livephotounlock

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dxam.coloros.livephotounlock.update.CheckState
import com.dxam.coloros.livephotounlock.update.DownloadState
import com.dxam.coloros.livephotounlock.update.UpdateManager
import com.dxam.coloros.livephotounlock.update.UpdateManifest
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ModuleScreen(this) }
    }

    override fun onResume() {
        super.onResume()
        ModuleApplication.synchronizeScope()
        UpdateManager.resumeInstallation(this)
    }
}

private val Background = Color(0xFFF7F8FC)
private val Accent = Color(0xFF246BFD)
private val Success = Color(0xFF15805D)
private val Warning = Color(0xFFB75D00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModuleScreen(activity: MainActivity) {
    val context = LocalContext.current
    val galleryVersion = remember {
        runCatching {
            val info = context.packageManager.getPackageInfo(ModuleApplication.TARGET_PACKAGE, 0)
            "${info.versionName ?: "未知"} (${info.longVersionCode})"
        }.getOrDefault("未检测到")
    }
    val service = ModuleApplication.service
    val hasScope = ModuleApplication.hasRequiredScope()
    val active = service != null
    val ready = active && hasScope
    val statusColor = when {
        ready -> Success
        active -> Warning
        else -> Color(0xFF9B2C2C)
    }
    val statusTitle = when {
        ready -> "模块已就绪"
        active -> "需要授予相册作用域"
        else -> "未连接到 Xposed 框架"
    }
    val statusDetail = when {
        ready -> "ColorOS 相册已在作用域中，重启相册后即可解除实况导出时长限制。"
        active -> "框架已识别模块，但尚未勾选 ColorOS 相册。请在弹出的授权窗口中允许。"
        else -> "请确认模块已在支持 libxposed API 102 的框架中启用，然后返回此页面。"
    }

    LaunchedEffect(Unit) {
        delay(2_000)
        UpdateManager.checkAutomatically()
    }
    UpdateManager.manualMessage?.let { message ->
        LaunchedEffect(message) { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
    }

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Background,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("ColorOS 相册增强", fontWeight = FontWeight.Bold)
                            Text("实况导出时长解锁", style = MaterialTheme.typography.bodySmall, color = Color(0xFF60646C))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                )
            }
        ) { contentPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                StatusCard(statusColor, statusTitle, statusDetail, galleryVersion, active, hasScope)
                UpdateCard()
                AboutCard()
            }
        }
        UpdateManager.dialogUpdate?.let { UpdateDialog(activity, it) }
    }
}

@Composable
private fun StatusCard(statusColor: Color, title: String, detail: String, galleryVersion: String, active: Boolean, hasScope: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(statusColor, CircleShape))
                Text(title, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            Text(detail, color = Color(0xFF51555D), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Text("当前相册版本：$galleryVersion", color = Color(0xFF60646C), style = MaterialTheme.typography.bodySmall)
            ModuleApplication.scopeMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = statusColor, style = MaterialTheme.typography.bodySmall)
            }
            if (active && !hasScope) {
                Spacer(Modifier.height(18.dp))
                Button(onClick = ModuleApplication::synchronizeScope, colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                    Text("申请相册作用域")
                }
            }
        }
    }
}

@Composable
private fun UpdateCard() {
    val checking = UpdateManager.checkState == CheckState.Checking
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(Color.White)) {
        Row(modifier = Modifier.padding(start = 22.dp, end = 10.dp, top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("应用更新", fontWeight = FontWeight.SemiBold)
                    if (UpdateManager.hasUpdateBadge) {
                        Spacer(Modifier.width(7.dp))
                        Box(Modifier.size(8.dp).background(Color(0xFFE53935), CircleShape))
                    }
                }
                Text("当前版本 ${BuildConfig.VERSION_NAME}", color = Color(0xFF60646C), style = MaterialTheme.typography.bodySmall)
            }
            Box(Modifier.size(width = 64.dp, height = 48.dp), contentAlignment = Alignment.Center) {
                if (checking) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else TextButton(onClick = UpdateManager::checkManually) { Text("检查") }
            }
        }
    }
}

@Composable
private fun UpdateDialog(activity: MainActivity, update: UpdateManifest) {
    val required = update.isRequired(BuildConfig.VERSION_CODE.toLong())
    val download = UpdateManager.downloadState
    val busy = download is DownloadState.Downloading || download == DownloadState.Verifying
    BackHandler(required || busy) {}
    Dialog(
        onDismissRequest = { if (!required && !busy) UpdateManager.ignore(update) },
        properties = DialogProperties(dismissOnBackPress = !required && !busy, dismissOnClickOutside = !required && !busy)
    ) {
        Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(Modifier.padding(20.dp)) {
                Text(if (required) "必须更新" else "发现新版本", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                SelectionContainer {
                    Column(Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState())) {
                        Text("版本 ${update.versionName}", fontWeight = FontWeight.SemiBold)
                        update.publishedAt?.let { Text("发布时间：${formatPublishedAt(it)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                        Spacer(Modifier.height(12.dp))
                        SafeMarkdown(update.changelog)
                        (download as? DownloadState.Failed)?.let {
                            Spacer(Modifier.height(12.dp))
                            Text("下载失败：${it.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!required) {
                        TextButton(onClick = { UpdateManager.skip(update) }, enabled = !busy, contentPadding = PaddingValues(horizontal = 6.dp)) { Text("跳过此版本") }
                        TextButton(onClick = { UpdateManager.ignore(update) }, enabled = !busy, contentPadding = PaddingValues(horizontal = 6.dp)) { Text("忽略") }
                    } else Spacer(Modifier.weight(1f))
                    Spacer(Modifier.weight(1f))
                    Button(
                        onClick = {
                            when (download) {
                                is DownloadState.ReadyToInstall, DownloadState.NeedsAuthorization -> UpdateManager.install(activity)
                                else -> UpdateManager.download(update)
                            }
                        },
                        enabled = !busy && download != DownloadState.LaunchingInstaller,
                        modifier = Modifier.defaultMinSize(minWidth = 116.dp, minHeight = 48.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        when (download) {
                            is DownloadState.Downloading -> {
                                if (download.percent == null) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                else CircularProgressIndicator({ download.percent / 100f }, Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp)); Text(download.percent?.let { "$it%" } ?: "下载中")
                            }
                            DownloadState.Verifying -> { CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("校验中") }
                            is DownloadState.ReadyToInstall, DownloadState.NeedsAuthorization -> Text("安装")
                            is DownloadState.Failed -> Text("重试下载")
                            DownloadState.LaunchingInstaller -> Text("正在打开")
                            else -> Text("下载安装")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SafeMarkdown(markdown: String) {
    var inCode = false
    markdown.lines().forEach { raw ->
        if (raw.trim().startsWith("```")) { inCode = !inCode; return@forEach }
        val line = raw.trimEnd()
        if (line.matches(Regex("-{3,}"))) {
            HorizontalDivider(Modifier.padding(vertical = 8.dp)); return@forEach
        }
        val heading = line.takeWhile { it == '#' }.length.takeIf { it in 1..6 && line.getOrNull(it) == ' ' }
        val display = when {
            heading != null -> line.drop(heading + 1)
            line.startsWith("> ") -> "❯ ${line.drop(2)}"
            line.matches(Regex("^[-*+] .*")) -> "• ${line.drop(2)}"
            else -> line
        }
        Text(
            text = inlineMarkdown(display),
            modifier = Modifier.padding(vertical = if (display.isBlank()) 4.dp else 2.dp),
            fontWeight = if (heading != null) FontWeight.Bold else null,
            style = if (heading != null) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontFamily = if (inCode) FontFamily.Monospace else null
        )
    }
}

private fun inlineMarkdown(text: String) = buildAnnotatedString {
    val token = Regex("\\[([^]]+)]\\((https?://[^ )]+)\\)|\\*\\*([^*]+)\\*\\*|`([^`]+)`|(?<!\\*)\\*([^*]+)\\*(?!\\*)")
    var cursor = 0
    token.findAll(text).forEach { match ->
        append(text.substring(cursor, match.range.first))
        when {
            match.groups[1] != null -> withLink(LinkAnnotation.Url(match.groupValues[2])) {
                withStyle(SpanStyle(color = Accent, textDecoration = TextDecoration.Underline)) { append(match.groupValues[1]) }
            }
            match.groups[3] != null -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(match.groupValues[3]) }
            match.groups[4] != null -> withStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0xFFECEFF4))) { append(match.groupValues[4]) }
            else -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(match.groupValues[5]) }
        }
        cursor = match.range.last + 1
    }
    append(text.substring(cursor))
}


@Composable
private fun AboutCard() {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri("https://www.daxiaamu.com") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column(Modifier.padding(22.dp)) {
            Text("关于", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("作者：大侠阿木", color = Color(0xFF51555D))
            Text("进入大侠阿木博客", color = Accent, textDecoration = TextDecoration.Underline)
        }
    }
}
private val PublishedAtFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun formatPublishedAt(value: String): String = runCatching {
    Instant.parse(value).atZone(ZoneId.systemDefault()).format(PublishedAtFormatter)
}.getOrElse {
    value.replace('T', ' ').removeSuffix("Z").substringBefore('.')
}
