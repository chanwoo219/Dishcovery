import Foundation

@MainActor
class RecipeViewModel: ObservableObject {
    @Published var recipes: [Recipe] = []
    @Published var isLoading = false

    func fetchRecipes() async {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/getAppRecipes") else { return }

        do {
            let (data, response) = try await URLSession.shared.data(from: url)

            if let httpResponse = response as? HTTPURLResponse {
                print(" HTTP Status Code:", httpResponse.statusCode)
            }
            
            // 서버 응답 로그 확인
            if let jsonString = String(data: data, encoding: .utf8) {
                print(" 서버 응답 데이터:\n\(jsonString)")
            }

            let decoder = JSONDecoder()
            let recipes = try decoder.decode([Recipe].self, from: data)
            print(" 디코딩 성공! 총 \(recipes.count)개 레시피")

            // 메인 쓰레드에서 상태 업데이트
            DispatchQueue.main.async {
                self.recipes = recipes
            }

        } catch {
            print(" Fetch Error:", error.localizedDescription)
        }
    }

}
