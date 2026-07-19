import Foundation

@MainActor
class ForgotPasswordViewModel: ObservableObject {
    @Published var userMail: String = ""
    @Published var code: String = ""
    @Published var showCodeField = false
    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var didVerify = false

    func sendCode() {
        Task {
            guard let url = URL(string: "\(API.baseURL)/api/auth/reset/request") else { return }
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try? JSONEncoder().encode(["userMail": userMail])

            do {
                let (data, _) = try await URLSession.shared.data(for: request)
                let decoded = try? JSONDecoder().decode(MessageResponse.self, from: data)
                toastMessage = decoded?.message ?? "인증코드를 이메일로 보냈습니다."
                showToast = true
                showCodeField = true
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }

    func verifyCode() {
        Task {
            guard let url = URL(string: "\(API.baseURL)/api/auth/reset/verify-code") else { return }
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try? JSONEncoder().encode(["userMail": userMail, "code": code])

            do {
                let (data, response) = try await URLSession.shared.data(for: request)
                let decoded = try? JSONDecoder().decode(MessageResponse.self, from: data)
                if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                    didVerify = true
                } else {
                    toastMessage = decoded?.message ?? "인증코드가 올바르지 않거나 만료되었습니다."
                    showToast = true
                }
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
