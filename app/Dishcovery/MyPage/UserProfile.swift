import Foundation

struct UserProfile: Codable {
    let userId: String
    let userName: String
    let userMail: String
    let userImgPath: String?
    let pointBalance: Int
}
