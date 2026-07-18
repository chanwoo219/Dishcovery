import Foundation

struct ShopReview: Identifiable, Codable {
    var id: Int { reviewId }
    let reviewId: Int
    let productId: String
    let userId: String
    let rating: Int
    let content: String
    let createdAt: String?
}
