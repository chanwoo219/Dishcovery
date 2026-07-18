import SwiftUI

@available(iOS 16.0, *)
struct MyRecipeView: View {
    @StateObject private var viewModel = MyRecipeViewModel()
    @Binding var path: NavigationPath   // ✅ 부모에서 받음

    var body: some View {
        ScrollView {
            LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {

                ForEach(viewModel.myRecipes) { recipe in
                    Button {
                        // ✅ 기존 NavigationStack의 path 사용
                        path.append(Page.recipeDetail(recipe: recipe))
                    } label: {
                        RecipeCardView(recipe: recipe)
                    }
                    .buttonStyle(PlainButtonStyle())
                }
            }
            .padding()
        }
        .task {
            await viewModel.fetchMyRecipes()
        }
    }
}
