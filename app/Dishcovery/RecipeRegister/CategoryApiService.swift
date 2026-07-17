import Foundation

class CategoryApiService {

    func fetchCategories() async throws -> [CodeVo] {
        guard let url = URL(string: "\(API.baseURL)/api/categoryList") else {
            throw URLError(.badURL)
        }

        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([CodeVo].self, from: data)  // 배열 decode
    }

}

