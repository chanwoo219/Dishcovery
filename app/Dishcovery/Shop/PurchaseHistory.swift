import Foundation

struct PurchaseHistory: Identifiable, Codable {
    var id: Int { purchaseId }
    let purchaseId: Int
    let productId: String
    let productName: String
    let mainImage: String?
    let productPoint: Int
    let qty: Int
    let purchaseDate: String?
}
