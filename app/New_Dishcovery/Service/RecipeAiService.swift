import Foundation

class RecipeAiService {
    static let shared = RecipeAiService()

    func fetchRecipe(ingredients: String) async throws -> String {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/recommend") else {
            throw URLError(.badURL)
        }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body = ["ingredients": ingredients]
        request.httpBody = try JSONEncoder().encode(body)

        let (data, _) = try await URLSession.shared.data(for: request)

        let decoded = try JSONDecoder().decode(RecipeResponse.self, from: data)
        return decoded.recipe
    }
}
