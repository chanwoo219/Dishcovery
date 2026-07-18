import Foundation

@MainActor
class ChangeNicknameViewModel: ObservableObject {
    @Published var userName: String = UserDefaults.standard.string(forKey: "USERNAME") ?? ""
    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var didSucceed = false

    func save() {
        Task {
            do {
                let result = try await UserApiService.shared.changeNickname(userName: userName)
                UserDefaults.standard.set(result.token, forKey: "JWT_TOKEN")
                UserDefaults.standard.set(result.userName, forKey: "USERNAME")
                userName = result.userName
                toastMessage = result.message
                showToast = true
                didSucceed = true
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
