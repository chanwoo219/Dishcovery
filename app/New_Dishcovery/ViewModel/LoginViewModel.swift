import Foundation

@MainActor
class LoginViewModel: ObservableObject {
    @Published var userId: String = ""
    @Published var userPswd: String = ""
    @Published var message: String = ""
    @Published var loginSuccess: Bool = false

    private var appState: AppState

    init(appState: AppState) {
        self.appState = appState
    }

    func login() async {
        guard let url = URL(string: "\(API.baseURL)/api/auth/login") else {
            message = "잘못된 URL"
            return
        }

        let loginRequest = LoginRequest(userId: userId, userPswd: userPswd)
        
        guard let bodyData = try? JSONEncoder().encode(loginRequest) else {
            message = "요청 생성 실패"
            return
        }
        
        print("보내는 JSON: \(String(data: bodyData, encoding: .utf8)!)")

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = bodyData

        do {
            let (data, response) = try await URLSession.shared.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                message = "서버 응답 오류"
                return
            }

            if httpResponse.statusCode == 200 {
                let loginResponse = try JSONDecoder().decode(LoginResponse.self, from: data)
                
                // JWT 저장
                UserDefaults.standard.set(loginResponse.token, forKey: "JWT_TOKEN")
                UserDefaults.standard.set(loginResponse.username, forKey: "USERNAME")
                
                // AppState 업데이트
                appState.isLoggedIn = true
                appState.username = loginResponse.username
                
                loginSuccess = true
                message = "로그인 성공!"
            } else if httpResponse.statusCode == 401 {
                message = "아이디 또는 비밀번호가 올바르지 않습니다."
                loginSuccess = false
            } else {
                message = "서버 오류: \(httpResponse.statusCode)"
                loginSuccess = false
            }
        } catch {
            message = "네트워크 오류: \(error.localizedDescription)"
            loginSuccess = false
        }
    }
}



struct LoginResponse: Codable {
    let token: String
    let username: String
}

// 요청 바디
struct LoginRequest: Codable {
    let userId: String
    let userPswd: String
}
