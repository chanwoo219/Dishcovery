import Foundation

struct MessageResponse: Codable {
    let message: String
}

enum ShopApiError: Error, LocalizedError {
    case unauthorized
    case server(String)

    var errorDescription: String? {
        switch self {
        case .unauthorized: return "로그인이 필요합니다."
        case .server(let msg): return msg
        }
    }
}

class ShopApiService {
    static let shared = ShopApiService()
    private init() {}

    private func authHeader() -> String? {
        guard let token = UserDefaults.standard.string(forKey: "JWT_TOKEN") else { return nil }
        return "Bearer \(token)"
    }

    func fetchProducts(searchName: String? = nil) async throws -> [ShopProduct] {
        var urlString = "\(API.baseURL)/api/shop/products"
        if let searchName = searchName, !searchName.isEmpty {
            let encoded = searchName.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
            urlString += "?searchName=\(encoded)"
        }
        guard let url = URL(string: urlString) else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([ShopProduct].self, from: data)
    }

    func fetchProduct(productId: String) async throws -> ShopProduct {
        guard let url = URL(string: "\(API.baseURL)/api/shop/products/\(productId)") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode(ShopProduct.self, from: data)
    }

    func fetchRecommended(productId: String) async throws -> [ShopProduct] {
        guard let url = URL(string: "\(API.baseURL)/api/shop/products/\(productId)/recommended") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([ShopProduct].self, from: data)
    }

    func fetchMyPoint() async throws -> Int {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/shop/point") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        let (data, response) = try await URLSession.shared.data(for: request)
        if let http = response as? HTTPURLResponse, http.statusCode == 401 {
            throw ShopApiError.unauthorized
        }
        let decoded = try JSONDecoder().decode([String: Int].self, from: data)
        return decoded["point"] ?? 0
    }

    func purchase(productId: String, qty: Int) async throws -> String {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/shop/purchase") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(["productId": productId, "qty": String(qty)])

        let (data, response) = try await URLSession.shared.data(for: request)
        let decoded = try JSONDecoder().decode(MessageResponse.self, from: data)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw ShopApiError.server(decoded.message)
        }
        return decoded.message
    }

    func fetchReviews(productId: String) async throws -> [ShopReview] {
        guard let url = URL(string: "\(API.baseURL)/api/shop/products/\(productId)/reviews") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([ShopReview].self, from: data)
    }

    func addReview(productId: String, rating: Int, content: String) async throws -> String {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/shop/products/\(productId)/reviews") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(["rating": String(rating), "content": content])

        let (data, response) = try await URLSession.shared.data(for: request)
        let decoded = try JSONDecoder().decode(MessageResponse.self, from: data)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw ShopApiError.server(decoded.message)
        }
        return decoded.message
    }

    func fetchInquiries(productId: String) async throws -> [ShopInquiry] {
        guard let url = URL(string: "\(API.baseURL)/api/shop/products/\(productId)/inquiries") else { throw URLError(.badURL) }
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode([ShopInquiry].self, from: data)
    }

    func addInquiry(productId: String, content: String) async throws -> String {
        guard let auth = authHeader() else { throw ShopApiError.unauthorized }
        guard let url = URL(string: "\(API.baseURL)/api/shop/products/\(productId)/inquiries") else { throw URLError(.badURL) }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(auth, forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONEncoder().encode(["content": content])

        let (data, response) = try await URLSession.shared.data(for: request)
        let decoded = try JSONDecoder().decode(MessageResponse.self, from: data)
        if let http = response as? HTTPURLResponse, !(200...299).contains(http.statusCode) {
            throw ShopApiError.server(decoded.message)
        }
        return decoded.message
    }
}
