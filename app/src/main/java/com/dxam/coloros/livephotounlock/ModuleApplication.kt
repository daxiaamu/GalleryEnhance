package com.dxam.coloros.livephotounlock

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

class ModuleApplication : Application(), XposedServiceHelper.OnServiceListener {
    companion object {
        const val TARGET_PACKAGE = "com.coloros.gallery3d"

        var service by mutableStateOf<XposedService?>(null)
            private set
        var scopeRevision by mutableIntStateOf(0)
            private set
        var scopeMessage by mutableStateOf<String?>(null)
            private set

        private var requestInFlight = false

        fun hasRequiredScope(): Boolean {
            scopeRevision
            return service?.scope?.contains(TARGET_PACKAGE) == true
        }

        fun synchronizeScope() {
            val current = service ?: return
            if (hasRequiredScope() || requestInFlight) return
            requestInFlight = true
            scopeMessage = "正在请求相册作用域…"
            current.requestScope(
                listOf(TARGET_PACKAGE),
                object : XposedService.OnScopeEventListener {
                    override fun onScopeRequestApproved(approved: List<String>) {
                        requestInFlight = false
                        scopeRevision++
                        scopeMessage = if (approved.contains(TARGET_PACKAGE)) {
                            "相册作用域已同步"
                        } else {
                            "未授予相册作用域，请点击下方按钮重试"
                        }
                    }

                    override fun onScopeRequestFailed(message: String) {
                        requestInFlight = false
                        scopeRevision++
                        scopeMessage = "作用域同步失败：$message"
                    }
                }
            )
        }
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(boundService: XposedService) {
        service = boundService
        scopeRevision++
        scopeMessage = null
        synchronizeScope()
    }

    override fun onServiceDied(deadService: XposedService) {
        if (service === deadService) service = null
        requestInFlight = false
        scopeRevision++
    }
}