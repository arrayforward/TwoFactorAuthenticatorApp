# TwoFactorAuthenticator (双重认证器)

一款安全、轻量的 Android OTP 双重认证应用，支持 GitHub 及任何基于 TOTP 标准的两步验证。通过摄像头扫描二维码即可快速添加账号，也支持手动输入密钥。

## 功能特性

- **二维码扫描** — 使用摄像头扫描 GitHub、Google、Microsoft 等任何 TOTP 服务的 `otpauth://totp/` 二维码
- **手动添加** — 支持手动输入服务商、账号名和 Base32 密钥
- **TOTP 生成** — 符合 RFC 6238 标准，基于 HMAC-SHA1 算法生成 6 位动态验证码
- **实时倒计时** — 每个验证码配有进度条和剩余秒数显示
- **一键复制** — 点击验证码卡片即可复制到剪贴板
- **加密存储** — 所有密钥使用 Android EncryptedSharedPreferences 进行 AES-256 加密存储
- **深色主题** — Material 3 深色主题，视觉舒适
- **完全离线** — 无需网络权限，数据完全本地化

## 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 相机 | CameraX + ZXing 二维码解析 |
| 加密 | javax.crypto (HMAC-SHA1) + AndroidX Security Crypto |
| 架构 | MVVM + StateFlow |
| 导航 | Jetpack Navigation Compose |

## 项目结构

```
app/src/main/java/com/otp/authenticator/
├── MainActivity.kt              # 入口 Activity，页面导航
├── OtpApp.kt                    # Application 类
├── data/
│   ├── OtpAccount.kt            # 账号数据模型
│   └── OtpRepository.kt        # 加密持久化存储
├── otp/
│   └── TotpGenerator.kt        # RFC 6238 TOTP 算法实现
├── scanner/
│   └── QrCodeAnalyzer.kt       # CameraX + ZXing 二维码分析器
└── ui/
    ├── theme/
    │   ├── Color.kt             # 颜色定义
    │   └── Theme.kt             # Material 3 深色主题
    ├── screens/
    │   ├── HomeScreen.kt        # 主页 — 验证码列表
    │   ├── ScanScreen.kt        # 扫描页 — 相机扫码
    │   └── AddAccountScreen.kt  # 添加页 — 手动输入
    └── viewmodel/
        └── MainViewModel.kt     # UI 状态管理
```

## 构建指南

### 环境要求

- Android SDK 34+
- JDK 17
- Gradle 8.4+

### 构建与安装

```bash
# 克隆仓库
git clone https://github.com/arrayforward/TwoFactorAuthenticatorApp.git
cd TwoFactorAuthenticatorApp

# 构建 Debug APK
./gradlew assembleDebug

# 安装到已连接的设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 发布版构建

发布版需要签名密钥，将 `keystore.properties` 放在项目根目录：

```properties
storeFile=../release.keystore
storePassword=你的密钥库密码
keyAlias=你的密钥别名
keyPassword=你的密钥密码
```

```bash
# 构建签名 Release APK
./gradlew assembleRelease
```

## 权限说明

- `android.permission.CAMERA` — 仅用于扫描二维码

不请求网络、存储等其他权限。

## 使用说明

### 扫描添加（推荐）

1. 点击主页面右下角的蓝色扫描按钮
2. 授予相机权限
3. 将相机对准服务的两步验证设置二维码
4. 扫描成功后自动添加并返回主页

### 手动添加

1. 点击主页面右下角的绿色 + 按钮
2. 输入服务商名称（如 GitHub）
3. 输入账号标识（如用户名）
4. 输入 Base32 格式的密钥
5. 点击保存

### GitHub 两步验证设置

1. 登录 GitHub → Settings → Password and authentication
2. 点击 "Enable two-factor authentication"
3. 选择 "Authenticator app"
4. GitHub 会显示一个二维码
5. 用本应用扫描该二维码即可完成绑定

## 安全说明

- 所有密钥数据以 AES-256 加密存储在设备本地
- 使用 Android Keystore 系统生成加密主密钥
- 数据不会上传到任何服务器
- 建议同时妥善保管服务的恢复码

## 许可证

MIT License
