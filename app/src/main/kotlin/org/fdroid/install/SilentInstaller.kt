package org.fdroid.install

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

/**
 * 静默安装管理器，支持 Root、Shizuku、系统应用三种静默安装方式。
 * 无权限时回退到系统 PackageInstaller。
 */
object SilentInstaller {

  enum class InstallMethod {
    ROOT, SHIZUKU, SYSTEM_APP, PACKAGE_INSTALLER, NONE
  }

  data class InstallCapability(
    val rootAvailable: Boolean = false,
    val shizukuAvailable: Boolean = false,
    val systemAppAvailable: Boolean = false,
  ) {
    val bestMethod: InstallMethod
      get() = when {
        rootAvailable -> InstallMethod.ROOT
        shizukuAvailable -> InstallMethod.SHIZUKU
        systemAppAvailable -> InstallMethod.SYSTEM_APP
        else -> InstallMethod.PACKAGE_INSTALLER
      }

    val availableMethods: List<InstallMethod>
      get() = buildList {
        if (rootAvailable) add(InstallMethod.ROOT)
        if (shizukuAvailable) add(InstallMethod.SHIZUKU)
        if (systemAppAvailable) add(InstallMethod.SYSTEM_APP)
        add(InstallMethod.PACKAGE_INSTALLER)
      }
  }

  fun detectCapability(context: Context): InstallCapability {
    val root = checkRoot()
    val shizuku = checkShizuku(context)
    val systemApp = checkSystemApp(context)
    return InstallCapability(root, shizuku, systemApp)
  }

  private fun checkRoot(): Boolean {
    return try {
      val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
      val output = process.inputStream.bufferedReader().readText()
      process.waitFor()
      output.contains("uid=0")
    } catch (e: Exception) {
      false
    }
  }

  private fun checkShizuku(context: Context): Boolean {
    return try {
      val shizukuService = Class.forName("moe.shizuku.api.Shizuku")
      val getServiceMethod = shizukuService.getMethod("getService")
      val service = getServiceMethod.invoke(null)
      service != null
    } catch (e: Exception) {
      false
    }
  }

  private fun checkSystemApp(context: Context): Boolean {
    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        context.packageManager.checkPermission(
          "android.permission.INSTALL_PACKAGES",
          context.packageName
        ) == PackageManager.PERMISSION_GRANTED
      } else {
        @Suppress("DEPRECATION")
        context.packageManager.checkPermission(
          "android.permission.INSTALL_PACKAGES",
          context.packageName
        ) == PackageManager.PERMISSION_GRANTED
      }
    } catch (e: Exception) {
      false
    }
  }

  /**
   * 尝试静默安装 APK。
   * @return true 表示静默安装成功，false 表示需要回退到系统安装器
   */
  fun installSilently(
    context: Context,
    apkFile: File,
    capability: InstallCapability = detectCapability(context),
  ): Boolean {
    return when (capability.bestMethod) {
      InstallMethod.ROOT -> installViaRoot(apkFile)
      InstallMethod.SHIZUKU -> installViaShizuku(context, apkFile)
      InstallMethod.SYSTEM_APP -> installViaSystemApp(context, apkFile)
      else -> false
    }
  }

  private fun installViaRoot(apkFile: File): Boolean {
    return try {
      val path = apkFile.absolutePath
      val process = Runtime.getRuntime().exec(
        arrayOf("su", "-c", "pm install -r -d \"$path\"")
      )
      val output = process.inputStream.bufferedReader().readText()
      val error = process.errorStream.bufferedReader().readText()
      process.waitFor()
      output.contains("Success") || error.contains("Success")
    } catch (e: Exception) {
      false
    }
  }

  private fun installViaShizuku(context: Context, apkFile: File): Boolean {
    return try {
      // 通过 Shizuku API 执行 pm install
      val shizukuClass = Class.forName("moe.shizuku.api.Shizuku")
      val newProcessMethod = shizukuClass.getMethod(
        "newProcess",
        Array<String>::class.java
      )
      val process = newProcessMethod.invoke(
        null,
        arrayOf("pm", "install", "-r", "-d", apkFile.absolutePath)
      )
      val waitForMethod = process.javaClass.getMethod("waitFor")
      val exitCode = waitForMethod.invoke(process) as Int
      exitCode == 0
    } catch (e: Exception) {
      false
    }
  }

  private fun installViaSystemApp(context: Context, apkFile: File): Boolean {
    // 系统应用可以通过 PackageInstaller Session 并设置 setRequireUserAction(false) 实现静默安装
    // 实际安装由 SessionInstallManager 处理，这里仅标记可用
    return false
  }

  fun getMethodName(method: InstallMethod): String {
    return when (method) {
      InstallMethod.ROOT -> "Root 静默安装"
      InstallMethod.SHIZUKU -> "Shizuku 静默安装"
      InstallMethod.SYSTEM_APP -> "系统应用静默安装"
      InstallMethod.PACKAGE_INSTALLER -> "系统安装器（需用户确认）"
      InstallMethod.NONE -> "不可用"
    }
  }
}
