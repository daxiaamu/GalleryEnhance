# ColorOS 相册实况时长解锁

libxposed API 102 模块，仅作用于 `com.coloros.gallery3d`。

模块使用 DexKit 2.2.0 根据稳定配置字符串动态定位混淆方法，并为 ColorOS 相册
16.45.2 保留符号兜底。它拦截 `video_editor_olive_save_max_duration` 的读取，
解除视频导出为实况照片时的 3 秒限制，不修改其他编辑功能。

Compose 页面只显示模块与相册作用域状态。作用域缺失时，模块会通过 API 102
自动请求同步，并提供手动重试入口。

## 构建

```powershell
.\gradlew.bat :app:assembleRelease
```

安装后在支持 libxposed API 102 的框架中启用模块。首次打开模块页面并允许相册
作用域，然后强制停止相册或重启设备。