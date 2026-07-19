import SwiftUI

@available(iOS 16.0, *)
struct RecipeEditView: View {
    @StateObject private var viewModel: RecipeEditViewModel
    @Binding var path: NavigationPath

    init(recipeId: String, path: Binding<NavigationPath>) {
        _viewModel = StateObject(wrappedValue: RecipeEditViewModel(recipeId: recipeId))
        _path = path
    }

    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView()
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 28) {
                        BasicInfoView(title: $viewModel.title,
                                      categoryId: $viewModel.categoryId,
                                      cookTime: $viewModel.cookTime,
                                      cookDfct: $viewModel.cookDfct)

                        ImagePickerView(selectedImageData: $viewModel.selectedImageData)

                        DescriptionAndIngredientsView(rcpDisc: $viewModel.rcpDisc,
                                                      recipeIngr: $viewModel.recipeIngr)

                        StepsView(stepDescriptions: $viewModel.stepDescriptions)
                        TipAndTagView(recipeTip: $viewModel.recipeTip, recipeTag: $viewModel.recipeTag)

                        Button(action: viewModel.submit) {
                            Text("저장")
                                .frame(maxWidth: .infinity)
                                .padding()
                                .background(Color.orange)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                        }
                        .padding(.vertical)
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("레시피 수정")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await viewModel.load()
        }
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast)
        .onChange(of: viewModel.shouldDismiss) { dismiss in
            if dismiss {
                path.removeLast()
            }
        }
    }
}
