import SwiftUI

struct RecipeAiView: View {

    @StateObject var vm = RecipeAiViewModel()

    var body: some View {
        VStack(alignment: .leading, spacing: 20) {

            Text("AI 레시피 추천")
                .font(.largeTitle)
                .bold()

            TextField("재료 입력 (예: 닭가슴살, 양파)", text: $vm.ingredients)
                .textFieldStyle(.roundedBorder)
                .padding(.top)
                .keyboardType(.default)
            
            Button(action: { vm.loadRecipe() }) {
                Text("레시피 받아오기")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color("main"))
                    .foregroundColor(.primary)
                    .cornerRadius(12)
            }
            .disabled(vm.ingredients.isEmpty)

            if vm.isLoading {
                ProgressView("AI가 레시피 생성 중…")
                    .multilineTextAlignment(.center)
            }

            ScrollView {
                Text(vm.result)
                    .padding()
            }

            Spacer()
        }
        .padding()
    }
}
