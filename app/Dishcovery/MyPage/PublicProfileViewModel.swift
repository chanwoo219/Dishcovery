import Foundation

@MainActor
class PublicProfileViewModel: ObservableObject {
    @Published var user: PublicUser?
    @Published var recipes: [Recipe] = []

    func load(userId: String) async {
        do {
            user = try await UserApiService.shared.fetchPublicProfile(userId: userId)
        } catch {
            print("🔴 [PublicProfile] 유저 불러오기 실패:", error.localizedDescription)
        }
        do {
            recipes = try await UserApiService.shared.fetchRecipesByUser(userId: userId)
        } catch {
            recipes = []
        }
    }
}
