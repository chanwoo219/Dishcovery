import SwiftUI


struct DescriptionAndIngredientsView: View {
    @Binding var rcpDisc: String
    @Binding var recipeIngr: String


    var body: some View {
        Group {
            Text("요리 소개와 준비물").font(.title2).bold()
            Text("요리소개").font(.headline).bold()
            TextEditor(text: $rcpDisc).frame(minHeight: 80).overlay(RoundedRectangle(cornerRadius: 10).stroke(.gray.opacity(0.3)))
            Text("준비물").font(.headline).bold()
            TextEditor(text: $recipeIngr).frame(minHeight: 80).overlay(RoundedRectangle(cornerRadius: 10).stroke(.gray.opacity(0.3)))
        }
    }
}
