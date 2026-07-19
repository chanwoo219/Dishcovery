import Foundation

@MainActor
class ResetPasswordViewModel: ObservableObject {
    @Published var newPassword: String = ""
    @Published var confirmPassword: String = ""
    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var didSucceed = false

    func reset(userMail: String, code: String) {
        Task {
            guard let url = URL(string: "\(API.baseURL)/api/auth/reset/verify") else { return }
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = try? JSONEncoder().encode([
                "userMail": userMail,
                "code": code,
                "newPassword": newPassword,
                "confirmPassword": confirmPassword
            ])

            do {
                let (data, response) = try await URLSession.shared.data(for: request)
                let decoded = try? JSONDecoder().decode(MessageResponse.self, from: data)
                toastMessage = decoded?.message ?? "비밀번호가 변경되었습니다."
                showToast = true
                if let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) {
                    didSucceed = true
                }
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
