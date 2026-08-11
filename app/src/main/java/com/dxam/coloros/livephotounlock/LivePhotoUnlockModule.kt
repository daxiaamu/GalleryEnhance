package com.dxam.coloros.livephotounlock

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import org.luckypray.dexkit.DexKitBridge
import java.io.File
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.zip.ZipFile

class LivePhotoUnlockModule : XposedModule() {
    companion object {
        private const val TAG = "ColorOSLivePhotoUnlock"
        private const val TARGET_PACKAGE = "com.coloros.gallery3d"
        private const val CONFIG_KEY = "video_editor_olive_save_max_duration"
        private const val KNOWN_CONFIG_CLASS = "com.oplus.aiunit.vision.k7a"
        private const val UNRESTRICTED_SECONDS = 2_000_000
        private const val DEXKIT_LIBRARY = "libdexkit.so"
    }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(Log.INFO, TAG, "Loaded with libxposed API $apiVersion")
    }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        if (param.packageName != TARGET_PACKAGE || !param.isFirstPackage) return

        runCatching {
            val dexKitReady = loadDexKit()
            val target = findConfigMethod(param.defaultClassLoader, dexKitReady)
            hook(target).intercept { chain ->
                if (chain.getArg(0) == CONFIG_KEY) UNRESTRICTED_SECONDS else chain.proceed()
            }
            log(Log.INFO, TAG, "Duration gate removed via ${target.declaringClass.name}#${target.name}; dexKit=$dexKitReady")
        }.onFailure {
            log(Log.ERROR, TAG, "Unable to locate the motion-photo duration config", it)
        }
    }

    private fun loadDexKit(): Boolean = runCatching {
        val installed = File(moduleApplicationInfo.nativeLibraryDir, DEXKIT_LIBRARY)
        if (installed.isFile) {
            System.load(installed.absolutePath)
        } else {
            val outputDir = File(System.getProperty("java.io.tmpdir"), "coloros-live-photo-dexkit").apply { mkdirs() }
            val output = File(outputDir, DEXKIT_LIBRARY)
            ZipFile(moduleApplicationInfo.sourceDir).use { zip ->
                val entry = zip.getEntry("lib/arm64-v8a/$DEXKIT_LIBRARY")
                    ?: error("DexKit arm64 library is missing")
                if (!output.isFile || output.length() != entry.size) {
                    zip.getInputStream(entry).use { input ->
                        output.outputStream().use { input.copyTo(it) }
                    }
                }
            }
            System.load(output.absolutePath)
        }
        log(Log.INFO, TAG, "DexKit native library loaded")
        true
    }.getOrElse {
        log(Log.WARN, TAG, "DexKit unavailable; using the verified symbol fallback", it)
        false
    }

    private fun findConfigMethod(classLoader: ClassLoader, dexKitReady: Boolean): Method {
        if (dexKitReady) {
            runCatching {
                DexKitBridge.create(classLoader, true).use { bridge ->
                    val callers = bridge.findMethod {
                        searchPackages("com.oplus.gallery.videoeditorpage")
                        matcher { usingStrings(CONFIG_KEY) }
                    }
                    val candidates = callers.flatMap { it.invokes }.filter {
                        Modifier.isStatic(it.modifiers) &&
                            it.returnTypeName == "int" &&
                            it.paramTypeNames == listOf("java.lang.String", "int")
                    }.distinctBy { it.descriptor }

                    candidates.singleOrNull()?.let { return it.getMethodInstance(classLoader) }
                }
            }.onFailure {
                log(Log.WARN, TAG, "DexKit lookup failed; trying known 16.45.2 symbol", it)
            }
        }

        return Class.forName(KNOWN_CONFIG_CLASS, false, classLoader)
            .getDeclaredMethod("e", String::class.java, Int::class.javaPrimitiveType)
            .apply { isAccessible = true }
    }
}