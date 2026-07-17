import SwiftUI

struct RecipeCardView: View {
    let recipe: Recipe

    var body: some View {
            VStack(alignment: .leading) {

                // ✅ 이미지 처리
                if let path = recipe.imgUrl {
                   
                    AsyncImage(
                            url: URL(string: (API.baseURL) + path)
                        ) { image in
                            image
                                .resizable()
                                .scaledToFill()
                        } placeholder: {
                            Rectangle()
                                .fill(Color(.secondarySystemBackground))
                        }
                        .onAppear {
                            print("🟢 이미지 URL:",  (API.baseURL) + path)
                        }
                    
                        .frame(height: 180)
                        .clipped()
                        .cornerRadius(12)
                    } else {
                        Rectangle()
                            .fill(Color(.secondarySystemBackground))
                            .frame(height: 180)
                            .cornerRadius(12)
                    }


                // 제목
                Text(recipe.title ?? "제목 없음")
                    .font(.headline)
                    .padding(.top, 5)

                // 설명
                Text(recipe.rcpDisc ?? "")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }
            .padding()
            .background(Color(.secondarySystemBackground))
            .cornerRadius(15)
            .shadow(radius: 3)
        }
}
