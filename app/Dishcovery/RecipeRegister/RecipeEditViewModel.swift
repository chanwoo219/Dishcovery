import SwiftUI
import UIKit

@MainActor
class RecipeEditViewModel: ObservableObject {
    let recipeId: String

    @Published var title: String = ""
    @Published var categoryId: String = ""
    @Published var cookTime: String = ""
    @Published var cookDfct: String = ""
    @Published var rcpDisc: String = ""
    @Published var recipeIngr: String = ""
    @Published var recipeTip: String = ""
    @Published var recipeTag: String = ""
    @Published var stepDescriptions: [String] = [""]
    @Published var imgUrl: String?

    @Published var selectedImageData: Data?
    @Published var isLoading = true

    @Published var toastMessage = ""
    @Published var showToast = false
    @Published var shouldDismiss = false

    init(recipeId: String) {
        self.recipeId = recipeId
    }

    func load() async {
        do {
            let recipe = try await RecipeApiService.shared.fetchRecipeForEdit(recipeId: recipeId)
            title = recipe.title
            categoryId = recipe.categoryId ?? ""
            cookTime = recipe.cookTime
            cookDfct = recipe.cookDfct
            rcpDisc = recipe.rcpDisc ?? ""
            recipeIngr = recipe.recipeIngr
            recipeTip = recipe.recipeTip
            recipeTag = recipe.recipeTag ?? ""
            imgUrl = recipe.imgUrl
        } catch {
            toastMessage = "레시피를 불러오지 못했습니다."
            showToast = true
        }

        do {
            let steps = try await RecipeApiService.shared.fetchSteps(recipeId: recipeId)
            let sorted = steps.sorted { ($0.stepOrder ?? 0) < ($1.stepOrder ?? 0) }
            let descriptions = sorted.compactMap { $0.stepDescription }
            stepDescriptions = descriptions.isEmpty ? [""] : descriptions
        } catch {
            stepDescriptions = [""]
        }

        isLoading = false
    }

    func submit() {
        Task {
            let request = RecipeUpdateRequest(
                recipeId: recipeId,
                title: title,
                categoryId: categoryId,
                cookTime: cookTime,
                cookDfct: cookDfct,
                rcpDisc: rcpDisc,
                recipeIngr: recipeIngr,
                recipeTip: recipeTip,
                recipeTag: recipeTag,
                stepDescriptions: stepDescriptions.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty },
                imgUrl: imgUrl,
                imageData: selectedImageData
            )

            do {
                try await RecipeApiService.shared.updateRecipe(request)
                toastMessage = "레시피가 수정되었습니다."
                showToast = true
                try? await Task.sleep(nanoseconds: 1_200_000_000)
                shouldDismiss = true
            } catch {
                toastMessage = error.localizedDescription
                showToast = true
            }
        }
    }
}
