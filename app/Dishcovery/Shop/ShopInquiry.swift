import Foundation

struct ShopInquiry: Identifiable, Codable {
    var id: Int { inquiryId }
    let inquiryId: Int
    let productId: String
    let userId: String
    let content: String
    let createdAt: String?
}
