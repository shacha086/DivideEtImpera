package com.shacha.divide.et.impera.hook

import android.app.Activity
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.onLoadClass
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.shacha.divide.et.impera.utils.factory.castUnsafe
import com.shacha.divide.et.impera.utils.factory.getMethodRecursive
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.util.Stack
import java.util.Vector

@InjectYukiHookWithXposed
object HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "Divide et impera"
            isEnable = true
        }
        isDebug = false
    }

    override fun onHook() = encase {
        // Your code here.
        YLog.info("Divide et impera!")
        loadApp(isExcludeSelf = true) {
            YLog.info("Start hooking ${packageName}...")
            appClassLoader!!.onLoadClass {
                val onWindowFocusChanged = try {
                    it.getMethod("onWindowFocusChanged", Boolean::class.javaPrimitiveType)
                } catch (e: NoSuchMethodException) {
                    null
                } ?: return@onLoadClass

                onWindowFocusChanged.hook {
                    replaceUnit { }
                }
                YLog.info("hooked ${it.simpleName}.onWindowFocusChanged")
                it.getMethodRecursive("onPause")?.hook {
                    after {
                        XposedBridge.invokeOriginalMethod(
                            onWindowFocusChanged,
                            instance,
                            arrayOf(false)
                        )
                    }
                }
                it.getMethodRecursive("onResume")?.hook {
                    after {
                        XposedBridge.invokeOriginalMethod(
                            onWindowFocusChanged,
                            instance,
                            arrayOf(true)
                        )
                    }
                }
                return@onLoadClass
            }

        }
    }
}