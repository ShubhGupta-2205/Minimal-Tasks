package com.example.util

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

object PermissionHelper {

    /**
     * Checks whether the app has permission to draw overlays (display over other apps / lock screen)
     */
    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    /**
     * Checks whether the app is whitelisted from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true
        }
    }

    /**
     * Checks whether exact alarms can be scheduled (Android 12+)
     */
    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Checks whether notification permission is granted (Android 13+)
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Checks if all critical background & alarm permissions are granted
     */
    fun areAllAlarmPermissionsGranted(context: Context): Boolean {
        return canDrawOverlays(context) &&
                isIgnoringBatteryOptimizations(context) &&
                canScheduleExactAlarms(context) &&
                hasNotificationPermission(context)
    }

    /**
     * Launch intent for Autostart & Background Launch permission (Xiaomi MIUI / HyperOS, Vivo, Oppo, Realme, OnePlus, Huawei, Samsung, Asus, etc.)
     */
    fun openAutoStartPermission(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val pkg = context.packageName

        // 1. Xiaomi / Redmi / POCO (MIUI / HyperOS Autostart)
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
            val xiaomiIntents = listOf(
                Intent().apply {
                    component = android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent("miui.intent.action.OP_AUTO_START").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    putExtra("extra_pkgname", pkg)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            for (intent in xiaomiIntents) {
                try {
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {}
            }
        }

        // 2. Oppo / Realme / OnePlus (ColorOS / OxygenOS / Realme UI)
        if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
            val oppoIntents = listOf(
                Intent().apply {
                    component = android.content.ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.oplus.battery", "com.oplus.battery.AppListActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.coloros.oppoguardelf", "com.coloros.powermanager.fuelgaurd.PowerConsumptionOptimizationActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            for (intent in oppoIntents) {
                try {
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {}
            }
        }

        // 3. Vivo / iQOO (Funtouch OS / OriginOS)
        if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
            val vivoIntents = listOf(
                Intent().apply {
                    component = android.content.ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.PurviewTabActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            for (intent in vivoIntents) {
                try {
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {}
            }
        }

        // 4. Huawei / Honor (EMUI / MagicOS)
        if (manufacturer.contains("huawei") || manufacturer.contains("honor")) {
            val huaweiIntents = listOf(
                Intent().apply {
                    component = android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            for (intent in huaweiIntents) {
                try {
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {}
            }
        }

        // 5. Samsung (One UI Device Care / Battery)
        if (manufacturer.contains("samsung")) {
            val samsungIntents = listOf(
                Intent().apply {
                    component = android.content.ComponentName("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent().apply {
                    component = android.content.ComponentName("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            for (intent in samsungIntents) {
                try {
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {}
            }
        }

        // 6. Asus
        if (manufacturer.contains("asus")) {
            try {
                val asusIntent = Intent().apply {
                    component = android.content.ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(asusIntent)
                return
            } catch (_: Exception) {}
        }

        // 7. General Android 14+ / App details fallback
        try {
            val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$pkg")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appDetailsIntent)
        } catch (_: Exception) {
            requestIgnoreBatteryOptimization(context)
        }
    }

    /**
     * Launch intent for Lock Screen display permission (Xiaomi MIUI / HyperOS, Vivo, Oppo, Android 14+ Full Screen Intent)
     */
    fun openLockScreenPermission(context: Context) {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        // 1. Xiaomi / Redmi / POCO (MIUI / HyperOS - Show on Lock screen & Pop-up windows)
        if (manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco")) {
            val miuiIntents = listOf(
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    putExtra("extra_pkgname", context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
            for (intent in miuiIntents) {
                try {
                    context.startActivity(intent)
                    return
                } catch (e: Exception) {
                    Log.d("PermissionHelper", "MIUI intent failed, trying next", e)
                }
            }
        }

        // 2. Android 14+ (API 34) Full Screen Intent settings
        if (Build.VERSION.SDK_INT >= 34) {
            try {
                val fullScreenIntent = Intent("android.settings.MANAGE_APP_USE_FULL_SCREEN_INTENT").apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fullScreenIntent)
                return
            } catch (e: Exception) {
                Log.d("PermissionHelper", "Android 14 full screen intent failed", e)
            }
        }

        // 3. Vivo (Lock screen / Background popup)
        if (manufacturer.contains("vivo") || manufacturer.contains("iqoo")) {
            try {
                val vivoIntent = Intent().apply {
                    setClassName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.SoftPermissionDetailActivity")
                    putExtra("packagename", context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(vivoIntent)
                return
            } catch (e: Exception) {
                Log.d("PermissionHelper", "Vivo intent failed", e)
            }
        }

        // 4. Oppo / Realme / OnePlus
        if (manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus")) {
            try {
                val oppoIntent = Intent().apply {
                    setClassName("com.coloros.safecenter", "com.coloros.safecenter.permission.PermissionManagerActivity")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(oppoIntent)
                return
            } catch (e: Exception) {
                Log.d("PermissionHelper", "ColorOS intent failed", e)
            }
        }

        // 5. Fallback to App Info / Details settings
        try {
            val appDetailsIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(appDetailsIntent)
        } catch (e: Exception) {
            Log.e("PermissionHelper", "All lockscreen permission intents failed", e)
            openOverlaySettings(context)
        }
    }

    /**
     * Launch intent for Display Over Other Apps (Overlay)
     */
    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("PermissionHelper", "Failed to open overlay settings with package URI", e)
                try {
                    val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    Log.e("PermissionHelper", "Failed fallback overlay settings", e2)
                }
            }
        }
    }

    /**
     * Launch intent to ignore battery optimizations
     */
    fun requestIgnoreBatteryOptimization(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("PermissionHelper", "Failed direct request ignore battery optimizations", e)
                try {
                    val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (e2: Exception) {
                    Log.e("PermissionHelper", "Failed fallback ignore battery settings", e2)
                }
            }
        }
    }

    /**
     * Launch intent for Exact Alarm permission (Android 12+)
     */
    fun openExactAlarmSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:${context.packageName}")
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e("PermissionHelper", "Failed to open exact alarm settings", e)
            }
        }
    }

    /**
     * Launch general app notification settings
     */
    fun openAppNotificationSettings(context: Context) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } else {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("PermissionHelper", "Failed to open app notification settings", e)
        }
    }
}
