import SwiftUI

@available(iOS 16.0, *)
struct ShopListView: View {
    @StateObject private var viewModel = ShopListViewModel()
    @Binding var path: NavigationPath

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                TextField("상품 검색", text: $viewModel.searchText)
                    .textFieldStyle(.roundedBorder)
                    .onSubmit {
                        viewModel.search()
                    }

                Button {
                    viewModel.search()
                } label: {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.orange)
                }
            }
            .padding(.horizontal)
            .padding(.top, 8)

            HStack {
                Spacer()
                Text("내 포인트 \(viewModel.myPoint)P")
                    .font(.subheadline)
                    .foregroundColor(.orange)
            }
            .padding(.horizontal)
            .padding(.top, 4)

            ScrollView {
                if viewModel.isLoading {
                    ProgressView()
                        .padding(.top, 40)
                } else if viewModel.products.isEmpty {
                    Text("표시할 상품이 없습니다.")
                        .foregroundColor(.secondary)
                        .padding(.top, 40)
                } else {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(viewModel.products) { product in
                            Button {
                                path.append(Page.shopDetail(productId: product.productId))
                            } label: {
                                ShopProductCardView(product: product)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                    }
                    .padding()
                }
            }
        }
        .task {
            await viewModel.loadProducts()
            await viewModel.loadMyPoint()
        }
    }
}
