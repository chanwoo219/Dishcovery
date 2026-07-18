import Foundation

struct PublicUser: Codable, Identifiable {
    var id: String { userId }
    let userId: String
    let userName: String
    let userImgPath: String?
}
