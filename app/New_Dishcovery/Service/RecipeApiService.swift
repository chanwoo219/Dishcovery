import SwiftUI

class RecipeApiService {
    static let shared = RecipeApiService()
    private init() {}

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
        addField(name: "stepDescriptions", value: recipe.stepDescriptions.joined(separator: ","))

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
