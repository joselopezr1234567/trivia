import SwiftUI
import ComposeApp // Esto permite traer el MainViewController de Kotlin

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            // ✅ Cambiamos ContentView() por ComposeView()
            ComposeView()
                .ignoresSafeArea(.all) // Recomendado para que Compose use toda la pantalla
        }
    }
}
