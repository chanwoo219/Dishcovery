import Foundation

@MainActor
class WithdrawViewModel: ObservableObject {
    @Published var password: String = ""
    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var didSucceed = false

    func withdraw() {
        Task {
            do {
                let message = try await UserApiService.shared.withdraw(password: password)
                toastMessage = message
                showToast = true
                didSucceed = true
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
