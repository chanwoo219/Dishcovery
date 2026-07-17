import SwiftUI


struct TipAndTagView: View {
    @Binding var recipeTip: String
    @Binding var recipeTag: String


    var body: some View {
        Group {
            Text("마무리").font(.title2).bold()
            Text("팁 &amp; 노하우").font(.headline).bold()
            TextEditor(text: $recipeTip).frame(minHeight: 60).overlay(RoundedRectangle(cornerRadius: 10).stroke(.gray.opacity(0.3)))
            Text("해시태그").font(.headline).bold()
            TextField("해시태그 (#파스타 #집들이)", text: $recipeTag).textFieldStyle(.roundedBorder)
        }
    }
}
