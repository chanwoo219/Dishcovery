import Foundation
import UIKit

@MainActor
class MyPageViewModel: ObservableObject {
    @Published var profile: UserProfile?
    @Published var myRecipes: [Recipe] = []
    @Published var recommendedUsers: [PublicUser] = []
    @Published var isUploadingImage = false

    func load() async {
        async let profileTask: () = loadProfile()
        async let recipesTask: () = loadMyRecipes()
        async let usersTask: () = loadRecommendedUsers()
        _ = await (profileTask, recipesTask, usersTask)
    }

    func loadProfile() async {
        do {
            profile = try await UserApiService.shared.fetchMyProfile()
        } catch {
            print("🔴 [MyPage] 프로필 불러오기 실패:", error.localizedDescription)
        }
    }

    func loadMyRecipes() async {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN"),
              let url = URL(string: "\(API.baseURL)/api/recipes/myRecipe") else { return }
        var request = URLRequest(url: url)
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        do {
            let (data, _) = try await URLSession.shared.data(for: request)
            myRecipes = try JSONDecoder().decode([Recipe].self, from: data)
        } catch {
            print("🔴 [MyPage] 내 레시피 불러오기 실패:", error.localizedDescription)
        }
    }

    func loadRecommendedUsers() async {
        do {
            recommendedUsers = try await UserApiService.shared.fetchRecommendedUsers()
        } catch {
            print("🔴 [MyPage] 추천 유저 불러오기 실패:", error.localizedDescription)
        }
    }

    func uploadProfileImage(_ image: UIImage) {
        guard let data = image.jpegData(compressionQuality: 0.8) else { return }
        Task {
            isUploadingImage = true
            do {
                _ = try await UserApiService.shared.uploadProfileImage(imageData: data)
                await loadProfile()
            } catch {
                print("🔴 [MyPage] 프로필 사진 업로드 실패:", error.localizedDescription)
            }
            isUploadingImage = false
        }
    }
}
