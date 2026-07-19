import SwiftUI

enum RecipeApiError: Error, LocalizedError {
    case unauthorized
    case server(String)

    var errorDescription: String? {
        switch self {
        case .unauthorized: return "로그인이 필요합니다."
        case .server(let msg): return msg
        }
    }
}

class RecipeApiService {
    static let shared = RecipeApiService()
    private init() {}

    private func authHeader() -> String? {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else { return nil }
        return "Bearer \(token)"
    }

    func fetchComments(recipeId: String) async throws -> [RecipeComment] {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/comments") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([RecipeComment].self, from: data)
    }

    func addComment(recipeId: String, content: String) async throws {
        guard let auth = authHeader() else { throw RecipeApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/comments") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(["content": content])

        let (_, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw RecipeApiError.server("댓글 등록에 실패했습니다.")
        }
    }

    func deleteComment(recipeId: String, commentId: Int) async throws {
        guard let auth = authHeader() else { throw RecipeApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/comments/\(commentId)") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let (_, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw RecipeApiError.server("댓글 삭제 권한이 없습니다.")
        }
    }

    func fetchLikeStatus(recipeId: String) async throws -> RecipeLikeStatus {
        var request = URLRequest(url: URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/like")!)
        if let auth = authHeader() {
            request.setValue(auth, forHTTPHeaderField: "Authorization")
        }
        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode(RecipeLikeStatus.self, from: data)
    }

    func toggleLike(recipeId: String) async throws -> RecipeLikeStatus {
        guard let auth = authHeader() else { throw RecipeApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/like") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw RecipeApiError.unauthorized
        }
        return try JSONDecoder().decode(RecipeLikeStatus.self, from: data)
    }

    func fetchRecipeForEdit(recipeId: String) async throws -> RecipeEditData {
        guard let auth = authHeader() else { throw RecipeApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/edit") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let (data, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw RecipeApiError.unauthorized
        }
        return try JSONDecoder().decode(RecipeEditData.self, from: data)
    }

    func fetchSteps(recipeId: String) async throws -> [RecipeStep] {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)/steps") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([RecipeStep].self, from: data)
    }

    func updateRecipe(_ recipe: RecipeUpdateRequest) async throws {
        guard let auth = authHeader() else { throw RecipeApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipe.recipeId)/update") else { throw URLError(.badURL) }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        request.httpBody = createUpdateBody(recipe: recipe, boundary: boundary)

        let (_, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw RecipeApiError.server("레시피 수정에 실패했습니다.")
        }
    }

    private func createUpdateBody(recipe: RecipeUpdateRequest, boundary: String) -> Data {
        var body = Data()
        let lineBreak = "\r\n"

        func addField(name: String, value: String) {
            body.append("--\(boundary)\(lineBreak)")
            body.append("Content-Disposition: form-data; name=\"\(name)\"\(lineBreak + lineBreak)")
            body.append(value + lineBreak)
        }

        addField(name: "title", value: recipe.title)
        addField(name: "categoryId", value: recipe.categoryId)
        addField(name: "cookTime", value: recipe.cookTime)
        addField(name: "cookDfct", value: recipe.cookDfct)
        addField(name: "rcpDisc", value: recipe.rcpDisc)
        addField(name: "recipeIngr", value: recipe.recipeIngr)
        addField(name: "recipeTip", value: recipe.recipeTip)
        addField(name: "recipeTag", value: recipe.recipeTag)
        // 같은 이름으로 여러 번 넣어야 서버 String[]로 바인딩됨 (콤마join하면 한 덩어리로 들어감)
        for step in recipe.stepDescriptions {
            addField(name: "stepDescriptions", value: step)
        }
        if let imgUrl = recipe.imgUrl {
            addField(name: "imgUrl", value: imgUrl)
        }

        if let img = recipe.imageData {
            body.append("--\(boundary)\(lineBreak)")
            body.append("Content-Disposition: form-data; name=\"mainImages\"; filename=\"recipe.jpg\"\(lineBreak)")
            body.append("Content-Type: image/jpeg\(lineBreak + lineBreak)")
            body.append(img)
            body.append(lineBreak)
        }

        body.append("--\(boundary)--\(lineBreak)")
        return body
    }

    func deleteRecipe(recipeId: String) async throws {
        guard let auth = authHeader() else { throw RecipeApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipeId)") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "DELETE"
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let (_, response) = try await URLSession.shared.data(for: request)
        guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
            throw RecipeApiError.server("본인 레시피만 삭제할 수 있습니다.")
        }
    }

    func uploadRecipe(_ recipe: RecipeUploadRequest,
                      token: String,
                      completion: @escaping (Bool, String) -> Void) {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/SaveRecipeData") else {
            completion(false, "잘못된 URL")
            return
        }

        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")

        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        request.httpBody = createBody(recipe: recipe, boundary: boundary)

        URLSession.shared.dataTask(with: request) { _, response, error in
            if let error = error {
                completion(false, "업로드 실패: \(error.localizedDescription)")
                return
            }
            completion(true, "레시피 등록 완료!")
        }.resume()
    }

    private func createBody(recipe: RecipeUploadRequest, boundary: String) -> Data {
        var body = Data()
        let lineBreak = "\r\n"

        func addField(name: String, value: String) {
            body.append("--\(boundary)\(lineBreak)")
            body.append("Content-Disposition: form-data; name=\"\(name)\"\(lineBreak + lineBreak)")
            body.append(value + lineBreak)
        }

        addField(name: "title", value: recipe.title)
        addField(name: "categoryId", value: recipe.categoryId)
        addField(name: "cookTime", value: recipe.cookTime)
        addField(name: "cookDfct", value: recipe.cookDfct)
        addField(name: "rcpDisc", value: recipe.rcpDisc)
        addField(name: "recipeIngr", value: recipe.recipeIngr)
        addField(name: "recipeTip", value: recipe.recipeTip)
        addField(name: "recipeTag", value: recipe.recipeTag)
        // 같은 이름으로 여러 번 넣어야 서버 String[]로 바인딩됨 (콤마join하면 한 덩어리로 들어감)
        for step in recipe.stepDescriptions {
            addField(name: "stepDescriptions", value: step)
        }

        if let img = recipe.imageData {
            body.append("--\(boundary)\(lineBreak)")
            body.append("Content-Disposition: form-data; name=\"mainImages\"; filename=\"recipe.jpg\"\(lineBreak)")
            body.append("Content-Type: image/jpeg\(lineBreak + lineBreak)")
            body.append(img)
            body.append(lineBreak)
        }

        body.append("--\(boundary)--\(lineBreak)")
        return body
    }
}

extension Data {
    mutating func append(_ string: String) {
        if let d = string.data(using: .utf8) { append(d) }
    }
}
