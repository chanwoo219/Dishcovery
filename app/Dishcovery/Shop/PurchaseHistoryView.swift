import SwiftUI

@available(iOS 16.0, *)
struct PurchaseHistoryView: View {
    @StateObject private var viewModel = PurchaseHistoryViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Text("총 \(viewModel.totalQty)개 구매")
                Spacer()
                Text("내 포인트 \(viewModel.myPoint)P")
                    .foregroundColor(.orange)
            }
            .font(.subheadline)
            .padding()

            ScrollView {
                if viewModel.isLoading {
                    ProgressView()
                        .padding(.top, 40)
                } else if viewModel.purchases.isEmpty {
                    Text("아직 구매한 상품이 없습니다.")
                        .foregroundColor(.secondary)
                        .padding(.top, 40)
                } else {
                    VStack(spacing: 12) {
                        ForEach(viewModel.purchases) { purchase in
                            Button {
                                path.append(Page.shopDetail(productId: purchase.productId))
                            } label: {
                                HStack(spacing: 12) {
                                    Rectangle()
                                        .fill(Color(.secondarySystemBackground))
                                        .frame(width: 60, height: 60)
                                        .overlay(
                                            Group {
                                                if let path = purchase.mainImage {
                                                    AsyncImage(url: URL(string: API.baseURL + path)) { image in
                                                        image.resizable().scaledToFill()
                                                    } placeholder: {
                                                        Color.clear
                                                    }
                                                }
                                            }
                                        )
                                        .clipped()
                                        .cornerRadius(8)

                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(purchase.productName)
                                            .font(.subheadline)
                                            .foregroundColor(.primary)
                                        Text("\(purchase.qty)개 · \(purchase.productPoint)P")
                                            .font(.caption)
                                            .foregroundColor(.secondary)
                                        if let date = purchase.purchaseDate {
                                            Text(date)
                                                .font(.caption2)
                                                .foregroundColor(.secondary)
                                        }
                                    }
                                    Spacer()
                                }
                                .padding()
                                .background(Color(.secondarySystemBackground))
                                .cornerRadius(12)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("구매 내역")
        .navigationBarTitleDisplayMode(.inline)
        .task {
            await viewModel.load()
        }
    }
}
