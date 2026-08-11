# ColorOS 相册实况时长解锁

[![Release](https://img.shields.io/github/v/release/daxiaamu/GalleryEnhance?display_name=tag&sort=semver)](https://github.com/daxiaamu/GalleryEnhance/releases/latest)
[![APK downloads](https://img.shields.io/github/downloads/daxiaamu/GalleryEnhance/total?logo=github)](https://github.com/daxiaamu/GalleryEnhance/releases)
[![Download APK](https://img.shields.io/badge/下载-APK-246BFD?logo=android&logoColor=white)](https://github.com/daxiaamu/GalleryEnhance/releases/latest)
[![License](https://img.shields.io/github/license/daxiaamu/GalleryEnhance)](LICENSE)
[![Update manifest](https://github.com/daxiaamu/GalleryEnhance/actions/workflows/publish-update-manifest.yml/badge.svg)](https://github.com/daxiaamu/GalleryEnhance/actions/workflows/publish-update-manifest.yml)

[![Android 13+](https://img.shields.io/badge/Android-13%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/about/versions/13)
[![Kotlin 2.3.10](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![libxposed API 102](https://img.shields.io/badge/libxposed_API-102-ED1C24)](https://github.com/libxposed/api)
[![minSdk 33](https://img.shields.io/badge/minSdk-33-3DDC84)](app/build.gradle.kts)
[![targetSdk 36](https://img.shields.io/badge/targetSdk-36-3DDC84)](app/build.gradle.kts)

[![Stars](https://img.shields.io/github/stars/daxiaamu/GalleryEnhance?style=flat&logo=github)](https://github.com/daxiaamu/GalleryEnhance/stargazers)
[![Forks](https://img.shields.io/github/forks/daxiaamu/GalleryEnhance?style=flat&logo=github)](https://github.com/daxiaamu/GalleryEnhance/forks)
[![Issues](https://img.shields.io/github/issues/daxiaamu/GalleryEnhance)](https://github.com/daxiaamu/GalleryEnhance/issues)
[![Last commit](https://img.shields.io/github/last-commit/daxiaamu/GalleryEnhance)](https://github.com/daxiaamu/GalleryEnhance/commits/main)
[![Repo size](https://img.shields.io/github/repo-size/daxiaamu/GalleryEnhance)](https://github.com/daxiaamu/GalleryEnhance)

[【机场推荐：白月光，稳定高速】](https://www.sibker.com/register?invite_code=2XQR1UUz)

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
## 应用内更新

主界面启动约 2 秒后会自动检查 stable 更新，也可以在“应用更新”卡片中手动检查。更新清单以 GitHub Contents API 为权威源，并使用多个 Raw/CDN 镜像做容灾；APK 下载后必须依次通过 SHA-256、包名、versionCode 和签名证书校验，随后交给系统安装器。

发布新版本前，先人工更新 `update/policy-stable.json`（预发布版本使用 `policy-beta.json`）并递增 `policyRevision`。发布 GitHub Release 且只附带一个 APK 后，`publish-update-manifest.yml` 会从 APK 读取版本、验证至少 5 个不同主机的完整 APK 哈希，并生成对应的 `update.json`。镜像不足或校验失败时不会覆盖旧清单。

“关于”区域提供作者信息及 [大侠阿木博客](https://www.daxiaamu.com) 入口。
## 许可证

本项目采用 [GNU General Public License v3.0 or later](LICENSE) 发布，SPDX 标识为 `GPL-3.0-or-later`。
