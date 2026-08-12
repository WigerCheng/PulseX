# PulseX - 心率看板

PulseX 是一款专为 Android 打造的心率监测与记录工具。它采用最前沿的 Android 技术栈，通过蓝牙 BLE 连接心率设备，提供实时的视觉反馈和详尽的数据记录。

## 🌟 功能特性

- **实时心率监测**：通过蓝牙 BLE 连接标准 GATT 心率服务，提供毫秒级的数据更新。
- **M3 Expressive 设计**：完全遵循 Material 3 Expressive 设计语言，拥有灵动的交互动画（如心跳脉冲效果、入场弹性动画）。
- **Session 记录**：支持手动开启/停止运动 Session，自动统计最高心率、最低心率及平均值，并生成变动趋势图表。
- **桌面微件 (Glance)**：提供基于 Jetpack Glance 的桌面微件，无需打开 App 即可在主屏幕实时关注心率。
- **Live Update (Android 16+)**：率先适配 Android 16 的实时更新通知，在锁屏和状态栏提供卓越的持续监控体验。
- **历史回溯**：完整的 Session 历史列表，支持查看详情、删除及撤销删除。
- **智能连接管理**：支持自动发现、手动连接/断开，以及基于 BroadcastReceiver 的跨组件状态分发。

## 🛠 技术栈

- **UI 框架**：[Jetpack Compose](https://developer.android.com/jetpack/compose) (M3 Expressive)
- **架构模式**：MVI (Model-View-Intent)
- **依赖注入**：[Hilt](https://dagger.dev/hilt/)
- **桌面微件**：[Jetpack Glance](https://developer.android.com/jetpack/compose/glance)
- **后台任务**：Lifecycle-aware Foreground Service
- **数据存储**：Room (Database), DataStore (Preferences)
- **异步处理**：Kotlin Coroutines & Flow
- **权限管理**：适配 Android 12+ 蓝牙权限模型及 Android 13+ 通知权限。

## 📱 页面介绍

- **首页 (Home)**：实时心率显示卡片（带脉冲动画）、Session 控制面板（包含实时迷你图表）及最近一次 Session 快照。
- **记录 (History)**：按时间顺序排列的运动记录列表。
- **设备 (Scan)**：强大的蓝牙扫描器，支持显示设备信号强度及连接状态。
- **设置 (Settings)**：权限状态看板、应用版本信息及桌面微件添加引导。

## 🚀 快速开始

1. **环境要求**：Android Studio Ladybug+，设备需支持蓝牙 BLE。
2. **构建项目**：
   ```bash
   ./gradlew assembleDebug
   ```
3. **运行**：部署到 Android 8.0+ 的实机或支持蓝牙的模拟器上。

---
**PulseX** - 每一拍心跳，都清晰可见。
