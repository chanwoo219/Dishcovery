import Foundation

struct Recipe: Identifiable, Codable, Hashable {
    var id: String { recipeId }

    let recipeId: String
    let userId: String

    let categoryId: String?      // ✅ null 가능
    let title: String
    let rcpDisc: String?

    let cookTime: String         // 서버가 "30" 형태 String
    let cookDfct: String         // 서버가 "2" 형태 String

    let rgtDate: String?
    let updDate: String?

    let imgUrl: String?          // ✅ null 가능
    let recipeIngr: String
    let recipeTip: String
    let recipeTag: String?

    let stepDescription: String? // ✅ 서버 필드명 그대로 받기 (단일)
    let stepOrder: Int?          // ✅ 서버에 존재(0)
    let stepList: [String]?      // 서버는 null이라 일단 optional
}
