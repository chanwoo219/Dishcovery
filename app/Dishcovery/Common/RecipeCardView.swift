import SwiftUI

struct RecipeCardView: View {
    let recipe: Recipe

    var body: some View {
            VStack(alignment: .leading) {

                // ✅ 이미지 처리
                // 사각형이 레이아웃 크기를 결정하고 이미지는 overlay로만 그려서,
                // 원본 사진 비율이 카드 폭에 영향을 주지 못하게 한다 (iOS 버전 무관).
                Rectangle()
                    .fill(Color(.secondarySystemBackground))
                    .frame(height: 180)
                    .frame(maxWidth: .infinity)
                    .overlay(
                        Group {
                            if let path = recipe.imgUrl {
                                AsyncImage(
                                    url: URL(string: (API.baseURL) + path)
                                ) { image in
                                    image
                                        .resizable()
                                        .scaledToFill()
                                } placeholder: {
                                    Color.clear
                                }
                            }
                        }
                    )
                    .clipped()
                    .cornerRadius(12)


                // 제목
                Text(recipe.title ?? "제목 없음")
                    .font(.headline)
                    .lineLimit(1)
                    .padding(.top, 5)

                // 설명 (길이와 무관하게 항상 2줄 높이를 예약해 카드 크기를 통일)
                Text(recipe.rcpDisc ?? "")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
                    .frame(height: 38, alignment: .topLeading)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()
            .background(Color(.secondarySystemBackground))
            .cornerRadius(15)
            .shadow(radius: 3)
        }
}
