import SwiftUI

struct RecipeDetailView: View {
    let recipe: Recipe

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {

                // 이미지
                        
                if let path = recipe.imgUrl {
                    AsyncImage(
                        url: URL(string: (API.baseURL) + path)
                    ) { image in
                        image.resizable().scaledToFill()
                    } placeholder: {
                        Color.gray.opacity(0.2)
                    }
                    .onAppear {
                        print("🟢 상세 이미지 URL:", (API.baseURL) + path)
                    }
                    .frame(height: 250)
                    .clipped()
                    .cornerRadius(12)
                }


                // 제목
                Text(recipe.title)
                    .font(.title)
                    .fontWeight(.bold)

                // 짧은 설명
                if let desc = recipe.rcpDisc {
                    Text(desc)
                        .font(.body)
                        .foregroundColor(.gray)
                }

                Divider()

                // 재료
                VStack(alignment: .leading, spacing: 8) {
                    Text("🧂 재료")
                        .font(.headline)
                    Text(recipe.recipeIngr)
                        .font(.body)
                }

                Divider()

                // 조리 방법
                VStack(alignment: .leading, spacing: 8) {
                    Text("🍳 조리 tip")
                        .font(.headline)
                    Text(recipe.recipeTip)
                        .font(.body)
                }
                Divider()
                
                // 조리 단계
                if let steps = recipe.stepList, !steps.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("🍽 조리 순서")
                            .font(.headline)

                        ForEach(steps.indices, id: \.self) { index in
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Step \(index + 1)")
                                    .font(.subheadline)
                                    .fontWeight(.bold)

                                Text(steps[index])
                                    .font(.body)
                                    .foregroundColor(.secondary)
                            }
                            .padding()
                            .background(Color.gray.opacity(0.05))
                            .cornerRadius(12)
                        }
                    }
                } else if let singleStep = recipe.stepDescription, !singleStep.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("🍽 조리 순서")
                            .font(.headline)
                        VStack(alignment: .leading, spacing: 6) {
                            Text("Step 1")
                                .font(.subheadline)
                                .fontWeight(.bold)
                            Text(singleStep)
                                .font(.body)
                                .foregroundColor(.secondary)
                        }
                        .padding()
                        .background(Color.gray.opacity(0.05))
                        .cornerRadius(12)
                    }
                }

                Divider()
                
                Spacer()
            }
            .padding()
        }
        .navigationTitle("레시피 상세")
        .navigationBarTitleDisplayMode(.inline)
    }
}
