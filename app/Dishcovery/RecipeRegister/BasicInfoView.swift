import SwiftUI

struct BasicInfoView: View {
    @Binding var title: String
    @Binding var categoryId: String
    @Binding var cookTime: String
    @Binding var cookDfct: String

    @StateObject var viewModel = RecipeRegisterViewModel()
    
    var body: some View {
        Group {
            Text("기본 정보")
                .font(.title2)
                .bold()
            
            HStack {
                Text("레시피 이름")
                    .font(.headline)
                TextField("레시피 이름", text: $title)
                    .textFieldStyle(.roundedBorder)
            }
            
            HStack {
                Text("카테고리")
                    .font(.headline)
                
                Picker("카테고리 선택", selection: $viewModel.categoryId) {
                    Text("선택").tag("")
                    ForEach(viewModel.categories, id: \.code) { cat in
                        Text(cat.codeName).tag(cat.code)
                    }
                }
                .pickerStyle(.menu)
            }

            HStack {
                Text("조리시간")
                    .font(.headline)
                TextField("조리시간 (분)", text: $cookTime)
                    .textFieldStyle(.roundedBorder)
            }
            
            Picker("난이도", selection: $cookDfct) {
                Text("선택").tag("")
                Text("쉬움").tag("쉬움")
                Text("중간").tag("중간")
                Text("어려움").tag("어려움")
            }
            .pickerStyle(.segmented)
        }
    }
}
