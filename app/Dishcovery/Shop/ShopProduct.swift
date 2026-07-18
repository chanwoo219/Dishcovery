import Foundation

struct ShopProduct: Identifiable, Codable {
    var id: String { productId }
    let productId: String
    let productName: String
    let productPoint: Int
    let mainImage: String?
}
