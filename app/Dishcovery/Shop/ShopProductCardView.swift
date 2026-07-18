import SwiftUI

struct ShopProductCardView: View {
    let product: ShopProduct

    var body: some View {
        VStack(alignment: .leading) {
            Rectangle()
                .fill(Color(.secondarySystemBackground))
                .frame(height: 140)
                .frame(maxWidth: .infinity)
                .overlay(
                    Group {
                        if let path = product.mainImage {
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

            Text(product.productName)
                .font(.headline)
                .lineLimit(1)
                .padding(.top, 5)

            Text("\(product.productPoint)P")
                .font(.subheadline)
                .foregroundColor(.orange)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.secondarySystemBackground))
        .cornerRadius(15)
        .shadow(radius: 3)
    }
}
