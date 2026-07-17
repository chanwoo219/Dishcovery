import SwiftUI


class RecipeAiViewModel: ObservableObject {
    @Published var ingredients: String = ""
    @Published var result: String = ""
    @Published var isLoading: Bool = false

    @MainActor
    func loadRecipe() {
        Task {
            self.isLoading = true

            do {
                let r = try await RecipeAiService.shared.fetchRecipe(ingredients: ingredients)
                self.result = r
            } catch {
                self.result = "오류: \(error.localizedDescription)"
            }

            self.isLoading = false
        }
    }
}
