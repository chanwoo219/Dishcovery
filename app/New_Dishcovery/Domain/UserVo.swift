import Foundation

struct UserVo: Identifiable, Codable {
    var id: String { userId }  // SwiftUI List 등에서 필요

    let userId: String
    let userName: String
    let userPswd: String
    let userMail: String

}
