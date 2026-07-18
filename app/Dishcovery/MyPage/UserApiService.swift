import Foundation

struct NicknameChangeResponse: Codable {
    let message: String
    let token: String
    let userName: String
}

struct ProfileImageUploadResponse: Codable {
    let message: String
    let imgUrl: String
}

class UserApiService {
    static let shared = UserApiService()
    private init() {}

    private func authHeader() -> String? {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else { return nil }
        return "Bearer \(token)"
    }

    func fetchMyProfile() async throws -> UserProfile {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/users/me") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let (data, response) = try await URLSession.shared.data(for: request)
        if let http = response as? HTTPURLResponse, http.statusCode == 401 {
            throw ShopApiError.unauthorized
        }
        return try JSONDecoder().decode(UserProfile.self, from: data)
    }

    func fetchRecommendedUsers() async throws -> [PublicUser] {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/users/recommended") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let (data, _) = try await URLSession.shared.data(for: request)
        return try JSONDecoder().decode([PublicUser].self, from: data)
    }

    func fetchPublicProfile(userId: String) async throws -> PublicUser {
        guard let url = URL(string: "\(API.baseURL)/api/users/\(userId)") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode(PublicUser.self, from: data)
    }

    func fetchRecipesByUser(userId: String) async throws -> [Recipe] {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/byUser/\(userId)") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([Recipe].self, from: data)
    }

    func changeNickname(userName: String) async throws -> NicknameChangeResponse {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/users/nickname") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(["userName": userName])

        let (data, response) = try await URLSession.shared.data(for: request)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            let decoded = try? JSONDecoder().decode(MessageResponse.self, from: data)
            throw ShopApiError.server(decoded?.message ?? "닉네임 변경에 실패했습니다.")
        }
        return try JSONDecoder().decode(NicknameChangeResponse.self, from: data)
    }

    func uploadProfileImage(imageData: Data) async throws -> String {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/users/profile-image") else { throw URLError(.badURL) }

        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")

        let boundary = UUID().uuidString
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")

        var body = Data()
        let lineBreak = "\r\n"
        body.append("--\(boundary)\(lineBreak)")
        body.append("Content-Disposition: form-data; name=\"image\"; filename=\"profile.jpg\"\(lineBreak)")
        body.append("Content-Type: image/jpeg\(lineBreak + lineBreak)")
        body.append(imageData)
        body.append(lineBreak)
        body.append("--\(boundary)--\(lineBreak)")
        request.httpBody = body

        let (data, response) = try await URLSession.shared.data(for: request)
        let decoded = try JSONDecoder().decode(ProfileImageUploadResponse.self, from: data)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw ShopApiError.server(decoded.message)
        }
        return decoded.imgUrl
    }

    func withdraw(password: String) async throws -> String {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/users/withdraw") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(["password": password])

        let (data, response) = try await URLSession.shared.data(for: request)
        let decoded = try JSONDecoder().decode(MessageResponse.self, from: data)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw ShopApiError.server(decoded.message)
        }
        return decoded.message
    }
}
