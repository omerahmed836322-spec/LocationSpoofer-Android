package com.locationspoofer.app

import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName == "android" || lpparam.packageName == "com.locationspoofer.app") return

        XposedHelpers.findAndHookMethod(
            LocationManager::class.java.name, lpparam.classLoader,
            "getLastKnownLocation", String::class.java,
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (LocationEngine.isSpoofingEnabled) {
                        param.result = LocationEngine.generateSpoofedLocation()
                    }
                }
            }
        )
    }
}
