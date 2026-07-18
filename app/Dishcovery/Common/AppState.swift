import SwiftUI

class AppState: ObservableObject {
    @Published var isLoggedIn: Bool = false
    @Published var username: String = ""
    @Published var userImgPath: String? = nil
    
    init() {
        // 앱 실행 시 저장된 JWT 여부 확인
        if let token = UserDefaults.standard.string(forKey: "JWT_TOKEN"),
           let name = UserDefaults.standard.string(forKey: "USERNAME"),
           !token.isEmpty {
            self.isLoggedIn = true
            self.username = name
        }
    }
}
