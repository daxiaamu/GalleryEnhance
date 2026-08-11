package com.dxam.coloros.livephotounlock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ModuleScreen() }
    }

    override fun onResume() {
        super.onResume()
        ModuleApplication.synchronizeScope()
    }
}

private val Background = Color(0xFFF7F8FC)
private val Accent = Color(0xFF246BFD)
private val Success = Color(0xFF15805D)
private val Warning = Color(0xFFB75D00)

@Composable
private fun ModuleScreen() {
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

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Background) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text("ColorOS 相册增强", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("实况导出时长解锁", color = Color(0xFF60646C))

                StatusCard(statusColor, statusTitle, statusDetail, active, hasScope)
            }
        }
    }
}

@Composable
private fun StatusCard(statusColor: Color, title: String, detail: String, active: Boolean, hasScope: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(12.dp).background(statusColor, CircleShape))
                Text(title, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(14.dp))
            Text(detail, color = Color(0xFF51555D), style = MaterialTheme.typography.bodyMedium)
            ModuleApplication.scopeMessage?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = statusColor, style = MaterialTheme.typography.bodySmall)
            }
            if (active && !hasScope) {
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = ModuleApplication::synchronizeScope,
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) { Text("申请相册作用域") }
            }
        }
    }
}