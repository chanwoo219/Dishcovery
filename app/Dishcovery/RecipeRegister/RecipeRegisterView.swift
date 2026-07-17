import SwiftUI


struct RecipeRegisterView: View {

    @StateObject private var viewModel = RecipeRegisterViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        ZStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 28) {
                    BasicInfoView(title: $viewModel.title,
                                  categoryId: $viewModel.categoryId,
                                  cookTime: $viewModel.cookTime,
                                  cookDfct: $viewModel.cookDfct)
//                    ImagePickerView(selectedImage: $viewModel.selectedImage)
//                        .onChange(of: viewModel.selectedImage) { _ in
//                                viewModel.updateImageData()
//                            }
                    ImagePickerView(selectedImageData: Binding<Data?>(
                        get: { viewModel.selectedImage?.jpegData(compressionQuality: 0.8) },
                        set: { data in
                            if let data = data {
                                viewModel.selectedImage = UIImage(data: data)
                            } else {
                                viewModel.selectedImage = nil
                            }
                        }
                    ))
                    DescriptionAndIngredientsView(rcpDisc: $viewModel.rcpDisc,
                                                  recipeIngr: $viewModel.recipeIngr)

                    StepsView(stepDescriptions: $viewModel.stepDescriptions)
                    TipAndTagView(recipeTip: $viewModel.recipeTip, recipeTag: $viewModel.recipeTag)

                    Button(action: viewModel.submitRecipe) {
                        Text("레시피 등록")
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
        .toast(message: viewModel.toastMessage, isShowing: $viewModel.showToast) //전체 ZStack에 적용
        .onChange(of: viewModel.shouldNavigateToMain) { navigate in
            if navigate {
                path.removeLast(path.count)
            }
        }
    }
}
