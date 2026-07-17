import Foundation

struct RecipeUploadRequest {
    let title: String
    let categoryId: String
    let cookTime: String
    let cookDfct: String
    let rcpDisc: String
    let recipeIngr: String
    let recipeTip: String
    let recipeTag: String
    let stepDescriptions: [String]
    let imageData: Data?
}
