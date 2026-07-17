import SwiftUI

struct RecipeStep: Codable {
    let stepOrder: Int?
    let stepDescription: String?
}

struct RecipeDetailView: View {
    let recipe: Recipe

    @State private var steps: [RecipeStep] = []

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {

                // 이미지

                if let path = recipe.imgUrl {
                    Rectangle()
                        .fill(Color(.secondarySystemBackground))
                        .frame(height: 250)
                        .frame(maxWidth: .infinity)
                        .overlay(
                            AsyncImage(
                                url: URL(string: (API.baseURL) + path)
                            ) { image in
                                image.resizable().scaledToFill()
                            } placeholder: {
                                Color.clear
                            }
                        )
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

                // 조리시간 / 난이도 / 해시태그
                HStack(spacing: 16) {
                    Label(recipe.cookTime, systemImage: "clock")
                    Label(recipe.cookDfct, systemImage: "gauge")
                }
                .font(.subheadline)
                .foregroundColor(.secondary)

                if let tag = recipe.recipeTag, !tag.isEmpty {
                    Text(tag)
                        .font(.subheadline)
                        .foregroundColor(.blue)
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
                if !steps.isEmpty {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("🍽 조리 순서")
                            .font(.headline)

                        ForEach(steps.indices, id: \.self) { index in
                            VStack(alignment: .leading, spacing: 6) {
                                Text("Step \(index + 1)")
                                    .font(.subheadline)
                                    .fontWeight(.bold)

                                Text(steps[index].stepDescription ?? "")
                                    .font(.body)
                                    .foregroundColor(.secondary)
                            }
                            .padding()
                            .background(Color(.secondarySystemBackground))
                            .cornerRadius(12)
                        }
                    }

                    Divider()
                }

                Spacer()
            }
            .padding()
        }
        .navigationTitle("레시피 상세")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await loadSteps()
        }
    }

    private func loadSteps() async {
        guard let url = URL(string: "\(API.baseURL)/api/recipes/\(recipe.recipeId)/steps") else { return }
        do {
            let (data, _) = try await URLSession.shared.data(from: url)
            let decoded = try JSONDecoder().decode([RecipeStep].self, from: data)
            steps = decoded.sorted { ($0.stepOrder ?? 0) < ($1.stepOrder ?? 0) }
        } catch {
            print("🔴 조리 단계 불러오기 실패:", error.localizedDescription)
        }
    }
}
