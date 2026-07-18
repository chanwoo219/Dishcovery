import SwiftUI

@available(iOS 16.0, *)
struct PublicProfileView: View {
    let userId: String
    @StateObject private var viewModel = PublicProfileViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                VStack(spacing: 8) {
                    AvatarView(imgPath: viewModel.user?.userImgPath, size: 80)

                    if let user = viewModel.user {
                        Text(user.userName)
                            .font(.title3)
                            .fontWeight(.bold)
                        Text(user.userId)
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                    }
                }
                .frame(maxWidth: .infinity)

                Text("등록한 레시피")
                    .font(.headline)

                if viewModel.recipes.isEmpty {
                    Text("아직 등록한 레시피가 없어요.")
                        .foregroundColor(.secondary)
                } else {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(viewModel.recipes) { recipe in
                            Button {
                                path.append(Page.recipeDetail(recipe: recipe))
                            } label: {
                                RecipeCardView(recipe: recipe)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                }
            }
            .padding()
        }
        .task {
            await viewModel.load(userId: userId)
        }
    }
}
