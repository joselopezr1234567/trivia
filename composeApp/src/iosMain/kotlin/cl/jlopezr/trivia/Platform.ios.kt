package cl.jlopezr.trivia

import platform.UIKit.UIDevice
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val language: String = NSLocale.currentLocale.languageCode
}

actual fun getPlatform(): Platform = IOSPlatform()