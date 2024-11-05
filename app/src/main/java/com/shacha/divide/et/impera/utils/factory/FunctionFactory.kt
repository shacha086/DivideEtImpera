@file:Suppress("unused")

package com.shacha.divide.et.impera.utils.factory

import android.content.Context
import android.content.res.Configuration
import com.highcapable.yukihookapi.hook.type.android.TypedArrayClass
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.reflect.KClass
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

/**
 * System dark mode is enabled or not
 *
 * 系统深色模式是否开启
 * @return [Boolean] Whether to enable / 是否开启
 */
val Context.isSystemInDarkMode get() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

/**
 * System dark mode is disable or not
 *
 * 系统深色模式是否没开启
 * @return [Boolean] Whether to enable / 是否开启
 */
inline val Context.isNotSystemInDarkMode get() = isSystemInDarkMode.not()

/**
 * dp to pxInt
 *
 * dp 转换为 pxInt
 * @param context using instance / 使用的实例
 * @return [Int]
 */
fun Number.dp(context: Context) = dpFloat(context).toInt()

/**
 * dp to pxFloat
 *
 * dp 转换为 pxFloat
 * @param context using instance / 使用的实例
 * @return [Float]
 */
fun Number.dpFloat(context: Context) = toFloat() * context.resources.displayMetrics.density

@Suppress("UNCHECKED_CAST")
fun <T> Any.castUnsafe() = this as T

fun Class<*>.getMethodRecursive(name: String, vararg parameterTypes: Class<*>): Method? {
    // search superclasses
    var c: Class<*>? = this
    while (c != null) {
        val result: Method? = try {
            c.getDeclaredMethod(name, *parameterTypes).apply { isAccessible = true }
                .castUnsafe()
        } catch (e: NoSuchMethodException) {
            null
        }
        if (result != null) {
            return result
        }
        c = c.superclass
    }
    return findInterfaceMethod(name, *parameterTypes)
}

fun Class<*>.findInterfaceMethod(name: String, vararg parameterTypes: Class<*>): Method? {
    val iftable: Array<Any>? = Class::class.java.getDeclaredField("ifTable").apply { isAccessible = true }.get(this)?.castUnsafe()
    if (iftable != null) {
        // Search backwards so more specific interfaces are searched first. This ensures that
        // the method we return is not overridden by one of it's subtypes that this class also
        // implements.
        var i = iftable.size - 2
        while (i >= 0) {
            val ifc = iftable[i] as Class<*>

            val result: Method? = Class::class.java.getDeclaredMethod("getPublicMethodRecursive", String::class.java, parameterTypes.javaClass).apply { isAccessible = true }(ifc, name, parameterTypes)?.castUnsafe()  // ifc.getPublicMethodRecursive(name, parameterTypes)
            if (result != null) {
                return result
            }
            i -= 2
        }
    }

    return null
}