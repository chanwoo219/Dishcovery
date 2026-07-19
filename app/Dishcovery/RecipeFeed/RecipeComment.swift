import Foundation

struct RecipeComment: Identifiable, Codable {
    var id: Int { commentId }
    let commentId: Int
    let recipeId: String
    let userId: String
    let userName: String?
    let content: String
    let createdAt: String?
}

struct RecipeLikeStatus: Codable {
    let likeCount: Int
    let liked: Bool
}
