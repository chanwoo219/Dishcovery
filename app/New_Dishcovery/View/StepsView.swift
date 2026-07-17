import SwiftUI


struct StepsView: View {
    @Binding var stepDescriptions: [String]


    var body: some View {
        Group {
            HStack {
                Text("조리 단계").font(.title2).bold()
                Spacer()
                Button("+ 단계 추가") { stepDescriptions.append("") }
            }
            ForEach(stepDescriptions.indices, id: \.self) { index in
                VStack(alignment: .leading) {
                    HStack {
                        Text("Step \(index + 1)")
                        Spacer()
                        if stepDescriptions.count > 1 {
                            Button(role: .destructive) { stepDescriptions.remove(at: index) } label: { Image(systemName: "trash") }
                        }
                    }
                    TextEditor(text: $stepDescriptions[index]).frame(minHeight: 60).overlay(RoundedRectangle(cornerRadius: 10).stroke(.gray.opacity(0.3)))
                }
            }
        }
    }
}
