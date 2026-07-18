import Foundation

@MainActor
class ForgotPasswordViewModel: ObservableObject {
    @Published var userMail: String = ""
    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var didSucceed = false

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
                didSucceed = true
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
