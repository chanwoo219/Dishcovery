import Foundation

// GET /{id}/edit 응답 - RecipeVo의 stepList는 중첩 객체라 여기선 안 받고 /{id}/steps로 따로 조회한다
struct RecipeEditData: Codable {
    let recipeId: String
    let categoryId: String?
    let title: String
    let rcpDisc: String?
    let cookTime: String
    let cookDfct: String
    let imgUrl: String?
    let recipeIngr: String
    let recipeTip: String
    let recipeTag: String?
}

struct RecipeUpdateRequest {
    let recipeId: String
    let title: String
    let categoryId: String
    let cookTime: String
    let cookDfct: String
    let rcpDisc: String
    let recipeIngr: String
    let recipeTip: String
    let recipeTag: String
    let stepDescriptions: [String]
    let imgUrl: String?
    let imageData: Data?
}
